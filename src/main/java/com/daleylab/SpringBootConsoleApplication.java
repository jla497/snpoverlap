package com.daleylab;

/**
 * Main function for reading and parsing chromosome locations then outputs SNPs found in those locations.
 * @author JLee05
 */
import com.daleylab.service.ProcessBed;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
@Configuration
@ComponentScan
public class SpringBootConsoleApplication implements CommandLineRunner {

    @Autowired
    private ProcessBed processBed;

    public static void main(String[] args) throws Exception {

        //disabled banner, don't want to see the spring logo
        SpringApplication app = new SpringApplication(SpringBootConsoleApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);

        //SpringApplication.run(SpringBootConsoleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        if (args.length == 2 ) {
            FileReader in = null;
            FileReader in2 = null;
            FileReader in3 = null;
            HashMap<Integer,HashSet<String>> snpList1 = null;
            HashMap<Integer,HashSet<String>> snpList2 = null;
           
            try{
            
                File temp = new File(args[0].toString());
                
                in = new FileReader(temp);
                
            }catch(Exception e){
                e.printStackTrace();
            }
           
            
             try{
            
                File temp = new File(args[1].toString());
                
                in2 = new FileReader(temp);
                
            }catch(Exception e){
                e.printStackTrace();
            }
            
           /*Read and parse all chromosome locations. Ouputs hashMap<int,arrayList>*/
            try{
            HashMap<Integer,ArrayList<Long[]>> locs1 = processBed.GetListOfChromLocations(in);
             
            HashMap<Integer,ArrayList<Long[]>> locs2 = processBed.GetListOfChromLocations(in2);
            
           //find snps for the list of chromosome ranges in locs1 and locs2    
            processBed.MatchChromToSnp(locs1,locs2);
            }catch(Exception e){
                
                e.printStackTrace();
                
                System.out.println("It appears there is an issue with your input files. Input file should have the following header columns: "
                                    + "|chr|chrom_start|chrom_end|...\n"
                                    + "Bed files should have the following header columns: "
                                    +"|chr|snp_chromosome_start|snp_chromosome_end|snpId|...");
            }
            
        }else{
        
        System.out.println("Incorrect number of arguments.... arg[0] firstFileName arg[1] secondFileName");
        }

        exit(0);
    }
}