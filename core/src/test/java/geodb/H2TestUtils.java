package geodb;

import java.sql.DriverManager;

import org.h2.tools.DeleteDbFiles;

/**
 * <code>H2TestUtils</code> assists in generating H2-specific SQL and utility
 * methods for unit testing.
 */
public class H2TestUtils implements DatabaseTestUtils {
    /**
     * @see geodb.DatabaseTestUtils#createDB(java.lang.String)
     */
    public void createDB(String databaseName) throws Exception {
        Class.forName("org.h2.Driver");
        DriverManager.getConnection("jdbc:h2:target/" + databaseName).close();
    }

    /**
     * @see geodb.DatabaseTestUtils#destroyDB(java.lang.String)
     */
    public void destroyDB(String databaseName) throws Exception {
        DeleteDbFiles.execute("target", databaseName, true);
    }

    /**
     * @see geodb.DatabaseTestUtils#getCreateTestTableSql(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public String getCreateTestTableSql(String tableName, String idColumnName,
            String geomColumnName) {
        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(tableName).append(" (").append(idColumnName)
                .append(" INT AUTO_INCREMENT PRIMARY KEY");
        if (geomColumnName != null) {
            sql.append(", ").append(geomColumnName).append(" BLOB");
        }
        sql.append(')');
        return sql.toString();
    }

    /**
     * @see geodb.DatabaseTestUtils#getDropTableSql(java.lang.String)
     */
    public String getDropTableSql(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    /**
     * @see geodb.DatabaseTestUtils#isDropTableIfExistsSupported()
     */
    public boolean isDropTableIfExistsSupported() {
        return true;
    }

    public String getLimitClauseSql(int limit) {
        return "LIMIT " + limit;
    }
}
