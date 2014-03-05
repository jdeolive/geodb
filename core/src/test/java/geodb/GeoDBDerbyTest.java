package geodb;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Tests <code>GeoDB</code> with H2.
 */
public class GeoDBDerbyTest extends TestGeoDB {
    /** The database connection instance. */
    protected Connection cx;

    /** The Derby test utilities. */
    private static DerbyTestUtils testUtils = new DerbyTestUtils();

    @Override
    protected Connection getConnection() {
        return cx;
    }

    @Override
    protected DatabaseTestUtils getTestUtils() {
        return testUtils;
    }

    @BeforeClass
    public static void createDB() throws Exception {
        testUtils.destroyDB(getDatabaseName());
        testUtils.createDB(getDatabaseName());
    }

    @AfterClass
    public static void destroyDB() throws Exception {
        testUtils.destroyDB(getDatabaseName());
    }

    @Before
    public void setUp() throws Exception {
        cx = DriverManager.getConnection("jdbc:derby:directory:target/" + getDatabaseName());
        super.setUp();
    }
}
