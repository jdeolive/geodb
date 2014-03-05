package geodb.aggregate;

import geodb.DatabaseTestUtils;
import geodb.H2TestUtils;
import geodb.TestGeoDBFunction;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * <code>AggregateFunction</code> runs the tests in {@link TestGeoDBFunction}
 * with H2.
 */
public class AggregateFunctionH2Test extends TestAggregateFunction {
    /** The H2 test utilities. */
    private static final H2TestUtils TEST_UTILS = new H2TestUtils();

    /** The H2 database connection instance. */
    private Connection cx;

    @Override
    protected Connection getConnection() {
        return cx;
    }

    @Override
    protected DatabaseTestUtils getTestUtils() {
        return TEST_UTILS;
    }

    @BeforeClass
    @AfterClass
    public static void destroyDB() throws Exception {
        TEST_UTILS.destroyDB(getDatabaseName());
    }

    @Before
    public void setup() throws Exception {
        Class.forName("org.h2.Driver");
        cx = DriverManager.getConnection("jdbc:h2:target/" + getDatabaseName());
        super.setup();
    }
}
