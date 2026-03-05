# The ExitNeRdoM library
This repository contains a source code and library of ExitNeRdoM methodology and the accompanying reproducible use-case.
The library is a result of research presented at: https://arxiv.org/abs/2501.01209

## ExitNeRdoM setup
To set up the methodology:
* create the working directory
* place the CLUS library (it's dependencies and the .jar file - available in CLUS_library folder) into working directory
* the ExitNeRdoM library (it's dependencies and the .jar file - available at the dist folder of Source_and_lib_ExitNeRdoM) into working directory
* the Settings file into working directory (available in the Settings_and_other_documents folder)
* the preferences file into working directory (available in the Settings_and_other_documents folder)
* the input data into working directory (neural network models - available at the Resulting_models_and_outputs/representations/arff/trained)
* set the correct paths in the Settings file
* run the methodology using ```java -jar -XmxMemAmount SettingsName.set numThreads >out.txt```

## Reproducible use-case
The use-case consists of relating penultimate layers of the two small mlps trained on the WDBC dataset. 
The studied layers contain 32 neurons each. 
* The ExitNeRdoM creates 2  files per neuron, one describing individual neuron activations, and the other describing neuron activations in interactions
* 128 redescription sets (.rr files) are created as output
* Trained mlp models are available at Resulting_models_and_outputs/representations/models/trained
* 3 sets of results obtained by the ExitNeRdoM from these networks are available at the directories:
    * One using 20 iterations to compute each redescription set (available at Resulting_models_and_outputs/redescriptions/WDBC_network_redescriptions_Settings1)
    * One using 2 iterations to compute each redescription set (available at Resulting_models_and_outputs/redescriptions/WDBC_network_redescriptions_Settings2)
    * One using 1 iteration to compute each redescription set (available at Resulting_models_and_outputs/redescriptions/WDBC_network_redescriptions_Settings3)
