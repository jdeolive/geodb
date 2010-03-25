package geodb;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
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
}
