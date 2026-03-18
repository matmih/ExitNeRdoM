/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.data.type.NominalAttrType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import org.javatuples.Pair;

/**
 *
 * @author matej
 */
public class AttributeBatchRedescriptionMW implements Runnable{
    int threadID, start, end;
    ApplicationSettings appset;
    Mappings fid;
    DataSetCreator datJ;
    final Object lock;
    
   public AttributeBatchRedescriptionMW(int startIndex, int endIndex, int tID, ApplicationSettings app, Mappings m, DataSetCreator d, final Object l){
       threadID = tID;
       start = startIndex;
       end = endIndex;
       appset = app;
       fid = m;
       datJ = d;
       lock = l;
   }
   
   //should be implemented
   //needs modifications
   @Override public void run(){
                long startTime = System.currentTimeMillis();

        System.out.println("Num targets: "+appset.numTargets);
        System.out.println("Num trees in RS: "+appset.numTreesinForest);
        System.out.println("Average tree depth in RS: "+appset.aTreeDepth);
        System.out.println("Allow left side rule negation: "+appset.leftNegation);
        System.out.println("Allow right side rule negation: "+appset.rightNegation);
        System.out.println("Allow left side rule disjunction: "+appset.leftDisjunction);
        System.out.println("Allow right side rule disjunction: "+appset.rightDisjunction);
        System.out.println("Types of LSTrees: "+appset.treeTypes.get(0));
        System.out.println("Types of RSTrees: "+appset.treeTypes.get(1));
        System.out.println("Use Network information: "+appset.useNC.toString());
        System.out.println("Spatial matrix: "+appset.spatialMatrix.toString());
        System.out.println("Spatial measure: "+appset.spatialMeasures.toString());
        
        
        System.out.println("Attribute importance gen: ");
        for(int i=0;i<appset.attributeImportanceGen.size();i++)
              System.out.print(appset.attributeImportanceGen.get(i)+" ");
        System.out.println();
        
        System.out.println("Important attributes: ");
        for(int i=0;i<appset.importantAttributes.size();i++){
            for(int j=0;j<appset.importantAttributes.get(i).size();j++){
                for(int k=0;k<appset.importantAttributes.get(i).get(j).size();k++){
                    if(k<appset.importantAttributes.get(i).get(j).size())
                        System.out.print(appset.importantAttributes.get(i).get(j).get(k)+" , ");
                }
                System.out.print(" + ");
            }
        System.out.println();
        }
        
        NHMCDistanceMatrix nclMatInit=null;
         Random r=new Random();
        //RedescriptionSet rs=new RedescriptionSet();
         RedescriptionSet rtmp = new RedescriptionSet();
        RuleReader rr=new RuleReader();
        RuleReader rr1=new RuleReader();
         RuleReader rrFA=new RuleReader();
         boolean oom[]= new boolean[1];
         Jacard jsN[]=new Jacard[3];
           for(int i=0;i<jsN.length;i++)
            jsN[i]=new Jacard();
         
        int elemFreq[]=null;
        int attrFreq[]=null;
        ArrayList<Double> redScores=null;
        ArrayList<Double> redScoresAtt=null;
        ArrayList<Double> targetAtScore=null;
        ArrayList<Double> redDistCoverage=null;
        ArrayList<Double> redDistCoverageAt=null;
         double Statistics[]={0.0,0.0,0.0};//previousMedian - 0, numberIterationsStable - 1, minDifference - 2
         ArrayList<Double> maxDiffScoreDistribution = null;
          ArrayList<Double> redDistNetwork=null;
      
       if(appset.optimizationType == 0){
        if(appset.redesSetSizeType==1 && appset.numRetRed!=Integer.MAX_VALUE)
            appset.numInitial=appset.numRetRed;
        else{
            if(appset.numRetRed!=Integer.MAX_VALUE && appset.numRetRed!=-1)
                appset.numInitial=appset.numRetRed;
            else
                appset.numInitial=20;
        }
       }
       
       if(appset.optimizationType==0){
            
                   
         elemFreq=new int[datJ.numExamples];
         attrFreq=new int[datJ.schema.getNbAttributes()];  
            
          System.out.println("Number of redescriptions: "+appset.numInitial);
        
        redScores=new ArrayList<>(appset.numInitial);
        redScoresAtt=new ArrayList<>(appset.numInitial);
        redDistCoverage=new ArrayList<>(appset.numInitial);
        redDistCoverageAt=new ArrayList<>(appset.numInitial);
     
         targetAtScore=null;
        //double Statistics[]={0.0,0.0,0.0};//previousMedian - 0, numberIterationsStable - 1, minDifference - 2
        maxDiffScoreDistribution=new ArrayList<>(appset.numInitial);
        
        if(appset.attributeImportance!=0)
            targetAtScore = new ArrayList<>(appset.numInitial);
        
        for(int z=0;z<appset.numInitial;z++){
            redScores.add(Double.NaN);
            redScoresAtt.add(Double.NaN);
            redDistCoverage.add(Double.NaN);
            redDistCoverageAt.add(Double.NaN);
            maxDiffScoreDistribution.add(Double.NaN);
            if(appset.attributeImportance!=0)
                targetAtScore.add(Double.NaN);
        }
      }
        
         DataSetCreator datJInit=null;
          
       if(!appset.useSplitTesting){ 
        if(appset.initClusteringFileName.equals("")){
            if(appset.system.equals("windows"))
                datJInit = new DataSetCreator(appset.outFolderPath+"\\Jinput.arff");
            else
                datJInit = new DataSetCreator(appset.outFolderPath+"/Jinput.arff");
        }
       }
       
    Path copied = Paths.get(appset.outFolderPath+"\\JinputBack"+threadID+".arff");
    Path originalPath = Paths.get(appset.outFolderPath+"\\Jinput.arff");
    
    if(!appset.system.equals("windows")){
        copied = Paths.get(appset.outFolderPath+"/JinputBack"+threadID+".arff");
        originalPath = Paths.get(appset.outFolderPath+"/Jinput.arff");
    }
    //svaka dretva si napravi kopiju Jinput -> svaka radi sa svojim
       
       synchronized(lock){
                try{
        datJInit.readDataset();
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        }
        catch(IOException e){
            e.printStackTrace();
        }
       }
        
        datJInit.W2indexs.addAll(datJ.W2indexs);
      
        System.out.println("Number of attributes: "+datJInit.schema.getNbAttributes());
        
        int numAttr = datJInit.schema.getNbAttributes()-1;
        //add blank target attribute, modify values in each iteration
        datJInit.schema.addAttrType(new NominalAttrType("target"));
         int lastNominal=datJInit.schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
        datJInit.schema.getAttrType(datJInit.schema.getNbAttributes()-1).setArrayIndex(lastNominal++);
    
          ArrayList<DataTuple> dataList=datJInit.data.toArrayList(); 
          for(int el = 0;el<dataList.size();el++){
              int arow[];
              if(datJInit.schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                arow=new int[dataList.get(el).m_Ints.length+1];
              else
                  arow=new int[1];
                if(datJInit.schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                    for(int kk=0;kk<dataList.get(el).m_Ints.length;kk++){
                    arow[kk] = dataList.get(el).m_Ints[kk];
                }
                arow[dataList.get(el).m_Ints.length]=0;
                }
              else
                  arow[0]=0;
                dataList.get(el).m_Ints=arow;
          }
        
        int sView = 0;  
        HashMap<Integer,Pair<Double,Double>> bins = null;
        HashSet<Integer> usedViews  = new HashSet<>();  
          for(int at=start;at<=end;at++){
            
            if(at>0){
                if(!appset.useSplitTesting){ 
        if(appset.initClusteringFileName.equals("")){
            if(appset.system.equals("windows"))
                datJInit = new DataSetCreator(appset.outFolderPath+"\\JinputBack"+threadID+".arff");
            else
                datJInit = new DataSetCreator(appset.outFolderPath+"/JinputBack"+threadID+".arff");
        }
       }
        
               // synchronized(lock){
                try{
        datJInit.readDataset();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        //        }
        
        datJInit.W2indexs.addAll(datJ.W2indexs);
        
        datJInit.schema.addAttrType(new NominalAttrType("target"));
         lastNominal=datJInit.schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length;
        datJInit.schema.getAttrType(datJInit.schema.getNbAttributes()-1).setArrayIndex(lastNominal++);
    
          dataList=datJInit.data.toArrayList(); 
          
          for(int el = 0;el<dataList.size();el++){
              int arow[];
              if(datJInit.schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0){
                arow=new int[dataList.get(el).m_Ints.length+1];
                for(int kk=0;kk<dataList.get(el).m_Ints.length;kk++){
                    arow[kk] = dataList.get(el).m_Ints[kk];
                }
              }
              else
                  arow=new int[1];
                if(datJInit.schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL).length>0)
                arow[dataList.get(el).m_Ints.length]=0;
              else
                  arow[0]=0;
                dataList.get(el).m_Ints=arow;
                
          }
      
            }
            
            RedescriptionSet rttmp = new RedescriptionSet();
             rr=new RuleReader();
             rr1=new RuleReader();
             rrFA=new RuleReader();
           
            System.out.println(datJInit.schema.getAttrType(at+1).getName()+" "+datJInit.schema.getAttrType(at+1).getTypeName());
            ClusAttrType t=datJInit.schema.getAttrType(at+1);
           // appset.importantAttributes.add(new ArrayList<String>());
            
        //can be generalized to mw by using sView = datJInit.W2indexs.get(i)
       
            int f = 0;
         for(int ii=0;ii<datJInit.W2indexs.size();ii++) //OVE INDEKSE TREBA TOCNO NAMJESTITI I DOBRO TESTIRATI!!!!
             if((at+1)<(datJInit.W2indexs.get(ii)-1)){//was -2 before, testing in 2 view setting
                 sView = ii;
                 f = 1;
                 break;
             }
         
         if(f == 0) sView = datJInit.W2indexs.size();
         
       System.out.println("Attribute - view: ");
       System.out.println(at+" "+sView);

       String s;
      
        int test = 1;
        int clustMode = appset.clusteringMode;

         if(appset.initClusteringFileName.equals(""))//zamijeniti sa initialClusteringGenAttribute
            if(clustMode == 1)
             bins = datJInit.initialClusteringGenAttribute2BinsMT(appset.outFolderPath,appset,datJ.schema.getNbDescriptiveAttributes(), at+1, threadID, r, fid);
        else if(clustMode == 0)  
            bins = datJInit.initialClusteringGenAttributeMT(appset.outFolderPath,appset,datJ.schema.getNbDescriptiveAttributes(), at+1, threadID, r, fid);
        else if(clustMode == 2){//equal frequency binning
           bins = datJInit.initialClusteringGenAttributeEqFreqMT(appset.outFolderPath,appset,datJ.schema.getNbDescriptiveAttributes(), at+1, threadID, r, fid); 
        }
        else if(clustMode == 3){//quantile binning
        bins = datJInit.initialClusteringGenAttributeKMeansMT(appset.outFolderPath,appset,datJ.schema.getNbDescriptiveAttributes(), at+1, threadID, r, fid);       
        }
            //datJInit.initialClusteringGen1(appset.outFolderPath,appset,datJ.schema.getNbDescriptiveAttributes(),r);
       
        double min = Double.MAX_VALUE;
        
        for(int z=0;z<bins.keySet().size();z++){
            if(!bins.containsKey(z)) continue;
            System.out.println("Strange int value: "+bins.get(z).getValue0());
            System.out.println(bins.get(z));
            System.out.println("Klasa: "+bins.get(z).getValue0().getClass().getName());
            double bm = bins.get(z).getValue0();
            if(bm<min)
                min = bm;
        }
        
        int first = -1;
        for(int z=0;z<bins.keySet().size();z++){
            if(t.getTypeName().toLowerCase().contains("numeric")){
                String rule = t.getName()+" > "+bins.get(z).getValue0();
                if(bins.get(z).getValue0() == min)
                    first = 1;
                 Rule rn = new Rule(rule, fid);
                 System.out.println("f "+first+" il "+bins.get(z).getValue0()+" ih "+bins.get(z).getValue1());
                 rn.computeEntities(datJInit, fid, at, bins.get(z).getValue0(), bins.get(z).getValue1(), first, 1);
                 rn.closeInterval(datJ, fid);
                 if(rn.rule.contains(""))
                 System.out.println("Rule test support: "+rn.elements.size());
                if(sView == 0)
                    rr1.rules.add(rn);
                else rr.rules.add(rn);
            }
            else{
                String rule = t.getName()+" = "+fid.cattAtt.get(at).getValue1().get(z);
                Rule rn = new Rule(rule, fid);
                rn.computeEntities(datJInit, fid, at, bins.get(z).getValue0(), bins.get(z).getValue1(), first, 0);
                rn.closeInterval(datJ, fid);
                if(sView == 0)
                    rr1.rules.add(rn);
                else rr.rules.add(rn);
            }
        }
        
        System.out.println("Number of rules after binning: ");
        System.out.println(rr1.rules.size()+" "+rr.rules.size());
        
        //create rules from bins or target categories
        //rr->view1, rr1->view2
        //gornja granica uključena [] kod binova
        
        SettingsReader initSettings=new SettingsReader();
        SettingsReader initSettings1=new SettingsReader();
        
        if(appset.initClusteringFileName.equals(""))
             if(appset.system.equals("windows"))
                 initSettings.setDataFilePath(appset.outFolderPath+"\\JinputInitial"+threadID+".arff");
             else
                  initSettings.setDataFilePath(appset.outFolderPath+"/JinputInitial"+threadID+".arff");
        else{
            if(appset.system.equals("windows"))
                 initSettings.setDataFilePath(appset.outFolderPath+"\\"+appset.initClusteringFileName);
            else
                initSettings.setDataFilePath(appset.initClusteringFileName);
        }
        
        System.out.println("WIndexes size: "+datJ.W2indexs.size());
     
      if(sView>0){  
        if(appset.system.equals("windows"))
             initSettings.setPath(appset.outFolderPath+"\\view1"+threadID+".s");
        else
             initSettings.setPath(appset.outFolderPath+"/view1"+threadID+".s");
        initSettings.createInitialSettings1(1, datJ.W2indexs.get(0), datJInit.schema.getNbAttributes(), appset);
        System.out.println("distance file size: "+appset.distanceFilePaths.size()+"");
        System.out.println("use nc: "+appset.useNC.size());
        if(appset.useNC.get(0) == false)
             initSettings.createInitialSettingsGen(0, 3, datJ.W2indexs.get(0), datJ.schema.getNbAttributes(), appset,1);
        else
             initSettings.createInitialSettingsGen(0, 4, datJ.W2indexs.get(0), datJ.schema.getNbAttributes(), appset,1);
        
        if(appset.system.equals("windows"))
             initSettings1.setPath(appset.outFolderPath+"\\view2"+threadID+".s");
        else
             initSettings1.setPath(appset.outFolderPath+"/view2"+threadID+".s");
        initSettings1.createInitialSettings1(0, datJ.W2indexs.get(0), datJInit.schema.getNbAttributes(), appset);
        System.out.println("distance file size: "+appset.distanceFilePaths.size()+"");
        System.out.println("use nc: "+appset.useNC.size());
        if(datJ.W2indexs.size()>1)
            initSettings1.createInitialSettingsGen(1, datJ.W2indexs.get(0)+1, datJ.W2indexs.get(1), datJ.schema.getNbAttributes(), appset,1);
        else
            initSettings1.createInitialSettingsGen(1, datJ.W2indexs.get(0)+1, datJInit.schema.getNbAttributes(), datJ.schema.getNbAttributes(), appset,1);
        
        
      }
      
        ClusProcessExecutor exec=new ClusProcessExecutor();

        if(sView >0){ 
        //RunInitW1S1
        exec.run(appset.javaPath,appset.clusPath ,appset.outFolderPath,"view1"+threadID+".s",0, appset.clusteringMemory);//was 1 before for rules
        System.out.println("Process 1 side 1 finished!");
        } 
        //read the rules obtained from first attribute set
          String input1="";
           String inputFa="";
       if(sView >0){ 
          if(appset.system.equals("windows"))
             input1=appset.outFolderPath+"\\view1"+threadID+".out";
          else
              input1=appset.outFolderPath+"/view1"+threadID+".out"; 
           
           rr1.extractRules(input1,fid,datJInit,appset);
      }
      
        SettingsReader set=null;
        SettingsReader set1=null;
        SettingsReader setF=null;
        SettingsReader setF1=null;
   
      if(sView == 0){   
        //RunInitW1S2
        if(appset.system.equals("windows")){
            initSettings.setPath(appset.outFolderPath+"\\view2"+threadID+".s");
        }
        else{
           initSettings.setPath(appset.outFolderPath+"/view2"+threadID+".s"); 
        }
        
        if(appset.system.equals("windows"))
             initSettings1.setPath(appset.outFolderPath+"\\view1"+threadID+".s");
        else
             initSettings1.setPath(appset.outFolderPath+"/view1"+threadID+".s");
        
        initSettings1.createInitialSettings1(1, datJ.W2indexs.get(0), datJInit.schema.getNbAttributes(), appset);
        if(appset.useNC.get(0) == false)
             initSettings1.createInitialSettingsGen(0, 3, datJ.W2indexs.get(0), datJ.schema.getNbAttributes(), appset,1);
        else
             initSettings1.createInitialSettingsGen(0, 4, datJ.W2indexs.get(0), datJ.schema.getNbAttributes(), appset,1);
        
       
        if(datJ.W2indexs.size()>1)
            initSettings.createInitialSettingsGen(1, datJ.W2indexs.get(0)+1, datJ.W2indexs.get(1), datJ.schema.getNbAttributes(), appset,1);
        else
            initSettings.createInitialSettingsGen(1, datJ.W2indexs.get(0)+1, datJInit.schema.getNbAttributes(), datJ.schema.getNbAttributes(), appset,1);
        
        exec.run(appset.javaPath,appset.clusPath, appset.outFolderPath, "view2"+threadID+".s", 0,appset.clusteringMemory);//was 1 before
        System.out.println("Process 1 side 2 finished!");

        //read the rules obtained from first attribute set
       if(appset.system.equals("windows"))
        input1=appset.outFolderPath+"\\view2"+threadID+".out";
       else
           input1=appset.outFolderPath+"/view2"+threadID+".out";
        rr.extractRules(input1,fid,datJInit,appset);
      }
      
      
       if(appset.numSupplementTrees>0){
            if(sView == 0){
                 if(appset.system.equals("windows")){
                     setF=new SettingsReader(appset.outFolderPath+"\\view2tmpF"+threadID+".s",appset.outFolderPath+"\\view2"+threadID+".s");
                     setF.setDataFilePath(appset.outFolderPath+"\\JinputInitial"+threadID+".arff");
                 }
                 else{
                     setF=new SettingsReader(appset.outFolderPath+"/view2tmpF"+threadID+".s",appset.outFolderPath+"/view2"+threadID+".s");
                     setF.setDataFilePath(appset.outFolderPath+"/JinputInitial"+threadID+".arff");
                 }
            }
            else{
              if(appset.system.equals("windows")){
                     setF=new SettingsReader(appset.outFolderPath+"\\view1tmpF"+threadID+".s",appset.outFolderPath+"\\view1"+threadID+".s");
                     setF.setDataFilePath(appset.outFolderPath+"\\JinputInitial"+threadID+".arff");
                 }
                 else{
                     setF=new SettingsReader(appset.outFolderPath+"/view1tmpF"+threadID+".s",appset.outFolderPath+"/view1"+threadID+".s");
                     setF.setDataFilePath(appset.outFolderPath+"/JinputInitial"+threadID+".arff");
                 }  
            }
            
             }
       
       if(appset.numSupplementTrees>0)
                    setF.ModifySettingsF(0,datJ.schema.getNbAttributes(),appset);
   
       if(appset.numSupplementTrees>0){
           if(sView == 0)
                exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath, "view2tmpF"+threadID+".s", 0,appset.clusteringMemory);//was 1 for rules before
           else{
                exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath, "view1tmpF"+threadID+".s", 0,appset.clusteringMemory);//was 1 for rules before
           } 
       }
       
        if(appset.numSupplementTrees>0){ 
            if(sView ==0){
                if(appset.system.equals("windows"))
                        inputFa=appset.outFolderPath+"\\view2tmpF"+threadID+".out";
                else inputFa=appset.outFolderPath+"/view2tmpF"+threadID+".out";
            }
            else{
                 if(appset.system.equals("windows"))
                     inputFa=appset.outFolderPath+"\\view1tmpF"+threadID+".out";
                 else  inputFa=appset.outFolderPath+"/view1tmpF"+threadID+".out";
            }
        }
       
       //load rules into rule reader
        
        if(appset.numSupplementTrees>0){
                rrFA.extractRules(inputFa,fid,datJ,appset);
                rrFA.setSize();
            }
        
          if(appset.numSupplementTrees>0){
              if(sView == 0)
                rr.addnewRulesCF(rrFA, appset.numnewRAttr); 
              else rr1.addnewRulesCF(rrFA, appset.numnewRAttr); 
          }
        
       /*FileDeleter delTmp=new FileDeleter();
           if(appset.system.equals("windows"))
                delTmp.setPath(appset.outFolderPath+"\\JinputInitial.arff");
           else
               delTmp.setPath(appset.outFolderPath+"/JinputInitial.arff");
           delTmp.delete();
           */
       
           System.out.println("Rule set sizes: "+rr1.rules.size()+" "+rr.rules.size());      
          double minJG  = appset.minJS;
          double minAJG = appset.minAddRedJS;
          appset.minJS = 0.05;
          appset.minAddRedJS = 0.04;
          
          int sViewTmp = sView;
          
          if(sView == 0){
              sViewTmp = 1;
          }
           
            if(appset.useJoin){ //change to multi-view
            //rttmp.createGuidedJoinExt(rr1, rr, jsN, appset, 0, 0, 0, oom,fid,datJ);//two-view version
            System.out.println("Number of rules before join: "+rr1.rules.size()+" "+rr.rules.size());
            rttmp.createGuidedJoinExt(rr1, rr, jsN, appset, 0, 0, 0, oom,fid,datJ,0,sViewTmp,appset.maxRSSize);//multi-view version -> OK!
            rr.removeElements(rr.newRuleIndex);
            rr1.removeElements(rr1.newRuleIndex);
        }
        else if(!appset.useJoin){
          //  rttmp.createGuidedNoJoinExt(rr1, rr, jsN, appset, 0, 0,0,oom,fid,datJ);
            rttmp.createGuidedNoJoinExt(rr1, rr, jsN, appset, 0, 0, 0, oom,fid,datJ,0,sViewTmp);//multi-view version
            rr.removeElements(rr.newRuleIndex);
            rr1.removeElements(rr1.newRuleIndex);
        }
            //should be replaced with function that only creates rule-string
            rttmp.adaptSet(datJ, fid, 1);
             rttmp.removePVal(appset);
             
              //napraviti kombinacije binova???
              //rafinirati redeskripcije koristeći binove
              //pravilo mora sadrzavati samo taj atribut da bi rafiniranje krenulo
             if(clustMode == 0)
                rttmp.refineRedsBins(bins, jsN, appset, fid, datJ, t, at, min, sView);
             rttmp.adaptSet(datJ, fid, 1);//ispis, samo za debug
             /*System.out.println("Supports: ");
             for(int zz=0;zz<rttmp.redescriptions.size();zz++){
                 System.out.println(rttmp.redescriptions.get(zz).viewElementsLists.size());
                 for(int zz1=0;zz1<rttmp.redescriptions.get(zz).supportsSides.size();zz1++)
                     System.out.print(rttmp.redescriptions.get(zz).supportsSides.get(zz1)+" ");
                 System.out.print(" "+rttmp.redescriptions.get(zz).elements.size());
                 System.out.println();
             }*/
             
             double ResultsScore = 0.0; 
             double coverage[] = new double[2];
             
             appset.minJS = minJG;
             appset.minAddRedJS = minAJG;
            
             
          RuleReader rr3 = null;   
          System.out.println("W2 indexes: ");
          System.out.println(datJ.W2indexs);
          System.out.println("Num reds: "+rttmp.redescriptions.size());
          //System.exit(-1);
          usedViews.clear();
          if(sView>0){
              usedViews.add(0);
              usedViews.add(sView);
          }
          else{
              usedViews.add(0);
              usedViews.add(1);
          }

         for(int nws=0;nws<datJ.W2indexs.size()+1;nws++){
             if(usedViews.contains(nws))
                 continue;
            rr3 = new RuleReader();
            int oldIndW=rr3.newRuleIndex, endIndW=0;
            
            SettingsReader setMW=new SettingsReader();
           if(appset.system.equals("windows")){ 
            setMW.setPath(appset.outFolderPath+"\\view3tmp"+threadID+".s");
            setMW.setStaticFilePath=appset.outFolderPath+"\\view3tmp"+threadID+".s";
            setMW.setDataFilePath(appset.outFolderPath+"\\Jinputnew"+threadID+".arff");
           }
           else{
              setMW.setPath(appset.outFolderPath+"/view3tmp"+threadID+".s");
              setMW.setStaticFilePath=appset.outFolderPath+"/view3tmp"+threadID+".s";
              setMW.setDataFilePath(appset.outFolderPath+"/Jinputnew"+threadID+".arff"); 
           }
           
           System.out.println("Data indexes: ");
           for(int ii=0;ii<datJ.W2indexs.size();ii++)
               System.out.print(datJ.W2indexs.get(ii)+" ");
           System.out.println();
           
           System.out.println("Granice: ");
           for(int ii=1;ii<datJ.W2indexs.size()+1;ii++)
               if((ii-1)<(datJ.W2indexs.size()-2+1))
                   System.out.println((datJ.W2indexs.get(ii-1)+1)+" - "+(datJ.W2indexs.get(ii)));
               else System.out.println((datJ.W2indexs.get(ii-1)+1)+" - "+datJ.schema.getNbAttributes());
           
            if((nws-1)<(datJ.W2indexs.size()-2+1))
                setMW.createInitialSettingsGen(nws, datJ.W2indexs.get(nws-1)+1 ,datJ.W2indexs.get(nws),datJ.schema.getNbAttributes() , appset,0);//Potencijalno dodati datJ.W2indexs.get(nws)+1
            else
                setMW.createInitialSettingsGen(nws, datJ.W2indexs.get(nws-1)+1 ,datJ.schema.getNbAttributes()+1,datJ.schema.getNbAttributes() , appset,0);
                 
            
           int   numBins=0;
        int Size=rttmp.redescriptions.size();
        
        if(Size%appset.numTargets==0)
            numBins=Size/appset.numTargets;
        else numBins=Size/appset.numTargets+1;
        
         NHMCDistanceMatrix nclMat=null;
        if((appset.distanceFilePaths.size()>0 || appset.useNC.get(0)==true) && appset.networkInit==false)
            nclMat=new NHMCDistanceMatrix(datJ.numExamples,appset);
        NHMCDistanceMatrix nclMat1=null;
        if((appset.distanceFilePaths.size()>1 || appset.useNC.get(1)==true) && appset.networkInit==false)
            nclMat1=new NHMCDistanceMatrix(datJ.numExamples,appset);
            int oldRIndex[]={0};
        for(int z=0;z<numBins;z++){
       
            if(z==0){//should create network from redescriptions!
               if(appset.useNC.size()>nws && appset.networkInit==false){ 
                if(appset.useNC.get(nws)==true && rr3.rules.size()>0){
                    nclMat.reset(appset);
                    if(appset.distanceFilePaths.size()>=nws && appset.networkInit==false && appset.useNetworkAsBackground==false){
                             nclMat.loadDistance(new File(appset.distanceFilePaths.get(nws)), fid);
                              if(appset.system.equals("windows")){ 
                                     nclMat.writeToFile(new File(appset.outFolderPath+"\\distance.csv"), fid,appset);
                              }
                              else{
                                  nclMat.writeToFile(new File(appset.outFolderPath+"/distance.csv"), fid,appset);
                              }
                    }
                     else if(appset.computeDMfromRules==true){
                             nclMat.computeDistanceMatrix(rttmp.redescriptions, fid, appset.maxDistance, datJ.numExamples,oldRIndex);
                             if(appset.system.equals("windows")){ 
                                    nclMat.resetFile(new File(appset.outFolderPath+"\\distances.csv"));
                                    nclMat.writeToFile(new File(appset.outFolderPath+"\\distances.csv"), fid,appset);
                             }
                             else{
                                 nclMat.resetFile(new File(appset.outFolderPath+"/distances.csv"));
                                 nclMat.writeToFile(new File(appset.outFolderPath+"/distances.csv"), fid,appset);
                             }
                     }
                   }
               }
                endIndW=rr3.rules.size();
            }
            
            int nARules=0, nARules1=0;
            double startPerc=0;//percentage[z];
            double endPerc=0;//percentage[z+1];
            int minCovElements[]=new int[]{0};
            int maxCovElements[]=new int[]{0};
            int cuttof=0;
            DataSetCreator dsc=null;
            DataSetCreator dsc1=null;
           /* cuttof=rs.findCutoff(naex, startPerc, endPerc, minCovElements,maxCovElements,oldRIndex, appset.minSupport,appset.maxSupport,appset.numTargets);
             System.out.println("minCovElements: "+minCovElements[0]);
             System.out.println("maxCovElements: "+maxCovElements[0]);
             System.out.println("cuttof: "+cuttof);
             
           if(cuttof==-1)
                continue;*/
            
           if(appset.system.equals("windows")) 
                dsc=new DataSetCreator(appset.outFolderPath+"\\JinputBack"+threadID+".arff");
           else
               dsc=new DataSetCreator(appset.outFolderPath+"/JinputBack"+threadID+".arff");
      
      //  synchronized(lock){    
             try{
        dsc.readDataset();
        }
        catch(IOException e){
            e.printStackTrace();
        }
     //   }
            
            System.out.println("startPerc: "+startPerc);
            System.out.println("endPerc: "+endPerc);
            
            int endTmp=0;
             if((z+1)*appset.numTargets>rttmp.redescriptions.size())
                 endTmp=rttmp.redescriptions.size();
             else endTmp=(z+1)*appset.numTargets;
              //add conditions in the this part of the code...!!!
             int startIndexRR=oldRIndex[0]+z*appset.numTargets;
            
            for(int i=startIndexRR;i<endTmp;i++)//oldRIndex[0];i<rs.redescriptions.size();i++) //do on the fly when reading rules
                   // if(rs.redescriptions.get(i).elements.size()<=naex*endPerc && rs.redescriptions.get(i).elements.size()>=naex*startPerc && rs.redescriptions.get(i).elements.size()>=minCovElements[0] && rs.redescriptions.get(i).elements.size()<=maxCovElements[0]) //do parameters analysis in this step
                        nARules++;
             setMW.ModifySettings(nARules,dsc.schema.getNbAttributes());
             try{
                 if(appset.treeTypes.get(nws)==1/*appset.typeOfRSTrees==1*/){ 
                     if(appset.system.equals("windows")) 
                         dsc.modifyDatasetS(startIndexRR,endTmp, rttmp.redescriptions,appset.outFolderPath+"\\Jinputnew"+threadID+".arff",fid,appset);
                     else 
                         dsc.modifyDatasetS(startIndexRR,endTmp, rttmp.redescriptions,appset.outFolderPath+"/Jinputnew"+threadID+".arff",fid,appset);
                 }
         else if(appset.treeTypes.get(nws)==0/*appset.typeOfRSTrees==0*/){
             if(appset.system.equals("windows")) 
                dsc.modifyDatasetCat(startIndexRR,endTmp, rttmp.redescriptions,appset.outFolderPath+"\\Jinputnew"+threadID+".arff",fid,appset);
             else
                dsc.modifyDatasetCat(startIndexRR,endTmp, rttmp.redescriptions,appset.outFolderPath+"/Jinputnew"+threadID+".arff",fid,appset); 
         }
         //if(appset.treeTypes.get(nws)==1/*appset.typeOfRSTrees==1*/)   
               // dsc.modifyDatasetS(nARules, startPerc, endPerc, oldRIndex[0], rs.redescriptions.size(), minCovElements[0],maxCovElements[0], rs.redescriptions,appset.outFolderPath+"\\Jinputnew.arff",fid);
        // else if(appset.treeTypes.get(nws)==0/*appset.typeOfRSTrees==0*/)
          //      dsc.modifyDatasetCat(nARules, startPerc, endPerc, oldRIndex[0], rs.redescriptions.size(), minCovElements[0],maxCovElements[0], rs.redescriptions,appset.outFolderPath+"\\Jinputnew.arff",fid);
        }
        catch(IOException e){
            e.printStackTrace();
        }
             
             exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath, "view3tmp"+threadID+".s"/*"wbtmp.s"*/, 0,appset.clusteringMemory);//was 1 for rules before
             System.out.println("Process 1 side "+(nws+1)+" finished!");
             //System.exit(-1);
             String input;
             if(appset.system.equals("windows")) 
              input=appset.outFolderPath+"\\view3tmp"+threadID+".out";
             else
                 input=appset.outFolderPath+"/view3tmp"+threadID+".out";
             
              int newRules=0;
              RuleReader ItRules=new RuleReader();
              
             ItRules.extractRules(input,fid,datJ,appset);
             System.out.println("ItRules 3: "+ItRules.rules.size());
        ItRules.setSize();
            if(z==0)
                newRules=rr3.addnewRulesC(ItRules, appset.numnewRAttr,1);
            else
                newRules=rr3.addnewRulesC(ItRules, appset.numnewRAttr,0);
           }
        System.out.println("New rules 3: "+rr3.rules.size());
        
        if(appset.useJoin){//do redescription construction
            System.out.println("Num rules view3: "+rr3.rules.size());
            //rttmp.combineViewRulesJoin(rr3, jsN, appset, oldIndW, 0, oom, fid, datJ, oldRIndex ,nws); //two-view version
            rttmp.combineViewRulesJoin(rr3, jsN, appset, oldIndW, 0, oom, fid, datJ, oldRIndex ,nws, appset.maxRSSize); //multi-view version
        }
        else{//(rr, rr1, jsN, appset, oldIndexRR, oldIndexRR1, RunInd, oom,fid,datJ);
            rttmp.combineViewRules(rr3, jsN, appset, oldIndW, 0, oom, fid, datJ, oldRIndex ,nws);
        }
           
        //rs.combineViewRules(readers.get(nws-2), jsN, appset, oldIndW, RunInd, oom, fid, datJ, oldRIndex ,nws);
        }
         
