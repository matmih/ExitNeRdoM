"""
ADNI Neural Network Training & Representation Extraction

This script is designed to train neural networks on the ADNI dataset and extract representations from these models. The script includes functions for data preparation, model training, representation extraction, and saving the results in ARFF format.
"""

from typing import Optional, Any, List, Tuple, Dict
import argparse
import os
from pathlib import Path
import pickle
from scipy.io import arff
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data.dataloader import Dataset, DataLoader
from torchmetrics.functional import accuracy

import pytorch_lightning as pl
from pytorch_lightning.callbacks import LearningRateMonitor
from pytorch_lightning.callbacks.progress import TQDMProgressBar
from pytorch_lightning.loggers import CSVLogger, TensorBoardLogger

from torch.optim.lr_scheduler import OneCycleLR


# =============================================================================
# CONFIGURATION
# =============================================================================

SEED = 1234
DATASET_NAME = "adni"
ROOT_DIR = Path(__file__).parent.parent
DATA_DIR = ROOT_DIR / "Input_and_processed_data"
DATASET_DIR = DATA_DIR / "datasets"
DATASET_FILE_PATH = DATASET_DIR / "ADNI" / "ADNIMERGE-clean-2.arff"

OUTPUT_DIR = ROOT_DIR / "Resulting_models_and_outputs" / "ADNI"
MODELS_DIR = OUTPUT_DIR / "models"
LOG_DIR = OUTPUT_DIR / "logs"
REPRESENTATION_DIR = OUTPUT_DIR / "representations"
SCORES_DIR = OUTPUT_DIR / "scores"
PKL_DIR = REPRESENTATION_DIR / 'pkl'
ARFF_DIR = REPRESENTATION_DIR / 'arff'

BATCH_SIZE = 16
NUM_WORKERS = int(os.cpu_count() / 2)
MAX_EPOCHS = 50
NORMALIZE_INPUT_DATA = True
REMOVE_NANS = True

CATS = ['CN','AD','LMCI','EMCI','SMC']

# Setup reproducibility
pl.seed_everything(SEED, workers=True)
torch.backends.cudnn.deterministic = True
torch.backends.cudnn.benchmark = False

# =============================================================================
# HANDLING FILE PATHS
# =============================================================================

def get_paths(models_config: Dict[str, Any]) -> Dict[str, Dict[str, Path]]:
    """
    Get the file paths for the models and their representations.

    Args:
        models_config (Dict[str, Any]): A dictionary with the names of the models as keys.

    Returns:
        Dict[str, Dict[str, Path]]: A dictionary containing three dictionaries,
            each mapping model names to paths.
    """
    model_names = models_config.keys()
    
    paths = {
        'model': {
            model_name: MODELS_DIR / f"{model_name}.ckpt" for model_name in model_names
        },
        'score': {
            model_name: SCORES_DIR / f"{model_name}.ckpt" for model_name in model_names
        },
        'pkl': {
            model_name: PKL_DIR / f"{model_name}.pkl" for model_name in model_names
        },
        'arff': {
            model_name: ARFF_DIR / f"{model_name}.arff" for model_name in model_names
        }
    }
    return paths


# =============================================================================
# DATA HANDLING
# =============================================================================

def prepare_adni_data()-> Tuple[pd.DataFrame, pd.DataFrame, pd.Series, pd.Series]:
    """Get and prepare ADNI dataset, split it, and return train and test data.

    Returns:
        tuple: A tuple of four elements (X_train, X_test, y_train, y_test) where:
            - X_train (pandas.DataFrame): The training features
            - X_test (pandas.DataFrame): The testing features
            - y_train (pandas.Series): The training labels
            - y_test (pandas.Series): The testing labels
    """
    # Load dataset
    df = pd.DataFrame(arff.loadarff(DATASET_FILE_PATH)[0])

    cat2int_map = {CATS[i]:i for i in range(len(CATS))}
    
    # Prepare input and output data
    input_columns = df.columns[1:]
    output_columns = 'DX_bl'

    removed_rows = df[df['DX_bl']==b'?'].index
    df = df.drop(removed_rows) # remove unlabeled sample

    X, y = df[input_columns], df[output_columns]
    y = y.map(lambda x: cat2int_map[str(x)[2:-1]])

    # Split data
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.3, random_state=SEED)

    # Do normalization od the data
    if NORMALIZE_INPUT_DATA:
        norm = {}
        # Calculate normalization parameters on train data
        for k in X.keys():
            norm[k] = {'mean':X_train[k].mean(), 'std':X_train[k].std()}
        # Apply normalization to train and test data
        for k in X.keys():
            X_train.loc[:, k] = (X_train[k]-norm[k]['mean'])/(1e-10+norm[k]['std'])
            X_test.loc[:, k] = (X_test[k]-norm[k]['mean'])/(1e-10+norm[k]['std'])

    # Replace NAN values with ZEROS (a mean value)
    if REMOVE_NANS:
        for k in X.keys():
            X_train.loc[X_train[k].isna(), k] = 0
            X_test.loc[X_test[k].isna(), k] = 0
            
    X_train = np.array(X_train.values)
    X_test = np.array(X_test.values)
    y_train = np.array(y_train.values)
    y_test = np.array(y_test.values)
    
    return X_train, X_test, y_train, y_test


