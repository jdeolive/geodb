package geodb;

/**
 * Implementations of <code>DatabaseTestUtils</code> provide the
 * database-specific implementations of the methods in this interface.
 */
public interface DatabaseTestUtils {
    /**
     * Creates a new database with the given name.
     * 
     * @param databaseName
     *            the database name.
     * @throws Exception
     *             if anything goes wrong while creating the new database.
     */
    void createDB(String databaseName) throws Exception;

    /**
     * Destroys the database with the given name. The name is expected to be
     * something like .
     * 
     * @param databaseName
     *            the database name.
     * @throws Exception
     *             if anything goes wrong while destroying the database.
     */
    void destroyDB(String databaseName) throws Exception;

    /**
     * Creates a test table with the given name. The ID column should be an
     * <code>integer</code> type that is the primary key and have the
     * auto-increment capability.
     * 
     * @param tableName
     *            the table name.
     * @param idColumnName
     *            the ID column name.
     * @param geomColumnName
     *            the optional Geometry column name.
     * @returns the <code>CREATE TABLE</code> SQL.
     */
    String getCreateTestTableSql(String tableName, String idColumnName,
            String geomColumnName);

    /**
     * Drops the given table.
     * 
     * @param tableName
     *            the table name.
     * @return the <code>DROP TABLE</code> SQL.
     */
    String getDropTableSql(String tableName);

    /**
     * Indicates if a <code>DROP TABLE</code> statement drops the table if it
     * exists.
     * 
     * @return <code>true</code> if the {@link #dropTable(String)} SQL can be
     *         run even if the table does not exist.
     */
    boolean isDropTableIfExistsSupported();

    /**
     * Creates the SQL to limit the number of results returned by a
     * <code>SELECT</code> statement.
     * 
     * @param limit
     *            the number of rows to fetch.
     * @return the <code>LIMIT</code> clause to a <code>SELECT</code> statement.
     */
    String getLimitClauseSql(int limit);
}