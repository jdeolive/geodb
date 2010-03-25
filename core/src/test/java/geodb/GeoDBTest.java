package geodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

public class GeoDBTest extends GeoDBTestSupport {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        Statement st = cx.createStatement();
        st.execute("DROP TABLE IF EXISTS _GEODB");
        st.execute("DROP TABLE IF EXISTS spatial");
        st.execute("DROP TABLE IF EXISTS spatial_hatbox");
        st.execute("DROP TABLE IF EXISTS noindex");
        st.close();
    }
    
    @Test
    public void testInitDB() throws Exception {
        ResultSet tables = cx.getMetaData().getTables(null, null, "_GEODB", new String[] {"TABLE"});
        assertFalse(tables.next());
        
        GeoDB.InitGeoDB(cx);
        tables = cx.getMetaData().getTables(null, null, "_GEODB", new String[] {"TABLE"});
        assertTrue(tables.next());
        
        tables.close();
    }
    
    @Test
    public void testCreateSpatialIndex() throws Exception {
        GeoDB.InitGeoDB(cx);
        Statement st = cx.createStatement();
      
        st.execute("CREATE TABLE spatial (id INT AUTO_INCREMENT PRIMARY KEY, geom BLOB)");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(0 0)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(1 1)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(2 2)', 4326))");
        st.close();
        
        ResultSet tables = 
            cx.getMetaData().getTables(null, null, "SPATIAL_HATBOX", new String[] {"TABLE"});
        assertFalse(tables.next());
        GeoDB.CreateSpatialIndex(cx, null, "SPATIAL", "GEOM", "4326");
        
        tables = 
            cx.getMetaData().getTables(null, null, "SPATIAL_HATBOX", new String[] {"TABLE"});
        assertTrue(tables.next());
        st.close();
    }
 
    @Test
    public void testGetSRID() throws Exception {
        Statement st = cx.createStatement();
        st.execute("CREATE TABLE spatial (id INT AUTO_INCREMENT PRIMARY KEY, geom BLOB)");
        st.execute("CALL CreateSpatialIndex(null, 'SPATIAL', 'GEOM', '4326')");
        assertEquals(4326, GeoDB.GetSRID(cx, null, "SPATIAL"));
        
        st.execute("CREATE TABLE noindex (id INT AUTO_INCREMENT PRIMARY KEY, geom BLOB)");
        assertEquals(-1, GeoDB.GetSRID(cx, null, "NOINDEX"));
        
        st.close();
    }

}
