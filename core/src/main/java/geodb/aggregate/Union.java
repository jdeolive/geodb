package geodb.aggregate;

import java.sql.Connection;
import java.sql.SQLException;

import org.locationtech.jts.geom.Geometry;

public class Union extends GeoAggregateFunction {

    private Geometry result;

    @Override
    protected void add(Geometry geometry) {
        if (result == null) {
            result = geometry;
        } else {
            if (geometry != null) {
                result = result.union(geometry);
            }
        }
    }

    @Override
    protected Geometry getGeometryResult() {
        return result;
    }

    public void init(Connection arg0) throws SQLException {
        result = null;
    }
}
