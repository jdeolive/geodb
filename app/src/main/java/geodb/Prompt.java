package geodb;

import henplus.HenPlus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Prompt {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: geodb <database>");
            System.exit(-1);
        }
        
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
        String database = args[0];
        args[0] = "jdbc:h2:" + database;
        HenPlus.main(args);
    }
}
