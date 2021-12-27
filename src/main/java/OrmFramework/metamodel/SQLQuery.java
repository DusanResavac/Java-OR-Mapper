package OrmFramework.metamodel;

import lombok.Getter;
import lombok.Setter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SQLQuery implements Query {

    @Getter @Setter
    private Query previousQuery = null;
    @Getter @Setter
    private String columnName = null;
    @Getter @Setter
    private QueryOperations operation = null;
    @Getter @Setter
    private List<Object> parameters = null;
    @Getter @Setter
    private QueryConnection connection;
    @Getter @Setter
    private Class c = null;

    public SQLQuery(Class c) {
        this.c = c;
    }

    public SQLQuery(SQLQuery previousQuery) {
        this.previousQuery = previousQuery;
    }


    private Query saveValues(String columnName, QueryOperations operation, QueryConnection connection, Object... objects) {
        this.columnName = columnName;
        this.operation = operation;
        this.parameters = List.of(Arrays.stream(objects).toArray());
        this.connection = connection;
        return new SQLQuery(this);
    }

    @Override
    public Query or() {
        this.connection = QueryConnection.OR;
        return new SQLQuery(this);
    }

    @Override
    public Query and() {
        this.connection = QueryConnection.AND;
        return new SQLQuery(this);
    }

    @Override
    public Query is(String columnName, QueryOperations operation, Object... objects) {
        return saveValues(columnName, operation, QueryConnection.IS, objects);
    }

    @Override
    public Query isNot(String columnName, QueryOperations operation, Object... objects) {
        return saveValues(columnName, operation, QueryConnection.IS_NOT, objects);
    }

    @Override
    public Query group() {
        this.connection = QueryConnection.GROUP;
        return new SQLQuery(this);
    }

    @Override
    public Query not() {
        this.connection = QueryConnection.NOT;
        return new SQLQuery(this);
    }

    @Override
    public Query groupEnd() {
        this.connection = QueryConnection.GROUP_END;
        return new SQLQuery(this);
    }

    public <T> List<T> getWithRawSQL(String prefix, String sqlString, Object... parameters) throws SQLException, NoSuchMethodException {
        _Entity ent = Orm._getEntity(this.getC());
        sqlString = ent.getSQL(prefix) + sqlString;
        List<T> result = new ArrayList<>();
        PreparedStatement prepStmt = Orm.getConnection().prepareStatement(sqlString);

        for (int i = 0; i < parameters.length; i++) {
            prepStmt.setObject(i + 1, parameters[i]);
        }

        ResultSet re = prepStmt.executeQuery();
        List<Object> localCache = new ArrayList<Object>();
        // create all objects returned
        while (re.next()) {
            result.add((T) Orm._createObject(this.getC(), re, localCache));
        }

        return result;
    }

    @Override
    public <T> List<T> get() throws NoSuchMethodException, SQLException, NoSuchFieldException {
        List<Query> queryOrder = reverseList();
        List<T> result = new ArrayList<>();

        // No queryOrders = return all of that kind
        if (queryOrder.size() == 0) {
            PreparedStatement prepStmt = Orm.getConnection().prepareStatement(Orm._getEntity(this.c).getSQL(null));
            ResultSet re = prepStmt.executeQuery();

            List<Object> localCache = new ArrayList<>();
            while (re.next()) {
                result.add((T) Orm._createObject(this.c, re, localCache));
            }

            return result;
        }

        _Entity ent = Orm._getEntity(queryOrder.get(0).getC());
        StringBuilder queryString = new StringBuilder(ent.getSQL(null));
        queryString.append(" WHERE ");
        List<Query> queryOrdersWithParams = new ArrayList<>();

        // check the connection and generate the appropriate sql
        for (Query query: queryOrder) {
            switch (query.getConnection()) {
                case IS -> {
                    // "is" has a comparison operation - therefore generate additional sql
                    queryString.append(generateSQLForOperation(query));
                    queryOrdersWithParams.add(query);
                }
                case IS_NOT -> {
                    // "isNot" has a comparison operation - therefore generate additional sql
                    queryString.append(" NOT ").append(generateSQLForOperation(query));
                    queryOrdersWithParams.add(query);
                }
                case OR -> queryString.append(" OR ");
                case AND -> queryString.append(" AND ");
                case NOT -> queryString.append(" NOT ");
                case GROUP -> queryString.append(" ( ");
                case GROUP_END -> queryString.append(" ) ");
            }
        }

        PreparedStatement prepStmt = Orm.getConnection().prepareStatement(queryString.toString());
        int counter = 0;
        // set all parameters for the prepared statement
        for (Query query: queryOrdersWithParams) {
            _Field field = null;
            // to correctly transform the value to a datatype that the database can store,
            // we need to get the correct field and call toColumnType (Since that field knows the column type)
            for (_Field f: ent.getFields()) {
                if (f.getColumnName().equalsIgnoreCase(query.getColumnName()) ||
                        (f.getRemoteColumnName() != null && f.getRemoteColumnName().equalsIgnoreCase(query.getColumnName())) ||
                        f.getName().equalsIgnoreCase(query.getColumnName())) {
                    field = f;
                    break;
                }
            }
            if (field == null) {
                throw new NoSuchFieldException("Could not find column that matches the column name");
            }
            // finally set all parameters
            for (Object obj: query.getParameters()) {
                Object temp = field.toColumnType(obj);
                prepStmt.setObject(counter+1, temp);
                counter++;
            }
        }

        ResultSet re = prepStmt.executeQuery();
        List<Object> localCache = new ArrayList<Object>();
        // create all objects returned
        while (re.next()) {
            result.add((T) Orm._createObject(queryOrder.get(0).getC(), re, localCache));
        }

        return result;
    }


    /**
     * Based on the operation of the query, generates the correct sql string
     * @param query
     * @return
     */
    private String generateSQLForOperation(Query query) {
        String result = query.getColumnName();

        // set the operation and add the placeholder(s) "?"
        result += switch (query.getOperation()) {
            case EQUALS -> " = ?";
            case GREATER_THAN -> " > ?";
            case GREATER_THAN_OR_EQUAL -> " >= ?";
            case LESS_THAN -> " < ?";
            case LESS_THAN_OR_EQUAL -> " <= ?";
            case LIKE -> " LIKE ?";
            // add as many placeholders as parameters exist
            case IN -> " IN (" + query.getParameters().stream().map(p -> "?").collect(Collectors.joining(", ")) + ")";
        };

        return result;
    }

    /**
     * Reverse the list starting when the last connection "get()" is called
     * @return
     */
    private List<Query> reverseList() {
        Query current = this.previousQuery;
        Query previous = null;
        Query next;

        /*
        1 <- 2 <- 3 <- 4 <- null    |  current = 4, current.setPrevious = previous(null), previous = current(4), current = current.getPrevious (originally 3)
        1 <- 2 <- 3 -> 4            |  current = 3, current.setPrevious = previous(4), previous = current(3), current = current.getPrevious (originally 2)
        1 <- 2 -> 3 -> 4            |  current = 2, current.setPrevious = previous(3), previous = current(2), current = current.getPrevious (originally 1)
        1 -> 2 -> 3 -> 4            |  current = 1, current.setPrevious = previous(2), previous = current(1), current = current.getPrevious (originally null)
         */

        while (current != null) {
            next = current.getPreviousQuery();
            current.setPreviousQuery(previous);
            previous = current;
            current = next;
        }

        List<Query> result = new ArrayList<>();

        while (previous != null) {
            result.add(previous);
            previous = previous.getPreviousQuery();
        }


        return result;
    }

    @Override
    public String toString() {
        return "SQLQuery{" +
                ", columnName='" + columnName + '\'' +
                ", operation=" + operation +
                ", parameters=" + parameters +
                ", connection=" + connection +
                '}';
    }
}
