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

/**
 *
 * @author matej
 */
public class Conjunction {
    TIntHashSet elements=null;
    TIntHashSet attributes=null;
    TIntObjectHashMap<ArrayList<Double>> Rule=null;
    boolean isNegated=false;
    TIntHashSet elementsTest = null;
    boolean categoricalDisjunction = false;
    
    Conjunction(){
        elements=new TIntHashSet();
        attributes=new TIntHashSet();
        Rule=new TIntObjectHashMap<>();
        isNegated=false;
    }
    
    Conjunction(DataSetCreator dat){
        elements=new TIntHashSet(2*dat.numExamples);
    
       Rule=new TIntObjectHashMap();
       attributes=new TIntHashSet(dat.schema.getNbAttributes());
       isNegated=false;
    }
    
    Conjunction(Conjunction con){
       elements=new TIntHashSet(con.elements);
       attributes=new TIntHashSet(con.attributes);
       Rule=new TIntObjectHashMap(con.Rule);
       isNegated=con.isNegated;
       categoricalDisjunction = con.categoricalDisjunction;
    }
    
    Conjunction(String _rule, DataSetCreator dat, Mappings map){
        elements=new TIntHashSet(dat.numExamples);
        attributes=new TIntHashSet(dat.schema.getNbAttributes());
        Rule=new TIntObjectHashMap();
        String rs[]=_rule.split(" AND ");
         
        for(int i=0;i<rs.length;i++){
                 if(rs[i].contains(">")){
                    String t[]=rs[i].split(" > ");

                     if(!attributes.contains(map.attId.get(t[0]))){
                        t[1]=t[1].replaceAll(",", ".");
                        attributes.add(map.attId.get(t[0]));
                    }
                }
                else if(rs[i].contains("<=")){
                     String t[]=rs[i].split(" <= ");
                     if(!attributes.contains(map.attId.get(t[0]))){
                        t[1]=t[1].replaceAll(",", ".");

                        attributes.add(map.attId.get(t[0]));
                    }
                }
                else if(rs[i].contains("=") && !rs[i].contains("<")){
                      String t[]=rs[i].split(" = ");
                       if(!attributes.contains(map.attId.get(t[0]))){
                           attributes.add(map.attId.get(t[0]));
                      }
                }
                else if(rs[i].contains(" in ")){
                    categoricalDisjunction = true;
                    String t[]=rs[i].split(" in ");
                    if(!attributes.contains(map.attId.get(t[0]))){
                           attributes.add(map.attId.get(t[0]));
                      }
                }
            }
        
        if(attributes.size()==0){
            System.out.println("Something is wrong in the Conjunction from Rule computation!");
            System.out.println("Rule :"+_rule);
        }
    }
    
     Conjunction(String _rule, DataSetCreator dat, Mappings map, double load){
        elements=new TIntHashSet(dat.numExamples);
        attributes=new TIntHashSet(dat.schema.getNbAttributes());
        Rule=new TIntObjectHashMap();
        String rs[]=_rule.split(" AND ");
         
        for(int i=0;i<rs.length;i++){
                if(rs[i].contains(">=")){
                    String t[]=rs[i].split(" >= ");
                    t[0] = t[0].trim();
                    t[0] = t[0].replace("W1R: ","");
                    t[0] = t[0].replace("W2R: ", "");

                     if(!attributes.contains(map.attId.get(t[0]))){
                       // t[1]=t[1].replaceAll(",", ".");
                        attributes.add(map.attId.get(t[0]));
                        
                        ArrayList<Double> attVal=new ArrayList<>(Collections.nCopies(4, 0.0));//leftRule.get(att);

                        String bounds[] = t[1].split(" <= ");
                        double min = Double.parseDouble(bounds[0].trim());
                        double max = Double.parseDouble(bounds[1].trim());
                       // System.out.println("min/max"+" "+min+" "+max);
                        
                      attVal.set(0, 1.0);
                      attVal.set(2, 1.0);
                      attVal.set(1, min);
                      attVal.set(3, max);
                      Rule.put(map.attId.get(t[0]), attVal);
                        
                    }
                }
                else if(rs[i].contains(">")){
                    String t[]=rs[i].split(" > ");

                     if(!attributes.contains(map.attId.get(t[0]))){
                        t[1]=t[1].replaceAll(",", ".");
                        attributes.add(map.attId.get(t[0]));
                    }
                }
                else if(rs[i].contains("<=")){
                     String t[]=rs[i].split(" <= ");
                     if(!attributes.contains(map.attId.get(t[0]))){
                        t[1]=t[1].replaceAll(",", ".");

                        attributes.add(map.attId.get(t[0]));
                    }
                }
                else if(rs[i].contains("=") && !rs[i].contains("<")){
                      String t[]=rs[i].split(" = ");
                       if(!attributes.contains(map.attId.get(t[0]))){
                           attributes.add(map.attId.get(t[0]));
                      }
                }
                else if(rs[i].contains(" in ")){
                    categoricalDisjunction = true;
                    String t[]=rs[i].split(" in ");
                    if(!attributes.contains(map.attId.get(t[0]))){
                           attributes.add(map.attId.get(t[0]));
                      }
                }
            }
        
        if(attributes.size()==0){
            System.out.println("Something is wrong in the Conjunction from Rule computation!");
            System.out.println("Rule :"+_rule);
        }
    }
    
    
    Conjunction(String _rule, DataSetCreator dat, Mappings map, int irrelevant){
        elements=new TIntHashSet(dat.numExamples);
        attributes=new TIntHashSet(dat.schema.getNbAttributes());
        Rule=new TIntObjectHashMap();
        String rs[]=_rule.split(" AND ");
         
        for(int i=0;i<rs.length;i++){
                if(rs[i].contains(">=")){
                    String t[]=rs[i].split(" >= ");
//System.out.println("t[0] "+t[0]);
                     if(!attributes.contains(map.attId.get(t[0]))){
                       // t[1]=t[1].replaceAll(",", ".");
                        attributes.add(map.attId.get(t[0]));
                    }
                }
                else if(rs[i].contains("<=")){
                     String t[]=rs[i].split(" <= ");
                     if(!attributes.contains(map.attId.get(t[0]))){
                       // t[1]=t[1].replaceAll(",", ".");

                        attributes.add(map.attId.get(t[0]));
                    }
                }
                else if(rs[i].contains("=") && !rs[i].contains("<")){
                      String t[]=rs[i].split(" = ");
                       if(!attributes.contains(map.attId.get(t[0]))){
                           attributes.add(map.attId.get(t[0]));
                      }
                }
            }
        
        if(attributes.size()==0){
            System.out.println("Something is wrong in the Conjunction from Rule computation!");
            System.out.println("Rule :"+_rule);
        }
    }
    