             for(int i=rttmp.redescriptions.size()-1;i>=0;i--){
                 rttmp.redescriptions.get(i).closeInterval(datJ, fid);
                 rttmp.redescriptions.get(i).createRuleString(fid);
                 //System.out.println("nw: "+rttmp.redescriptions.get(i).numViews());
                 //System.out.println(rttmp.redescriptions.get(i));
                 System.out.println();
                 if(rttmp.redescriptions.get(i).numViews()!=(datJ.W2indexs.size()+1))
                     rttmp.redescriptions.remove(i);
             }
             
              
             for(int k = rttmp.redescriptions.size()-1;k>=0;k--){
                 if(rttmp.redescriptions.get(k).JS<appset.minJS)
                     rttmp.redescriptions.remove(k);
             }
             
             Jacard js1 = new Jacard();
             
             for(int k=rttmp.redescriptions.size()-1;k>=0;k--)
                 for(int k1 = k-1;k1>=0;k1--){
                     if((js1.computeAttributeJacard(rttmp.redescriptions.get(k), rttmp.redescriptions.get(k1), datJ) == 1) && js1.computeRedescriptionElementJacard(rttmp.redescriptions.get(k), rttmp.redescriptions.get(k1)) == 1){
                         rttmp.redescriptions.remove(k);
                         break;
                     }
                 } 

