/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import clus.data.io.ARFFFile;
import clus.data.io.ClusReader;
import clus.data.io.ClusView;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.main.Settings;
import clus.util.ClusException;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.javatuples.Pair;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author matej
 */
public class DataSetCreator {
    public String DataPath;
    public int numExamples;
    public ClusReader cread=null;
    Settings cset;   
    RowData data=null;
    ClusSchema schema=null;
    ArrayList<Integer> W2indexs=new ArrayList<>();

    public DataSetCreator(){
        
    }

    
    public ArrayList<DataSetCreator> createSplit(double percentage){
        
        DataSetCreator train=new DataSetCreator();
        DataSetCreator test=new DataSetCreator();
        //DataSetCreator test=new DataSetCreator(); 
        
        train.W2indexs=this.W2indexs;
        test.W2indexs=this.W2indexs;
      //  test.W2indexs=this.W2indexs;
        
        train.DataPath=this.DataPath;
        test.DataPath=this.DataPath;
       // test.DataPath=this.DataPath;
        
        train.W2indexs=new ArrayList<>();
        train.W2indexs.addAll(this.W2indexs);
        
        test.W2indexs=new ArrayList<>();
        test.W2indexs.addAll(this.W2indexs);
       
        //test.W2indexs=new ArrayList<>();
      //  test.W2indexs.addAll(this.W2indexs);
        
        try{
            train.cread=new ClusReader(train.DataPath,this.cset);
            test.cread=new ClusReader(test.DataPath,this.cset);
          //  test.cread=new ClusReader(test.DataPath,this.cset);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        train.cset = new Settings();
        test.cset = new Settings();
       // test.cset = new Settings();
 
        train.schema = this.schema;
        test.schema = this.schema;
        //test.schema = this.schema;
         train.data=new RowData(schema);
         test.data=new RowData(schema);
        
        RowData datTrain = null;
        RowData datTest = null;
        
        double a=this.numExamples*percentage;
        int numExTrain = (int)a;
        int numExTest = this.numExamples - numExTrain;
        
        Random rand=new Random();
        
        ArrayList<DataTuple> dataList=data.toArrayList();
         ArrayList<DataTuple> dataTrainList=new ArrayList<>();
         
          ArrayList<DataTuple> dataTestList=new ArrayList<>();
        
         HashSet<Integer> indexes=new HashSet<>();
         HashSet<Integer> indexesTest=new HashSet<>();
         
         int count=0;
         
         while(count<numExTrain){
             
             int randomNum = rand.nextInt((dataList.size() - 0) + 0) + 0;
             
             
             if(!indexes.contains(randomNum)){
                 count++;
                 indexes.add(randomNum);
             }
             
         }
         
         for(int i=0;i<dataList.size();i++)
             if(!indexes.contains(i))
                    indexesTest.add(i);
         
         for(int i:indexes){
             dataTrainList.add(dataList.get(i));
         }
         
         for(int i:indexesTest){
             dataTestList.add(dataList.get(i));
         }
         
         
         train.data.setFromList(dataTrainList);
         train.numExamples=numExTrain;
         
         test.data.setFromList(dataTestList);
         test.numExamples=numExTest;
         
         ArrayList<DataSetCreator> res = new ArrayList<>();
         res.add(train); res.add(test);
         
         System.out.println("Num examples train: "+dataTrainList.size());
         System.out.println("Num examples test: "+dataTestList.size());
         
         //return train;
         
         return res;
        
       // return TrainTest;
        
    }
    
    public DataSetCreator(ArrayList<String> inputs, String outFolder, ApplicationSettings appset ){
        this.DataPath=inputs.get(0);
        ArrayList<DataSetCreator> dats=new ArrayList<>();
        for(int i=1;i<inputs.size();i++){
            DataSetCreator dt=new DataSetCreator(inputs.get(i));
            dats.add(dt);
        }
            
        //DataSetCreator d2=new DataSetCreator(input2);
        cset=new Settings();
        String name=outFolder;

       if(appset.system.equals("windows"))
            name+="\\Jinput.arff";
        else 
            name+="/Jinput.arff";

        try{
                  this.readDataset();
        }
        catch(Exception e){
                    e.printStackTrace();
                }
        
         W2indexs.add(this.schema.getNbAttributes()+1);
         int countWind = this.schema.getNbAttributes()+1;
        for(int i=0;i<dats.size();i++){
                try{
                  //this.readDataset();
                  dats.get(i).readDataset();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                if((i+1)<dats.size()){
                    countWind+=dats.get(i).schema.getNbAttributes()-1;
                 W2indexs.add(countWind/*this.schema.getNbAttributes()+1+dats.get(i).schema.getNbAttributes()-1*/);
                }
        }
        
        //System.out.println("Num attributes dataset 1: "+this.schema.getNbAttributes());
        //System.out.println("Num attributes dataset 2: "+d2.schema.getNbAttributes());

         //int numAttributes=schema.getNbAttributes()-1;
        
        // ArrayList<DataTuple> dataList2=d2.data.toArrayList();
         ArrayList<DataTuple> dataList=data.toArrayList();
        // System.out.println("object: "+t.getNominal(d2.data.m_Data[0]));
         int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("lastDouble: "+lastDouble);
         int lastCat=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;

        // int schCount=lastDouble, schCatCount=lastCat;
         int shCount=0, nhCount=0;
         for(int i=0;i<dats.size();i++){
             shCount+=dats.get(i).schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
             nhCount+=dats.get(i).schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         }
         
         System.out.println("Number of numeric attributes in remaining datasets: "+shCount);
         
                for(int j=0;j<data.getNbRows();j++){
                    int l=data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length+shCount;
                    double arow[]=null;
                    int carow[]=null;
                   // System.out.println("Joint length: "+l);
                    int l1=data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length+nhCount;
                    
                    if(l1>0)
                        carow=new int[l1];
                    
                    if(l>0)
                    arow=new double[l];
                    if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                        for(int k=0;k<dataList.get(j).m_Doubles.length;k++){
                            arow[k]=dataList.get(j).m_Doubles[k];
                         }
                    }
                    
                   if(data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                        for(int k=0;k<dataList.get(j).m_Ints.length;k++){
                             carow[k]=dataList.get(j).m_Ints[k];
                        }
                  }
                    
                   /*  System.out.println("last: "+last);
                     System.out.println("dataset2 size: "+dataList2.get(0).m_Doubles.length);
                     System.out.println("arow length: "+arow.length);
                    */
                   int ind=lastDouble;
                   for(int k1=0;k1<dats.size();k1++){
                        DataSetCreator d2=dats.get(k1);
                        ArrayList<DataTuple> dataList2=d2.data.toArrayList();
                    if(d2.data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                         for(int k=0;k<dataList2.get(j).m_Doubles.length;k++){
                             arow[k+ind]=dataList2.get(j).m_Doubles[k];
                             }
                         }
                    
                    if(d2.data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                         for(int k=0;k<dataList2.get(j).m_Ints.length;k++){
                             carow[k+lastCat]=dataList2.get(j).m_Ints[k];
                              }
                          }
                    ind+=d2.schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
                   }
                 dataList.get(j).m_Doubles=arow;
                 dataList.get(j).m_Ints=carow;
               //}
             }
    
     if(appset.useNC.get(0)==true){
         ClusAttrType cat=schema.getAttrType(schema.getNbAttributes()-1);
         ClusAttrType copy=cat.cloneType();
         schema.addAttrType(copy);
         for(int i=schema.getNbAttributes()-2;i>=2;i--){
             ClusAttrType copy1=schema.getAttrType(i-1).cloneType();
             schema.setAttrType(copy1, i);
         }
         
         ClusAttrType copy1=new NumericAttrType("GIS");//schema.getAttrType(2).cloneType();
             schema.setAttrType(copy1, 1);
         
          schema.getAttrType(1).setName("GIS");
         
          int numD=-1;
          double count=0.0;
        for(int j=0;j<data.getNbRows();j++){
            double row[]=null;
            if(dataList.get(j).m_Doubles!=null){
                row=new double[dataList.get(j).m_Doubles.length+1];
                numD=dataList.get(j).m_Doubles.length;
            }
            else
                row=new double[1];
         row[0]=count;//Double.parseDouble(dataList.get(j).m_Objects[0].toString().replace("\"", ""));
         count++;
         
         if(numD>0){
         row[1]=dataList.get(j).m_Doubles[0];
         for(int i=1;i<dataList.get(j).m_Doubles.length;i++)
             row[i+1]=dataList.get(j).m_Doubles[i];
         }
         
         dataList.get(j).m_Doubles=row;
        }
         
        for(int i=0;i<W2indexs.size();i++)
            W2indexs.set(i, W2indexs.get(i)+1);
        
     }           
                
     for(int k=0;k<dats.size();k++){  
         DataSetCreator d2=dats.get(k);
       for(int i=1;i<d2.schema.getNbAttributes();i++){
           ClusAttrType attr = d2.schema.getAttrType(i);
	   ClusAttrType copy = attr.cloneType();
            schema.addAttrType(copy);
          /*if(d2.schema.getAttrType(i).getTypeName().contains("Numeric"))
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(schCount++);//should generalize
          else if(d2.schema.getAttrType(i).getTypeName().contains("Nominal"))
              schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(schCatCount++);*/
        }
     }
     
       try{
              schema.addIndices(0);
       }
       catch(ClusException e){
           e.printStackTrace();
       }
       
                 data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }
    }
    
