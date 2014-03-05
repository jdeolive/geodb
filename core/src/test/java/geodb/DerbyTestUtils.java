package geodb;

import java.io.File;
import java.sql.DriverManager;

import org.apache.commons.io.FileUtils;

/**
 * <code>DerbyTestUtils</code> provides the Apache Derby-specific SQL and
 * utility methods for unit testing.
 */
public class DerbyTestUtils implements DatabaseTestUtils {
    /**
     * @see geodb.DatabaseTestUtils#createDB(java.lang.String)
     */
    public void createDB(String databaseName) throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        DriverManager.getConnection(
                "jdbc:derby:directory:target/" + databaseName + ";create=true")
                .close();
    }

    /**
     * @see geodb.DatabaseTestUtils#destroyDB(java.lang.String)
     */
    public void destroyDB(String databaseName) throws Exception {
        try {
            DriverManager.getConnection(
                    "jdbc:derby:directory:target/" + databaseName
                            + ";shutdown=true").close();
        } catch (Exception ignore) {
        }
        FileUtils.deleteDirectory(new File("target/" + databaseName));
    }

    /**
     * @see geodb.DatabaseTestUtils#getCreateTestTableSql(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public String getCreateTestTableSql(String tableName, String idColumnName,
            String geomColumnName) {
        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(tableName).append(" (");
        sql.append(idColumnName)
                .append(" INT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT ")
                .append(tableName.toUpperCase()).append("_PK PRIMARY KEY");
        if (geomColumnName != null) {
            sql.append(", ").append(geomColumnName)
                    .append(" VARCHAR (32672) FOR BIT DATA");
        }
        sql.append(')');
        return sql.toString();
    }

    /**
     * @see geodb.DatabaseTestUtils#getDropTableSql(java.lang.String)
     */
    public String getDropTableSql(String tableName) {
        return "DROP TABLE " + tableName;
    }

    /**
     * @see geodb.DatabaseTestUtils#isDropTableIfExistsSupported()
     */
    public boolean isDropTableIfExistsSupported() {
        return false;
    }

    public String getLimitClauseSql(int limit) {
        return "FETCH FIRST " + limit + " ROWS ONLY";
    }
}
