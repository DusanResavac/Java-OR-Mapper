package OrmFramework.metamodel;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public interface Query {
    public Query or();
    public Query and();
    public Query not();
    public Query is(String columnName, QueryOperations operation, Object... objects);
    public Query isNot(String columnName, QueryOperations operation, Object... objects);
    public Query group();
    public Query groupEnd();

    /**
     * Converts all entered commands to matching sql syntax and returns the results.
     * @param <T> Object type (Class)
     * @return List of Objects of the specified type T
     * @throws NoSuchMethodException
     * @throws SQLException thrown if an SQL error occurs
     * @throws NoSuchFieldException thrown if the specified column name in one of commands could not be found
     */
    public <T> List<T> get() throws NoSuchMethodException, SQLException, NoSuchFieldException;

    /**
     * Performs a custom select that is specified in the second parameter sqlString. It is recommended to set the prefix to
     * some name + DOT. E.g. for students: "s.". Parameters that are stated in the Object... parameter will be appended
     * as arguments for the preparedStatement. If user input is validated, the use of this parameter is highly encouraged.
     * @param prefix String prefix before each column
     * @param sqlString SQL String
     * @param parameters Object...
     * @param <T> Object type (Class)
     * @return List of objects of specified type T
     * @throws SQLException thrown when an SQL error occurs
     * @throws NoSuchMethodException thrown when a needed field doesn't have the necessary get and set methods
     */
    public <T> List<T> getWithRawSQL(String prefix, String sqlString, Object... parameters) throws SQLException, NoSuchMethodException;

    String getColumnName();
    QueryOperations getOperation();
    List<Object> getParameters();
    QueryConnection getConnection();
    Query getPreviousQuery();
    void setPreviousQuery(Query previous);
    Class getC();
}