     public DataSetCreator(ArrayList<String> inputs, String outFolder, ApplicationSettings appset, String outName ){
        this.DataPath=inputs.get(0);
        ArrayList<DataSetCreator> dats=new ArrayList<>();
        for(int i=1;i<inputs.size();i++){
            DataSetCreator dt=new DataSetCreator(inputs.get(i));
            dats.add(dt);
        }
            
        //DataSetCreator d2=new DataSetCreator(input2);
        cset=new Settings();
        String name=outFolder;

       if(appset.system.equals("windows"))
            name+="\\"+outName;
        else 
            name+="/"+outName;

        try{
                  this.readDataset();
        }
        catch(Exception e){
                    e.printStackTrace();
                }
        
         W2indexs.add(this.schema.getNbAttributes()+1);
         int countWind = this.schema.getNbAttributes()+1;
        for(int i=0;i<dats.size();i++){
                try{
                  //this.readDataset();
                  dats.get(i).readDataset();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                if((i+1)<dats.size()){
                    countWind+=dats.get(i).schema.getNbAttributes()-1;
                 W2indexs.add(countWind/*this.schema.getNbAttributes()+1+dats.get(i).schema.getNbAttributes()-1*/);
                }
        }
        
        //System.out.println("Num attributes dataset 1: "+this.schema.getNbAttributes());
        //System.out.println("Num attributes dataset 2: "+d2.schema.getNbAttributes());

         //int numAttributes=schema.getNbAttributes()-1;
        
        // ArrayList<DataTuple> dataList2=d2.data.toArrayList();
         ArrayList<DataTuple> dataList=data.toArrayList();
        // System.out.println("object: "+t.getNominal(d2.data.m_Data[0]));
         int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("lastDouble: "+lastDouble);
         int lastCat=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;

        // int schCount=lastDouble, schCatCount=lastCat;
         int shCount=0, nhCount=0;
         for(int i=0;i<dats.size();i++){
             shCount+=dats.get(i).schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
             nhCount+=dats.get(i).schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         }
         
         System.out.println("Number of numeric attributes in remaining datasets: "+shCount);
         
                for(int j=0;j<data.getNbRows();j++){
                    int l=data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length+shCount;
                    double arow[]=null;
                    int carow[]=null;
                   // System.out.println("Joint length: "+l);
                    int l1=data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length+nhCount;
                    
                    if(l1>0)
                        carow=new int[l1];
                    
                    if(l>0)
                    arow=new double[l];
                    if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                        for(int k=0;k<dataList.get(j).m_Doubles.length;k++){
                            arow[k]=dataList.get(j).m_Doubles[k];
                         }
                    }
                    
                   if(data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                        for(int k=0;k<dataList.get(j).m_Ints.length;k++){
                             carow[k]=dataList.get(j).m_Ints[k];
                        }
                  }
                    
                   /*  System.out.println("last: "+last);
                     System.out.println("dataset2 size: "+dataList2.get(0).m_Doubles.length);
                     System.out.println("arow length: "+arow.length);
                    */
                   int ind=lastDouble;
                   for(int k1=0;k1<dats.size();k1++){
                        DataSetCreator d2=dats.get(k1);
                        ArrayList<DataTuple> dataList2=d2.data.toArrayList();
                    if(d2.data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                         for(int k=0;k<dataList2.get(j).m_Doubles.length;k++){
                             arow[k+ind]=dataList2.get(j).m_Doubles[k];
                             }
                         }
                    
                    if(d2.data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                         for(int k=0;k<dataList2.get(j).m_Ints.length;k++){
                             carow[k+lastCat]=dataList2.get(j).m_Ints[k];
                              }
                          }
                    ind+=d2.schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
                   }
                 dataList.get(j).m_Doubles=arow;
                 dataList.get(j).m_Ints=carow;
               //}
             }
    
     if(appset.useNC.get(0)==true){
         ClusAttrType cat=schema.getAttrType(schema.getNbAttributes()-1);
         ClusAttrType copy=cat.cloneType();
         schema.addAttrType(copy);
         for(int i=schema.getNbAttributes()-2;i>=2;i--){
             ClusAttrType copy1=schema.getAttrType(i-1).cloneType();
             schema.setAttrType(copy1, i);
         }
         
         ClusAttrType copy1=new NumericAttrType("GIS");//schema.getAttrType(2).cloneType();
             schema.setAttrType(copy1, 1);
         
          schema.getAttrType(1).setName("GIS");
         
          int numD=-1;
          double count=0.0;
        for(int j=0;j<data.getNbRows();j++){
            double row[]=null;
            if(dataList.get(j).m_Doubles!=null){
                row=new double[dataList.get(j).m_Doubles.length+1];
                numD=dataList.get(j).m_Doubles.length;
            }
            else
                row=new double[1];
         row[0]=count;//Double.parseDouble(dataList.get(j).m_Objects[0].toString().replace("\"", ""));
         count++;
         
         if(numD>0){
         row[1]=dataList.get(j).m_Doubles[0];
         for(int i=1;i<dataList.get(j).m_Doubles.length;i++)
             row[i+1]=dataList.get(j).m_Doubles[i];
         }
         
         dataList.get(j).m_Doubles=row;
        }
         
        for(int i=0;i<W2indexs.size();i++)
            W2indexs.set(i, W2indexs.get(i)+1);
        
     }           
                
     for(int k=0;k<dats.size();k++){  
         DataSetCreator d2=dats.get(k);
       for(int i=1;i<d2.schema.getNbAttributes();i++){
           ClusAttrType attr = d2.schema.getAttrType(i);
	   ClusAttrType copy = attr.cloneType();
            schema.addAttrType(copy);
          /*if(d2.schema.getAttrType(i).getTypeName().contains("Numeric"))
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(schCount++);//should generalize
          else if(d2.schema.getAttrType(i).getTypeName().contains("Nominal"))
              schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(schCatCount++);*/
        }
     }
     
       try{
              schema.addIndices(0);
       }
       catch(ClusException e){
           e.printStackTrace();
       }
       
                 data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }
    }
    
    
    public DataSetCreator(String input1, String input2, String outFolder ){
 
        DataSetCreator d2=new DataSetCreator(input2);
        this.DataPath=input1;
        cset=new Settings();
        String name=outFolder;

        name+="\\Jinput.arff";

        try{
        this.readDataset();
        d2.readDataset();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //System.out.println("Num attributes dataset 1: "+this.schema.getNbAttributes());
        //System.out.println("Num attributes dataset 2: "+d2.schema.getNbAttributes());
        W2indexs.add(this.schema.getNbAttributes()+1);

        cset=new Settings();

         //int numAttributes=schema.getNbAttributes()-1;
        
         ArrayList<DataTuple> dataList2=d2.data.toArrayList();
         ArrayList<DataTuple> dataList=data.toArrayList();
        // System.out.println("object: "+t.getNominal(d2.data.m_Data[0]));
         int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("lastDouble: "+lastDouble);
         int lastCat=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;

        // int schCount=lastDouble, schCatCount=lastCat;
         
                for(int j=0;j<data.getNbRows();j++){
                    int l=data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length+d2.data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
                    double arow[]=null;
                    int carow[]=null;
                   // System.out.println("Joint length: "+l);
                    int l1=data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length+d2.data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
                    
                    if(l1>0)
                        carow=new int[l1];
                    
                    if(l>0)
                    arow=new double[l];
                    if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                        for(int k=0;k<dataList.get(j).m_Doubles.length;k++){
                            arow[k]=dataList.get(j).m_Doubles[k];
                         }
                    }
                    
                   if(data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                        for(int k=0;k<dataList.get(j).m_Ints.length;k++){
                             carow[k]=dataList.get(j).m_Ints[k];
                        }
                  }
                    

                   /*  System.out.println("last: "+last);
                     System.out.println("dataset2 size: "+dataList2.get(0).m_Doubles.length);
                     System.out.println("arow length: "+arow.length);
                    */
                    if(d2.data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                         for(int k=0;k<dataList2.get(j).m_Doubles.length;k++){
                             arow[k+lastDouble]=dataList2.get(j).m_Doubles[k];
                  }
                    }
                    
                    if(d2.data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                         for(int k=0;k<dataList2.get(j).m_Ints.length;k++){
                             carow[k+lastCat]=dataList2.get(j).m_Ints[k];
                  }
                    }

                 dataList.get(j).m_Doubles=arow;
                 dataList.get(j).m_Ints=carow;
               }
                
       for(int i=1;i<d2.schema.getNbAttributes();i++){
           ClusAttrType attr = d2.schema.getAttrType(i);
	   ClusAttrType copy = attr.cloneType();
            schema.addAttrType(copy);
          /*if(d2.schema.getAttrType(i).getTypeName().contains("Numeric"))
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(schCount++);//should generalize
          else if(d2.schema.getAttrType(i).getTypeName().contains("Nominal"))
              schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(schCatCount++);*/
        }
     
       try{
              schema.addIndices(0);
       }
       catch(ClusException e){
           e.printStackTrace();
       }
       
                 data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }
    }
    
    void initialClustering(String outFolder, ApplicationSettings appset){
          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<DataTuple> newTuples=new ArrayList<>();
        
         int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;

         System.out.println("initial lastDouble: "+lastDouble);
         System.out.println("old attr size: "+schema.getNbAttributes());
         schema.addAttrType(new NumericAttrType("target"));
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);
         System.out.println("new attr size: "+schema.getNbAttributes()); 
         
         int numShufflingStep=Math.min(500, Math.min((int)(0.7*data.getNbRows()),schema.getNbAttributes()));
         
         Random rand=new Random();
         
         System.out.println("Doing initial clustering...");
         System.out.println("data list size: "+dataList.size());
         System.out.println("Num shuffling steps: "+numShufflingStep);
         
         int numIt=0, step=dataList.size()/100;
         
          for(int j=0;j<dataList.size();j++){
              DataTuple tup=dataList.get(j).deepCloneTuple();
              String id=tup.m_Objects[0].toString().replace("\"", "");
              id="\""+id+"new-Init\"";
              tup.m_Objects[0]=id;
              
              for(int i=0;i<numShufflingStep;i++){//should fix
                    int ind=rand.nextInt(dataList.size());
                    DataTuple told=dataList.get(ind);
                    for(int k=0;k<((int)(Math.max(1.0,0.08*schema.getNbAttributes())));k++){
                           int toChange=0;
                           int toChange1=0;
                           
                       for(int nview=0;nview<W2indexs.size()-1;nview++){                     
                           if(nview==0)
                             toChange=rand.nextInt(W2indexs.get(0)-1/*schema.getNbAttributes()-1*/);
                           else{
                               toChange1=rand.nextInt(W2indexs.get(nview)-1);
                               toChange=rand.nextInt(W2indexs.get(nview+1)-W2indexs.get(k)+1); 
                               toChange+=toChange1;
                           }
                        //int toChange1=rand.nextInt(W2indexs.get(0)-1);
                        //int toChange2=rand.nextInt(W2indexs.get(1)-W2indexs.get(0)+1);
                        //toChange2+=toChange1;
                        //this.W2index-1
                        ClusAttrType t=schema.getAttrType(toChange);
                       // System.out.println("attr name: "+t.getTypeName());
                        if(t.getTypeName().contains("Numeric")){
                            int elemInd=t.getArrayIndex();
                            tup.m_Doubles[elemInd]=told.m_Doubles[elemInd];
                        }
                        else if(t.getTypeName().contains("Nominal")){
                            int elemInd=t.getArrayIndex();
                            tup.m_Ints[elemInd]=told.m_Ints[elemInd];
                        }
                       }
                       
                       toChange= rand.nextInt(schema.getNbAttributes()-1-W2indexs.get(W2indexs.size()-1)+1);
                       toChange1= rand.nextInt(W2indexs.get(W2indexs.size()-1)-1);
                       toChange+=toChange1;
                               
                        ClusAttrType t1=schema.getAttrType(toChange);
                       // System.out.println("attr name: "+t.getTypeName());
                        if(t1.getTypeName().contains("Numeric")){
                            int elemInd=t1.getArrayIndex();
                            if(elemInd==0 && appset.networkInit==true)
                                elemInd++;
                            if(elemInd<tup.m_Doubles.length)
                            tup.m_Doubles[elemInd]=told.m_Doubles[elemInd];
                            else{k--; continue;}
                        }
                        else if(t1.getTypeName().contains("Nominal")){
                            int elemInd=t1.getArrayIndex();
                            tup.m_Ints[elemInd]=told.m_Ints[elemInd];
                        }
                        
                    }
                    //System.out.println("done "+(i+1));
              }
              
              newTuples.add(tup);
              
              numIt++;
        if(numIt%step==0)
                System.out.println((((double)numIt/dataList.size())*100)+"% completed...");
                if(numIt==dataList.size())
                    System.out.println("100% completed!");
              
          }
          
          /*System.out.println("dataList ds: "+dataList.get(0).m_Doubles.length);
          System.out.println("newT ds: "+newTuples.get(0).m_Doubles.length);*/
          
          for(int i=0;i<dataList.size();i++){
              double arow[];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
              arow=new double[dataList.get(i).m_Doubles.length+1];
              else
                  arow=new double[1];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                 for(int j=0;j<dataList.get(i).m_Doubles.length;j++)
                    arow[j]=dataList.get(i).m_Doubles[j];
              }
             
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                arow[dataList.get(i).m_Doubles.length]=1.0;
              else
                  arow[0]=1.0;
              dataList.get(i).m_Doubles=arow;
          }
          
          for(int i=0;i<newTuples.size();i++){
              double arow[];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                 arow=new double[newTuples.get(i).m_Doubles.length+1];
              else
                  arow=new double[1];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                     for(int j=0;j<newTuples.get(i).m_Doubles.length;j++){
                         if(j==0 && appset.networkInit==true)
                             arow[j]=data.getNbRows()+i;
                         else
                            arow[j]=newTuples.get(i).m_Doubles[j];
                     }
              }
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                    arow[newTuples.get(i).m_Doubles.length]=0.0;
              else arow[0]=0.0;
              newTuples.get(i).m_Doubles=arow;
          }
          
          for(int i=0;i<newTuples.size();i++)
              dataList.add(newTuples.get(i));
        
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;
        
        if(appset.system.equals("windows"))
            name+="\\JinputInitial.arff";
        else 
            name+="/JinputInitial.arff";
                 
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }       
    }
    
    
    void initialClusteringGen1(String outFolder, ApplicationSettings appset, int numChangedAttrs, Random r){
          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<DataTuple> newTuples=new ArrayList<>();
        
         int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;

         System.out.println("initial lastDouble: "+lastDouble);
         System.out.println("old attr size: "+schema.getNbAttributes());
         schema.addAttrType(new NumericAttrType("target"));
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);
         System.out.println("new attr size: "+schema.getNbAttributes()); 
         
         int numShufflingStep=Math.min(500, Math.min((int)(0.7*data.getNbRows()),schema.getNbAttributes()));
         System.out.println("Doing initial clustering...");
         System.out.println("data list size: "+dataList.size());
         System.out.println("Num shuffling steps: "+numShufflingStep);
         
         int numIt=0, step=dataList.size()/100;
         if(step==0)
             step=1;
         //Random r=new Random();
         
          for(int j=0;j<dataList.size();j++){
              DataTuple tup=dataList.get(j).deepCloneTuple();
              String id=tup.m_Objects[0].toString().replace("\"", "");
              id="\""+id+"new-Init\"";
              tup.m_Objects[0]=id;
              
              //create random permuatation containing k elements
              int[] array = new int[dataList.size()];
              
              for(int i=0;i<dataList.size();i++)
                  array[i]=i;
         
              for(int i = 0; i < dataList.size(); i++){
                      int ran = i + r.nextInt (dataList.size()-i);

                       int temp = array[i];
                        array[i] = array[ran];
                        array[ran] = temp;
                }   
              
              int numAttrs=schema.getNbAttributes()-2;
              //System.out.println("numAttr: "+numAttrs);
              for(int i=0;i<numAttrs;i++){
                   ClusAttrType t=schema.getAttrType(i+1);
                        //System.out.println("attr name: "+t.getTypeName());
                        if(t.getTypeName().contains("Numeric")){
                            int elemInd=t.getArrayIndex();
                            /*System.out.println("Elem index: "+elemInd);
                            System.out.println("array: "+array[i%dataList.size()]);
                            System.out.println("Tuple size: "+tup.m_Doubles.length);*/
                            tup.m_Doubles[elemInd]=dataList.get(array[i%dataList.size()]).m_Doubles[elemInd];
                        }
                        else if(t.getTypeName().contains("Nominal")){
                            int elemInd=t.getArrayIndex();
                            tup.m_Ints[elemInd]=dataList.get(array[i%dataList.size()]).m_Ints[elemInd];
                        }
              }
             
              newTuples.add(tup);
              
              numIt++;
        if(numIt%step==0)
                System.out.println((((double)numIt/dataList.size())*100)+"% completed...");
                if(numIt==dataList.size())
                    System.out.println("100% completed!");   
          }
          
          for(int i=0;i<dataList.size();i++){
              double arow[];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
              arow=new double[dataList.get(i).m_Doubles.length+1];
              else
                  arow=new double[1];
    
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                 for(int j=0;j<dataList.get(i).m_Doubles.length;j++)
                    arow[j]=dataList.get(i).m_Doubles[j];
              }
             
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                arow[dataList.get(i).m_Doubles.length]=1.0;
              else
                  arow[0]=1.0;
              dataList.get(i).m_Doubles=arow;
          }
          
          for(int i=0;i<newTuples.size();i++){
              double arow[];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                 arow=new double[newTuples.get(i).m_Doubles.length+1];
              else
                  arow=new double[1];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                     for(int j=0;j<newTuples.get(i).m_Doubles.length;j++){
                         if(j==0 && appset.networkInit==true)
                             arow[j]=data.getNbRows()+i;
                         else
                            arow[j]=newTuples.get(i).m_Doubles[j];
                     }
              }
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                    arow[newTuples.get(i).m_Doubles.length]=0.0;
              else arow[0]=0.0;
              newTuples.get(i).m_Doubles=arow;
          }
          
          for(int i=0;i<newTuples.size();i++)
              dataList.add(newTuples.get(i));
        
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;

        if(appset.system.equals("windows"))
            name+="\\JinputInitial.arff";
        else 
            name+="/JinputInitial.arff";
                 
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }       
    }
    
     HashMap<Integer,Pair<Double,Double>> initialClusteringGenAttribute(String outFolder, ApplicationSettings appset, int numChangedAttrs, int attributeIndex, Random r, Mappings map){
           
         HashMap<Integer,Pair<Double,Double>> bins = new HashMap<>();

          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<String> values = new ArrayList<>();
          //create histogram using attribute values
          //add different values to the nominal type of the target attribute
          //replace existing attr type with a new one
          //add corresponding int values to the dataTuples/instances
         
         ClusAttrType selectedTargetAttr = schema.getAttrType(attributeIndex);
         String[] categories=null;
         
          if(selectedTargetAttr.getTypeName().contains("Numeric")){
                //create histogram
              HashSet<Double> attrV = new HashSet<>();
                    ArrayList<Double> attrValues = null;//new ArrayList<>();
                    
                    int arind = schema.getAttrType(attributeIndex).getArrayIndex();
                    
                    for(int s = 0; s<dataList.size();s++){
                        int ind = s;
                        
                        if(dataList.get(ind).m_Doubles[arind]!=Double.POSITIVE_INFINITY)
                             attrV.add(dataList.get(ind).m_Doubles[arind]);
                        else  continue;
                    }
                        //attrValues.add(data1.get(rand.nextInt(data1.numInstances())).value(r));
                      
                      attrValues = new ArrayList(attrV);
                      Collections.sort(attrValues);
                      double vp[] = new double[attrValues.size()];
                      
                      System.out.println("vp: ");
                      for(int s=0;s<attrValues.size();s++){
                          System.out.print(attrValues.get(s)+" ");
                          vp[s] = attrValues.get(s);
                      }
                      System.out.println();
                      
                      double n = Math.pow(dataList.size(), -0.33);
                      DescriptiveStatistics da = new DescriptiveStatistics(vp);
                      double iqr = da.getPercentile(75) - da.getPercentile(25);
                      double h = 2*iqr*n;
                      
                      System.out.println("h: "+h);
                      
                      if(h == 0 || h == Double.NaN || h == Double.POSITIVE_INFINITY){
                          int indexNext = 0;
                          for(int z=0;z<attrV.size();z++){
                                values.add("c"+z);
                                if(z==0){
                                     bins.put(z, new Pair<>(Double.NEGATIVE_INFINITY,attrValues.get(0)));
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                }
                                else if(z<(attrValues.size()-1)){
                                    bins.put(z, new Pair<>(attrValues.get(indexNext-1),attrValues.get(indexNext)));
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                }
                                else{
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                    bins.put(z, new Pair<>(attrValues.get(indexNext),Double.POSITIVE_INFINITY));
                                }
                          }
                      }
                      else{
                      ArrayList<Double> splittingPoints = new ArrayList<>();
                      
                      double tmpInit = attrValues.get(0);
                      for(int s=1;s<attrValues.size();s++){
                              if(Math.abs(attrValues.get(s)-tmpInit)>h){
                                  splittingPoints.add(attrValues.get(s));
                                  tmpInit = attrValues.get(s);
                              }
                      }
                      
                     System.out.println("attrValues size: "+attrValues.size());
                     System.out.println("n: "+n);
                      
                       for(int s=0;s<splittingPoints.size()+1;s++){
                           //if(s<splittingPoints.size())
                            // System.out.println("S"+s+" : "+splittingPoints.get(s));
                        values.add("c"+s);
                        if(s == 0)
                         bins.put(s, new Pair<>(Double.NEGATIVE_INFINITY,splittingPoints.get(s)));
                        else if(s<splittingPoints.size())
                            bins.put(s, new Pair<>(splittingPoints.get(s-1),splittingPoints.get(s)));
                        else 
                            bins.put(s, new Pair<>(splittingPoints.get(s-1), Double.POSITIVE_INFINITY));
                       }
                       System.out.println();

                  System.out.println("Splitting point size: "+splittingPoints.size());
                  int numAssigned = 0;
                  HashMap<Integer,Integer> classCounts = new HashMap<>();
                  
                  int targetIndex = schema.getAttrType(schema.getNbAttributes()-1).getArrayIndex();
                  
                  Random rand = new Random();
                  
                    for(int s=0;s<dataList.size();s++){
                        
                        if(dataList.get(s).m_Doubles[arind]<=splittingPoints.get(0)){
                             dataList.get(s).m_Ints[targetIndex] = 0;
                             numAssigned++;
                             if(!classCounts.containsKey(0))
                                 classCounts.put(0, 1);
                             else classCounts.put(0,classCounts.get(0)+1);
                        }
                        else if(dataList.get(s).m_Doubles[arind]>splittingPoints.get(splittingPoints.size()-1)){
                           // data1.get(s).setClassValue(values.get(values.size()-1));
                            dataList.get(s).m_Ints[targetIndex] = values.size()-1;
                            numAssigned++;
                            
                            if(!classCounts.containsKey(values.size()-1))
                                 classCounts.put(values.size()-1, 1);
                             else classCounts.put(values.size()-1,classCounts.get(values.size()-1)+1);
                            
                        }
                        else{
                            int a = 0;
                            for(int s1 = 1;s1<splittingPoints.size();s1++)
                                if(dataList.get(s).m_Doubles[arind]<=splittingPoints.get(s1)){
                                     // data1.get(s).setClassValue(values.get(s1));
                                      dataList.get(s).m_Ints[targetIndex] = s1;
                                         numAssigned++;
                                         a = 1;
                                         if(!classCounts.containsKey(s1))
                                                  classCounts.put(s1, 1);
                                          else classCounts.put(s1,classCounts.get(s1)+1);
                                         break;
                                }
                            if(a == 0){
                                
                            // dataList.get(s).setClassValue(values.get(rand.nextInt(values.size())));
                             dataList.get(s).m_Ints[targetIndex] = rand.nextInt(values.size());
                            }
                        }

                    }

                     System.out.println("Num assigned: "+numAssigned); 
                     
                     System.out.println("Class assignement: ");
                     for(int c:classCounts.keySet())
                         System.out.println(c+" "+classCounts.get(c));
                      }
     
                categories = new String[values.size()];
               
               for(int i=0;i<categories.length;i++)
                   categories[i] =values.get(i);             
            }
            else{
               categories = new String[map.cattAtt.get(attributeIndex-1).getValue0().keySet().size()];
               
               for(int i=0;i<categories.length;i++){
                   categories[i] = map.cattAtt.get(attributeIndex-1).getValue1().get(i);
                   bins.put(i, new Pair<>((double)i,Double.NEGATIVE_INFINITY));
               }
               
            }

          ClusAttrType newtype = new NominalAttrType("target",categories);
          
          int lastNominal=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length; 
          schema.setAttrType(newtype, schema.getNbAttributes()-1);
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastNominal);

          for(int i=0;i<dataList.size();i++){
              
              if(selectedTargetAttr.getTypeName().contains("Numeric")){
                  
              }
              else{
                String val="";

               int nIndex=schema.getAttrType(attributeIndex).getArrayIndex();
        
        NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);

        if(dataList.get(i).m_Ints[nIndex]>=nom[nIndex].m_Values.length)
           val =  "?";
         val=nom[nIndex].m_Values[dataList.get(i).m_Ints[nIndex]];  
         
         if(val.equals("?"))
             dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = 0;
         else
            dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = map.cattAtt.get(attributeIndex-1).getValue0().get(val.trim()); 
         
              } 
          }
          
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;

        if(appset.system.equals("windows"))
            name+="\\JinputInitial.arff";
        else 
            name+="/JinputInitial.arff";
                 
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }  
        return bins;
    }
    
      HashMap<Integer,Pair<Double,Double>> initialClusteringGenAttributeMT(String outFolder, ApplicationSettings appset, int numChangedAttrs, int attributeIndex, int tid, Random r, Mappings map){
           
         HashMap<Integer,Pair<Double,Double>> bins = new HashMap<>();

          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<String> values = new ArrayList<>();
          //create histogram using attribute values
          //add different values to the nominal type of the target attribute
          //replace existing attr type with a new one
          //add corresponding int values to the dataTuples/instances
         
         ClusAttrType selectedTargetAttr = schema.getAttrType(attributeIndex);
         String[] categories=null;
         
          if(selectedTargetAttr.getTypeName().contains("Numeric")){
                //create histogram
              HashSet<Double> attrV = new HashSet<>();
                    ArrayList<Double> attrValues = null;//new ArrayList<>();
                    
                    int arind = schema.getAttrType(attributeIndex).getArrayIndex();
                    
                    for(int s = 0; s<dataList.size();s++){
                        int ind = s;
                        
                        if(dataList.get(ind).m_Doubles[arind]!=Double.POSITIVE_INFINITY)
                             attrV.add(dataList.get(ind).m_Doubles[arind]);
                        else  continue;
                    }
                        //attrValues.add(data1.get(rand.nextInt(data1.numInstances())).value(r));
                      
                      attrValues = new ArrayList(attrV);
                      Collections.sort(attrValues);
                      double vp[] = new double[attrValues.size()];
                      
                      System.out.println("vp: ");
                      for(int s=0;s<attrValues.size();s++){
                          System.out.print(attrValues.get(s)+" ");
                          vp[s] = attrValues.get(s);
                      }
                      System.out.println();
                      
                      double n = Math.pow(dataList.size(), -0.33);
                      DescriptiveStatistics da = new DescriptiveStatistics(vp);
                      double iqr = da.getPercentile(75) - da.getPercentile(25);
                      double h = 2*iqr*n;
                      
                      System.out.println("h: "+h);
                      
                      if(h == 0 || h == Double.NaN || h == Double.POSITIVE_INFINITY){
                          int indexNext = 0;
                          for(int z=0;z<attrV.size();z++){
                                values.add("c"+z);
                                if(z==0){
                                     bins.put(z, new Pair<>(Double.NEGATIVE_INFINITY,attrValues.get(0)));
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                }
                                else if(z<(attrValues.size()-1)){
                                    bins.put(z, new Pair<>(attrValues.get(indexNext-1),attrValues.get(indexNext)));
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                }
                                else{
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                    bins.put(z, new Pair<>(attrValues.get(indexNext),Double.POSITIVE_INFINITY));
                                }
                          }
                      }
                      else{
                      ArrayList<Double> splittingPoints = new ArrayList<>();
                      
                      double tmpInit = attrValues.get(0);
                      for(int s=1;s<attrValues.size();s++){
                              if(Math.abs(attrValues.get(s)-tmpInit)>h){
                                  splittingPoints.add(attrValues.get(s));
                                  tmpInit = attrValues.get(s);
                              }
                      }
                      
                     System.out.println("attrValues size: "+attrValues.size());
                     System.out.println("n: "+n);
                      
                       for(int s=0;s<splittingPoints.size()+1;s++){
                           //if(s<splittingPoints.size())
                            // System.out.println("S"+s+" : "+splittingPoints.get(s));
                        values.add("c"+s);
                        if(s == 0)
                         bins.put(s, new Pair<>(Double.NEGATIVE_INFINITY,splittingPoints.get(s)));
                        else if(s<splittingPoints.size())
                            bins.put(s, new Pair<>(splittingPoints.get(s-1),splittingPoints.get(s)));
                        else 
                            bins.put(s, new Pair<>(splittingPoints.get(s-1), Double.POSITIVE_INFINITY));
                       }
                       System.out.println();

                  System.out.println("Splitting point size: "+splittingPoints.size());
                  int numAssigned = 0;
                  HashMap<Integer,Integer> classCounts = new HashMap<>();
                  
                  int targetIndex = schema.getAttrType(schema.getNbAttributes()-1).getArrayIndex();
                  
                  Random rand = new Random();
                  
                    for(int s=0;s<dataList.size();s++){
                        
                        if(dataList.get(s).m_Doubles[arind]<=splittingPoints.get(0)){
                             dataList.get(s).m_Ints[targetIndex] = 0;
                             numAssigned++;
                             if(!classCounts.containsKey(0))
                                 classCounts.put(0, 1);
                             else classCounts.put(0,classCounts.get(0)+1);
                        }
                        else if(dataList.get(s).m_Doubles[arind]>splittingPoints.get(splittingPoints.size()-1)){
                           // data1.get(s).setClassValue(values.get(values.size()-1));
                            dataList.get(s).m_Ints[targetIndex] = values.size()-1;
                            numAssigned++;
                            
                            if(!classCounts.containsKey(values.size()-1))
                                 classCounts.put(values.size()-1, 1);
                             else classCounts.put(values.size()-1,classCounts.get(values.size()-1)+1);
                            
                        }
                        else{
                            int a = 0;
                            for(int s1 = 1;s1<splittingPoints.size();s1++)
                                if(dataList.get(s).m_Doubles[arind]<=splittingPoints.get(s1)){
                                     // data1.get(s).setClassValue(values.get(s1));
                                      dataList.get(s).m_Ints[targetIndex] = s1;
                                         numAssigned++;
                                         a = 1;
                                         if(!classCounts.containsKey(s1))
                                                  classCounts.put(s1, 1);
                                          else classCounts.put(s1,classCounts.get(s1)+1);
                                         break;
                                }
                            if(a == 0){
                                
                            // dataList.get(s).setClassValue(values.get(rand.nextInt(values.size())));
                             dataList.get(s).m_Ints[targetIndex] = rand.nextInt(values.size());
                            }
                        }

                    }

                     System.out.println("Num assigned: "+numAssigned); 
                     
                     System.out.println("Class assignement: ");
                     for(int c:classCounts.keySet())
                         System.out.println(c+" "+classCounts.get(c));
                      }
     
                categories = new String[values.size()];
               
               for(int i=0;i<categories.length;i++)
                   categories[i] =values.get(i);             
            }
            else{
               categories = new String[map.cattAtt.get(attributeIndex-1).getValue0().keySet().size()];
               
               for(int i=0;i<categories.length;i++){
                   categories[i] = map.cattAtt.get(attributeIndex-1).getValue1().get(i);
                   bins.put(i, new Pair<>((double)i,Double.NEGATIVE_INFINITY));
               }
               
            }

          ClusAttrType newtype = new NominalAttrType("target",categories);
          
          int lastNominal=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length; 
          schema.setAttrType(newtype, schema.getNbAttributes()-1);
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastNominal);

          for(int i=0;i<dataList.size();i++){
              
              if(selectedTargetAttr.getTypeName().contains("Numeric")){
                  
              }
              else{
                String val="";

               int nIndex=schema.getAttrType(attributeIndex).getArrayIndex();

        //System.out.println("nIndex: "+nIndex);
        NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);
       // System.out.println("Indeks kategorije za instancu: "+i+" je: "+dataList.get(i).m_Ints[nIndex]);
       // System.out.println("All indices for instance: "+i);
       // for(int kk=0;kk<dataList.get(i).m_Ints.length;kk++)
     //       System.out.print(dataList.get(i).m_Ints[kk]+" ");
      //  System.out.println();
      //  System.out.println("Broj nominalnih vrijednosti: "+nom[nIndex].m_Values.length);
     //   System.out.println("Svi nominalni indeksi: ");
     //   for(int kk=0;kk<nom[nIndex].m_Values.length;kk++)
       //     System.out.print(nom[nIndex].m_Values[kk]+" ");
      //  System.out.println();
        if(dataList.get(i).m_Ints[nIndex]>=nom[nIndex].m_Values.length)
           val =  "?";
         val=nom[nIndex].m_Values[dataList.get(i).m_Ints[nIndex]];  
      //   System.out.println("val: "+val);
         if(val.equals("?"))
             dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = 0;
         else
            dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = map.cattAtt.get(attributeIndex-1).getValue0().get(val.trim()); 
         
       //  System.out.println("Upisana vrijednost: "+dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1]);
         
              } 
          }
          
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;

        if(appset.system.equals("windows"))
            name+="\\JinputInitial"+tid+".arff";
        else 
            name+="/JinputInitial"+tid+".arff";
                 
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }  
        return bins;
    }
     
     
    HashMap<Integer,Pair<Double,Double>> initialClusteringGenAttribute2Bins(String outFolder, ApplicationSettings appset, int numChangedAttrs, int attributeIndex, Random r, Mappings map){
           
         HashMap<Integer,Pair<Double,Double>> bins = new HashMap<>();

          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<String> values = new ArrayList<>();

         ClusAttrType selectedTargetAttr = schema.getAttrType(attributeIndex);
         String[] categories=null;
         
         System.out.println("NAME: "+selectedTargetAttr.getName());
         
          if(selectedTargetAttr.getTypeName().contains("Numeric")){
                //create histogram
              HashSet<Double> attrV = new HashSet<>();
                    ArrayList<Double> attrValues = null;//new ArrayList<>();
                    
                    int arind = schema.getAttrType(attributeIndex).getArrayIndex();
                    
                    for(int s = 0; s<dataList.size();s++){
                        int ind = s;
                        
                        if(dataList.get(ind).m_Doubles[arind]!=Double.POSITIVE_INFINITY)
                             attrV.add(dataList.get(ind).m_Doubles[arind]);
                        else  continue;
                    }
                      
                      attrValues = new ArrayList(attrV);
                      Collections.sort(attrValues);
                      double vp[] = new double[attrValues.size()];
                      
                      System.out.println("vp: ");
                      for(int s=0;s<attrValues.size();s++){
                          System.out.print(attrValues.get(s)+" ");
                          vp[s] = attrValues.get(s);
                      }
                      System.out.println();
                      
                      double n = Math.pow(dataList.size(), -0.33);
                      DescriptiveStatistics da = new DescriptiveStatistics(vp);
                      double iqr = da.getPercentile(75) - da.getPercentile(25);
                      double h = 2*iqr*n;
                      
                      System.out.println("h: "+h);
                      
                      if(h == 0 || h == Double.NaN || h == Double.POSITIVE_INFINITY){
                          int indexNext = 0;
                          for(int z=0;z<attrV.size();z++){
                                values.add("c"+z);
                                if(z==0){
                                     bins.put(z, new Pair<>(Double.NEGATIVE_INFINITY,attrValues.get(0)));
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                }
                                else if(z<(attrValues.size()-1)){
                                    bins.put(z, new Pair<>(attrValues.get(indexNext-1),attrValues.get(indexNext)));
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                }
                                else{
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                    bins.put(z, new Pair<>(attrValues.get(indexNext),Double.POSITIVE_INFINITY));
                                }
                          }
                      }
                      else{
                      ArrayList<Double> splittingPoints = new ArrayList<>();
                      
                      double tmpInit = attrValues.get(0);
                      for(int s=1;s<attrValues.size();s++){
                              if(Math.abs(attrValues.get(s)-tmpInit)>h){
                                  splittingPoints.add(attrValues.get(s));
                                  tmpInit = attrValues.get(s);
                              }
                      }
                      
                     System.out.println("attrValues size: "+attrValues.size());
                     System.out.println("n: "+n);
                      
                       for(int s=0;s<2;s++){

                        values.add("c"+s);
                        if(s == 0)
                         bins.put(s, new Pair<>(Double.NEGATIVE_INFINITY,splittingPoints.get(s)));
                        else 
                            bins.put(s, new Pair<>(splittingPoints.get(s-1), Double.POSITIVE_INFINITY));
                       }
                       System.out.println();

                  System.out.println("Splitting point size: "+splittingPoints.size());
                  int numAssigned = 0;
                  HashMap<Integer,Integer> classCounts = new HashMap<>();
                  
                  int targetIndex = schema.getAttrType(schema.getNbAttributes()-1).getArrayIndex();
                  
                  Random rand = new Random();
                  
                    for(int s=0;s<dataList.size();s++){
                        
                        if(dataList.get(s).m_Doubles[arind]<=splittingPoints.get(0)){
                             dataList.get(s).m_Ints[targetIndex] = 0;
                             numAssigned++;
                             if(!classCounts.containsKey(0))
                                 classCounts.put(0, 1);
                             else classCounts.put(0,classCounts.get(0)+1);
                        }
                        else{
                                      dataList.get(s).m_Ints[targetIndex] = 1;
                                         numAssigned++;
                                         if(!classCounts.containsKey(1))
                                                  classCounts.put(1, 1);
                                          else classCounts.put(1,classCounts.get(1)+1);
                                
                        }

                    }

                     System.out.println("Num assigned: "+numAssigned); 
                     
                     System.out.println("Class assignement: ");
                     for(int c:classCounts.keySet())
                         System.out.println(c+" "+classCounts.get(c));
                      }
     
                categories = new String[values.size()];
               
               for(int i=0;i<categories.length;i++)
                   categories[i] =values.get(i);             
            }
            else{
               categories = new String[map.cattAtt.get(attributeIndex-1).getValue0().keySet().size()];
               
               for(int i=0;i<categories.length;i++){
                   categories[i] = map.cattAtt.get(attributeIndex-1).getValue1().get(i);
                   bins.put(i, new Pair<>((double)i,Double.NEGATIVE_INFINITY));
               }
               
            }

          ClusAttrType newtype = new NominalAttrType("target",categories);
          
          int lastNominal=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length; 
          schema.setAttrType(newtype, schema.getNbAttributes()-1);
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastNominal);

          for(int i=0;i<dataList.size();i++){
              
              if(selectedTargetAttr.getTypeName().contains("Numeric")){
                  
              }
              else{
                String val="";

               int nIndex=schema.getAttrType(attributeIndex).getArrayIndex();
        
        NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);

        if(dataList.get(i).m_Ints[nIndex]>=nom[nIndex].m_Values.length)
           val =  "?";
         val=nom[nIndex].m_Values[dataList.get(i).m_Ints[nIndex]];  
         
         if(val.equals("?"))
             dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = 0;
         else
            dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = map.cattAtt.get(attributeIndex-1).getValue0().get(val.trim()); 
         
              } 
          }
          
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;

        if(appset.system.equals("windows"))
            name+="\\JinputInitial.arff";
        else 
            name+="/JinputInitial.arff";
                 
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }  
        return bins;
    }
    
    
     HashMap<Integer,Pair<Double,Double>> initialClusteringGenAttribute2BinsMT(String outFolder, ApplicationSettings appset, int numChangedAttrs, int attributeIndex, int tID, Random r, Mappings map){
           
         HashMap<Integer,Pair<Double,Double>> bins = new HashMap<>();

          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<String> values = new ArrayList<>();

         ClusAttrType selectedTargetAttr = schema.getAttrType(attributeIndex);
         String[] categories=null;
         
         System.out.println("NAME: "+selectedTargetAttr.getName());
         
          if(selectedTargetAttr.getTypeName().contains("Numeric")){
                //create histogram
              HashSet<Double> attrV = new HashSet<>();
                    ArrayList<Double> attrValues = null;//new ArrayList<>();
                    
                    int arind = schema.getAttrType(attributeIndex).getArrayIndex();
                    
                    for(int s = 0; s<dataList.size();s++){
                        int ind = s;
                        
                        if(dataList.get(ind).m_Doubles[arind]!=Double.POSITIVE_INFINITY)
                             attrV.add(dataList.get(ind).m_Doubles[arind]);
                        else  continue;
                    }
                      
                      attrValues = new ArrayList(attrV);
                      Collections.sort(attrValues);
                      double vp[] = new double[attrValues.size()];
                      
                      System.out.println("vp: ");
                      for(int s=0;s<attrValues.size();s++){
                          System.out.print(attrValues.get(s)+" ");
                          vp[s] = attrValues.get(s);
                      }
                      System.out.println();
                      
                      double n = Math.pow(dataList.size(), -0.33);
                      DescriptiveStatistics da = new DescriptiveStatistics(vp);
                      double iqr = da.getPercentile(75) - da.getPercentile(25);
                      double h = 2*iqr*n;
                      
                      System.out.println("h: "+h);
                      
                      if(h == 0 || h == Double.NaN || h == Double.POSITIVE_INFINITY){
                          int indexNext = 0;
                          for(int z=0;z<attrV.size();z++){
                                values.add("c"+z);
                                if(z==0){
                                     bins.put(z, new Pair<>(Double.NEGATIVE_INFINITY,attrValues.get(0)));
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                }
                                else if(z<(attrValues.size()-1)){
                                    bins.put(z, new Pair<>(attrValues.get(indexNext-1),attrValues.get(indexNext)));
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                }
                                else{
                                     for(int w=indexNext;w<attrValues.size();w++)
                                         if(attrValues.get(w)!=attrValues.get(0)){
                                             indexNext = w;
                                             break;
                                         }
                                    bins.put(z, new Pair<>(attrValues.get(indexNext),Double.POSITIVE_INFINITY));
                                }
                          }
                      }
                      else{
                      ArrayList<Double> splittingPoints = new ArrayList<>();
                      
                      double tmpInit = attrValues.get(0);
                      for(int s=1;s<attrValues.size();s++){
                              if(Math.abs(attrValues.get(s)-tmpInit)>h){
                                  splittingPoints.add(attrValues.get(s));
                                  tmpInit = attrValues.get(s);
                              }
                      }
                      
                     System.out.println("attrValues size: "+attrValues.size());
                     System.out.println("n: "+n);
                      
                       for(int s=0;s<2;s++){

                        values.add("c"+s);
                        if(s == 0)
                         bins.put(s, new Pair<>(Double.NEGATIVE_INFINITY,splittingPoints.get(s)));
                        else 
                            bins.put(s, new Pair<>(splittingPoints.get(s-1), Double.POSITIVE_INFINITY));
                       }
                       System.out.println();

                  System.out.println("Splitting point size: "+splittingPoints.size());
                  int numAssigned = 0;
                  HashMap<Integer,Integer> classCounts = new HashMap<>();
                  
                  int targetIndex = schema.getAttrType(schema.getNbAttributes()-1).getArrayIndex();
                  
                  Random rand = new Random();
                  
                    for(int s=0;s<dataList.size();s++){
                        
                        if(dataList.get(s).m_Doubles[arind]<=splittingPoints.get(0)){
                             dataList.get(s).m_Ints[targetIndex] = 0;
                             numAssigned++;
                             if(!classCounts.containsKey(0))
                                 classCounts.put(0, 1);
                             else classCounts.put(0,classCounts.get(0)+1);
                        }
                        else{
                                      dataList.get(s).m_Ints[targetIndex] = 1;
                                         numAssigned++;
                                         if(!classCounts.containsKey(1))
                                                  classCounts.put(1, 1);
                                          else classCounts.put(1,classCounts.get(1)+1);
                                
                        }

                    }

                     System.out.println("Num assigned: "+numAssigned); 
                     
                     System.out.println("Class assignement: ");
                     for(int c:classCounts.keySet())
                         System.out.println(c+" "+classCounts.get(c));
                      }
     
                categories = new String[values.size()];
               
               for(int i=0;i<categories.length;i++)
                   categories[i] =values.get(i);             
            }
            else{
               categories = new String[map.cattAtt.get(attributeIndex-1).getValue0().keySet().size()];
               
               for(int i=0;i<categories.length;i++){
                   categories[i] = map.cattAtt.get(attributeIndex-1).getValue1().get(i);
                   bins.put(i, new Pair<>((double)i,Double.NEGATIVE_INFINITY));
               }
               
            }

          ClusAttrType newtype = new NominalAttrType("target",categories);
          
          int lastNominal=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length; 
          schema.setAttrType(newtype, schema.getNbAttributes()-1);
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastNominal);

          for(int i=0;i<dataList.size();i++){
              
              if(selectedTargetAttr.getTypeName().contains("Numeric")){
                  
              }
              else{
                String val="";

               int nIndex=schema.getAttrType(attributeIndex).getArrayIndex();
        
        NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);

        if(dataList.get(i).m_Ints[nIndex]>=nom[nIndex].m_Values.length)
           val =  "?";
         val=nom[nIndex].m_Values[dataList.get(i).m_Ints[nIndex]];  
         
         if(val.equals("?"))
             dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = 0;
         else
            dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = map.cattAtt.get(attributeIndex-1).getValue0().get(val.trim()); 
         
              } 
          }
          
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;

        if(appset.system.equals("windows"))
            name+="\\JinputInitial"+tID+".arff";
        else 
            name+="/JinputInitial"+tID+".arff";
                 
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }  
        return bins;
    }
     
     
  HashMap<Integer,Pair<Double,Double>> initialClusteringGenAttributeEqFreqMT(String outFolder, ApplicationSettings appset, int numChangedAttrs, int attributeIndex, int tid, Random r, Mappings map){
           
         HashMap<Integer,Pair<Double,Double>> bins = new HashMap<>();

          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<String> values = new ArrayList<>();
          //create histogram using attribute values
          //add different values to the nominal type of the target attribute
          //replace existing attr type with a new one
          //add corresponding int values to the dataTuples/instances
         
         double epsilon = 0.0000001; 
          
         ClusAttrType selectedTargetAttr = schema.getAttrType(attributeIndex);
         String[] categories=null;
         
          if(selectedTargetAttr.getTypeName().contains("Numeric")){
                //create histogram
              HashSet<Double> attrV = new HashSet<>();
                    ArrayList<Double> attrValues = null;//new ArrayList<>();
                    
                    int arind = schema.getAttrType(attributeIndex).getArrayIndex();
                    
                    for(int s = 0; s<dataList.size();s++){
                        int ind = s;
                        
                        if(dataList.get(ind).m_Doubles[arind]!=Double.POSITIVE_INFINITY)
                             attrV.add(dataList.get(ind).m_Doubles[arind]);
                        else  continue;
                    }
                        //attrValues.add(data1.get(rand.nextInt(data1.numInstances())).value(r));
                      
                      attrValues = new ArrayList(attrV);
                      Collections.sort(attrValues);
                      double vp[] = new double[attrValues.size()];
                      
                      System.out.println("vp: ");
                      for(int s=0;s<attrValues.size();s++){
                          System.out.print(attrValues.get(s)+" ");
                          vp[s] = attrValues.get(s);
                      }
                      System.out.println();
                      
                      
                      int m = (int)Math.ceil(2*Math.pow(attrValues.size(), 1/3.0));
                      int numInBin = attrValues.size()/m;
                      
                      ArrayList<Double> splittingPoints = new ArrayList<>();
                      System.out.println("Attr val: "+attrValues.size()+" "+"Num in bins: "+numInBin);
                      int count = 0;
                      for(int s=0;s<attrValues.size();s++){
                            count++;   
                            
                             if(numInBin == 0){
                                 splittingPoints.add(attrValues.get(s));
                                 bins.put(s, new Pair<>(attrValues.get(0),attrValues.get(0)+epsilon));
                                 break;
                             }
                            
                              if(count == numInBin){
                                  splittingPoints.add(attrValues.get(s));
                                  count = 0;
                              }
                      }
                      
                      if(numInBin>=1){
                          System.out.println("attrValues: "+attrValues.size()+" "+"splittingPoints"+" "+splittingPoints.size());
                       for(int s=0;s<splittingPoints.size()+1;s++){

                        values.add("c"+s);
                        if(s == 0)
                         bins.put(s, new Pair<>(Double.NEGATIVE_INFINITY,splittingPoints.get(s)));
                        else if(s<splittingPoints.size())
                            bins.put(s, new Pair<>(splittingPoints.get(s-1),splittingPoints.get(s)));
                        else 
                            bins.put(s, new Pair<>(splittingPoints.get(s-1), Double.POSITIVE_INFINITY));
                       }
                       System.out.println();
                      }
                      else{
                          for(int s = 0; s<attrValues.size();s++)
                              values.add("c"+s);
                      }
                  System.out.println("Splitting point size: "+splittingPoints.size());
                  int numAssigned = 0;
                  HashMap<Integer,Integer> classCounts = new HashMap<>();
                  
                  int targetIndex = schema.getAttrType(schema.getNbAttributes()-1).getArrayIndex();
                  
                  Random rand = new Random();
                  
                    for(int s=0;s<dataList.size();s++){
                        
                        
                        if(dataList.get(s).m_Doubles[arind]<=splittingPoints.get(0)){
                             dataList.get(s).m_Ints[targetIndex] = 0;
                             numAssigned++;
                             if(!classCounts.containsKey(0))
                                 classCounts.put(0, 1);
                             else classCounts.put(0,classCounts.get(0)+1);
                        }
                        else if(dataList.get(s).m_Doubles[arind]>splittingPoints.get(splittingPoints.size()-1)){
                           // data1.get(s).setClassValue(values.get(values.size()-1));
                            dataList.get(s).m_Ints[targetIndex] = values.size()-1;
                            numAssigned++;
                            
                            if(!classCounts.containsKey(values.size()-1))
                                 classCounts.put(values.size()-1, 1);
                             else classCounts.put(values.size()-1,classCounts.get(values.size()-1)+1);
                            
                        }
                        else{
                            int a = 0;
                            for(int s1 = 1;s1<splittingPoints.size();s1++)
                                if(dataList.get(s).m_Doubles[arind]<=splittingPoints.get(s1)){
                                     // data1.get(s).setClassValue(values.get(s1));
                                      dataList.get(s).m_Ints[targetIndex] = s1;
                                         numAssigned++;
                                         a = 1;
                                         if(!classCounts.containsKey(s1))
                                                  classCounts.put(s1, 1);
                                          else classCounts.put(s1,classCounts.get(s1)+1);
                                         break;
                                }
                            if(a == 0){
                                
                            // dataList.get(s).setClassValue(values.get(rand.nextInt(values.size())));
                             dataList.get(s).m_Ints[targetIndex] = rand.nextInt(values.size());
                            }
                        }

                    }

                     System.out.println("Num assigned: "+numAssigned); 
                     
                     System.out.println("Class assignement: ");
                     for(int c:classCounts.keySet())
                         System.out.println(c+" "+classCounts.get(c));
                      
     
                categories = new String[values.size()];
               
               for(int i=0;i<categories.length;i++)
                   categories[i] =values.get(i);             
            }
            else{
               categories = new String[map.cattAtt.get(attributeIndex-1).getValue0().keySet().size()];
               
               for(int i=0;i<categories.length;i++){
                   categories[i] = map.cattAtt.get(attributeIndex-1).getValue1().get(i);
                   bins.put(i, new Pair<>((double)i,Double.NEGATIVE_INFINITY));
               }
               
            }

          ClusAttrType newtype = new NominalAttrType("target",categories);
          
          int lastNominal=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length; 
          schema.setAttrType(newtype, schema.getNbAttributes()-1);
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastNominal);

          for(int i=0;i<dataList.size();i++){
              
              if(selectedTargetAttr.getTypeName().contains("Numeric")){
                  
              }
              else{
                String val="";

               int nIndex=schema.getAttrType(attributeIndex).getArrayIndex();

        //System.out.println("nIndex: "+nIndex);
        NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);
       // System.out.println("Indeks kategorije za instancu: "+i+" je: "+dataList.get(i).m_Ints[nIndex]);
       // System.out.println("All indices for instance: "+i);
       // for(int kk=0;kk<dataList.get(i).m_Ints.length;kk++)
     //       System.out.print(dataList.get(i).m_Ints[kk]+" ");
      //  System.out.println();
      //  System.out.println("Broj nominalnih vrijednosti: "+nom[nIndex].m_Values.length);
     //   System.out.println("Svi nominalni indeksi: ");
     //   for(int kk=0;kk<nom[nIndex].m_Values.length;kk++)
       //     System.out.print(nom[nIndex].m_Values[kk]+" ");
      //  System.out.println();
        if(dataList.get(i).m_Ints[nIndex]>=nom[nIndex].m_Values.length)
           val =  "?";
         val=nom[nIndex].m_Values[dataList.get(i).m_Ints[nIndex]];  
      //   System.out.println("val: "+val);
         if(val.equals("?"))
             dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = 0;
         else
            dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = map.cattAtt.get(attributeIndex-1).getValue0().get(val.trim()); 
         
       //  System.out.println("Upisana vrijednost: "+dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1]);
         
              } 
          }
          
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;

        if(appset.system.equals("windows"))
            name+="\\JinputInitial"+tid+".arff";
        else 
            name+="/JinputInitial"+tid+".arff";
                 
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }  
        return bins;
    }
    
  
  HashMap<Integer,Pair<Double,Double>> initialClusteringGenAttributeKMeansMT(String outFolder, ApplicationSettings appset, int numChangedAttrs, int attributeIndex, int tid, Random r, Mappings map){
          
        //determine the correct k
        //get clusters
        //assign entities to clusters
       SimpleKMeans kmeans = new SimpleKMeans();
       kmeans.setSeed(1);
      
         HashMap<Integer,Pair<Double,Double>> bins = new HashMap<>();

          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<String> values = new ArrayList<>();
          //create histogram using attribute values
          //add different values to the nominal type of the target attribute
          //replace existing attr type with a new one
          //add corresponding int values to the dataTuples/instances
         
         ClusAttrType selectedTargetAttr = schema.getAttrType(attributeIndex);
         String[] categories=null;
         
          if(selectedTargetAttr.getTypeName().contains("Numeric")){
                //create histogram
              HashSet<Double> attrV = new HashSet<>();
                    ArrayList<Double> attrValues = new ArrayList(data.getNbRows());//new ArrayList<>();
                    
                    int arind = schema.getAttrType(attributeIndex).getArrayIndex();
                    
                    for(int s = 0; s<dataList.size();s++){
                        int ind = s;
                        
                        if(dataList.get(ind).m_Doubles[arind]!=Double.POSITIVE_INFINITY)
                            attrValues.add(dataList.get(ind).m_Doubles[arind]);
                        else  continue;
                    }

                    
                    // ArrayList<Attribute> attributes = new ArrayList<>();
                      FastVector attributes = new FastVector();
                     Attribute numerickiAtribut = new Attribute("value");
                     attributes.addElement(numerickiAtribut);
                     
                     Instances dataset = new Instances("Data", attributes, attrValues.size());

        
                     for (double val : attrValues) {
                            Instance inst = new Instance(1);
                            inst.setDataset(dataset);
                            inst.setValue(0, val);
                             dataset.add(inst);
                      }
                    
                  try{   
                      Random rand = new Random();
                       int targetIndex = schema.getAttrType(schema.getNbAttributes()-1).getArrayIndex();
                      ArrayList<TIntHashSet> clusterGroups;
                      int BestK = -1;
                      double BestSI = -1;
                     for(int k=2;k<11;k++){
                         kmeans = new SimpleKMeans();
                         kmeans.setSeed(1);
                         kmeans.setNumClusters(k); 
                         kmeans.buildClusterer(dataset);

                         int numClusters = kmeans.numberOfClusters();
                         clusterGroups = new ArrayList<>();
                         for (int i = 0; i < numClusters; i++) {
                                clusterGroups.add(new TIntHashSet());
                             }
                         
                         for (int i = 0; i < dataset.numInstances(); i++) {
                              int clusterID = kmeans.clusterInstance(dataset.instance(i));
                                  clusterGroups.get(clusterID).add(i);
                             }
                         
                         double MeanSilluhete = 0.0;
                         
                          for(int s = 0; s<dataset.numInstances();s++){
                              int clusterID = kmeans.clusterInstance(dataset.instance(s));
                              TIntHashSet instancesInCluster = clusterGroups.get(clusterID);
                              double ai = 0.0;
                              
                              Instance a = dataset.instance(s);
                              double vala = a.value(0);
                              
                              TIntIterator it = instancesInCluster.iterator();
                              
                              int el = -1;
                              int count = 0;
                              while(it.hasNext()){
                                  el = it.next();
                                  if(el == s) continue;
                                  count++;
                                  Instance i1 = dataset.instance(el);
                                  double val1 = i1.value(0);
                                  
                                  ai+=Math.sqrt((vala - val1)*(vala-val1));
                              }
                              
                              ai/=count;
                              
                              double bi = Double.POSITIVE_INFINITY;
                               double biTmp = 0.0;
                               TIntHashSet instancesTmp;
                                TIntIterator itTmp;
                                int countTmp = 0, elTmp;
                                
                              for(int j = 0;j<clusterGroups.size();j++){
                                  biTmp = 0.0;
                                  if(j == clusterID) continue;

                                   instancesTmp = clusterGroups.get(j);
                                   itTmp = instancesTmp.iterator();
                                  
                                   countTmp = 0;
                                                          
                              while(itTmp.hasNext()){
                                  elTmp = itTmp.next();
                                  countTmp++;
                                  Instance i1 = dataset.instance(elTmp);
                                  double val1 = i1.value(0);
                                  
                                  biTmp+=Math.sqrt((vala - val1)*(vala-val1));
                              }
                              
                              biTmp/=countTmp;
                              
                              if(biTmp<bi)
                                  bi = biTmp;
                                  
                              }
                              
                              double ti = Math.max(ai, bi);
                              double si = (bi-ai)/ti;
                              
                              MeanSilluhete+=si;
                              
                          }
                          
                         MeanSilluhete/=dataset.numInstances();
                        
                         if(BestK == -1){
                             BestK = k;
                             BestSI = MeanSilluhete;
                         }
                         else{
                              if(MeanSilluhete > BestSI){
                                  BestSI = MeanSilluhete;
                                  BestK = k;
                              }
                         }
                         
                     }
                     
                     System.out.println("BestK: "+BestK);
                      
                     kmeans = new SimpleKMeans();
                     kmeans.setSeed(1);
                         kmeans.setNumClusters(BestK); 
                         kmeans.buildClusterer(dataset);
                         
                       System.out.println("number of clusters: "+kmeans.numberOfClusters());
                         
                     for(int s=0;s<dataList.size();s++){
                         if(dataList.get(s).m_Doubles[arind]!=Double.POSITIVE_INFINITY){
                             int v = dataList.get(s).m_Ints[targetIndex] = rand.nextInt(BestK);
                         }
                         else{
                             int clusterID = kmeans.clusterInstance(dataset.instance(s));
                             dataList.get(s).m_Ints[targetIndex] = clusterID;
                         }
                     }   
                     
                     //create bins
                     int numClusters = kmeans.numberOfClusters();
                         clusterGroups = new ArrayList<>();
                         for (int i = 0; i < numClusters; i++) {
                                clusterGroups.add(new TIntHashSet());
                             }
                         
                         for (int i = 0; i < dataset.numInstances(); i++) {
                              int clusterID = kmeans.clusterInstance(dataset.instance(i));
                            //  System.out.println("cluster id: "+clusterID);
                                  clusterGroups.get(clusterID).add(i);
                             }
                         
                      for(int j = 0;j<clusterGroups.size();j++){
                                 TIntHashSet instancesTmp = clusterGroups.get(j);
                                 TIntIterator  itTmp = instancesTmp.iterator();
                                 double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
                                  while(itTmp.hasNext()){
                                  int el = itTmp.next();
                                  Instance i1 = dataset.instance(el);
                                  double val1 = i1.value(0);
                                  if(min>val1) min = val1;
                                  if(max<val1) max = val1;
                              }
                               bins.put(j, new Pair<>(min,max));  
                      }   
                      
                      
                categories = new String[kmeans.numberOfClusters()];
               
               for(int i=0;i<kmeans.numberOfClusters();i++){
                   values.add("c"+i);
                   categories[i] =values.get(i);     
               }
                         
                    //assign groups to instances     
                    //add random category to entities with missing values... 
                  }
                  catch(Exception e){}         
          }   
            else{
               categories = new String[map.cattAtt.get(attributeIndex-1).getValue0().keySet().size()];
               
               for(int i=0;i<categories.length;i++){
                   categories[i] = map.cattAtt.get(attributeIndex-1).getValue1().get(i);
                   bins.put(i, new Pair<>((double)i,Double.NEGATIVE_INFINITY));
               }
               
            }

          ClusAttrType newtype = new NominalAttrType("target",categories);
          
          int lastNominal=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length; 
          schema.setAttrType(newtype, schema.getNbAttributes()-1);
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastNominal);

          for(int i=0;i<dataList.size();i++){
              
              if(selectedTargetAttr.getTypeName().contains("Numeric")){
                  
              }
              else{
                String val="";

               int nIndex=schema.getAttrType(attributeIndex).getArrayIndex();

        //System.out.println("nIndex: "+nIndex);
        NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);
       // System.out.println("Indeks kategorije za instancu: "+i+" je: "+dataList.get(i).m_Ints[nIndex]);
       // System.out.println("All indices for instance: "+i);
       // for(int kk=0;kk<dataList.get(i).m_Ints.length;kk++)
     //       System.out.print(dataList.get(i).m_Ints[kk]+" ");
      //  System.out.println();
      //  System.out.println("Broj nominalnih vrijednosti: "+nom[nIndex].m_Values.length);
     //   System.out.println("Svi nominalni indeksi: ");
     //   for(int kk=0;kk<nom[nIndex].m_Values.length;kk++)
       //     System.out.print(nom[nIndex].m_Values[kk]+" ");
      //  System.out.println();
        if(dataList.get(i).m_Ints[nIndex]>=nom[nIndex].m_Values.length)
           val =  "?";
         val=nom[nIndex].m_Values[dataList.get(i).m_Ints[nIndex]];  
      //   System.out.println("val: "+val);
         if(val.equals("?"))
             dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = 0;
         else
            dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1] = map.cattAtt.get(attributeIndex-1).getValue0().get(val.trim()); 
         
       //  System.out.println("Upisana vrijednost: "+dataList.get(i).m_Ints[dataList.get(i).m_Ints.length-1]);
         
              } 
          }
          
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;

        if(appset.system.equals("windows"))
            name+="\\JinputInitial"+tid+".arff";
        else 
            name+="/JinputInitial"+tid+".arff";
                 
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }  
        return bins;
    }
    
    
    void initialClusteringGen(String outFolder, ApplicationSettings appset, int numChangedAttrs){
          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<DataTuple> newTuples=new ArrayList<>();
        
         int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;

         System.out.println("initial lastDouble: "+lastDouble);
         System.out.println("old attr size: "+schema.getNbAttributes());
         schema.addAttrType(new NumericAttrType("target"));
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);
         System.out.println("new attr size: "+schema.getNbAttributes()); 
         
         int numShufflingStep=Math.min(500, Math.min((int)(0.7*data.getNbRows()),schema.getNbAttributes()));
         System.out.println("Doing initial clustering...");
         System.out.println("data list size: "+dataList.size());
         System.out.println("Num shuffling steps: "+numShufflingStep);
         
         int numIt=0, step=dataList.size()/100;
         Random r=new Random();
         
          for(int j=0;j<dataList.size();j++){
              DataTuple tup=dataList.get(j).deepCloneTuple();
              String id=tup.m_Objects[0].toString().replace("\"", "");
              id="\""+id+"new-Init\"";
              tup.m_Objects[0]=id;
              
              //create random permuatation containing k elements
              int[] array = new int[dataList.size()];
              
              for(int i=0;i<dataList.size();i++)
                  array[i]=i;
         
              for(int i = 0; i < dataList.size(); i++){
                      int ran = i + r.nextInt (dataList.size()-i);

                       int temp = array[i];
                        array[i] = array[ran];
                        array[ran] = temp;
                }   
              
              int numAttrs=schema.getNbAttributes()-2;
              //System.out.println("numAttr: "+numAttrs);
              for(int i=0;i<numAttrs;i++){
                   ClusAttrType t=schema.getAttrType(i+1);
                        //System.out.println("attr name: "+t.getTypeName());
                        if(t.getTypeName().contains("Numeric")){
                            int elemInd=t.getArrayIndex();
                            /*System.out.println("Elem index: "+elemInd);
                            System.out.println("array: "+array[i%dataList.size()]);
                            System.out.println("Tuple size: "+tup.m_Doubles.length);*/
                            tup.m_Doubles[elemInd]=dataList.get(array[i%dataList.size()]).m_Doubles[elemInd];
                        }
                        else if(t.getTypeName().contains("Nominal")){
                            int elemInd=t.getArrayIndex();
                            tup.m_Ints[elemInd]=dataList.get(array[i%dataList.size()]).m_Ints[elemInd];
                        }
              }
             
              newTuples.add(tup);
              
              numIt++;
        if(numIt%step==0)
                System.out.println((((double)numIt/dataList.size())*100)+"% completed...");
                if(numIt==dataList.size())
                    System.out.println("100% completed!");   
          }
          
          for(int i=0;i<dataList.size();i++){
              double arow[];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
              arow=new double[dataList.get(i).m_Doubles.length+1];
              else
                  arow=new double[1];
    
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                 for(int j=0;j<dataList.get(i).m_Doubles.length;j++)
                    arow[j]=dataList.get(i).m_Doubles[j];
              }
             
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                arow[dataList.get(i).m_Doubles.length]=1.0;
              else
                  arow[0]=1.0;
              dataList.get(i).m_Doubles=arow;
          }
          
          for(int i=0;i<newTuples.size();i++){
              double arow[];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                 arow=new double[newTuples.get(i).m_Doubles.length+1];
              else
                  arow=new double[1];
              
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                     for(int j=0;j<newTuples.get(i).m_Doubles.length;j++){
                         if(j==0 && appset.networkInit==true)
                             arow[j]=data.getNbRows()+i;
                         else
                            arow[j]=newTuples.get(i).m_Doubles[j];
                     }
              }
              if(data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                    arow[newTuples.get(i).m_Doubles.length]=0.0;
              else arow[0]=0.0;
              newTuples.get(i).m_Doubles=arow;
          }
          
          for(int i=0;i<newTuples.size();i++)
              dataList.add(newTuples.get(i));
        
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;

         if(appset.system.equals("windows"))
            name+="\\JinputInitial.arff";
        else 
            name+="/JinputInitial.arff";
            
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }       
    }
    
    
    void initialClusteringCategorical(String outFolder){
          ArrayList<DataTuple> dataList=data.toArrayList();
          ArrayList<DataTuple> newTuples=new ArrayList<>();
        
         int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;

         System.out.println("initial lastDouble: "+lastDouble);
         System.out.println("old attr size: "+schema.getNbAttributes());
         schema.addAttrType(new NominalAttrType("target"));
         schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);
         System.out.println("new attr size: "+schema.getNbAttributes()); 
         
         int numShufflingStep=Math.min(500, Math.min((int)(0.7*data.getNbRows()),schema.getNbAttributes()));
         
         Random rand=new Random();
         
         System.out.println("Doing initial clustering...");
         System.out.println("data list size: "+dataList.size());
         System.out.println("Num shuffling steps: "+numShufflingStep);
         
         int numIt=0, step=dataList.size()/100;
         
          for(int j=0;j<dataList.size();j++){
              DataTuple tup=dataList.get(j).deepCloneTuple();
              String id=tup.m_Objects[0].toString().replace("\"", "");
              id="\""+id+"new-Init\"";
              tup.m_Objects[0]=id;
              
              for(int i=0;i<numShufflingStep;i++){//should fix
                    int ind=rand.nextInt(dataList.size());
                    DataTuple told=dataList.get(ind);
                    for(int k=0;k<((int)(Math.max(1.0,0.08*schema.getNbAttributes())));k++){
                        int toChange=rand.nextInt(schema.getNbAttributes()-1);
                        ClusAttrType t=schema.getAttrType(toChange);
                       // System.out.println("attr name: "+t.getTypeName());
                        if(t.getTypeName().contains("Numeric")){
                            int elemInd=t.getArrayIndex();
                            tup.m_Doubles[elemInd]=told.m_Doubles[elemInd];
                        }
                        else if(t.getTypeName().contains("Nominal")){
                            int elemInd=t.getArrayIndex();
                            tup.m_Ints[elemInd]=told.m_Ints[elemInd];
                        }
                        
                    }
                    //System.out.println("done "+(i+1));
              }
              
              newTuples.add(tup);
              
              numIt++;
        if(numIt%step==0)
                System.out.println((((double)numIt/dataList.size())*100)+"% completed...");
                if(numIt==dataList.size())
                    System.out.println("100% completed!");
              
          }
          
          /*System.out.println("dataList ds: "+dataList.get(0).m_Doubles.length);
          System.out.println("newT ds: "+newTuples.get(0).m_Doubles.length);*/
          
          for(int i=0;i<dataList.size();i++){
              int arow[];
              
              if(data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
              arow=new int[dataList.get(i).m_Ints.length+1];
              else
                  arow=new int[1];
              
              if(data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                 for(int j=0;j<dataList.get(i).m_Ints.length;j++)
                    arow[j]=dataList.get(i).m_Ints[j];
              }
             
              if(data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                arow[dataList.get(i).m_Ints.length]=0;
              else
                  arow[0]=1;
              dataList.get(i).m_Ints=arow;
          }
          
          for(int i=0;i<newTuples.size();i++){
              int arow[];
              
              if(data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                 arow=new int[newTuples.get(i).m_Ints.length+1];
              else
                  arow=new int[1];
              
              if(data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                     for(int j=0;j<newTuples.get(i).m_Ints.length;j++)
                         arow[j]=newTuples.get(i).m_Ints[j];
              }
              if(data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                    arow[newTuples.get(i).m_Ints.length]=1;
              else arow[0]=0;
              newTuples.get(i).m_Ints=arow;
          }
          
          for(int i=0;i<newTuples.size();i++)
              dataList.add(newTuples.get(i));
        
           data.setFromList(dataList);
                 data.setSchema(schema);
                 schema.setSettings(cset);

                 String name=outFolder;

        name+="\\JinputInitial.arff";
                 
        try{
                 writeArff(name, data);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }
         
    }

    public DataSetCreator(String path){
        DataPath=path;
        cset=new Settings(); 
    }
    
    HashSet<Integer> analyzeAttributes(double misPerc){
      HashSet<Integer> attrIndex=new HashSet<>();
      
      double missingPerc=0.0;
      int missing=0;
      ArrayList<DataTuple> dataList=data.toArrayList();
      
  
          for(int k=0;k<dataList.get(0).m_Doubles.length;k++){
              missing=0;   
              missingPerc=0.0;
              for(int j=0;j<data.getNbRows();j++){
                      if(dataList.get(j).m_Doubles[k]==Double.POSITIVE_INFINITY)
                          missing++;
          }
           
              missingPerc=(double)missing/data.getNbRows();
              if(missingPerc>=misPerc)
                  attrIndex.add(k+1);
      }
      
      return attrIndex;
    }
    
    public void removeAttributes(HashSet<Integer> attributeIndexes, String outFolder){
        
     ArrayList<DataTuple> dataList=data.toArrayList();
                RowData dat=data;
                ClusSchema sch=new ClusSchema(dat.getSchema().getRelationName());
               
                sch.addAttrType(schema.getAttrType(0));
                System.out.println("Number of doubles: "+dataList.get(0).m_Doubles.length);

                for(int i=1;i<schema.getNbAttributes()-1;i++)
                    if(!attributeIndexes.contains(i))
                        sch.addAttrType(schema.getAttrType(i+1));
               
                for(int j=0;j<data.getNbRows();j++){
                     double arow[]=new double[dataList.get(j).m_Doubles.length-attributeIndexes.size()];
                     int count=0;
                  for(int k=0;k<dataList.get(j).m_Doubles.length;k++){
                      if(!attributeIndexes.contains(k+1))
                       arow[count++]=dataList.get(j).m_Doubles[k];
                    }
                 dataList.get(j).m_Doubles=arow;
               }
                 dat.setFromList(dataList);
                 dat.setSchema(sch);
                 sch.setSettings(cset);  
                 
                 String out=outFolder+"\\JinputTmp.arff";
                 
                 try{
                 writeArff(out,data);
                 }
                 catch(Exception e){
                     e.printStackTrace();
                 }
    }
    
    public void readDataset()throws IOException{
     
        cread=new ClusReader(DataPath,cset); 
        //ClusRandom.initialize(cset);
        ARFFFile a=new ARFFFile(cread);
        try{
		schema = a.read(cset);
		schema.initialize();
		ClusView view = schema.createNormalView();
		 data = view.readData(cread, schema);
                 schema=data.getSchema();
               /* NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);
                NumericAttrType[] num = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL);
                System.out.println("Num nominal: "+nom.length);
                System.out.println("Num numeric: "+num.length);
               /* if(nom.length>0){
                    System.out.println(nom[0].m_Values[0]+" "+nom[0].m_Values[1]); 
                    System.out.println("Nominal array index: "+nom[0].getArrayIndex());
                    DataTuple tup=data.getTuple(0); 
                    System.out.println("att 2: "+tup.getIntVal(0));
                    System.out.println("Integer in tuple size: "+tup.m_Ints.length);
                    for(int i=0;i<tup.m_Ints.length;i++)
                        System.out.print(tup.m_Ints[i]+" ");
                    System.out.println();
                    for(int i=0;i<tup.m_Ints.length;i++)
                        System.out.print(nom[0].m_Values[tup.m_Ints[i]]+" ");
                    System.out.println();
                    
                    for(int i=0;i<schema.getNbAttributes();i++)
                        System.out.println(schema.getAttrType(i).getTypeName());
                }
                
                if(num.length>0){
                    for(int i=0;i<schema.getNbAttributes();i++)
                        System.out.println(schema.getAttrType(i).getTypeName());
                }*/
       // data=a.readArff(DataPath);
       // schema=data.getSchema();
        //System.out.println("data: "+data.getNbRows());
        //System.out.println("data: "+data.m_Schema.getRelationName());
        }
        catch(Exception e){
           e.printStackTrace();
        } 
            numExamples=data.getNbRows();
           cread.close();
           
           System.out.println("Read dataset num doubles: "+schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length);
    }
    
    
    public double getValue(int attId, int rowInd){//should count number of numeric before
        double val=0.0;
        int dIndex=-1;
        
        /*for(int i=1;i<=attId+1;i++){
            if(schema.getAttrType(i).getTypeName().contains("Numeric"))
                dIndex++;
        }*/
        
        dIndex=schema.getAttrType(attId+1).getArrayIndex();
   
        /*System.out.println("attId: "+attId);
        System.out.println("rowInd: "+rowInd);
        System.out.println("dIndex: "+dIndex);
        System.out.println("attr name: "+schema.getAttrType(attId+1).getName());
        System.out.println("attr type: "+schema.getAttrType(attId+1).getTypeName());*/
        
        val=data.getTuple(rowInd).getDoubleVal(dIndex);
       
        return val;
    }
    
    public String getValueCategorical(int attId, int rowInd){//should count number of categorical before
        String val="";
        int nIndex=-1;
        
        /*for(int i=1;i<=attId+1;i++)
            if(schema.getAttrType(i).getTypeName().contains("Nominal"))
                nIndex++;*/
        
        nIndex=schema.getAttrType(attId+1).getArrayIndex();//test if it works
        
        NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);
        
        DataTuple tup=data.getTuple(rowInd);
        //int vl=tup.m_Ints[nIndex];
        //NominalAttrType tN=nom[nIndex];
        //System.out.println("tup value: "+tup.m_Ints[nIndex]);
        if(tup.m_Ints[nIndex]>=nom[nIndex].m_Values.length)
            return "?";
        val=nom[nIndex].m_Values[tup.m_Ints[nIndex]];
        //val=data.getTuple(rowInd).getObjVal(attId);
        return val;
    }
    
    public void modifyDataset(int numRules, RuleReader RR,Mappings map,String Output) throws IOException{//not used but should be changed
        
           int naex=numExamples;
           int nARules=numRules;
        //locate the percentage of the rules to look at (that are specific enough) 
        //change the target label in the setting2.s file and save it
        //create a new .arff file with the corresponding target attributes
           
            ArrayList<DataTuple> dataList=data.toArrayList();

            System.out.println("New rule index: "+RR.newRuleIndex);
            System.out.println("Rule size: "+RR.rules.size());
            
            for(int i=RR.newRuleIndex;i<RR.rules.size();i++)
                if(RR.rules.get(i).elements.size()<=naex*0.2 && RR.rules.get(i).elements.size()>3)
            schema.addAttrType(new NumericAttrType("target"+(i+1-RR.newRuleIndex)));
        //schema.setClusteringAll(true);
            System.out.println("nARules: "+nARules);
            System.out.println("New number of attributes: "+schema.getNbAttributes());
        //Random rand=new Random();
         int count=0; 
           
        for (int j=0;j<data.getNbRows();j++){
            count=0;
        double arow[]=new double[schema.getNbAttributes()-1];
        for(int k=0;k<dataList.get(j).m_Doubles.length;k++){
            arow[k]=dataList.get(j).m_Doubles[k];
                    }
            for(int i=RR.newRuleIndex;i<RR.rules.size();i++)
                if(RR.rules.get(i).elements.size()<=naex*0.2 && RR.rules.get(i).elements.size()>3){
                    if(RR.rules.get(i).elements.contains(map.exampleId.get((String)dataList.get(j).m_Objects[0])))
                arow[dataList.get(j).m_Doubles.length+count]=1.0;
                    else
                         arow[dataList.get(j).m_Doubles.length+count]=0.0;
                    count++;
                }
            dataList.get(j).m_Doubles=arow;
        }
           
       data.setFromList(dataList);
       data.setSchema(schema);
       schema.setSettings(cset);
       
       try{
      // writeArffHeader(wrt, schema);
       writeArff(Output, data);
       }
       catch(Exception e){
           e.printStackTrace();
       }  
    }

       public void modifyDatasetCat(int numRules, double startPercentage, double endPercentage, int startIndex, int endIndex, int cuttof,int cuttofMax , RuleReader RR,String Output, Mappings map) throws IOException{

           int naex=numExamples;
           int nARules=numRules;
        //locate the percentage of the rules to look at (that are specific enough)
        //change the target label in the setting2.s file and save it
        //create a new .arff file with the corresponding target attributes

            ArrayList<DataTuple> dataList=data.toArrayList();

            System.out.println("New rule index: "+RR.newRuleIndex);
            System.out.println("Rule size: "+RR.rules.size());

            int lastDouble=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int prevDouble=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int inc=0;
            
            for(int i=startIndex;i<endIndex;i++)
                if(RR.rules.get(i).elements.size()<=naex*endPercentage && RR.rules.get(i).elements.size()>=naex*startPercentage && RR.rules.get(i).elements.size()>=cuttof && RR.rules.get(i).elements.size()<=cuttofMax){
            schema.addAttrType(new NominalAttrType("target"+(i+1-startIndex)));
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);//temporary should be generalized
            inc++;
                }
        //schema.setClusteringAll(true);
            System.out.println("nARules: "+nARules);
            System.out.println("New number of attributes: "+schema.getNbAttributes());
        //Random rand=new Random();
         int count=0;
         
         int l=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("New size: "+(l+inc));
         System.out.println("l: "+l);
         System.out.println("inc: "+inc);
         System.out.println("l+inc: "+(l+inc));//wrong!
         System.out.println("Old size: "+prevDouble);
         
        for (int j=0;j<data.getNbRows();j++){
            count=0;
         
        int arow[]=new int[l+inc];
        if(prevDouble>0){
            for(int k=0;k<dataList.get(j).m_Ints.length;k++){
                arow[k]=dataList.get(j).m_Ints[k];
                    }
        }

            for(int i=startIndex;i<endIndex;i++)
                if(RR.rules.get(i).elements.size()<=naex*endPercentage && RR.rules.get(i).elements.size()>=naex*startPercentage && RR.rules.get(i).elements.size()>=cuttof && RR.rules.get(i).elements.size()<=cuttofMax){
                    if(RR.rules.get(i).elements.contains(map.exampleId.get((String)dataList.get(j).m_Objects[0])))
                arow[l+count]=0;
                    else
                         arow[l+count]=1;
                    count++;
                }
            dataList.get(j).m_Ints=arow;
        }

       data.setFromList(dataList);
       data.setSchema(schema);
       schema.setSettings(cset);

       try{
      // writeArffHeader(wrt, schema);
       writeArff(Output, data);
       }
       catch(Exception e){
           e.printStackTrace();
       }
    }
       
       public void modifyDatasetCat(int numRules, double startPercentage, double endPercentage, int startIndex, int endIndex, int cuttof,int cuttofMax , ArrayList<Redescription> redescriptions,String Output, Mappings map) throws IOException{

           int naex=numExamples;
           int nARules=numRules;
        //locate the percentage of the rules to look at (that are specific enough)
        //change the target label in the setting2.s file and save it
        //create a new .arff file with the corresponding target attributes

            ArrayList<DataTuple> dataList=data.toArrayList();

            int lastDouble=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int prevDouble=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int inc=0;
            
            for(int i=startIndex;i<endIndex;i++)
                if(redescriptions.get(i).elements.size()<=naex*endPercentage && redescriptions.get(i).elements.size()>=naex*startPercentage && redescriptions.get(i).elements.size()>=cuttof && redescriptions.get(i).elements.size()<=cuttofMax){
            schema.addAttrType(new NominalAttrType("target"+(i+1-startIndex)));
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);//temporary should be generalized
            inc++;
                }
        //schema.setClusteringAll(true);
            System.out.println("nARules: "+nARules);
            System.out.println("New number of attributes: "+schema.getNbAttributes());
        //Random rand=new Random();
         int count=0;
         
         int l=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("New size: "+(l+inc));
         System.out.println("l: "+l);
         System.out.println("inc: "+inc);
         System.out.println("l+inc: "+(l+inc));//wrong!
         System.out.println("Old size: "+prevDouble);
         
        for (int j=0;j<data.getNbRows();j++){
            count=0;
         
        int arow[]=new int[l+inc];
        if(prevDouble>0){
            for(int k=0;k<dataList.get(j).m_Ints.length;k++){
                arow[k]=dataList.get(j).m_Ints[k];
                    }
        }

            for(int i=startIndex;i<endIndex;i++)
                if(redescriptions.get(i).elements.size()<=naex*endPercentage && redescriptions.get(i).elements.size()>=naex*startPercentage && redescriptions.get(i).elements.size()>=cuttof && redescriptions.get(i).elements.size()<=cuttofMax){
                    if(redescriptions.get(i).elements.contains(map.exampleId.get((String)dataList.get(j).m_Objects[0])))
                arow[l+count]=0;
                    else
                         arow[l+count]=1;
                    count++;
                }
            dataList.get(j).m_Ints=arow;
        }

       data.setFromList(dataList);
       data.setSchema(schema);
       schema.setSettings(cset);

       try{
      // writeArffHeader(wrt, schema);
       writeArff(Output, data);
       }
       catch(Exception e){
           e.printStackTrace();
       }
    }
       
       public void modifyDatasetS(int startIndex, int endIndex, RuleReader RR,String Output, Mappings map, ApplicationSettings appset) throws IOException{

           int naex=numExamples;
        //locate the percentage of the rules to look at (that are specific enough)
        //change the target label in the setting2.s file and save it
        //create a new .arff file with the corresponding target attributes

            ArrayList<DataTuple> dataList=data.toArrayList();

            System.out.println("New rule index: "+RR.newRuleIndex);
            System.out.println("Rule size: "+RR.rules.size());

            int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int prevDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int inc=0;
            
            for(int i=startIndex;i<endIndex;i++)
                if(RR.rules.get(i).elements.size()>=appset.minSupport){
            schema.addAttrType(new NumericAttrType("target"+(i+1-startIndex)));
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);//temporary should be generalized
            inc++;
                }
        //schema.setClusteringAll(true);
            System.out.println("nARules: "+inc);
            System.out.println("New number of attributes: "+schema.getNbAttributes());
        //Random rand=new Random();
         int count=0;
         
         int l=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("New size: "+(l+inc));
         System.out.println("l: "+l);
         System.out.println("inc: "+inc);
         System.out.println("l+inc: "+(l+inc));//wrong!
         System.out.println("Old size: "+prevDouble);
         
        for (int j=0;j<data.getNbRows();j++){
            count=0;
         
        double arow[]=new double[l+inc];
        if(prevDouble>0){
            for(int k=0;k<dataList.get(j).m_Doubles.length;k++){
                arow[k]=dataList.get(j).m_Doubles[k];
                    }
        }

            for(int i=startIndex;i<endIndex;i++)
                if(RR.rules.get(i).elements.size()>=appset.minSupport){
                    if(RR.rules.get(i).elements.contains(map.exampleId.get((String)dataList.get(j).m_Objects[0])))
                arow[l+count]=1.0;
                    else
                         arow[l+count]=0.0;
                    count++;
                }
            dataList.get(j).m_Doubles=arow;
        }

       data.setFromList(dataList);
       data.setSchema(schema);
       schema.setSettings(cset);

       try{
      // writeArffHeader(wrt, schema);
       writeArff(Output, data);
       }
       catch(Exception e){
           e.printStackTrace();
       }
    }
       
       public void modifyDatasetCat(int startIndex, int endIndex, RuleReader RR,String Output, Mappings map, ApplicationSettings appset) throws IOException{

           int naex=numExamples;
          // int nARules=numRules;
        //locate the percentage of the rules to look at (that are specific enough)
        //change the target label in the setting2.s file and save it
        //create a new .arff file with the corresponding target attributes

            ArrayList<DataTuple> dataList=data.toArrayList();

            System.out.println("New rule index: "+RR.newRuleIndex);
            System.out.println("Rule size: "+RR.rules.size());

            int lastDouble=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int prevDouble=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int inc=0;
            
            for(int i=startIndex;i<endIndex;i++)
                if(RR.rules.get(i).elements.size()>=appset.minSupport){
            schema.addAttrType(new NominalAttrType("target"+(i+1-startIndex)));
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);//temporary should be generalized
            inc++;
                }
        //schema.setClusteringAll(true);
            System.out.println("nARules: "+inc);
            System.out.println("New number of attributes: "+schema.getNbAttributes());
        //Random rand=new Random();
         int count=0;
         
         int l=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("New size: "+(l+inc));
         System.out.println("l: "+l);
         System.out.println("inc: "+inc);
         System.out.println("l+inc: "+(l+inc));//wrong!
         System.out.println("Old size: "+prevDouble);
         
        for (int j=0;j<data.getNbRows();j++){
            count=0;
         
        int arow[]=new int[l+inc];
        if(prevDouble>0){
            for(int k=0;k<dataList.get(j).m_Ints.length;k++){
                arow[k]=dataList.get(j).m_Ints[k];
                    }
        }

            for(int i=startIndex;i<endIndex;i++)
                if(RR.rules.get(i).elements.size()>=appset.minSupport){
                    if(RR.rules.get(i).elements.contains(map.exampleId.get((String)dataList.get(j).m_Objects[0])))
                arow[l+count]=0;
                    else
                         arow[l+count]=1;
                    count++;
                }
            dataList.get(j).m_Ints=arow;
        }

       data.setFromList(dataList);
       data.setSchema(schema);
       schema.setSettings(cset);

       try{
      // writeArffHeader(wrt, schema);
       writeArff(Output, data);
       }
       catch(Exception e){
           e.printStackTrace();
       }
    }
       
       public void modifyDatasetS(int startIndex, int endIndex,ArrayList<Redescription> redescriptions,String Output, Mappings map,ApplicationSettings appset) throws IOException{

           int naex=numExamples;
        //locate the percentage of the rules to look at (that are specific enough)
        //change the target label in the setting2.s file and save it
        //create a new .arff file with the corresponding target attributes

            ArrayList<DataTuple> dataList=data.toArrayList();

            int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int prevDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int inc=0;
            
            for(int i=startIndex;i<endIndex;i++)
                if(redescriptions.get(i).elements.size()>=appset.minSupport){
            schema.addAttrType(new NumericAttrType("target"+(i+1-startIndex)));
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);//temporary should be generalized
            inc++;
                }
        //schema.setClusteringAll(true);
        //Random rand=new Random();
         int count=0;
         
         int l=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("New size: "+(l+inc));
         System.out.println("l: "+l);
         System.out.println("inc: "+inc);
         System.out.println("l+inc: "+(l+inc));//wrong!
         System.out.println("Old size: "+prevDouble);
         
        for (int j=0;j<data.getNbRows();j++){
            count=0;
         
        double arow[]=new double[l+inc];
        if(prevDouble>0){
            for(int k=0;k<dataList.get(j).m_Doubles.length;k++){
                arow[k]=dataList.get(j).m_Doubles[k];
                    }
        }

            for(int i=startIndex;i<endIndex;i++)
                if(redescriptions.get(i).elements.size()>=appset.minSupport){
                    if(redescriptions.get(i).elements.contains(map.exampleId.get((String)dataList.get(j).m_Objects[0])))
                arow[l+count]=1.0;
                    else
                         arow[l+count]=0.0;
                    count++;
                }
            dataList.get(j).m_Doubles=arow;
        }

       data.setFromList(dataList);
       data.setSchema(schema);
       schema.setSettings(cset);

       try{
      // writeArffHeader(wrt, schema);
       writeArff(Output, data);
       }
       catch(Exception e){
           e.printStackTrace();
       }
    }
       
        public void modifyDatasetCat(int startIndex, int endIndex, ArrayList<Redescription> redescriptions,String Output, Mappings map, ApplicationSettings appset) throws IOException{

           int naex=numExamples;
        //locate the percentage of the rules to look at (that are specific enough)
        //change the target label in the setting2.s file and save it
        //create a new .arff file with the corresponding target attributes

            ArrayList<DataTuple> dataList=data.toArrayList();

            int lastDouble=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int prevDouble=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int inc=0;
            
            for(int i=startIndex;i<endIndex;i++)
                if(redescriptions.get(i).elements.size()>=appset.minSupport){
            schema.addAttrType(new NominalAttrType("target"+(i+1-startIndex)));
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);//temporary should be generalized
            inc++;
                }
        //schema.setClusteringAll(true);
            System.out.println("New number of attributes: "+schema.getNbAttributes());
        //Random rand=new Random();
         int count=0;
         
         int l=schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("New size: "+(l+inc));
         System.out.println("l: "+l);
         System.out.println("inc: "+inc);
         System.out.println("l+inc: "+(l+inc));//wrong!
         System.out.println("Old size: "+prevDouble);
         
        for (int j=0;j<data.getNbRows();j++){
            count=0;
         
        int arow[]=new int[l+inc];
        if(prevDouble>0){
            for(int k=0;k<dataList.get(j).m_Ints.length;k++){
                arow[k]=dataList.get(j).m_Ints[k];
                    }
        }

            for(int i=startIndex;i<endIndex;i++)
                if(redescriptions.get(i).elements.size()>=appset.minSupport){
                    if(redescriptions.get(i).elements.contains(map.exampleId.get((String)dataList.get(j).m_Objects[0])))
                arow[l+count]=0;
                    else
                         arow[l+count]=1;
                    count++;
                }
            dataList.get(j).m_Ints=arow;
        }

       data.setFromList(dataList);
       data.setSchema(schema);
       schema.setSettings(cset);

       try{
      // writeArffHeader(wrt, schema);
       writeArff(Output, data);
       }
       catch(Exception e){
           e.printStackTrace();
       }
    }

       public void modifyDatasetS(int numRules, double startPercentage, double endPercentage, int startIndex, int endIndex, int cuttof,int cuttofMax , RuleReader RR,String Output, Mappings map) throws IOException{

           int naex=numExamples;
           int nARules=numRules;
        //locate the percentage of the rules to look at (that are specific enough)
        //change the target label in the setting2.s file and save it
        //create a new .arff file with the corresponding target attributes

            ArrayList<DataTuple> dataList=data.toArrayList();

            System.out.println("New rule index: "+RR.newRuleIndex);
            System.out.println("Rule size: "+RR.rules.size());

            int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int prevDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int inc=0;
            
            for(int i=startIndex;i<endIndex;i++)
                if(RR.rules.get(i).elements.size()<=naex*endPercentage && RR.rules.get(i).elements.size()>=naex*startPercentage /*&& RR.rules.get(i).elements.size()>=cuttof && RR.rules.get(i).elements.size()<=cuttofMax*/){
            schema.addAttrType(new NumericAttrType("target"+(i+1-startIndex)));
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);//temporary should be generalized
            inc++;
                }
        //schema.setClusteringAll(true);
            System.out.println("nARules: "+nARules);
            System.out.println("New number of attributes: "+schema.getNbAttributes());
        //Random rand=new Random();
         int count=0;
         
         int l=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("New size: "+(l+inc));
         System.out.println("l: "+l);
         System.out.println("inc: "+inc);
         System.out.println("l+inc: "+(l+inc));//wrong!
         System.out.println("Old size: "+prevDouble);
         
        for (int j=0;j<data.getNbRows();j++){
            count=0;
         
        double arow[]=new double[l+inc];
        if(prevDouble>0){
            for(int k=0;k<dataList.get(j).m_Doubles.length;k++){
                arow[k]=dataList.get(j).m_Doubles[k];
                    }
        }

            for(int i=startIndex;i<endIndex;i++)
                if(RR.rules.get(i).elements.size()<=naex*endPercentage && RR.rules.get(i).elements.size()>=naex*startPercentage /*&& RR.rules.get(i).elements.size()>=cuttof && RR.rules.get(i).elements.size()<=cuttofMax*/){
                    if(RR.rules.get(i).elements.contains(map.exampleId.get((String)dataList.get(j).m_Objects[0])))
                arow[l+count]=1.0;
                    else
                         arow[l+count]=0.0;
                    count++;
                }
            dataList.get(j).m_Doubles=arow;
        }

       data.setFromList(dataList);
       data.setSchema(schema);
       schema.setSettings(cset);

       try{
      // writeArffHeader(wrt, schema);
       writeArff(Output, data);
       }
       catch(Exception e){
           e.printStackTrace();
       }
    }
       
       public void modifyDatasetS(int numRules, double startPercentage, double endPercentage, int startIndex, int endIndex, int cuttof,int cuttofMax , ArrayList<Redescription> redescriptions,String Output, Mappings map) throws IOException{

           int naex=numExamples;
           int nARules=numRules;
        //locate the percentage of the rules to look at (that are specific enough)
        //change the target label in the setting2.s file and save it
        //create a new .arff file with the corresponding target attributes

            ArrayList<DataTuple> dataList=data.toArrayList();

            int lastDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int prevDouble=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
            int inc=0;
            
            for(int i=startIndex;i<endIndex;i++)
                if(redescriptions.get(i).elements.size()<=naex*endPercentage && redescriptions.get(i).elements.size()>=naex*startPercentage && redescriptions.get(i).elements.size()>=cuttof && redescriptions.get(i).elements.size()<=cuttofMax){
            schema.addAttrType(new NumericAttrType("target"+(i+1-startIndex)));
            schema.getAttrType(schema.getNbAttributes()-1).setArrayIndex(lastDouble++);//temporary should be generalized
            inc++;
                }
        //schema.setClusteringAll(true);
            System.out.println("nARules: "+nARules);
            System.out.println("New number of attributes: "+schema.getNbAttributes());
        //Random rand=new Random();
         int count=0;
         
         int l=schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL).length;
         System.out.println("New size: "+(l+inc));
         System.out.println("l: "+l);
         System.out.println("inc: "+inc);
         System.out.println("l+inc: "+(l+inc));//wrong!
         System.out.println("Old size: "+prevDouble);
         
        for (int j=0;j<data.getNbRows();j++){
            count=0;
         
        double arow[]=new double[l+inc];
        if(prevDouble>0){
            for(int k=0;k<dataList.get(j).m_Doubles.length;k++){
                arow[k]=dataList.get(j).m_Doubles[k];
                    }
        }

            for(int i=startIndex;i<endIndex;i++)
                if(redescriptions.get(i).elements.size()<=naex*endPercentage && redescriptions.get(i).elements.size()>=naex*startPercentage && redescriptions.get(i).elements.size()>=cuttof && redescriptions.get(i).elements.size()<=cuttofMax){
                    if(redescriptions.get(i).elements.contains(map.exampleId.get((String)dataList.get(j).m_Objects[0])))
                arow[l+count]=1.0;
                    else
                         arow[l+count]=0.0;
                    count++;
                }
            dataList.get(j).m_Doubles=arow;
        }

       data.setFromList(dataList);
       data.setSchema(schema);
       schema.setSettings(cset);

       try{
      // writeArffHeader(wrt, schema);
       writeArff(Output, data);
       }
       catch(Exception e){
           e.printStackTrace();
       }
    }
       
    public void splitDataset(ArrayList<Integer> splitIndex, String outFolder){
        int oldIndex=0, numCreated=1;

        System.out.println("splitIndex size: "+splitIndex.size());

        for(int index:splitIndex){
                //Settings set=new Settings();
                //settings.add(set);
            try{
            readDataset();
            }
            catch(IOException e){
                e.printStackTrace();
            }

             ArrayList<DataTuple> dataList=data.toArrayList();
                RowData dat=new RowData(data);
                ClusSchema sch=new ClusSchema(dat.getSchema().getRelationName());
                System.out.println("nb attr: "+schema.getNbAttributes());
               // System.out.println("attribute: "+schema.getAttrType(0));
                sch.addAttrType(schema.getAttrType(0));
                System.out.println("Number of doubles: "+dataList.get(0).m_Doubles.length);

                for(int i=oldIndex;i<index;i++)
                    sch.addAttrType(schema.getAttrType(i+1));

               
                for(int j=0;j<data.getNbRows();j++){
                     double arow[]=new double[index-oldIndex];
                  for(int k=oldIndex;k<index;k++){
                       arow[k-oldIndex]=dataList.get(j).m_Doubles[k];
                    }
                 dataList.get(j).m_Doubles=arow;
               }


                 dat.setFromList(dataList);
                 dat.setSchema(sch);
                 sch.setSettings(cset);

                 try{
                 writeArff(outFolder+"\\input"+(numCreated)+".arff", dat);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }

                 oldIndex=index;
                 numCreated++;

                 if((numCreated-1)==splitIndex.size()){

                     try{
            readDataset();
            }
            catch(IOException e){
                e.printStackTrace();
            }

                     System.out.println("Number of doubles: "+dataList.get(0).m_Doubles.length);
                     index=schema.getNbAttributes()-1;
                      dataList=data.toArrayList();
                      dat=new RowData(data);
                      sch=new ClusSchema(dat.getSchema().getRelationName());
                sch.clearAttributeStatusClusteringAndTarget();
                sch.addAttrType(schema.getAttrType(0));

                for(int i=oldIndex;i<index;i++)
                    sch.addAttrType(schema.getAttrType(i+1));

                  
                for(int j=0;j<data.getNbRows();j++){
                  double arow[]=new double[index-oldIndex];
                  for(int k=oldIndex;k<index;k++){
                       arow[k-oldIndex]=dataList.get(j).m_Doubles[k];
                    }
                 dataList.get(j).m_Doubles=arow;
               }
                 dat.setFromList(dataList);
                 dat.setSchema(sch);
                 sch.setSettings(cset);
                 oldIndex=index;

                 try{
                 writeArff(outFolder+"\\input"+(numCreated)+".arff", dat);
            }
                 catch(Exception e){
                     e.printStackTrace();
                 }

                 }
        }

    }
    
            public static void writeArff(String fname, RowData data) throws IOException, ClusException {
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		ClusSchema schema = data.getSchema();
		ARFFFile.writeArffHeader(wrt, schema);
                ARFFFile.writeArff(fname, data);
		/*wrt.println("@DATA");
		for (int j = 0; j < data.getNbRows(); j++) {
			DataTuple tuple = data.getTuple(j);
                        wrt.write(tuple.m_Objects[0]+",");
                        for(int k=0;k<tuple.m_Doubles.length;k++)
                            if(k+1<tuple.m_Doubles.length)
                                if(Double.isInfinite(tuple.m_Doubles[k]))
                                    wrt.write("?"+",");
                                else
                                     wrt.write(""+tuple.m_Doubles[k]+",");
                        else
                                if(Double.isInfinite(tuple.m_Doubles[k]))
                                     wrt.write("?"+"\n");
                                else
                                    wrt.write(""+tuple.m_Doubles[k]+"\n");
			//tuple.writeTuple(wrt);
		}*/
		wrt.close();
	}   
}
