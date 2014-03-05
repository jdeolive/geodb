package geodb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

public abstract class TestGeoDBExtraFunctions extends GeoDBTestSupport {
    /**
     * The name of the test database.
     * 
     * @return the database name.
     */
    public static String getDatabaseName() {
        return "geodb_extra_functions";
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
    }
 
    
    @Test
    public void testDimension() throws SQLException, IOException, ParseException {
    	insertThreePoints();
        Connection cx = getConnection();
    	Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("select st_dimension(geom) from spatial");
        rs.next();
        int srid = rs.getInt(1);
        st.close();
        assertThat(srid, is(0));
    }

    @Test
    public void testBoundary() throws SQLException, IOException, ParseException {
        insertThreePoints();
        Connection cx = getConnection();
    	Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("select st_boundary(geom) from spatial");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        st.close();
        assertThat(geometry.getArea(), is(0.0));
    }

    @Test
    public void testRelate() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
    	Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("VALUES ST_Relate(ST_GeomFromText('POINT(1 2)',4326), ST_Buffer(ST_GeomFromText('POINT(1 2)',4326),2))");
        rs.next();
        String result = rs.getString(1);
        st.close();
      //I don't really understand this function so not sure if the result is correct. At least the result of both tests seems consistent.
        assertThat(result, is("0FFFFFFF2"));
    }

    @Test
    public void testConvexHull() throws SQLException, IOException, ParseException {
    	insertThreePoints();
        Connection cx = getConnection();
    	Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("select st_convexhull(geom) from spatial");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        st.close();
        assertThat(geometry.getArea(), is(0.0));
    }

    
    @Test
    public void testDifference() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
    	Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("VALUES ST_Difference(" +
        													 "ST_GeomFromText('POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))', 4326)," +
        													 "ST_GeomFromText('POLYGON((5 5, 5 10, 10 10, 10 5, 5 5))', 4326))");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        st.close();
        assertThat(geometry.getArea(), is(75.0));
    }
    
    @Test
    public void testIntersection() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
    	Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("VALUES ST_Intersection(" +
				 "ST_GeomFromText('POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))', 4326)," +
				 "ST_GeomFromText('POLYGON((5 5, 5 10, 10 10, 10 5, 5 5))', 4326))");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        st.close();
        assertThat(geometry.getArea(), is(25.0));
    }

    @Test
    public void testSymdifference() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
    	Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("VALUES ST_SymDifference(" +
				 "ST_GeomFromText('POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))', 4326)," +
				 "ST_GeomFromText('POLYGON((5 5, 5 15, 10 15, 10 5, 5 5))', 4326))");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        st.close();
        assertThat(geometry.getArea(), is(100.0));
    }


    @Test
    public void testUnion() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
    	Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("VALUES ST_Union(" +
				 "ST_GeomFromText('POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))', 4326)," +
				 "ST_GeomFromText('POLYGON((5 5, 5 15, 10 15, 10 5, 5 5))', 4326))");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        st.close();
        assertThat(geometry.getArea(), is(125.0));
    }

    private void insertThreePoints() throws SQLException{
        Connection cx = getConnection();
    	Statement st = cx.createStatement();
    	createTable(st, "spatial", "id", "geom");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(0 0)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(1 1)', 4326))");
        st.execute("INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(2 2)', 4326))");
        st.close();
    }
}
