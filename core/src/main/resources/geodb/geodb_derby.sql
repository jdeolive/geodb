create procedure AddGeometryColumn (
    IN SPATIAL_SCHEMA VARCHAR(128),
    IN SPATIAL_TABLE VARCHAR(128),
    IN SPATIAL_COLUMN VARCHAR(128),
    IN SPATIAL_SRID INT,
    IN SPATIAL_TYPE VARCHAR(128),
    IN SPATIAL_DIM INT
)
    language java
    external name 'geodb.GeoDB.AddGeometryColumnProc'
    parameter style java
    modifies sql data;

create procedure CreateSpatialIndex (
    IN SPATIAL_SCHEMA VARCHAR(128),
    IN SPATIAL_TABLE VARCHAR(128),
    IN SPATIAL_COLUMN VARCHAR(128),
    IN SPATIAL_SRID VARCHAR(16)
)
    language java
    external name 'geodb.GeoDB.CreateSpatialIndexProc'
    parameter style java
    modifies sql data;

create procedure DropGeometryColumn (
    IN SPATIAL_SCHEMA VARCHAR(128),
    IN SPATIAL_TABLE VARCHAR(128),
    IN SPATIAL_COLUMN VARCHAR(128)
)
    language java
    external name 'geodb.GeoDB.DropGeometryColumnProc'
    parameter style java
    modifies sql data;

create procedure DropGeometryColumns (
    IN SPATIAL_SCHEMA VARCHAR(128),
    IN SPATIAL_TABLE VARCHAR(128)
)
    language java
    external name 'geodb.GeoDB.DropGeometryColumnsProc'
    parameter style java
    modifies sql data;

create procedure DropSpatialIndex (
    IN SPATIAL_SCHEMA VARCHAR(128),
    IN SPATIAL_TABLE VARCHAR(128)
)
    language java
    external name 'geodb.GeoDB.DropSpatialIndexProc'
    parameter style java
    modifies sql data;

