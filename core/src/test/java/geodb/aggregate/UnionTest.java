package geodb.aggregate;

import static geodb.GeoDBTestUtils.createLineString;
import static geodb.GeoDBTestUtils.createPoint;
import static geodb.GeoDBTestUtils.createPolygon;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.vividsolutions.jts.geom.Point;
import geodb.GeoDB;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

public class UnionTest {

    private Union union;

    @Before
    public void initExtent() throws SQLException {
        union = new Union();
        union.init(null);
    }

    @Test
    public void extentEmptyCollection() throws SQLException {
        Object result = union.getResult();
        assertThat(result, is(nullValue()));
    }

    @Test
    public void extentOnePoint() throws SQLException {
        union.add(createPoint(3, 5));
        Object result = union.getResult();
        assertThat(result, is(not(nullValue())));
        Geometry envelope = GeoDB.gFromWKB((byte[]) result);
        assertThat(envelope.getArea(), is(0.0));

        assertThat(envelope, is(instanceOf(Point.class)));
        assertThat((Point)envelope, is(equalTo(createPoint(3, 5))));
    }

    @Test
    public void extentTwoPoints() throws SQLException {
        union.add(createPoint(3, 5));
        union.add(createPoint(5, 3));
        Object result = union.getResult();
        assertThat(result, is(not(nullValue())));
        Geometry geomResult = GeoDB.gFromWKB((byte[]) result);

        assertThat(geomResult.getArea(), is(0.0));
        assertTrue(geomResult.contains(createPoint(3, 5)));
        assertTrue(geomResult.contains(createPoint(5, 3)));
        assertFalse(geomResult.contains(createPoint(4, 4)));
    }

    @Test
    public void extentTrianglePolygon() throws SQLException {
        union.add(createPolygon(0, 1, 2, 2, 2, 0, 0, 1));
        Object result = union.getResult();
        assertThat(result, is(not(nullValue())));
        Geometry envelope = GeoDB.gFromWKB((byte[]) result);

        assertThat(envelope.getArea(), is(2.0));
        assertTrue(envelope.contains(createPoint(1, 1)));
        assertTrue(envelope.contains(createPoint(1.99999, 1.9999)));
        assertFalse(envelope.contains(createPoint(2.00001, 2.00001)));
        assertFalse(envelope.contains(createPoint(0, 0)));
    }

    @Test
    public void extentTwoTrianglePolygon() throws SQLException {
        union.add(createPolygon(0, 1, 2, 2, 2, 0, 0, 1));
        union.add(createPolygon(1, 1, 3, 2, 3, 0, 1, 1));
        Object result = union.getResult();
        assertThat(result, is(not(nullValue())));
        Geometry envelope = GeoDB.gFromWKB((byte[]) result);

        assertThat(envelope.getArea(), is(3.5));
        assertTrue(envelope.contains(createPoint(1, 1)));
        assertTrue(envelope.contains(createPoint(2.99999, 1.9999)));
        assertFalse(envelope.contains(createPoint(3.00001, 2.00001)));
        assertFalse(envelope.contains(createPoint(0, 0)));
    }
}
