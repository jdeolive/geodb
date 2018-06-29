package geodb;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * Various utilities for test classes.
 */
public class GeoDBTestUtils {

    static GeometryFactory factory = new GeometryFactory();
    
    public static Point createPoint(double x, double y) {
        return factory.createPoint(new Coordinate(x, y));
    }

    public static LineString createLineString(double... xyPairs) {
        return factory.createLineString(coordinates(xyPairs));
    }

    public static Polygon createPolygon(double... xyPairs) {
        return factory.createPolygon(new LinearRing(new CoordinateArraySequence(coordinates(xyPairs)), factory), null);
    }

    static Coordinate[] coordinates(double... xyPairs) {
        if (xyPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Even number of arguments required");
        }

        Coordinate[] coords = new Coordinate[xyPairs.length/2];
        for (int i = 0; i < xyPairs.length; i += 2) {
            coords[i/2] = new Coordinate(xyPairs[i], xyPairs[i+1]);
        }

        return coords;
    }
}
