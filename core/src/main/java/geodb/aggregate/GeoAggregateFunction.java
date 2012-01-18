package geodb.aggregate;

import geodb.GeoDB;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import org.h2.api.AggregateFunction;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

public abstract class GeoAggregateFunction implements AggregateFunction {

    private Geometry createGeometry(ByteArrayInputStream stream) {
        InputStreamInStream inputStreamInStream = new InputStreamInStream(stream);
        Geometry geometry = null;
        try {
            geometry = new WKBReader().read(inputStreamInStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("given geometry can not be found", e);
        } catch (ParseException e) {
            throw new IllegalArgumentException("given geometry is not valid", e);
        }
        return geometry;

    }

    protected abstract void add(Geometry geometry);

    protected abstract Geometry getGeometryResult();

    public final void add(Object arg0) throws SQLException {
        if (arg0 != null) {
            Geometry geometry = createGeometry((ByteArrayInputStream) arg0);
            if (geometry != null) {
                add(geometry);
            }
        }
    }

    public final Object getResult() throws SQLException {
        Geometry geometryResult = getGeometryResult();
        if (geometryResult != null) {
            return GeoDB.gToWKB(geometryResult);
        }
        return null;
    }

    public final int getType(int[] arg0) throws SQLException {
        return Types.BLOB;
    }

}