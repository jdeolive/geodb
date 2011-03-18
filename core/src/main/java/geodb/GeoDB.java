package geodb;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import net.sourceforge.hatbox.MetaNode;
import net.sourceforge.hatbox.jts.Proc;
import net.sourceforge.hatbox.tools.CmdLine;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

public class GeoDB {

    static {
        CmdLine.LOGGER.setLevel(Level.WARNING);
    }

    static GeometryFactory gfactory = new GeometryFactory();
    
    static final WKBWriter wkbwriter() {
        try {
            return new WKBWriter(2, true);
        }
        catch(NoSuchMethodError e) {
            //means they are using an older verison of jts, fallback to old constructor
            //TODO: log a warning
            return new WKBWriter(2);
        }
        
    }
    
    static final WKBReader wkbreader() {
        return new WKBReader(gfactory);
    }
    
    static final WKTWriter wktwriter() {
        return new WKTWriter();
    }
    
    static final WKTReader wktreader() {
        return new WKTReader(gfactory);
    }
             

    
    /**
     * Returns the current GeoH2 version.
     */
    public static String GeoToolsVersion() {
        return "0.1";
    }
    
    /**
     * Returns the internal version of the GeoH2 bindings in order to track upgrades.
     */
    public static String CheckSum() {
        return "8";
    }
    
