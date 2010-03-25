package geodb;

import java.sql.Connection;
import java.sql.DriverManager;

import org.h2.tools.DeleteDbFiles;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class GeoDBTestSupport {

    protected Connection cx;
    
    @BeforeClass
    @AfterClass
    public static void destroyDB() throws Exception {
        DeleteDbFiles.execute(".", "geodb", true);
    }
    
    @Before
    public void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        cx = DriverManager.getConnection("jdbc:h2:geodb");
    }
    
    @After
    public void tearDown() throws Exception {
        cx.close();
    }
}
