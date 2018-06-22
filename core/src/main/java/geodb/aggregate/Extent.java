package geodb.aggregate;

import java.sql.Connection;
import java.sql.SQLException;

import org.locationtech.jts.geom.Geometry;

public class Extent extends GeoAggregateFunction {

    private Geometry result;

    @Override
    protected void add(Geometry geometry) {
        if (result == null) {
            result = geometry;
        } else {
            if (geometry != null) {
                result = result.union(geometry.getEnvelope());
            }
        }
    }

    @Override
    protected Geometry getGeometryResult() {
        if (result != null) {
            return result.getEnvelope();
        }
        return null;
    }

    public void init(Connection arg0) throws SQLException {
        result = null;
    }
}