CREATE FUNCTION EnvelopeAsText (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.EnvelopeAsText'
    parameter style java;

CREATE FUNCTION GeometryType (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.GeometryType'
    parameter style java;

CREATE FUNCTION ST_Area (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS DOUBLE PRECISION
    language java
    external name 'geodb.GeoDB.ST_Area'
    parameter style java;

CREATE FUNCTION ST_AsEWKB (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_AsEWKB'
    parameter style java;

CREATE FUNCTION ST_AsEWKT (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_AsEWKT'
    parameter style java;

CREATE FUNCTION ST_AsHexEWKB (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_AsHexEWKB'
    parameter style java;

CREATE FUNCTION ST_AsText (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_AsText'
    parameter style java;

CREATE FUNCTION ST_AsBinary (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_AsBinary'
    parameter style java;

CREATE FUNCTION ST_BBox (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_BBox'
    parameter style java;

CREATE FUNCTION ST_Boundary (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Boundary'
    parameter style java;

CREATE FUNCTION ST_Buffer (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA,
    SPATIAL_DISTANCE DOUBLE PRECISION
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Buffer'
    parameter style java;

CREATE FUNCTION ST_Centroid (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Centroid'
    parameter style java;

CREATE FUNCTION ST_Crosses (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Crosses'
    parameter style java;

CREATE FUNCTION ST_Contains (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Contains'
    parameter style java;

CREATE FUNCTION ST_ConvexHull (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_ConvexHull'
    parameter style java;

CREATE FUNCTION ST_DWithin (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA,
    SPATIAL_DISTANCE DOUBLE PRECISION
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_DWithin'
    parameter style java;

CREATE FUNCTION ST_Disjoint (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Disjoint'
    parameter style java;

CREATE FUNCTION ST_Distance (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS DOUBLE PRECISION
    language java
    external name 'geodb.GeoDB.ST_Distance'
    parameter style java;

CREATE FUNCTION ST_Difference (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Difference'
    parameter style java;

CREATE FUNCTION ST_Dimension (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS INT
    language java
    external name 'geodb.GeoDB.ST_Dimension'
    parameter style java;

# TODO: Is this the correct return type?
CREATE FUNCTION ST_Envelope (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_Envelope'
    parameter style java;

CREATE FUNCTION ST_Equals (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Equals'
    parameter style java;

CREATE FUNCTION ST_GeoHash (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_GeoHash'
    parameter style java;

CREATE FUNCTION ST_GeomFromEWKB (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_GeomFromEWKB'
    parameter style java;

CREATE FUNCTION ST_GeomFromEWKT (
    SPATIAL_WKT VARCHAR(32672)
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_GeomFromEWKT'
    parameter style java;

CREATE FUNCTION ST_GeomFromText (
    SPATIAL_WKT VARCHAR(32672),
    SPATIAL_SRID INTEGER
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_GeomFromText'
    parameter style java;

CREATE FUNCTION ST_GeomFromWKB (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA,
    SPATIAL_SRID INTEGER
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_GeomFromWKB'
    parameter style java;

CREATE FUNCTION ST_Intersection (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Intersection'
    parameter style java;

#CREATE ALIAS ST_Intersection FOR "geodb.GeoDB.ST_Intersection"

CREATE FUNCTION ST_Intersects (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Intersects'
    parameter style java;

CREATE FUNCTION ST_IsEmpty (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_IsEmpty'
    parameter style java;

CREATE FUNCTION ST_IsSimple (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_IsSimple'
    parameter style java;

CREATE FUNCTION ST_IsValid (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_IsValid'
    parameter style java;

CREATE FUNCTION ST_MakePoint (
    SPATIAL_X DOUBLE PRECISION,
    SPATIAL_Y DOUBLE PRECISION
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_MakePoint'
    parameter style java;

CREATE FUNCTION ST_MakeBox2D (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_MakeBox2D'
    parameter style java;

CREATE FUNCTION ST_MakeBox2D (
    SPATIAL_X1 DOUBLE PRECISION,
    SPATIAL_Y1 DOUBLE PRECISION,
    SPATIAL_X2 DOUBLE PRECISION,
    SPATIAL_Y2 DOUBLE PRECISION
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_MakeBox2D'
    parameter style java;

CREATE FUNCTION ST_Overlaps (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Overlaps'
    parameter style java;

# Derby doesn't support function overloading.
#CREATE FUNCTION ST_Relate (
#    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
#    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA,
#    SPATIAL_INTERSECTION_PATTERN VARCHAR(32672)
#) RETURNS BOOLEAN
#    language java
#    external name 'geodb.GeoDB.ST_Relate'
#    parameter style java;

CREATE FUNCTION ST_Relate (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_Relate'
    parameter style java;

CREATE FUNCTION ST_SRID (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS INT
    language java
    external name 'geodb.GeoDB.ST_SRID'
    parameter style java;

CREATE FUNCTION ST_X (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS DOUBLE PRECISION
    language java
    external name 'geodb.GeoDB.ST_X'
    parameter style java;

CREATE FUNCTION ST_Y (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS DOUBLE PRECISION
    language java
    external name 'geodb.GeoDB.ST_Y'
    parameter style java;

CREATE FUNCTION ST_SetSRID (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA,
    SPATIAL_SRID INT
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_SetSRID'
    parameter style java;

CREATE FUNCTION ST_Simplify (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA,
    SPATIAL_DISTANCE DOUBLE PRECISION
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Simplify'
    parameter style java;

CREATE FUNCTION ST_SymDifference (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_SymDifference'
    parameter style java;

CREATE FUNCTION ST_Touches (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Touches'
    parameter style java;

CREATE FUNCTION ST_Union (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Union'
    parameter style java;

CREATE FUNCTION ST_Within (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Within'
    parameter style java;

CREATE FUNCTION Version () RETURNS VARCHAR(16)
    language java
    external name 'geodb.GeoDB.Version'
    parameter style java;

# Derby can't alias types.
#CREATE DOMAIN POINT AS BLOB
#CREATE DOMAIN LINESTRING AS BLOB
#CREATE DOMAIN POLYGON AS BLOB
#CREATE DOMAIN MULTIPOINT AS BLOB
#CREATE DOMAIN MULTILINESTRING AS BLOB
#CREATE DOMAIN MULTIPOLYGON AS BLOB
#CREATE DOMAIN GEOMETRYCOLLECTION AS BLOB
#CREATE DOMAIN GEOMETRY AS BLOB

CREATE DERBY AGGREGATE ST_Extent
    FOR VARCHAR(32672) FOR BIT DATA
    RETURNS LONG VARCHAR FOR BIT DATA
    EXTERNAL NAME 'geodb.aggregate.Extent';

CREATE DERBY AGGREGATE ST_Union_Aggregate
    FOR VARCHAR(32672) FOR BIT DATA
    RETURNS LONG VARCHAR FOR BIT DATA
    EXTERNAL NAME 'geodb.aggregate.Union';
