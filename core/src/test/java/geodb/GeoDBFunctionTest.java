package geodb;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public abstract class GeoDBFunctionTest extends GeoDBTestSupport {
    /**
     * The name of the test database.
     * 
     * @return the database name.
     */
    public static String getDatabaseName() {
        return "geodb_function";
    }

    @Before
    public void setUp() throws Exception {
        Connection cx = getConnection();
        GeoDB.InitGeoDB(cx);
        
        Statement st = cx.createStatement();
        dropTable(st, "spatial");
        dropTable(st, "spatial2");
        st.execute("DELETE FROM geometry_columns");

        createTable(st, "spatial", "id", "geom");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(0 0)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(1 1)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(2 2)', 4326))");
        st.close();
    }
    
    @Test
    public void testSRID() throws Exception {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_SRID(geom) FROM spatial " + getTestUtils().getLimitClauseSql(1));
        rs.next();
        assertEquals(4326, rs.getInt(1));
        
        rs.close();
        st.close();
    }
    
    @Test
    public void testAddGeometryColumn() throws Exception {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        
        st.execute("CALL AddGeometryColumn(NULL,'SPATIAL', 'FOO', -1, 'POINT', 2)");
        
        ResultSet rs = st.executeQuery("SELECT * FROM geometry_columns WHERE " +
            " f_table_name = 'SPATIAL' AND f_geometry_column = 'FOO'");
        assertTrue(rs.next());
        assertEquals("SPATIAL", rs.getString(2));
        assertEquals("FOO", rs.getString(3));
        assertEquals(2, rs.getInt(4));
        assertEquals(-1, rs.getInt(5));
        assertEquals("POINT", rs.getString(6));
        
        assertFalse(rs.next());
        rs.close();
        
        st.execute("INSERT INTO spatial (foo) VALUES (ST_GeomFromText('POINT(0 0)',-1))");
        try {
            st.execute("INSERT INTO spatial (foo) VALUES (ST_GeomFromText('LINESTRING(0 0, 1 1)',-1))");
            fail("inserting non point should have failed");
        }
        catch(SQLException e) {}
    }

    @Test
    public void testAddGeometryColumn2() throws Exception {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        createTable(st, "spatial2", "id", null);
        st.execute("CALL AddGeometryColumn(null, 'SPATIAL2', 'GEOM', 4326, 'POINT', 2)");

        ResultSet rs = st.executeQuery("SELECT * from geometry_columns");
        assertTrue(rs.next());

        /*
        f_table_catalog   | character varying(256) | not null
        f_table_schema    | character varying(256) | not null
        f_table_name      | character varying(256) | not null
        f_geometry_column | character varying(256) | not null
        coord_dimension   | integer                | not null
        srid              | integer                | not null
        type              | character varying(30)  | not null
        */
        assertEquals("SPATIAL2", rs.getString("f_table_name"));
        assertEquals("GEOM", rs.getString("f_geometry_column"));
        assertEquals(2, rs.getInt("coord_dimension"));
        assertEquals(4326, rs.getInt("srid"));
        assertEquals("POINT", rs.getString("type"));
        
        rs.close();
        st.close();
    }

    @Test
    public void testDropGeometryColumn() throws Exception {
        testAddGeometryColumn();
        
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        st.executeQuery("SELECT foo FROM spatial");
        
        st.execute("CALL DropGeometryColumn(NULL, 'SPATIAL', 'FOO')");
        try {
            st.executeQuery("SELECT foo FROM spatial");
            fail("column foo should have been deleted");
        }
        catch(SQLException e) {}
        
        ResultSet rs = st.executeQuery("SELECT * FROM geometry_columns WHERE " +
            " f_table_name = 'SPATIAL' and f_geometry_column = 'FOO'");
        assertFalse(rs.next());
    }
    
    @Test
    public void testDropGeometryColumns() throws Exception {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        
        st.execute("CALL AddGeometryColumn(NULL,'SPATIAL', 'FOO', -1, 'POINT', 2)");
        st.execute("CALL AddGeometryColumn(NULL,'SPATIAL', 'BAR', -1, 'POINT', 2)");
        
        st.executeQuery("SELECT foo, bar FROM spatial");
        
        st.execute("CALL DropGeometryColumns(NULL, 'SPATIAL')");
        try {
            st.executeQuery("SELECT foo FROM spatial");
            fail("column foo should have been deleted");
        }
        catch(SQLException e) {}
        try {
            st.executeQuery("SELECT bar FROM spatial");
            fail("column foo should have been deleted");
        }
        catch(SQLException e) {}
        
        ResultSet rs = st.executeQuery("SELECT * FROM geometry_columns WHERE " +
            " f_table_name = 'SPATIAL' and f_geometry_column IN ('FOO','BAR')");
        assertFalse(rs.next());
    }
    
    @Test
    public void testDistance() throws Exception {
        WKTReader wkt = new WKTReader();
        Geometry g1 = wkt.read("POINT(12123.343 79586.125)");
        Geometry g2 = wkt.read("POINT(90711.7123 56791.89)");
        double dist = g1.distance(g2);
        
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery(
            "VALUES ST_Distance(ST_GeomFromText('POINT(12123.343 79586.125)',-1), " +
                            " ST_GeomFromText('POINT(90711.7123 56791.89)',-1))");
        rs.next();
        double result = rs.getDouble(1);
        assertEquals(dist, result, 0.00001);
        
    }
    
    @Test
    public void testST_X() throws Exception {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery(
            "VALUES ST_X(ST_GeomFromText('POINT(12123.343 79586.125)',-1))");
        rs.next();
        double result = rs.getDouble(1);
        assertEquals(12123.343, result, 0.00001);
    }
    
    @Test
    public void testST_Y() throws Exception {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery(
            "VALUES ST_Y(ST_GeomFromText('POINT(12123.343 79586.125)',-1))");
        rs.next();
        double result = rs.getDouble(1);
        assertEquals(79586.125, result, 0.00001);
    }
}
