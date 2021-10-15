package OrmFramework.metamodel;

import OrmFramework.metamodel._Entity;
import OrmFramework.metamodel._Field;

import java.sql.*;
import java.util.*;

public class Orm {

    private static final Map<Class, _Entity> _entities = new HashMap<>();
    /** Database connection. */
    private static Connection _connection;

    /**
     * Returns the matching _Entity to an object and creates it if necessary
     * @param o
     * @return
     * @throws NoSuchMethodException
     */
    public static _Entity _getEntity(Object o) throws NoSuchMethodException {
        Class c = (o instanceof Class) ? (Class) o : o.getClass();

        if (!_entities.containsKey(c)) {
            _entities.put(c, new _Entity(c));
        }

        return _entities.get(c);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // public static methods                                                                                            //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Gets the database connection.
     * @return Connection. */
    public static Connection getConnection()
    {
        return _connection;
    }


    /** Sets the database connection.
     * @param connection Connection. */
    public static void setConnection(Connection connection)
    {
        _connection = connection;
    }


    /** Connects to a database.
     * @param url Connection URL.
     * @throws SQLException Thrown when the connection could not be established. */
    public static void connect(String url) throws SQLException {
        DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
        _connection = DriverManager.getConnection(url);
    }

    /** Saves an object.
     * @param obj Object. */
    public static void save(Object obj) throws Exception {
        _Entity ent = _getEntity(obj);

        StringBuilder insert = new StringBuilder("INSERT INTO " + ent.getTableName() + " (");
        StringBuilder update = new StringBuilder("ON DUPLICATE KEY UPDATE ");
        StringBuilder insertVars = new StringBuilder();
        ArrayList<Object> parametersInsert = new ArrayList<>();
        ArrayList<Object> parametersUpdate = new ArrayList<>();

        boolean first = true;
        for (int i = 0; i < ent.get_internals().length; i++) {
            if (i > 0) { insert.append(", "); insertVars.append(", "); }
            insert.append(ent.get_internals()[i].getColumnName());
            insertVars.append("?");
            parametersInsert.add(ent.get_internals()[i].toColumnType(ent.get_internals()[i].getValue(obj)));

            // In case of a conflict, update all fields except for the primary key
            if (!ent.get_internals()[i].isPrimaryKey()) {
                if (first) { first = false; } else { update.append(", "); }
                update.append(ent.get_internals()[i].getColumnName()).append(" = ?");
                parametersUpdate.add(ent.get_internals()[i].toColumnType(ent.get_internals()[i].getValue(obj)));
            }
        }
        insert.append(") VALUES (").append(insertVars).append(") ");
        if (parametersUpdate.size() > 0) {
            insert.append(update);
        }
        parametersInsert.addAll(parametersUpdate);

        PreparedStatement preparedStatement = getConnection().prepareStatement(insert.toString());
        int n = 1;
        for (Object i: parametersInsert) { preparedStatement.setObject(n++, i); }

        preparedStatement.execute();
        preparedStatement.close();
    }

    /** Creates an instance by its primary key.
     * @param c Class.
     * @param pk Primary key.
     * @return Object. */
    protected static Object _createObject(Class c, Object pk, Collection<Object> localCache) throws Exception {

        Object result = searchCache(c, pk, localCache);

        if (result == null) {

            _Entity ent = _getEntity(c);

            PreparedStatement prepStmt = getConnection().prepareStatement(ent.getSQL(null) + " WHERE " + ent.getPrimaryKey().getColumnName() + " = ?");
            prepStmt.setObject(1, pk);


            ResultSet re = prepStmt.executeQuery();
            if (re.next()) {
                result = _createObject(c, re, localCache);
            }

            re.close();
            prepStmt.close();
        }

        if (result == null) { throw new Exception("No data."); }
        return result;
    }

    /** Creates an object from a database result set.
     * @param c Type.
     * @param re Result set.
     * @return Object. */
    protected static Object _createObject(Class c, ResultSet re, Collection<Object> localCache) throws NoSuchMethodException {
        _Entity ent = _getEntity(c);
        Object result = null;

        try {
            result = searchCache(c, re.getObject(ent.getPrimaryKey().getColumnName()), localCache);

            if (result == null) {
                if (localCache == null) { localCache = new ArrayList<>(); }
                localCache.add(result = c.getDeclaredConstructor().newInstance());

            }

            for (_Field f: ent.get_internals()) {
                Object columnObject = re.getObject(f.getColumnName());
                f.setValue(result, f.toFieldType(columnObject, localCache));
            }

            for (_Field f: ent.get_externals()) {
                if (f.getGenericFieldAttribute() == null) {
                    f.setValue(result, f.fill(result, f.getFieldType(), localCache));
                } else {
                    f.setValue(result, f.fill(new ArrayList<>(), result, f.getGenericFieldAttribute(), localCache));
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        return result;
    }

    /** Creates an instance by its primary keys.
     * @param <T> Type.
     * @param t Type class.
     * @param pk Primary key.
     * @return Object. */
    public static <T> T get(Class<T> t, Object pk) {
        try {
            return (T) _createObject(t, pk, null);
        }
        catch (Exception ex) { return null; }
    }

    protected static Object searchCache(Class c, Object pk, Collection<Object> localCache) throws NoSuchMethodException {
        if (localCache != null) {
            for (Object obj: localCache) {
                if (!obj.getClass().equals(c)) { continue; }
                if (_getEntity(c).getPrimaryKey().getValue(obj).equals(pk)) { return obj; }
            }
        }

        return null;
    }

}