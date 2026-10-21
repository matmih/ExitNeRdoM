"""
CIFAR10 Neural Network Training & Representation Extraction

This script is designed to train neural networks on the CIFAR10 dataset and extract representations from these models. The script includes functions for data preparation, model training, representation extraction, and saving the results in ARFF format.
"""

from typing import Optional, Any, List, Tuple, Dict, Callable
import argparse
import os
from pathlib import Path
import pickle
import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import Subset
from torch.utils.data.dataloader import DataLoader
from torchvision.datasets import CIFAR10
from torchvision import transforms as T
from torchmetrics.functional import accuracy

import pytorch_lightning as pl
from pytorch_lightning.callbacks import LearningRateMonitor
from pytorch_lightning.callbacks.progress.tqdm_progress import TQDMProgressBar
from pytorch_lightning.loggers import CSVLogger, TensorBoardLogger

from torch.optim.lr_scheduler import OneCycleLR

# =============================================================================
# CONFIGURATION
# =============================================================================

SEED = 1234
DATASET_NAME = "cifar10"
ROOT_DIR = Path(__file__).parent.parent
DATA_DIR = ROOT_DIR / "Input_and_processed_data"
DATASET_DIR = DATA_DIR / "datasets" / "CIFAR10"
DATASET_DIR.mkdir(parents=True, exist_ok=True)

OUTPUT_DIR = ROOT_DIR / "Resulting_models_and_outputs" / "CIFAR10"
MODELS_DIR = OUTPUT_DIR / "models"
LOG_DIR = OUTPUT_DIR / "logs"
REPRESENTATION_DIR = OUTPUT_DIR / "representations"
SCORES_DIR = OUTPUT_DIR / "scores"
PKL_DIR = REPRESENTATION_DIR / 'pkl'
ARFF_DIR = REPRESENTATION_DIR / 'arff'

BATCH_SIZE = 16
NUM_WORKERS = int(os.cpu_count() / 2)
MAX_EPOCHS = 10
NORMALIZE_INPUT_DATA = True
REMOVE_NANS = True

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
def cifar10_normalization() -> Callable:
    """"""
    return T.Normalize(
        mean=[x / 255.0 for x in [125.3, 123.0, 113.9]],
        std=[x / 255.0 for x in [63.0, 62.1, 66.7]],
    )


def get_cifar10_dataloaders() -> Tuple[DataLoader, DataLoader]:
    """Get CIFAR10 dataset and create train and test data loaders."""
    # Transforms
    train_transforms = T.Compose(
        [
            T.RandomCrop(32, padding=4),
            T.RandomHorizontalFlip(),
            T.ToTensor(),
            cifar10_normalization(),
        ]
    )

    test_transforms = T.Compose(
        [
            T.ToTensor(),
            cifar10_normalization(),
        ]
    )

    # PyTorch datasets and loaders
    trainset = CIFAR10(root=DATASET_DIR, train=True,
                       download=True, transform=train_transforms)

    indices = torch.arange(1000)
    testset = Subset(CIFAR10(root=DATASET_DIR, train=False,
                     download=True, transform=test_transforms), indices)
    
    return (
        DataLoader(trainset, batch_size=BATCH_SIZE, shuffle=True, 
                  num_workers=NUM_WORKERS, persistent_workers=True),
        DataLoader(testset, batch_size=1000, shuffle=False, 
                  num_workers=NUM_WORKERS, persistent_workers=True)
    )

# =============================================================================
# MODEL DEFINITION
# =============================================================================

def resnet18() -> Tuple[nn.Module, List[Tuple[nn.Module, str]]]:
    from torchvision.models import resnet18
    model = resnet18(pretrained=False, num_classes=10)
    model.conv1 = nn.Conv2d(3, 64, kernel_size=(3, 3),
                            stride=(1, 1), padding=(1, 1), bias=False)
    model.maxpool = nn.Identity()
    return model

# =============================================================================
# PYTORCH LIGHTNING MODULE
# =============================================================================

