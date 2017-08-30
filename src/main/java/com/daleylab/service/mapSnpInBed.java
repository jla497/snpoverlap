/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daleylab.service;

import static com.daleylab.service.ProcessBed.isNullOrWhitespace;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author JLee05
 */
@Component
@Scope("prototype")
@PropertySource({"classpath:application.properties"})
public class mapSnpInBed implements Runnable {

    ArrayList<Long[]> chromList;
    HashSet<String> snps_notin_ref;
    int inputNum;
    int chromNum;
    String bedDelimiter;
    
    
    public mapSnpInBed( ArrayList<Long[]>chromList, int inputNum, int chromNum ) throws FileNotFoundException{
       
        this.chromList=chromList;
        this.inputNum=inputNum;
        this.chromNum=chromNum;
        this.bedDelimiter = "\t";
        //this.snps_notin_ref = snps_notin_ref;
       
    }
    
    @Override
    public void run() {
       
        HashSet<Long> snpsMatchStartPos = getSnpsMatchStartPos();
        
        try {
            writeSnpFile(snpsMatchStartPos,inputNum,chromNum);
            
        } catch (IOException ex) {
          ex.printStackTrace();
        }
        
        
    
    }
//    
//    public  HashSet<Long> getSnpsMatchStartPos() {
//        
//        String bedFilePrefix = "sorted_bed_chr_";
//        StringBuilder sb = new StringBuilder();
//        sb.append("bedfiles/");
//        sb.append(bedFilePrefix);
//
//        sb.append(chromNum);
//
//        sb.append(".bed");
//
//        String fileName = sb.toString();
//        String workingDir = System.getProperty("user.dir");
//        
//       
//        if(chromNum == 23 ){
//            fileName ="bedfiles/"+bedFilePrefix+"X.bed";
//        }else if(chromNum == 24){
//            fileName = "bedfiles/"+bedFilePrefix+"Y.bed";
//        }else if(chromNum == 25){
//            fileName = "bedfiles/"+bedFilePrefix+"MT.bed";
//        }
//        //System.out.println(System.getProperty("user.dir"));
//        System.out.println("Loading "+fileName+"...");
//
//      FileReader fileContent = null;
//       String absolutePath = workingDir+File.separator+fileName;
//        try {
//            fileContent = new FileReader(absolutePath);
//        } catch (FileNotFoundException ex) {
//           ex.printStackTrace();
//        }
//        
//        
//       HashSet<Long> result = new HashSet<Long>();
//       
//       Long count = 0L;
//
//        System.out.println("in MapBedFile...");
//        
//        if (!isNullOrWhitespace(fileContent.toString())) {
//            
//            BufferedReader reader = new BufferedReader(fileContent);
//            String line;
//            
//            try {
//                while ((line = reader.readLine()) != null) {
//                        if(isLineValid(line)==false){
//                      
//                            continue;
//                        }
//                        
//                         String snpID = parseSnpId(line);
//
//                        Long snpStart = parseSnpStart(line);
//
//                        Long snpEnd = parseSnpEnd(line);
//                        
//                        for(Long[] chromosomes: chromList){
//                             Long scanStart = chromosomes[0];
//
//                             Long scanEnd = chromosomes[1];  
//                       
//                             if(snpStart>= scanStart && snpEnd <= scanEnd){
////                               
//                                 result.add(Long.parseLong(snpID)); 
//                                 
//                             }
//                        }
//           
//                         count++;
//                         
//                        if(count % 1000000L ==0){
//                        
//                            System.out.println("Currently read "+count+" number of bed file lines...");
//                        }
//                    }
//                    
//                   
//   
//            }catch(Exception e){
//                
//             e.printStackTrace();   
//            }      
//            
//             try {
//                reader.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//
//            try {
//                fileContent.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//        
//     
//       System.out.println("Finished parsing bed file for chr"+chromNum+" for input file "+inputNum);
//      
//       return result;
//    }
    
