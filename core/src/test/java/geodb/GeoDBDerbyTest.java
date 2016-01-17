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
 * <code>GeoDBDerbyTest</code> runs the tests in {@link GeoDBTest} with Derby.
 */
public class GeoDBDerbyTest extends GeoDBTest {
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

    @Override
    protected void createInitGeoDBProcedure(Statement st) throws SQLException {
        // Ensure that the procedure does not already exist.
        ResultSet rs = st.executeQuery("SELECT * FROM SYS.SYSALIASES a "
                + "INNER JOIN SYS.SYSSCHEMAS s "
                + "ON s.SCHEMANAME = CURRENT SCHEMA "
                + "AND a.ALIAS = 'INITGEODB'");
        boolean procedureExists = rs.next();
        rs.close();
        if (!procedureExists) {
            st.execute("CREATE PROCEDURE InitGeoDB () "
                    + "language java external name 'geodb.GeoDB.InitGeoDBProc' "
                    + "parameter style java modifies sql data");
        }
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