# Pytorch Lightning module factory
def getPLModule(model_class: nn.Module) -> pl.LightningModule:
    
    class PLModule(pl.LightningModule):
        model_cls = model_class
        def __init__(self, seed:Optional[int]=None, lr:float=0.1, mean_val:float=0, std_val:float=0.1):
            super().__init__()
            self.save_hyperparameters()
            self.mean_val = mean_val
            self.std_val = std_val
            self.model = model_class()
            self._set_seed(seed)
        
        def _set_seed(self, seed: Optional[int]):
            if seed is not None:
                state = torch.get_rng_state()
                torch.manual_seed(seed)
                self.model.apply(self.get_reset_weights_method(mean_val=self.mean_val, std_val=self.std_val))
                torch.set_rng_state(state)

        @staticmethod
        def get_reset_weights_method(mean_val:float=0, std_val:float=0.1):
            norm_vals = {
                'mean': mean_val,
                'std': std_val,
            }
            def _reset_weights(m: nn.Module):
                if isinstance(m, (nn.Conv2d, nn.Linear)):
                    nn.init.normal_(m.weight, mean=norm_vals['mean'], std=norm_vals['std'])
                    if m.bias is not None:
                        nn.init.constant_(m.bias, 0)
                elif isinstance(m, (nn.BatchNorm2d, nn.GroupNorm)):
                    nn.init.constant_(m.weight, 1)
                    nn.init.constant_(m.bias, 0)
                # else:
                #     print(type(m))
            return _reset_weights

        def forward(self, x):
            out = self.model(x)
            return F.log_softmax(out, dim=1)

        def training_step(self, batch, batch_idx):
            x, y = batch
            logits = self(x)
            loss = F.nll_loss(logits, y)
            self.log("train_loss", loss)
            return loss

        def evaluate(self, batch, stage=None):
            x, y = batch
            logits = self(x)
            loss = F.nll_loss(logits, y)
            preds = torch.argmax(logits, dim=1)
            acc = accuracy(preds, y, task="multiclass", num_classes=10)

            if stage:
                self.log(f"{stage}_loss", loss, prog_bar=True)
                self.log(f"{stage}_acc", acc, prog_bar=True)

        def validation_step(self, batch, batch_idx):
            self.evaluate(batch, "val")

        def test_step(self, batch, batch_idx):
            self.evaluate(batch, "test")

        def configure_optimizers(self):
            optimizer = torch.optim.SGD(
                self.parameters(),
                lr=self.hparams.lr,
                momentum=0.9,
                weight_decay=5e-4,
            )
            print('MAX_EPOCHS', self.trainer.max_epochs)
            steps_per_epoch = 60000 // BATCH_SIZE + 1
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
        devices=1 if torch.cuda.is_available() else None,
        logger=[CSVLogger(save_dir=LOG_DIR), TensorBoardLogger(save_dir=LOG_DIR)],
        callbacks=[LearningRateMonitor(logging_interval="step"), TQDMProgressBar(refresh_rate=10)],
        deterministic=True
    )
    return trainer


# =============================================================================
# REPRESENTATION EXTRACTION
# =============================================================================
def get_activation(name, activation: Dict[str, Any], activation_type='output', flatten=False, adaptiveAveragePool=False):
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
    parser = argparse.ArgumentParser(description="CIFAR10 Neural Network Training & Representation Extraction")
    parser.add_argument("-r", "--redo", action="store_true", help="Redo the experiment from scratch")
    args = parser.parse_args()

    print("=" * 100)
    print("CIFAR10 Neural Network Training & Representation Extraction")
    print("=" * 100)
    
    # 1. Prepare data
    print("#"*100, "\n1. Loading and preparing CIFAR10 dataset...")
    train_loader, test_loader = get_cifar10_dataloaders()
    
    # Prepare models
    print("#"*100, "\n2. Preparing NN models...")
    models_config = {}
    models_config.update({
            f"resnet18-s-{seed}":{"model_class": resnet18, "seed": seed, "mean_val": 0, "std_val": 0.1}
            for seed in range(0, 2000)
        })
    models_config.update({
            f"resnet18-s-{1000*seed + 1}":{"model_class": resnet18, "seed": 1000*seed + 1, "mean_val": 0, "std_val": 0.1}
            for seed in range(0, 100)
        })
    models_config.update({
            f"resnet18-s-{1000*seed}":{"model_class": resnet18, "seed": 1000*seed, "mean_val": 0, "std_val": 0.01}
            for seed in range(0, 100)
        })
    paths = get_paths(models_config)

    # Extract representations
    print("#"*100, "\n3. Train, evaluate and get representations...")
    for model_name in models_config:
        model_path = paths['model'][model_name]
        pkl_path = paths['pkl'][model_name]
        arff_path = paths['arff'][model_name]
        score_path = paths['score'][model_name]
        if model_path.exists() and pkl_path.exists() and arff_path.exists() and score_path.exists() and (not args.redo):
            # PKL representations exist, don't redo scoring
            continue

        # Model initialization
        pl.seed_everything(SEED)
        seed = models_config[model_name]['seed']

        mean_val = models_config[model_name]['mean_val']
        std_val = models_config[model_name]['std_val']                
        plmodel = getPLModule(models_config[model_name]['model_class'])(lr=0.1, seed=seed, mean_val=mean_val, std_val=std_val)    

        trainer = create_trainer()

        # Train - only first NN in 1000, with 0 index
        if (seed % 1000) == 0:
            trainer.fit(plmodel, train_dataloaders=train_loader, val_dataloaders=test_loader)

        # Set hooks
        activation = {}
        hooks = []
        layers = [(plmodel.model.fc, 'input')]
        for layer_name, (module, activation_type) in enumerate(layers):
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