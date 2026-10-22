/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Matej
 */
public class ComputeRedescriptionPermutationTests {
     public static void main(String [] args){
        ApplicationSettings appset=new ApplicationSettings();
        appset.readSettings(new File(args[0]));
        
        int dataset = Integer.parseInt(args[1]);
        //0 - WDBC, 1 - ADNI, 2 - Cifar10
        
        DataSetCreator datJ=new DataSetCreator(appset.viewInputPaths, appset.outFolderPath,appset);
        Mappings fid=new Mappings();
        
        if(appset.system.equals("windows"))
            fid.createIndex(appset.outFolderPath+"\\Jinput.arff");
        else
            fid.createIndex(appset.outFolderPath+"/Jinput.arff");
        
        ArrayList<Redescription> allReds = new ArrayList<>(10000);
        
        int numNeurons = 512; //Cifar10
       // int numNeurons = 24; //ADNI
       // int numNeurons = 64; //WDBC
       
       if(dataset == 0)
           numNeurons = 64;
       else if(dataset == 1)
           numNeurons = 24;
       
        String file = "";

      if(dataset == 2){
        for(int neur = 0;neur<numNeurons;neur++){
                RedescriptionLoader load = new RedescriptionLoader(fid, datJ);
                file = "redescriptionsDLCifar1_2ForestDLInterpretabilityMW cifar10resnet18s0neuron"+neur+" 0.6 JSType 0minSupp 10.rr";
                ArrayList<Redescription> reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
                
                file = "redescriptionsDLCifar1_2ForestDLInterpretabilityMWAllcifar10resnet18s0neuron"+neur+"0.6 JSType 0minSupp 10.rr";
                reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
                
                file = "redescriptionsDLCifar1_2ForestDLInterpretabilityMW cifar10resnet18s1000neuron"+neur+" 0.6 JSType 0minSupp 10.rr";
                reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
                
                file = "redescriptionsDLCifar1_2ForestDLInterpretabilityMWAllcifar10resnet18s1000neuron"+neur+"0.6 JSType 0minSupp 10.rr";
                reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
        } //Cifar10
      }
      else if(dataset == 1){
        for(int neur = 0;neur<numNeurons;neur++){
                RedescriptionLoader load = new RedescriptionLoader(fid, datJ);
                file = "redescriptionsDLWDBC1_2ForestDLInterpretabilityMW adnimlps0neuron"+neur+" 0.8 JSType 0minSupp 10.rr";
                ArrayList<Redescription> reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
                
                file = "redescriptionsDLWDBC1_2ForestDLInterpretabilityMWAlladnimlps0neuron"+neur+"0.8 JSType 0minSupp 10.rr";
                reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
                
                file = "redescriptionsDLWDBC1_2ForestDLInterpretabilityMW adnimlps1000neuron"+neur+" 0.8 JSType 0minSupp 10.rr";
                reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
                
                file = "redescriptionsDLWDBC1_2ForestDLInterpretabilityMWAlladnimlps1000neuron"+neur+"0.8 JSType 0minSupp 10.rr";
                reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
        }  //Adni
      }
      else if(dataset == 0){
        for(int neur = 0;neur<numNeurons;neur++){
                RedescriptionLoader load = new RedescriptionLoader(fid, datJ);
                file = "redescriptionsDLWDBC1_2ForestDLInterpretabilityMW wdbcmlps0neuron"+neur+" 0.8 JSType 0minSupp 10.rr";
                ArrayList<Redescription> reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
                
                file = "redescriptionsDLWDBC1_2ForestDLInterpretabilityMWAllwdbcmlps0neuron"+neur+"0.8 JSType 0minSupp 10.rr";
                reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
                
                file = "redescriptionsDLWDBC1_2ForestDLInterpretabilityMW wdbcmlps1000neuron"+neur+" 0.8 JSType 0minSupp 10.rr";
                reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
                
                file = "redescriptionsDLWDBC1_2ForestDLInterpretabilityMWAllwdbcmlps1000neuron"+neur+"0.8 JSType 0minSupp 10.rr";
                reds = load.loadRedescriptions(new File(appset.outFolderPath+"Trained_0_1000\\"+file));
                allReds.addAll(reds);
        }  //WDBC
      }
      
        System.out.println("All reds size: "+allReds.size());
        ArrayList<Integer> numWorse = new ArrayList<>();
        
        for(int i=0;i<allReds.size();i++) numWorse.add(0);
        
        String outP = appset.outFolderPath+"\\RandomNetworksPermutation"+"\\";
        int j=1;
        DataSetCreator datJR = null;
        ArrayList<String> randPaths = new ArrayList<>();   
        
        for(int i=0;i<1000;i++){
            if(dataset == 2){
                randPaths.add(outP+"resnet18-s-"+j+".arff");
                randPaths.add(outP+"resnet18-s-"+(1000+j)+".arff"); //Cifar10
            }
            else{
                randPaths.add(outP+"mlp-s-"+j+".arff");
                randPaths.add(outP+"mlp-s-"+(1000+j)+".arff"); //WDBC & ADNI
            }
                datJR = new DataSetCreator(randPaths, outP,appset);
                for(int z=0;z<allReds.size();z++){
                    allReds.get(z).ComputeValidationStatistics(datJ, datJR, fid);
                    if(allReds.get(z).JSAll>=allReds.get(z).JS) numWorse.set(z, numWorse.get(z)+1);
                }
                randPaths.clear();
            System.out.println("Run: "+(i+1));
            j++;
        }
       
        try{
        FileWriter out = new FileWriter("EmpPvals.txt");
        int OK1 = 0, OK5=0;
        double val = 0.0;
        
        for(int i=0;i<numWorse.size();i++){
            val = (((double)numWorse.get(i)+1)/1001.0);
            out.write(val+" ");
            if(val<=0.01) OK1++;
            if(val<=0.05) OK5++;
          }
        
        out.write("\n");
        out.write("Num significant 0.01 "+OK1+"\n");
        out.write("Num significant 0.05 "+OK5+"\n");
        out.write("Total: "+numWorse.size());
        out.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
