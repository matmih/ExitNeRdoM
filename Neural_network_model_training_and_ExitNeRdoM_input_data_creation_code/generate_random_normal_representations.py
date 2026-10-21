import os
from pathlib import Path
import numpy as np
import random
import arff
import numpy as np
from scipy.stats import foldnorm
from scipy.optimize import minimize
from tqdm import tqdm

def clean_numeric_vals(data: np.array):
    # Calculate mean and standard deviation of values in d
    # Convert to object array to avoid dtype incompatibility with mixed types
    data_obj = data.astype(object)

    # Extract only numeric values, skipping strings (assumed to be in first column or non-numeric)
    numeric_vals = []
    for val in data_obj:
        try:
            num_val = float(val)
            numeric_vals.append(num_val)
        except (ValueError, TypeError):
            continue

    if len(numeric_vals) == 0:
        raise ValueError("No numeric values found in datasets for computing statistics.")
    return np.array(numeric_vals)
    

def get_folded_normal_vals(folded_data):
    folded_data = clean_numeric_vals(folded_data)
    # Fit the data to calculate the parameters
    # We fix floc=0 because a standard folded normal distribution starts at 0
    c_est, loc_est, scale_est = foldnorm.fit(folded_data, floc=0)

    # Recover the original normal distribution parameters (mu and sigma)
    est_sigma = scale_est
    est_mu = c_est * scale_est
    return est_mu, est_sigma


def generate_2000_datasets_normal(file1_path, file2_path, output_directory,
                                  num_datasets=2000, samples_per_dataset=1000,
                                  per_attribute=False, use_abs=True, use_zero_clipping=False):

    with open(file1_path, 'r', encoding='utf-8') as f1:
        dataset1 = arff.load(f1)
    with open(file2_path, 'r', encoding='utf-8') as f2:
        dataset2 = arff.load(f2)
   
    if not os.path.exists(output_directory):
            os.makedirs(output_directory)

    attributes = dataset1['attributes']  
    data1 = dataset1['data']
    data2 = dataset2['data']

    d1 = np.array(data1)
    d2 = np.array(data2)
    d = np.concat([d1[:, 1:].flatten(), d2[:, 1:].flatten()], axis=0)

    d1_strings = [str(row[0]).strip("'\"") for row in data1 if row is not None]
   
    d2_strings = []
    for i in range(samples_per_dataset):
        d2_strings.append(d1_strings[i % len(d1_strings)])

    mean_val, std_val = get_folded_normal_vals(d)

    print(f"Normal distribution parameters: mean = {mean_val:.6f}, std = {std_val:.6f}")

    # Prepare attribute statistics for sampling from normal distributions
    attr_stats = {}
    for idx, (attr_name, attr_type) in enumerate(attributes):
        if idx == 0:
            continue  
            
        if isinstance(attr_type, str) and attr_type.upper() in ['NUMERIC', 'REAL', 'INTEGER']:
            if per_attribute:
                vals1 = [row[idx] for row in data1 if row[idx] is not None]
                vals2 = [row[idx] for row in data2 if row[idx] is not None]

                all_vals = vals1 + vals2
                if not all_vals:
                    continue
                attr_stats[idx] = ['NUMERIC', np.mean(all_vals), np.std(all_vals)]
            else:
                attr_stats[idx] = ['NUMERIC', mean_val, std_val]

        else:
            cats = list(set(attr_type)) if isinstance(attr_type, list) else []
            attr_stats[idx] = ('NOMINAL', cats)


    for i in tqdm(range(1, num_datasets + 1)):
        new_data = []
       
        for sample_idx in range(samples_per_dataset):
            row = []
            cisti_tekst = d2_strings[sample_idx]
            row.append(f"{cisti_tekst}")
           
            for idx in range(1, len(attributes)):
                attr_info = attr_stats[idx]
                if attr_info[0] == 'NUMERIC':
                    _, _mean, _std = attr_info
                    val = random.gauss(_mean, _std)
                    if use_abs:
                        val = abs(val)
                    if use_zero_clipping:
                       if val < 0: val = 0.0 # Clip negative values to 0 for numeric attributes
                    row.append(val)
                else:
                    _, cats = attr_info
                    row.append(random.choice(cats) if cats else None)
                   
            new_data.append(row)

        replica_structure = {
            'description': f'Replica {i}',
            'relation': f'replica_{i}',
            'attributes': attributes,
            'data': new_data
        }
       
        model_name = Path(file1_path).stem.split("-")[0]

        output_filename = os.path.join(output_directory, f"{model_name}-s-{i}.arff")
        with open(output_filename, 'w', encoding='utf-8') as out_f:
            arff.dump(replica_structure, out_f)


if __name__ == "__main__":
    for per_attribute, dir_name in [(True, "random-norm-per-attribute-arff"), (False, "random-norm-all-attributes-arff")]:
        generate_2000_datasets_normal(
            '../Resulting_models_and_outputs/WDBC/representations/arff/mlp-s-0.arff',
            '../Resulting_models_and_outputs/WDBC/representations/arff/mlp-s-1000.arff',
            f'../Resulting_models_and_outputs/WDBC/representations/{dir_name}',
            2000,
            171,
            per_attribute=per_attribute
        )

        generate_2000_datasets_normal(                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
            '../Resulting_models_and_outputs/CIFAR10/representations/arff/resnet18-s-0.arff',
            '../Resulting_models_and_outputs/CIFAR10/representations/arff/resnet18-s-1000.arff',
            f'../Resulting_models_and_outputs/CIFAR10/representations/{dir_name}',
            2000,
            1000,
            per_attribute=per_attribute
        )

        generate_2000_datasets_normal(
            '../Resulting_models_and_outputs/ADNI/representations/arff/mlp-s-0.arff',
            '../Resulting_models_and_outputs/ADNI/representations/arff/mlp-s-1000.arff',
            f'../Resulting_models_and_outputs/ADNI/representations/{dir_name}',
            2000,
            275,
            per_attribute=per_attribute
        )