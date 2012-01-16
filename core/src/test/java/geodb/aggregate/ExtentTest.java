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

public class ExtentTest {

	private Extent extent;
	
	private GeometryFactory factory;
	
	
	@Before
	public void init_extent() throws SQLException{
		extent=new Extent();
		extent.init(null);
	}

	@Before
	public void init_geometry_factory(){
		factory=new GeometryFactory();
	}
	
	@Test
	public void extent_empty_collection() throws SQLException {
		Object result = extent.getResult();
		assertThat(result, is(nullValue()));
	}

	@Test
	public void extent_one_point() throws SQLException{
		extent.add(createPoint(3, 5));
		Object result = extent.getResult();
		assertThat(result, is(not(nullValue())));
		Geometry envelope = GeoDB.gFromWKB((byte[]) result);
		assertThat(envelope.getArea(), is(0.0));
		assertThat(envelope, is(equalTo(createPoint(3, 5))));
	}


	@Test
	public void extent_two_points() throws SQLException{
		extent.add(createPoint(3, 5));
		extent.add(createPoint(5, 3));
		Object result = extent.getResult();
		assertThat(result, is(not(nullValue())));
		Geometry envelope = GeoDB.gFromWKB((byte[]) result);
		
		assertThat(envelope.getArea(), is(4.0));
		assertTrue(envelope.contains(createPoint(4, 4)));
		assertTrue(envelope.contains(createPoint(4.99999, 4.9999)));
		assertFalse(envelope.contains(createPoint(5.00001, 5.00001)));
	}

	@Test
	public void extent_triangle_polygon() throws SQLException{
		extent.add(createPolygon(new Coordinate[]{new Coordinate(0, 1),new Coordinate(2, 2),new Coordinate(2, 0),new Coordinate(0, 1)}));
		Object result = extent.getResult();
		assertThat(result, is(not(nullValue())));
		Geometry envelope = GeoDB.gFromWKB((byte[]) result);
		
		assertThat(envelope.getArea(), is(4.0));
		assertTrue(envelope.contains(createPoint(1, 1)));
		assertTrue(envelope.contains(createPoint(1.99999, 1.9999)));
		assertFalse(envelope.contains(createPoint(2.00001, 2.00001)));
		assertTrue(envelope.contains(createPoint(0.00001, 0.00001)));
	}
	
	@Test
	public void extent_two_triangle_polygon() throws SQLException{
		extent.add(createPolygon(new Coordinate[]{new Coordinate(0, 1),new Coordinate(2, 2),new Coordinate(2, 0),new Coordinate(0, 1)}));
		extent.add(createPolygon(new Coordinate[]{new Coordinate(1, 1),new Coordinate(3, 2),new Coordinate(3, 0),new Coordinate(1, 1)}));
		Object result = extent.getResult();
		assertThat(result, is(not(nullValue())));
		Geometry envelope = GeoDB.gFromWKB((byte[]) result);
		
		assertThat(envelope.getArea(), is(6.0));
		assertTrue(envelope.contains(createPoint(1, 1)));
		assertTrue(envelope.contains(createPoint(2.99999, 1.9999)));
		assertFalse(envelope.contains(createPoint(3.00001, 2.00001)));
		assertTrue(envelope.contains(createPoint(0.00001, 0.00001)));
	}


	
	private Geometry createPoint(double x,double y){
		return factory.createPoint(new Coordinate(x, y));
	}
	
	private Geometry createPolygon(Coordinate[] coordinates){
		return factory.createPolygon(new LinearRing(new CoordinateArraySequence(coordinates), factory),null);
	}

}
