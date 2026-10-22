/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static redescriptionmining.SettingsReader.ENCODING;

/**
 *
 * @author Matej
 */
public class RedescriptionLoader {
    
    Mappings m;
    DataSetCreator d;
    
    public RedescriptionLoader(Mappings fid, DataSetCreator dat){
         m = fid;
         d = dat;
    }
    
    public ArrayList<Redescription> loadRedescriptions(File input){
    
         ArrayList<Redescription> reds = new ArrayList<>();
         
         BufferedReader reader;
         
         String q1 = "", q2= "";
         double JS = 0.0, pval = 1.0;
         TIntHashSet support = new TIntHashSet(), union = new TIntHashSet();
         TIntHashSet validation = new TIntHashSet();
         
           try {
                  Path path =Paths.get(input.getAbsolutePath());
                  reader = Files.newBufferedReader(path,ENCODING);
                  String line = null;
                  int count=0, coveredSup = 0, coveredUn  = 0;
                  
                  while ((line = reader.readLine()) != null){
                        if(line.contains("W1R:")){
                            String tmp[] = line.split("W1R:");
                            q1 = tmp[1].trim();
                        }
                        
                        if(line.contains("W2R:")){
                            String tmp[] = line.split("W2R:");
                            q2 = tmp[1].trim();
                        }
                         
                        if(line.contains("JS:")){
                            String tmp[] = line.split("JS:");
                            JS = Double.parseDouble(tmp[1].trim());
                        }
                         
                        if(line.contains("p-value :")){
                               String tmp[] = line.split("p-value :");
                               pval = Double.parseDouble(tmp[1].trim());
                          }
                        
                        if(line.contains("Covered examples (intersection): ")){
                            coveredSup = 1;
                            continue;
                        }
                        
                        if(line.contains("Union elements:")){
                            coveredUn = 1;
                            continue;
                        }
                        
                        if(coveredSup == 1){
                            String tmp[] = line.split(" ");
                            for(String el:tmp){
                                support.add(m.exampleId.get(el));
                            }
                            coveredSup = 0;
                        }
                        
                        if(coveredUn == 1){
                            String tmp[] = line.split(" ");
                            for(String el:tmp){
                                union.add(m.exampleId.get(el));
                            }
                            coveredUn = 0;
                            
                        /*System.out.println(q1);
                        System.out.println(q2);
                        System.out.println(JS);
                        System.out.println(pval);
                        System.out.println(support);
                        System.out.println(union);*/
                        
                        Redescription r = new Redescription(q1,q2,JS,m,d,0,0,1, 1.0);
                        
                        //r.closeInterval(d, m);
                        
                        r.viewElementsLists.get(0).get(0).computeSuportValidation(d, m);
                        r.viewElementsLists.get(1).get(0).computeSuportValidation(d, m);
                        
                        //System.out.println("Elements test: "+r.viewElementsLists.get(0).get(0).elementsTest.size()+" "+r.viewElementsLists.get(1).get(0).elementsTest.size());
                        
                        r.viewElementsLists.get(0).get(0).elements = r.viewElementsLists.get(0).get(0).elementsTest;
                        r.viewElementsLists.get(1).get(0).elements = r.viewElementsLists.get(1).get(0).elementsTest;
                        
                        TIntIterator it = r.viewElementsLists.get(0).get(0).elements.iterator();
                        
                        while(it.hasNext()){
                            int e = it.next();
                            if(r.viewElementsLists.get(1).get(0).elements.contains(e))
                                validation.add(e);
                        } 
                 
                 /* System.out.println("Velicine supporta: ");
                  System.out.println(validation.size()+" "+support.size());*/
                        
                  if(validation.size() == support.size())
                      System.out.println("Jednaka velicina supporta");
                  else System.out.println("RAZLICITA VELICINA SUPPORTA");
                  
                  it = validation.iterator();
                  int equal = 1;
                  
                  while(it.hasNext()){
                      int el = it.next();
                      if(!support.contains(el)){
                          equal = 0; break;
                      }
                  }
                     
                  System.out.println("Support OK: "+equal);
                   reds.add(r);
                   
                  support.clear();
                  union.clear();
                  validation.clear();
                  JS = 0.0; 
                  pval = 1.0;
                            
                        }
                        
                        
                  }
                  
                        
                 reader.close();
           }
           catch(Exception e){
               e.printStackTrace();
           }
         
         return reds;
        
    }
}
