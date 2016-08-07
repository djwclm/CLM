/*
 * MeteringV0R1.java
 * Metering Application, Version 0, release 0
 *   updated july 11, 2014 Version 0 release 1 to allow alt log file input
 *   ipdated July 14 to use 2014 EUR7-1.log as default as this file is > 8 hrs
 *   takes .log file and creates a new .now file with current date and time
 *  Updated July 29, 2014 to use the Bernicke log file (source Al F.)  EUR9-18.log
 *  located in ~/Desktop/octave/CLMData/EUR9-18.log  
 *  (and replaciated in dir Bernicke_Log_file dir)
 * Java ref [1] used Java Phrasebook Timonthy Fisher Developer's Library @ 2007
 *  lib of congress cat card # 2006921449
 */

package meteringV0R1;
import java.util.HashMap;
import java.util.Properties;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import javax.swing.Timer; //  to resolve conflict with java.util.Timer   

/**
 * @version 0 Release 0
 * @author djwj
 */
public class MeteringV0R1  extends Thread   {
    // General boiler plate info
    // thread def required to use timing such as sleep()
    
    public static long waitMS = 1000; // number of milisec to wait to target 1 sec rate
    // Note, relative paths are added to userDir for full path
    public static String DD_Loc;       // relative path to data drop loc of .log file
    // I.E.  ~/Desktop/octave/CLMData/EUR6-24.log
    // following is default loc, if input is empty (as in testing)
 //   public static String default_log = "/Desktop/octave/CLMData/EUR9-18.log";
 //    public static String default_log = "/Desktop/octave/CLMData/EUR12-12.log";
    public static String default_log = "/Desktop/octave/CLMData/XEUR2-13.log";   
    public static String MS_Loc;       // relative path to current Metering Simul. loc
    // I.E.  ~/Desktop/octave/CLMData/EUR7-1.now  on July 1
    public static boolean debug = true; // set true for debuging info
    public Properties defaultsettings = new Properties();
    public static String datelabel;  // stores date as mm-dd ; used in file names
    public static String FileLabel;  // Selects EUR or GBP
    /**
     * @param args the command line arguments
     * log_Loc  is the relative path to the input log file 
     * (i.e. /Desktop/octave/CLMData/EUR6-24.log
     * if not specified (as in NetBeans testing) set to  above example
     */
    public static void main(String[] args) {
        
            String log_loc;
            if(args.length < 1){
                // no location set, so use default
                log_loc = default_log;
            } else{
                log_loc = args[0]; //"/Desktop/octave/CLMData/EUR6-24.log"
            };
            
            // determine if GBP or EUR
            FileLabel = "";
            int result1, result2;
            result1 = log_loc.indexOf("EUR");
            if (result1 > -1 ) {  FileLabel = "EUR" ;};
            result2 = log_loc.indexOf("GBP");
            if(result2 > -1 ){ FileLabel = "GBP";};
            
            String userDir = System.getProperty("user.home");
            String localOS = System.getProperty("os.name");
            String localArch = System.getProperty("os.arch");
            String localVersion = System.getProperty("os.version");
        
            String temp = localOS.substring(0,4);
            String  platformUsed = temp.trim(); 
            //bm platformUsed => Mac, Wind, Mint, or Linu (linux) 

            
            MS_Loc = userDir + "/Desktop/octave/CLMData/"; // for Mint16
            //?? above needs to be fixed so it is based on arch. 
            
            ///// The date/Time need to be current when reading file
            /// This set is to create file labels
            /// also used inside read loop!!!!
            Date nowDate = new Date();  // When date is set
            /// for both Date and Time
            String DTG = String.format("%tD ",nowDate)+String.format(
                                                        "%tT",nowDate);
            /// for just Time
            String DTG2 = String.format("%tT",nowDate);
            ///// if rewrite MATLAB input code could drop DTG2 
        
            ///////////////////
            ////// build datelabel for use in labeling files
            StringBuilder builder = new StringBuilder();
            // used in generation of date label
            // create date label
            Date fileDate = new Date();  // When date is set for file name
            String datelab = String.format("%tD",fileDate);//results in mm/dd/yy
            String datelab2 = datelab.substring(0,5);
            datelabel = datelab2.replace("/","-");
            ////bm PATCH datelabel to match microsoft/matlab name std format
            ////   01-06 => 1-6  Warning, this is a non std coll. seq.
            ////    and will impact sorting
            /////////////////////// mod 10/10/2014 - djwj

            StringBuilder  xx=builder.append(datelabel); // setup
            /////// WARNING the mod order is important, change at your risk
            //// rewrite on 10/01/2014 in DD_OE_V1M3
            //// reused on MeteringV0R1 on 10/10/2014

     
            if(datelabel.substring(3,4).contains("0"))
            {   
                /// mm-dd => need to dump 0's in 0x-0y
                /// 01234  this is "char" position
                xx.deleteCharAt(3);   // 
            };
            // String MChar = datelabel.substring(0,1);
            //  if( MChar.equals("0")) 
     
            if((datelabel.substring(0,1)).contains("0"))
            {   
                xx.deleteCharAt(0);   // 
            };
            // replace corrected date
            datelabel = xx.toString();
            System.out.println(" Lines 98-126 LABEL FOR DATE :" + datelabel);
        ////////////////////////////////// end of patch
        


//////
        
        

        
            if (debug){
                System.out.println(" debug is set true ************");
                System.out.println(" debug : user home dir is :" + userDir);
                System.out.println(" debug : local OS is      :" + localOS);
                System.out.println(" debug : local arch. is   :" + localArch);
                System.out.println(" debug : local OS ver. is :" + localVersion);
                System.out.println(" debug : log_loc is       :" + log_loc);
                System.out.println(" debug : DTG is           :" + DTG);
                System.out.println(" debug : DTG2 is          :" + DTG2);
                System.out.println(" debug : Now File date is :" + datelabel);
                System.out.println(" debug end *****************");
            }
        
            // The following is to read the log file in reference dir under DD_Loc
         
            // opening  file for reading
            // note exception handling
            String lineIn;
            String lineOut;
        
            try{
                BufferedReader inLog = 
                new BufferedReader(new FileReader((userDir + log_loc)));
        
                while( (lineIn = inLog.readLine())!= null)
                {   // process one line a
                    //  GET current Time (perhaps date too?)
                    // MUST update DTG and DTG2 while in read loop
                    // overwrite date and time with current values
                    Date currDate = new Date();  // When date is set
                    /// for both Date and Time
                    String DTGcurr = String.format("%tD ",currDate)+String.format(
                                                        "%tT",currDate);
                    /// for just Time
                    String DTG2curr = String.format("%tT",currDate);
                    // using StringTokenizer (instead of split)
                    /// ref [1] p 27
                    StringTokenizer st = new StringTokenizer(lineIn);
                    // as called with one param, the 
                    // defaut delimiter is " \t\n\r\f"
                    // go to inline code as JAVA and Strings is a B***H
              
                    String tt0, tt1, tt2, tt3,tt4, tt5, tt6, tt7, tt8, tt9;
                
                    tt0 = st.nextToken() ;      // Org MM/DD/YY (replace)
                    tt1 = st.nextToken() ;      // org HH:mm:ss (replace)
                    tt2 = st.nextToken() + " "; // org data 
                    tt3 = st.nextToken() + " "; // org data 
                    tt4 = st.nextToken() + " "; // org data 
                    tt5 = st.nextToken() + " "; // org data 
                    tt6 = st.nextToken() + " "; // org data 
                    tt7 = st.nextToken() ;      // org HH:mm:ss (replace)
                    tt8 = st.nextToken() + " "; // org data 
                    tt9 = st.nextToken() + " "; // org data 
                    // now have input line decomposed
                    // re assemble , DTG is first 2 replacements
                    lineOut = DTGcurr + " "  + tt2 + tt3 + tt4 + tt5 + tt6 
                               + DTG2curr  + " " + tt8 + tt9;
                    
                    if(debug){
                        System.out.print("debug:           ");
                        System.out.println("          1         2         3         4");
                        System.out.print("debug:           ");
                        System.out.println("01234567890123456789012345678901234567890");
                        System.out.println("debug: org line: "+lineIn);
                       System.out.println("debug: mod line: "+lineOut);
                    }
            
                    // so now place in a new dir as .now
                    ///
                    // using same code as used in orgional java DD 
                    // Only writing the  .out file
                
                    /// WARNING windows os may not be reporting userDir properly?
                    //    use with care
                    // Based on input log name as contained in FileLabel , 
                    //  chose EUR or GBP as listed in FileLabel
                    String nowPath;
                    nowPath = MS_Loc + FileLabel +datelabel + ".now";
                    Path NowPath = Paths.get(nowPath);   
                    File NowFile = NowPath.toFile();     
                    if(debug) {
                        System.out.println(" debug : New .now file is :" + NowFile);
                    }
                    // try printing (Converts unicode to char data) now files, 
 
                    try(PrintWriter Nowout = new PrintWriter(
                                     new BufferedWriter(
                                     new FileWriter(NowFile)))){
                        Nowout.println(lineOut);  ///output
                    }catch (IOException ioe){
                        System.out.println(" ERROR IN Nowout " + ioe);
                    };
                    sleep(waitMS);
                };
    
            } catch(IOException ioe2) {
                System.out.println(" ERROR IN Nowout " + ioe2);
            } catch(InterruptedException iep) {
                System.out.println(" ERROR IN Nowout " + iep);
            };
            //  need to close out files and stop
            
        }; // close out of main
    }; // end of MeteringV0R1