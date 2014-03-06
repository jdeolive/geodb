package geodb;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * <code>GeoDBExtraFunctionsDerbyTest</code> runs the tests in
 * {@link GeoDBExtraFunctionsTest} with Derby.
 */
public class GeoDBExtraFunctionsDerbyTest extends GeoDBExtraFunctionsTest {
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
    public void setup() throws Exception {
        cx = DriverManager.getConnection("jdbc:derby:directory:target/"
                + getDatabaseName());
        super.setup();
    }
}