class ClassificationColumnarDataset(Dataset):
    def __init__(self, df, cats, y):
        self.cats = None
        if cats:
            self.dfcats = df[cats] #type: pandas.core.frame.DataFrame
            self.cats = np.stack([c.values for n, c in self.dfcats.items()], axis=1).astype(np.int64) #tpye: numpy.ndarray
        
        self.dfconts = df.drop(cats, axis=1) #type: pandas.core.frame.DataFrame
        self.conts = np.stack([c.values for n, c in self.dfconts.items()], axis=1).astype(np.float32) #tpye: numpy.ndarray
        self.y = torch.tensor(y, dtype=torch.long) # LongTensor je obavezan za CrossEntropyLoss
        # self.y = y.values
        
    def __len__(self): return len(self.y)

    def __getitem__(self, idx):
        if self.cats:
            return [self.cats[idx], self.conts[idx], self.y[idx]]
        else:
            return [self.conts[idx], self.y[idx]]

class TabularDataset(Dataset):
    def __init__(self, X, y):
        # Convert to torch tensors
        self.X = torch.tensor(X, dtype=torch.float32)
        self.y = torch.tensor(y, dtype=torch.long) # LongTensor is mandatory for CrossEntropyLoss
        
    def __len__(self):
        return len(self.X)
    
    def __getitem__(self, idx):
        return self.X[idx], self.y[idx]


# =============================================================================
# MODEL DEFINITION
# =============================================================================

class MLP(nn.Module):
    def __init__(self):
        super().__init__()
        self.flatten = nn.Flatten()
        self.linear1 = nn.Linear(33, 64)
        self.linear2 = nn.Linear(64, 24)
        self.linearL = nn.Linear(24, len(CATS))
        self.layers = [(self.linearL, 'input')]
    
    def forward(self, X):
        X = self.flatten(X)
        X = F.relu(self.linear1(X))
        X = F.relu(self.linear2(X))
        X = self.linearL(X)
        return X

# =============================================================================
# PYTORCH LIGHTNING MODULE
# =============================================================================

def getPLModule(model_class: nn.Module):
    class PLModule(pl.LightningModule):
        model_cls = model_class
        def __init__(self, seed:Optional[int]=None, lr:float=0.001, loss_fn=nn.CrossEntropyLoss()):
            super().__init__()
            self.lr = lr
            self.loss_fn = loss_fn
            self.save_hyperparameters()
            if seed!=None:
                state = torch.get_rng_state()
                torch.manual_seed(seed)
            self.model = self.model_cls()
            self.layers = self.model.layers
            if seed!=None:
                torch.set_rng_state(state)

        def forward(self, x):
            out = self.model(x)
            # return F.log_softmax(out, dim=1)
            return out

        def training_step(self, batch, batch_idx):
            x, y = batch
            logits = self(x)
            loss = self.loss_fn(logits, y)
            self.log("train_loss", loss)
            return loss

        def evaluate(self, batch, stage=None):
            x, y = batch
            logits = self(x)
            loss = self.loss_fn(logits, y)
            preds = torch.argmax(logits, axis=1)
            acc = accuracy(preds, y, task="multiclass", num_classes=len(CATS))

            if stage:
                self.log(f"{stage}_loss", loss, prog_bar=True)
                self.log(f"{stage}_acc", acc, prog_bar=True)

        def validation_step(self, batch, batch_idx):
            self.evaluate(batch, "val")

        def test_step(self, batch, batch_idx):
            self.evaluate(batch, "test")

        def configure_optimizers(self):
            print("#########LR", self.hparams.lr)
            optimizer = torch.optim.SGD(
                self.parameters(),
                lr=self.hparams.lr,
                momentum=0.9,
                weight_decay=5e-4,
            )
            print('MAX_EPOCHS', self.trainer.max_epochs)
            steps_per_epoch = 641 // BATCH_SIZE + 1
            scheduler_dict = {
                "scheduler": OneCycleLR(
                    optimizer,
                    self.lr,
                    epochs=self.trainer.max_epochs,
                    steps_per_epoch=steps_per_epoch,
                ),
                "interval": "step",
            }
            return {"optimizer": optimizer, "lr_scheduler": scheduler_dict}
    return PLModule
   

