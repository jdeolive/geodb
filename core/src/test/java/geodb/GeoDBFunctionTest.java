package geodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

public class GeoDBFunctionTest extends GeoDBTestSupport {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        GeoDB.InitGeoDB(cx);
        
        Statement st = cx.createStatement();
        st.execute("DROP TABLE IF EXISTS spatial");
        st.execute("DELETE FROM geometry_columns");
        
        st.execute("CREATE TABLE spatial (id INT AUTO_INCREMENT PRIMARY KEY, geom BLOB)");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(0 0)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(1 1)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(2 2)', 4326))");
        st.close();
    }
    
    @Test
    public void testSRID() throws Exception {
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_SRID(geom) FROM spatial LIMIT 1");
        rs.next();
        assertEquals(4326, rs.getInt(1));
        
        rs.close();
        st.close();
    }
    
    @Test
    public void testAddGeometryColumn() throws Exception {
        Statement st = cx.createStatement();
        
        st.execute("CALL AddGeometryColumn(NULL,'SPATIAL', 'FOO', -1, 'POINT', 2)");
        
        ResultSet rs = st.executeQuery("SELECT * FROM geometry_columns WHERE " +
            " f_table_name = 'SPATIAL' AND f_geometry_column = 'FOO'");
        assertTrue(rs.next());
        assertEquals("SPATIAL", rs.getString(2));
        assertEquals("FOO", rs.getString(3));
        assertEquals(-1, rs.getInt(4));
        assertEquals(2, rs.getInt(5));
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
    public void testDropGeometryColumn() throws Exception {
        testAddGeometryColumn();
        
        Statement st = cx.createStatement();
        st.executeQuery("SELECT foo FROM spatial");
        
        st.execute("CALL DropGeometryColumn(NULL, 'SPATIAL', 'FOO')");
        try {
            st.executeQuery("SELECT foo FROM spatial");
            fail("column foo should have been deleted");
        }
        catch(SQLException e) {}
        
        ResultSet rs = st.executeQuery("SELECT * FROM geometry_columns WHERE " +
            " f_table_name = 'SPATIAL' and f_geometry_column = 'foo'");
        assertFalse(rs.next());
    }
}
