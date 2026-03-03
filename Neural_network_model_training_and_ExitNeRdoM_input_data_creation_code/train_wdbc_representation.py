"""
WDBC Neural Network Training & Representation Extraction

This script is designed to train neural networks on the Wisconsin Breast Cancer Diagnostic (WDBC) dataset and extract representations from these models. The script includes functions for data preparation, model training, representation extraction, and saving the results in ARFF format.
"""

from typing import Optional, Any, List, Tuple, Dict
import argparse
import os
from pathlib import Path
import requests
import pickle
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
DATASET_NAME = "WDBC"
DATA_DIR = Path(__file__).parent.parent / "Input_and_processed_data"
DATASET_DIR = DATA_DIR / "datasets"
DATASET_DIR.mkdir(parents=True, exist_ok=True)
DATASET_FILE_PATH = DATASET_DIR / "WDBC.dat"
DATASET_URL = "https://ftp.cs.wisc.edu/math-prog/cpo-dataset/machine-learn/cancer/WDBC/WDBC.dat"

OUTPUT_DIR = Path(__file__).parent.parent / "Resulting_models_and_outputs"
MODELS_DIR = OUTPUT_DIR / "models"
LOG_DIR = OUTPUT_DIR / "logs"
REPRESENTATION_DIR = OUTPUT_DIR / "representations"
PKL_DIR = REPRESENTATION_DIR / 'pkl'
ARFF_DIR = REPRESENTATION_DIR / 'arff'

BATCH_SIZE = 64 if torch.cuda.is_available() else 32
NUM_WORKERS = int(os.cpu_count() / 2)
MAX_EPOCHS = 20
NORMALIZE_INPUT_DATA = True

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
            model_name: {
                "init": MODELS_DIR / f"init/{model_name}.ckpt",
                "trained": MODELS_DIR / f"trained/{model_name}.ckpt"
            } for model_name in model_names
        },
        'pkl': {
            model_name: {
                "init": PKL_DIR / f"init/{model_name}.pkl",
                "trained": PKL_DIR / f"trained/{model_name}.pkl"
            } for model_name in model_names
        },
        'arff': {
            model_name: {
                "init": ARFF_DIR / f"init/{model_name}.arff",
                "trained": ARFF_DIR / f"trained/{model_name}.arff"
            } for model_name in model_names
        }
    }
    return paths


# =============================================================================
# DATA HANDLING
# =============================================================================

def prepare_wdbc_data()-> Tuple[pd.DataFrame, pd.DataFrame, pd.Series, pd.Series]:
    """Get and prepare WDBC dataset, split it, and return train and test data.

    Returns:
        tuple: A tuple of four elements (X_train, X_test, y_train, y_test) where:
            - X_train (pandas.DataFrame): The training features
            - X_test (pandas.DataFrame): The testing features
            - y_train (pandas.Series): The training labels
            - y_test (pandas.Series): The testing labels
    """
    # Download data if needed
    if not DATASET_FILE_PATH.exists():
        r = requests.get(DATASET_URL)
        DATASET_DIR.mkdir(exist_ok=True, parents=True)
        with open(DATASET_FILE_PATH,'wb') as f:
            f.write(r.content)

    # Load dataset
    columns = ['id', 'class'] + [f'f{i}' for i in range(1, 31)]
    df = pd.read_csv(DATASET_FILE_PATH, names=columns)
    df['class'] = df['class'].map(lambda x: int(x=='M'))
    
    # Prepare input and output data
    df = df.drop(columns=['id'])
    input_columns = df.columns[1:]
    output_columns = df.columns[:1]
    X, y = df[input_columns], df[output_columns]

    # Split data
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.3, random_state=SEED)

    # Make normalization on train dataset
    norm = {}
    for k in X.keys():
        norm[k] = {'mean':X_train[k].mean(), 'std':X_train[k].std()}

    # Do normalization od the data
    if NORMALIZE_INPUT_DATA:
        norm = {}
        # Calculate normalization parameters on train data
        for k in X.keys():
            norm[k] = {'mean':X_train[k].mean(), 'std':X_train[k].std()}
        # Apply normalization to train and test data
        for k in X.keys():
            X_train[k] = (X_train[k]-norm[k]['mean'])/(1e-10+norm[k]['std'])
            X_test[k] = (X_test[k]-norm[k]['mean'])/(1e-10+norm[k]['std'])
    
    return X_train, X_test, y_train, y_test


class TabularDataset(Dataset):
    """Dataset for tabular data with continuous features only."""
    def __init__(self, X: pd.DataFrame, y: pd.Series):
        self.X = torch.tensor(X.values, dtype=torch.float32)
        self.y = torch.tensor(y.values, dtype=torch.float32)
        
    def __len__(self):
        return len(self.y)

    def __getitem__(self, idx) -> Tuple[torch.Tensor, torch.Tensor]:
        return self.X[idx], self.y[idx]


# =============================================================================
# MODEL DEFINITION
# =============================================================================

class MLP(nn.Module):
    def __init__(self):
        super().__init__()
        self.flatten = nn.Flatten()
        self.linear1 = nn.Linear(30,512)
        self.linear2 = nn.Linear(512,128)
        self.linear3 = nn.Linear(128,32)
        self.linear4 = nn.Linear(32,1)
        self.layers = [(self.linear4, 'input')]
    
    def forward(self, X):
        X = self.flatten(X)
        X = F.relu(self.linear1(X))
        X = F.relu(self.linear2(X))
        X = F.relu(self.linear3(X))
        X = self.linear4(X)
        return torch.sigmoid(X)



# =============================================================================
# PYTORCH LIGHTNING MODULE
# =============================================================================

