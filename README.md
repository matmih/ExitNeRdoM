# The ExitNeRdoM library
This repository contains a source code and library of ExitNeRdoM methodology and the accompanying reproducible toy example.
The library is a result of research presented at: https://arxiv.org/abs/2501.01209

## ExitNeRdoM setup
To set up the methodology:
* create the working directory
* place the CLUS library (it's dependencies and the .jar file - available in __CLUS_library folder__) into working directory
* the ExitNeRdoM library (it's dependencies and the .jar file - available at the dist folder of __Source_and_lib_ExitNeRdoM__) into working directory
* the Settings file into working directory (available in the __Settings_and_other_documents__ folder)
* the preferences file into working directory (available in the __Settings_and_other_documents__ folder)
* the input data into working directory (neural network models - available at the __Resulting_models_and_outputs/representations/arff/trained__)
* set the correct paths in the Settings file
* run the methodology using ```java -jar -XmxMemAmount SettingsName.set numThreads >out.txt```

## Reproducible toy example
The toy example consists of relating penultimate layers of the two small mlps trained on the WDBC dataset. 
The studied layers contain 32 neurons each. 
* The ExitNeRdoM creates 2  files per neuron, one describing individual neuron activations, and the other describing neuron activations in interactions
* 128 redescription sets (.rr files) are created as output
* Trained mlp models are available at __Resulting_models_and_outputs/representations/models/trained__
* 3 sets of results obtained by the ExitNeRdoM from these networks are available at the directories:
    * One using 20 iterations to compute each redescription set (available at __Resulting_models_and_outputs/redescriptions/WDBC_network_redescriptions_Settings1__)
    * One using 2 iterations to compute each redescription set (available at __Resulting_models_and_outputs/redescriptions/WDBC_network_redescriptions_Settings2__)
    * One using 1 iteration to compute each redescription set (available at __Resulting_models_and_outputs/redescriptions/WDBC_network_redescriptions_Settings3__)
 
## Empirical comparison of network training and analyses
* MLPs were trained using:
   *__CPU__: AMD Ryzen 9 5900X 12-Core Processor (12 cores, 24 threads)
   *__GPU__: RTX 3080 (8704 cores)
* Execution times:
   * Time taken to train __mlp-s-111__ (test_acc = 0.953): 1.50 seconds
   * Time taken to train __mlp-s-112__ (test_acc = 0.953): 1.08 seconds
   * Toy example networks are small, thus a subset of computational resources was used during training.
* ExitNeRdoM output was computed using:
  *__CPU__: Intel Xeon W-1370 (8 cores, 16 threads)
* Execution times (using 14 threads):
  * Time take to obtain __WDBC_network_redescriptions_Settings1__ (48/64 individual, 51/64 interaction, with accuracy >=0.5): 12 minutes, 7 seconds (281,78 x mlp train time)      
  * Time take to obtain __WDBC_network_redescriptions_Settings2__ (48/64 individual, 49/64 interaction, with accuracy >=0.5 ): 1 minute, 10 seconds (27,13 x mlp train time)
  * Time take to obtain __WDBC_network_redescriptions_Settings3__ (48/64 individual, 48/64 interaction, with accuracy >=0.5): 37 seconds (14.34 x mlps train time)
 
## Execution times considerations
* ExitNeRdoM is currently run only on CPU threads, increasing the number of threads from 14 will linearly speed up the computation
* ExitNeRdoM creates and writes in multiple output files (128 in this example, __2*TotalnumNeurons__ in general)
* Neural networks are trained on CPU and GPU, where GPUs can run thousands of threads in parallel.
* ExitNeRdoM analyses of toy example networks was performed on the MLP activations obtained on the train set. As it was shown in the referenced manuscript, the methodology can be executed on much smaller data subset without significant loss of accuracy. The manuscript presents results obtained on the test set (normally 20%-30% the size of the whole dataset). Thus, one could expect >3x reduction in execution times on such inputs. 

## Results considerations
* MLP training provides a number of deep learning models (two predictive MLP models in the toy example). These can be considered as black boxes performing predefined tasks. 
* ExitNeRdoM output provides understandable redescriptions describing neurons of selected layers (penultimate in the toy example) of input networks on the individual and interaction level (it unpacks the boxes and obtains potentially large number of information)
* The amount of information ExitNeRdoM obtains depends on the input models and on the algorithmic parameters.
* Algoritmic parameters can cause variations in execution times, since the level of detail about the boxes can be very high (very detailed analyses) or very low but accurate.
* In addition to obtaining local predictors, understanding neuron and groups of neuron functions, the methdology allows utilising additional sources of information to perform scientific analyses.  
