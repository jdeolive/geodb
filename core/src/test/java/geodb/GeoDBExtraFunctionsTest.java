package geodb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

public abstract class GeoDBExtraFunctionsTest extends GeoDBTestSupport {
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

    @Test
    public void testArea() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
        Geometry original = GeoDB.gFromWKT(
                "POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))", 4326);
        Statement st = cx.createStatement();
        ResultSet rs = st.executeQuery("VALUES ST_Area(ST_GeomFromText('"
                + original.toText() + "', 4326))");
        rs.next();
        double area = rs.getDouble(1);
        rs.close();
        assertEquals(original.getArea(), area, 0.0001);

        rs = st.executeQuery("VALUES ST_Area(null)");
        rs.next();
        area = rs.getDouble(1);
        assertEquals(-1, area, 0.0001);
        rs.close();

        st.close();
    }

    @Test
    public void testCentroid() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st
                .executeQuery("VALUES ST_Centroid("
                        + "ST_GeomFromText('MULTIPOINT ( -1 0, -1 2, -1 3, -1 4, -1 7, 0 1, 0 3, 1 1, 2 0, 6 0, 7 8, 9 8, 10 6 )', 4326))");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        rs.close();
        assertTrue(geometry instanceof Point);
        Point point = (Point)geometry;
        assertEquals(2.30769230769231, point.getX(), 0.00000000000001);
        assertEquals(3.30769230769231, point.getY(), 0.00000000000001);

        rs = st.executeQuery("VALUES ST_Centroid(null)");
        rs.next();
        binaryStream = rs.getBinaryStream(1);
        rs.close();
        assertNull(binaryStream);

        st.close();
    }

    @Test
    public void testSimplify() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st
                .executeQuery("VALUES ST_Simplify("
                        + "ST_GeomFromText('POLYGON((0 0, 0 1, 1 1, 1 0, 0.9 0, 0 0))', 4326), .1)");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        rs.close();
        assertTrue(geometry instanceof Polygon);
        Polygon polygon = (Polygon)geometry;
        assertEquals(5, polygon.getExteriorRing().getNumPoints());

        st.close();
    }

    @Test
    public void testMakePoint() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st
                .executeQuery("VALUES ST_MakePoint(1, 2)");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        rs.close();
        assertTrue(geometry instanceof Point);
        Point point = (Point)geometry;
        assertEquals(1, point.getX(), 0.00000000000001);
        assertEquals(2, point.getY(), 0.00000000000001);
        assertEquals(0, point.getSRID());

        st.close();
    }

    // TODO: ST_MakeBox2D does not create a JTS-formatted WKB.
    @Ignore
    public void testMakeBox2D() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st
                .executeQuery("VALUES ST_MakeBox2D(" +
                 "ST_GeomFromText('POINT(0 0)', 4326)," +
                 "ST_GeomFromText('POINT(1 1)', 4326))");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        rs.close();
        Envelope envelope = geometry.getEnvelopeInternal();
        assertEquals(0, envelope.getMinX(), 0.00000000000001);
        assertEquals(0, envelope.getMinY(), 0.00000000000001);
        assertEquals(1, envelope.getMaxX(), 0.00000000000001);
        assertEquals(1, envelope.getMaxY(), 0.00000000000001);

        st.close();
    }

    @Test
    public void testSetSRID() throws SQLException, IOException, ParseException {
        Connection cx = getConnection();
        Statement st = cx.createStatement();
        ResultSet rs = st
                .executeQuery("VALUES ST_SetSRID("
                        + "ST_GeomFromText('POINT(-123.365556 48.428611)', 4326), 3785)");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(binaryStream));
        rs.close();
        assertEquals(3785, geometry.getSRID());

        st.close();
    }

    @Test
    public void testEmpty() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_IsEmpty",
                "GEOMETRYCOLLECTION EMPTY",
                "POLYGON((1 2, 3 4, 5 6, 1 2))");
    }

    @Test
    public void testSimple() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_IsSimple",
                "POLYGON((1 2, 3 4, 5 6, 1 2))",
                "LINESTRING(1 1,2 2,2 3.5,1 3,1 2,2 1)");
    }

    @Test
    public void testValid() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_IsValid",
                "LINESTRING(0 0, 1 1)",
                "POLYGON((0 0, 1 1, 1 2, 1 1, 0 0))");
    }

    /**
     * Tests a predicate that takes one geometry as an argument and returns a
     * boolean value.
     * 
     * @param predicate
     *            the predicate to test.
     * @param wktPass
     *            the WKT that should pass.
     * @param wktFail
     *            the WKT that should fail.
     * @throws SQLException
     *             if unable to run the test in the database.
     */
    private void testBooleanPredicate(final String predicate,
            final String wktPass, final String wktFail) throws SQLException {
        Connection cx = getConnection();
        Statement st = cx.createStatement();

        // Test the passing condition.
        ResultSet rs = st.executeQuery("VALUES " + predicate + "("
                + "ST_GeomFromText('" + wktPass + "', 4326))");
        rs.next();
        boolean result = rs.getBoolean(1);
        rs.close();
        assertTrue(result);

        // Test the failing condition.
        rs = st.executeQuery("VALUES " + predicate + "(" + "ST_GeomFromText('"
                + wktFail + "', 4326))");
        rs.next();
        result = rs.getBoolean(1);
        rs.close();
        assertFalse(result);

        // Test the null argument.
        rs = st.executeQuery("VALUES " + predicate + "(null)");
        rs.next();
        result = rs.getBoolean(1);
        rs.close();
        assertFalse(result);

        st.close();
    }

    @Test
    public void testContains() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_Contains",
                "POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))", 
                "POINT(5 5)",
                "POINT(15 15)");
    }

    @Test
    public void testCrosses() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_Crosses",
                "LINESTRING(0 -2, 0 2)", 
                "LINESTRING(-2 0, 2 0)",
                "POINT(1 1)");
    }

    @Test
    public void testDisjoint() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_Disjoint",
                "POINT(0 0)", 
                "LINESTRING ( 2 0, 0 2 )",
                "LINESTRING ( 0 0, 0 2 )");
    }

    @Test
    public void testEquals() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_Equals",
                "LINESTRING(0 0, 10 10)", 
                "LINESTRING(0 0, 5 5, 10 10)",
                "LINESTRING(0 0, 5 5, 11 11)");
    }

    @Test
    public void testIntersects() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_Intersects",
                "POINT(0 0)", 
                "LINESTRING ( 0 0, 0 2 )",
                "LINESTRING ( 2 0, 0 2 )");
    }

    @Test
    public void testOverlaps() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_Overlaps",
                "POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))", 
                "POLYGON((1 1, 1 11, 11 11, 11 1, 1 1))",
                "POLYGON((1 1, 1 9, 9 9, 9 1, 1 1))");
    }

    @Test
    public void testTouches() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_Touches",
                "LINESTRING(0 0, 1 1, 0 2)", 
                "POINT(0 2)",
                "POINT(1 1)");
    }

    @Test
    public void testWithin() throws SQLException, IOException, ParseException {
        testBooleanPredicate("ST_Within",
                "POLYGON((1 1, 1 9, 9 9, 9 1, 1 1))", 
                "POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))",
                "POLYGON((2 2, 1 11, 11 11, 11 1, 2 2))");
    }

    /**
     * Tests a predicate that takes two geometries as arguments and returns a
     * boolean value.
     * 
     * @param predicate
     *            the predicate to test.
     * @param wktA
     *            the WKT of the first geometry.
     * @param wktBPass
     *            the WKT of the second geometry that should pass.
     * @param wktBFail
     *            the WKT of the second geometry that should fail.
     * @throws SQLException
     *             if unable to run the test in the database.
     */
    private void testBooleanPredicate(final String predicate,
            final String wktA, final String wktBPass, final String wktBFail)
            throws SQLException {
        Connection cx = getConnection();
        Statement st = cx.createStatement();

        // Test the passing condition.
        ResultSet rs = st.executeQuery("VALUES " + predicate + "("
                + "ST_GeomFromText('" + wktA + "', 4326),"
                + "ST_GeomFromText('" + wktBPass + "', 4326))");
        rs.next();
        boolean result = rs.getBoolean(1);
        rs.close();
        assertTrue(result);

        // Test the failing condition.
        rs = st.executeQuery("VALUES " + predicate + "(" + "ST_GeomFromText('"
                + wktA + "', 4326)," + "ST_GeomFromText('" + wktBFail
                + "', 4326))");
        rs.next();
        result = rs.getBoolean(1);
        rs.close();
        assertFalse(result);

        // Test the first null.
        rs = st.executeQuery("VALUES " + predicate + "(null,"
                + "ST_GeomFromText('" + wktBPass + "', 4326))");
        rs.next();
        result = rs.getBoolean(1);
        rs.close();
        assertFalse(result);

        // Test the second null.
        rs = st.executeQuery("VALUES " + predicate + "(" + "ST_GeomFromText('"
                + wktA + "', 4326), null)");
        rs.next();
        result = rs.getBoolean(1);
        rs.close();
        assertFalse(result);

        st.close();
    }

    @Test
    public void testDWithin() throws SQLException, IOException, ParseException {
        final String predicate = "ST_DWithin";
        final String wktA = "POINT(0 0)";
        final String wktBPass = "POINT(7 7)";
        final String wktBFail = "POINT(8 8)";

        Connection cx = getConnection();
        Statement st = cx.createStatement();

        // Test the passing condition.
        ResultSet rs = st.executeQuery("VALUES " + predicate + "("
                + "ST_GeomFromText('" + wktA + "', 4326),"
                + "ST_GeomFromText('" + wktBPass + "', 4326), 10)");
        rs.next();
        boolean result = rs.getBoolean(1);
        rs.close();
        assertTrue(result);

        // Test the failing condition.
        rs = st.executeQuery("VALUES " + predicate + "(" + "ST_GeomFromText('"
                + wktA + "', 4326)," + "ST_GeomFromText('" + wktBFail
                + "', 4326), 10)");
        rs.next();
        result = rs.getBoolean(1);
        rs.close();
        assertFalse(result);

        // Test the first null.
        rs = st.executeQuery("VALUES " + predicate + "(null,"
                + "ST_GeomFromText('" + wktBPass + "', 4326), 10)");
        rs.next();
        result = rs.getBoolean(1);
        rs.close();
        assertFalse(result);

        // Test the second null.
        rs = st.executeQuery("VALUES " + predicate + "(" + "ST_GeomFromText('"
                + wktA + "', 4326), null, 10)");
        rs.next();
        result = rs.getBoolean(1);
        rs.close();
        assertFalse(result);

        st.close();
    }

    @Test
    public void testWKB() throws SQLException, IOException, ParseException {
        String wkt = "POLYGON((743238 2967416,743238 2967450, 743265 2967450,743265.625 2967416,743238 2967416))";
        int srid = 2249;
        Geometry original = new WKTReader().read(wkt);
        original.setSRID(srid);
        Connection cx = getConnection();
        Statement st = cx.createStatement();

        ResultSet rs = st
                .executeQuery("VALUES ST_GeomFromWKB(ST_AsBinary(ST_GeomFromText('"
                        + wkt + "', " + srid + ")), " + srid + ")");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(
                binaryStream));
        binaryStream.close();
        rs.close();
        assertEquals(original, geometry);
        assertEquals(original.getSRID(), geometry.getSRID());

        rs = st.executeQuery("VALUES ST_AsBinary(null)");
        rs.next();
        binaryStream = rs.getBinaryStream(1);
        rs.close();
        assertNull(binaryStream);

        rs = st.executeQuery("VALUES ST_GeomFromWKB(null, " + srid + ")");
        rs.next();
        binaryStream = rs.getBinaryStream(1);
        rs.close();
        assertNull(binaryStream);

        st.close();
    }

    @Test
    public void testEWKB() throws SQLException, IOException, ParseException {
        String wkt = "POLYGON((743238 2967416,743238 2967450, 743265 2967450,743265.625 2967416,743238 2967416))";
        int srid = 2249;
        Geometry original = new WKTReader().read(wkt);
        original.setSRID(srid);
        Connection cx = getConnection();
        Statement st = cx.createStatement();

        ResultSet rs = st
                .executeQuery("VALUES ST_GeomFromEWKB(ST_AsEWKB(ST_GeomFromText('"
                        + wkt + "', " + srid + ")))");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(
                binaryStream));
        binaryStream.close();
        rs.close();
        assertEquals(original, geometry);
        assertEquals(original.getSRID(), geometry.getSRID());

        rs = st.executeQuery("VALUES ST_AsEWKB(null)");
        rs.next();
        binaryStream = rs.getBinaryStream(1);
        rs.close();
        assertNull(binaryStream);

        rs = st.executeQuery("VALUES ST_GeomFromEWKB(null)");
        rs.next();
        binaryStream = rs.getBinaryStream(1);
        rs.close();
        assertNull(binaryStream);

        st.close();
    }

    @Test
    public void testWKT() throws SQLException, IOException, ParseException {
        String wkt = "POLYGON((743238 2967416,743238 2967450, 743265 2967450,743265.625 2967416,743238 2967416))";
        int srid = 2249;
        Geometry original = new WKTReader().read(wkt);
        original.setSRID(srid);
        Connection cx = getConnection();
        Statement st = cx.createStatement();

        // Ensure that the EWKT matches the expected pattern.
        ResultSet rs = st.executeQuery("VALUES ST_AsText(ST_GeomFromText('"
                + wkt + "', " + srid + "))");
        rs.next();
        String ewkt = rs.getString(1);
        rs.close();
        String pattern = "^POLYGON[\\s]*[(][(][\\d\\s.,]+[)][)]$";
        assertTrue("'" + ewkt + "' does not match the pattern " + pattern,
                ewkt.matches(pattern));

        rs = st.executeQuery("VALUES ST_GeomFromText(ST_AsText(ST_GeomFromText('"
                + wkt + "', " + srid + ")), " + srid + ")");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(
                binaryStream));
        binaryStream.close();
        rs.close();
        assertEquals(original, geometry);
        assertEquals(original.getSRID(), geometry.getSRID());

        rs = st.executeQuery("VALUES ST_AsText(null)");
        rs.next();
        wkt = rs.getString(1);
        rs.close();
        assertNull(wkt);

        rs = st.executeQuery("VALUES ST_GeomFromText(null, " + srid + ")");
        rs.next();
        binaryStream = rs.getBinaryStream(1);
        rs.close();
        assertNull(binaryStream);

        st.close();
    }

    @Test
    public void testEWKT() throws SQLException, IOException, ParseException {
        String wkt = "POLYGON((743238 2967416,743238 2967450, 743265 2967450,743265.625 2967416,743238 2967416))";
        int srid = 2249;
        Geometry original = new WKTReader().read(wkt);
        original.setSRID(srid);
        Connection cx = getConnection();
        Statement st = cx.createStatement();

        // Ensure that the EWKT matches the expected pattern.
        ResultSet rs = st.executeQuery("VALUES ST_AsEWKT(ST_GeomFromText('"
                + wkt + "', " + srid + "))");
        rs.next();
        String ewkt = rs.getString(1);
        rs.close();
        String pattern = "^SRID=" + srid
                + ";POLYGON[\\s]*[(][(][\\d\\s.,]+[)][)]$";
        assertTrue("'" + ewkt + "' does not match the pattern " + pattern,
                ewkt.matches(pattern));

        rs = st.executeQuery("VALUES ST_GeomFromEWKT(ST_AsEWKT(ST_GeomFromText('"
                + wkt + "', " + srid + ")))");
        rs.next();
        InputStream binaryStream = rs.getBinaryStream(1);
        Geometry geometry = new WKBReader().read(new InputStreamInStream(
                binaryStream));
        rs.close();
        assertEquals(original, geometry);
        assertEquals(original.getSRID(), geometry.getSRID());

        rs = st.executeQuery("VALUES ST_AsEWKT(null)");
        rs.next();
        ewkt = rs.getString(1);
        rs.close();
        assertNull(ewkt);

        rs = st.executeQuery("VALUES ST_GeomFromEWKT(null)");
        rs.next();
        binaryStream = rs.getBinaryStream(1);
        rs.close();
        assertNull(binaryStream);

        st.close();
    }
}