def getPLModule(model_class: nn.Module):
    
    class PLModule(pl.LightningModule):
        model_cls = model_class
        def __init__(self, seed:Optional[int]=None, lr:float=0.00001, loss_fn=nn.BCELoss()):
            super().__init__()
            self.loss_fn = loss_fn
            self.save_hyperparameters()
            self.model = model_class()
            self._set_seed(seed)
        
        def _set_seed(self, seed: Optional[int]):
            if seed is not None:
                state = torch.get_rng_state()
                torch.manual_seed(seed)
                self.model.apply(self._reset_weights)
                torch.set_rng_state(state)
        
        @staticmethod
        def _reset_weights(m: nn.Module):
            if isinstance(m, nn.Linear):
                nn.init.kaiming_normal_(m.weight, nonlinearity='relu')

        def forward(self, x):
            out = self.model(x)
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
            preds = logits
            acc = accuracy((preds>0.5).to(torch.int64), y.to(torch.int64), task='binary')

            if stage:
                self.log(f"{stage}_loss", loss, prog_bar=True)
                self.log(f"{stage}_acc", acc, prog_bar=True)

        def validation_step(self, batch, batch_idx):
            self.evaluate(batch, "val")

        def test_step(self, batch, batch_idx):
            self.evaluate(batch, "test")

        def configure_optimizers(self):
            print("LR =", self.hparams.lr)
            optimizer = torch.optim.SGD(
                self.parameters(),
                # lr=self.hparams.lr,
                momentum=0.9,
                weight_decay=5e-4,
            )
            print("MAX_EPOCHS =", self.trainer.max_epochs)
            steps_per_epoch = 569 // BATCH_SIZE
            scheduler_dict = {
                "scheduler": OneCycleLR(
                    optimizer,
                    self.hparams.lr,
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
        accelerator="auto",
        devices=1 if torch.cuda.is_available() else None,  # limiting got iPython runs
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
def get_activation(name, activation: set, activation_type='output', flatten=False, adaptiveAveragePool=False):
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
    parser = argparse.ArgumentParser(description="WDBC Neural Network Training & Representation Extraction")
    parser.add_argument("-r", "--redo", action="store_true", help="Redo the experiment from scratch")
    args = parser.parse_args()

    if args.redo:
        # Redo the experiment
        pass
    else:
        # Load the experiment
        pass

    print("=" * 100)
    print("WDBC Neural Network Training & Representation Extraction")
    print("=" * 100)
    
    # 1. Prepare data
    print("#"*100, "\n1. Loading and preparing WDBC dataset...")
    X_train, X_test, y_train, y_test = prepare_wdbc_data()
    train_loader, test_loader = create_data_loaders(X_train, X_test, y_train, y_test)
    
    # Prepare models
    print("#"*100, "\n2. Preparing NN models...")
    models_config = {
        f"mlp-s-{seed}": {"model_class": MLP, "seed": seed}
        for seed in range(111, 113)
    }
    paths = get_paths(models_config)
    print(paths)

    # Train models
    print("#"*100, "\n3. Training NN models...")
    for model_name in models_config:
        path = paths['model'][model_name]["trained"]
        if path.exists() and (not args.redo):
            # Model exist, don't redo model training
            continue
        # Initialization
        pl.seed_everything(SEED)
        seed = models_config[model_name]['seed']
        plmodel = getPLModule(models_config[model_name]['model_class'])(lr=1e-1, seed=seed)
        trainer = create_trainer()
        # Train and save models
        trainer.fit(plmodel, train_dataloaders=train_loader, val_dataloaders=test_loader)
        path.parent.mkdir(parents=True, exist_ok=True)
        trainer.save_checkpoint(path)

    # Extract representations
    state = 'trained'
    print("#"*100, "\n4. Getting representations...")
    for model_name in models_config:
        path = paths['pkl'][model_name][state]
        if path.exists() and (not args.redo):
            # PKL representations exist, don't redo representation extraction
            continue
        # Load pl model
        plcls = getPLModule(models_config[model_name]['model_class'])
        plmodel = plcls.load_from_checkpoint(paths['model'][model_name][state], weights_only=False)
        # Set hooks
        activation = {}
        hooks = []
        for layer_name, (module, activation_type)  in enumerate(plmodel.model.layers):
            hooks.append(
                module.register_forward_hook(
                    get_activation(layer_name, activation, activation_type=activation_type, flatten=True)))
        # Run test to get test representations
        trainer = create_trainer()
        trainer.test(plmodel, dataloaders=test_loader)
        # Remove hooks
        for h in hooks:
            h.remove()
            hooks = {}
        # Concatenate representations
        ks = list(activation.keys())
        for a in ks:
            activation[a] = np.concatenate(activation[a], 0)
            # Save activations/features
            Path(path).parent.mkdir(parents=True, exist_ok=True)
            pickle.dump(activation[a], open(path,"wb"))
            del(activation[a])

    # Convert to arff format
    print("#"*100, "\n5. Converting representations...")
    for model_name in models_config:
        path = paths['arff'][model_name][state]
        if path.exists() and (not args.redo):
            # ARFF representations exist, don't redo representation convertion
            continue
        df = pd.DataFrame(pd.read_pickle(paths['pkl'][model_name][state]))
        fn = "".join([state, DATASET_NAME, model_name.replace('-', '')])
        df.columns = [''.join('{}neuron{}'.format(fn, i).split('_')).lower()
                      for i in range(df.shape[1])]
        relation_name="-".join([state, DATASET_NAME, model_name])
        export(df, path, relation_name=relation_name)
    print("#"*100, "\nDone!")

main()