              if(appset.system.equals("windows"))
                    rttmp.writeToFile(appset.outFolderPath+"\\"+appset.outputName+"DLInterpretabilityMW "+t.getName()+" "+appset.minJS+" JSType "+0+"minSupp "+appset.minSupport+".rr", datJ, fid, startTime,rttmp.redescriptions.size(),appset, ResultsScore, coverage,oom);
              else
                    rttmp.writeToFile(appset.outFolderPath+"/"+appset.outputName+"DLInterpretabilityMW "+t.getName()+" "+appset.minJS+" JSType "+0+"minSupp "+appset.minSupport+".rr", datJ, fid, startTime,rttmp.redescriptions.size(),appset, ResultsScore, coverage,oom);
          // System.exit(-1);
             // rtmp.redescriptions.addAll(rttmp.redescriptions);
            //add code for multi-view  
              
           /*if(test == 1)
            continue;*/
           
        //kod multi-view dodaj pravila drugih pogleda
          
     //combine rules into reds      
 
     //add constraint to the importantAttribute set
       for(int i=0;i<=datJInit.W2indexs.size();i++){
           appset.importantAttributes.add(new ArrayList<ArrayList<String>>());
           appset.importantAttributes.get(i).add(new ArrayList<String>());
           if(i!=sView)
               appset.importantAttributes.get(i).get(0).add("");
       }
       
