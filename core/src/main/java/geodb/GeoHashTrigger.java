package geodb;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.h2.api.Trigger;

/**
 * An H2 trigger which keeps a geometric column in sync with its geohash.
 * 
 * @author Justin Deoliveira, jdeolive@opengeo.org
 *
 */
public class GeoHashTrigger implements Trigger {

    int type;
    List<Integer[]> gindex;
    
    public void fire(Connection cx, Object[] oldRow, Object[] newRow) throws SQLException {
        
        for (Integer[] entry: gindex ) {
            String hash = null;
            
            ByteArrayInputStream input = null;
            if ( type == INSERT ) {
                input = (ByteArrayInputStream) newRow[entry[0]];
            }
            if ( type == UPDATE ) {
                input = (ByteArrayInputStream) oldRow[entry[0]];
            }   
            
            if ( input != null ) {
                byte[] buffer = new byte[input.available()];
                if ( buffer.length > 0 ) {
                    input.read(buffer, 0, buffer.length);
                    hash = GeoDB.ST_GeoHash(buffer);
                    input.reset();
                }
            }
            
            if ( type == INSERT ) {
                newRow[entry[1]] = hash;
            }
            if ( type == UPDATE ) {
                oldRow[entry[1]] = hash;
            }    
             
        }
        
    }

    public void init(Connection cx, String schemaName, String triggerName, String tableName, 
        boolean before, int type) throws SQLException {
        this.type = type;
        this.gindex = new ArrayList<Integer[]>();
        
        //figure out what the geometry columns are
        DatabaseMetaData md = cx.getMetaData();
        ResultSet columns = md.getColumns( null, schemaName, tableName, "%" );
        
        List<String> gColNames = new ArrayList();
        for ( int i = 0; columns.next(); i++ ) {
            String name = columns.getString("COLUMN_NAME");
            
            if ( name.startsWith( "_") && name.endsWith( "_geohash") ) {
                String gColName = name.substring( 1, name.length()-8);
                gColNames.add( gColName );
                gindex.add( new Integer[]{null,i} );
            }
        }
        columns.close();
        columns = md.getColumns( null, schemaName, tableName, "%" );
        for( int i = 0; columns.next(); i++ ) {
            String name = columns.getString("COLUMN_NAME");
            int j = gColNames.indexOf( name );
            if ( j != -1 ) {
                gindex.get( j )[0] = i;
            }
        }
    }

}
