/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package redescriptionmining;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.javatuples.Triplet;

/**
 *
 * @author matej
 */
public class Redescription implements Comparable<Redescription>{
    TIntHashSet elements=null;
    TIntHashSet elementsValidation=null;
    TIntHashSet elementsTest=null;
    
    ArrayList<ArrayList<Conjunction>> viewElementsLists=null;
    
    TIntHashSet elementsUnion=null;
    TIntHashSet elementsUnionValidation=null;
    TIntHashSet elementsUnionTest=null;
   
    ArrayList<String> ruleStrings=null;
    double /*JS=0.0,*/ JSTrain=0.0, JSAll=0.0, JSTest;
    public double JS=0.0, JSPes=0.0, JSOpt=0.0, JSGR=0.0, lift=0.0;
    double pVal=0.0, pValValidation=0.0, pValTest=0.0;
    ArrayList<Integer> supportsSides=null;
    
    Redescription(DataSetCreator dat){
        ruleStrings=new ArrayList<>();
        elements=new TIntHashSet(dat.numExamples);
        viewElementsLists=new ArrayList<>();
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>());
        elementsUnion=new TIntHashSet(dat.numExamples);
        supportsSides=new ArrayList<Integer>();
    }
    
    Redescription(int numViews){
        ruleStrings=new ArrayList<>(numViews);
        
        for(int i=0;i<numViews;i++)
            ruleStrings.add(null);
        
    }
   
    Redescription(RedescriptionReReMiGen R){
        this.JS = R.JS;
        this.pVal = R.pval;
        this.elements = new TIntHashSet();
        this.elements.addAll(R.elements);
        this.elementsUnion = new TIntHashSet();
        this.elementsUnion.addAll(R.elementsUnion);
        this.ruleStrings = new ArrayList<>(R.queries.size());
        for(int i=0;i<R.queries.size();i++)
            this.ruleStrings.add(R.queries.get(i));
    }
    
    Redescription(Redescription r){
         ruleStrings=new ArrayList<>(r.ruleStrings.size());
          for(int i=0;i<r.ruleStrings.size();i++)
             ruleStrings.add(r.ruleStrings.get(i));
    }
    
    
    Redescription deepCopy(Redescription r){
        Redescription tmp = new Redescription(r);
        
        tmp.JS = r.JS;
        tmp.pVal = r.pVal;
        tmp.elements = new TIntHashSet();
        tmp.elements.addAll(r.elements);
        tmp.elementsUnion = new TIntHashSet();
        tmp.elementsUnion.addAll(r.elementsUnion);
        
     
         tmp.viewElementsLists=new ArrayList<>(r.viewElementsLists.size());
         Conjunction c;
        for(int i=0;i<r.viewElementsLists.size()+1;i++){
            tmp.viewElementsLists.add(new ArrayList<Conjunction>(5));
            for(int j=0;j<r.viewElementsLists.get(i).size();j++){
                c = new Conjunction(r.viewElementsLists.get(i).get(j));
               tmp.viewElementsLists.get(i).add(c);
            }
        }
        
        return tmp;
    }
    
    Redescription(Redescription r, int w1, int w2, int numViews){
         ruleStrings=new ArrayList<>(numViews);
         
         for(int i=0;i<numViews;i++)
             ruleStrings.add(null);
         
         ruleStrings.set(w1, r.ruleStrings.get(0)); 
         ruleStrings.set(w2, r.ruleStrings.get(1));
    }
    
      Redescription(String LR, String RR, double sim, Mappings map, DataSetCreator dat, int operator, int view1, int view2){
         ruleStrings=new ArrayList<>(2*dat.W2indexs.size());
         for(int i=0;i<dat.W2indexs.size()+1;i++)
             ruleStrings.add("");
       // ruleStrings.add(LR); ruleStrings.add(RR);
         ruleStrings.set(view1, LR);
         ruleStrings.set(view2, RR);
         
        elements=new TIntHashSet(dat.numExamples);
        viewElementsLists=new ArrayList<>(2*dat.W2indexs.size());
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>(5));
        elementsUnion=new TIntHashSet(dat.numExamples);
        JS=sim;
        
        Conjunction cleft=new Conjunction(LR,dat,map);
        Conjunction cright=new Conjunction(RR,dat,map);
        
        if(operator==0){  
            viewElementsLists.get(view1).add(cleft);
            viewElementsLists.get(view2).add(cright);
        }
        else if(operator==1){
           cright.isNegated=true; 
           viewElementsLists.get(view1).add(cleft);
           viewElementsLists.get(view2).add(cright);
        }
        else{
            cleft.isNegated=true;
            viewElementsLists.get(view1).add(cleft);
            viewElementsLists.get(view2).add(cright);
        }
        supportsSides=new ArrayList<Integer>();
    }
      
      
      Redescription(String LR, String RR, double sim, Mappings map, DataSetCreator dat, int operator, int view1, int view2, double load){
         ruleStrings=new ArrayList<>(2*dat.W2indexs.size());
         for(int i=0;i<dat.W2indexs.size()+1;i++)
             ruleStrings.add("");
       // ruleStrings.add(LR); ruleStrings.add(RR);
         ruleStrings.set(view1, LR);
         ruleStrings.set(view2, RR);
         
        elements=new TIntHashSet(dat.numExamples);
        viewElementsLists=new ArrayList<>(2*dat.W2indexs.size());
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>(5));
        elementsUnion=new TIntHashSet(dat.numExamples);
        JS=sim;
        
        Conjunction cleft=new Conjunction(LR,dat,map,1.0);
        Conjunction cright=new Conjunction(RR,dat,map,1.0);
        
        if(operator==0){  
            viewElementsLists.get(view1).add(cleft);
            viewElementsLists.get(view2).add(cright);
        }
        else if(operator==1){
           cright.isNegated=true; 
           viewElementsLists.get(view1).add(cleft);
           viewElementsLists.get(view2).add(cright);
        }
        else{
            cleft.isNegated=true;
            viewElementsLists.get(view1).add(cleft);
            viewElementsLists.get(view2).add(cright);
        }
        supportsSides=new ArrayList<Integer>();
    }
     
     
    
     Redescription(String LR, String RR, double sim, Mappings map, DataSetCreator dat, int view1, int view2){
        ruleStrings=new ArrayList<>();
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            ruleStrings.add("");
       // ruleStrings.add(LR); ruleStrings.add(RR);
        ruleStrings.set(view1, LR); ruleStrings.set(view2, RR);
        elements=new TIntHashSet(dat.numExamples);
        viewElementsLists=new ArrayList<>();
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>());
        elementsUnion=new TIntHashSet(dat.numExamples);
        JS=sim;

        Conjunction cleft=new Conjunction(LR,dat,map);
        Conjunction cright=new Conjunction(RR,dat,map);
        
        viewElementsLists.get(view1).add(cleft);
        viewElementsLists.get(view2).add(cright);
        supportsSides=new ArrayList<Integer>();
    }
     
     @Override 
              public String toString(){
                  String tmp = "";
                  
                  for(int i=0;i<ruleStrings.size();i++){
                      tmp+="R"+(i+1)+": "+ruleStrings.get(i)+"\n";
                  }
                  
                  tmp+="JS: "+this.JS+"\n";
                  tmp+="p: "+this.pVal+"\n";
                  
                  return tmp;
              }
     
      void computeElements(Rule r1, Rule r2, Rule r3, Rule r4, int view1, int view2, int numViews){

        TIntIterator itleft= r3.ruleAtts.iterator();
       TIntIterator itright= r4.ruleAtts.iterator();
        
       for(int i=0;i<numViews;i++)
           supportsSides.add(0);
       
       supportsSides.set(view1, r1.elements.size());
       supportsSides.set(view2, r2.elements.size());
       
       //supportsSides.add(r1.elements.size());
       //supportsSides.add(r2.elements.size());
      
        TIntIterator iterator = r1.elements.iterator();
        TIntIterator iterator2;
        
                   while (iterator.hasNext()) {
                        int elem = iterator.next();
                        if(r2.elements.contains(elem))
                            elements.add(elem);                    
                    }
        
        iterator = r3.elements.iterator();
        
        while(iterator.hasNext()){
            int elem=iterator.next();
            
            viewElementsLists.get(view1).get(0).elements.add(elem);
           // viewElementsLists.get(0).get(0).elements.add(elem);
        }

        iterator2 = r4.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(view2).get(0).elements.add(elem);
            //viewElementsLists.get(1).get(0).elements.add(elem);
        }
    }
    
     
     void computeElements(Rule r1, Rule r2, int view1, int view2, int numViews){

        for(int i=0;i<numViews;i++)
            supportsSides.add(0);
        
        supportsSides.set(view1,r1.elements.size());
        supportsSides.set(view2,r2.elements.size());
        //supportsSides.add(r1.elements.size());
        //supportsSides.add(r2.elements.size());
      
        TIntIterator iterator = r1.elements.iterator();
        TIntIterator iterator2;
        
                   while (iterator.hasNext()) {
                        int elem = iterator.next();
                        if(r2.elements.contains(elem))
                            elements.add(elem);                   
                    }
        
        iterator = r1.elements.iterator();
        
        while(iterator.hasNext()){
            int elem=iterator.next();
            viewElementsLists.get(view1).get(0).elements.add(elem);
        }

        iterator2 = r2.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(view2).get(0).elements.add(elem);
        }
    }
    
    
    int checkMerge(Redescription r){
        int index = -1;
        
        for(int i=0;i<ruleStrings.size();i++)
            if(ruleStrings.get(i)!=null && r.ruleStrings.get(i)!=null)
                index = 1;
        
        int found = 0;
        
         Redescription rtmp = new Redescription(ruleStrings.size());
        
        for(int i=0;i<r.ruleStrings.size();i++)
            if(r.ruleStrings.get(i)!=null && rtmp.ruleStrings.get(i) == null){
                rtmp.ruleStrings.set(i, r.ruleStrings.get(i));
                found = 1;
            }
        
        if(found == 0)
            return 0;
        
        if(index == -1)
            return 1;
        else return 2;
    }
    
    ArrayList<Redescription> merge(Redescription r){
        ArrayList<Redescription> res = new ArrayList<>();
        int index = -1;
        
        for(int i=0;i<ruleStrings.size();i++)
            if(ruleStrings.get(i)!=null && r.ruleStrings.get(i)!=null){
                index = 1;
                break;
            }
        
        Redescription rtmp = new Redescription(ruleStrings.size());
        
        for(int i=0;i<ruleStrings.size();i++)
                if(ruleStrings.get(i)!=null)
                    rtmp.ruleStrings.set(i, ruleStrings.get(i));
        
        int found = 0;
        
        for(int i=0;i<r.ruleStrings.size();i++)
            if(r.ruleStrings.get(i)!=null && rtmp.ruleStrings.get(i) == null){
                rtmp.ruleStrings.set(i, r.ruleStrings.get(i));
                found = 1;
            }
                       
        if(found == 0)
            return res;
        
        res.add(rtmp);
        
        if(index == 1){
            rtmp = new Redescription(ruleStrings.size());
            
            for(int i=0;i<r.ruleStrings.size();i++)
                if(r.ruleStrings.get(i)!=null)
                    rtmp.ruleStrings.set(i, r.ruleStrings.get(i));
        
        for(int i=0;i<ruleStrings.size();i++)
            if(ruleStrings.get(i)!=null && rtmp.ruleStrings.get(i) == null)
                rtmp.ruleStrings.set(i, ruleStrings.get(i));
        
          res.add(rtmp);
        }
        
        return res;
    }
    
    int numViews(){
        int nw = 0;
        
        for(int i=0;i<this.ruleStrings.size();i++)
            if(ruleStrings.get(i) != null && this.viewElementsLists.get(i).size()!=0)
                nw++;
        
        return nw;
    }
    
    void computeStatistics(ArrayList<DataSetCreator> dats, ArrayList<Mappings> mappings){
        //J, p-val, support
        HashSet<Integer> missingView = new HashSet<>();
         ArrayList<TIntHashSet> Supports=new ArrayList<>();// TIntHashSet(dataset.numExamples);
     
         //fill structures from queries
         //create attribute, numerical bound mapping and attribute category mapping
         
         HashMap<Integer,HashMap<Integer,HashMap<String,ArrayList<Double>>>> numericalAttributes = new HashMap<>();
         HashMap<Integer,HashMap<Integer,HashMap<String,String>>> categoricalAttributes = new HashMap<>();
         HashMap<Integer,HashSet<Integer>> negatedClauses = new HashMap<>();
         
         this.viewElementsLists = new ArrayList<>();
        
         for(int i=0;i<this.ruleStrings.size();i++){
            // System.out.println("Mappings size: "+mappings.get(i).attId.keySet().size());
             this.viewElementsLists.add(new ArrayList<Conjunction>());
             if(this.ruleStrings.get(i) == null)
                 continue;
             
             //System.out.println(this.ruleStrings.get(i));
             String elConj[] = this.ruleStrings.get(i).replaceAll("\\)OR", "OR").split(" OR ");
             
             for(int j=0;j<elConj.length;j++){
                 //System.out.println("El conj: "+elConj[j]);
                 if(elConj[j].contains("NOT ")){
                     int negated = 1;
                     if(!negatedClauses.containsKey(i))
                         negatedClauses.put(i, new HashSet<Integer>());
                     negatedClauses.get(i).add(j);
                     String rule = elConj[j].trim().replace("NOT ", "");
                     rule = rule.replaceAll("\\(", "");
                     rule = rule.replaceAll("\\)", "").trim();
                     String attrs[] = rule.split(" AND ");
                     
                     for(int zz = 0; zz<attrs.length;zz++){
                         if(attrs[zz].contains(">=")){
                            String t[]=attrs[zz].split(" >= ");
                            
                            if(!numericalAttributes.containsKey(i))
                                    numericalAttributes.put(i, new HashMap<Integer,HashMap<String,ArrayList<Double>>>());
                            
                            if(!numericalAttributes.get(i).containsKey(j))
                                numericalAttributes.get(i).put(j, new HashMap<String,ArrayList<Double>>());
                    
                            String at = t[0].trim();
                            ArrayList<Double> bounds = new ArrayList<>();
                            
                            String t1[] = t[1].split(" <= ");
                            bounds.add(Double.parseDouble(t1[0]));
                            bounds.add(Double.parseDouble(t1[1]));
                            
                            numericalAttributes.get(i).get(j).put(at,bounds);
                            
                     }               
                else if(attrs[zz].contains("=") && !attrs[zz].contains("<")){
                      String t[]=attrs[zz].split(" = ");
                      String at = t[0].trim();
                      String cat = t[1].trim();
                      
                      if(!categoricalAttributes.containsKey(i))
                                    categoricalAttributes.put(i, new HashMap<Integer,HashMap<String,String>>());
                            
                      if(!categoricalAttributes.get(i).containsKey(j))
                                categoricalAttributes.get(i).put(j, new HashMap<String,String>());
                      categoricalAttributes.get(i).get(j).put(at, cat);
                    
                      
                }
                     }
                     
                     Conjunction c = new Conjunction(rule,dats.get(i),mappings.get(i),0);
                     c.isNegated = true;
                     this.viewElementsLists.get(i).add(c);
                 }
                 else{
                     String rule = elConj[j].trim();
                      rule = rule.replaceAll("\\(", "");
                     rule = rule.replaceAll("\\)", "").trim();
                      Conjunction c = new Conjunction(rule,dats.get(i),mappings.get(i),0);
                      this.viewElementsLists.get(i).add(c);
                      
                      
                     String attrs[] = rule.split(" AND ");
                     
                     for(int zz = 0; zz<attrs.length;zz++){
                         if(attrs[zz].contains(">=")){
                            String t[]=attrs[zz].split(" >= ");
                            //System.out.println("attrs: "+attrs[zz].trim());
                            if(!numericalAttributes.containsKey(i))
                                    numericalAttributes.put(i, new HashMap<Integer,HashMap<String,ArrayList<Double>>>());
                            
                            if(!numericalAttributes.get(i).containsKey(j))
                                numericalAttributes.get(i).put(j, new HashMap<String,ArrayList<Double>>());
                    
                            String at = t[0].trim();
                            ArrayList<Double> bounds = new ArrayList<>();
                            
                            String t1[] = t[1].split(" <= ");
                            bounds.add(Double.parseDouble(t1[0]));
                            bounds.add(Double.parseDouble(t1[1]));
                            
                            numericalAttributes.get(i).get(j).put(at,bounds);
                            
                     }               
                else if(attrs[zz].contains("=") && !attrs[zz].contains("<")){
                      String t[]=attrs[zz].split(" = ");
                      String at = t[0].trim();
                      String cat = t[1].trim();
                      
                      if(!categoricalAttributes.containsKey(i))
                                    categoricalAttributes.put(i, new HashMap<Integer,HashMap<String,String>>());
                            
                      if(!categoricalAttributes.get(i).containsKey(j))
                                categoricalAttributes.get(i).put(j, new HashMap<String,String>());
                      categoricalAttributes.get(i).get(j).put(at, cat);
                    
                      
                } 
                     }
                 }
             }           
         }
         
        for(int k=0;k<viewElementsLists.size();k++)
            Supports.add(new TIntHashSet(dats.get(0).numExamples));
        
     for(int k=0;k<viewElementsLists.size();k++){
         
         if(this.ruleStrings.get(k) == null){
             missingView.add(k);
             continue;
         }
       for(int c=0;c<viewElementsLists.get(k).size();c++){  
           Conjunction conj=viewElementsLists.get(k).get(c);
           
           int negated = 0;
           
          // if(negatedClauses.get(k).contains(c))
           //    negated = 1;
           
         for(int i=0;i<dats.get(0).numExamples;i++){
            // TIntIterator itL=conj.Rule.keySet().iterator();
             int contained=1;
            
        if(conj.isNegated==false){  
            
            if(numericalAttributes.containsKey(k)){
                if(numericalAttributes.get(k).containsKey(c)){
            
            HashMap<String, ArrayList<Double>> nats = numericalAttributes.get(k).get(c);
            Iterator<String> itL = nats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 
                 ArrayList<Double> attrVal=nats.get(attr);
                 /*System.out.println("Attribute: "+map.idAtt.get(attr));
                 System.out.println("view: "+k);
                 System.out.println("conjunction: "+c);
                 System.out.println("elements size: "+conj.elements.size());
                 System.out.println("rule attr size: "+conj.Rule.keySet().size());
                 System.out.println("attrs size: "+conj.attributes.size());*/
                 
               //if(!mappings.get(k).cattAtt.containsKey(attr)){
                   double val=dats.get(k).getValue(atCode, i);
                 if(val>=attrVal.get(0) && val<=attrVal.get(1))
                        continue;
                 else{
                    contained=0;
                    break;
                 }
             //  }
              /* else{
                   String cat=mappings.get(k).cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(0));
                   String realCat=dats.get(k).getValueCategorical(attr, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else{
                       contained=0;
                       break;
                   }
               }*/
             }
            }
          }
            
             if(contained!=0){
              
              if(categoricalAttributes.containsKey(k)){   
                 if(categoricalAttributes.get(k).containsKey(c)){
                 
             HashMap<String, String> cats = categoricalAttributes.get(k).get(c);
             Iterator<String> itL = cats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 String cat= cats.get(attr);
                   String realCat=dats.get(k).getValueCategorical(atCode, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else{
                       contained=0;
                       break;
                   }
             }
            }
          }
           }
             
        }
             else{
                 contained=0;
              
            if(numericalAttributes.containsKey(k)){
                if(numericalAttributes.get(k).containsKey(c)){     
                 HashMap<String, ArrayList<Double>> nats = numericalAttributes.get(k).get(c);
            Iterator<String> itL = nats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 
                 ArrayList<Double> attrVal=nats.get(attr);

                   double val=dats.get(k).getValue(atCode, i);
                 if(val<attrVal.get(0) || (val>attrVal.get(1) && val!=Double.POSITIVE_INFINITY)){
                        contained = 1;
                        break;
                 }
                 else{
                    continue;
                 }
             }
            }
           }

              if(contained!=1){
                  
                   if(categoricalAttributes.containsKey(k)){   
                 if(categoricalAttributes.get(k).containsKey(c)){
                     
                 
             HashMap<String, String> cats = categoricalAttributes.get(k).get(c);
             Iterator<String> itL = cats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 String cat = cats.get(attr);
                   String realCat=dats.get(k).getValueCategorical(atCode, i);
                   if(!cat.contentEquals(realCat) && !realCat.equals("?")){
                       contained = 1;
                       break;
                   }
                   else{
                       continue;
                   }
             }
            }
          }
           }              
             }

             if(contained==0)
                 continue;
             else{
                 //lSupport.add(i);
                 Supports.get(k).add(i);
             }
         }
       }
     }
     
     HashSet<Integer> intersection = new HashSet<>();
     HashSet<Integer> union = new HashSet<>();
     
     TIntHashSet start = null;
     
     int startInd = -1;
     
      for(int k=0;k<viewElementsLists.size();k++)
          if(missingView.contains(k))
              continue;
          else{
              start = Supports.get(k);
              startInd = k;
          }
      
      TIntIterator it = start.iterator();
      
      while(it.hasNext()){
          int el = it.next();
          int contained = 1;
           for(int k=0;k<viewElementsLists.size();k++){
                if(missingView.contains(k) || k == startInd)
                        continue;
                if(!Supports.get(k).contains(el)){
                    contained = 0;
                    break;
                }
                
           }
           
           if(contained == 1)
               intersection.add(el);          
      }
      
      for(int k=0;k<viewElementsLists.size();k++){
                if(missingView.contains(k) || k == startInd)
                        continue;
                
            it = Supports.get(k).iterator();
            
            while(it.hasNext()){
                union.add(it.next());
            }
            
      }
      
      /*System.out.println("intersection size: "+intersection.size());
      System.out.println("union size: "+union.size());
      System.out.println("Missing: "+missingView.size());
      System.out.println("Num views: "+viewElementsLists.size());*/
      
      this.elements = new TIntHashSet();
      this.elementsUnion = new TIntHashSet();
      
      this.JS = (double)intersection.size()/(double)union.size();
      this.elements.addAll(intersection);
      this.elementsUnion.addAll(union);
      
       double elemSize=1.0, numExamples=1.0;
            
            for(int k=0;k<this.viewElementsLists.size();k++){
                if(!missingView.contains(k)){
                    elemSize*=Supports.get(k).size();
                    numExamples*=dats.get(0).numExamples;
                }
            }
            
            double prob=elemSize/numExamples;
            BinomialDistribution dist=new BinomialDistribution(dats.get(0).numExamples,prob);
            this.pVal=1.0-dist.cumulativeProbability(this.elements.size());
     
    }
    
    Redescription computeStatisticsAndReturn(Redescription R2, int type, ArrayList<DataSetCreator> dats, ArrayList<Mappings> mappings, double minJ, double minSupp){
       //type: 0, only direct joing possible (take all from the first red, add complement from the second)
        //type: 1, take all from the second red, add complement from the first
        
        Redescription tmp = null;
        
        if(type == 0){
            //J, p-val, support
        HashSet<Integer> missingView = new HashSet<>();
         ArrayList<TIntHashSet> Supports=new ArrayList<>();// TIntHashSet(dataset.numExamples);
        ArrayList<ArrayList<Conjunction>> Conjunctions = new ArrayList<>();
         //fill structures from queries
         //create attribute, numerical bound mapping and attribute category mapping
         
         HashMap<Integer,HashMap<Integer,HashMap<String,ArrayList<Double>>>> numericalAttributes = new HashMap<>();
         HashMap<Integer,HashMap<Integer,HashMap<String,String>>> categoricalAttributes = new HashMap<>();
         HashMap<Integer,HashSet<Integer>> negatedClauses = new HashMap<>();
         
         for(int i=0;i<this.ruleStrings.size();i++){
             Conjunctions.add(new ArrayList<Conjunction>());
            String elConj[] = null;
             if(this.ruleStrings.get(i) == null && R2.ruleStrings.get(i) == null)
                 continue;
             else if(this.ruleStrings.get(i)!= null && R2.ruleStrings.get(i) == null)            
                elConj= this.ruleStrings.get(i).replaceAll("\\)OR", "OR").split(" OR ");
             else if(this.ruleStrings.get(i) == null && R2.ruleStrings.get(i) != null) 
                 elConj= R2.ruleStrings.get(i).replaceAll("\\)OR", "OR").split(" OR ");
             else if(this.ruleStrings.get(i) != null && R2.ruleStrings.get(i) != null)
                 elConj= this.ruleStrings.get(i).replaceAll("\\)OR", "OR").split(" OR ");
             
             for(int j=0;j<elConj.length;j++){

                 if(elConj[j].contains("NOT ")){
                     int negated = 1;
                     if(!negatedClauses.containsKey(i))
                         negatedClauses.put(i, new HashSet<Integer>());
                     negatedClauses.get(i).add(j);
                     String rule = elConj[j].trim().replace("NOT ", "");
                     rule = rule.replaceAll("\\(", "");
                     rule = rule.replaceAll("\\)", "").trim();
                     String attrs[] = rule.split(" AND ");
                     
                     for(int zz = 0; zz<attrs.length;zz++){
                         if(attrs[zz].contains(">=")){
                            String t[]=attrs[zz].split(" >= ");
                            
                            if(!numericalAttributes.containsKey(i))
                                    numericalAttributes.put(i, new HashMap<Integer,HashMap<String,ArrayList<Double>>>());
                            
                            if(!numericalAttributes.get(i).containsKey(j))
                                numericalAttributes.get(i).put(j, new HashMap<String,ArrayList<Double>>());
                    
                            String at = t[0].trim();
                            ArrayList<Double> bounds = new ArrayList<>();
                            
                            String t1[] = t[1].split(" <= ");
                            bounds.add(Double.parseDouble(t1[0]));
                            bounds.add(Double.parseDouble(t1[1]));
                            
                            numericalAttributes.get(i).get(j).put(at,bounds);
                            
                     }               
                else if(attrs[zz].contains("=") && !attrs[zz].contains("<")){
                      String t[]=attrs[zz].split(" = ");
                      String at = t[0].trim();
                      String cat = t[1].trim();
                      
                      if(!categoricalAttributes.containsKey(i))
                                    categoricalAttributes.put(i, new HashMap<Integer,HashMap<String,String>>());
                            
                      if(!categoricalAttributes.get(i).containsKey(j))
                                categoricalAttributes.get(i).put(j, new HashMap<String,String>());
                      categoricalAttributes.get(i).get(j).put(at, cat);
                    
                      
                }
                     }
                     
                     Conjunction c = new Conjunction(rule,dats.get(i),mappings.get(i),0);
                     c.isNegated = true;
                     Conjunctions.get(i).add(c);
                 }
                 else{
                     String rule = elConj[j].trim();
                      rule = rule.replaceAll("\\(", "");
                     rule = rule.replaceAll("\\)", "").trim();
                      Conjunction c = new Conjunction(rule,dats.get(i),mappings.get(i),0);
                      Conjunctions.get(i).add(c);
                      
                     String attrs[] = rule.split(" AND ");
                     
                     for(int zz = 0; zz<attrs.length;zz++){
                         if(attrs[zz].contains(">=")){
                            String t[]=attrs[zz].split(" >= ");
                            //System.out.println("attrs: "+attrs[zz].trim());
                            if(!numericalAttributes.containsKey(i))
                                    numericalAttributes.put(i, new HashMap<Integer,HashMap<String,ArrayList<Double>>>());
                            
                            if(!numericalAttributes.get(i).containsKey(j))
                                numericalAttributes.get(i).put(j, new HashMap<String,ArrayList<Double>>());
                    
                            String at = t[0].trim();
                            ArrayList<Double> bounds = new ArrayList<>();
                            
                            String t1[] = t[1].split(" <= ");
                            bounds.add(Double.parseDouble(t1[0]));
                            bounds.add(Double.parseDouble(t1[1]));
                            
                            numericalAttributes.get(i).get(j).put(at,bounds);
                            
                     }               
                else if(attrs[zz].contains("=") && !attrs[zz].contains("<")){
                      String t[]=attrs[zz].split(" = ");
                      String at = t[0].trim();
                      String cat = t[1].trim();
                      
                      if(!categoricalAttributes.containsKey(i))
                                    categoricalAttributes.put(i, new HashMap<Integer,HashMap<String,String>>());
                            
                      if(!categoricalAttributes.get(i).containsKey(j))
                                categoricalAttributes.get(i).put(j, new HashMap<String,String>());
                      categoricalAttributes.get(i).get(j).put(at, cat);
                    
                      
                } 
                     }
                 }
             }           
         }
         
        for(int k=0;k<ruleStrings.size();k++)
            Supports.add(new TIntHashSet(dats.get(0).numExamples));
        
     for(int k=0;k<ruleStrings.size();k++){
         
         if(this.ruleStrings.get(k) == null && R2.ruleStrings.get(k) == null){
             missingView.add(k);
             continue;
         }
       for(int c=0;c<Conjunctions.get(k).size();c++){  
           Conjunction conj=Conjunctions.get(k).get(c);
           
           int negated = 0;
           
         for(int i=0;i<dats.get(0).numExamples;i++){

             int contained=1;
            
        if(conj.isNegated==false){  
            
            if(numericalAttributes.containsKey(k)){
                if(numericalAttributes.get(k).containsKey(c)){
            
            HashMap<String, ArrayList<Double>> nats = numericalAttributes.get(k).get(c);
            Iterator<String> itL = nats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 
                 ArrayList<Double> attrVal=nats.get(attr);
                
                   double val=dats.get(k).getValue(atCode, i);
                 if(val>=attrVal.get(0) && val<=attrVal.get(1))
                        continue;
                 else{
                    contained=0;
                    break;
                 }
             }
            }
          }
            
             if(contained!=0){
              
              if(categoricalAttributes.containsKey(k)){   
                 if(categoricalAttributes.get(k).containsKey(c)){
                 
             HashMap<String, String> cats = categoricalAttributes.get(k).get(c);
             Iterator<String> itL = cats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 String cat= cats.get(attr);
                   String realCat=dats.get(k).getValueCategorical(atCode, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else{
                       contained=0;
                       break;
                   }
             }
            }
          }
           }
             
        }
             else{
                 contained=0;
              
            if(numericalAttributes.containsKey(k)){
                if(numericalAttributes.get(k).containsKey(c)){     
                 HashMap<String, ArrayList<Double>> nats = numericalAttributes.get(k).get(c);
            Iterator<String> itL = nats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 
                 ArrayList<Double> attrVal=nats.get(attr);

                   double val=dats.get(k).getValue(atCode, i);
                 if(val<attrVal.get(0) || (val>attrVal.get(1) && val!=Double.POSITIVE_INFINITY)){
                        contained = 1;
                        break;
                 }
                 else{
                    continue;
                 }
             }
            }
           }

              if(contained!=1){
                  
                   if(categoricalAttributes.containsKey(k)){   
                 if(categoricalAttributes.get(k).containsKey(c)){
                     
                 
             HashMap<String, String> cats = categoricalAttributes.get(k).get(c);
             Iterator<String> itL = cats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 String cat = cats.get(attr);
                   String realCat=dats.get(k).getValueCategorical(atCode, i);
                   if(!cat.contentEquals(realCat) && !realCat.equals("?")){
                       contained = 1;
                       break;
                   }
                   else{
                       continue;
                   }
             }
            }
          }
           }              
             }

             if(contained==0)
                 continue;
             else{
                 //lSupport.add(i);
                 Supports.get(k).add(i);
             }
         }
       }
     }
     
     HashSet<Integer> intersection = new HashSet<>();
     HashSet<Integer> union = new HashSet<>();
     
     TIntHashSet start = null;
     
     int startInd = -1;
     
      for(int k=0;k<this.ruleStrings.size();k++)
          if(missingView.contains(k))
              continue;
          else{
              start = Supports.get(k);
              startInd = k;
          }
      
      TIntIterator it = start.iterator();
      
      while(it.hasNext()){
          int el = it.next();
          int contained = 1;
           for(int k=0;k<Conjunctions.size();k++){
                if(missingView.contains(k) || k == startInd)
                        continue;
                if(!Supports.get(k).contains(el)){
                    contained = 0;
                    break;
                }
                
           }
           
           if(contained == 1)
               intersection.add(el);          
      }
      
      for(int k=0;k<Conjunctions.size();k++){
                if(missingView.contains(k) || k == startInd)
                        continue;
                
            it = Supports.get(k).iterator();
            
            while(it.hasNext()){
                union.add(it.next());
            }
            
      }
      //check if OK. 
      
      double j = (double)intersection.size()/(double)union.size();
       double elemSize=1.0, numExamples=1.0;
            
            for(int k=0;k<this.ruleStrings.size();k++){
                if(!missingView.contains(k)){
                    elemSize*=Supports.get(k).size();
                    numExamples*=dats.get(0).numExamples;
                }
            }
      double prob=elemSize/numExamples;
            BinomialDistribution dist=new BinomialDistribution(dats.get(0).numExamples,prob);
          double  p=1.0-dist.cumulativeProbability(intersection.size());
       
      
          if(j>=minJ && intersection.size()>=minSupp)
                              tmp = new Redescription(this);
          else return null;
          
          for(int i=0;i<tmp.ruleStrings.size();i++)
              if(tmp.ruleStrings.get(i) == null && R2.ruleStrings.get(i)!=null)
                  tmp.ruleStrings.set(i, R2.ruleStrings.get(i));
          
          
      tmp.elements = new TIntHashSet();
      tmp.elementsUnion = new TIntHashSet();
      
       tmp.JS = (double)intersection.size()/(double)union.size();
       tmp.elements.addAll(intersection);
       tmp.elementsUnion.addAll(union);
      
      tmp.JS = j;
      tmp.pVal = p;
            
        //    double prob=elemSize/numExamples;
          //  BinomialDistribution dist=new BinomialDistribution(dats.get(0).numExamples,prob);
           // this.pVal=1.0-dist.cumulativeProbability(this.elements.size());
        }
        else if(type == 1){
                  //J, p-val, support
        HashSet<Integer> missingView = new HashSet<>();
         ArrayList<TIntHashSet> Supports=new ArrayList<>();// TIntHashSet(dataset.numExamples);
        ArrayList<ArrayList<Conjunction>> Conjunctions = new ArrayList<>();
         //fill structures from queries
         //create attribute, numerical bound mapping and attribute category mapping
         
         HashMap<Integer,HashMap<Integer,HashMap<String,ArrayList<Double>>>> numericalAttributes = new HashMap<>();
         HashMap<Integer,HashMap<Integer,HashMap<String,String>>> categoricalAttributes = new HashMap<>();
         HashMap<Integer,HashSet<Integer>> negatedClauses = new HashMap<>();
         
         for(int i=0;i<this.ruleStrings.size();i++){
             Conjunctions.add(new ArrayList<Conjunction>());
            String elConj[] = null;
             if(this.ruleStrings.get(i) == null && R2.ruleStrings.get(i) == null)
                 continue;
             else if(R2.ruleStrings.get(i)!= null && this.ruleStrings.get(i) == null)            
                elConj= R2.ruleStrings.get(i).replaceAll("\\)OR", "OR").split(" OR ");
             else if(R2.ruleStrings.get(i) == null && this.ruleStrings.get(i) != null) 
                 elConj= this.ruleStrings.get(i).replaceAll("\\)OR", "OR").split(" OR ");
             else if(this.ruleStrings.get(i) != null && R2.ruleStrings.get(i) != null)
                 elConj= R2.ruleStrings.get(i).replaceAll("\\)OR", "OR").split(" OR ");
             
             for(int j=0;j<elConj.length;j++){

                 if(elConj[j].contains("NOT ")){
                     int negated = 1;
                     if(!negatedClauses.containsKey(i))
                         negatedClauses.put(i, new HashSet<Integer>());
                     negatedClauses.get(i).add(j);
                     String rule = elConj[j].trim().replace("NOT ", "");
                     rule = rule.replaceAll("\\(", "");
                     rule = rule.replaceAll("\\)", "").trim();
                     String attrs[] = rule.split(" AND ");
                     
                     for(int zz = 0; zz<attrs.length;zz++){
                         if(attrs[zz].contains(">=")){
                            String t[]=attrs[zz].split(" >= ");
                            
                            if(!numericalAttributes.containsKey(i))
                                    numericalAttributes.put(i, new HashMap<Integer,HashMap<String,ArrayList<Double>>>());
                            
                            if(!numericalAttributes.get(i).containsKey(j))
                                numericalAttributes.get(i).put(j, new HashMap<String,ArrayList<Double>>());
                    
                            String at = t[0].trim();
                            ArrayList<Double> bounds = new ArrayList<>();
                            
                            String t1[] = t[1].split(" <= ");
                            bounds.add(Double.parseDouble(t1[0]));
                            bounds.add(Double.parseDouble(t1[1]));
                            
                            numericalAttributes.get(i).get(j).put(at,bounds);
                            
                     }               
                else if(attrs[zz].contains("=") && !attrs[zz].contains("<")){
                      String t[]=attrs[zz].split(" = ");
                      String at = t[0].trim();
                      String cat = t[1].trim();
                      
                      if(!categoricalAttributes.containsKey(i))
                                    categoricalAttributes.put(i, new HashMap<Integer,HashMap<String,String>>());
                            
                      if(!categoricalAttributes.get(i).containsKey(j))
                                categoricalAttributes.get(i).put(j, new HashMap<String,String>());
                      categoricalAttributes.get(i).get(j).put(at, cat);
                    
                      
                }
                     }
                     
                     Conjunction c = new Conjunction(rule,dats.get(i),mappings.get(i),0);
                     c.isNegated = true;
                     Conjunctions.get(i).add(c);
                 }
                 else{
                     String rule = elConj[j].trim();
                      rule = rule.replaceAll("\\(", "");
                     rule = rule.replaceAll("\\)", "").trim();
                      Conjunction c = new Conjunction(rule,dats.get(i),mappings.get(i),0);
                      Conjunctions.get(i).add(c);
                      
                     String attrs[] = rule.split(" AND ");
                     
                     for(int zz = 0; zz<attrs.length;zz++){
                         if(attrs[zz].contains(">=")){
                            String t[]=attrs[zz].split(" >= ");
                            //System.out.println("attrs: "+attrs[zz].trim());
                            if(!numericalAttributes.containsKey(i))
                                    numericalAttributes.put(i, new HashMap<Integer,HashMap<String,ArrayList<Double>>>());
                            
                            if(!numericalAttributes.get(i).containsKey(j))
                                numericalAttributes.get(i).put(j, new HashMap<String,ArrayList<Double>>());
                    
                            String at = t[0].trim();
                            ArrayList<Double> bounds = new ArrayList<>();
                            
                            String t1[] = t[1].split(" <= ");
                            bounds.add(Double.parseDouble(t1[0]));
                            bounds.add(Double.parseDouble(t1[1]));
                            
                            numericalAttributes.get(i).get(j).put(at,bounds);
                            
                     }               
                else if(attrs[zz].contains("=") && !attrs[zz].contains("<")){
                      String t[]=attrs[zz].split(" = ");
                      String at = t[0].trim();
                      String cat = t[1].trim();
                      
                      if(!categoricalAttributes.containsKey(i))
                                    categoricalAttributes.put(i, new HashMap<Integer,HashMap<String,String>>());
                            
                      if(!categoricalAttributes.get(i).containsKey(j))
                                categoricalAttributes.get(i).put(j, new HashMap<String,String>());
                      categoricalAttributes.get(i).get(j).put(at, cat);     
                } 
                     }
                 }
             }           
         }
         
        for(int k=0;k<ruleStrings.size();k++)
            Supports.add(new TIntHashSet(dats.get(0).numExamples));
        
     for(int k=0;k<ruleStrings.size();k++){
         
         if(this.ruleStrings.get(k) == null && R2.ruleStrings.get(k) == null){
             missingView.add(k);
             continue;
         }
       for(int c=0;c<Conjunctions.get(k).size();c++){  
           Conjunction conj=Conjunctions.get(k).get(c);
           
           int negated = 0;
           
         for(int i=0;i<dats.get(0).numExamples;i++){

             int contained=1;
            
        if(conj.isNegated==false){  
            
            if(numericalAttributes.containsKey(k)){
                if(numericalAttributes.get(k).containsKey(c)){
            
            HashMap<String, ArrayList<Double>> nats = numericalAttributes.get(k).get(c);
            Iterator<String> itL = nats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 
                 ArrayList<Double> attrVal=nats.get(attr);
                
                   double val=dats.get(k).getValue(atCode, i);
                 if(val>=attrVal.get(0) && val<=attrVal.get(1))
                        continue;
                 else{
                    contained=0;
                    break;
                 }
             }
            }
          }
            
             if(contained!=0){
              
              if(categoricalAttributes.containsKey(k)){   
                 if(categoricalAttributes.get(k).containsKey(c)){
                 
             HashMap<String, String> cats = categoricalAttributes.get(k).get(c);
             Iterator<String> itL = cats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 String cat= cats.get(attr);
                   String realCat=dats.get(k).getValueCategorical(atCode, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else{
                       contained=0;
                       break;
                   }
             }
            }
          }
           }
             
        }
             else{
                 contained=0;
              
            if(numericalAttributes.containsKey(k)){
                if(numericalAttributes.get(k).containsKey(c)){     
                 HashMap<String, ArrayList<Double>> nats = numericalAttributes.get(k).get(c);
            Iterator<String> itL = nats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 
                 ArrayList<Double> attrVal=nats.get(attr);

                   double val=dats.get(k).getValue(atCode, i);
                 if(val<attrVal.get(0) || (val>attrVal.get(1) && val!=Double.POSITIVE_INFINITY)){
                        contained = 1;
                        break;
                 }
                 else{
                    continue;
                 }
             }
            }
           }

              if(contained!=1){
                  
                   if(categoricalAttributes.containsKey(k)){   
                 if(categoricalAttributes.get(k).containsKey(c)){
                     
                 
             HashMap<String, String> cats = categoricalAttributes.get(k).get(c);
             Iterator<String> itL = cats.keySet().iterator();
            
             while(itL.hasNext()){
                 String attr=itL.next();
                 int atCode = mappings.get(k).attId.get(attr);
                 String cat = cats.get(attr);
                   String realCat=dats.get(k).getValueCategorical(atCode, i);
                   if(!cat.contentEquals(realCat) && !realCat.equals("?")){
                       contained = 1;
                       break;
                   }
                   else{
                       continue;
                   }
             }
            }
          }
           }              
             }

             if(contained==0)
                 continue;
             else{
                 //lSupport.add(i);
                 Supports.get(k).add(i);
             }
         }
       }
     }
     
     HashSet<Integer> intersection = new HashSet<>();
     HashSet<Integer> union = new HashSet<>();
     
     TIntHashSet start = null;
     
     int startInd = -1;
     
      for(int k=0;k<this.ruleStrings.size();k++)
          if(missingView.contains(k))
              continue;
          else{
              start = Supports.get(k);
              startInd = k;
          }
      
      TIntIterator it = start.iterator();
      
      while(it.hasNext()){
          int el = it.next();
          int contained = 1;
           for(int k=0;k<Conjunctions.size();k++){
                if(missingView.contains(k) || k == startInd)
                        continue;
                if(!Supports.get(k).contains(el)){
                    contained = 0;
                    break;
                }
                
           }
           
           if(contained == 1)
               intersection.add(el);          
      }
      
      for(int k=0;k<Conjunctions.size();k++){
                if(missingView.contains(k) || k == startInd)
                        continue;
                
            it = Supports.get(k).iterator();
            
            while(it.hasNext()){
                union.add(it.next());
            }
            
      }
      //check if OK. 
      
      double j = (double)intersection.size()/(double)union.size();
       double elemSize=1.0, numExamples=1.0;
            
            for(int k=0;k<this.ruleStrings.size();k++){
                if(!missingView.contains(k)){
                    elemSize*=Supports.get(k).size();
                    numExamples*=dats.get(0).numExamples;
                }
            }
            
      double prob=elemSize/numExamples;
            BinomialDistribution dist=new BinomialDistribution(dats.get(0).numExamples,prob);
          double  p=1.0-dist.cumulativeProbability(intersection.size());
      
          if(j>=minJ && intersection.size()>=minSupp)
                              tmp = new Redescription(this);
          else return null;
          
          for(int i=0;i<tmp.ruleStrings.size();i++)
              if(tmp.ruleStrings.get(i) == null && R2.ruleStrings.get(i)!=null)
                  tmp.ruleStrings.set(i, R2.ruleStrings.get(i));
                
      tmp.elements = new TIntHashSet();
      tmp.elementsUnion = new TIntHashSet();
      
       tmp.JS = (double)intersection.size()/(double)union.size();
       tmp.elements.addAll(intersection);
       tmp.elementsUnion.addAll(union);
      
      tmp.JS = j;
      tmp.pVal = p;
        }
        
        return tmp;
    }
    
    int checkAttributes(ApplicationSettings appset, Mappings map, DataSetCreator dat){//wrong check
        
       int contains=0;
       TIntHashSet attrs=this.computeAttributesRedSet(viewElementsLists, dat);
       int constraint_size = appset.importantAttributes.get(0).size();
      
       //System.out.println(this.ruleStrings.get(0));
      // System.out.println(this.ruleStrings.get(1));
       
       int s1=0;
       int both1=0;
     for(int i=0;i<constraint_size;i++){  //appset.viewInputPaths.size()
       /*ArrayList<ArrayList<String>> constraints = appset.importantAttributes.get(i);
        
        if(constraints.size()==0)
            continue;*/
        both1=0;
        TIntIterator it = attrs.iterator();
       //System.out.println("inputPaths size: "+appset.viewInputPaths.size());
        for(int w=0;w<appset.viewInputPaths.size();w++){
            ArrayList<ArrayList<String>> constraints = appset.importantAttributes.get(w);
          //    System.out.println("AI: "+appset.attributeImportanceGen.get(w));
           //   System.out.println("CS: "+constraints.size());
       if(appset.attributeImportanceGen.get(w)==2){ 
       // for(int j=0;j<constraints.size();j++){
            s1=1;
            for(int k=0;k<constraints.get(i).size();k++){
                if(constraints.get(i).get(k).equals(""))
                    continue;
                if(!attrs.contains(map.attId.get(constraints.get(i).get(k)))){
                    s1=0;
                    break;
                } 
            }
            /*if(s1==1)
                break;*/
        
        /*if(s1==1)
            continue;
        else return 0;*/
        //System.out.println("View: "+(w+1)+" , s1: "+s1);
       if(s1==0)
           break;
       if(s1==1)
           both1++;
       }
       else if(appset.attributeImportanceGen.get(w)==1){
           s1=0;
           // for(int j=0;j<constraints.size();j++){
                for(int k=0;k<constraints.get(i).size();k++){
                    if(constraints.get(i).get(k).equals("")){
                       // System.out.println("Contains empty condition, OK!");
                        s1=1;
                          break;
                    }
                    //System.out.println("Before checking: "+constraints.get(i).get(k));
                    if(attrs.contains(map.attId.get(constraints.get(i).get(k)))){
                      //  System.out.println("Not contains attribute: "+constraints.get(i).get(k));
                        s1=1;
                        break;
                 }
                }
               /* if(s1==1)
                    break;
        // }
            if(s1==1)
                    continue;
            else
                return 0;*/
               //System.out.println("View: "+(w+1)+" , s1: "+s1);
                if(s1==0)
                    break;
                if(s1==1)
                    both1++;
       }
       }
        //System.out.print(both1+" ");
        if(both1==appset.viewInputPaths.size())
            return 1;
     }
    // System.out.println("\n out: "+both1+" ");
     if(both1==appset.viewInputPaths.size())
       return 1;
     else return 0;
    }
    
    Redescription(String LR, String RR, DataSetCreator dat){
        ruleStrings=new ArrayList<>();
        ruleStrings.add(LR); ruleStrings.add(RR);
        elements=new TIntHashSet(dat.numExamples);
         viewElementsLists=new ArrayList<>();
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>());
        elementsUnion=new TIntHashSet(dat.numExamples);
        Conjunction left=new Conjunction(dat);
        Conjunction right=new Conjunction(dat);
        viewElementsLists.get(0).add(left);
        viewElementsLists.get(1).add(right);
        supportsSides=new ArrayList<Integer>();
    }
    
    Redescription(String LR, String RR, double sim, DataSetCreator dat){
        ruleStrings=new ArrayList<>();
        ruleStrings.add(LR); ruleStrings.add(RR);
        elements=new TIntHashSet(dat.numExamples);
         viewElementsLists=new ArrayList<>();
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>());
        elementsUnion=new TIntHashSet(dat.numExamples);
        Conjunction left=new Conjunction(dat);
        Conjunction right=new Conjunction(dat);
        viewElementsLists.get(0).add(left);
        viewElementsLists.get(1).add(right);
        supportsSides=new ArrayList<Integer>();
        JS=sim;
    }
    
    Redescription(Redescription R, DataSetCreator dat){
        ruleStrings=new ArrayList<>();
        elements=new TIntHashSet(dat.numExamples);
        viewElementsLists=new ArrayList<>();
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>());
        elementsUnion=new TIntHashSet(dat.numExamples);
        supportsSides=new ArrayList<Integer>();
        TIntIterator iterator = R.elements.iterator();
        
        while(iterator.hasNext()){
            elements.add(iterator.next());
        }
       
       for(int k=0;k<viewElementsLists.size();k++){ 
           ArrayList<Conjunction> RE=R.viewElementsLists.get(k);
           for(int i=0;i<RE.size();i++){
            Conjunction c=new Conjunction(RE.get(i));
            viewElementsLists.get(k).add(c);
        }
      }
      
         iterator=R.elementsUnion.iterator();
        
         while(iterator.hasNext()){
            elementsUnion.add(iterator.next());
        }
         
        JS=R.JS;
        
        for(int i=0;i<R.supportsSides.size();i++)
            supportsSides.add(R.supportsSides.get(i));
    }
    
     Redescription(String LR, String RR, double sim, Mappings map, DataSetCreator dat){
        ruleStrings=new ArrayList<>();
        ruleStrings.add(LR); ruleStrings.add(RR);
        elements=new TIntHashSet(dat.numExamples);
        viewElementsLists=new ArrayList<>();
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>());
        elementsUnion=new TIntHashSet(dat.numExamples);
        JS=sim;

        Conjunction cleft=new Conjunction(LR,dat,map);
        Conjunction cright=new Conjunction(RR,dat,map);
        
        viewElementsLists.get(0).add(cleft);
        viewElementsLists.get(1).add(cright);
        supportsSides=new ArrayList<Integer>();
    }
    
     Redescription(String LR, String RR, double sim, Mappings map, DataSetCreator dat, int operator){
         ruleStrings=new ArrayList<>(2*dat.W2indexs.size());
        ruleStrings.add(LR); ruleStrings.add(RR);
        elements=new TIntHashSet(dat.numExamples);
        viewElementsLists=new ArrayList<>(2*dat.W2indexs.size());
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>(5));
        elementsUnion=new TIntHashSet(dat.numExamples);
        JS=sim;
        
        Conjunction cleft=new Conjunction(LR,dat,map);
        Conjunction cright=new Conjunction(RR,dat,map);
        
        if(operator==0){  
            viewElementsLists.get(0).add(cleft);
            viewElementsLists.get(1).add(cright);
        }
        else if(operator==1){
           cright.isNegated=true; 
           viewElementsLists.get(0).add(cleft);
           viewElementsLists.get(1).add(cright);
        }
        else{
            cleft.isNegated=true;
            viewElementsLists.get(0).add(cleft);
            viewElementsLists.get(1).add(cright);
        }
        supportsSides=new ArrayList<Integer>();
    }
     
     Redescription(Redescription R, String RR, double sim, Mappings map, DataSetCreator dat, int operator, int view){
         ruleStrings=new ArrayList<>();
         for(int i=0;i<R.ruleStrings.size();i++)
             ruleStrings.add(R.ruleStrings.get(i));
        ruleStrings.add(RR);
        elements=new TIntHashSet(dat.numExamples);
        viewElementsLists=new ArrayList<>();
        for(int i=0;i<dat.W2indexs.size()+1;i++)
            viewElementsLists.add(new ArrayList<Conjunction>());
        elementsUnion=new TIntHashSet(dat.numExamples);
        JS=sim;
        
        //Conjunction cleft=new Conjunction(LR,dat,map);
        Conjunction cright=new Conjunction(RR,dat,map);
        
        if(operator==0){  
            //viewElementsLists.get(0).add(cleft);
            for(int i=0;i<view;i++){
                for(int j=0;j<R.viewElementsLists.get(i).size();j++){
                    Conjunction ct=new Conjunction(R.viewElementsLists.get(i).get(j));
                    viewElementsLists.get(i).add(ct); 
                }
            }
            viewElementsLists.get(view).add(cright);
           for(int i=view+1;i<R.viewElementsLists.size();i++){
                for(int j=0;j<R.viewElementsLists.get(i).size();j++){
                    Conjunction ct=new Conjunction(R.viewElementsLists.get(i).get(j));
                    viewElementsLists.get(i).add(ct); 
                }
            }
        }
        else if(operator==1){
           cright.isNegated=true; 
           //viewElementsLists.get(0).add(cleft);
           for(int i=0;i<view;i++){
               for(int j=0;j<R.viewElementsLists.get(i).size();j++){
                   Conjunction ct=new Conjunction(R.viewElementsLists.get(i).get(j));
                    viewElementsLists.get(i).add(ct); 
                //viewElementsLists.get(i).addAll(R.viewElementsLists.get(i));
               }
           }
           viewElementsLists.get(view).add(cright);
            for(int i=view+1;i<R.viewElementsLists.size();i++){
               for(int j=0;j<R.viewElementsLists.get(i).size();j++){
                   Conjunction ct=new Conjunction(R.viewElementsLists.get(i).get(j));
                    viewElementsLists.get(i).add(ct); 
                //viewElementsLists.get(i).addAll(R.viewElementsLists.get(i));
               }
           }
        }
        supportsSides=new ArrayList<Integer>();
    }
     
     void removeRedundant(){
         
         int sEone=1;
         
         for(int i=0;i<viewElementsLists.size();i++)
             if(!(viewElementsLists.get(i).size()==1))
                 return;
         
         Conjunction c;
         ArrayList<HashSet<Conjunction>> toRemove=new ArrayList<>();//HashSet();
         for(int i=0;i<viewElementsLists.size();i++)
             toRemove.add(new HashSet<Conjunction>());
         
         for(int i=0;i<viewElementsLists.size();i++){
             if(viewElementsLists.get(i).size()>1){
                 for(int j=0;j<viewElementsLists.get(i).size();j++){
                     c=viewElementsLists.get(i).get(j);
                        for(int k=j+1;k<viewElementsLists.get(i).size();k++){
                             if(j!=i){
                                 if(c.dominates(viewElementsLists.get(i).get(k))==1){
                                     toRemove.get(i).add(viewElementsLists.get(i).get(k));
                                     System.out.println("Removing elementary disjunction");
                                    }
                                 else if(viewElementsLists.get(i).get(k).dominates(c)==1){
                                         toRemove.get(i).add(c);
                                         System.out.println("Removing elementary disjunction");
                                     }
                                 }
                         }
                     }
             }
         }
            
         for(int i=0;i<toRemove.size();i++){
             HashSet<Conjunction> side=toRemove.get(i);
            for(Conjunction conj:side)
                 toRemove.get(i).remove(conj);
                }

            toRemove.clear();
     }
     
     
     void disjunctiveJoin(Rule r, ApplicationSettings appset ,DataSetCreator dat, Mappings map,ArrayList<TIntHashSet> SideElems, int side, int negate){//0 left, 1 right  //fix memory!
         
         if(r.elements.size()<appset.minSupport || r.elements.size()>appset.maxSupport)
             return;
         
         Conjunction conj=new Conjunction(r.rule,dat,map);
         if(negate==1)
             conj.isNegated=true;
         viewElementsLists.get(side).add(conj);

         int countMax=0, countUMax=0;
         if(negate==0){
             TIntIterator iterator = r.elements.iterator();
             
             while(iterator.hasNext()){
                  int elem=iterator.next();
                  viewElementsLists.get(side).get(viewElementsLists.get(side).size()-1).elements.add(elem);
            }
             iterator = r.elements.iterator();
        
        /*System.out.println("elements size before: "+elements.size());
        System.out.println("element union size before: "+elementsUnion.size());
        System.out.println("Jaccard similarity before: "+JS);*/
             
        while(iterator.hasNext()){
            int elem=iterator.next();
            int contained=1;
            
            for(int i=0;i<SideElems.size();i++)
                if(i!=side){
                    if(!SideElems.get(i).contains(elem)){
                        contained=0;
                        break;
                    }
                }
            
                if(contained==1){
                     if(!elements.contains(elem)){
                        countMax++;
                    elements.add(elem);   
                     }
                }
                if(!elementsUnion.contains(elem)){
                countUMax++;
            elementsUnion.add(elem);
                }
        }
        /*System.out.println("New elements obtained: "+countMax);
        System.out.println("New elements corrupted: "+countUMax);
        System.out.println("elements size after: "+elements.size());
        System.out.println("element union size after: "+elementsUnion.size());*/
      }
       
         else if(negate==1){
               TIntHashSet elems=null;

           for(int i=0;i<dat.numExamples;i++){
               int contained=0;
            if(!r.elements.contains(i)){
                         TIntIterator it=r.ruleAtts.iterator();
                         while(it.hasNext()){
                             int at=it.next();
                             if(!map.catAttInd.contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                         }
                    }
            if(contained==1){
                
                int contained1=1;
            
            for(int i1=0;i1<SideElems.size();i1++)
                if(i1!=side){
                    if(!SideElems.get(i1).contains(i)){
                        contained1=0;
                        break;
                    }
                }
            
                if(contained1==1){
                    elements.add(i);          
                }

               elementsUnion.add(i);
            }
           }
           
               TIntIterator iterator = r.elements.iterator();
        
               while(iterator.hasNext()){
                   int elem=iterator.next();
                   viewElementsLists.get(side).get(viewElementsLists.get(side).size()-1).elements.add(elem);    
                 }
         }
       
         supportsSides.clear();
         for(int i=0;i<viewElementsLists.size();i++){
             supportsSides.add(this.computeElements(viewElementsLists.get(i), dat, map).size());
         }
     
        double JSOld=JS;
         JS=(double)elements.size()/elementsUnion.size();

        if(JSOld>JS){
            System.out.println("JS wrong!: ");
            System.out.println("JSOld: "+JSOld);
            System.out.println("JSnew: "+JS);
            System.out.println("Negated: "+negate);
        }
     }
     
     
     
     void disjunctiveJoinPess(Rule r, ApplicationSettings appset ,DataSetCreator dat, Mappings map,ArrayList<TIntHashSet> SideElems, int side, int negate){//0 left, 1 right  //fix memory!
         
         if(r.elements.size()<appset.minSupport || r.elements.size()>appset.maxSupport)
             return;
         
         Conjunction conj=new Conjunction(r.rule,dat,map);
         if(negate==1)
             conj.isNegated=true;
         viewElementsLists.get(side).add(conj);

         int countMax=0, countUMax=0;
         if(negate==0){
             TIntIterator iterator = r.elements.iterator();
             
             while(iterator.hasNext()){
                  int elem=iterator.next();
                  viewElementsLists.get(side).get(viewElementsLists.get(side).size()-1).elements.add(elem);
            }
             iterator = r.elements.iterator();
        
        /*System.out.println("elements size before: "+elements.size());
        System.out.println("element union size before: "+elementsUnion.size());
        System.out.println("Jaccard similarity before: "+JS);*/
             
        while(iterator.hasNext()){
            int elem=iterator.next();
            int contained=1;
            
            for(int i=0;i<SideElems.size();i++)
                if(i!=side){
                    if(!SideElems.get(i).contains(elem)){
                        contained=0;
                        break;
                    }
                }
            
                if(contained==1){
                     if(!elements.contains(elem)){
                        countMax++;
                    elements.add(elem);   
                     }
                }
                if(!elementsUnion.contains(elem)){
                countUMax++;
            elementsUnion.add(elem);
                }
        }
        /*System.out.println("New elements obtained: "+countMax);
        System.out.println("New elements corrupted: "+countUMax);
        System.out.println("elements size after: "+elements.size());
        System.out.println("element union size after: "+elementsUnion.size());*/
      }
       
         else if(negate==1){
               TIntHashSet elems=null;

           for(int i=0;i<dat.numExamples;i++){
               int contained=0;
            if(!r.elements.contains(i)){
                         TIntIterator it=r.ruleAtts.iterator();
                         while(it.hasNext()){
                             int at=it.next();
                             if(!map.catAttInd.contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                         }
                    }
            if(contained==1){
                
                int contained1=1;
            
            for(int i1=0;i1<SideElems.size();i1++)
                if(i1!=side){
                    if(!SideElems.get(i1).contains(i)){
                        contained1=0;
                        break;
                    }
                }
            
                if(contained1==1){
                    elements.add(i);          
                }

               elementsUnion.add(i);
            }
           }
           
               TIntIterator iterator = r.elements.iterator();
        
               while(iterator.hasNext()){
                   int elem=iterator.next();
                   viewElementsLists.get(side).get(viewElementsLists.get(side).size()-1).elements.add(elem);    
                 }
         }
       
         for(int i=0;i<viewElementsLists.size();i++){
             supportsSides.add(this.computeElements(viewElementsLists.get(i), dat, map).size());
         }
     
         TIntHashSet missings=new TIntHashSet();
         
         for(int i=0;i<dat.numExamples;i++){
             Iterator<Integer> itL=r.ruleMap.keySet().iterator();
             int contained=1, missing=0, fnc=1;
             
            // System.out.println("left rule size: "+r1.ruleMap.keySet().size());
            // System.out.println("right rule size: "+r2.ruleMap.keySet().size());
            
        if(negate==0){  
            // System.out.println("Missing for left rule: ");
             while(itL.hasNext()){
                 int attr=itL.next();
                 
                 ArrayList<Double> attrVal=r.ruleMap.get(attr);
               //  System.out.println("attrVal size: "+attrVal);
                 /*System.out.println("Attribute: "+map.idAtt.get(attr));
                 System.out.println("view: "+k);
                 System.out.println("conjunction: "+c);
                 System.out.println("elements size: "+conj.elements.size());
                 System.out.println("rule attr size: "+conj.Rule.keySet().size());
                 System.out.println("attrs size: "+conj.attributes.size());*/
                 
               if(!map.cattAtt.containsKey(attr)){
                   double val=dat.getValue(attr, i);
                 if(val>=attrVal.get(1) && val<=attrVal.get(3))
                        continue;
                 else if(val==Double.POSITIVE_INFINITY){
                     missing=1;
                     contained=0;
                     //break;
                 }    
                 else{
                    contained=0;
                    fnc=0;
                    break;
                 }
               }
               else{
                   String cat=map.cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(0));
                   String realCat=dat.getValueCategorical(attr, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else if(!(map.cattAtt.get(attr).getValue0().keySet().contains(cat))){
                       missing=1;
                       //contained=0;
                       //break;
                   }
                   else{
                       contained=0;
                       break;
                   }
               }
             }
        }
        else{
             contained=0;
                    if(!r.elements.contains(i)){
                        TIntIterator it=r.ruleAtts.iterator();//conj.attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.cattAtt.keySet().contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                    fnc=0;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    fnc=0;
                                    break;
                                }
                            }
                        }
                        if(contained==0){
                            missing=1;
                            //fnc=0;
                        }
                    }      
        }
		
		if(missing==1 && fnc==1)
                missings.add(i);		 
				 }
         
         TIntIterator it1=missings.iterator();
         
         while(it1.hasNext())
             elementsUnion.add(it1.next());
         
        double JSOld=JS;
         JS=(double)elements.size()/elementsUnion.size();
        if(JSOld>JS){
            System.out.println("JS wrong!: ");
            System.out.println("JSOld: "+JSOld);
            System.out.println("JSnew: "+JS);
            System.out.println("Negated: "+negate);
        }
     }
     
       TIntHashSet viewsUsed(){
         
         TIntHashSet tmp = new TIntHashSet();
         
         for(int i=0;i<viewElementsLists.size();i++)
             if(viewElementsLists.get(i).size()>0)
                 tmp.add(i);
         
         return tmp;
         
     }
     
     void createRuleString(Mappings map){
         ruleStrings.clear();
         int orEncountered=0;
         
         for(int i=0;i<viewElementsLists.size();i++)
             if(viewElementsLists.get(i).size()==0){
                 System.out.println("This redescription contains zero side, CRS!");
                 break;
             }
    
   for(int sid=0;sid<viewElementsLists.size();sid++){
       ArrayList<Conjunction> side=viewElementsLists.get(sid);
       String RSTemp="";
     for(int i=0;i<side.size();i++){
         Conjunction conj=side.get(i);
         TIntIterator it=conj.Rule.keySet().iterator();
         //System.out.println("CRS conj Rule size: "+conj.Rule.keySet().size());
         if(conj.Rule.size()==0)
             System.out.println("This rule contains empty conjunction");
         if(orEncountered==1){
             RSTemp+="( ";
         }
         
         if(conj.isNegated==true){
             RSTemp+="NOT ";
             RSTemp+="( "; 
         }
         
         while(it.hasNext()){
             Integer at=it.next();
             RSTemp+=map.idAtt.get(at)+" ";
             ArrayList<Double> val=conj.Rule.get(at);
             if(val==null){
                 System.out.println("null rule set for attribute: "+map.idAtt.get(at));
             }
             if(!map.cattAtt.containsKey(at)){
                if(val.get(0)==1){
                     RSTemp+=">= ";
                     RSTemp+=val.get(1)+" ";
                }
                if(val.get(2)==1){
                     RSTemp+="<= ";
                     RSTemp+=val.get(3)+" ";
                }
               }
             else{
                if(!conj.categoricalDisjunction){
                RSTemp+="= ";  
                RSTemp+=map.cattAtt.get(at).getValue1().get((int)(double)val.get(0))+" ";
                 }
                 else{
                     RSTemp+="in {";
                     for(int zz=0;zz<val.size();zz++){
                         if(zz<val.size()-1)
                         RSTemp+=map.cattAtt.get(at).getValue1().get((int)(double)val.get(zz))+",";
                         else RSTemp+=map.cattAtt.get(at).getValue1().get((int)(double)val.get(zz));
                     }
                     RSTemp+="} ";
                 }
             }
             
             if(it.hasNext())
                    RSTemp+="AND ";
         }
         
         if(conj.isNegated==true){
             RSTemp+=" ) "; 
         }
         
         if(orEncountered==1){
             RSTemp+=" )";
             orEncountered=0;
         }
         
         if((i+1)<side.size()){
             RSTemp+="OR ";
             orEncountered=1;
         }
     }
     ruleStrings.add(RSTemp);
   }
  }
     
    void computeElements(Rule r1, Rule r2){

        supportsSides.add(r1.elements.size());
        supportsSides.add(r2.elements.size());
      
        TIntIterator iterator = r1.elements.iterator();
        TIntIterator iterator2;
        
                   while (iterator.hasNext()) {
                        int elem = iterator.next();
                        if(r2.elements.contains(elem))
                            elements.add(elem);                   
                    }
        
        iterator = r1.elements.iterator();
        
        while(iterator.hasNext()){
            int elem=iterator.next();
            viewElementsLists.get(0).get(0).elements.add(elem);
        }

        iterator2 = r2.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(1).get(0).elements.add(elem);
        }
    }
    
     void computeElements(Redescription R, Rule r2, int view){

         for(int i=0;i<R.supportsSides.size();i++)
             supportsSides.add(R.supportsSides.get(i));
         
        supportsSides.add(r2.elements.size());
      
        TIntIterator iterator = R.elements.iterator();
        TIntIterator iterator2;
        
                   while (iterator.hasNext()) {
                        int elem = iterator.next();
                        if(r2.elements.contains(elem))
                            elements.add(elem);                   
                    }

        iterator2 = r2.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(view).get(0).elements.add(elem);
        }
    }
    
    void computeElements(Rule r1, Rule r2, Rule r3, Rule r4){

        TIntIterator itleft= r3.ruleAtts.iterator();
       TIntIterator itright= r4.ruleAtts.iterator();
        
       supportsSides.add(r1.elements.size());
       supportsSides.add(r2.elements.size());
      
        TIntIterator iterator = r1.elements.iterator();
        TIntIterator iterator2;
        
                   while (iterator.hasNext()) {
                        int elem = iterator.next();
                        if(r2.elements.contains(elem))
                            elements.add(elem);                    
                    }
        
        iterator = r3.elements.iterator();
        
        while(iterator.hasNext()){
            int elem=iterator.next();
            
            viewElementsLists.get(0).get(0).elements.add(elem);
        }

        iterator2 = r4.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(1).get(0).elements.add(elem);
        }
    }
    
    void computeElements(Rule r1, Rule r2,DataSetCreator dat, Mappings map ,int mode){
   
      if(mode==0){
        supportsSides.add(r1.elements.size());
        supportsSides.add(r2.elements.size());
      
        TIntIterator iterator = r1.elements.iterator();
        TIntIterator iterator2;
        
                   while (iterator.hasNext()) {
                        int elem = iterator.next();
                        if(r2.elements.contains(elem))
                            elements.add(elem);                    
                    }
        
        iterator = r1.elements.iterator();
        
        while(iterator.hasNext()){
            int elem=iterator.next();
            viewElementsLists.get(0).get(0).elements.add(elem);
        }

        iterator2 = r2.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(1).get(0).elements.add(elem);
        }
    }
      else if(mode==1){
          int count=0;
          supportsSides.add(r1.elements.size());
      
        TIntIterator iterator = r1.elements.iterator();
        TIntIterator iterator2;

           for(int i=0;i<dat.numExamples;i++){
               int contained=0;
            if(!r2.elements.contains(i)){
                         TIntIterator it=r2.ruleAtts.iterator();
                         while(it.hasNext()){
                             int at=it.next();
                             if(!map.cattAtt.keySet().contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                    break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                         }
                    }
            if(contained==1){
                if(r1.elements.contains(i)){
                    elements.add(i);
                }
                count++;
            }
           }
           
           supportsSides.add(count);
           
           viewElementsLists.get(1).get(0).negate();
        
        iterator = r1.elements.iterator();
        
        while(iterator.hasNext()){
            int elem=iterator.next();
            viewElementsLists.get(0).get(0).elements.add(elem);
        }

        iterator2 = r2.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(1).get(0).elements.add(elem);
        }
      }
      else if(mode==2){
           int count=0;
          TIntIterator iterator;
          TIntIterator iterator2 = r2.elements.iterator();
           
           for(int i=0;i<dat.numExamples;i++){
               int contained=0;
            if(!r1.elements.contains(i)){
                         TIntIterator it=r1.ruleAtts.iterator();
                         while(it.hasNext()){
                             int at=it.next();
                             if(!map.cattAtt.keySet().contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                      contained=1;
                                      break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                         }
                    }
            if(contained==1){
                count++;
                if(r2.elements.contains(i))
                    elements.add(i);
             }
           }
           
           supportsSides.add(count);
           viewElementsLists.get(0).get(0).negate();

           supportsSides.add(r2.elements.size());
           
           iterator = r1.elements.iterator();
        
        while(iterator.hasNext()){
            int elem=iterator.next();
            viewElementsLists.get(0).get(0).elements.add(elem);
        }

        iterator2 = r2.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(1).get(0).elements.add(elem);
        }
      }
      
     /* System.out.println("Check in redescription...");
      System.out.println("First try...");
      this.closeInterval(dat, map);
      //this.minimizeOptimal(dat, map,1);
      this.validate(dat, map);
      this.clearRuleMaps();
      System.out.println("Second try...");
      this.closeInterval(dat, map);
      this.minimizeOptimal(dat, map,1);
      this.validate(dat, map);
      this.clearRuleMaps();*/
      
  }  
    
     void computeElements(Redescription R, Rule r2,DataSetCreator dat, Mappings map ,int mode, int view){
   
          for(int i=0;i<R.supportsSides.size();i++)
             supportsSides.add(R.supportsSides.get(i));
         
      if(mode==0){
        supportsSides.add(r2.elements.size());
      
        TIntIterator iterator = R.elements.iterator();
        TIntIterator iterator2;
        
                   while (iterator.hasNext()) {
                        int elem = iterator.next();
                        if(r2.elements.contains(elem))
                            elements.add(elem);                    
                    }

        iterator2 = r2.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(view).get(0).elements.add(elem);
        }
    }
      else if(mode==1){
          int count=0;
      
        TIntIterator iterator = R.elements.iterator();
        TIntIterator iterator2;

           for(int i=0;i<dat.numExamples;i++){
               int contained=0;
            if(!r2.elements.contains(i)){
                         TIntIterator it=r2.ruleAtts.iterator();
                         while(it.hasNext()){
                             int at=it.next();
                             if(!map.cattAtt.keySet().contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                    break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                         }
                    }
            if(contained==1){
                if(R.elements.contains(i)){
                    elements.add(i);
                }
                count++;
            }
           }
           
           supportsSides.add(count);
           
           viewElementsLists.get(view).get(0).negate();

        iterator2 = r2.elements.iterator();
        
        while(iterator2.hasNext()){
            int elem=iterator2.next();
            viewElementsLists.get(view).get(0).elements.add(elem);
        }
      }
  }
    
    void computeUnion(Rule r1, Rule r2){
        
        TIntIterator iterator = r1.elements.iterator();
        TIntIterator iterator2 = r2.elements.iterator();
        
         while (iterator.hasNext()) {
                        int elem = iterator.next();
                        elementsUnion.add(elem);
         }

          while (iterator2.hasNext()) {
                        int elem = iterator2.next();
                        elementsUnion.add(elem);
         }
    }
    
     void computeUnion(Rule r1, Rule r2, DataSetCreator dat, Mappings map, int mode){
        
        TIntIterator iterator = r1.elements.iterator();
        TIntIterator iterator2 = r2.elements.iterator();
        
         if(mode==0){
         while (iterator.hasNext()) {
                        int elem = iterator.next();
                        elementsUnion.add(elem);
         }

          while (iterator2.hasNext()) {
                        int elem = iterator2.next();
                        elementsUnion.add(elem);
         }
    }
         else if(mode==1){
           for(int i=0;i<dat.numExamples;i++){
               int contained=0;
            if(!r2.elements.contains(i)){
                         TIntIterator it=r2.ruleAtts.iterator();
                         while(it.hasNext()){
                             int at=it.next();
                             if(!map.cattAtt.keySet().contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                         }
                    }
            if(contained==1){
                elementsUnion.add(i);
            }
           }
             
            while (iterator.hasNext()) {
                        int elem = iterator.next();
                        elementsUnion.add(elem);
          } 
         }
         else if(mode==2){
              for(int i=0;i<dat.numExamples;i++){
               int contained=0;
            if(!r1.elements.contains(i)){
                         TIntIterator it=r1.ruleAtts.iterator();
                         while(it.hasNext()){
                             int at=it.next();
                             if(!map.cattAtt.keySet().contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                         }
                    }
            if(contained==1)
                elementsUnion.add(i);
           }
           iterator2 = r2.elements.iterator();
           while(iterator2.hasNext()){
               int elem=iterator2.next();
               elementsUnion.add(elem);
           }   
       }
  }
     
     void computeUnion(Redescription R, Rule r2, DataSetCreator dat, Mappings map, int mode){
        
        TIntIterator iterator = R.elementsUnion.iterator();
        TIntIterator iterator2 = r2.elements.iterator();
        
         if(mode==0){
         while (iterator.hasNext()) {
                        int elem = iterator.next();
                        elementsUnion.add(elem);
         }

          while (iterator2.hasNext()) {
                        int elem = iterator2.next();
                        elementsUnion.add(elem);
         }
    }
         else if(mode==1){
           for(int i=0;i<dat.numExamples;i++){
               int contained=0;
            if(!r2.elements.contains(i)){
                         TIntIterator it=r2.ruleAtts.iterator();
                         while(it.hasNext()){
                             int at=it.next();
                             if(!map.cattAtt.keySet().contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                         }
                    }
            if(contained==1){
                elementsUnion.add(i);
            }
           }
             
            while (iterator.hasNext()) {
                        int elem = iterator.next();
                        elementsUnion.add(elem);
          } 
         }
  }

    TIntHashSet computeElements(ArrayList<Conjunction> side, DataSetCreator dat, Mappings map){
        TIntHashSet elem=new TIntHashSet(2*dat.numExamples);
        for(int i=0;i<side.size();i++){
            TIntHashSet elemT=side.get(i).elements;
          
            if(side.get(i).isNegated==false){
                TIntIterator it=elemT.iterator();
            
                while(it.hasNext()){
                    elem.add(it.next());
                }
              }
            else{
                for(int el=0;el<dat.numExamples;el++){
                    if(!elemT.contains(el)){
                        TIntIterator it=side.get(i).attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.catAttInd.contains(at)){
                                if(dat.getValue(at, el)!=Double.POSITIVE_INFINITY){
                                    elem.add(el);
                                    break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, el))){
                                    elem.add(el);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
    }
            return elem;
    }
    
    
     ArrayList<TIntHashSet> computeElementsGen(DataSetCreator dat, Mappings map){
        ArrayList<TIntHashSet> elem=new ArrayList<>();//TIntHashSet(this.viewElementsLists.size()*dat.numExamples);
        
       for(int k=0;k<viewElementsLists.size();k++){ 
           TIntHashSet tmpSet=new TIntHashSet(2*dat.numExamples);
           ArrayList<Conjunction> side=viewElementsLists.get(k);
        for(int i=0;i<side.size();i++){
            TIntHashSet elemT=side.get(i).elements;
          
            if(side.get(i).isNegated==false){
                TIntIterator it=elemT.iterator();
            
                while(it.hasNext()){
                    tmpSet.add(it.next());
                }
              }
            else{
                for(int el=0;el<dat.numExamples;el++){
                    if(!elemT.contains(el)){
                        TIntIterator it=side.get(i).attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.catAttInd.contains(at)){
                                if(dat.getValue(at, el)!=Double.POSITIVE_INFINITY){
                                    tmpSet.add(el);
                                    break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, el))){
                                    tmpSet.add(el);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        elem.add(tmpSet);
    }
            return elem;
    }
     
     ArrayList<Double> computeAllJSMeasures(DataSetCreator dataset, Mappings map){
         ArrayList<Double> measures=new ArrayList<Double>();
         
         ArrayList<TIntHashSet> Supports=new ArrayList<>();// TIntHashSet(dataset.numExamples);
         ArrayList<TIntHashSet> Missings=new ArrayList<>();
         
        for(int k=0;k<viewElementsLists.size();k++){
            Supports.add(new TIntHashSet(dataset.numExamples));
            Missings.add(new TIntHashSet(dataset.numExamples));
        }
        
     for(int k=0;k<viewElementsLists.size();k++){//should be checked
       for(int c=0;c<viewElementsLists.get(k).size();c++){  
           Conjunction conj=viewElementsLists.get(k).get(c);
         for(int i=0;i<dataset.numExamples;i++){
             TIntIterator itL=conj.Rule.keySet().iterator();
             int contained=1, missing=0, fnc=1;
            
        if(conj.isNegated==false){  
             
             while(itL.hasNext()){
                 int attr=itL.next();
                 
                 ArrayList<Double> attrVal=conj.Rule.get(attr);
                 /*System.out.println("Attribute: "+map.idAtt.get(attr));
                 System.out.println("view: "+k);
                 System.out.println("conjunction: "+c);
                 System.out.println("elements size: "+conj.elements.size());
                 System.out.println("rule attr size: "+conj.Rule.keySet().size());
                 System.out.println("attrs size: "+conj.attributes.size());*/
                 
               if(!map.cattAtt.containsKey(attr)){
                   double val=dataset.getValue(attr, i);
                 if(val>=attrVal.get(1) && val<=attrVal.get(3))
                        continue;
                 else if(val==Double.POSITIVE_INFINITY){
                     missing=1;
                     contained=0;
                     //break;
                 }    
                 else{
                    contained=0;
                    fnc=0;
                    break;
                 }
               }
               else{
                   String cat=map.cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(0));
                   String realCat=dataset.getValueCategorical(attr, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else if(!(map.cattAtt.get(attr).getValue0().keySet().contains(cat))){
                       missing=1;
                       //contained=0;
                       //break;
                   }
                   else{
                       contained=0;
                       break;
                   }
               }
             }
        }
             else{
                 contained=0;
                    if(!conj.elements.contains(i)){
                        TIntIterator it=conj.attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.cattAtt.keySet().contains(at)){
                                if(dataset.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                    fnc=0;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dataset.getValueCategorical(at, i))){
                                    contained=1;
                                    fnc=0;
                                    break;
                                }
                            }
                        }
                        if(contained==0){
                            missing=1;
                            //fnc=0;
                        }
                    }      
             }

             if(missing==1 && fnc==1)
                 Missings.get(k).add(i);
        
             if(contained==0)
                 continue;
             else{
                 //lSupport.add(i);
                 Supports.get(k).add(i);
             }
         }
       }
     }
     
     TIntHashSet sup=new TIntHashSet(dataset.numExamples);
     TIntHashSet uni=new TIntHashSet(dataset.numExamples);
         
     TIntIterator it=Supports.get(0).iterator();
     
          while(it.hasNext()){
              int elem=it.next();
              int contained=1;
             for(int k=1;k<viewElementsLists.size();k++){
                if(viewElementsLists.get(k).size()>0) 
                    if(!Supports.get(k).contains(elem)){
                         contained=0;
                         break;
                     }
                 }
                 if(contained==1)
                     sup.add(elem);
            //}
         }
          
          for(int k=0;k<viewElementsLists.size();k++){
              it=Supports.get(k).iterator();
              
              while(it.hasNext()){
                  int elem=it.next();
                  uni.add(elem);
              }
          }
          
          
          double JSRP=sup.size()/((double)uni.size());
          measures.add(JSRP);
          
          TIntHashSet uniTmp=new TIntHashSet(dataset.numExamples);
          
          for(int k=0;k<viewElementsLists.size();k++){
              
              it=Supports.get(k).iterator();
              
              while(it.hasNext()){
                  int elem=it.next();
                  uniTmp.add(elem);
              } 
          }
          
           for(int k=0;k<viewElementsLists.size();k++){
              
              it=Missings.get(k).iterator();
              
              while(it.hasNext()){
                  int elem=it.next();
                  if(!sup.contains(elem))
                  uniTmp.remove(elem);
              } 
          }
          
           double JSGround=sup.size()/((double)uniTmp.size());
            this.JSGR=JSGround;
           measures.add(JSGround);
           
           uniTmp.clear();
           
           for(int k=0;k<viewElementsLists.size();k++){
              
              it=Supports.get(k).iterator();
              
              while(it.hasNext()){
                  int elem=it.next();
                  uniTmp.add(elem);
              } 
          }
          
           for(int k=0;k<viewElementsLists.size();k++){
              
              it=Missings.get(k).iterator();
              
              while(it.hasNext()){
                  int elem=it.next();
                  uniTmp.add(elem);
              } 
          }
           
           double JSPess=sup.size()/((double)uniTmp.size());
           this.JSPes=JSPess;
           measures.add(JSPess);
           
           uniTmp.clear();
           
          for(int k=0;k<viewElementsLists.size();k++){
              
              it=Supports.get(k).iterator();
              
              while(it.hasNext()){
                  int elem=it.next();
                  uniTmp.add(elem);
              } 
          }
          
           for(int k=0;k<viewElementsLists.size();k++){
              
              it=Missings.get(k).iterator();
              
              while(it.hasNext()){
                  int elem=it.next();
                  int contained=1;
                  for(int z=0;z<viewElementsLists.size();z++){
                      if(z!=k){
                          if(!Supports.get(z).contains(elem) && !Missings.get(z).contains(elem)){
                              contained=0;
                              break;
                          }
                      }
                  }
                  
                  if(contained==1){
                      sup.add(elem);
                      uniTmp.add(elem);
                  }
              } 
          }
           
           double JSOpt=sup.size()/((double)uniTmp.size());
           this.JSOpt=JSOpt;
           measures.add(JSOpt);
     
         return measures;
     }
    
   ArrayList<TIntHashSet> computeAttributes(ArrayList<ArrayList<Conjunction>> Viewside, DataSetCreator dat){
          ArrayList<TIntHashSet> elem=new ArrayList<>();//TIntHashSet(dat.schema.getNbAttributes());
         
          for(int k=0;k<Viewside.size();k++)
              elem.add(new TIntHashSet(dat.schema.getNbAttributes()));
          
         for(int k=0;k<Viewside.size();k++){ 
          for(int i=0;i<Viewside.get(k).size();i++){
              TIntIterator it=Viewside.get(k).get(i).attributes.iterator();
             
              while(it.hasNext()){
                  elem.get(k).add(it.next());
              }   
          }
        }
              
       return elem;    
   }
   
   
   TIntHashSet computeAttributesRedSet(ArrayList<ArrayList<Conjunction>> Viewside, DataSetCreator dat){
          TIntHashSet elem=new TIntHashSet(dat.schema.getNbAttributes());//TIntHashSet(dat.schema.getNbAttributes());
          
         for(int k=0;k<Viewside.size();k++){ 
          for(int i=0;i<Viewside.get(k).size();i++){
              TIntIterator it=Viewside.get(k).get(i).attributes.iterator();
             
              while(it.hasNext()){
                  elem.add(it.next());
              }   
          }
        }
              
       return elem;    
   }
   
   ArrayList<Integer> computeAttributesRed(ArrayList<ArrayList<Conjunction>> Viewside, DataSetCreator dat){
          ArrayList<Integer> elem=new ArrayList<>();//TIntHashSet(dat.schema.getNbAttributes());
          
         for(int k=0;k<Viewside.size();k++){ 
          for(int i=0;i<Viewside.get(k).size();i++){
              TIntIterator it=Viewside.get(k).get(i).attributes.iterator();
             
              while(it.hasNext()){
                  elem.add(it.next());
              }   
          }
        }
              
       return elem;    
   }
   
   int computeAttributesDuplicate(ArrayList<Conjunction> side, DataSetCreator dat){
         int elem=0;
                   
          for(int i=0;i<side.size();i++){
             elem+=side.get(i).attributes.size();
          }           
       return elem;    
   }
   
    int computeAttributesDuplicateGen(DataSetCreator dat){
         int elem=0;
         
        for(int i=0;i<viewElementsLists.size();i++)
            for(int j=0;j<viewElementsLists.get(i).size();j++)
                elem+=viewElementsLists.get(i).get(j).attributes.size();
         /* for(int i=0;i<side.size();i++){
             elem+=side.get(i).attributes.size();
          }  */         
       return elem;    
   }
    
     int computeAttributesDuplicateGenBaseline(){
         int elem=0;
        
        for(int i=0;i<ruleStrings.size();i++){
            String s = ruleStrings.get(i).replaceAll("\\(OR", "OR").trim();
            String tmp[] = s.split(" OR ");
            for(int j=0;j<tmp.length;j++){
                 tmp[j] = tmp[j].replaceAll("\\)", "");
                 tmp[j] = tmp[j].replaceAll("\\(", "");
                 tmp[j] = tmp[j].replaceAll(" NOT ", "");
                 String tmp1[] = tmp[j].split(" AND ");
                 
                elem+=tmp1.length;                
            }
        }
         
       return elem;    
   }
    
    int countDiff(Redescription R, DataSetCreator dat, Mappings map){ //compute elements over again
        int count=0;
     
      for(int i=0;i<viewElementsLists.size();i++){  
        TIntHashSet l=computeElements(this.viewElementsLists.get(i),dat,map);
        TIntHashSet lR=R.computeElements(R.viewElementsLists.get(i),dat,map);
        
        TIntIterator it = l.iterator();
        
        while(it.hasNext()){
            int s=it.next();
            if(!lR.contains(s))
               count++;
        }
      }   
     return count;
    }
    
   
      void computeUnion(TIntHashSet elements1, TIntHashSet elements2/*, HashSet<Integer> elementsU*/){

        this.elementsUnion.clear();
        
        TIntIterator it=elements1.iterator();
        
        while(it.hasNext()){
            this.elementsUnion.add(it.next());
        }

        it=elements2.iterator();
        
        while(it.hasNext()){
            this.elementsUnion.add(it.next());
        }
    }
      
       void computeUnionGen(ArrayList<TIntHashSet> elementsViews){

        this.elementsUnion.clear();
        
        for(int k=0;k<elementsViews.size();k++){
        
        TIntIterator it=elementsViews.get(k).iterator();
        
        while(it.hasNext()){
            this.elementsUnion.add(it.next());
        }
       }
    }
       
        public void updateFreqTable(int elemFreq[], int attrFreq[],ArrayList<Double> redScore, ArrayList<Double> redScoreAtr,ArrayList<Double> redDistCoverage, ArrayList<Double> redDistCoverageA,Redescription nRed, RedescriptionSet set ,DataSetCreator dat){
             TIntIterator it=elements.iterator();
           
           while(it.hasNext()){
               int el=it.next();
               if(elemFreq[el]<=0)
                   System.out.println("elemFreq<0 updateFreq");
               elemFreq[el]-=1;
           }
           
           it=nRed.elements.iterator();
           
            while(it.hasNext()){
               int el=it.next();
               elemFreq[el]+=1;
           }
           
           ArrayList<Integer> attr=this.computeAttributesRed(this.viewElementsLists, dat);
           ArrayList<Integer> attrNew=nRed.computeAttributesRed(nRed.viewElementsLists, dat);
           
           for(int i=0;i<attr.size();i++){
               if(attrFreq[attr.get(i)]<=0)
                   System.out.println("attrFreq<0 updateFreq");
               attrFreq[attr.get(i)]-=1;
           }
           
           for(int i=0;i<attrNew.size();i++)
               attrFreq[attrNew.get(i)]+=1;
       }
       
       public void updateFreqTableOld(int elemFreq[], int attrFreq[],ArrayList<Double> redScore, ArrayList<Double> redScoreAtr,Redescription nRed, RedescriptionSet set ,DataSetCreator dat){
             TIntIterator it=elements.iterator();
           
           while(it.hasNext()){
               int el=it.next();
               if(elemFreq[el]<=0)
                   System.out.println("elemFreq<0 updateFreq");
               elemFreq[el]-=1;
           }
           
           it=nRed.elements.iterator();
           
            while(it.hasNext()){
               int el=it.next();
               elemFreq[el]+=1;
           }
           
           ArrayList<Integer> attr=this.computeAttributesRed(viewElementsLists, dat);
           ArrayList<Integer> attrNew=nRed.computeAttributesRed(nRed.viewElementsLists, dat);
           
           for(int i=0;i<attr.size();i++){
               if(attrFreq[attr.get(i)]<=0)
                   System.out.println("attrFreq<0 updateFreq");
               attrFreq[attr.get(i)]-=1;
           }
           
           for(int i=0;i<attrNew.size();i++)
               attrFreq[attrNew.get(i)]+=1;
       }
       
       
       public void updateScore(int elemFreq[], int attrFreq[],ArrayList<Double> redScore, ArrayList<Double> redScoreAtr,ArrayList<Double> redDistCoverage,double Statistics[] ,Redescription nRed, RedescriptionSet set,DataSetCreator dat){
       
           Statistics[0]=1.0;
           TIntIterator it=elements.iterator();
           
           while(it.hasNext()){
               int el=it.next();
               if(elemFreq[el]<=0)
                   System.out.println("elemFreq<0 updateScore");
               elemFreq[el]-=1;
           }
           
           it=nRed.elements.iterator();
           
            while(it.hasNext()){
               int el=it.next();
               elemFreq[el]+=1;
           }
           
           ArrayList<Integer> attr=this.computeAttributesRed(this.viewElementsLists, dat);
           ArrayList<Integer> attrNew=nRed.computeAttributesRed(nRed.viewElementsLists, dat);
           
           for(int i=0;i<attr.size();i++){
               if(attrFreq[attr.get(i)]<0)
                   System.out.println("attrFreq<0 updateScore");
               if(attrFreq[attr.get(i)]==0)
                   System.out.println("attrFreq==0 updateScore");
               attrFreq[attr.get(i)]-=1;
           }
           
           for(int i=0;i<attrNew.size();i++)
               attrFreq[attrNew.get(i)]+=1;
           
           
           /*TIntHashSet attrSet=new TIntHashSet(attr);
           TIntHashSet attrSetNew=new TIntHashSet(attrNew);*/
           
           for(int i=0;i<set.redescriptions.size();i++)
               if(set.redescriptions.get(i)!=this){
                   it=set.redescriptions.get(i).elements.iterator();
                   ArrayList<Integer> redAt=set.redescriptions.get(i).computeAttributesRed(set.redescriptions.get(i).viewElementsLists, dat);
                   TIntHashSet redAtSet=new TIntHashSet(redAt);
                   double score=redScore.get(i);
                   
                    while(it.hasNext()){
                      int el=it.next();
                      if(elements.contains(el)){
                          score-=1.0;///set.redescriptions.get(i).elements.size();
                      if(elemFreq[el]==1)
                            redDistCoverage.set(i,redDistCoverage.get(i)+1);
                      }
                      if(nRed.elements.contains(el)){
                          score+=1.0;///set.redescriptions.get(i).elements.size();
                            if(elemFreq[el]==2)
                                redDistCoverage.set(i, redDistCoverage.get(i)-1);
                          }
                    }
                    
                    redScore.set(i, score);
                    double atScore=redScoreAtr.get(i);
                    
                      for(int j=0;j<attr.size();j++){
                          if(redAtSet.contains(attr.get(j)))
                                  atScore-=1.0;///4*set.redescriptions.get(i).numUsedViews();
                      }
                       
                      for(int j=0;j<attrNew.size();j++){
                        if(redAtSet.contains(attrNew.get(j)))
                              atScore+=1.0;///4*set.redescriptions.get(i).numUsedViews();
                      }
                      
                 redScoreAtr.set(i, atScore);     
               }
       }
       
       
        public void updateScoreNew(int elemFreq[], int attrFreq[],ArrayList<Double> redScore, ArrayList<Double> redScoreAtr,ArrayList<Double> redDistCoverage, ArrayList<Double> redDistCoverageAt,double Statistics[], RedescriptionSet set,DataSetCreator dat){
       
           Statistics[0]=1.0;
           TIntIterator it=elements.iterator();
           
           it=elements.iterator();
           
            while(it.hasNext()){
               int el=it.next();
               elemFreq[el]+=1;
           }
           
           ArrayList<Integer> attrNew=computeAttributesRed(viewElementsLists, dat);
           
           for(int i=0;i<attrNew.size();i++)
               attrFreq[attrNew.get(i)]+=1;
           
           
           /*TIntHashSet attrSet=new TIntHashSet(attr);
           TIntHashSet attrSetNew=new TIntHashSet(attrNew);*/
           
           for(int i=0;i<set.redescriptions.size();i++)
               if(set.redescriptions.get(i)!=this){
                   it=set.redescriptions.get(i).elements.iterator();
                   ArrayList<Integer> redAt=set.redescriptions.get(i).computeAttributesRed(set.redescriptions.get(i).viewElementsLists, dat);
                   TIntHashSet redAtSet=new TIntHashSet(redAt);
                   double score=redScore.get(i);
                   
                    while(it.hasNext()){
                      int el=it.next();
                      if(elements.contains(el))
                          score+=1.0;///set.redescriptions.get(i).elements.size();
                      if(elemFreq[el]==2)
                          redDistCoverage.set(i, redDistCoverage.get(i)-1);
                    }
                    
                    redScore.set(i, score);
                    double atScore=redScoreAtr.get(i);
                       
                      for(int j=0;j<attrNew.size();j++){
                        if(redAtSet.contains(attrNew.get(j))){
                              atScore+=1.0;///4*set.redescriptions.get(i).numUsedViews();
                        }
                      }
                      
                 redScoreAtr.set(i, atScore);     
               }
       }
        
        
         public double computeDistCoverage(int elemFreq[],int attrFreq[], DataSetCreator dat){
            double score=0.0;
             TIntIterator el=elements.iterator();
            
            while(el.hasNext()){
               int elem=el.next();
               if(elemFreq[elem]==1)
               score+=1;
           }
           
           return score;   
        }
         
         
         void computeUnionPess(Rule r1, Rule r2, DataSetCreator dat, Mappings map, int mode){
        
        TIntIterator iterator = r1.elements.iterator();
        TIntIterator iterator2 = r2.elements.iterator();
        
         while (iterator.hasNext()) {
                        int elem = iterator.next();
                        elementsUnion.add(elem);
         }

          while (iterator2.hasNext()) {
                        int elem = iterator2.next();
                        elementsUnion.add(elem);
         }
          
          ArrayList<TIntHashSet> Missings=new ArrayList<>();

            Missings.add(new TIntHashSet(dat.numExamples));
            Missings.add(new TIntHashSet(dat.numExamples));

         for(int i=0;i<dat.numExamples;i++){
             Iterator<Integer> itL=r1.ruleMap.keySet().iterator();
             Iterator<Integer> itR=r2.ruleMap.keySet().iterator();
             int contained=1, contained1=1, missing=0, fnc=1, missing1=0, fnc1=1;
            
        if(mode==0 || mode==1){  

             while(itL.hasNext()){
                 int attr=itL.next();
                 
                 ArrayList<Double> attrVal=r1.ruleMap.get(attr);
                 
               if(!map.cattAtt.containsKey(attr)){
                   double val=dat.getValue(attr, i);
                 if(val>=attrVal.get(1) && val<=attrVal.get(3))
                        continue;
                 else if(val==Double.POSITIVE_INFINITY){
                     missing=1;
                     contained=0;
                     //break;
                 }    
                 else{
                    contained=0;
                    fnc=0;
                    break;
                 }
               }
               else{
                   String cat=map.cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(0));
                   String realCat=dat.getValueCategorical(attr, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else if(!(map.cattAtt.get(attr).getValue0().keySet().contains(cat))){
                       missing=1;
                   }
                   else{
                       contained=0;
                       break;
                   }
               }
             }
        }
        else{
             contained=0;
                    if(!r1.elements.contains(i)){
                        TIntIterator it=r1.ruleAtts.iterator();//conj.attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.cattAtt.keySet().contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                    fnc=0;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained=1;
                                    fnc=0;
                                    break;
                                }
                            }
                        }
                        if(contained==0){
                            missing=1;
                        }
                    }      
        }
             
            if(mode==0 || mode==2){ 
                
             while(itR.hasNext()){
                 int attr=itR.next();
                 
                 ArrayList<Double> attrVal=r2.ruleMap.get(attr);
                 
               if(!map.cattAtt.containsKey(attr)){
                   double val=dat.getValue(attr, i);
                 if(val>=attrVal.get(1) && val<=attrVal.get(3))
                        continue;
                 else if(val==Double.POSITIVE_INFINITY){
                     missing1=1;
                     contained1=0;
                 }    
                 else{
                    contained1=0;
                    fnc1=0;
                    break;
                 }
               }
               else{
                   String cat=map.cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(0));
                   String realCat=dat.getValueCategorical(attr, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else if(!(map.cattAtt.get(attr).getValue0().keySet().contains(cat))){
                       missing1=1;
                   }
                   else{
                       contained1=0;
                       break;
                   }
               }
             }
            }
            else{
                 contained1=0;
                    if(!r2.elements.contains(i)){
                        TIntIterator it=r2.ruleAtts.iterator();//conj.attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.cattAtt.keySet().contains(at)){
                                if(dat.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained1=1;
                                    fnc1=0;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, i))){
                                    contained1=1;
                                    fnc1=0;
                                    break;
                                }
                            }
                        }
                        if(contained1==0){
                            missing1=1;
                        }
                    }  
            }
                        
             if(missing==1 && fnc==1)
                 Missings.get(0).add(i);

             if(missing1==1 && fnc1==1)
                 Missings.get(1).add(i);
         }
         
         TIntIterator it=Missings.get(0).iterator();
         while(it.hasNext()){
             
             elementsUnion.add(it.next());
         }
         
        it=Missings.get(1).iterator();
         while(it.hasNext()){
             
             elementsUnion.add(it.next());
         }
         
         
    }
        
        public double computeNetworkDensity(NHMCDistanceMatrix mat, ApplicationSettings appset){
            double density = 0.0, sum=0.0;
            HashSet<Integer> usedNodes = new HashSet<>();
            
            if(mat.inputType==0){ //only one network
                
                TIntIterator it = this.elements.iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                   
                    TIntIterator it1 = this.elements.iterator();

                    while(it1.hasNext()){
                        int elem1 = it1.next();
                        
                        if(usedNodes.contains(elem1))
                            continue;
                        
                        if(elem!=elem1){
                           // if(!mat.m_distancesS.containsKey(elem+"#"+elem1))
                            //   System.out.println(elem+" "+elem1);
                            //else
                            sum+=mat.m_distancesS.get(elem+"#"+elem1);
                        }
                    }
                    usedNodes.add(elem);
                }

                density = 2.0*sum/(this.elements.size()*(this.elements.size()-1));
                
            }
            else if(mat.inputType==1){//multiplex
                
                 TIntIterator it = this.elements.iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                    
                     HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn = mat.connectivityMultiplex.get(elem);
                   
                     
                      
                                Iterator<Integer> itN=conn.keySet().iterator();
                                
                                while(itN.hasNext()){
                                    int layId=itN.next();
                                    ArrayList<Triplet<Integer,Integer,Double>> edg=conn.get(layId);
                                    
                                    for(int i1=0;i1<edg.size();i1++){
                                        Triplet<Integer,Integer,Double> t=edg.get(i1);
                                        if(!this.elements.contains(t.getValue0()) || usedNodes.contains(t.getValue0()))
                                            continue;
                                        
                                        int elem2=t.getValue0();
                                        
                                        if(elem!=elem2){
                                             
                                            sum+=edg.get(i1).getValue2();//dodaj weightove
                                        }
                                    }  
                                }
                                
                                  usedNodes.add(elem);
                }

                if(mat.networkType==0)
                     density = 2.0*sum/(this.elements.size()*(this.elements.size()-1)*mat.numLayers);
                else if(mat.networkType==1)
                     density = 2.0*sum/(this.elements.size()*(this.elements.size()-1)*mat.numLayers+((this.elements.size())^mat.numLayers));
            }
            System.out.println("density: "+density);
            return density;
        }
        
         public double computeDistCoverageAt(int elemFreq[],int attrFreq[], DataSetCreator dat){
           double atrSc=0.0;
           
           ArrayList<Integer> attrs=this.computeAttributesRed(viewElementsLists, dat);
           TIntHashSet at=new TIntHashSet(attrs);
           
           TIntIterator it=at.iterator();
           
           while(it.hasNext()){
               int atId=it.next();
               int occCount=0;
               for(int k=0;k<attrs.size();k++){
                   if(atId==attrs.get(k))
                       occCount++;
               }
               
               if(attrFreq[atId]==occCount)
                   atrSc+=1;
              
           }
           
           return atrSc;
        }
       
       public void updateScoreOld(int elemFreq[], int attrFreq[],ArrayList<Double> redScore, ArrayList<Double> redScoreAtr,Redescription nRed, RedescriptionSet set ,DataSetCreator dat){
           
           TIntIterator it=elements.iterator();
           
           while(it.hasNext()){
               int el=it.next();
               if(elemFreq[el]<=0)
                   System.out.println("elemFreq<0 updateScore");
               elemFreq[el]-=1;
           }
           
           it=nRed.elements.iterator();
           
            while(it.hasNext()){
               int el=it.next();
               elemFreq[el]+=1;
           }
           
           ArrayList<Integer> attr=this.computeAttributesRed(viewElementsLists, dat);
           ArrayList<Integer> attrNew=nRed.computeAttributesRed(nRed.viewElementsLists, dat);
           
           for(int i=0;i<attr.size();i++){
               if(attrFreq[attr.get(i)]<=0)
                   System.out.println("attrFreq<0 updateScore");
               attrFreq[attr.get(i)]-=1;
           }
           
           for(int i=0;i<attrNew.size();i++)
               attrFreq[attrNew.get(i)]+=1;
           
           
           /*TIntHashSet attrSet=new TIntHashSet(attr);
           TIntHashSet attrSetNew=new TIntHashSet(attrNew);*/
           
           for(int i=0;i<set.redescriptions.size();i++)
               if(set.redescriptions.get(i)!=this){
                   it=set.redescriptions.get(i).elements.iterator();
                   ArrayList<Integer> redAt=set.redescriptions.get(i).computeAttributesRed(set.redescriptions.get(i).viewElementsLists, dat);
                   TIntHashSet redAtSet=new TIntHashSet(redAt);
                   double score=redScore.get(i);
                   
                    while(it.hasNext()){
                      int el=it.next();
                      if(elements.contains(el))
                          score-=1.0;///set.redescriptions.get(i).elements.size();
                      if(nRed.elements.contains(el))
                          score+=1.0;///set.redescriptions.get(i).elements.size();
                    }
                    
                    redScore.set(i, score);
                    double atScore=redScoreAtr.get(i);
                    
                      for(int j=0;j<attr.size();j++){
                          if(redAtSet.contains(attr.get(j)))
                                  atScore-=1.0;///4*set.redescriptions.get(i).numUsedViews();
                      }
                       
                      for(int j=0;j<attrNew.size();j++){
                        if(redAtSet.contains(attrNew.get(j)))
                              atScore+=1.0;///4*set.redescriptions.get(i).numUsedViews();
                      }
                      
                 redScoreAtr.set(i, atScore);
                    
               }
       }
       
       int computePVal(DataSetCreator dat, Mappings map){
        int numFullRed=0;

            ArrayList<TIntHashSet> elems=this.computeElementsGen(dat, map);
            
            double elemSize=1.0, numExamples=1.0;
            
            for(int k=0;k<this.viewElementsLists.size();k++){
                if(this.viewElementsLists.get(k).size()>0){
                    elemSize*=elems.get(k).size();
                    numExamples*=dat.numExamples;
                }
            }
            
            double prob=elemSize/numExamples;
            BinomialDistribution dist=new BinomialDistribution(dat.numExamples,prob);
            this.pVal=1.0-dist.cumulativeProbability(this.elements.size());
           
            if(this.JS==1.0)
                numFullRed++;
            
        return numFullRed;
    }
       
       double computePValS(DataSetCreator dat, Mappings map){
        int numFullRed=0;

            ArrayList<TIntHashSet> elems=this.computeElementsGen(dat, map);
            
            double elemSize=1.0, numExamples=1.0;
            
            for(int k=0;k<this.viewElementsLists.size();k++){
                if(this.viewElementsLists.get(k).size()>0){
                    elemSize*=elems.get(k).size();
                    numExamples*=dat.numExamples;
                }
            }
            
            double prob=elemSize/numExamples;
            BinomialDistribution dist=new BinomialDistribution(dat.numExamples,prob);
            
        return (1.0-dist.cumulativeProbability(this.elements.size()));
    }
       
        int computePValValidation(DataSetCreator dat, Mappings map){
        int numFullRed=0;

            ArrayList<TIntHashSet> elems=this.computeElementsGen(dat, map);
            
            double elemSize=1.0, numExamples=1.0;
            
            for(int k=0;k<this.viewElementsLists.size();k++){
                if(this.viewElementsLists.get(k).size()>0){
                    elemSize*=elems.get(k).size();
                    numExamples*=dat.numExamples;
                }
            }
            
            double prob=elemSize/numExamples;
            BinomialDistribution dist=new BinomialDistribution(dat.numExamples,prob);
            this.pValValidation=1.0-dist.cumulativeProbability(this.elements.size());
           
            if(this.JSAll==1.0)
                numFullRed++;
            
        return numFullRed;
    }
       
        public double computeScoreTargeted(Mappings map,ApplicationSettings appset, DataSetCreator dat){
            double score=0.0, atImpSc=0.0, atImpSc1=0.0;
            
            TIntHashSet usedAttrs=this.computeAttributesRedSet(this.viewElementsLists, dat);
            
            TIntHashSet constAttrs = new TIntHashSet();
            
                 int maxSum=0, numNormAttrs=0; 
               for(int i=0;i<appset.importantAttributes.get(0).size();i++){//constraint sets 
                   int cumSum=0, numConstAtt=0; 
                for(int j=0;j<appset.importantAttributes.size();j++){//views
                        numConstAtt+=appset.importantAttributes.get(j).get(i).size();
                        int conSum=0;
                        for(int k=0;k<appset.importantAttributes.get(j).get(i).size();k++){
                            if(appset.importantAttributes.get(j).get(i).get(k).equals(""))
                                continue;
                                    
                             if(usedAttrs.contains(map.attId.get(appset.importantAttributes.get(j).get(i).get(k)))){
                                               conSum+=1.0;
                                     constAttrs.add(map.attId.get(appset.importantAttributes.get(j).get(i).get(k)));
                             }
                        }
                        
                        conSum/=numConstAtt;
                        
                        if(maxSum<conSum){
                            maxSum=conSum;
                        } 
                    }
                }
               
               atImpSc = maxSum;
               
               TIntIterator it = usedAttrs.iterator();
               int count=0;
               while(it.hasNext()){
                   if(constAttrs.contains(it.next()))
                       count++;
               }
               
               atImpSc1 = count/usedAttrs.size();
                
        ///cumSum/=appset.importantAttributes        
            
                                      /* for(int i=0;i<appset.importantAttributes.size();i++)
                                           if(usedAttrs.contains(map.attId.get(appset.importantAttributes.get(i))))
                                               atImpSc+=1.0;
                                       
                                       ArrayList<Integer> uAD = this.computeAttributesRed(this.viewElementsLists, dat);
                                       
                                        for(int i=0;i<appset.importantAttributes.size();i++)
                                           for(int j=0;j<uAD.size();j++)
                                               if(uAD.get(j)==map.attId.get(appset.importantAttributes.get(i)))
                                                         atImpSc1+=1.0;
                                           
                                       atImpSc/=appset.importantAttributes.size();
                                       atImpSc1/=uAD.size();*/
            
             score=0.5*atImpSc+0.5*atImpSc1;
                                       
            return score;
        }
       
       public double computeScore(int elemFreq[], int attrFreq[], DataSetCreator dat){
           double score=0.0;
           
           TIntIterator el=elements.iterator();
           
           while(el.hasNext()){
               int elem=el.next();
               score+=elemFreq[elem]-1;
           }
           
           if(elements.size()>0 && score<0.0){
               el=elements.iterator();
               while(el.hasNext()){
               int elem=el.next();
               System.out.println("Element: "+elem);
               System.out.println("Freq: "+elemFreq[elem]);
           }
           }
           
           return score;
       }
       
       public double computeScoreAttr(int elemFreq[], int attrFreq[], DataSetCreator dat){
           
           double atrSc=0.0;
           
           ArrayList<Integer> attrs=this.computeAttributesRed(viewElementsLists, dat);
           
           for(int k=0;k<attrs.size();k++){
               atrSc+=attrFreq[attrs.get(k)]-1;
           }
           
           return atrSc;
       }
    
    public int compareTo(Redescription compareRedescription) {
       
        if(this.JS>compareRedescription.JS)
            return 1;
        else if(this.JS<compareRedescription.JS)
            return -1;
        else if(this.JS==compareRedescription.JS && this.pVal<compareRedescription.pVal)
            return 1;
        else if(this.JS==compareRedescription.JS && this.pVal>compareRedescription.pVal)
            return -1;
        else
            return 0;
    }
    
    public int CompareQuality(Redescription compareRedescription){
          //correct joining to disallow intersection reduction!
       
       for(int k=0;k<compareRedescription.viewElementsLists.size();k++){ 
        for(int i=0;i<compareRedescription.viewElementsLists.get(k).size();i++)//curently only positive join allowed
            if(compareRedescription.viewElementsLists.get(k).get(i).isNegated==true)
                return -1;
       }
       
         for(int k=0;k<viewElementsLists.size();k++){ 
              for(int i=0;i<viewElementsLists.get(k).size();i++)//curently only positive join allowed
                     if(viewElementsLists.get(k).get(i).isNegated==true)
                             return -1;
                       }
       
         for(int k=0;k<viewElementsLists.size();k++){
             if(viewElementsLists.get(k).size()>2)
                 return -1;
         }
         
         for(int k=0;k<compareRedescription.viewElementsLists.size();k++){
             if(compareRedescription.viewElementsLists.get(k).size()>2)
                 return -1;
         }
         
       
       int equalElemSize=0;
       if(this.elements.size()==compareRedescription.elements.size())
           equalElemSize=1;
       else if(this.elements.size()<compareRedescription.elements.size())
           equalElemSize=-1;
       else
           equalElemSize=2;
       
       TIntIterator it = this.elements.iterator();
       
       if(equalElemSize==-1 || equalElemSize==1)
        while(it.hasNext()){
            int elem=it.next();
        //for(int elem:this.elements)
                if(compareRedescription.elements.contains(elem))
                    continue;
                else {
                    return -1;//redescriptions have different intersections
                }
        }
       
       it=compareRedescription.elements.iterator();
       
       if(equalElemSize==2)
           while(it.hasNext()){
               int elem=it.next();
        //for(int elem:compareRedescription.elements)
                if(this.elements.contains(elem))
                    continue;
                else {
                    return -1;//redescriptions have different intersections
                }
           }
        //fix domination check!
      
        if(equalElemSize==1 || (equalElemSize==-1 && this.JS!=1.0 && compareRedescription.JS!=1.0))//compareRedescription.JS<0.5 && this.JS<0.5)) 
            return 2;  //redescriptions are joinable
        
        if((equalElemSize==2 && compareRedescription.JS!=1.0 && this.JS!=1.0))//compareRedescription.JS<0.5 && this.JS<0.5))
            return 1;
        return -1;
        
    }
    
    public boolean containsNegation(){
      
      for(int k=0;k<viewElementsLists.size();k++)  
        for(int i=0;i<viewElementsLists.get(k).size();i++)
            if(viewElementsLists.get(k).get(i).isNegated==true)
                return true;
      
       return false;
    }
    
    public boolean containsDisjunction(){
        
        for(int k=0;k<viewElementsLists.size();k++)
            if(viewElementsLists.get(k).size()>1)
                return true;
        
        return false;
    }
    
     public int CompareEqual(Redescription compareRedescription){//MODIFY
          //correct joining to disallow intersection reduction!
      
       for(int k=0;k<viewElementsLists.size();k++)  
         for(int i=0;i<viewElementsLists.get(k).size();i++)
            if(viewElementsLists.get(k).get(i).isNegated==true)
                return -2;
         
       for(int k=0;k<compareRedescription.viewElementsLists.size();k++)  
         for(int i=0;i<compareRedescription.viewElementsLists.get(k).size();i++)
            if(compareRedescription.viewElementsLists.get(k).get(i).isNegated==true)
                return -2;
         
       int equalElemSize=0;
       if(this.elements.size()==compareRedescription.elements.size())
           equalElemSize=1;
       else
           equalElemSize=-1;
       
       TIntIterator it = this.elements.iterator();
       
       if(equalElemSize==1)
           while(it.hasNext()){
               int elem=it.next();
                if(compareRedescription.elements.contains(elem))
                    continue;
                else {
                    return -1;//redescriptions have different intersections
                }
           }
       
        if(equalElemSize==1) 
            return 2;  //redescriptions are joinable
      
        return -1;
        
    }


     void deleteBounds(){
     
      for(int k=0;k<viewElementsLists.size();k++){
          for(int i=0;i<viewElementsLists.get(k).size();i++){
              Conjunction conj=viewElementsLists.get(k).get(i);
               TIntIterator itL=conj.Rule.keySet().iterator();
         
                while(itL.hasNext()){
                   int att=itL.next();
                  conj.Rule.put(att, null);
                 }
          }
      }   
    }
     
     void clearRuleMaps(){
         
         for(int k=0;k<viewElementsLists.size();k++){
             for(int i=0;i<viewElementsLists.get(k).size();i++){
                 Conjunction conj=viewElementsLists.get(k).get(i);
                 conj.Rule.clear();
             }
         }
     }
     
     void closeInterval(DataSetCreator dataset, Mappings map){
    
         /*for(int k=0;k<viewElementsLists.size();k++)
             if(viewElementsLists.get(k).size()==0){
                  System.out.println("This redescription contains zero side! CI");
                  break;
             }*/
                 
  for(int k=0;k<viewElementsLists.size();k++){
      for(int i=0;i<viewElementsLists.get(k).size();i++){
          Conjunction conj=viewElementsLists.get(k).get(i);
          TIntIterator it = conj.attributes.iterator();
          
         while(it.hasNext()){
             conj.Rule.put(it.next(), null);
         }
      }
  }

  for(int k=0;k<viewElementsLists.size();k++){
    for(int i=0;i<viewElementsLists.get(k).size();i++){
        Conjunction conj=viewElementsLists.get(k).get(i);
         TIntIterator itL=conj.Rule.keySet().iterator();
         /*System.out.println("conjunction element size : "+conj.elements.size());
         System.out.println("conjunction attribute size : "+conj.Rule.keySet().size());
         System.out.println("conjunction attribute size left check: "+conj.attributes.size());*/
         while(itL.hasNext()){
             double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
             int att=itL.next();
         
         if(!map.cattAtt.containsKey(att)){
              TIntIterator it=conj.elements.iterator();
             while(it.hasNext()){
                 
                 int s=it.next();
           int ik=s;
        double val=dataset.getValue(att, ik);
        
                if(val<min)
                    min=val;
                if(val>max)
                    max=val;
        }
             ArrayList<Double> attVal=new ArrayList<>(Collections.nCopies(4, 0.0));//leftRule.get(att);

             attVal.set(0, 1.0);
             attVal.set(2, 1.0);
             attVal.set(1, min);
             attVal.set(3, max);
             conj.Rule.put(att, attVal);
         }
         else{
             if(!conj.categoricalDisjunction){
           TIntIterator  it=conj.elements.iterator();
             //System.out.println("Categorical attribute");
            // System.out.println("Num elements on the left side: "+leftRuleElements.size());
             while(it.hasNext()){
                 //System.out.println("Iterating through elements");
              int s=it.next();
                 int ik=s;
                 String val=dataset.getValueCategorical(att, ik);
                // System.out.println("Value: "+val);
                ArrayList<Double> attVal=new ArrayList<>();
                attVal.add((double)map.cattAtt.get(att).getValue0().get(val));
               // System.out.println("Attr value..");
               // System.out.println(attVal.get(0));
                conj.Rule.put(att, attVal);
                break;
             }
             }
             else{
                  TIntIterator  it=conj.elements.iterator();
                HashSet<String> categories = new HashSet<>();
             while(it.hasNext()){
                 //System.out.println("Iterating through elements");
              int s=it.next();
                 int ik=s;
                 String val=dataset.getValueCategorical(att, ik);
                 categories.add(val);
              /*  // System.out.println("Value: "+val);
                ArrayList<Double> attVal=new ArrayList<>();
                attVal.add((double)map.cattAtt.get(att).getValue0().get(val));
               // System.out.println("Attr value..");
               // System.out.println(attVal.get(0));
                conj.Rule.put(att, attVal);
                break;*/
             }
             ArrayList<Double> attVal=new ArrayList<>();
             for(String cat:categories)
             attVal.add((double)map.cattAtt.get(att).getValue0().get(cat));
             conj.Rule.put(att, attVal);
             }
         }
       }
    }   
   }
  }

     int validate(DataSetCreator dataset, Mappings map){
         HashSet<Integer> contElem=new HashSet<>();
      System.out.println("numViews: "+viewElementsLists.size());
      
      int fullV=0;
      for(int i=0;i<viewElementsLists.size();i++)
          if(viewElementsLists.get(i).size()>0)
              fullV++;
      System.out.println("Number of viewes used: "+fullV);
     
      /*this.closeInterval(dataset, map);
      this.createRuleString(map);
      for(int i=0;i<this.ruleStrings.size();i++)
          System.out.println("View "+(i+1)+" : "+ruleStrings.get(i));*/
      
      ArrayList<TIntHashSet> Supports=new ArrayList<>();// TIntHashSet(dataset.numExamples);
     
        for(int k=0;k<viewElementsLists.size();k++)
            Supports.add(new TIntHashSet(dataset.numExamples));
        
     for(int k=0;k<viewElementsLists.size();k++){
       for(int c=0;c<viewElementsLists.get(k).size();c++){  
           Conjunction conj=viewElementsLists.get(k).get(c);
         for(int i=0;i<dataset.numExamples;i++){
             TIntIterator itL=conj.Rule.keySet().iterator();
             int contained=1;
            
        if(conj.isNegated==false){  
             
             while(itL.hasNext()){
                 int attr=itL.next();
                 
                 ArrayList<Double> attrVal=conj.Rule.get(attr);
                 /*System.out.println("Attribute: "+map.idAtt.get(attr));
                 System.out.println("view: "+k);
                 System.out.println("conjunction: "+c);
                 System.out.println("elements size: "+conj.elements.size());
                 System.out.println("rule attr size: "+conj.Rule.keySet().size());
                 System.out.println("attrs size: "+conj.attributes.size());*/
                 
               if(!map.cattAtt.containsKey(attr)){
                   double val=dataset.getValue(attr, i);
                 if(val>=attrVal.get(1) && val<=attrVal.get(3))
                        continue;
                 else{
                    contained=0;
                    break;
                 }
               }
               else{
                   if(!conj.categoricalDisjunction){
                   String cat=map.cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(0)).trim();
                   String realCat=dataset.getValueCategorical(attr, i).trim();
                   /*System.out.println("case !CD cat "+" realCat:");
                   System.out.println(cat+" "+realCat);
                   System.out.println("Attribute: "+map.idAtt.get(attr));
                   System.out.println(this.ruleStrings.get(0)+"\n"+this.ruleStrings.get(1));*/
                   if(cat.contentEquals(realCat)){
                       continue;
                   }
                   else{
                       contained=0;
                       break;
                   }
                   }
                   else{
                       String realCat=dataset.getValueCategorical(attr, i).trim();
                       int found = 0;
                       for(int zz=0;zz<attrVal.size();zz++){
                            String cat=map.cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(zz)).trim();
                            if(cat.contentEquals(realCat)){
                                found = 1;
                                break;
                            }
                       }
                       if(found == 1) continue;
                       else{
                           contained = 0;
                           break;
                       }
                   }
               }
             }
        }
             else{
                 contained=0;
                    if(!conj.elements.contains(i)){
                        TIntIterator it=conj.attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.cattAtt.keySet().contains(at)){
                                if(dataset.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dataset.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                        }
                    }      
             }

             if(contained==0)
                 continue;
             else{
                 //lSupport.add(i);
                 Supports.get(k).add(i);
             }
         }
       }
     }
     
     TIntIterator it=Supports.get(0).iterator();
     
          while(it.hasNext()){
              int elem=it.next();
              int contained=1;
             for(int k=1;k<viewElementsLists.size();k++){
                if(viewElementsLists.get(k).size()>0) 
                    if(!Supports.get(k).contains(elem)){
                         contained=0;
                         break;
                     }
                 }
                 if(contained==1)
                     contElem.add(elem);
            //}
         }
         
          ArrayList<TIntHashSet> sideElems=new ArrayList<>();
          
          for(int k=0;k<viewElementsLists.size();k++)
              sideElems.add(computeElements(viewElementsLists.get(k),dataset,map));
         
         if(elements.size()!=contElem.size()){
             this.createRuleString(map);
            for(int k=0;k<viewElementsLists.size();k++){
                System.out.println("Categorical disjunctions: ");
                for(int zz=0;zz<viewElementsLists.get(k).size();zz++)
                System.out.println(viewElementsLists.get(k).get(zz).categoricalDisjunction);
                System.out.println("Rule W"+(k+1)+" "+ruleStrings.get(k));
            }
             System.out.println("elements size in validation: "+elements.size());
             System.out.println("control elements size in validation: "+contElem.size());
             System.out.println("Reds acc: "+this.JS);
             System.out.println("Control support sizes: ");
             for(int zz=0;zz<Supports.size();zz++)
             System.out.println(Supports.get(zz).size()+" ");
             
             System.out.println("Computed clause support sizes: ");
             for(int zz=0;zz<sideElems.size();zz++)
             System.out.println(sideElems.get(zz).size()+" ");
             
             if(elements.size()>contElem.size()){
                 for(int i:elements.toArray())
                     if(!contElem.contains(i))
                         System.out.println("Additional element: "+map.idExample.get(i));
               
              for(int k=0;k<sideElems.size();k++){
                  it=sideElems.get(k).iterator();
                   while(it.hasNext()){
                        int elem=it.next();
                        if(!Supports.get(k).contains(elem)){
                            System.out.println("view"+(k+1)+" "+" rule missing element");
                            System.out.println("elem: "+map.idExample.get(elem));
                        }
                    }                     
             }
            }
             else{
                 for(int i:contElem){
                      if(!elements.contains(i))
                          System.out.println("Missing element: "+map.idExample.get(i));
                 }
             }
             deleteBounds();
             return 0;
         }

         for(int s:contElem)
             if(!elements.contains(s)){
                 System.out.println("Not supported element: "+s);
                 System.out.println("String of this elements: "+map.idExample.get(s));
                 deleteBounds();
                 return 0;
             }
       deleteBounds();
         return 1;
     }
     
     
     
     
     int ComputeValidationStatistics(DataSetCreator datasetTrain,DataSetCreator dataset, Mappings map){
         
         this.closeInterval(datasetTrain, map);
         
         TIntHashSet contElem = new TIntHashSet(dataset.numExamples);
        // HashSet<Integer> contElem=new HashSet<>();
      System.out.println("numViews: "+viewElementsLists.size());
      
      int fullV=0;
      for(int i=0;i<viewElementsLists.size();i++)
          if(viewElementsLists.get(i).size()>0)
              fullV++;
      System.out.println("Number of viewes used: "+fullV);
     
      /*this.closeInterval(dataset, map);
      this.createRuleString(map);
      for(int i=0;i<this.ruleStrings.size();i++)
          System.out.println("View "+(i+1)+" : "+ruleStrings.get(i));*/
      
      ArrayList<TIntHashSet> Supports=new ArrayList<>();// TIntHashSet(dataset.numExamples);
     
        for(int k=0;k<viewElementsLists.size();k++)
            Supports.add(new TIntHashSet(dataset.numExamples));
        
     for(int k=0;k<viewElementsLists.size();k++){
       for(int c=0;c<viewElementsLists.get(k).size();c++){  
           Conjunction conj=viewElementsLists.get(k).get(c);
         for(int i=0;i<dataset.numExamples;i++){
             TIntIterator itL=conj.Rule.keySet().iterator();
             int contained=1;
            
        if(conj.isNegated==false){  
             
             while(itL.hasNext()){
                 int attr=itL.next();
                 
                 ArrayList<Double> attrVal=conj.Rule.get(attr);
                 /*System.out.println("Attribute: "+map.idAtt.get(attr));
                 System.out.println("view: "+k);
                 System.out.println("conjunction: "+c);
                 System.out.println("elements size: "+conj.elements.size());
                 System.out.println("rule attr size: "+conj.Rule.keySet().size());
                 System.out.println("attrs size: "+conj.attributes.size());*/
                 
               if(!map.cattAtt.containsKey(attr)){
                   double val=dataset.getValue(attr, i);
                 if(val>=attrVal.get(1) && val<=attrVal.get(3))
                        continue;
                 else{
                    contained=0;
                    break;
                 }
               }
               else{
                   String cat=map.cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(0));
                   String realCat=dataset.getValueCategorical(attr, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else{
                       contained=0;
                       break;
                   }
               }
             }
        }
             else{
                 contained=0;
                 conj.computeSuportValidation(dataset, map);
                    if(!conj.elementsTest.contains(i)){
                        TIntIterator it=conj.attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.cattAtt.keySet().contains(at)){
                                if(dataset.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dataset.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                        }
                    }      
             }

             if(contained==0)
                 continue;
             else{
                 //lSupport.add(i);
                 Supports.get(k).add(i);
             }
         }
       }
     }
     
     TIntIterator it=Supports.get(0).iterator();
     
          while(it.hasNext()){
              int elem=it.next();
              int contained=1;
             for(int k=1;k<viewElementsLists.size();k++){
                if(viewElementsLists.get(k).size()>0) 
                    if(!Supports.get(k).contains(elem)){
                         contained=0;
                         break;
                     }
                 }
                 if(contained==1)
                     contElem.add(elem);
            //}
         }
          
      this.elementsValidation=contElem;
      TIntHashSet redUnion=new TIntHashSet(dataset.numExamples);
      
      TIntIterator itUnion;
      
      for(int i=0;i<Supports.size();i++){
          itUnion=Supports.get(i).iterator();
          
          while(itUnion.hasNext()){
              redUnion.add(itUnion.next());
          }
          
      }
      
      this.elementsUnionValidation=redUnion;
     // System.out.println("Validation sets size: "+this.elementsValidation.size()+" "+this.elementsUnionValidation.size());
      this.JSAll=(double)this.elementsValidation.size()/this.elementsUnionValidation.size();
      if(this.elementsUnionValidation.size() == 0) this.JSAll = 0.0;
      
     // this.JSAll=this.computePVal(dataset, map);
      
      //this.computePValValidation(dataset, map);
      
      double pd=1.0, pn=1.0;
      
      for(int i=0;i<Supports.size();i++){
          pd*=Supports.get(i).size();
          pn*=dataset.numExamples;
      }
      
       double prob=pd/pn;//(double)this.elementsValidation.size()/dataset.numExamples;
            BinomialDistribution dist=new BinomialDistribution(dataset.numExamples,prob);
            this.pValValidation=1.0-dist.cumulativeProbability(this.elementsValidation.size());
         
         /* ArrayList<TIntHashSet> sideElems=new ArrayList<>();
          
          for(int k=0;k<viewElementsLists.size();k++)
              sideElems.add(computeElements(viewElementsLists.get(k),dataset,map));
         
         if(elements.size()!=contElem.size()){
             this.createRuleString(map);
            for(int k=0;k<viewElementsLists.size();k++)
                System.out.println("Rule W"+(k+1)+" "+ruleStrings.get(k));
             System.out.println("elements size in validation: "+elements.size());
             System.out.println("control elements size in validation: "+contElem.size());
             
             if(elements.size()>contElem.size()){
                 for(int i:elements.toArray())
                     if(!contElem.contains(i))
                         System.out.println("Additional element: "+map.idExample.get(i));
               
              for(int k=0;k<sideElems.size();k++){
                  it=sideElems.get(k).iterator();
                   while(it.hasNext()){
                        int elem=it.next();
                        if(!Supports.get(k).contains(elem)){
                            System.out.println("view"+(k+1)+" "+" rule missing element");
                            System.out.println("elem: "+map.idExample.get(elem));
                        }
                    }                     
             }
            }
             else{
                 for(int i:contElem){
                      if(!elements.contains(i))
                          System.out.println("Missing element: "+map.idExample.get(i));
                 }
             }
             deleteBounds();
             return 0;
         }

         for(int s:contElem)
             if(!elements.contains(s)){
                 System.out.println("Not supported element: "+s);
                 System.out.println("String of this elements: "+map.idExample.get(s));
                 deleteBounds();
                 return 0;
             }*/
       deleteBounds();
         return 1;
     }
     
     
        int ComputeTestStatistics(DataSetCreator datasetTrain,DataSetCreator dataset, Mappings map){
         
         this.closeInterval(datasetTrain, map);
         
         TIntHashSet contElem = new TIntHashSet(dataset.numExamples);

      System.out.println("numViews: "+viewElementsLists.size());
      
      int fullV=0;
      for(int i=0;i<viewElementsLists.size();i++)
          if(viewElementsLists.get(i).size()>0)
              fullV++;
      System.out.println("Number of viewes used: "+fullV);
     
      ArrayList<TIntHashSet> Supports=new ArrayList<>();
     
        for(int k=0;k<viewElementsLists.size();k++)
            Supports.add(new TIntHashSet(dataset.numExamples));
        
     for(int k=0;k<viewElementsLists.size();k++){
       for(int c=0;c<viewElementsLists.get(k).size();c++){  
           Conjunction conj=viewElementsLists.get(k).get(c);
         for(int i=0;i<dataset.numExamples;i++){
             TIntIterator itL=conj.Rule.keySet().iterator();
             int contained=1;
            
        if(conj.isNegated==false){  
             
             while(itL.hasNext()){
                 int attr=itL.next();
                 
                 ArrayList<Double> attrVal=conj.Rule.get(attr);
                 
               if(!map.cattAtt.containsKey(attr)){
                   double val=dataset.getValue(attr, i);
                 if(val>=attrVal.get(1) && val<=attrVal.get(3))
                        continue;
                 else{
                    contained=0;
                    break;
                 }
               }
               else{
                   String cat=map.cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(0));
                   String realCat=dataset.getValueCategorical(attr, i);
                   if(cat.contentEquals(realCat))
                       continue;
                   else{
                       contained=0;
                       break;
                   }
               }
             }
        }
             else{
                 contained=0;
                   conj.computeSuportValidation(dataset, map);
                    if(!conj.elementsTest.contains(i)){
                        TIntIterator it=conj.attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.cattAtt.keySet().contains(at)){
                                if(dataset.getValue(at, i)!=Double.POSITIVE_INFINITY){
                                    contained=1;
                                break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dataset.getValueCategorical(at, i))){
                                    contained=1;
                                    break;
                                }
                            }
                        }
                    }      
             }

             if(contained==0)
                 continue;
             else{

                 Supports.get(k).add(i);
             }
         }
       }
     }
     
     TIntIterator it=Supports.get(0).iterator();
     
          while(it.hasNext()){
              int elem=it.next();
              int contained=1;
             for(int k=1;k<viewElementsLists.size();k++){
                if(viewElementsLists.get(k).size()>0) 
                    if(!Supports.get(k).contains(elem)){
                         contained=0;
                         break;
                     }
                 }
                 if(contained==1)
                     contElem.add(elem);
         }
          
      this.elementsTest=contElem;
      TIntHashSet redUnion=new TIntHashSet(dataset.numExamples);
      
      TIntIterator itUnion;
      
      for(int i=0;i<Supports.size();i++){
          itUnion=Supports.get(i).iterator();
          
          while(itUnion.hasNext()){
              redUnion.add(itUnion.next());
          }
          
      }
      
      
      this.elementsUnionTest=redUnion;
      this.JSTest=(double)this.elementsTest.size()/this.elementsUnionTest.size();
      
      double pd=1.0, pn=1.0;
      
      for(int i=0;i<Supports.size();i++){
          pd*=Supports.get(i).size();
          pn*=dataset.numExamples;
      }
      
       double prob=pd/pn;
            BinomialDistribution dist=new BinomialDistribution(dataset.numExamples,prob);
            this.pValTest=1.0-dist.cumulativeProbability(this.elementsTest.size());
 
       deleteBounds();
         return 1;
     }

     public ArrayList<Conjunction> joinConjunction(Conjunction c, Mappings map, DataSetCreator dat, int side){
         
         Conjunction cOrig=null;
         ArrayList<Conjunction> res=new ArrayList<>();
         
         ArrayList<Conjunction> tmp=viewElementsLists.get(side);
         
         for(int i=0;i<tmp.size();i++){ 
             cOrig=new Conjunction(tmp.get(i));
             TIntIterator it = c.attributes.iterator();
        
              while(it.hasNext()){
                 int at=it.next();

                 if(!cOrig.attributes.contains(at)){
                    cOrig.attributes.add(at);
                }   
             }
              
        cOrig.computeIntersection(c);  
            if(!cOrig.elements.isEmpty()){
                int add=1;
                    for(int ci=0;ci<res.size();ci++){
                        if(cOrig.equalElements(res.get(ci))==1)
                            add=0;
                    }
                    if(add==1)
                        res.add(cOrig);
                }
         }
         
         return res;
     }
     
    public void join(Redescription joinRedescription, Mappings map, DataSetCreator dat){//Conjunctive join
       
        int diff=countDiff(joinRedescription,dat,map);
        
        if(diff<=0)
            return;
        
        ArrayList<Conjunction> res=null;
        ArrayList<Conjunction> finRes=new ArrayList<>();
     
        for(int k=0;k<viewElementsLists.size();k++){
            if(viewElementsLists.get(k).size()==0 || joinRedescription.viewElementsLists.get(k).size()==0)
                continue;
            for(int i=0;i<joinRedescription.viewElementsLists.get(k).size();i++){
                 res=this.joinConjunction(joinRedescription.viewElementsLists.get(k).get(i), map, dat, k);
                    
                 for(int j=0;j<res.size();j++)
                     finRes.add(res.get(j));
                 res.clear();
            }
         
            this.viewElementsLists.get(k).clear();
        
        for(int i=0;i<finRes.size();i++)
            viewElementsLists.get(k).add(finRes.get(i));
        
        finRes.clear();
        }
    
        
        ArrayList<TIntHashSet> elemsW=new ArrayList<>();
        
        for(int k=0;k<viewElementsLists.size();k++){
            elemsW.add(this.computeElements(viewElementsLists.get(k), dat, map));
        }

        
        TIntIterator it=elements.iterator();

        this.computeUnionGen(elemsW);
        
        JS=(double)this.elements.size()/this.elementsUnion.size();
        supportsSides.clear();
        for(int k=0;k<viewElementsLists.size();k++)
            supportsSides.add(elemsW.get(k).size());
    }
    
    int numUsedViews(){
        int count=0;
        
        for(int i=0;i<viewElementsLists.size();i++)
            if(viewElementsLists.get(i).size()>0)
                count++;
        
        return count;
    }
    
    void checkRedescription(DataSetCreator dat, Mappings map){
        TIntIterator it = elements.iterator();
       
      for(int k=0;k<viewElementsLists.size();k++){  
          TIntHashSet Elems=this.computeElements(viewElementsLists.get(k), dat, map);
        while(it.hasNext()){
            int s=it.next();
            if(!Elems.contains(s))
               System.out.println("Error in redescription, removed to many attributes view "+(k+1)+" elements!");
        }      
      }
    }
    
    void minimizeConjunction(Conjunction c, DataSetCreator dat, Mappings map){
        
       TIntHashSet toRemove=new TIntHashSet();
       TIntObjectHashMap<TIntHashSet> attributeSupp=new TIntObjectHashMap();
        
       for(int i=0;i<dat.numExamples;i++){
       for(int sAt: c.Rule.keys()){
           if(!map.cattAtt.keySet().contains(sAt)){
            if(!attributeSupp.containsKey(sAt)){
                 TIntHashSet tmp=new  TIntHashSet();
                attributeSupp.put(sAt, tmp);
            }

        double val=dat.getValue(sAt, i);
        if(val!=Double.POSITIVE_INFINITY && val>=c.Rule.get(sAt).get(1)  && val<=c.Rule.get(sAt).get(3)){
            int elem=i;
                if(!c.elements.contains(elem)){
           attributeSupp.get(sAt).add(elem);
                }
         }
       }
           else{
               if(!attributeSupp.containsKey(sAt)){
                TIntHashSet tmp=new TIntHashSet();
                attributeSupp.put(sAt, tmp);
            }

        String val=dat.getValueCategorical(sAt, i);
        if(map.cattAtt.get(sAt).getValue0().keySet().contains(val) && (map.cattAtt.get(sAt).getValue0().get(val)==((int)(double)c.Rule.get(sAt).get(0)))){
            int elem=i;
                if(!c.elements.contains(elem)){
           attributeSupp.get(sAt).add(elem);
                }
         }
        }    
     }
   }

    int max=0;
    int maxInd=0;
     TIntHashSet toKeap=new  TIntHashSet();
    ArrayList< TIntHashSet> partition=new ArrayList<>();
     TIntHashSet elem1=null;
  
   int keys[]=attributeSupp.keys();
     
  for(int k=0;k<keys.length;k++){
         elem1=new  TIntHashSet(attributeSupp.get(keys[k]));
          toKeap=new  TIntHashSet();
          toKeap.add(keys[k]);
    for(int i=0;i<keys.length;i++){
        max=0; maxInd=0;
         TIntHashSet cont=new TIntHashSet();
         if(i==k)
             continue;
         for(int j=0;j<keys.length;j++){
             
             int ssAt1=keys[j];
             if(j==k)
                 continue;
             if(!toKeap.contains(ssAt1)){
                 TIntIterator it=elem1.iterator();
                 while(it.hasNext()){
                     int element=it.next();
                     if(!attributeSupp.get(ssAt1).contains(element)){
                         cont.add(element);
                     }
                 }
                 if(cont.size()>max && !toKeap.contains(keys[j])){
                     max=cont.size();
                     maxInd=j;
                 }
                 cont.clear();
             }
         }

           toKeap.add((int)keys[maxInd]);
           TIntIterator it=elem1.iterator();
           
           while(it.hasNext()){
               int element=it.next();
               if(!attributeSupp.get(keys[maxInd]).contains(element)){
                         cont.add(element);
                     }
           }
           
           it=cont.iterator();
           
           while(it.hasNext()){
               int element=it.next();
               elem1.remove(element);
           }

            cont.clear();
           if(elem1.isEmpty())
               break;
     }
  
    //check if first element is redundant
    TIntHashSet checkFirst=null;
    
    TIntIterator it=toKeap.iterator();//sumnjivo, ne mora dati prvi
    
    int atG=-1;
    while(it.hasNext()){
        int at=it.next();
        if(at==keys[k])
            continue;
        else {atG=at; break;}
    }
    
    if(atG!=-1){
    checkFirst=new TIntHashSet(attributeSupp.get(atG));
    
    it=toKeap.iterator();
    
    while(it.hasNext()){
        int at=it.next();
        if(at==keys[k] || at==atG)
            continue;
        
        TIntHashSet checkFirstTmp=attributeSupp.get(at);
        
        TIntIterator elIt=checkFirst.iterator();
        
        while(elIt.hasNext()){
            int elTmp=elIt.next();
            if(!checkFirstTmp.contains(elTmp))
              elIt.remove();  //checkFirst.remove(elTmp);
        }
    }
      ///added, should be tested   
    if(checkFirst.isEmpty()==true)
        toKeap.remove(keys[k]);
    }
    
    
    if(elem1.isEmpty())
        partition.add(toKeap);
  }
  
  int minPartInd=0;
  int min=Integer.MAX_VALUE;
  for(int i=0;i<partition.size();i++){
      if(partition.get(i).size()<min){
          min=partition.get(i).size();
          minPartInd=i;
      }
  }

   if(partition.size()>0){
     for(int s:keys)
         if(!partition.get(minPartInd).contains(s)){
        c.Rule.remove(s);
        c.attributes.remove(s);
        System.out.println("Removing attributes MinOpt...");
        System.out.println("Removed attribute: "+map.idAtt.get(s));
         }
   }
     toRemove.clear();
     toKeap.clear();
     partition.clear();
}
    
    void minimizeOptimal(DataSetCreator dat, Mappings map, int RuleString){
 
        /*for(int k=0;k<viewElementsLists.size();k++)
            if(viewElementsLists.get(k).size()==0){
                 System.out.println("This redescription contains zero rule! MO");
                 break;
            }*/
        /*this.closeInterval(dat, map);
        this.createRuleString(map);
        int val=this.validate(dat, map);
        System.out.println("value: "+val);
        this.closeInterval(dat, map);
        this.createRuleString(map);
        String left=this.ruleStrings.get(0);
        String right=this.ruleStrings.get(1);*/
      
      for(int k=0;k<viewElementsLists.size();k++){  
        for(int i=0;i<viewElementsLists.get(k).size();i++){
             if(viewElementsLists.get(k).get(i).categoricalDisjunction)
                continue;
            if(viewElementsLists.get(k).get(i).attributes.size()==0)
                System.out.println("Empty conjunction in minimizeOptimal");
            if(viewElementsLists.get(k).get(i).isNegated==false)
                minimizeConjunction(viewElementsLists.get(k).get(i), dat, map);
            
            if(viewElementsLists.get(k).get(i).attributes.size()==0)
                System.out.println("Empty conjunction in minimizeOptimal after");
        }
      }
      
     /* this.clearRuleMaps();
      this.closeInterval(dat, map);
      this.createRuleString(map);
      int val1=this.validate(dat, map);
      System.out.println("value1: "+val1);
      if( val1==0 && val==1){
          System.out.println("Rules before: ");
          System.out.println(left);
          System.out.println(right);
          System.out.println("Not OK after minimization");
          System.exit(-1);
      }
      this.closeInterval(dat, map);*/
     if(RuleString!=0)
        createRuleString(map);
    } 

 }
