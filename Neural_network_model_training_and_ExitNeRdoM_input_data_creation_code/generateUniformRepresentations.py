import os
import numpy as np
import pandas as pd
import random
import arff 

def generate_2000_datasets(file1_path, file2_path, output_directory, num_datasets=2000, samples_per_dataset=1000, dataset = 0):
    with open(file1_path, 'r', encoding='utf-8') as f1:
        dataset1 = arff.load(f1)
    with open(file2_path, 'r', encoding='utf-8') as f2:
        dataset2 = arff.load(f2)
    
    attributes = dataset1['attributes']  
    data1 = dataset1['data']
    data2 = dataset2['data']
    
    if not os.path.exists(output_directory):
        os.makedirs(output_directory)

        
    d1_strings = [str(row[0]).strip("'\"") for row in data1 if row is not None]
    
    d2_strings = []
    for i in range(samples_per_dataset):
        d2_strings.append(d1_strings[i % len(d1_strings)])

    attr_ranges = {}
    for idx, (attr_name, attr_type) in enumerate(attributes):
        if idx == 0:
            continue  
            
        if isinstance(attr_type, str) and attr_type.upper() in ['NUMERIC', 'REAL', 'INTEGER']:
            vals1 = [row[idx] for row in data1 if row[idx] is not None]
            vals2 = [row[idx] for row in data2 if row[idx] is not None]
            
            global_min = min(min(vals1), min(vals2))
            global_max = max(max(vals1), max(vals2))
            attr_ranges[idx] = ('NUMERIC', global_min, global_max)
        else:
            cats = list(set(attr_type)) if isinstance(attr_type, list) else []
            attr_ranges[idx] = ('NOMINAL', cats)

    print(f"Pokreće se generiranje {num_datasets} replika...")

    for i in range(1, num_datasets + 1):
        new_data = []
        
        for sample_idx in range(samples_per_dataset):
            row = []
            cisti_tekst = d2_strings[sample_idx]
            row.append(f"{cisti_tekst}")
            
            for idx in range(1, len(attributes)):
                attr_info = attr_ranges[idx]
                if attr_info[0] == 'NUMERIC':
                    _, g_min, g_max = attr_info
                    row.append(random.uniform(g_min, g_max))
                else:
                    _, cats = attr_info
                    row.append(random.choice(cats) if cats else None)
                    
            new_data.append(row)
            
        replica_structure = {
            'description': f'Generirana replika {i}',
            'relation': f'replica_{i}',
            'attributes': attributes,
            'data': new_data
        }
        
        output_filename = os.path.join(output_directory, f"resnet18-s-{i}.arff")
        if dataset < 2:
            output_filename = os.path.join(output_directory, f"mlp-s-{i}.arff")
        with open(output_filename, 'w', encoding='utf-8') as out_f:
            arff.dump(replica_structure, out_f)
            
        if i % 200 == 0:
            print(f"Generirano: {i}/{num_datasets} datoteka.")

    print(f"Uspješno spremljeno {num_datasets} datoteka u: '{output_directory}'")

if __name__ == "__main__":
    #dataset: 0 - WDBC, 1 - ADNI, 2 - cifar10
    dataset = 2
    if dataset == 0:
         generate_2000_datasets('mlp-s-0.arff','mlp-s-1000.arff','path\\RandomNetworksPermutation', 2000, 171,dataset) #WDBC
    if dataset == 1:
         generate_2000_datasets('mlp-s-0.arff','mlp-s-1000.arff','path\\RandomNetworksPermutation', 2000, 275, dataset) #ADNI
    if dataset == 2:
       generate_2000_datasets('resnet18-s-0.arff','resnet18-s-1000.arff','path\\RandomNetworksPermutation', 2000, 1000, dataset) #cifar10 
