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
    public <T> List<T> get() throws NoSuchMethodException, SQLException, NoSuchFieldException;
    public <T> List<T> getWithRawSQL(String prefix, String sqlString, Object... parameters) throws SQLException, NoSuchMethodException;

    String getColumnName();
    QueryOperations getOperation();
    List<Object> getParameters();
    QueryConnection getConnection();
    Query getPreviousQuery();
    void setPreviousQuery(Query previous);
    Class getC();
}
