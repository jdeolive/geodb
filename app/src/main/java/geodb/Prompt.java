package geodb;

import henplus.HenPlus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.h2.tools.Server;

public class Prompt {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsageAndExit();
        }
        
        List<String> params = new ArrayList(Arrays.asList(args));
        boolean web = false;
        for (Iterator<String> i = params.iterator(); i.hasNext();) {
            String arg = i.next();
            if ("-w".equals(arg) || "--web".equals(arg)) {
                i.remove();
                web = true;
            }
        }
      
        if (web) {
            runAsWeb();
        }
        else {
            if (params.isEmpty()) {
                printUsageAndExit();
            }
            
            String database = params.get(params.size()-1);
            runAsCommandLine(database);
        }
    }
    
    static void printUsageAndExit() {
        System.out.println("Usage: geodb [options] <database>");
        System.out.println();
        System.out.println("Options:");
        System.out.println("\t --web, -w\t\trun as web application");
        System.exit(-1);
    }
    
    static void runAsWeb() throws Exception {
        Server.main(new String[]{});
    }
    
    static void runAsCommandLine(String database) throws Exception {
        String home = System.getProperty("user.home");
        
        //find henplus configuration directory
        File hpdir = new File(home, ".henplus");
        if (!hpdir.exists()) {
            if (!hpdir.getParentFile().canWrite()) {
                System.out.println("ERROR: Can not write to home directory");
                System.exit(-1);
            }
            
            hpdir.mkdir();
        }
        
        //load the driver configuration file
        File hpconfig = new File(hpdir, "drivers");
        if (!hpconfig.exists()) {
            hpconfig.createNewFile();
        }
        if (!hpconfig.exists()) {
            System.out.println("ERROR: Could not create HenPlus configuration");
            System.exit(-1);
        }
        
        Properties hpprops = new Properties();
        FileInputStream fin = new FileInputStream(hpconfig);
        hpprops.load(fin);
        fin.close();
        
        if (!hpprops.containsKey("driver.h2.class") ) {
            hpprops.put("driver.h2.class", "org.h2.Driver");
            FileOutputStream fout = new FileOutputStream(hpconfig);
            hpprops.store(fout, "JDBC drivers");
            fout.close();
        }
        
        //start henplus
        HenPlus.main(new String[]{"jdbc:h2:" + database});
    }
}