# =============================================================================
# TRAINING UTILITIES
# =============================================================================

def create_trainer():
    trainer = pl.Trainer(
        max_epochs=MAX_EPOCHS,
        # accelerator="auto",
        # devices=1 if torch.cuda.is_available() else None,
        accelerator="cuda",
        devices=[2],
        logger=[CSVLogger(save_dir=LOG_DIR), TensorBoardLogger(save_dir=LOG_DIR)],
        callbacks=[LearningRateMonitor(logging_interval="step"), TQDMProgressBar(refresh_rate=10)],
        deterministic=True
    )
    return trainer

def create_data_loaders(X_train, X_test, y_train, y_test) -> Tuple[DataLoader, DataLoader]:
    """Create train and test data loaders."""
    train_ds = TabularDataset(X_train, y_train)
    test_ds = TabularDataset(X_test, y_test)
    
    return (
        DataLoader(train_ds, batch_size=BATCH_SIZE, shuffle=True, 
                  num_workers=NUM_WORKERS, persistent_workers=True),
        DataLoader(test_ds, batch_size=BATCH_SIZE, shuffle=False, 
                  num_workers=NUM_WORKERS, persistent_workers=True)
    )


# =============================================================================
# REPRESENTATION EXTRACTION
# =============================================================================
def get_activation(name, activation: Dict, activation_type='output', flatten=False, adaptiveAveragePool=False):
    activation[name] = []
    def hook(model, input, output):
        if adaptiveAveragePool:
            aep = torch.nn.AdaptiveAvgPool2d((1,1))
            output = aep(output)
        if flatten:
            if activation_type=='input': # use layer's input activations
                d = input[0].detach()
            elif activation_type=='output': # use layer's output activations
                d = output.detach()
            else:
                raise
            d = d.view(d.shape[0], -1).to('cpu').numpy()
            activation[name].append(d)
        else:
            d = output.detach().to('cpu').numpy()
            activation[name].append(d)
    return hook


# =============================================================================
# EXPORT UTILITIES
# =============================================================================

def arff_attr_naming(x, id_name='ID'):
    if x == 'index':
        return id_name
    tags = [s.lower().capitalize() for s in x.replace(' ','_').split('_')]
    tags[0] = tags[0].lower()
    return ''.join(tags)


def save_df2arff(df: pd.DataFrame, filename: str|Path, relation_name: str,
                 attr_id_name: str='id', attr_naming_fun=arff_attr_naming):
    """Save ARFF file from pandas dataframe.

    Args:
        df (pd.DataFrame): Input dataframe to save as ARFF.
        filename (str|Path): Output path for the ARFF file.
        relation_name (str): Name of the relation attribute in the ARFF file.
        attr_id_name (str, optional): Name of the ID attribute. Defaults to 'id'.
        attr_naming_fun (Callable[[str], str], optional): Function to format column names. Defaults to arff_attr_naming.

    Returns:
        None
    """
    if attr_naming_fun:
        df_view = df.reset_index().rename(
            lambda x: attr_naming_fun(x, id_name=attr_id_name), axis='columns')
    else:
        df_view = df.reset_index()

    df_view[attr_id_name] = df_view[attr_id_name].apply(lambda x: "'{}'".format(x))

    arff_attr = []
    arff_attr.append('@Attribute {col_name} string\n'.format(col_name=attr_id_name))

    for col_name in df_view.columns[1:]:
        if df_view[col_name].dtype == 'O' or df_view[col_name].dtype == 'object':
            col_vc = df_view[col_name].value_counts()
            if len(col_vc)<=1:
                print('Single value attribute: {}'.format(col_name))
            arff_attr.append('@Attribute {col_name} {vals}\n'.format(
                col_name=col_name, vals='string'))
        else:
            arff_attr.append('@Attribute {col_name} numeric\n'.format(col_name=col_name))

    df_view = df_view.fillna('?')
    
    size = len(df_view)
    batch = 1000
    i = 0
    while i<size:
        if not Path(filename).exists():
            with open(filename, 'w') as f:
                f.write("@RELATION '{}'\n".format(relation_name))
                f.write('\n')
                f.writelines(arff_attr)
                f.write('\n')
                f.write('@Data\n')
                np.savetxt(f, df_view[i:i+batch].values, fmt='%s', delimiter=',')
        else:
            with open(filename, 'a') as f:
                np.savetxt(f, df_view[i:i+batch].values, fmt='%s', delimiter=',')
        i += batch

                             