      // appset.importantAttributes.set(sView,new ArrayList<ArrayList<String>>());
       //appset.importantAttributes.get(sView).add(new ArrayList<String>());
       appset.importantAttributes.get(sView).get(0).add(t.getName().trim());
       
       //set constraint type to hard - for the view containing the attribute
       appset.attributeImportanceGen = new ArrayList<>();
       for(int i=0;i<=datJInit.W2indexs.size();i++)
           appset.attributeImportanceGen.add(1);
       appset.attributeImportanceGen.set(sView, 2);
     
     
           datJInit=null;   //NE RACUNA MWAll za PRVI POGLED, NEGDJE JOS BUG U KODU!     
        
        int leftSide=1, rightSide=0;//set left to 1 when computing lf, otherwise right
         int leftSide1=1, rightSide1=0; //left, right side for Side 2
        if(sView == 0){
            leftSide=0; rightSide=1;
            leftSide1=1; rightSide1=0;
        }
        else{
            leftSide=1; rightSide=0;
            leftSide1=0; rightSide1=1;
        }
        System.out.println("sView = "+sView+" leftSide = "+leftSide);
       
        int it=0;
        Jacard js=new Jacard();
      //  Jacard jsN[]=new Jacard[3];
        
        for(int i=0;i<jsN.length;i++)
            jsN[i]=new Jacard();
       
        int newRedescriptions=1;
        int numIter=0;
        int RunInd=0;
       
        int naex=datJ.numExamples;
        
        //add arrayList of view rules
        ArrayList<RuleReader> readers=new ArrayList<>();
        int oldRIndex[]={0};
        
        NHMCDistanceMatrix nclMat=null;
        if((appset.distanceFilePaths.size()>0 || appset.useNC.get(0)==true) && appset.networkInit==false)
            nclMat=new NHMCDistanceMatrix(datJ.numExamples,appset);
        NHMCDistanceMatrix nclMat1=null;
        if((appset.distanceFilePaths.size()>1 || appset.useNC.get(1)==true) && appset.networkInit==false)
            nclMat1=new NHMCDistanceMatrix(datJ.numExamples,appset);
       
        if(appset.useNetworkAsBackground==true)
            appset.networkInit=false;
        
        if(appset.useNC.size()>=2 && appset.useNC.get(1) == true){
            if(appset.system.equals("windows")) 
                initSettings.setPath(appset.outFolderPath+"\\view2"+threadID+".s");
            else
                initSettings.setPath(appset.outFolderPath+"/view2"+threadID+".s");
         if(appset.useNC.size()>2)
            initSettings.createInitialSettingsGenN(1, datJ.W2indexs.get(0)+1, datJ.W2indexs.get(1), datJ.schema.getNbAttributes(), appset);
         else
            initSettings.createInitialSettingsGenN(1, datJ.W2indexs.get(0)+1, datJ.schema.getNbAttributes()+1, datJ.schema.getNbAttributes(), appset); 
        }
        if(appset.useNC.size()>1 && appset.useNC.get(0) == true){
             if(appset.system.equals("windows")) 
                initSettings.setPath(appset.outFolderPath+"\\view1"+threadID+".s");
             else
                initSettings.setPath(appset.outFolderPath+"/view1"+threadID+".s");
         initSettings.createInitialSettingsGenN(1, 4, datJ.W2indexs.get(0), datJ.schema.getNbAttributes(), appset);
        }
        
        int numSuppTrees = appset.numSupplementTrees;
      
