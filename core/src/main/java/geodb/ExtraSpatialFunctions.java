package geodb;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

public class ExtraSpatialFunctions {

	private ExtraSpatialFunctions() {
	}

	public static Integer dimension(byte[] wkb) {
		Geometry geometry = GeoDB.gFromWKB(wkb);
		if (geometry != null) {
			return geometry.getDimension();
		}
		return null;
	}

	public static byte[] boundary(byte[] wkb) {
		Geometry geometry = GeoDB.gFromWKB(wkb);
		if (geometry != null) {
			Geometry boundary = geometry.getBoundary();
			if (boundary != null) {
				return GeoDB.gToWKB(boundary);
			}
		}
		return null;
	}

	public static boolean relate(byte[] wkb1, byte[] wkb2,
			String intersectionPattern) {
		Geometry geometry1 = GeoDB.gFromWKB(wkb1);
		Geometry geometry2 = GeoDB.gFromWKB(wkb1);
		if (geometry1 != null && geometry2 != null) {
			return geometry1.relate(geometry2, intersectionPattern);
		}
		return false;
	}

	public static String relate(byte[] wkb1, byte[] wkb2) {
		Geometry geometry1 = GeoDB.gFromWKB(wkb1);
		Geometry geometry2 = GeoDB.gFromWKB(wkb1);
		if (geometry1 != null && geometry2 != null) {
			IntersectionMatrix result = geometry1.relate(geometry2);
			return result.toString();
		}
		return null;
	}

	public static byte[] convexHull(byte[] wkb) {
		Geometry geometry = GeoDB.gFromWKB(wkb);
		if (geometry != null) {
			Geometry boundary = geometry.convexHull();
			if (boundary != null) {
				return GeoDB.gToWKB(boundary);
			}
		}
		return null;
	}

	public static byte[] difference(byte[] wkb1, byte[] wkb2) {
		if (wkb1 == null) {
			return null;
		}
		if (wkb2 == null) {
			return wkb1;
		}
		Geometry geometry1 = GeoDB.gFromWKB(wkb1);
		Geometry geometry2 = GeoDB.gFromWKB(wkb2);
		if (geometry1 == null) {
			return null;
		}
		if (geometry2 == null) {
			return wkb1;
		}

		return GeoDB.gToWKB(geometry1.difference(geometry2));
	}

	public static byte[] intersection(byte[] wkb1, byte[] wkb2) {
		if (wkb1 == null || wkb2 == null) {
			return null;
		}
		Geometry geometry1 = GeoDB.gFromWKB(wkb1);
		Geometry geometry2 = GeoDB.gFromWKB(wkb2);
		if (geometry1 == null || geometry2 == null) {
			return null;
		}

		return GeoDB.gToWKB(geometry1.intersection(geometry2));
	}

	public static byte[] symdifference(byte[] wkb1, byte[] wkb2) {
		if (wkb1 == null) {
			return wkb2;
		}
		if (wkb2 == null) {
			return wkb1;
		}
		Geometry geometry1 = GeoDB.gFromWKB(wkb1);
		Geometry geometry2 = GeoDB.gFromWKB(wkb2);
		if (geometry1 == null) {
			return GeoDB.gToWKB(geometry2);
		}
		if (geometry2 == null) {
			return GeoDB.gToWKB(geometry1);
		}

		return GeoDB.gToWKB(geometry1.symDifference(geometry2));
	}

	public static byte[] union(byte[] wkb1, byte[] wkb2) {
		if (wkb1 == null) {
			return wkb2;
		}
		if (wkb2 == null) {
			return wkb1;
		}
		Geometry geometry1 = GeoDB.gFromWKB(wkb1);
		Geometry geometry2 = GeoDB.gFromWKB(wkb2);
		if (geometry1 == null) {
			return GeoDB.gToWKB(geometry2);
		}
		if (geometry2 == null) {
			return GeoDB.gToWKB(geometry1);
		}

		return GeoDB.gToWKB(geometry1.union(geometry2));
	}

}
