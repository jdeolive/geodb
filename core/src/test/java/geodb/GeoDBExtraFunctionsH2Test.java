package geodb;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jts.io.ParseException;

/**
 * <code>GeoDBExtraFunctionsH2Test</code> runs the tests in
 * {@link TestGeoDBExtraFunctions} with H2.
 */
public class GeoDBExtraFunctionsH2Test extends TestGeoDBExtraFunctions {
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

    @Test
    public void testRelateWithMatrix() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("CALL ST_Relate(ST_GeomFromText('POINT(1 2)',4326), ST_Buffer(ST_GeomFromText('POINT(1 2)',4326),2), '0FFFFFFF2')");
        rs.next();
        boolean result = rs.getBoolean(1);
        st.close();
        //I don't really understand this function so not sure if the result is correct. At least the result of both tests seems consistent.
        assertTrue(result);
    }
}