    public HashSet<Long> getSnpsMatchStartPos(){
    
        String bedFilePrefix = "r_sorted_bed_chr_";
        StringBuilder sb = new StringBuilder();
        sb.append("bedfiles/");
        sb.append(bedFilePrefix);

        sb.append(chromNum);

        sb.append(".bed");

        String fileName = sb.toString();
        String workingDir = System.getProperty("user.dir");
        
       
        if(chromNum == 23 ){
            fileName ="bedfiles/"+bedFilePrefix+"X.bed";
        }else if(chromNum == 24){
            fileName = "bedfiles/"+bedFilePrefix+"Y.bed";
        }else if(chromNum == 25){
            fileName = "bedfiles/"+bedFilePrefix+"MT.bed";
        }
        //System.out.println(System.getProperty("user.dir"));
        System.out.println("Loading "+fileName+"...");

      FileReader fileContent = null;
       String absolutePath = workingDir+File.separator+fileName;
        try {
            fileContent = new FileReader(absolutePath);
        } catch (FileNotFoundException ex) {
           ex.printStackTrace();
        }
        
      HashSet<Long> result = new HashSet<Long>();
 
//        Matcher m = p.matcher(p);
//        
//        Matcher m1 = p1.matcher(p2);
       // System.out.println("in MapBedFile...");
        
        if (isNullOrWhitespace(fileContent.toString())) {
            System.out.println("bedfile empty...");
            return null;
        }
        
        BufferedReader reader = new BufferedReader(fileContent);
        String line = null;
       
        String snpID = null;
            
        Long snpStart = null;

        Long snpEnd = null;

        Long scanStart = 0L;

        Long scanEnd = 0L;
  
        boolean Bedeof = false;
        
        boolean listEnd = false;

        boolean endOfChromRange = false;
        
        Iterator<Long[]> itr = chromList.iterator();
 
        
        Long[] range = (Long[]) itr.next();
                
        scanStart = range[0];
                
        scanEnd = range[1];
        
         Long countF = 0L;
        
        while(!Bedeof && !listEnd){
            
            
            
            while(endOfChromRange == false){
                countF++;
                 try {
                       if((line = reader.readLine())==null){
                           Bedeof = true;
                           break;
                       }
                   } catch (IOException ex) {
                      System.out.println("Bed file EOF...");
                   }
                  if(isLineValid(line)==false){
                      
                      continue;
                  }
                  
                  snpID = parseSnpId(line);

                  snpStart = parseSnpStart(line);

                  snpEnd = parseSnpEnd(line);
//                  if(scanStart > 88789510){
//                    System.out.println("snpID: "+snpID);
//                    System.out.println("snpstart: "+String.valueOf(snpStart));
//                    System.out.println("snpEnd: "+String.valueOf(snpEnd));
//                    System.out.println("scanStart: "+String.valueOf(scanStart));
//                    System.out.println("scanEnd: "+String.valueOf(scanEnd));
//                  }  
               
                  
                  if(snpStart >= scanStart && snpEnd <= scanEnd ){
//                     System.out.println(snpID+" added");
                      String temp = snpID.replace("rs", "");
                      
//                      if(snps_notin_ref.contains(temp)){
////                         System.out.println(temp);
//                          System.out.println("SNP not listed in Infi_rs_final list: "+temp+"    chr: "+chromNum+"   snpStart: "+snpStart+"  snpEnd: "+snpEnd+" scanStart: "+scanStart+" scanEnd: "+scanEnd);
//                       
//                      }
                      snpID = snpID.replace("rs","");
                      result.add(Long.parseLong(snpID));
                      
                  } if(snpStart >= scanEnd){
                      
                      endOfChromRange = true;
                  }
                  
            }
            
            while(endOfChromRange){
                
                if(!itr.hasNext()){
                    
                    listEnd = true;
                    break;
                }
                
                 range = (Long[]) itr.next();
                
                scanStart = range[0];
                
                scanEnd = range[1];
                
//              if(scanStart > 88789510){
//                    System.out.println("snpID: "+snpID);
//                    System.out.println("snpstart: "+String.valueOf(snpStart));
//                    System.out.println("snpEnd: "+String.valueOf(snpEnd));
//                    System.out.println("scanStart: "+String.valueOf(scanStart));
//                    System.out.println("scanEnd: "+String.valueOf(scanEnd));
//                  }  
               
                  
                if(snpStart >= scanStart && snpEnd <=scanEnd){
//                    System.out.println(snpID+" added");
                     String temp = snpID.replace("rs", "");
                      
//                      if(snps_notin_ref.contains(temp)){
////                          System.out.println(temp);
//                          System.out.println("SNP not listed in Infi_rs_final list: "+temp+"    chr: "+chromNum+"   snpStart: "+snpStart+"  snpEnd: "+snpEnd+" scanStart: "+scanStart+" scanEnd: "+scanEnd);
//                       
//                      }

                    result.add(Long.parseLong(snpID));
                   
                    
                } if(snpEnd <= scanEnd){
                    endOfChromRange = false;
                }

            }
            if(countF % 1000000 == 0){
            
                System.out.println("Read "+countF+" number of lines...");
            }
        }

        try {
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        try {
            fileContent.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
          System.out.println("Finished reading "+fileName+"...");
        return result;
    }
   
    public boolean isLineValid(String line){
            String chr,start, end, rsNum = null;
            
            Pattern lineP = Pattern.compile("^chr(\\d{1,2}|[mxy]|(mt)).*");
            
            Pattern p = Pattern.compile("^chr(\\d{1,2}|[mxy]|(mt))$");
            
            Pattern p2 = Pattern.compile("^\\d{1,13}$");
            
            Pattern p3 = Pattern.compile("^rs\\d{1,13}$");
            
            Pattern mt = Pattern.compile("^(m|M|mt|MT)$");

            line = line.toLowerCase();
            
            line = line.trim();
            Matcher lineMatch = lineP.matcher(line);
   
            if(!lineMatch.find()){
                //System.out.println("line does not match regex: "+line);
           
                return false;
            }

            String[] data = line.split(bedDelimiter);
            //0 based index
            if(data.length < 4){
                  System.out.println("less than 4 columns in input bed file: "+line);
                  return false;
            }

                    chr = data[0].trim();
                    start = data[1].trim();
                    end = data[2].trim();
                    rsNum = data[3].trim();
                    chr=chr.toLowerCase();

                    Matcher chrMatch = p.matcher(chr);
                    Matcher startMatch = p2.matcher(start);
                    Matcher endMatch = p2.matcher(end);
                    Matcher rsMatch = p3.matcher(rsNum);
    //                    if(!chrMatch.find() || !startMatch.find() || !endMatch.find()){
    //                    
    //                         throw new Exception("Syntax Error in line: "+line);
    //                    }
                    Long snpStart = 0L;

                    Long snpEnd = 0L;

                    if(chrMatch.matches() && startMatch.matches() && endMatch.matches() && rsMatch.matches()){

                        snpStart = Long.parseLong(start);

                        snpEnd = Long.parseLong(end);

                        if(snpStart > snpEnd){
                            System.out.println("chromosome start location greater than or equal to chromosome end location for line: "+line);
                            
                            return false;
                        }
                    }
                
                return true;

    }
    
    public String parseSnpId(String line){
    
        line = line.toLowerCase();
            
        line = line.trim();
    
         String[] data = line.split(bedDelimiter);
    
         String rsNum = data[3].trim();
         
         rsNum = rsNum.replace("rs","");
     
        return rsNum;

    }
    
    public Long parseSnpStart(String line){
 
        line = line.toLowerCase();
            
        line = line.trim();
    
        String[] data = line.split(bedDelimiter);
    
        String start = data[1].trim();
        Long snpStart = Long.parseLong(start);
        return snpStart;
    }
    
    public Long parseSnpEnd(String line){
 
        line = line.toLowerCase();
            
        line = line.trim();
    
        String[] data = line.split(bedDelimiter);
    
        String end = data[2].trim();
        
        Long snpEnd = Long.parseLong(end);
        
        return snpEnd;
    }

      public void writeSnpFile(HashSet<Long> Snps, int fileNum, int chrNum) throws IOException{
        
        String fileName = "snps_"+fileNum+"_"+chrNum+".rs";
        String chrom = "chr"+chrNum;
        FileWriter writer = new FileWriter(fileName, true);       
         
        for(Long snp : Snps){
//            writer.write(String.valueOf(chromNum));
//            writer.write("\t");
            String r = String.valueOf(snp);
            r = r.trim();
            writer.write(r);
            writer.write("\n");
        }
        System.out.println("input file "+fileNum+" snps for chromosome "+chrNum+" added to output file...");
        writer.close();
    }
      

}
    

