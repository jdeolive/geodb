package geodb;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;

public abstract class GeoDBTestSupport {

    /**
     * Returns the connection to the database under test.
     *
     * @return the connection instance.
     */
    protected abstract Connection getConnection();

    protected abstract DatabaseTestUtils getTestUtils();

    /**
     * Creates a test table with the given name. The ID column should be an
     * <code>integer</code> type that is the primary key and have the
     * auto-increment capability.
     * 
     * @param st
     *            the statement to execute the SQL.
     * @param tableName
     *            the table name.
     * @param idColumnName
     *            the ID column name.
     * @param geomColumnName
     *            the Geometry column name.
     * @throws SQLException
     *             if unable to create the table.
     */
    protected void createTable(Statement st, String tableName,
            String idColumnName, String geomColumnName) throws SQLException {
        String sql = getTestUtils().getCreateTestTableSql(tableName,
                idColumnName, geomColumnName);
        st.execute(sql);
    }

    /**
     * Drops the given table.
     * 
     * @param st
     *            the statement to execute the SQL.
     * @param tableName
     *            the table name.
     * @throws SQLException
     *             if unable to drop the table.
     */
    protected void dropTable(Statement st, String tableName) throws SQLException {
        String sql = getTestUtils().getDropTableSql(tableName);
        try {
            st.execute(sql);
        } catch (SQLException e) {
            if (getTestUtils().isDropTableIfExistsSupported()) {
                throw e;
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        getConnection().close();
    }
}
