package geodb.aggregate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import geodb.GeoDB;
import geodb.GeoDBTestSupport;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.WKBReader;

public abstract class TestAggregateFunction extends GeoDBTestSupport {
    /**
     * The name of the test database.
     * 
     * @return the database name.
     */
    public static String getDatabaseName() {
        return "geodb_aggregate_function";
    }

    @Before
    public void setup() throws Exception {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        dropTable(st, GeoDB.getGeoDBTableName(cx));
        dropTable(st, "spatial");
        dropTable(st, "spatial_hatbox");
        dropTable(st, "noindex");
        st.close();
        GeoDB.InitGeoDB(cx);
      
        st = cx.createStatement();
        createTable(st, "spatial", "id", "geom");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(0 0)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(1 1)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(2 2)', 4326))");
        st.close();
    }
 
    @Test
    public void testExtent() throws Exception {
        Connection cx = getConnection();
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
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("select st_union_aggregate(geom) from spatial");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        st.close();
        assertThat(geometry.getArea(), is(0.0));
    }
}