    public void computeSuportValidation(DataSetCreator dat, Mappings map){
        
        TIntHashSet tmp = new TIntHashSet(dat.numExamples);
        
         for(int el=0;el<dat.numExamples;el++){
             int contained=1;
                  TIntIterator it=attributes.iterator();              
                        while(it.hasNext()){
                            int attr = it.next();
                             ArrayList<Double> attrVal=Rule.get(attr);
                           if(!map.cattAtt.containsKey(attr)){
                                 double val=dat.getValue(attr, el);
                               // System.out.println(attrVal.get(1)+" "+attrVal.get(3));
                                if(val>=attrVal.get(1) && val<=attrVal.get(3))
                                        continue;
                 else{
                    contained=0;
                    break;
                 }
               }
               else{
                   String cat=map.cattAtt.get(attr).getValue1().get((int)(double)attrVal.get(0));
                   String realCat=dat.getValueCategorical(attr, el);
                   if(cat.contentEquals(realCat))
                       continue;
                   else{
                       contained=0;
                       break;
                   }
               }
              }
                        if(contained==1)
                            tmp.add(el);
         }
        
         elementsTest = tmp;
        
        /*if(isNegated==false){
                    //currently not needed
                        elementsTest=tmp;
            }
            else{
            elementsTest = new TIntHashSet(dat.numExamples);
            //implement
                for(int el=0;el<dat.numExamples;el++){
                    if(!tmp.contains(el)){
                        TIntIterator it=attributes.iterator();
                        while(it.hasNext()){
                            int at=it.next();
                            if(!map.catAttInd.contains(at)){
                                if(dat.getValue(at, el)!=Double.POSITIVE_INFINITY){
                                    elementsTest.add(el);
                                    break;
                                }
                            }
                            else{
                                if(map.cattAtt.get(at).getValue0().keySet().contains(dat.getValueCategorical(at, el))){
                                    elementsTest.add(el);
                                    break;
                                }
                            }
                        }
                    }
                }
            }*/
    }
    
    public void negate(){
        isNegated=true;
    }
    
    public void computeIntersection(Conjunction cJoin){
        TIntHashSet toRemove=new TIntHashSet();
        TIntIterator it=elements.iterator();
        
        while(it.hasNext()){
            int elem=it.next();
            if(!cJoin.elements.contains(elem))
                toRemove.add(elem);
        }
        
        it=toRemove.iterator();
        
        while(it.hasNext()){
            elements.remove(it.next());
        }
        
        toRemove.clear();   
    }
    
    public int equalElements(Conjunction c){
        int equal=1;
        
        TIntIterator it=elements.iterator();
        
        while(it.hasNext()){
            if(!c.elements.contains(it.next()))
                return 0;
        }
        
        return equal;
    }
    
    public int dominates(Conjunction c){

        if((isNegated==true && c.isNegated==false) || (isNegated==false && c.isNegated==true))
            return 0;
        
        if(isNegated==true && c.isNegated==true){
            return 0;
           /* TIntIterator it=attributes.iterator();
            while(it.hasNext()){
            if(!c.attributes.contains(it.next()))
                return 0;
        }
            if(attributes.size()<c.attributes.size())
                return 1;
            else return -1;*/
        }
        
        TIntIterator it=c.elements.iterator();
      
     if(elements.size()>=c.elements.size()){
        while(it.hasNext()){
            if(!elements.contains(it.next()))
                return 0;
        }
     }
     else{
         return -1;
     }
        
        return 1;
    }
    
}
