package OrmFramework.metamodel;

import OrmFramework.RelationType;

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
        _Field[] internals = ent.get_internals();

        boolean first = true;
        for (int i = 0; i < internals.length; i++) {
            if (i > 0) { insert.append(", "); insertVars.append(", "); }
            insert.append(internals[i].getColumnName());
            insertVars.append("?");
            parametersInsert.add(internals[i].toColumnType(internals[i].getValue(obj)));

            // In case of a conflict, update all fields except for the primary key
            if (!internals[i].isPrimaryKey()) {
                if (first) { first = false; } else { update.append(", "); }
                update.append(internals[i].getColumnName()).append(" = ?");
                parametersUpdate.add(internals[i].toColumnType(internals[i].getValue(obj)));
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

        /*for (_Field external: ent.get_externals()) {
            if (external.getRelation().equals(FieldType.MANY_TO_MANY)) {
                insert.setLength(0);
                String delete = "DELETE FROM " + external.getAssignmentTable() + " WHERE " + external.getRemoteColumnName() + " = ?";

                preparedStatement = getConnection().prepareStatement(delete);
                preparedStatement.setObject(1, ent.getPrimaryKey().toColumnType(ent.getPrimaryKey().getValue(obj)));
                preparedStatement.execute();
                preparedStatement.close();

                // to save the counterpart of the n to m relation, cast it to list and save it
                // e.g. if a student with multiple courses is saved, insert into the n:m table all the courses which this student visits
                List externalCollection = (List) external.getValue(obj);
                for (Object externalItem: externalCollection) {
                    _Entity extItemEnt = Orm._getEntity(externalItem);
                    Object mRemoteColumnName = null;
                    for (var externalsOtherSide: extItemEnt.get_externals()) {
                        if (externalsOtherSide.getRelation().equals(FieldType.MANY_TO_MANY) && externalsOtherSide.getAssignmentTable().equals(external.getAssignmentTable())) {
                            mRemoteColumnName = externalsOtherSide.getRemoteColumnName();
                        }
                    }

                    String insertString = "INSERT INTO " + external.getAssignmentTable() +
                            " (" + external.getRemoteColumnName() + ", " + mRemoteColumnName + ") VALUES (?, ?)";

                    preparedStatement = getConnection().prepareStatement(insertString);
                    preparedStatement.setObject(1, ent.getPrimaryKey().toColumnType(ent.getPrimaryKey().getValue(obj)));
                    preparedStatement.setObject(2, extItemEnt.getPrimaryKey().toColumnType(extItemEnt.getPrimaryKey().getValue(externalItem)));
                    preparedStatement.execute();
                    preparedStatement.close();

                }

                *//*preparedStatement = getConnection().prepareStatement(insert.toString());
                preparedStatement.setObject(1, );*//*
            }
        }*/
    }

    public static void saveWithNToMRelation(Object obj, boolean createOrUpdateCounterpart) throws Exception {

        _Entity ent = _getEntity(obj);

        // save the object regularly
        save(obj);

        PreparedStatement preparedStatement;

        // create n:m inserts for the assigned table
        for (_Field external: ent.get_externals()) {
            if (external.getRelation().equals(RelationType.MANY_TO_MANY)) {
                String delete = "DELETE FROM " + external.getAssignmentTable() + " WHERE " + external.getRemoteColumnName() + " = ?";

                preparedStatement = getConnection().prepareStatement(delete);
                preparedStatement.setObject(1, ent.getPrimaryKey().toColumnType(ent.getPrimaryKey().getValue(obj)));
                preparedStatement.execute();
                preparedStatement.close();

                // to save the counterpart of the n to m relation, cast it to list and save it
                // e.g. if a student with multiple courses is saved, insert into the n:m table all the courses which this student visits
                List externalCollection = (List) external.getValue(obj);
                if (externalCollection == null) {
                    return;
                }
                for (Object externalListItem: externalCollection) {
                    _Entity extItemEnt = Orm._getEntity(externalListItem);
                    Object mRemoteColumnName = null;
                    for (var externalsOtherSide: extItemEnt.get_externals()) {
                        if (externalsOtherSide.getRelation().equals(RelationType.MANY_TO_MANY) && externalsOtherSide.getAssignmentTable().equals(external.getAssignmentTable())) {
                            mRemoteColumnName = externalsOtherSide.getRemoteColumnName();
                        }
                    }

                    // if this is true, it replaces or creates the elements on the other side of the n:m relation
                    if (createOrUpdateCounterpart) {
                        save(externalListItem);
                    }

                    String insertString = "INSERT INTO " + external.getAssignmentTable() +
                            " (" + external.getRemoteColumnName() + ", " + mRemoteColumnName + ") VALUES (?, ?)";

                    preparedStatement = getConnection().prepareStatement(insertString);
                    preparedStatement.setObject(1, ent.getPrimaryKey().toColumnType(ent.getPrimaryKey().getValue(obj)));
                    preparedStatement.setObject(2, extItemEnt.getPrimaryKey().toColumnType(extItemEnt.getPrimaryKey().getValue(externalListItem)));
                    preparedStatement.execute();
                    preparedStatement.close();

                }
            }
        }
    }

    public static void delete(Object obj) throws NoSuchMethodException {
        _Entity ent = _getEntity(obj);

        try
        {
            PreparedStatement cmd = getConnection().prepareStatement("DELETE FROM " + ent.getTableName() + " WHERE " + ent.getPrimaryKey().getColumnName() + " = ?");
            cmd.setObject(1, ent.getPrimaryKey().getValue(obj));
            cmd.execute();
            cmd.close();
        }
        catch(Exception ex) {}

    }

    public static SQLQuery from (Class c) {
        return new SQLQuery(c);
    }

    /** Creates an instance by its primary key.
     * @param c Class.
     * @param pk Primary key.
     * @return Object. */
    protected static Object _createObject(Class c, Object pk, Collection<Object> localCache) throws Exception {

        if (pk == null) {
            return null;
        }

        Object result = searchCache(c, pk, localCache);

        if (result == null) {

            _Entity ent = _getEntity(c);

            // select * from Class where pk = ?
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
            } else {
                return result;
            }

            // set property values that are listed in the table
            for (_Field f: ent.get_internals()) {
                Object columnObject = re.getObject(f.getColumnName());
                f.setValue(result, f.toFieldType(columnObject, localCache));
            }

            // set values that come from other tables
            for (_Field f: ent.get_externals()) {
                switch (f.getRelation()) {
                    case MANY_TO_MANY -> f.setValue(result, f.fill(new ArrayList<>(), result, f.getFieldType(), localCache));
                    case ONE_TO_MANY -> f.setValue(result, f.fill(new ArrayList<>(), result, f.getFieldType(), localCache));
                    case ONE_TO_ONE -> f.setValue(result, f.setExternalField(result, f.getFieldType(), localCache));
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
