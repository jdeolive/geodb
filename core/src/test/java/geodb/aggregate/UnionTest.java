package geodb.aggregate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
	
	private GeometryFactory factory;
	
	
	@Before
	public void init_extent() throws SQLException{
		union=new Union();
		union.init(null);
	}

	@Before
	public void init_geometry_factory(){
		factory=new GeometryFactory();
	}
	
	@Test
	public void extent_empty_collection() throws SQLException {
		Object result = union.getResult();
		assertThat(result, is(nullValue()));
	}

	@Test
	public void extent_one_point() throws SQLException{
		union.add(createPoint(3, 5));
		Object result = union.getResult();
		assertThat(result, is(not(nullValue())));
		Geometry envelope = GeoDB.gFromWKB((byte[]) result);
		assertThat(envelope.getArea(), is(0.0));
		assertThat(envelope, is(equalTo(createPoint(3, 5))));
	}


	@Test
	public void extent_two_points() throws SQLException{
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
	public void extent_triangle_polygon() throws SQLException{
		union.add(createPolygon(new Coordinate[]{new Coordinate(0, 1),new Coordinate(2, 2),new Coordinate(2, 0),new Coordinate(0, 1)}));
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
	public void extent_two_triangle_polygon() throws SQLException{
		union.add(createPolygon(new Coordinate[]{new Coordinate(0, 1),new Coordinate(2, 2),new Coordinate(2, 0),new Coordinate(0, 1)}));
		union.add(createPolygon(new Coordinate[]{new Coordinate(1, 1),new Coordinate(3, 2),new Coordinate(3, 0),new Coordinate(1, 1)}));
		Object result = union.getResult();
		assertThat(result, is(not(nullValue())));
		Geometry envelope = GeoDB.gFromWKB((byte[]) result);
		
		assertThat(envelope.getArea(), is(3.5));
		assertTrue(envelope.contains(createPoint(1, 1)));
		assertTrue(envelope.contains(createPoint(2.99999, 1.9999)));
		assertFalse(envelope.contains(createPoint(3.00001, 2.00001)));
		assertFalse(envelope.contains(createPoint(0, 0)));
	}


	
	private Geometry createPoint(double x,double y){
		return factory.createPoint(new Coordinate(x, y));
	}
	
	private Geometry createPolygon(Coordinate[] coordinates){
		return factory.createPolygon(new LinearRing(new CoordinateArraySequence(coordinates), factory),null);
	}

}
