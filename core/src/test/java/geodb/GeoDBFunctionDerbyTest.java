package geodb;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * <code>GeoDBFunctionDerbyTest</code> runs the tests in
 * {@link TestGeoDBFunction} with Derby.
 */
public class GeoDBFunctionDerbyTest extends TestGeoDBFunction {
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
        cx = DriverManager.getConnection("jdbc:derby:directory:target/"
                + getDatabaseName());
        super.setUp();
    }
}
