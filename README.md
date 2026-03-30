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
* MLP training execution times (20 epochs):   
   * CPU + GPU:
        * __CPU__: AMD Ryzen 9 5900X 12-Core Processor (12 cores, 24 threads)
        * __GPU__: RTX 3080 (8704 cores)
        * Execution times (mlps train time):
            * Time taken to train __mlp-s-111__ (test_acc = 0.953): 1.50 seconds
            * Time taken to train __mlp-s-112__ (test_acc = 0.953): 1.08 seconds
            * Toy example networks are small, thus a subset of computational resources were used during training.
   * CPU:
      * __CPU__: Intel Xeon W-1370 (8 cores, 16 threads):
      * Execution times (mlpsCPU train time):
         * Time taken to train __mlp-s-111__ : 1.38 seconds
         * Time taken to train __mlp-s-112__ : 1.16 seconds
      * Execution times (loading, mlp model training, storing results - mlpsAll time):
         * Time taken: 3.21 seconds   
* ExitNeRdoM execution times (loading, computation, storing results):
   * __CPU__: Intel Xeon W-1370 (8 cores, 16 threads)
   * Execution times (using 14 threads):
     * Time taken to obtain __WDBC_network_redescriptions_Settings1__ (48/64 individual, 51/64 interaction, with accuracy >=0.5): 12 minutes, 7 seconds (281.78 x mlp train time, 286.22 x mlpCPU train time, 226.48 x mlpAll)      
     * Time taken to obtain __WDBC_network_redescriptions_Settings2__ (48/64 individual, 49/64 interaction, with accuracy >=0.5 ): 1 minute, 10 seconds (27.13 x mlp train time, 27.56 x mlpCPU train time, 21.81 x mlpAll)
     * Time taken to obtain __WDBC_network_redescriptions_Settings3__ (48/64 individual, 48/64 interaction, with accuracy >=0.5): 37 seconds (14.34 x mlps train time, 14,57 x mlpCPU train time, 11.53 x mlpAll)
     * Time taken to obtain __WDBC_network_redescriptions_Settings4__ (47/64 individual, 47/64 interaction, with accuracy >=0.5): 31 seconds (12.02 x mlps train time, 12.20 x mlpCPU train time, 9.66 x mlpAll)
   * Execution times (using 16 threads):
     * Time taken to obtain __WDBC_network_redescriptions_Settings4_b__ (47/64 individual, 47/64 interaction, with accuracy >=0.5): 23 seconds (8.91 x mlps train time, 9.06 x mlpCPU train time, 7.17 x mlpAll)
 
## Execution times considerations
* The ExitNeRdoM is currently run only on CPU threads, increasing the number of threads from 14 (or 16) will speed up the computation. Maximal number of threads usable on this toy example is 60.
* The ExitNeRdoM creates and writes in multiple output files (128 in this example, __2*TotalnumNeurons__ in general)
* Neural networks are trained on CPU and GPU, where GPUs can run thousands of threads in parallel.
* The ExitNeRdoM was run on the MLPs activations obtained on the test set (around 30% of data samples, Settings1-Settings3).
* Settings4 uses activations obtained on smaller test set (20% of data samples) which further increases the execution speed of this methodology.  

## Results considerations
* MLP training provides a number of deep learning models (two predictive MLP models in the toy example). These can be considered as black boxes performing predefined tasks. 
* ExitNeRdoM output provides understandable redescriptions describing neurons of selected layers (penultimate in the toy example) of input networks on the individual and interaction level (it unpacks the boxes and obtains potentially large number of information)
* The amount of information ExitNeRdoM obtains depends on the input models and on the algorithmic parameters.
* Algoritmic parameters can cause variations in execution times, since the level of detail about the boxes can be very high (very detailed analyses) or very low but accurate.
* In addition to obtaining local predictors, understanding neuron and groups of neuron functions, the methdology allows utilising additional sources of information to perform scientific analyses.

## Binning experiments
* We compared __WDBC_network_redescripitons_Settings4_, where binning was performed using modified two-bin binning, clusteringMode = 1, (47/64 individual, 47/64 interaction, with accuracy >=0.5) with:
* __WDBC_network_redescripitons_Settings4_, where we use standard equal width binning with Freedman-Diaconis rule, clusteringMode = 0, (47/64 individual, 47/64 interaction, with accuracy >=0.5)
* __WDBC_network_redescripitons_Settings4_, where we use equal frequency binning, clusteringMode = 2, (49/64 individual, 48/64 interaction, with accuracy >=0.5)
* __WDBC_network_redescriptions_Settings4_, where we use k-means based binning, clusteringMode = 3, (50/64 individual, 47/64 interaction, with accuracy >=0.5)
