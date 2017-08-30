/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daleylab.service;

import com.daleylab.AppConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 *
 * @author JLee05
 */
@Service
@PropertySource({"classpath:application.properties"})
public class ProcessBed {

//    @Autowired
//    SnpSearchService snpSearch;
    @Value("${input.file.delimiter}")
    private String chrDelimiter;
    @Value("${input.file.chrom.column}")
    private int chrColumn;
    @Value("${bed.file.delimiter}")
    private String bedDelimiter;
    @Value("${bed.filename.prefix}")
    private String bedFilePrefix;
    @Value("${outputfile}")
    private String outputFileName;
    final static Logger logger = Logger.getLogger("ProcessFile");

    public HashMap<Integer, ArrayList<Long[]>> GetListOfChromLocations(FileReader fileContent) {

        HashMap<Integer, ArrayList<Long[]>> rangeList = new HashMap<Integer, ArrayList<Long[]>>();

        Pattern p = Pattern.compile("^chr(\\d{1,2}|[mxy]|(mt))$");
        Pattern mt = Pattern.compile("^(m|M|mt|MT)$");
        Pattern p2 = Pattern.compile("^\\d{1,10}$");

//        Matcher m = p.matcher(p);
//        
//        Matcher m1 = p1.matcher(p2);
        logger.info("in GetListOfChromLocations...");

        if (!isNullOrWhitespace(fileContent.toString())) {

            BufferedReader reader = new BufferedReader(fileContent);

            String line;

            String chr;
            String start;
            String end;
            try {
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    String[] data = line.split(chrDelimiter);

                    //0 based index
                    if (data.length < 3) {
                        throw new Exception("Less than 3 columns in input file: " + line);
                    }
                    chr = data[chrColumn];
                    start = data[1];
                    end = data[2];
                    chr = chr.toLowerCase();
//                    logger.info("line: "+line);
//                    logger.info("chr: "+chr);
//                    logger.info("start: "+start);
//                    logger.info("end: "+end);

                    Matcher chrMatch = p.matcher(chr);
                    Matcher startMatch = p2.matcher(start);
                    Matcher endMatch = p2.matcher(end);

                    if (!chrMatch.find() || !startMatch.find() || !endMatch.find()) {

                        throw new Exception("Syntax Error in line: " + line);
                    }

//                    if(chrMatch.matches() && startMatch.matches() && endMatch.matches()){
                    Long chrStart = Long.parseLong(start);

                    Long chrEnd = Long.parseLong(end);

                    int key = 0;

                    chr = chr.replace("chr", "");
                    //for mitochondrial prefix
                    Matcher mitoMatch = mt.matcher(chr);

                    if (chr.equalsIgnoreCase("x")) {
                        key = 23; //x chromosome assigned 23
                    } else if (chr.equalsIgnoreCase("y")) {
                        key = 24;
                    } else if (mitoMatch.matches()) {
                        key = 25;
                    } else {
                        key = Integer.parseInt(chr);
                    }

                    Long[] tmp = {chrStart, chrEnd};

                    //arraylist for chromosome[key] not instantiated. Making a new arraylist
                    if (rangeList.get(key) == null) {

                        ArrayList<Long[]> newChrList = new ArrayList<Long[]>();

                        newChrList.add(tmp);

                        rangeList.put(key, newChrList);
                    } else {
                        rangeList.get(key).add(tmp);
                    }

//                         logger.info("final parse...");
//                         logger.info("chr: "+chr);
//                         logger.info("start: "+start);
//                         logger.info("end: "+end);
                    if (chrStart >= chrEnd) {
                        throw new Exception("chromosome start location greater than or equal to chromosome end location for line: " + line);
                    }

                }
//                }
            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        System.out.println("Finished parsing chromosome location file...");

        for (int idx = 1; idx <= 24; idx++) {

            ArrayList<Long[]> list = rangeList.get(idx);
            if (list != null) {

                Collections.sort(list, new Comparator<Long[]>() {
                    @Override
                    public int compare(Long[] l1, Long[] l2) {
                        return l1[1].compareTo(l2[1]);
                    }

                });

            }

        }

        return rangeList;
    }

    public void MatchChromToSnp(HashMap<Integer, ArrayList<Long[]>> chromLocs1, HashMap<Integer, ArrayList<Long[]>> chromLocs2) throws FileNotFoundException, IOException, InterruptedException {

        File snpsFile1 = new File("snps1.rs");
        File snpsFile2 = new File("snps2.rs");
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) context.getBean("taskExecutor");

        if (snpsFile1.exists()) {

            snpsFile1.delete();
        }
        if (snpsFile2.exists()) {

            snpsFile2.delete();
        }
        //chromosome 1-22 bad hack-> x chromosome == 23 and y chromosome == 24 mito.chromosome == 25
        for (int idx = 1; idx < 26; idx++) {

            ArrayList<Long[]> list1 = chromLocs1.get(idx);

            if (list1 != null && !list1.isEmpty()) {

                taskExecutor.execute(new mapSnpInBed(list1, 1, idx));
   
            }

        }

        while (taskExecutor.getActiveCount() > 0) {
            Thread.sleep(1000);
        }

        writeResultFile(1);

        for (int idx = 1; idx < 26; idx++) {

            ArrayList<Long[]> list2 = chromLocs2.get(idx);

            if (list2 != null && !list2.isEmpty()) {

                taskExecutor.execute(new mapSnpInBed(list2, 2, idx));

            }

        }

        while (taskExecutor.getActiveCount() > 0) {
            Thread.sleep(1000);
        }

        writeResultFile(2);

        taskExecutor.shutdown();
    }

    public void writeResultFile(int inputNum) throws IOException {
        String outputFileName = "snps" + inputNum + ".rs";
        FileWriter writer = new FileWriter(outputFileName, true);
        for (int chromNum = 1; chromNum < 26; chromNum++) {

            String fileName = "snps_" + inputNum + "_" + chromNum + ".rs";
            String workingDir = System.getProperty("user.dir");

            FileReader fileContent = null;

            String absolutePath = workingDir + File.separator + fileName;
            File tmp = new File(absolutePath);

            if (!tmp.exists()) {

                continue;

            }
            try {
                fileContent = new FileReader(absolutePath);

            } catch (FileNotFoundException ex) {

                //ex.printStackTrace();
                System.out.println(absolutePath + " not found...");
            }

            BufferedReader reader = new BufferedReader(fileContent);

            String line = null;
            while ((line = reader.readLine()) != null) {

                writer.write(line);
                writer.write("\n");

            }
            reader.close();
            tmp.delete();

        }

        writer.close();
    }

    public void writeSnpFile(HashSet<String> Snps, int fileNum, int chrNum) throws IOException {

        String fileName = "snps" + fileNum + "_" + chrNum + ".rs";
        String chrom = "chr" + chrNum;
        FileWriter writer = new FileWriter(fileName, true);

        for (String snp : Snps) {
//            if(snp.equalsIgnoreCase("rs10030708")){
//                System.out.println("writing to file: "+snp);
//            }
//            
            String row = snp.replace("rs", "");

            row.trim();

            writer.write(row);
            writer.write("\n");
        }
        System.out.println("input file " + fileNum + " snps for chromosome " + chrNum + " added to output file...");
        writer.close();
    }

    public static boolean isNullOrWhitespace(String s) {
        return s == null || isWhitespace(s);

    }

    public static boolean isWhitespace(String s) {
        int length = s.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public FileReader openNewFile(String fileName) {
        Long fileRowCount = 0L;
        FileReader in = null;
        try {

            File temp = new File(fileName);

            in = new FileReader(temp);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return in;
    }

}
