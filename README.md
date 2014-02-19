# Introducing GeoDB

GeoDB is a spatial extension of [H2](http://h2database.com), the Java SQL database. GeoDB utilizes the [JTS](http://tsusiatsoftware.net/jts/main.html) library as its geometry engine and the [Hatbox](http://hatbox.sourceforge.net) library for spatial indexing support.

# Quickstart

* Download [GeoDB](http://ares.boundlessgeo.com/geodb/geodb-0.8-app.zip)
* Unzip the `geodb-0.8-app.zip` file
* Update the `PATH` environment variable to include `geodb-0.8/bin`
* Run the @geodb@ command:

        % geodb foo

* Initialize the spatial database:

        @h2> CREATE ALIAS InitGeoDB for "geodb.GeoDB.InitGeoDB";
        @h2> CALL InitGeoDB();

* Create a spatial table:

        @h2> CREATE TABLE spatial (id INT AUTO_INCREMENT PRIMARY KEY, geom BLOB);

* Create some spatial data:

        @h2> INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(-5 -5)', 4326));
        @h2> INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(0 0)', 4326));
        @h2> INSERT INTO spatial (geom) VALUES (ST_GeomFromText('POINT(5 5)', 4326));
        
        @h2> SELECT ST_AsText(ST_Buffer(geom, 10)) as buffer FROM spatial;

* Create a spatial index

        @h2> CALL CreateSpatialIndex(null, 'SPATIAL', 'GEOM', '4326');

* Perform a spatial query

        @h2> SELECT ST_AsText(geom) FROM spatial WHERE  id IN (SELECT CAST(HATBOX_JOIN_ID AS INT) FROM HATBOX_MBR_INTERSECTS_ENV('PUBLIC', 'SPATIAL', -2, 2, -2, 2));

# License

GeoDB is licensed under the [MIT license](http://opensource.org/licenses/MIT). 

# More Information

General discussion takes place on the geodb [google group](http://groups.google.com/group/geodb). If you have any questions or comments please post a message there.