def export(df: pd.DataFrame, path: str|Path, relation_name=None)->str:
    """
    Export a pandas DataFrame to an ARFF file.

    Args:
        df (pd.DataFrame): The input DataFrame.
        path (str|Path): The base path for the output files.
        relation_name (str, optional): The name of the relation attribute in the ARFF file. If not provided, it defaults to the base name of the file.

    Returns:
        str: Path where the data was saved.
    """
    Path(str(path)).parent.mkdir(exist_ok=True, parents=True)
    p = str(path)
    if relation_name==None:
        relation_name=Path(p).stem
    save_df2arff(df, p, relation_name=relation_name, attr_naming_fun=arff_attr_naming)
    return p


# =============================================================================
# MAIN EXECUTION
# =============================================================================

def main():
    parser = argparse.ArgumentParser(description="ADNI Neural Network Training & Representation Extraction")
    parser.add_argument("-r", "--redo", action="store_true", help="Redo the experiment from scratch")
    args = parser.parse_args()

    print("=" * 100)
    print("ADNI Neural Network Training & Representation Extraction")
    print("=" * 100)
    
    # 1. Prepare data
    print("#"*100, "\n1. Loading and preparing ADNI dataset...")
    X_train, X_test, y_train, y_test = prepare_adni_data()
    train_loader, test_loader = create_data_loaders(X_train, X_test, y_train, y_test)
    
    # Prepare models
    print("#"*100, "\n2. Preparing NN models...")
    models_config = {}
    models_config.update({
            f"mlp-s-{seed}": {"model_class": MLP, "seed": seed}
            for seed in range(0, 2000)
        })
    models_config.update({
            f"mlp-s-{1000*seed}": {"model_class": MLP, "seed": 1000*seed}
            for seed in range(0, 100)
        })
    models_config.update({
            f"mlp-s-{1000*seed + 1}": {"model_class": MLP, "seed": 1000*seed + 1}
            for seed in range(0, 100)
        })
    paths = get_paths(models_config)

    # Train, evaluate and get representations
    print("#"*100, "\n3. Train, evaluate and get representations...")
    for model_name in models_config:
        model_path = paths['model'][model_name]
        pkl_path = paths['pkl'][model_name]
        arff_path = paths['arff'][model_name]
        score_path = paths['score'][model_name]
        
        if model_path.exists() and pkl_path.exists() and arff_path.exists() and score_path.exists() and (not args.redo):
            continue

        # Model initialization
        pl.seed_everything(SEED)
        seed = models_config[model_name]['seed']
        plmodel = getPLModule(models_config[model_name]['model_class'])(lr=0.03, seed=seed)
        trainer = create_trainer()

        # Train - only first in 1000
        if (seed % 1000) == 0:
            trainer.fit(plmodel, train_dataloaders=train_loader, val_dataloaders=test_loader)

        # Set hooks
        activation = {}
        hooks = []
        for layer_name, (module, activation_type)  in enumerate(plmodel.model.layers):
            hooks.append(
                module.register_forward_hook(
                    get_activation(layer_name, activation, activation_type=activation_type, flatten=True)))
            
        # Run test to get test representations and scores
        trainer = create_trainer()
        score = trainer.test(plmodel, dataloaders=test_loader)
        
        # Save scores
        score_path.parent.mkdir(parents=True, exist_ok=True)
        with open(score_path, 'w') as file:
            file.write(str(score[0]['test_acc']))

        # Remove hooks
        for h in hooks:
            h.remove()
            hooks = {}

        # Save models
        model_path.parent.mkdir(parents=True, exist_ok=True)
        trainer.save_checkpoint(model_path)

        # Concatenate representations - and save pkl
        ks = list(activation.keys())
        for a in ks:
            activation[a] = np.concatenate(activation[a], 0)
            # Save activations/features
            Path(pkl_path).parent.mkdir(parents=True, exist_ok=True)
            pickle.dump(activation[a], open(pkl_path, "wb"))
            del(activation[a])

        # Convert to arff format
        df = pd.DataFrame(pd.read_pickle(pkl_path))
        fn = "".join([DATASET_NAME, model_name.replace('-', '')])
        df.columns = [''.join('{}neuron{}'.format(fn, i).split('_')).lower()
                    for i in range(df.shape[1])]
        relation_name="-".join([DATASET_NAME, model_name])
        export(df, arff_path, relation_name=relation_name)
        print("#"*100, "\nDone!")


if __name__=="__main__":
    main()