    //
    // Database initializer
    //
    public static void InitGeoDB(Connection cx) throws SQLException {
        try {
            Statement st = cx.createStatement();
            try {
                //first check if this database is already spatial and up to date
                try { 
                    ResultSet rs = st.executeQuery("SELECT checksum FROM _GEODB");
                    try {
                        //table exists, check the checksum
                        if (rs.next()) {
                            if (CheckSum().equalsIgnoreCase( rs.getString(1) ) ) {
                                //no upgrade needed, good to go
                                return;
                            }
                            
                            //upgrade needed, fall through to below routine
                        }
                    }
                    finally {
                        rs.close();
                    }
                }
                catch (SQLException e) {
                    //first time, continue with initialization
                }
            
                //load h2 functions
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(GeoDB.class.getResourceAsStream("geodb.sql")));
                String line = null;
                while((line = in.readLine()) != null) {
                    try {
                        st.execute(line);
                    }
                    catch(SQLException e) {
                        //ignore
                    }
                }
                in.close();
                
                //load hatbox functions
                List<String> ddl = CmdLine.getddl("create_h2.sql");
                for (Iterator<String> i = ddl.iterator(); i.hasNext(); ) {
                    line = i.next();
                    try {
                        st.execute(line);
                    }
                    catch(SQLException e) {
                        //ignore
                    }
                }
                
                //create the _GEOH2 metadata table
                st.execute("CREATE TABLE IF NOT EXISTS _GEODB (checksum VARCHAR)");
                st.execute("DELETE FROM _GEODB");
                st.execute("INSERT INTO _GEODB VALUES (" + CheckSum() + ")" );
                
                //create the geometry columns table
                st.execute("CREATE TABLE IF NOT EXISTS geometry_columns (f_table_schema VARCHAR, " +
                    "f_table_name VARCHAR, f_geometry_column VARCHAR, coord_dimension INT, " +
                    "srid INT, type VARCHAR(30))");
            }
            finally {
                st.close();
            }
        }
        catch( Exception e ) {
            throw (SQLException) new SQLException("Could not initialize database").initCause(e);
        }
    }
    
    //
    // Management functions
    //
    /**
     * Adds a geometry column to a table.
     * 
     * @param schema The table schema, may be <code>null</code> to specify default schema
     * @param table The table name, not null
     * @param column The geometry column name, not null
     * @param srid The spatial reference system identifier
     * @param type The geometry type, one of "POINT", "LINESTRING", "POLYGON", "MULTIPOINT", 
     *             "MULTILINESTRING", "MULTIPOLYGON", "GEOMETRY", "GEOMETRYCOLLECTION"
     * @param dim The geometry dimension 
     */
    public static void AddGeometryColumn(Connection cx, String schema, String table, 
        String column, int srid, String type, int dim) throws SQLException {
        
        type = type.toUpperCase();
        
        Statement st = cx.createStatement();
        try {
            ResultSet rs = 
                cx.getMetaData().getColumns(null, schema, table, column);
            try {
                if (!rs.next()) {
                    st.execute("ALTER TABLE " + tbl(schema, table) + " ADD " 
                        + esc(column) + " " + type + " COMMENT '" + type + "'");
                }
            }
            finally {
                rs.close();
            }
            
            schema = schema != null ? schema : "PUBLIC";   
            if (!"GEOMETRY".equals(type) && !"GEOMETRYCOLLECTION".equals(type)) {
                st.execute("ALTER TABLE " + tbl(schema, table) + " ADD CONSTRAINT " + 
                    esc(geotypeConstraint(schema,table,column)) + " CHECK " + esc(column) + 
                    " IS NULL OR " + "GeometryType(" + esc(column) + ") = '" + type + "'");
            }
            st.execute("INSERT INTO geometry_columns VALUES (" + 
                str(schema) + ", " + str(table) + ", " + str(column) + ", " + 
                srid + ", " + dim + ", " + str(type) + ")");
        }
        finally {
            st.close();
        }
    }
    
    /**
     * Drops a geometry column from a table.
     * 
     * @param schema The table schema, may be <code>null</code> to specify default schema
     * @param table The table name, not null
     * @param column The geometry column name, not null
     */
    public static void DropGeometryColumn(Connection cx, String schema, String table, String column) 
        throws SQLException {
        
        Statement st = cx.createStatement();
        try {
            //check the case of a view
            boolean isView = false;
            ResultSet tables = cx.getMetaData().getTables(null, schema, table, new String[]{"VIEW"});
            try {
                isView = tables.next();
            }
            finally {
                tables.close();
            }
            
            schema = schema != null ? schema : "PUBLIC";
            st.execute("ALTER TABLE " + tbl(schema, table) + " DROP CONSTRAINT IF EXISTS " 
                + esc(geotypeConstraint(schema,table,column)));
            
            if (!isView) {
                st.execute("ALTER TABLE " + tbl(schema, table) + " DROP COLUMN " + esc(column));
            }
            
            st.execute("DELETE FROM geometry_columns WHERE f_table_schema = " + str(schema) + 
                " AND " + "f_table_name = " + str(table) + " AND f_geometry_column = " + str(column));
        }
        finally {
            st.close();
        }
    }
    
    /**
     * Drops all the geometry columns from a table.
     * 
     * @param schema The table schema, may be <code>null</code> to specify default schema
     * @param table The table name, not null
     */
    public static void DropGeometryColumns(Connection cx, String schema, String table) throws SQLException {
        Statement st = cx.createStatement();
        try {
            schema = schema != null ? schema : "PUBLIC";
            
            //look up the geometry column entries
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT f_geometry_column FROM geometry_columns")
               .append(" WHERE f_table_schema = '").append(schema).append("'")
               .append(" AND f_table_name = '").append(table).append("'");
            
            ResultSet columns = st.executeQuery(sql.toString());
            while(columns.next()) {
                String column = columns.getString(1);
                DropGeometryColumn(cx, schema, table, column);
            }
        }
        finally {
            st.close();
        }
    }
    
    //
    // Geometry Outputs
    //
    
    /**
     * Return the Well-Known Text (WKT) representation of the geometry without SRID metadata.
     */
    public static String ST_AsText( byte[] wkb ) {
        if ( wkb == null ) {
            return null;
        }
     
        return gToWKT(gFromWKB(wkb));
    }
    
    /**
     * Return the Well-Known Text (WKT) representation of the geometry with SRID meta data. 
     */
    public static String ST_AsEWKT( byte[] wkb ) {
        if ( wkb == null ) {
            return null;
        }
        
        Geometry g = gFromWKB(wkb);
        return gToEWKT(g);
    }
    
    /**
     * Return the Well-Known Binary (WKB) representation of the geometry with SRID meta data.
     */
    public static byte[] ST_AsEWKB( byte[] wkb ) {
        return wkb;
    }
    
    /**
     * Returns a Geometry in HEXEWKB format (as text).
     */
    public static String ST_AsHexEWKB( byte[] wkb ) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i < wkb.length; i++) {
          sb.append( Integer.toString( ( wkb[i] & 0xff ) + 0x100, 16).substring( 1 ) );
        }
        return sb.toString();
    }
    
    /**
     * Return a GeoHash representation (geohash.org) of the geometry.
     */
    public static String ST_GeoHash( byte[] wkb ) {
        if ( wkb == null ) {
            return null;
        }
        
        Geometry g = gFromWKB(wkb);
        Envelope e = g.getEnvelopeInternal();
        GeoString gs1 = new GeoString(e.getMinX(),e.getMinY());
        GeoString gs2 = new GeoString(e.getMaxX(),e.getMaxY());
        
        return gs1.union(gs2).toString();
    }
    
    //
    // Geometry Constructors
    //
    /**
     *  Return a specified ST_Geometry value from Extended Well-Known Binary representation (EWKB).
     */
    public static byte[] ST_GeomFromEWKB (byte[] wkb) {
        return gToWKB(gFromWKB(wkb));
    }
    
    /**
     * Return a specified ST_Geometry value from Extended Well-Known Text representation (EWKT).
     */
    public static byte[] ST_GeomFromEWKT (String wkt) {
        if ( wkt == null ) {
            return null;
        }
       
        return gToWKB(gFromEWKT(wkt));
    }
    
    /**
     * Return a specified ST_Geometry value from Well-Known Text representation (WKT).
     */
    public static byte[] ST_GeomFromText(String wkt, int srid) {
        if ( wkt == null ) {
            return null;
        }
        
        Geometry g = gFromWKT(wkt,srid);
        return gToWKB(g);
    }
    
    /**
     * Creates a geometry instance from a Well-Known Binary geometry representation (WKB) and optional SRID.
     */
    public static byte[] ST_GeomFromWKB(byte[] wkb, int srid) {
        if ( wkb == null ) {
            return null;
        }
        
        try {
            Geometry g = wkbreader().read(wkb);
            g.setSRID(srid);
            
            return gToWKB(g);
        } 
        catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    /**
     * Creates a Point geometry from x, y values.
     */
    public static byte[] ST_MakePoint(double x, double y) {
        return gToWKB(gfactory.createPoint(new Coordinate(x,y))); 
    }
    
    /**
     * Creates a BOX2D defined by the given point geometries.
     */
    public static byte[] ST_MakeBox2D( byte[] wkb1, byte[] wkb2 ) {
        if ( wkb1 == null || wkb2 == null ) {
            return null;
        }
        
        Point p1 = (Point) gFromWKB( wkb1 );
        Point p2 = (Point) gFromWKB( wkb1 );
        
        return ST_MakeBox2D(p1.getX(),p1.getY(),p2.getX(),p2.getY()); 
    }
    
    public static byte[] ST_MakeBox2D( double x1, double y1, double x2, double y2 ) {
        return envToWKB(x1,y1,x2,y2);
    }
    
    /*
     ST_BdPolyFromText - Construct a Polygon given an arbitrary collection of closed linestrings as a MultiLineString Well-Known text representation.
    ST_BdMPolyFromText - Construct a MultiPolygon given an arbitrary collection of closed linestrings as a MultiLineString text representation Well-Known text representation.
    ST_GeomCollFromText - Makes a collection Geometry from collection WKT with the given SRID. If SRID is not give, it defaults to -1.
    ST_GeomFromEWKB - Return a specified ST_Geometry value from Extended Well-Known Binary representation (EWKB).
    ST_GeomFromEWKT - Return a specified ST_Geometry value from Extended Well-Known Text representation (EWKT).
    ST_GeometryFromText - Return a specified ST_Geometry value from Well-Known Text representation (WKT). This is an alias name for ST_GeomFromText
    ST_GeomFromText - Return a specified ST_Geometry value from Well-Known Text representation (WKT).
    ST_GeomFromWKB - Creates a geometry instance from a Well-Known Binary geometry representation (WKB) and optional SRID.
    ST_LineFromMultiPoint - Creates a LineString from a MultiPoint geometry.
    ST_LineFromText - Makes a Geometry from WKT representation with the given SRID. If SRID is not given, it defaults to -1.
    ST_LineFromWKB - Makes a LINESTRING from WKB with the given SRID
    ST_LinestringFromWKB - Makes a geometry from WKB with the given SRID.
    
    ST_MakeBox3D - Creates a BOX3D defined by the given 3d point geometries.
    ST_MakeLine - Creates a Linestring from point geometries.
    ST_MakePolygon - Creates a Polygon formed by the given shell. Input geometries must be closed LINESTRINGS.
    ST_MakePoint - Creates a 2D,3DZ or 4D point geometry.
    ST_MakePointM - Creates a point geometry with an x y and m coordinate.
    ST_MLineFromText - Return a specified ST_MultiLineString value from WKT representation.
    ST_MPointFromText - Makes a Geometry from WKT with the given SRID. If SRID is not give, it defaults to -1.
    ST_MPolyFromText - Makes a MultiPolygon Geometry from WKT with the given SRID. If SRID is not give, it defaults to -1.
    ST_Point - Returns an ST_Point with the given coordinate values. OGC alias for ST_MakePoint.
    ST_PointFromText - Makes a point Geometry from WKT with the given SRID. If SRID is not given, it defaults to unknown.
    ST_PointFromWKB - Makes a geometry from WKB with the given SRID
    ST_Polygon - Returns a polygon built from the specified linestring and SRID.
    ST_PolygonFromText - Makes a Geometry from WKT with the given SRID. If SRID is not give, it defaults to -1.
    ST_WKBToSQL - Return a specified ST_Geometry value from Well-Known Binary representation (WKB). This is an alias name for ST_GeomFromWKB that takes no srid
    ST_WKTToSQL - Return a specified ST_Geometry value from Well-Known Text representation (WKT). This is an alias name for ST_GeomFromText
     */
    //
    // Geometry Accessors
    //
    
    /**
     * Returns the type of the geometry as a string. Eg: 'LINESTRING', 'POLYGON', 'MULTIPOINT', etc.
     */
    public static String GeometryType(byte[] wkb) {
        if ( wkb == null ) {
            return null;
        }
        
        Geometry g = gFromWKB( wkb );
        return g.getGeometryType().toUpperCase();
    }
    
    /**
     * Returns a geometry representing the bounding box of the supplied geometry.
     */
    public static Envelope ST_Envelope(byte[] wkb) {
        if ( wkb == null ) {
            return null;
        }
        return gFromWKB(wkb).getEnvelopeInternal();
        //return envToWKB(envFromWKB(wkb));
    }
    
    /**
     * Returns the envelope of a geometry as text.
     */
    public static String EnvelopeAsText( byte[] wkb ) {
        Envelope env = ST_Envelope(wkb);
        if (env == null) {
            return null;
        }
        return "("+env.getMinX()+","+env.getMinY()+","+env.getMaxX()+","+env.getMaxY()+")";
    }

    /**
     * Returns the spatial reference identifier for the ST_Geometry.
     */
    public static int ST_SRID(byte[] wkb) {
        if ( wkb == null ) {
            return -1;
        }
        
        Geometry g = gFromWKB(wkb);
        return g.getSRID();
    }
    
    /**
     * Returns true if the ST_Geometry is well formed.
     */
    public static boolean ST_IsValid( byte[] wkb ) {
        if ( wkb == null ) {
            return false;
        }
        
        Geometry g = gFromWKB(wkb);
        return g.isValid();
    }
    
    /**
     * Returns (TRUE) if this Geometry has no anomalous geometric points, such as self intersection 
     * or self tangency.
     */
    public static boolean ST_IsSimple ( byte[] wkb ) {
        if ( wkb == null ) {
            return false;
        }
        
        Geometry g = gFromWKB(wkb);
        return g.isSimple();
    }
    
    /**
     * Returns true if this Geometry is an empty geometry . If true, then this Geometry represents 
     * the empty point set - i.e. GEOMETRYCOLLECTION(EMPTY).
     */
    public static boolean ST_IsEmpty (byte[] wkb) {
        if ( wkb == null ) {
            return false;
        }
        
        Geometry g = gFromWKB(wkb);
        return g.isEmpty();
    }
    /*
    ST_Boundary - Returns the closure of the combinatorial boundary of this Geometry.
    ST_CoordDim - Return the coordinate dimension of the ST_Geometry value.
    ST_Dimension - The inherent dimension of this Geometry object, which must be less than or equal to the coordinate dimension.
    ST_EndPoint - Returns the last point of a LINESTRING geometry as a POINT.
    
    ST_ExteriorRing - Returns a line string representing the exterior ring of the POLYGON geometry. Return NULL if the geometry is not a polygon. Will not work with MULTIPOLYGON
    ST_GeometryN - Return the 1-based Nth geometry if the geometry is a GEOMETRYCOLLECTION, MULTIPOINT, MULTILINESTRING, MULTICURVE or MULTIPOLYGON. Otherwise, return NULL.
    ST_GeometryType - Return the geometry type of the ST_Geometry value.
    ST_InteriorRingN - Return the Nth interior linestring ring of the polygon geometry. Return NULL if the geometry is not a polygon or the given N is out of range.
    ST_IsClosed - Returns TRUE if the LINESTRING's start and end points are coincident.
    
    ST_IsRing - Returns TRUE if this LINESTRING is both closed and simple.
    
    
    ST_IsValidReason - Returns text stating if a geometry is valid or not and if not valid, a reason why.
    ST_M - Return the M coordinate of the point, or NULL if not available. Input must be a point.
    ST_NDims - Returns coordinate dimension of the geometry as a small int. Values are: 2,3 or 4.
    ST_NPoints - Return the number of points (vertexes) in a geometry.
    ST_NRings - If the geometry is a polygon or multi-polygon returns the number of rings.
    ST_NumGeometries - If geometry is a GEOMETRYCOLLECTION (or MULTI*) return the number of geometries, otherwise return NULL.
    ST_NumInteriorRings - Return the number of interior rings of the first polygon in the geometry. This will work with both POLYGON and MULTIPOLYGON types but only looks at the first polygon. Return NULL if there is no polygon in the geometry.
    ST_NumInteriorRing - Return the number of interior rings of the first polygon in the geometry. Synonym to ST_NumInteriorRings.
    ST_NumPoints - Return the number of points in an ST_LineString or ST_CircularString value.
    ST_PointN - Return the Nth point in the first linestring or circular linestring in the geometry. Return NULL if there is no linestring in the geometry.
    
    ST_StartPoint - Returns the first point of a LINESTRING geometry as a POINT.
    ST_Summary - Returns a text summary of the contents of the ST_Geometry.
    ST_X - Return the X coordinate of the point, or NULL if not available. Input must be a point.
    ST_Y - Return the Y coordinate of the point, or NULL if not available. Input must be a point.
    ST_Z - Return the Z coordinate of the point, or NULL if not available. Input must be a point.
    ST_Zmflag - Returns ZM (dimension semantic) flag of the geometries as a small int. Values are: 0=2d, 1=3dm, 2=3dz, 3=4d
    */
    
    //
    // Geometry editors
    //
    
    /**
     * Sets the SRID on a geometry to a particular integer value.
     */
    public static byte[] ST_SetSRID ( byte[] wkb, int srid ) {
        if ( wkb == null ) {
            return null;
        }
        
        Geometry g = gFromWKB(wkb);
        g.setSRID(srid);
        return gToWKB(g);
    }
    
    /*
    ST_AddPoint - Adds a point to a LineString before point <position> (0-based index).
    ST_Affine - Applies a 3d affine transformation to the geometry to do things like translate, rotate, scale in one step.
    ST_Force_2D - Forces the geometries into a "2-dimensional mode" so that all output representations will only have the X and Y coordinates.
    ST_Force_3D - Forces the geometries into XYZ mode. This is an alias for ST_Force_3DZ.
    ST_Force_3DZ - Forces the geometries into XYZ mode. This is a synonym for ST_Force_3D.
    ST_Force_3DM - Forces the geometries into XYM mode.
    ST_Force_4D - Forces the geometries into XYZM mode.
    ST_Force_Collection - Converts the geometry into a GEOMETRYCOLLECTION.
    ST_ForceRHR - Forces the orientation of the vertices in a polygon to follow the Right-Hand-Rule.
    ST_LineMerge - Returns a (set of) LineString(s) formed by sewing together a MULTILINESTRING.
    ST_Multi - Returns the geometry as a MULTI* geometry. If the geometry is already a MULTI*, it is returned unchanged.
    ST_RemovePoint - Removes point from a linestring. Offset is 0-based.
    ST_Reverse - Returns the geometry with vertex order reversed.
    ST_Rotate - This is a synonym for ST_RotateZ
    ST_RotateX - Rotate a geometry rotRadians about the X axis.
    ST_RotateY - Rotate a geometry rotRadians about the Y axis.
    ST_RotateZ - Rotate a geometry rotRadians about the Z axis.
    ST_Scale - Scales the geometry to a new size by multiplying the ordinates with the parameters. Ie: ST_Scale(geom, Xfactor, Yfactor, Zfactor).
    ST_Segmentize - Return a modified geometry having no segment longer than the given distance. Distance computation is performed in 2d only.
    ST_SetPoint - Replace point N of linestring with given point. Index is 0-based.

    ST_SnapToGrid - Snap all points of the input geometry to the grid defined by its origin and cell size. Remove consecutive points falling on the same cell, eventually returning NULL if output points are not enough to define a geometry of the given type. Collapsed geometries in a collection are stripped from it. Useful for reducing precision.
    ST_Transform - Returns a new geometry with its coordinates transformed to the SRID referenced by the integer parameter.
    ST_Translate - Translates the geometry to a new location using the numeric parameters as offsets. Ie: ST_Translate(geom, X, Y) or ST_Translate(geom, X, Y,Z).
    ST_TransScale - Translates the geometry using the deltaX and deltaY args, then scales it using the XFactor, YFactor args, working in 2D only.
    */
     
    //
    //
    //Spatial Relationships and Measurements

    /**
     * Returns the area of the geometry if it is a polygon or multi-polygon.
     */
    public static double ST_Area( byte[] wkb ) {
        if ( wkb == null ) {
            return -1;
        }
        
        Geometry g = gFromWKB(wkb);
        return g.getArea();
    }
    
    /**
     * Returns true if two bounding boxes (specified as either geometries or boxes) intersect.  
     */
    public static boolean ST_BBox( byte[] b1, byte[] b2 ) {
        if ( b1 == null || b2 == null ) {
            return false;
        }
        
        Envelope e1 = envFromWKB(b1);
        Envelope e2 = envFromWKB(b2);
        
        return e1.intersects(e2);
    }
    
    /**
     * Returns the geometric center of a geometry.
     */
    public static byte[] ST_Centroid ( byte[] wkb ) {
        if ( wkb == null ) {
            return null;
        }
        
        Geometry g = gFromWKB(wkb);
        return gToWKB( g.getCentroid() );
    }
    
    /**
     * Returns TRUE if the supplied geometries have some, but not all, interior points in common.
     * 
     */
    public static boolean ST_Crosses( byte[] wkb1, byte[] wkb2 ) {
        if ( wkb1 == null || wkb2 == null ) {
            return false;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.crosses( g2 );
    }
    
    /**
     *  Returns true if and only if no points of B lie in the exterior of A, and at least one point 
     *  of the interior of B lies in the interior of A.
     */
    public static boolean ST_Contains( byte[] wkb1, byte[] wkb2 ) {
        if ( wkb1 == null || wkb2 == null ) {
            return false;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.contains( g2 );
    }
    
    /**
     * Returns TRUE if the Geometries do not "spatially intersect" - if they do not share any space together.
     */
    public static boolean ST_Disjoint( byte[] wkb1, byte[] wkb2 ) {
        if ( wkb1 == null || wkb2 == null ) {
            return false;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.disjoint( g2 );
    }

    /**
     * Returns the distance between two geometries. 
     */
    public static double ST_Distance( byte[] wkb1, byte[] wkb2 ) {
        if ( wkb1 == null || wkb2 == null ) {
            return -1;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.distance(g2);
    }

   /**
     * Returns true if the geometries are within the specified distance of one another
     */
    public static boolean ST_DWithin( byte[] wkb1, byte[] wkb2, double distance ) {
        if ( wkb1 == null || wkb2 == null ) {
            return false;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.distance(g2) <= distance;
    }
    
    /**
     * Returns true if the given geometries represent the same geometry. Directionality is ignored.
     */
    public static boolean ST_Equals( byte[] wkb1, byte[] wkb2) {
        if ( wkb1 == null || wkb2 == null ) {
            return false;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.equals( g2 );
    }
    
    /**
     * Returns TRUE if the Geometries "spatially intersect" - (share any portion of space) and FALSE 
     * if they don't (they are Disjoint). 
     */
    public static boolean ST_Intersects( byte[] wkb1, byte[] wkb2) {
        if ( wkb1 == null || wkb2 == null ) {
            return false;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.intersects( g2 );
    }
    
    /**
     * Returns TRUE if the Geometries share space, are of the same dimension, but are not completely 
     * contained by each other.
     */
    public static boolean ST_Overlaps( byte[] wkb1, byte[] wkb2) {
        if ( wkb1 == null || wkb2 == null ) {
            return false;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.overlaps( g2 );
    }
    
    /**
     * Returns TRUE if the geometries have at least one point in common, but their interiors do not 
     * intersect.
     */
    public static boolean ST_Touches( byte[] wkb1, byte[] wkb2) {
        if ( wkb1 == null || wkb2 == null ) {
            return false;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.touches( g2 );
    }
    
    /**
     * Returns true if the geometry A is completely inside geometry B
     */
    public static boolean ST_Within( byte[] wkb1, byte[] wkb2) {
        if ( wkb1 == null || wkb2 == null ) {
            return false;
        }
        
        Geometry g1 = gFromWKB(wkb1);
        Geometry g2 = gFromWKB(wkb2);
        
        return g1.within( g2 );
    }
    /*
    
    ST_Azimuth - Returns the angle in radians from the horizontal of the vector defined by pointA and pointB
    
    ST_ContainsProperly - Returns true if B intersects the interior of A but not the boundary (or exterior). A does not contain properly itself, but does contain itself.
    ST_Covers - Returns 1 (TRUE) if no point in Geometry B is outside Geometry A
    ST_CoveredBy - Returns 1 (TRUE) if no point in Geometry A is outside Geometry B
    
    ST_LineCrossingDirection - Given 2 linestrings, returns a number between -3 and 3 denoting what kind of crossing behavior. 0 is no crossing.
    
    ST_Distance - Returns the 2-dimensional cartesian minimum distance between two geometries in projected units.
    ST_Distance_Sphere - Returns linear distance in meters between two lon/lat points. Uses a spherical earth and radius of 6370986 meters. Faster than , but less accurate. Only implemented for points.
    ST_Distance_Spheroid - Returns linear distance between two lon/lat points given a particular spheroid. Currently only implemented for points.
    
    
    ST_HasArc - Returns true if a geometry or geometry collection contains a circular string
    
    ST_Length - Returns the 2d length of the geometry if it is a linestring or multilinestring.
    ST_Length2D - Returns the 2-dimensional length of the geometry if it is a linestring or multi-linestring. This is an alias for ST_Length
    ST_Length3D - Returns the 3-dimensional or 2-dimensional length of the geometry if it is a linestring or multi-linestring.
    ST_Length_Spheroid - Calculates the 2D or 3D length of a linestring/multilinestring on an ellipsoid. This is useful if the coordinates of the geometry are in longitude/latitude and a length is desired without reprojection.
    ST_Length2D_Spheroid - Calculates the 2D length of a linestring/multilinestring on an ellipsoid. This is useful if the coordinates of the geometry are in longitude/latitude and a length is desired without reprojection.
    ST_Length3D_Spheroid - Calculates the length of a geometry on an ellipsoid, taking the elevation into account. This is just an alias for ST_Length_Spheroid.
    ST_Max_Distance - Returns the 2-dimensional largest distance between two geometries in projected units.
    ST_OrderingEquals - Returns true if the given geometries represent the same geometry and points are in the same directional order.
    
    ST_Perimeter - Return the length measurement of the boundary of an ST_Surface or ST_MultiSurface value. (Polygon, Multipolygon)
    ST_Perimeter2D - Returns the 2-dimensional perimeter of the geometry, if it is a polygon or multi-polygon. This is currently an alias for ST_Perimeter.
    ST_Perimeter3D - Returns the 3-dimensional perimeter of the geometry, if it is a polygon or multi-polygon.
    ST_PointOnSurface - Returns a POINT guaranteed to lie on the surface.
    ST_Relate - Returns true if this Geometry is spatially related to anotherGeometry, by testing for intersections between the Interior, Boundary and Exterior of the two geometries as specified by the values in the intersectionMatrixPattern. If no intersectionMatrixPattern is passed in, then returns the maximum intersectionMatrixPattern that relates the 2 geometries.
    
    
    */
    
    //
    // Geometry Processing
    //
    
    /**
     * Returns a geometry that represents all points whose distance from this Geometry is less than 
     * or equal to distance. Calculations are in the Spatial Reference System of this Geometry.
     */
    public static byte[] ST_Buffer (byte[] wkb, double distance) {
        if ( wkb == null ) {
            return null;
        }
        
        Geometry g = gFromWKB(wkb);
        
        return gToWKB( g.buffer( distance ) );
    }
    
    /**
     * Returns a "simplified" version of the given geometry using the Douglas-Peuker algorithm.
     */
    public static byte[] ST_Simplify( byte[] wkb, double tol ) {
        if ( wkb == null ) {
           return null;
        }
        
        Geometry g = gFromWKB(wkb);
        return gToWKB(DouglasPeuckerSimplifier.simplify(g,tol));
    }
    
    /*
    ST_BuildArea - Creates an areal geometry formed by the constituent linework of given geometry
    ST_Collect - Return a specified ST_Geometry value from a collection of other geometries.
    ST_ConvexHull - The convex hull of a geometry represents the minimum convex geometry that encloses all geometries within the set.
    ST_CurveToLine - Converts a CIRCULARSTRING/CURVEDPOLYGON to a LINESTRING/POLYGON
    ST_Difference - Returns a geometry that represents that part of geometry A that does not intersect with geometry B.
    ST_Dump - Returns a set of geometry_dump (geom,path) rows, that make up a geometry g1.
    ST_DumpRings - Returns a set of geometry_dump rows, representing the exterior and interior rings of a polygon.
    ST_Intersection - Returns a geometry that represents the shared portion of geomA and geomB
    ST_LineToCurve - Converts a LINESTRING/POLYGON to a CIRCULARSTRING, CURVED POLYGON
    ST_MemUnion - Same as ST_Union, only memory-friendly (uses less memory and more processor time).
    ST_MinimumBoundingCircle - Returns the smallest circle polygon that can fully contain a geometry. Default uses 48 segments per quarter circle.
    ST_Polygonize - Aggregate. Creates a GeometryCollection containing possible polygons formed from the constituent linework of a set of geometries.
    ST_Shift_Longitude - Reads every point/vertex in every component of every feature in a geometry, and if the longitude coordinate is <0, adds 360 to it. The result would be a 0-360 version of the data to be plotted in a 180 centric map
    
    ST_SimplifyPreserveTopology - Returns a "simplified" version of the given geometry using the Douglas-Peuker algorithm. Will avoid creating derived geometries (polygons in particular) that are invalid.
    ST_SymDifference - Returns a geometry that represents the portions of A and B that do not intersect. It is called a symmetric difference because ST_SymDifference(A,B) = ST_SymDifference(B,A).
    ST_Union - Returns a geometry that represents the point set union of the Geometries.
    */
    
    //
    // Miscellaneous Functions
    //
    /*
    ST_Accum - Aggregate. Constructs an array of geometries.
    ST_Box2D - Returns a BOX2D representing the maximum extents of the geometry.
    ST_Box3D - Returns a BOX3D representing the maximum extents of the geometry.
    ST_Estimated_Extent - Return the 'estimated' extent of the given spatial table. The estimated is taken from the geometry column's statistics. The current schema will be used if not specified.
    ST_Expand - Returns bounding box expanded in all directions from the bounding box of the input geometry
    ST_Extent - an aggregate function that returns the bounding box that bounds rows of geometries.
    ST_Extent3D - an aggregate function that returns the box3D bounding box that bounds rows of geometries.
    Find_SRID - The syntax is find_srid(<db/schema>, <table>, <column>) and the function returns the integer SRID of the specified column by searching through the GEOMETRY_COLUMNS table.
    ST_Mem_Size - Returns the amount of space (in bytes) the geometry takes.
    ST_Point_Inside_Circle - Is the point geometry insert circle defined by center_x, center_y , radius
    ST_XMax - Returns X maxima of a bounding box 2d or 3d or a geometry.
    ST_XMin - Returns X minima of a bounding box 2d or 3d or a geometry.
    ST_YMax - Returns Y maxima of a bounding box 2d or 3d or a geometry.
    ST_YMin - Returns Y minima of a bounding box 2d or 3d or a geometry.
    ST_ZMax - Returns Z minima of a bounding box 2d or 3d or a geometry.
    ST_ZMin - Returns Z minima of a bounding box 2d or 3d or a geometry.
    */
    
    //
    // Management functions
    //
    public static void CreateSpatialIndex( Connection cx, String schemaName, String tableName,
            String columnName, String srid) throws SQLException {
        HashMap<String,String> args = new HashMap();
        if (schemaName == null) {
            schemaName = "PUBLIC";
        }
        
        args.put("s", schemaName);
        args.put("t", tableName);
        args.put("geom", columnName);
        args.put("srid", srid);
        try {
            CmdLine.spatialize(cx, args);
            Proc.buildIndex(cx, schemaName, tableName, 10000, null);
        } 
        catch (Exception e) {
            throw (SQLException) new SQLException("Error creating spatial index").initCause(e);
        }
    }
    
    public static void CreateSpatialIndex_GeoHash( Connection cx, String schemaName, String tableName,
            String columnName ) throws SQLException {
        
        schemaName = "".equals( schemaName ) ? null : schemaName;
        
        Statement st = cx.createStatement();
        try {
            String table = "\"" + tableName + "\"";
            table = schemaName != null ? "\""+schemaName+"\"." + table : table;
            
            String column = "\"_" + columnName + "_GEOHASH\"";  
               
            String sql = "ALTER TABLE " + table + " ADD " + column + " VARCHAR";
            st.execute( sql );
            
            sql = "UPDATE " + table + 
                " SET " + column + " = ST_GeoHash(\"" + columnName + "\")";
            st.execute( sql );
            
            sql = "CREATE INDEX \"_"+columnName+"_GEOHASH_INDEX\" " +
                "ON " + table + "(" + column + ")";
            st.execute( sql );
        }
        finally {
            st.close();
        }
    }
    
    public static void DropSpatialIndex( Connection cx, String schemaName, String tableName) 
        throws SQLException {
        
        HashMap<String,String> args = new HashMap();
        if (schemaName == null) {
            schemaName = "PUBLIC";
        }
        
        args.put("s", schemaName);
        args.put("t", tableName);
        try {
            CmdLine.deSpatialize(cx, args);
        } 
        catch (Exception e) {
            throw (SQLException) new SQLException("Error dropping spatial index").initCause(e);
        }
    }
    
    public static int GetSRID( Connection cx, String schemaName, String tableName ) throws SQLException {
        //TODO: some logging here
        
        DatabaseMetaData dbmd = cx.getMetaData();
        ResultSet tables = 
            dbmd.getTables(null, schemaName, tableName + "_HATBOX", new String[]{"TABLE"});
        try {
            if (!tables.next()) {
                //no spatial index / metadata
                return -1;
            }
        }
        finally {
            tables.close();
        }
        
        String table = "";
        if (schemaName != null) {
            table += "\"" + schemaName +"\".";
        }
        table += "\"" + tableName + "_HATBOX\"";
        PreparedStatement ps = cx.prepareStatement("SELECT NODE_DATA FROM "+table+" WHERE ID = 1");
        
        try {
            try {
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        MetaNode md = new MetaNode(rs.getBytes(1));
                        return md.getSrid();
                    }
                }
                finally {
                    rs.close();
                }
            }
            catch(SQLException e ) {
            }
            
        }
        finally {
            ps.close();
        }
        
        return -1;
    }
    
//    public static ResultSet SpatialQuery( Connection cx, String sql ) throws SQLException {
//        sql = transformToSpatialQuery(cx,sql);
//        
//        Statement st = cx.createStatement();
//        return st.executeQuery( sql );
//    }
//    
//    public static String DebugSpatialQuery(Connection cx, String sql) throws SQLException {
//        return transformToSpatialQuery(cx, sql);
//    }
//    
//    static String transformToSpatialQuery(Connection cx, String sql) throws SQLException {
//        PlainSelect select = null;
//        Table table = null;
//        BBoxFunctionVisitor bbv = new BBoxFunctionVisitor(); 
//        try {
//            CCJSqlParser p = new CCJSqlParser(new StringReader(sql));
//            net.sf.jsqlparser.statement.Statement st  = p.Statement();
//            if ( st instanceof Select ) {
//                select = (PlainSelect) ((Select) st).getSelectBody();
//            }
//        }
//        catch( Exception e ) {
//            //syntax error in the query? just pass it through
//        }
//        
//        if ( select != null ) {
//            if ( select.getFromItem() instanceof Table ) {
//                table = ((Table)select.getFromItem());
//            }
//            select.getWhere().accept(bbv);
//        }
//        
//        if ( table != null && bbv.geometryColumn != null ) {
//            //rewrite the query to utilize the spatial index, if one eixsts
//            String indexColumnName = "_" + dequote(bbv.geometryColumn) + "_GEOHASH";
//            boolean useIndex = false;
//            
//            DatabaseMetaData md = cx.getMetaData();
//            ResultSet cols = md.getColumns(null, dequote(table.getSchemaName()), dequote(table.getName()),indexColumnName);
//            useIndex = cols.next();
//            cols.close();
//        
//            if ( useIndex ) {
//                //compute the geohash for the specified bounding box
//                String geohash = GeoHash.geohash(bbv.x1, bbv.y1, bbv.x2, bbv.y2);
//                
//                //compute all the parent geohashes
//                List<String> geohashes = new ArrayList<String>();
//                for ( int i = geohash.length(); i > 0; i-- ) {
//                    geohashes.add( geohash.substring(0,i) );
//                }
//                
//                boolean includeGeometryColumn = true;
//                StringBuffer sb = new StringBuffer();
//                for (SelectItem si : (List<SelectItem>) select.getSelectItems() ) {
//                    sb.append(si.toString()).append(",");
//                    if ( si.toString().equals( bbv.geometryColumn ) ) {
//                        includeGeometryColumn = false;
//                    }
//                }
//                if ( includeGeometryColumn ) {
//                    sb.append( bbv.geometryColumn );
//                }
//                else {
//                    sb.setLength(sb.length()-1);    
//                }
//                
//                String selectClause = "SELECT " + sb.toString();
//                
//                StringBuffer rsql = new StringBuffer();
//                
//                //start outer select
//                rsql.append( selectClause );
//                rsql.append( " FROM ");
//                
//                //start inner select 
//                rsql.append("(").append( selectClause );
//                rsql.append( " FROM ").append( table.getWholeTableName() );
//                rsql.append( " WHERE " ).append("\"").append( indexColumnName ).append("\"");
//                rsql.append(" IN (");
//                for ( String gh : geohashes ) {
//                    rsql.append("'").append(gh).append("'").append(",");
//                }
//                rsql.setLength(rsql.length()-1);
//                rsql.append( ")" );
//                //end inner select
//                
//                rsql.append( " UNION " );
//                
//                //start inner select
//                rsql.append("(").append( selectClause );
//                rsql.append( " FROM ").append( table.getWholeTableName() );
//                rsql.append( " WHERE " ).append("\"").append( indexColumnName ).append("\"");
//                rsql.append(" LIKE '").append(geohash).append("%'");
//                rsql.append(")");
//                //end inner select
//                
//                rsql.append( ") ");
//                rsql.append( " WHERE ").append( select.getWhere().toString() );
//                sql = rsql.toString();
//            }
//        }
//        
//        return sql;
//    }
//    
//    static String dequote(String name) {
//        if ( name == null ) {
//            return null;
//        }
//        if ( name.startsWith( "\"") ) {
//            name = name.substring(1,name.length()-1);
//        }
//        return name;
//    }
    
    //
    // helper/utility functions
    //
    public static byte[] gToWKB( Geometry g ) {
        return wkbwriter().write( g );
    }
    
    public static byte[] gToEWKB( Geometry g ) {
        try {
            // binary format:
            // |--32 bytes--|--EWKB--|
            // 
            // first 32 bytes is the bounding box
            
            //write the geometry
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            wkbwriter().write( g , new OutputStreamOutStream( bytes ) );
            byte[] b = bytes.toByteArray();
            
            //convert to postgis style ewkb which has the srid
            byte[] ewkb = new byte[32+b.length+4];
            
            //first 32 bytes is the boundaing box
            Envelope bbox = g.getEnvelopeInternal();
            envToWKB(bbox,ewkb,0);
            
            //first byte (endianess) + 4 bytes (type)
            System.arraycopy(b, 0, ewkb, 32, 5);
            
            //set the srid flag in the type byte
            ewkb[33] |= 0x20;
            
            //insert the srid (assuming big endian)
            int srid = g.getSRID();
            ewkb[37] = (byte)(srid >>> 24);
            ewkb[38] = (byte)(srid >> 16 & 0xff);
            ewkb[39] = (byte)(srid >> 8 & 0xff);
            ewkb[40] = (byte)(srid & 0xff);
            
            //copy the geometry
            System.arraycopy(b,5,ewkb,41,b.length-5);
            return ewkb;
        } 
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
    public static Geometry gFromWKB( byte[] wkb ) {
        return gFromWKB( wkb, wkbreader() );
    }
    
    public static Geometry gFromWKB( byte[] wkb, WKBReader wkbreader ) {
        try {
            return wkbreader.read( wkb );
        } 
        catch (ParseException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public static Geometry gFromEWKB( byte[] wkb ) {
        return gFromEWKB(wkb,wkbreader());
    }
    public static Geometry gFromEWKB( byte[] wkb, WKBReader wkbreader ) {
        
        try {
            //read the geometry
            return wkbreader.read( 
                new InputStreamInStream(new ByteArrayInputStream(wkb,32,wkb.length-32)) );
        } 
        catch( Exception e ) {
            // try the old format
            /*try {
                return fromWKB( wkb );
            }
            catch( Exception e2 ) {
                //fail below
            }*/
            
            throw new RuntimeException( e );
        }
    }
    
    public static byte[] envToWKB( Envelope e ) {
        return envToWKB(e.getMinX(),e.getMinY(),e.getMaxX(),e.getMaxY());
    }

    public static byte[] envToWKB( double x1, double y1, double x2, double y2 ) {
        return envToWKB(x1,y1,x2,y2,new byte[32],0);
    }
    
    public static byte[] envToWKB( Envelope e, byte[] wkb, int pos ) {
        return envToWKB(e.getMinX(),e.getMinY(),e.getMaxX(),e.getMaxY(),wkb,pos);
    }
    
    public static byte[] envToWKB( double x1, double y1, double x2, double y2, byte[] wkb, int pos ) {
        doubleToBytes(wkb,pos,x1);
        doubleToBytes(wkb,8,y1);
        doubleToBytes(wkb,16,x2);
        doubleToBytes(wkb,24,y2);
        return wkb;
    }
    
    public static Envelope envFromWKB( byte[] wkb ) {
        try {
            return wkbreader().read(wkb).getEnvelopeInternal();
        } 
        catch (ParseException e) {
            throw new RuntimeException(e);
        } 
        //double x1 = bytesToDouble(wkb,0);
        //double y1 = bytesToDouble(wkb,8);
        //double x2 = bytesToDouble(wkb,16);
        //double y2 = bytesToDouble(wkb,24);
        //return new Envelope(x1,x2,y1,y2);
    }
    
    static void doubleToBytes(byte[] b, int pos, double d ) {
        long l = Double.doubleToRawLongBits(d);
        b[pos] = (byte)(l >>> 56);
        b[pos+1] = (byte)(l >> 48 & 0xff);
        b[pos+2] = (byte)(l >> 40 & 0xff);
        b[pos+3] = (byte)(l >> 32 & 0xff);
        b[pos+4] = (byte)(l >> 24 & 0xff);
        b[pos+5] = (byte)(l >> 16 & 0xff);
        b[pos+6] = (byte)(l >> 8 & 0xff);
        b[pos+7] = (byte)(l & 0xff);
    }
    
    static double bytesToDouble(byte[] b, int pos) {
        //TODO: reference source of this code
        long l = (
            (long)(0xff & b[pos]) << 56  |
            (long)(0xff & b[pos+1]) << 48  |
            (long)(0xff & b[pos+2]) << 40  |
            (long)(0xff & b[pos+3]) << 32  |
            (long)(0xff & b[pos+4]) << 24  |
            (long)(0xff & b[pos+5]) << 16  |
            (long)(0xff & b[pos+6]) << 8   |
            (long)(0xff & b[pos+7]) << 0
        );
        return Double.longBitsToDouble(l);
    }
    
    public static Geometry gFromWKT( String wkt, int srid ) {
        try {
            Geometry g = wktreader().read( wkt );
            g.setSRID(srid);
            return g;
        } 
        catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public static String gToWKT( Geometry g ) {
        return g.toText();
    }
    
    public static Geometry gFromEWKT( String wkt ) {
        wkt = wkt.toUpperCase();
        
        //prune off the srid
        int srid = -1;
        if ( wkt.startsWith ("SRID=") ) {
            int semi = wkt.indexOf( ';' );
            if ( semi == -1 ) {
                throw new IllegalArgumentException( "Could not read EWKT format, should be 'SRID=<srid>;<WKT>'");
            }
            String s = wkt.substring(0,semi);
            srid = Integer.parseInt( s.substring(5, s.length()) );
            wkt = wkt.substring(s.length()+1);
        }
        
        return gFromWKT(wkt,srid);
    }
    
    public static String gToEWKT( Geometry g ) {
        return "SRID=" + g.getSRID() + ";" + g.toText();
    }
    
//    public static Envelope envFromBytes(byte[] bytes) {
//        //first try as serialized object
//        try {
//            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
//            ObjectInputStream ois = new ObjectInputStream(input);
//            
//            return (Envelope) ois.readObject();
//        }
//        catch( Exception e ) {
//            //failure, try as geometry
//            try {
//                Geometry g = gFromWKB(bytes);
//                return g.getEnvelopeInternal();
//            }
//            catch( Exception e1 ) {
//                throw new IllegalArgumentException("Unable to parse envelope.");
//            }
//        }
//    }
    
    /**
     * @deprecated use {@link #gFromWKB(byte[])}
     */
    private static Geometry fromWKB( byte[] wkb ) {
        
        try {
            ByteArrayInputStream bytes = 
                new ByteArrayInputStream( wkb, 0, wkb.length-4 );

            //read the geometry
            Geometry g = new WKBReader().read( new InputStreamInStream( bytes ) );
            
            //read the srid
            int srid = 0;
            srid |= wkb[wkb.length-4] & 0xFF;
            srid <<= 8;
            srid |= wkb[wkb.length-3] & 0xFF;
            srid <<= 8;
            srid |= wkb[wkb.length-2] & 0xFF;
            srid <<= 8;
            srid |= wkb[wkb.length-1] & 0xFF;
            g.setSRID(srid);
            
            return g;
            
        } 
        catch( Exception e ) {
            throw new RuntimeException( e );
        }
    }
    
    /**
     * deprecated {@link #gToWKB(Geometry)}
     */
    private static byte[] toWKB( Geometry g ) {
        try {
            WKBWriter w = new WKBWriter();
            
            //write the geometry
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            w.write( g , new OutputStreamOutStream( bytes ) );
   
            //supplement it with the srid
            int srid = g.getSRID();
            bytes.write( (byte)(srid >>> 24) );
            bytes.write( (byte)(srid >> 16 & 0xff) );
            bytes.write( (byte)(srid >> 8 & 0xff) );
            bytes.write( (byte)(srid & 0xff) );
            
            return bytes.toByteArray();
        } 
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
    //
    // some encoding helper functions
    //
    static String tbl(String schema, String table) {
        return schema != null ? esc(schema)+"."+esc(table) : esc(table);
    }
    
    static String esc(String s) {
        return "\"" + s + "\"";
    }
    
    static String str(String s) {
        return "'"+s+"'";
    }
    
    static String geotypeConstraint(String schema, String table, String column) {
        String name = table + "_" + column;
        if (schema != null) {
            name = schema + "_" + name;
        }
        return "ENFORCE_GEOTYPE_" + name;
    }
}