        int oldIndexRR=rr.newRuleIndex;
       int oldIndexRR1=rr1.newRuleIndex;
       int nARules=0, nARules1=0;
       int endIndexRR=0, endIndexRR1=0;
         int numBins=0, Size = 0;
          DataSetCreator dsc=null;
       DataSetCreator dsc1=null;
        while(newRedescriptions!=0 && RunInd<appset.numIterations){//upaliti i drugu stranu!
           appset.numSupplementTrees = 2; 
      
       
       rr.setSize();
       rr1.setSize();
       
        nARules=0; nARules1=0;
        oldIndexRR=rr.newRuleIndex;
        oldIndexRR1=rr1.newRuleIndex;
       System.out.println("OOIndRR: "+oldIndexRR);
       System.out.println("OOIndRR1: "+oldIndexRR1);
       endIndexRR=0; endIndexRR1=0;
             newRedescriptions=0;
            System.out.println("Iteration: "+(++numIter));

             //do rule creation with various generality levels
        double percentage[]=new double[]{0,0.2,0.4,0.6,0.8,1.0};

         numBins=0;
        Size=Math.max(rr.rules.size()-oldIndexRR, rr1.rules.size()-oldIndexRR1);
        if(Size%appset.numTargets==0)
            numBins=Size/appset.numTargets;
        else numBins=Size/appset.numTargets+1;
        
        for(int z=0;z<numBins;z++){

            nARules=0; nARules1=0;
            double startPerc=0;
            double endPerc=1;
            int minCovElements[]=new int[]{0}, minCovElements1[]=new int[]{0};
            int maxCovElements[]=new int[]{0}, maxCovElements1[]=new int[]{0};

            System.out.println("startPerc: "+startPerc);
            System.out.println("endPerc: "+endPerc);

            int cuttof=0,cuttof1=0;

            if(z==0){
                endIndexRR=rr.rules.size();
                endIndexRR1=rr1.rules.size();
               
                //compute network things only here
                if(appset.useNC.get(1)==true && appset.networkInit==false && appset.useNetworkAsBackground==false){
                    nclMat.reset(appset);
                    nclMat1.reset(appset);
                if(leftSide==1){
                    if(appset.distanceFilePaths.size()>=1){
                             nclMat.loadDistance(new File(appset.distanceFilePaths.get(1)), fid);
                    }
                     else if(appset.computeDMfromRules==true){
                             nclMat.computeDistanceMatrix(rr1, fid, appset.maxDistance, datJ.numExamples);
                     }
                   }
                }
                 if(appset.useNC.get(0)==true && appset.networkInit==false && appset.useNetworkAsBackground==false){
                 if(rightSide==1){
                    if(appset.distanceFilePaths.size()>=2){
                             nclMat.loadDistance(new File(appset.distanceFilePaths.get(0)), fid);
                    }
                     else if(appset.computeDMfromRules==true){
                             nclMat.computeDistanceMatrix(rr, fid, appset.maxDistance, datJ.numExamples);
                     }
                   }
                 }
                  if(appset.useNC.get(1)==true && appset.networkInit==false && appset.useNetworkAsBackground==false){
                if(leftSide1==1){
                    if(appset.distanceFilePaths.size()>=1){
                             nclMat1.loadDistance(new File(appset.distanceFilePaths.get(1)), fid);
                    }
                     else if(appset.computeDMfromRules==true){
                             nclMat1.computeDistanceMatrix(rr1, fid, appset.maxDistance, datJ.numExamples);
                     }
                }
                  }
                   if(appset.useNC.get(0)==true && appset.networkInit==false && appset.useNetworkAsBackground==false){
                    if(rightSide1==1){
                    if(appset.distanceFilePaths.size()>=2){
                             nclMat1.loadDistance(new File(appset.distanceFilePaths.get(0)), fid);
                    }
                     else if(appset.computeDMfromRules==true){
                             nclMat1.computeDistanceMatrix(rr, fid, appset.maxDistance, datJ.numExamples);
                     }
                  }
                }
            }
  
         if(!appset.useSplitTesting==true){    
          if(appset.system.equals("windows")){         
                dsc=new DataSetCreator(appset.outFolderPath+"\\JinputBack"+threadID+".arff");
                dsc1=new DataSetCreator(appset.outFolderPath+"\\JinputBack"+threadID+".arff");
          }
          else{
                dsc=new DataSetCreator(appset.outFolderPath+"/JinputBack"+threadID+".arff");
                dsc1=new DataSetCreator(appset.outFolderPath+"/JinputBack"+threadID+".arff");
          }
         }
         else{
              if(appset.trainFileName.equals("") || appset.testFileName.equals("")){
                     if(appset.system.equals("windows")){    
                             dsc=new DataSetCreator(appset.outFolderPath+"\\JinputTrain.arff");
                             dsc1=new DataSetCreator(appset.outFolderPath+"\\JinputTrain.arff");
                         }
                     else{
                             dsc=new DataSetCreator(appset.outFolderPath+"/JinputTrain.arff");
                             dsc1=new DataSetCreator(appset.outFolderPath+"/JinputTrain.arff"); 
                }
             }
              else{
                  if(appset.system.equals("windows")){    
                             dsc=new DataSetCreator(appset.outFolderPath+"\\"+appset.trainFileName);
                             dsc1=new DataSetCreator(appset.outFolderPath+"\\"+appset.trainFileName);
                         }
                     else{
                             dsc=new DataSetCreator(appset.outFolderPath+"/"+appset.trainFileName);
                             dsc1=new DataSetCreator(appset.outFolderPath+"/"+appset.trainFileName); 
                }
              }
         }
    
     //     synchronized(lock){
            try{
        dsc.readDataset();
        }
        catch(IOException e){
            e.printStackTrace();
        }
      //    }
            naex=dsc.data.getNbRows();
        //read dataset for cicle 2
       //  synchronized(lock){
        try{
        dsc1.readDataset();
        }
        catch(IOException e){
            e.printStackTrace();
        }
   //      }
            
            //create and modify settings for cicle 1
          if(leftSide==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets){
              //createSettings
            if(appset.system.equals("windows")){    
             set=new SettingsReader(appset.outFolderPath+"\\view2tmp"+threadID+".s",appset.outFolderPath+"\\view2"+threadID+".s");
             set.setDataFilePath(appset.outFolderPath+"\\Jinputnew"+threadID+".arff");
            }
            else{
                 set=new SettingsReader(appset.outFolderPath+"/view2tmp"+threadID+".s",appset.outFolderPath+"/view2"+threadID+".s");
                 set.setDataFilePath(appset.outFolderPath+"/Jinputnew"+threadID+".arff"); 
            }
             if(appset.numSupplementTrees>0){
                 if(appset.system.equals("windows")){
                     setF=new SettingsReader(appset.outFolderPath+"\\view2tmpF"+threadID+".s",appset.outFolderPath+"\\view2"+threadID+".s");
                     setF.setDataFilePath(appset.outFolderPath+"\\Jinputnew"+threadID+".arff");
                 }
                 else{
                     setF=new SettingsReader(appset.outFolderPath+"/view2tmpF"+threadID+".s",appset.outFolderPath+"/view2"+threadID+".s");
                     setF.setDataFilePath(appset.outFolderPath+"/Jinputnew"+threadID+".arff");
                 }
             }
             /*if(z!=0)
                 set.changeSeed();*/
             
             int endTmp=0;
             if((z+1)*appset.numTargets>(endIndexRR1-oldIndexRR1))
                 endTmp=endIndexRR1;
             else endTmp=(z+1)*appset.numTargets+oldIndexRR1;
             int startIndexRR1=oldIndexRR1+z*appset.numTargets;
             
             for(int i=startIndexRR1;i<endTmp;i++) //do on the fly when reading rules
                    if( rr1.rules.get(i).elements.size()>=appset.minSupport) //do parameters analysis in this step
                        nARules++;
             set.ModifySettings(nARules,dsc.schema.getNbAttributes()); //Setting ne postoji????
             if(appset.numSupplementTrees>0)
                setF.ModifySettingsF(nARules,dsc.schema.getNbAttributes(),appset);    //Setting ne postoji????
          }
          else if(rightSide==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){
              if(appset.system.equals("windows")){
                 set=new SettingsReader(appset.outFolderPath+"\\view1tmp"+threadID+".s",appset.outFolderPath+"\\view1"+threadID+".s");
                 set.setDataFilePath(appset.outFolderPath+"\\Jinputnew"+threadID+".arff");
                    }
              else{
                 set=new SettingsReader(appset.outFolderPath+"/view1tmp"+threadID+".s",appset.outFolderPath+"/view1"+threadID+".s");
                 set.setDataFilePath(appset.outFolderPath+"/Jinputnew"+threadID+".arff");  
              }
                 if(appset.numSupplementTrees>0){
                     if(appset.system.equals("windows")){
                        setF=new SettingsReader(appset.outFolderPath+"\\view1tmpF"+threadID+".s",appset.outFolderPath+"\\view1"+threadID+".s");
                        setF.setDataFilePath(appset.outFolderPath+"\\Jinputnew"+threadID+".arff");
                     }
                     else{
                         setF=new SettingsReader(appset.outFolderPath+"/view1tmpF"+threadID+".s",appset.outFolderPath+"/view1"+threadID+".s");
                         setF.setDataFilePath(appset.outFolderPath+"/Jinputnew"+threadID+".arff");
                     }
                 }

                  int endTmp=0;
             if((z+1)*appset.numTargets>(endIndexRR-oldIndexRR))
                 endTmp=endIndexRR;
             else endTmp=(z+1)*appset.numTargets+oldIndexRR;
                 
             int startIndexRR=oldIndexRR+z*appset.numTargets;
             
                 for(int i=startIndexRR;i<endTmp;i++) //do on the fly when reading rules
                        if(rr.rules.get(i).elements.size()>=appset.minSupport)
                            nARules++;
                set.ModifySettings(nARules,dsc1.schema.getNbAttributes());
                if(appset.numSupplementTrees>0)
                    setF.ModifySettingsF(nARules,dsc1.schema.getNbAttributes(),appset);
          }

            //create and modify settings for cicle 2
        if(leftSide1==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets){
            if(appset.system.equals("windows")){
                set1=new SettingsReader(appset.outFolderPath+"\\view2tmpC"+threadID+".s",appset.outFolderPath+"\\view2"+threadID+".s");
                set1.setDataFilePath(appset.outFolderPath+"\\JinputnewC"+threadID+".arff");
            }
            else{
                set1=new SettingsReader(appset.outFolderPath+"/view2tmpC"+threadID+".s",appset.outFolderPath+"/view2"+threadID+".s");
                set1.setDataFilePath(appset.outFolderPath+"/JinputnewC"+threadID+".arff");
            }
            if(appset.numSupplementTrees>0){
                if(appset.system.equals("windows")){
                     setF1=new SettingsReader(appset.outFolderPath+"\\view2tmpFC"+threadID+".s",appset.outFolderPath+"\\view2"+threadID+".s");
                     setF1.setDataFilePath(appset.outFolderPath+"\\JinputnewC"+threadID+".arff");
                }
                else{
                     setF1=new SettingsReader(appset.outFolderPath+"/view2tmpFC"+threadID+".s",appset.outFolderPath+"/view2"+threadID+".s");
                     setF1.setDataFilePath(appset.outFolderPath+"/JinputnewC"+threadID+".arff");
                }
            }
            
             int endTmp=0;
             if((z+1)*appset.numTargets>(endIndexRR1-oldIndexRR1))
                 endTmp=endIndexRR1;
             else endTmp=oldIndexRR1+(z+1)*appset.numTargets;
             
             int startIndexRR1=oldIndexRR1+z*appset.numTargets;
             
             for(int i=startIndexRR1;i<endTmp;i++)
                  if(rr1.rules.get(i).elements.size()>=appset.minSupport)
                       nARules1++;
             set1.ModifySettings(nARules1,dsc.schema.getNbAttributes());
             if(appset.numSupplementTrees>0)
                setF1.ModifySettingsF(nARules1,dsc.schema.getNbAttributes(),appset);
          }
          else if(rightSide1==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){

            if(appset.system.equals("windows")){  
              set1=new SettingsReader(appset.outFolderPath+"\\view1tmpC"+threadID+".s",appset.outFolderPath+"\\view1"+threadID+".s");
              set1.setDataFilePath(appset.outFolderPath+"\\JinputnewC"+threadID+".arff");
            }
            else{
               set1=new SettingsReader(appset.outFolderPath+"/view1tmpC"+threadID+".s",appset.outFolderPath+"/view1"+threadID+".s");
               set1.setDataFilePath(appset.outFolderPath+"/JinputnewC"+threadID+".arff"); 
            }
              if(appset.numSupplementTrees>0){
                  if(appset.system.equals("windows")){ 
                        setF1=new SettingsReader(appset.outFolderPath+"\\view1tmpFC"+threadID+".s",appset.outFolderPath+"\\view1"+threadID+".s");
                        setF1.setDataFilePath(appset.outFolderPath+"\\JinputnewC"+threadID+".arff");
                  }
                  else{
                        setF1=new SettingsReader(appset.outFolderPath+"/view1tmpFC"+threadID+".s",appset.outFolderPath+"/view1"+threadID+".s");
                        setF1.setDataFilePath(appset.outFolderPath+"/JinputnewC"+threadID+".arff"); 
                  }
              }

              int endTmp=0;
             if((z+1)*appset.numTargets>(endIndexRR-oldIndexRR))
                 endTmp=endIndexRR;
             else endTmp=(z+1)*appset.numTargets+oldIndexRR;
              
             int startIndexRR=oldIndexRR+z*appset.numTargets;
             
              for(int i=startIndexRR;i<endTmp;i++)
                  if(rr.rules.get(i).elements.size()>=appset.minSupport)
                        nARules1++;
              set1.ModifySettings(nARules1,dsc1.schema.getNbAttributes());
              if(appset.numSupplementTrees>0)
                    setF1.ModifySettingsF(nARules1,dsc1.schema.getNbAttributes(),appset);
          }

        RuleReader ItRules=new RuleReader();
        RuleReader ItRules1=new RuleReader();
        RuleReader ItRulesF=new RuleReader();
        RuleReader ItRulesF1=new RuleReader();

       //modify dataset for cicle 1
       if(leftSide==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets ){
           int startIndexRR1=oldIndexRR1+z*appset.numTargets;
           int endTmp=0;
             if((z+1)*appset.numTargets>(endIndexRR1-oldIndexRR1))
                 endTmp=endIndexRR1;
             else endTmp=(z+1)*appset.numTargets+oldIndexRR1;
        try{
         if(appset.treeTypes.get(1)==1/*appset.typeOfRSTrees==1*/) 
             if(appset.system.equals("windows")){ 
                 dsc.modifyDatasetS(startIndexRR1,endTmp,rr1,appset.outFolderPath+"\\Jinputnew"+threadID+".arff",fid,appset);
             }
             else{
                dsc.modifyDatasetS(startIndexRR1,endTmp,rr1,appset.outFolderPath+"/Jinputnew"+threadID+".arff",fid,appset); 
             }
               
         else if(appset.treeTypes.get(1)==0)
             if(appset.system.equals("windows")){ 
                 dsc.modifyDatasetCat(startIndexRR1,endTmp,rr1,appset.outFolderPath+"\\Jinputnew"+threadID+".arff",fid,appset);
             }
             else{
                dsc.modifyDatasetCat(startIndexRR1,endTmp,rr1,appset.outFolderPath+"/Jinputnew"+threadID+".arff",fid,appset); 
             }
        }
        catch(IOException e){
            e.printStackTrace();
        }
       }
       else if(rightSide==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){
           int endTmp=0;
             if((z+1)*appset.numTargets>(endIndexRR-oldIndexRR))
                 endTmp=endIndexRR;
             else endTmp=(z+1)*appset.numTargets+oldIndexRR;
              
             int startIndexRR=oldIndexRR+z*appset.numTargets;
             
         try{
             if(appset.treeTypes.get(0)==1)
                 if(appset.system.equals("windows")){ 
                    dsc.modifyDatasetS(startIndexRR,endTmp,rr,appset.outFolderPath+"\\Jinputnew"+threadID+".arff",fid,appset);
                 }
                 else{
                     dsc.modifyDatasetS(startIndexRR,endTmp,rr,appset.outFolderPath+"/Jinputnew"+threadID+".arff",fid,appset); 
                 }
             else if(appset.treeTypes.get(0)==0/*appset.typeOfLSTrees==0*/)
                  if(appset.system.equals("windows")){ 
                    dsc.modifyDatasetCat(startIndexRR,endTmp ,rr,appset.outFolderPath+"\\Jinputnew"+threadID+".arff",fid,appset);
                  }
                  else{
                     dsc.modifyDatasetCat(startIndexRR,endTmp ,rr,appset.outFolderPath+"/Jinputnew"+threadID+".arff",fid,appset); 
                  }
        }
        catch(IOException e){
            e.printStackTrace();
        }  
       }

       //modify dataset for cicle 2
       if(leftSide1==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets){
           int startIndexRR1=oldIndexRR1+z*appset.numTargets;
           int endTmp=0;
             if((z+1)*appset.numTargets>(endIndexRR1-oldIndexRR1))
                 endTmp=endIndexRR1;
             else endTmp=(z+1)*appset.numTargets+oldIndexRR1;
             
        try{
            if(appset.treeTypes.get(1)==1)
                if(appset.system.equals("windows")){ 
                    dsc1.modifyDatasetS(startIndexRR1,endTmp, rr1,appset.outFolderPath+"\\JinputnewC"+threadID+".arff",fid,appset);
                }
                else{
                   dsc1.modifyDatasetS(startIndexRR1,endTmp, rr1,appset.outFolderPath+"/JinputnewC"+threadID+".arff",fid,appset); 
                }
            else if(appset.treeTypes.get(1)==0)
                if(appset.system.equals("windows"))
                     dsc1.modifyDatasetCat(startIndexRR1,endTmp, rr1,appset.outFolderPath+"\\JinputnewC"+threadID+".arff",fid,appset);
                else
                     dsc1.modifyDatasetCat(startIndexRR1,endTmp, rr1,appset.outFolderPath+"/JinputnewC"+threadID+".arff",fid,appset);
        }
        catch(IOException e){
            e.printStackTrace();
        }
       }
       else if(rightSide1==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){
           int endTmp=0;
             if((z+1)*appset.numTargets>(endIndexRR-oldIndexRR))
                 endTmp=endIndexRR;
             else endTmp=(z+1)*appset.numTargets+oldIndexRR;
              
             int startIndexRR=oldIndexRR+z*appset.numTargets;
             
         try{
             if(appset.treeTypes.get(0)==1)
                 if(appset.system.equals("windows"))
                    dsc1.modifyDatasetS(startIndexRR,endTmp,rr,appset.outFolderPath+"\\JinputnewC"+threadID+".arff",fid,appset);
                 else
                    dsc1.modifyDatasetS(startIndexRR,endTmp,rr,appset.outFolderPath+"/JinputnewC"+threadID+".arff",fid,appset); 
             else if(appset.treeTypes.get(0)==0)
                 if(appset.system.equals("windows"))
                 dsc1.modifyDatasetCat(startIndexRR,endTmp,rr,appset.outFolderPath+"\\JinputnewC"+threadID+".arff",fid,appset);
             else
                   dsc1.modifyDatasetCat(startIndexRR,endTmp,rr,appset.outFolderPath+"/JinputnewC"+threadID+".arff",fid,appset);   
        }
        catch(IOException e){
            e.printStackTrace();
        }
       }
       
       dsc=null;
       dsc1=null;
      
       if((appset.useNC.get(0)==true && rightSide==1 && appset.networkInit==false && appset.useNetworkAsBackground==false) || (appset.useNC.get(1)==true && leftSide==1 && appset.networkInit==false && appset.useNetworkAsBackground==false)){ 
           if(appset.system.equals("windows")){  
             nclMat.resetFile(new File(appset.outFolderPath+"\\distances.csv"));
             nclMat.writeToFile(new File(appset.outFolderPath+"\\distances.csv"), fid,appset);
            }
           else{
              nclMat.resetFile(new File(appset.outFolderPath+"/distances.csv"));
             nclMat.writeToFile(new File(appset.outFolderPath+"/distances.csv"), fid,appset); 
           }
       }
       
        //run the second proces on new data
        // iterate until convergence (no new rules, or very small amount obtained)
         if(leftSide==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets){
             exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath, "view2tmp"+threadID+".s", 0,appset.clusteringMemory);//was 1 for rules before
             if(appset.numSupplementTrees>0)
                exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath, "view2tmpF"+threadID+".s", 0,appset.clusteringMemory);//was 1 for rules before
             System.out.println("Process 2 side 1 finished!");
         }
         else if(rightSide==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){
             exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath, "view1tmp"+threadID+".s", 0,appset.clusteringMemory);
             if(appset.numSupplementTrees>0)
                 exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath, "view1tmpF"+threadID+".s", 0,appset.clusteringMemory);
             System.out.println("Process 1 side 1 finished!");
         }

       /* if((appset.useNC.get(0)==true && rightSide1==1 && appset.networkInit==false && appset.useNetworkAsBackground==false) || (appset.useNC.get(1)==true && leftSide1==1 && appset.networkInit==false && appset.useNetworkAsBackground==false)){ 
              if(appset.system.equals("windows")){ 
                 nclMat1.resetFile(new File(appset.outFolderPath+"\\distances.csv"));
                 nclMat1.writeToFile(new File(appset.outFolderPath+"\\distances.csv"), fid,appset);
              }
              else{
                 nclMat1.resetFile(new File(appset.outFolderPath+"/distances.csv"));
                 nclMat1.writeToFile(new File(appset.outFolderPath+"/distances.csv"), fid,appset);
              }
        }*/
         
         //run the second proces for cicle 2 on new data
        // iterate until convergence (no new rules, or very small amount obtained)
         if(leftSide1==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets){
             exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath,"view2tmpC"+threadID+".s", 0,appset.clusteringMemory);
             if(appset.numSupplementTrees>0)
                exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath,"view2tmpFC"+threadID+".s", 0,appset.clusteringMemory);
             System.out.println("Process 2 side 2 finished!");
         }
         else if(rightSide1==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){
             exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath,"view1tmpC"+threadID+".s", 0,appset.clusteringMemory);
             if(appset.numSupplementTrees>0)
             exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath,"view1tmpFC"+threadID+".s" , 0,appset.clusteringMemory);
             System.out.println("Process 1 side 2 finished!");
         }

       //extract rules for cicle 1
        String input="", inputF="";
        if(leftSide==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets){
            if(appset.system.equals("windows")){ 
                 input=appset.outFolderPath+"\\view2tmp"+threadID+".out";
                 inputF=appset.outFolderPath+"\\view2tmpF"+threadID+".out";
            }
            else{
               input=appset.outFolderPath+"/view2tmp"+threadID+".out";
                 inputF=appset.outFolderPath+"/view2tmpF"+threadID+".out"; 
            }
        }
        else if(rightSide==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){
            if(appset.system.equals("windows")){ 
                 input=appset.outFolderPath+"\\view1tmp"+threadID+".out";
                    if(appset.numSupplementTrees>0) 
                        inputF=appset.outFolderPath+"\\view1tmpF"+threadID+".out";
            }
            else{
                input=appset.outFolderPath+"/view1tmp"+threadID+".out";
                    if(appset.numSupplementTrees>0) 
                        inputF=appset.outFolderPath+"/view1tmpF"+threadID+".out";
            }
        }
   
       int newRules=0;
       if((leftSide==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets) || (rightSide==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets)){
            ItRules.extractRules(input,fid,datJ,appset);
            ItRules.setSize();
            if(appset.numSupplementTrees>0){
                ItRulesF.extractRules(inputF,fid,datJ,appset);
                ItRulesF.setSize();
            }
       }
        if(leftSide==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets){
            if(z==0)
                newRules=rr.addnewRulesC(ItRules, appset.numnewRAttr,1);
            else
                newRules=rr.addnewRulesC(ItRules, appset.numnewRAttr,0);
            if(appset.numSupplementTrees>0)
                rr.addnewRulesCF(ItRulesF, appset.numnewRAttr); 
        }
        else if(rightSide==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){
            if(z==0)
                newRules=rr1.addnewRulesC(ItRules, appset.numnewRAttr,1);
            else
                newRules=rr1.addnewRulesC(ItRules, appset.numnewRAttr, 0);
            if(appset.numSupplementTrees>0)
                rr1.addnewRulesCF(ItRulesF, appset.numnewRAttr); 
        }     
         //extract rules for cicle 2
        input=""; inputF="";
        if(leftSide1==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets){
            if(appset.system.equals("windows")){ 
                    input=appset.outFolderPath+"\\view2tmpC"+threadID+".out";
                    inputF=appset.outFolderPath+"\\view2tmpFC"+threadID+".out";
            }
            else{
                    input=appset.outFolderPath+"/view2tmpC"+threadID+".out";
                    inputF=appset.outFolderPath+"/view2tmpFC"+threadID+".out"; 
            }
        }
        else if(rightSide1==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){
            if(appset.system.equals("windows")){ 
                    input=appset.outFolderPath+"\\view1tmpC"+threadID+".out";
                    inputF=appset.outFolderPath+"\\view1tmpFC"+threadID+".out";
            }
            else{
                input=appset.outFolderPath+"/view1tmpC"+threadID+".out";
                inputF=appset.outFolderPath+"/view1tmpFC"+threadID+".out";
            }
        }

       int newRules1=0;
       if((leftSide1==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets) || (rightSide1==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets)){
        ItRules1.extractRules(input,fid,datJ,appset);
        ItRules1.setSize();
        if(appset.numSupplementTrees>0){
             ItRulesF1.extractRules(inputF,fid,datJ,appset);
             ItRulesF1.setSize();
        }
       }
        if(leftSide1==1 && (endIndexRR1-oldIndexRR1)>z*appset.numTargets){
            if(z==0)
                    newRules1=rr.addnewRulesC(ItRules1, appset.numnewRAttr,1);
            else
                    newRules1=rr.addnewRulesC(ItRules1, appset.numnewRAttr, 0);
            if(appset.numSupplementTrees>0)
                rr.addnewRulesCF(ItRulesF1, appset.numnewRAttr); 
        }
        else if(rightSide1==1 && (endIndexRR-oldIndexRR)>z*appset.numTargets){
            if(z==0)
                    newRules1=rr1.addnewRulesC(ItRules1, appset.numnewRAttr,1);
            else
                    newRules1=rr1.addnewRulesC(ItRules1, appset.numnewRAttr,0);
            if(appset.numSupplementTrees>0)
             rr1.addnewRulesCF(ItRulesF1, appset.numnewRAttr);
        }

        System.out.println("New rules cicle 1: "+newRules);
        System.out.println("New rules cicle 2: "+newRules1);
       }
        
        
              if(appset.optimizationType==0){ 
        if(appset.useJoin){
            //add computation of rule support if bagging
            newRedescriptions=rtmp.createGuidedJoinBasic(rr1, rr, jsN, appset, oldIndexRR1, oldIndexRR, RunInd,oom,fid,datJ, elemFreq, attrFreq, redScores,redScoresAtt,redDistCoverage,redDistCoverageAt, redDistNetwork, targetAtScore, Statistics, maxDiffScoreDistribution,nclMatInit,0);
            rr.removeElements(rr.newRuleIndex);
            rr1.removeElements(rr1.newRuleIndex);
            if(appset.numSupplementTrees>0){
                 rr.removeRulesCF();
                 rr1.removeRulesCF();
            }
        }
        else if(!appset.useJoin){
            newRedescriptions=rtmp.createGuidedNoJoinBasic(rr1, rr, jsN, appset, oldIndexRR1, oldIndexRR, RunInd,oom,fid,datJ, elemFreq, attrFreq, redScores,redScoresAtt,redDistCoverage,redDistCoverageAt, redDistNetwork, targetAtScore, Statistics, maxDiffScoreDistribution,nclMatInit,0);
            rr.removeElements(rr.newRuleIndex);
            rr1.removeElements(rr1.newRuleIndex);
            if(appset.numSupplementTrees>0){
                rr.removeRulesCF();
                rr1.removeRulesCF();
            }
        }
       }
       else{
           if(appset.useJoin){ //exchange with multi-view version
           // newRedescriptions=rtmp.createGuidedJoinExt(rr1, rr, jsN, appset, oldIndexRR1, oldIndexRR, RunInd, oom,fid,datJ);//two-view version
            //newRedescriptions=rtmp.createGuidedJoinExt(rr1, rr, jsN, appset, oldIndexRR1, oldIndexRR, RunInd, oom,fid,datJ,0,sViewTmp,appset.maxRSSize);
            newRedescriptions=rtmp.createGuidedJoinConstrainedExt(rr1, rr, jsN, appset, oldIndexRR1, oldIndexRR, RunInd, oom,fid,datJ,0,sViewTmp,appset.maxRSSize);//multi-view version constrained
            rr.removeElements(rr.newRuleIndex);
            rr1.removeElements(rr1.newRuleIndex);
        }
        else if(!appset.useJoin){
            newRedescriptions=rtmp.createGuidedNoJoinExt(rr1, rr, jsN, appset, oldIndexRR1, oldIndexRR, RunInd,oom,fid,datJ);
            rr.removeElements(rr.newRuleIndex);
            rr1.removeElements(rr1.newRuleIndex);
        }
       }
       
         it++;
        
        System.out.println("New redescriptions: "+newRedescriptions);
         
        //if more than two viewes get guided search in here for further views...
        //should be modified for redescription addition
        //new join procedures should be created 
        System.out.println("Number of viewes: "+datJ.W2indexs.size());
        
         if(readers.size()<(datJ.W2indexs.size())+1)
             for(int z=0;z<(datJ.W2indexs.size())+1;z++)
            readers.add(new RuleReader());
        
        for(int nws=0;nws<datJ.W2indexs.size()+1;nws++){//umjesto view4 stavi view3 pravila????
            if(usedViews.contains(nws))
                 continue;
            System.out.println("Current view: "+(nws+1)+" of "+datJ.W2indexs.size());
            int oldIndW=readers.get(nws).newRuleIndex, endIndW=0;
            
            SettingsReader setMW=new SettingsReader();
           if(appset.system.equals("windows")){ 
            setMW.setPath(appset.outFolderPath+"\\view3tmp"+threadID+"W"+nws+".s");
            setMW.setStaticFilePath=appset.outFolderPath+"\\view3tmp"+threadID+"W"+nws+".s";
            setMW.setDataFilePath(appset.outFolderPath+"\\Jinputnew"+threadID+".arff");
           }
           else{
              setMW.setPath(appset.outFolderPath+"/view3tmp"+threadID+"W"+nws+".s");
              setMW.setStaticFilePath=appset.outFolderPath+"/view3tmp"+threadID+"W"+nws+".s";
              setMW.setDataFilePath(appset.outFolderPath+"/Jinputnew"+threadID+".arff"); 
           }
           //OK
            if((nws-1)<(datJ.W2indexs.size()-2+1))
                setMW.createInitialSettingsGen(nws, datJ.W2indexs.get(nws-1)+1 ,datJ.W2indexs.get(nws),datJ.schema.getNbAttributes() , appset,0);//potentially change to datJ.W2indexs.get(nws)+1
            else
                setMW.createInitialSettingsGen(nws, datJ.W2indexs.get(nws-1)+1 ,datJ.schema.getNbAttributes()+1,datJ.schema.getNbAttributes() , appset,0);
            
           
           /* if(nws == 2)
                setMW.createInitialSettingsGen(nws, datJ.W2indexs.get(nws-1)+1 ,datJ.W2indexs.get(nws)+1,datJ.schema.getNbAttributes() , appset,0);
            else
                setMW.createInitialSettingsGen(nws, datJ.W2indexs.get(nws-1)+1 ,datJ.schema.getNbAttributes()+1,datJ.schema.getNbAttributes() , appset,0);
            */
           
              numBins=0;
        Size=rtmp.redescriptions.size();
        if(Size%appset.numTargets==0)
            numBins=Size/appset.numTargets;
        else numBins=Size/appset.numTargets+1;
        System.out.println("num bins: "+numBins+" , tid: "+threadID);    
        for(int z=0;z<numBins;z++){
       
            if(z==0){//should create network from redescriptions!
               if(appset.useNC.size()>nws && appset.networkInit==false){ 
                if(appset.useNC.get(nws)==true && readers.get(nws).rules.size()>0){
                    nclMat.reset(appset);
                    if(appset.distanceFilePaths.size()>=nws && appset.networkInit==false && appset.useNetworkAsBackground==false){
                             nclMat.loadDistance(new File(appset.distanceFilePaths.get(nws)), fid);
                              if(appset.system.equals("windows")){ 
                                     nclMat.writeToFile(new File(appset.outFolderPath+"\\distance.csv"), fid,appset);
                              }
                              else{
                                  nclMat.writeToFile(new File(appset.outFolderPath+"/distance.csv"), fid,appset);
                              }
                    }
                     else if(appset.computeDMfromRules==true){
                             nclMat.computeDistanceMatrix(rtmp.redescriptions, fid, appset.maxDistance, datJ.numExamples,oldRIndex);
                             if(appset.system.equals("windows")){ 
                                    nclMat.resetFile(new File(appset.outFolderPath+"\\distances.csv"));
                                    nclMat.writeToFile(new File(appset.outFolderPath+"\\distances.csv"), fid,appset);
                             }
                             else{
                                 nclMat.resetFile(new File(appset.outFolderPath+"/distances.csv"));
                                 nclMat.writeToFile(new File(appset.outFolderPath+"/distances.csv"), fid,appset);
                             }
                     }
                   }
               }
                endIndW=readers.get(nws).rules.size();
            }
            
            nARules=0; nARules1=0;
            double startPerc=0;//percentage[z];
            double endPerc=0;//percentage[z+1];
            int minCovElements[]=new int[]{0};
            int maxCovElements[]=new int[]{0};
            int cuttof=0;
            
           /* cuttof=rs.findCutoff(naex, startPerc, endPerc, minCovElements,maxCovElements,oldRIndex, appset.minSupport,appset.maxSupport,appset.numTargets);
             System.out.println("minCovElements: "+minCovElements[0]);
             System.out.println("maxCovElements: "+maxCovElements[0]);
             System.out.println("cuttof: "+cuttof);
             
           if(cuttof==-1)
                continue;*/
            
           /*if(appset.system.equals("windows")) 
                dsc=new DataSetCreator(appset.outFolderPath+"\\Jinput.arff");
           else
               dsc=new DataSetCreator(appset.outFolderPath+"/Jinput.arff");*/
           if(appset.system.equals("windows")) 
                dsc=new DataSetCreator(appset.outFolderPath+"\\JinputBack"+threadID+".arff");
           else
               dsc=new DataSetCreator(appset.outFolderPath+"/JinputBack"+threadID+".arff");
        
     //       synchronized(lock){
             try{
        dsc.readDataset();
        }
        catch(IOException e){
            e.printStackTrace();
        }
     //       }
            
            System.out.println("startPerc: "+startPerc);
            System.out.println("endPerc: "+endPerc);
            
            int endTmp=0;
             if((z+1)*appset.numTargets>rtmp.redescriptions.size())
                 endTmp=rtmp.redescriptions.size();
             else endTmp=(z+1)*appset.numTargets;
              //add conditions in the this part of the code...!!!
             int startIndexRR=z*appset.numTargets;//oldRIndex[0]+z*appset.numTargets;
            
            for(int i=startIndexRR;i<endTmp;i++)//oldRIndex[0];i<rs.redescriptions.size();i++) //do on the fly when reading rules
                   // if(rs.redescriptions.get(i).elements.size()<=naex*endPerc && rs.redescriptions.get(i).elements.size()>=naex*startPerc && rs.redescriptions.get(i).elements.size()>=minCovElements[0] && rs.redescriptions.get(i).elements.size()<=maxCovElements[0]) //do parameters analysis in this step
                        nARules++;
             setMW.ModifySettings(nARules,dsc.schema.getNbAttributes());
             try{
                 if(appset.treeTypes.get(nws)==1/*appset.typeOfRSTrees==1*/){ 
                     if(appset.system.equals("windows")) 
                         dsc.modifyDatasetS(startIndexRR,endTmp, rtmp.redescriptions,appset.outFolderPath+"\\Jinputnew"+threadID+".arff",fid,appset);
                     else 
                         dsc.modifyDatasetS(startIndexRR,endTmp, rtmp.redescriptions,appset.outFolderPath+"/Jinputnew"+threadID+".arff",fid,appset);
                 }
         else if(appset.treeTypes.get(nws)==0/*appset.typeOfRSTrees==0*/){
             if(appset.system.equals("windows")) 
                dsc.modifyDatasetCat(startIndexRR,endTmp, rtmp.redescriptions,appset.outFolderPath+"\\Jinputnew"+threadID+".arff",fid,appset);
             else
                dsc.modifyDatasetCat(startIndexRR,endTmp, rtmp.redescriptions,appset.outFolderPath+"/Jinputnew"+threadID+".arff",fid,appset); 
         }
         //if(appset.treeTypes.get(nws)==1/*appset.typeOfRSTrees==1*/)   
               // dsc.modifyDatasetS(nARules, startPerc, endPerc, oldRIndex[0], rs.redescriptions.size(), minCovElements[0],maxCovElements[0], rs.redescriptions,appset.outFolderPath+"\\Jinputnew.arff",fid);
        // else if(appset.treeTypes.get(nws)==0/*appset.typeOfRSTrees==0*/)
          //      dsc.modifyDatasetCat(nARules, startPerc, endPerc, oldRIndex[0], rs.redescriptions.size(), minCovElements[0],maxCovElements[0], rs.redescriptions,appset.outFolderPath+"\\Jinputnew.arff",fid);
        }
        catch(IOException e){
            e.printStackTrace();
        }
             
             exec.run(appset.javaPath, appset.clusPath, appset.outFolderPath, "view3tmp"+threadID+"W"+nws+".s"/*"wbtmp.s"*/, 0,appset.clusteringMemory);//was 1 for rules before
             System.out.println("Process 1 side "+nws+" finished!");
             
             String input;
             if(appset.system.equals("windows")) 
              input=appset.outFolderPath+"\\view3tmp"+threadID+"W"+nws+".out";
             else
                 input=appset.outFolderPath+"/view3tmp"+threadID+"W"+nws+".out";
             
              int newRules=0;
              RuleReader ItRules=new RuleReader();
              
             ItRules.extractRules(input,fid,datJ,appset);
        ItRules.setSize();
            if(z==0)
                newRules=readers.get(nws).addnewRulesC(ItRules, appset.numnewRAttr,1);
            else
                newRules=readers.get(nws).addnewRulesC(ItRules, appset.numnewRAttr,0);
           }
        //exchange with multi-view version
        if(appset.useJoin){//do redescription construction
          //  rtmp.combineViewRulesJoin(readers.get(nws-2), jsN, appset, oldIndW, RunInd, oom, fid, datJ, oldRIndex ,nws); //two-view version
          System.out.println("Max red size: "+appset.maxRSSize);
            rtmp.combineViewRulesJoin(readers.get(nws), jsN, appset, oldIndW, RunInd, oom, fid, datJ, oldRIndex ,nws, appset.maxRSSize); //multi-view version
        }//implement memory management!
        else{//(rr, rr1, jsN, appset, oldIndexRR, oldIndexRR1, RunInd, oom,fid,datJ);
            rtmp.combineViewRules(readers.get(nws-2), jsN, appset, oldIndW, RunInd, oom, fid, datJ, oldRIndex ,nws);
        }
           
        //rs.combineViewRules(readers.get(nws-2), jsN, appset, oldIndW, RunInd, oom, fid, datJ, oldRIndex ,nws);
        }
        
       
        
        appset.numSupplementTrees = numSuppTrees;
       /* if(test == 1)
            break;*/
           
         System.out.println("Redescription size main: "+rtmp.redescriptions.size());
        
         for(int i=0;i<rtmp.redescriptions.size();i++){
            rtmp.redescriptions.get(i).closeInterval(datJ, fid);
            rtmp.redescriptions.get(i).createRuleString(fid);
            System.out.println(rtmp.redescriptions.get(i).ruleStrings.get(0));
            System.out.println(rtmp.redescriptions.get(i).ruleStrings.get(1));
            System.out.println(rtmp.redescriptions.get(i).JS+" "+rtmp.redescriptions.get(i).pVal);
            System.out.println("");System.out.println("");
        }
        
  
         if(leftSide==1){
            leftSide=0;
            rightSide=1;
        }
        else if(rightSide==1){
            rightSide=0;
            leftSide=1;
        }
        if(leftSide1==1){
            leftSide1=0;
            rightSide1=1;
        }
        else if(rightSide1==1){
            rightSide1=0;
            leftSide1=1;
        }
        RunInd++;
        System.out.println("Running index: "+RunInd);
        int tmpPr = 0, tmpPr1 = 0, countNumProduced = 0;
        if(rtmp.redescriptions.size()>appset.workRSSize){
           
                System.out.println("Rs size after reduction WS1: "+rtmp.redescriptions.size());
               
               if(rtmp.redescriptions.size()>((appset.workRSSize+appset.maxRSSize)/2)){
                   
                     tmpPr=rtmp.redescriptions.size();
            //rs.removeIncomplete();
                     
             int numW = datJ.W2indexs.size()+1;
                     
              for(int nwkk=1;nwkk<numW;nwkk++){          
                  rtmp.removeIncomplete(nwkk);
                  if(rtmp.redescriptions.size()<=((appset.workRSSize+appset.maxRSSize)/2))
                   break;
                  
           }        
                     
              tmpPr1=rtmp.redescriptions.size();
               countNumProduced+=tmpPr-tmpPr1;
               
                System.out.println("Rs size after reduction WS: "+rtmp.redescriptions.size());

               if(rtmp.redescriptions.size()>appset.workRSSize){
                   rtmp.computePVal(datJ,fid);
                   rtmp.removePVal(appset);
               }
               
               
               if(rtmp.redescriptions.size()<=((appset.workRSSize+appset.maxRSSize)/2))
                    continue;
                   
                   
                    RedescriptionSet ResultsAll = new RedescriptionSet();
                 CoocurenceMatrix coc=null;
         
         if(datJ.numExamples<10000 && datJ.schema.getNbAttributes()-1<10000){
                coc=new CoocurenceMatrix(datJ.numExamples,datJ.schema.getNbAttributes()-1);
                coc.computeMatrix(rtmp, datJ); 
         }
         
               HashSet<Redescription> tt = new HashSet<>();
           
        if(appset.parameters.size()==0){
            ArrayList<Double> par=new ArrayList<>();
          par.add(appset.minJS); par.add((double)appset.minSupport);  par.add((double)appset.missingValueJSType);
          System.out.println("Configuring the default parameters...");
          appset.parameters.add(par);
        }       
               
       for(int z=0;z<appset.parameters.size();z++){  
                 RedescriptionSet Result=new RedescriptionSet();

      ArrayList<RedescriptionSet> resSets=null; 

      if(datJ.numExamples<10000 && datJ.schema.getNbAttributes()-1<10000)
            resSets=Result.createRedescriptionSetsCoocGen(rtmp,appset.preferences,appset.parameters.get(z).get(2).intValue(), appset,datJ,fid,coc);//adds the most specific redescription first
      else
          resSets=Result.createRedescriptionSetsRandGen(rtmp,appset.preferences,appset.parameters.get(z).get(2).intValue(), appset,datJ,fid,coc);//should add one highly accurate redescription at random
      
          for(int k=0;k<resSets.size();k++){
              tt.addAll(resSets.get(k).redescriptions);
          }
       }
       
       ResultsAll.redescriptions.addAll(tt);
      rtmp.redescriptions.clear();
      System.out.println("t: "+tt.size());
      System.out.println("ResultsAll: "+ResultsAll.redescriptions.size());
      rtmp.redescriptions.addAll(tt);
               }
        
            }
               System.out.println("Rs size after reduction1 WS: "+rtmp.redescriptions.size());
        }
     
        //add the redescription creaton code

        
        //removing all redescriptions with inadequate minSupport and minJS
        rtmp.remove(appset);
         
        System.out.println("Redescription size main after remove: "+rtmp.redescriptions.size());
        
        //filtering
      // rs.filter(appset, rr, rr1,fid,datJ); // think about what we want and if we need it
        
        System.out.println("Redescription size main after filter: "+rtmp.redescriptions.size());
       
      int numFullRed=0;
        //computing pVal...
        numFullRed=rtmp.computePVal(datJ,fid);
        rtmp.removePVal(appset);
        
        for(int i=0;i<rtmp.redescriptions.size();i++){
            rtmp.redescriptions.get(i).closeInterval(datJ, fid);
            rtmp.redescriptions.get(i).createRuleString(fid);
        }
        
        //eliminate incomplete
        
        for(int i=rtmp.redescriptions.size()-1;i>=0;i--){
                 rtmp.redescriptions.get(i).closeInterval(datJ, fid);
                 rtmp.redescriptions.get(i).createRuleString(fid);//ispis debug multi-view
                // System.out.println("nw: "+rtmp.redescriptions.get(i).numViews());
                // System.out.println("nw: "+rtmp.redescriptions.get(i).numViews());
                 //System.out.println(rtmp.redescriptions.get(i));
                 if(rtmp.redescriptions.get(i).numViews()!=(datJ.W2indexs.size()+1))
                     rtmp.redescriptions.remove(i);
             }
        
        for(int i=0;i<appset.importantAttributes.size();i++)
        System.out.println("Should contain"+appset.importantAttributes.get(i).get(0).toString());
         for(int k= rtmp.redescriptions.size()-1;k>=0;k--){
                  if(appset.attributeImportanceGen.size()>0){
                                    rtmp.redescriptions.get(k).closeInterval(datJ, fid);
                                    rtmp.redescriptions.get(k).minimizeOptimal(datJ, fid, 1);
                                if(rtmp.redescriptions.get(k).checkAttributes(appset, fid, datJ)==0){
                                    rtmp.redescriptions.remove(k);
                                }
                           }
             }
        
        if(appset.system.equals("windows"))
                    rtmp.writeToFile(appset.outFolderPath+"\\"+appset.outputName+"DLInterpretabilityMWAll"+t.getName()+appset.minJS+" JSType "+0+"minSupp "+appset.minSupport+".rr", datJ, fid, startTime,rtmp.redescriptions.size(),appset, 0, new double[2] ,oom);
              else
                    rtmp.writeToFile(appset.outFolderPath+"/"+appset.outputName+"DLInterpretabilityMWAll"+t.getName()+appset.minJS+" JSType "+0+"minSupp "+appset.minSupport+".rr", datJ, fid, startTime,rtmp.redescriptions.size(),appset, 0, new double[2],oom);
      rtmp.redescriptions.clear();
    
 
      FileDeleter del=new FileDeleter();
     if(appset.system.equals("windows")){  
      del.setPath(appset.outFolderPath+"\\Jinputnew"+threadID+".arff");
      del.delete();
      del.setPath(appset.outFolderPath+"\\JinputnewC"+threadID+".arff");
      del.delete();
      del.setPath(appset.outFolderPath+"\\Jinput"+threadID+".arff");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmp"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmp"+threadID+".s");
      del.delete();
       del.setPath(appset.outFolderPath+"\\view3tmp"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmpC"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmpC"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmp"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmp"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmp"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmp"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmpC"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmpC"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmpC"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmpC"+threadID+".model");
      del.delete();
      ///
      del.setPath(appset.outFolderPath+"\\view1tmpFC"+threadID+".s");
      del.delete();
     del.setPath(appset.outFolderPath+"\\view2tmpFC"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmpF"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmpF"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmpF"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmpF"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmpF"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmpF"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmpFC"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1tmpFC"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmpFC"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2tmpFC"+threadID+".model");
      del.delete();
      ///
      del.setPath(appset.outFolderPath+"\\view3tmp"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view3tmp"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view1"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"\\view2"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"\\JinputInitial"+threadID+".arff");
      del.delete();

      if(datJ.W2indexs.size()>2){
          for(int iDel=0;iDel<datJ.W2indexs.size();iDel++){
              del.setPath(appset.outFolderPath+"\\view"+(iDel+1)+"tmp"+threadID+".s");
              del.delete();
              del.setPath(appset.outFolderPath+"\\view"+(iDel+1)+"tmp"+threadID+".out");
              del.delete();
              del.setPath(appset.outFolderPath+"\\view"+(iDel+1)+"tmp"+threadID+".model");
              del.delete();
          }
      }
     }
     else{
       del.setPath(appset.outFolderPath+"/Jinputnew"+threadID+".arff");
      del.delete();
      del.setPath(appset.outFolderPath+"/JinputnewC"+threadID+".arff");
      del.delete();
      del.setPath(appset.outFolderPath+"/Jinput"+threadID+".arff");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmp"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmp"+threadID+".s");
      del.delete();
       del.setPath(appset.outFolderPath+"/view3tmp"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmpC"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmpC"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmp"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmp"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmp"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmp"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmpC"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmpC"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmpC"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmpC"+threadID+".model");
      del.delete();
      ///
      del.setPath(appset.outFolderPath+"/view1tmpFC"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmpFC"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmpF"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmpF"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmpF"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmpF"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmpF"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmpF"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmpFC"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1tmpFC"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmpFC"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2tmpFC"+threadID+".model");
      del.delete();
      ///
      del.setPath(appset.outFolderPath+"/view3tmp"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view3tmp"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view1"+threadID+".model");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2"+threadID+".s");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2"+threadID+".out");
      del.delete();
      del.setPath(appset.outFolderPath+"/view2"+threadID+".model");
      del.delete();  
      del.setPath(appset.outFolderPath+"/JinputInitial"+threadID+".arff");
      del.delete();
      if(datJ.W2indexs.size()>1){
          for(int iDel=0;iDel<datJ.W2indexs.size();iDel++){
              del.setPath(appset.outFolderPath+"/view"+(iDel+1)+"tmp"+threadID+".s");
              del.delete();
              del.setPath(appset.outFolderPath+"/view"+(iDel+1)+"tmp"+threadID+".out");
              del.delete();
              del.setPath(appset.outFolderPath+"/view"+(iDel+1)+"tmp"+threadID+".model");
              del.delete();
              del.setPath(appset.outFolderPath+"/view3"+"tmp"+threadID+"W"+(iDel+1)+".s");
              del.delete();
              del.setPath(appset.outFolderPath+"/view3"+"tmp"+threadID+"W"+(iDel+1)+".out");
              del.delete();
              del.setPath(appset.outFolderPath+"/view3"+"tmp"+threadID+"W"+(iDel+1)+".model");
              del.delete();
              System.out.println("Deleted: "+appset.outFolderPath+"/view"+(iDel+1)+"tmp"+threadID+".*");
          }
      }
     }
     appset.importantAttributes.clear();
     appset.attributeImportanceGen.clear();
    }
          
         
          if(appset.system.equals("windows")){
              FileDeleter del=new FileDeleter();
             del.setPath(appset.outFolderPath+"\\JinputBack"+threadID+".arff");
             del.delete();
          }
          
          if(appset.system.equals("linux")){
              FileDeleter del=new FileDeleter();
           del.setPath(appset.outFolderPath+"/JinputBack"+threadID+".arff");
           del.delete();
          }
        
      }
}
