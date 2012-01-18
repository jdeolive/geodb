package geodb.aggregate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import geodb.GeoDB;
import geodb.GeoDBTestSupport;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.WKBReader;

public class AggregateFunctionTest extends GeoDBTestSupport {

    @Before
    public void setup() throws Exception {
        super.setUp();
        
        Statement st = cx.createStatement();
        st.execute("DROP TABLE IF EXISTS _GEODB");
        st.execute("DROP TABLE IF EXISTS spatial");
        st.execute("DROP TABLE IF EXISTS spatial_hatbox");
        st.execute("DROP TABLE IF EXISTS noindex");
        st.close();
        GeoDB.InitGeoDB(cx);
      
        st = cx.createStatement();
        st.execute("CREATE TABLE spatial (id INT AUTO_INCREMENT PRIMARY KEY, geom BLOB)");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(0 0)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(1 1)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(2 2)', 4326))");
        st.close();
    }
 
    @Test
    public void testExtent() throws Exception {
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("select st_extent(geom) from spatial");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        st.close();
        assertThat(geometry.getArea(), is(4.0));
    }

    @Test
    public void testUnion() throws Exception {
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("select st_union_aggregate(geom) from spatial");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        st.close();
        assertThat(geometry.getArea(), is(0.0));
    }


}
