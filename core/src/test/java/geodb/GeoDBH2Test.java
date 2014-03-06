package geodb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * <code>GeoDBH2Test</code> runs the tests in {@link GeoDBTest} with H2.
 */
public class GeoDBH2Test extends GeoDBTest {
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

    @Override
    protected void createInitGeoDBProcedure(Statement st) throws SQLException {
        ResultSet rs = st
                .executeQuery("SELECT * FROM INFORMATION_SCHEMA.FUNCTION_ALIASES "
                        + "WHERE alias_schema IN (schema(), 'PUBLIC') "
                        + "AND alias_name = 'INITGEODB'");
        boolean functionExists = rs.next();
        rs.close();
        if (!functionExists) {
            st.execute("CREATE ALIAS InitGeoDB for \"geodb.GeoDB.InitGeoDB\"");
        }
    }

    @BeforeClass
    @AfterClass
    public static void destroyDB() throws Exception {
        TEST_UTILS.destroyDB(getDatabaseName());
    }

    @Before
    public void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        cx = DriverManager.getConnection("jdbc:h2:target/" + getDatabaseName());
        super.setUp();
    }
}
