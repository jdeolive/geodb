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

#CREATE ALIAS DropSpatialIndex for "geodb.GeoDB.DropSpatialIndex"

create function EnvelopeAsText (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.EnvelopeAsText'
    parameter style java;

create function GeometryType (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.GeometryType'
    parameter style java;

create function ST_Area (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS DOUBLE PRECISION
    language java
    external name 'geodb.GeoDB.ST_Area'
    parameter style java;

create function ST_AsEWKB (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_AsEWKB'
    parameter style java;

create function ST_AsEWKT (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_AsEWKT'
    parameter style java;

create function ST_AsHexEWKB (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_AsHexEWKB'
    parameter style java;

create function ST_AsText (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_AsText'
    parameter style java;

create function ST_BBOX (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_BBox'
    parameter style java;

create function ST_Boundary (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Boundary'
    parameter style java;

create function ST_Buffer (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA,
    SPATIAL_DISTANCE DOUBLE PRECISION
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Buffer'
    parameter style java;

create function ST_Centroid (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Centroid'
    parameter style java;

create function ST_Crosses (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Crosses'
    parameter style java;

create function ST_Contains (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Contains'
    parameter style java;

create function ST_ConvexHull (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_ConvexHull'
    parameter style java;

create function ST_DWithin (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA,
    SPATIAL_DISTANCE DOUBLE PREISION
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_DWithin'
    parameter style java;

create function ST_Disjoint (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Disjoint'
    parameter style java;

create function ST_Distance (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS DOUBLE PRECISION
    language java
    external name 'geodb.GeoDB.ST_Distance'
    parameter style java;

create function ST_Difference (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Difference'
    parameter style java;

create function ST_Dimension (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS INT
    language java
    external name 'geodb.GeoDB.ST_Dimension'
    parameter style java;

# TODO: Is this the correct return type?
create function ST_Envelope (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_Envelope'
    parameter style java;

create function ST_Equals (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Equals'
    parameter style java;

create function ST_GeoHash (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_GeoHash'
    parameter style java;

create function ST_GeomFromEWKB (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_GeomFromEWKB'
    parameter style java;

create function ST_GeomFromEWKT (
    SPATIAL_WKT VARCHAR(32672)
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_GeomFromEWKT'
    parameter style java;

create function ST_GeomFromText (
    SPATIAL_WKT VARCHAR(32672),
    SPATIAL_SRID INTEGER
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_GeomFromText'
    parameter style java;

create function ST_GeomFromWKB (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA,
    SPATIAL_SRID INTEGER
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_GeomFromWKB'
    parameter style java;

create function ST_Intersection (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Intersection'
    parameter style java;

#CREATE ALIAS ST_Intersection FOR "geodb.GeoDB.ST_Intersection"

create function ST_Intersects (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Intersects'
    parameter style java;

create function ST_IsEmpty (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_IsEmpty'
    parameter style java;

create function ST_IsSimple (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_IsSimple'
    parameter style java;

create function ST_IsValid (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_IsValid'
    parameter style java;

create function ST_MakePoint (
    SPATIAL_X DOUBLE PRECISION,
    SPATIAL_Y DOUBLE PRECISION
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_MakePoint'
    parameter style java;

#CREATE ALIAS ST_MakePoint FOR "geodb.GeoDB.ST_MakePoint"

create function ST_MakeBox2D (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_MakeBox2D'
    parameter style java;

create function ST_MakeBox2D (
    SPATIAL_X1 DOUBLE PRECISION,
    SPATIAL_Y1 DOUBLE PRECISION,
    SPATIAL_X2 DOUBLE PRECISION,
    SPATIAL_Y2 DOUBLE PRECISION
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_MakeBox2D'
    parameter style java;

create function ST_Overlaps (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Overlaps'
    parameter style java;

# Derby doesn't support function overloading.
#create function ST_Relate (
#    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
#    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA,
#    SPATIAL_INTERSECTION_PATTERN VARCHAR(32672)
#) RETURNS BOOLEAN
#    language java
#    external name 'geodb.GeoDB.ST_Relate'
#    parameter style java;

create function ST_Relate (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS VARCHAR(32672)
    language java
    external name 'geodb.GeoDB.ST_Relate'
    parameter style java;

create function ST_SRID (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS INT
    language java
    external name 'geodb.GeoDB.ST_SRID'
    parameter style java;

create function ST_X (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS DOUBLE PRECISION
    language java
    external name 'geodb.GeoDB.ST_X'
    parameter style java;

create function ST_Y (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA
) RETURNS DOUBLE PRECISION
    language java
    external name 'geodb.GeoDB.ST_Y'
    parameter style java;

create function ST_SetSRID (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA,
    SPATIAL_SRID INT
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_SetSRID'
    parameter style java;

create function ST_Simplify (
    SPATIAL_WKB LONG VARCHAR FOR BIT DATA,
    SPATIAL_DISTANCE DOUBLE PRECISION
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Simplify'
    parameter style java;

create function ST_SymDifference (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_SymDifference'
    parameter style java;

create function ST_Touches (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Touches'
    parameter style java;

create function ST_Union (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS LONG VARCHAR FOR BIT DATA
    language java
    external name 'geodb.GeoDB.ST_Union'
    parameter style java;

create function ST_Within (
    SPATIAL_WKB1 LONG VARCHAR FOR BIT DATA,
    SPATIAL_WKB2 LONG VARCHAR FOR BIT DATA
) RETURNS BOOLEAN
    language java
    external name 'geodb.GeoDB.ST_Within'
    parameter style java;

#CREATE ALIAS Version FOR "geodb.GeoDB.Version"
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

#CREATE AGGREGATE ST_Extent FOR "geodb.aggregate.Extent"

CREATE DERBY AGGREGATE ST_Union_Aggregate
    FOR VARCHAR(32672) FOR BIT DATA
    RETURNS LONG VARCHAR FOR BIT DATA
    EXTERNAL NAME 'geodb.aggregate.Union';

#CREATE AGGREGATE ST_Union_Aggregate FOR "geodb.aggregate.Union"
