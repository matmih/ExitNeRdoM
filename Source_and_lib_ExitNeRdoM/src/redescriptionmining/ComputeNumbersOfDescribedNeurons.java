/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static redescriptionmining.ComputeAttributePairwiseAssociationsInterpretability.ENCODING;

/**
 *
 * @author Matej
 */
public class ComputeNumbersOfDescribedNeurons {
    public static void main(String [] args){
        String inputFolderPath = args[0].trim();
        String inputFolderPathTmp = "", inputFolderPathTrained = "", inputFolderPathRandom = "";
        
        int i=0, countTr = 0, countRnd = 0;
        
        ArrayList<Integer> countsTrained = new ArrayList<>();
        ArrayList<Integer> countsRandom = new ArrayList<>();
        
        BufferedReader reader = null;
        
        for(int numIter = 1;numIter<=50;numIter++){
            inputFolderPathTmp = inputFolderPath+"/Run"+numIter;
            
            countTr = 0;
            countRnd = 0;
            
            inputFolderPathTrained = inputFolderPathTmp+"/Trained_"+(i)+"_"+(i + 1000);
            inputFolderPathRandom = inputFolderPathTmp+"/Random_"+(i+1)+"_"+(i+1000+1);
            
            System.out.println(inputFolderPathTrained);
            System.out.println(inputFolderPathRandom);
            File[] files = new File(inputFolderPathTrained).listFiles();
            
             for(File f:files){
                 if(f.getAbsolutePath().contains("InterpretabilityMWAll")) continue;
                 
                 try{
                       Path path = Paths.get(f.getAbsolutePath());//Paths.get(input.getAbsolutePath());
                       reader = Files.newBufferedReader(path,ENCODING);
                       String line = null;
                       
                       while((line = reader.readLine()) != null){
                           
                           if(line.contains("JS: ")){
                               countTr++;
                               break;
                           }
                           
                       }
                       
                 }
                 catch(Exception e){
                     e.printStackTrace();
                 }
                 
             }
             
             
             files = new File(inputFolderPathRandom).listFiles();
            
             for(File f:files){
                 if(f.getAbsolutePath().contains("InterpretabilityMWAll")) continue;
                 
                 try{
                       Path path = Paths.get(f.getAbsolutePath());//Paths.get(input.getAbsolutePath());
                       reader = Files.newBufferedReader(path,ENCODING);
                       String line = null;
                       
                       while((line = reader.readLine()) != null){
                           
                           if(line.contains("JS: ")){
                               countRnd++;
                               reader.close();
                               break;
                           }
                           
                       }
                       reader.close();
                 }
                 catch(Exception e){
                     e.printStackTrace();
                 }
                 
             }
            
            //count the true numbers
            
            countsTrained.add(countTr);
            countsRandom.add(countRnd);
            i+=2000;
        }
        
        try{
               FileWriter fw = new FileWriter("CountsIndividual.txt");
               for(int it=0;it<countsTrained.size();it++)
                   if((it+1)<countsTrained.size())
                      fw.write(countsTrained.get(it)+",");
                   else fw.write(countsTrained.get(it)+"\n");
               
               for(int it=0;it<countsRandom.size();it++)
                   if((it+1)<countsRandom.size())
                      fw.write(countsRandom.get(it)+",");
                   else fw.write(countsRandom.get(it)+"\n");
               fw.close();
               
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        
    }
    
}
