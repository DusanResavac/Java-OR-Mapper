package OrmFramework.metamodel;

import OrmFramework.Cache;
import OrmFramework.LogFormatter;
import OrmFramework.NMRelation;
import OrmFramework.RelationType;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;

import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Orm {

    private static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Map<Class, _Entity> _entities = new HashMap<>();
    /** Database connection. */
    private static Connection _connection;
    @Getter @Setter
    private static Cache _cache = null;

    public static void clearTables (String schema) throws SQLException {
        PreparedStatement prepStmt = _connection.prepareStatement(
                "SELECT table_name" + " FROM " + "information_schema.tables " +
                        "WHERE" + " table_schema = ?" );
        prepStmt.setString(1, schema);
        ResultSet resultSet = prepStmt.executeQuery();

        while (resultSet.next()) {
            PreparedStatement prepStmt2 = _connection.prepareStatement("DELETE FROM " + resultSet.getString(1));
            prepStmt2.execute();
            prepStmt2.close();
            log.info("DELETE FROM " + resultSet.getString(1) + System.lineSeparator());
        }

        prepStmt.close();
    }

    /**
     * Creates all tables in the order they are entered. In case of n:m relations additional tables will be generated.
     * @param classes
     * @throws NoSuchMethodException
     * @throws SQLException
     */
    public static void createTables (Class<?>... classes) throws NoSuchMethodException, SQLException {
        Map<String, NMRelation> nmRelations = new HashMap<>();

        for (Class<?> c: classes) {
            _Entity ent = _getEntity(c);
            StringBuilder create = new StringBuilder("CREATE TABLE ")
                    .append(ent.getTableName())
                    .append("(\n");


            // Create the internal fields first
            for (_Field f: ent.get_internals()) {
                create.append(f.getColumnName()).append(" ");
                String columnType = getColumnType(f, f.isForeignKey() || f.isPrimaryKey());

                create.append(columnType).append(" ");

                if (f.isPrimaryKey()) {
                    create.append("PRIMARY KEY ");
                }
                if (!f.isNullable()) {
                    create.append("not null ");
                }

                create.append(",\n");

                if (f.isForeignKey()) {
                    create.append("FOREIGN KEY (")
                            .append(f.getColumnName())
                            .append(")")
                            .append(" REFERENCES ")
                            .append(_getEntity(f.getFieldType()).getTableName())
                            .append(" (")
                            .append(_getEntity(f.getFieldType()).getPrimaryKey().getColumnName())
                            .append("),\n");
                }
            }

            create.delete(create.length()-2, create.length()).append(");");

            PreparedStatement prepStmt = Orm.getConnection().prepareStatement(create.toString());
            prepStmt.execute();
            prepStmt.close();

            System.out.println(create);
            System.out.println();

            // save all n:m tables that need to be created
            for (_Field f: ent.get_externals()) {
                if (f.getRelation().equals(RelationType.MANY_TO_MANY)) {
                    // if the key already exists, set the second part of the relation
                    if (nmRelations.containsKey(f.getAssignmentTable())) {
                        NMRelation temp = nmRelations.get(f.getAssignmentTable());
                        temp.setEB(ent);
                        temp.setFB(f);
                        nmRelations.put(f.getAssignmentTable(), temp);
                    } else {
                        nmRelations.put(f.getAssignmentTable(), new NMRelation(ent, f, null, null));
                    }
                }
            }
        }

        // create all n:m tables
        for (Map.Entry<String, NMRelation> entry : nmRelations.entrySet()) {
            String assignmentTable = entry.getKey();
            NMRelation nmRelation = entry.getValue();
            StringBuilder create = new StringBuilder("CREATE TABLE ")
                    .append(assignmentTable).append(" (\n")
                    .append(nmRelation.getFA().getRemoteColumnName())
                    .append(" ")
                    .append(getColumnType(nmRelation.getEA().getPrimaryKey(), true))
                    .append(" not null,\n");
            create.append(nmRelation.getFB().getRemoteColumnName())
                    .append(" ")
                    .append(getColumnType(nmRelation.getEB().getPrimaryKey(), true))
                    .append(" not null,\n");
            create.append("PRIMARY KEY (")
                    .append(nmRelation.getFA().getRemoteColumnName())
                    .append(", ")
                    .append(nmRelation.getFB().getRemoteColumnName())
                    .append("),\n");
            create.append("FOREIGN KEY (")
                    .append(nmRelation.getFA().getRemoteColumnName())
                    .append(") REFERENCES ")
                    .append(nmRelation.getEA().getTableName())
                    .append(" (").append(nmRelation.getEA().getPrimaryKey().getColumnName()).append(")")
                    .append(" ON DELETE CASCADE,\n");
            create.append("FOREIGN KEY (")
                    .append(nmRelation.getFB().getRemoteColumnName())
                    .append(") REFERENCES ")
                    .append(nmRelation.getEB().getTableName())
                    .append(" (").append(nmRelation.getEB().getPrimaryKey().getColumnName()).append(")")
                    .append(" ON DELETE CASCADE);");


            PreparedStatement prepStmt = Orm.getConnection().prepareStatement(create.toString());
            prepStmt.execute();
            prepStmt.close();
            System.out.println(create);
            System.out.println();
        }
    }

    /**
     * Drop all tables specified in the schema
     * @param schema
     * @throws SQLException
     */
    public static void deleteTables(String schema) throws SQLException {
        PreparedStatement prepStmt = _connection.prepareStatement(
                "SELECT table_name" + " FROM " + "information_schema.tables " +
                        "WHERE" + " table_schema = ?" );
        prepStmt.setString(1, schema);
        ResultSet resultSet = prepStmt.executeQuery();
        try {
            _connection.setAutoCommit(false);
            _connection.commit();
            PreparedStatement prepStmt2 = _connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 0");
            prepStmt2.execute();
            prepStmt2.close();
            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                System.out.println("DROP TABLE IF EXISTS " + tableName + System.lineSeparator());
                prepStmt2 = _connection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
                prepStmt2.execute();
                prepStmt2.close();
            }
            prepStmt2 = _connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 1");
            prepStmt2.execute();
            prepStmt2.close();
        } catch (SQLException e) {
            _connection.rollback();
            e.printStackTrace();
        } finally {
            _connection.setAutoCommit(true);
            prepStmt.close();
        }
    }

    /**
     * Get database column type from specific _Field object.
     * @param f
     * @param isForeignOrPrimaryKey
     * @return
     * @throws NoSuchMethodException
     */
    public static String getColumnType(_Field f, boolean isForeignOrPrimaryKey) throws NoSuchMethodException {

        // switch statement doesn't work with classes?
        if (f.getColumnType().equals(int.class) || f.getColumnType().equals(Integer.class)) {
            return "INTEGER";
        }
        if (f.getColumnType().equals(String.class) || f.getColumnType().isEnum()) {
            return isForeignOrPrimaryKey ? "VARCHAR(24)" : "TEXT";
        }
        if (f.getColumnType().equals(boolean.class) || f.getColumnType().equals(Boolean.class)) {
            return "BOOLEAN";
        }
        if (f.getColumnType().equals(float.class) || f.getColumnType().equals(Float.class)) {
            return "FLOAT";
        }
        if (f.getColumnType().equals(double.class) || f.getColumnType().equals(Double.class)) {
            return "DOUBLE";
        }
        if (f.getColumnType().equals(Calendar.class)) {
            return "DATETIME";
        }

        // F is probably a referenced object -> get Primary key and get its column type
        return getColumnType(_getEntity(f.getColumnType()).getPrimaryKey(), isForeignOrPrimaryKey);
    }

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


    public static void setLevel (Level level) {
        if (log.getHandlers().length == 1) {
            log.getHandlers()[0].setLevel(level);
            log.setLevel(level);
        } else {
            ConsoleHandler handler = new ConsoleHandler();
            Formatter formatter = new LogFormatter();
            handler.setFormatter(formatter);
            handler.setLevel(level);
            log.addHandler(handler);
            log.setLevel(level);
        }
    }

    /** Connects to a database.
     * @param url Connection URL.
     * @throws SQLException Thrown when the connection could not be established. */
    public static void connect(String url) throws SQLException {
        log.setUseParentHandlers(false);
        setLevel(Level.INFO);
        DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
        _connection = DriverManager.getConnection(url);
    }

    /**
     * Saves an object into the database and updates the direct foreign key references (eg. in 1:N - One teacher can teach multiple classes.
     * If the classes in a teacher object are changed and the teacher is updated, the remote column in the classes table will be updated.).
     * This behaviour can also be extended recursively, if the second parameter is set to true. It that case the foreign key objects
     * and their references and foreign keys will be updated. This makes it possible to call this method once without worrying about
     * dependency order.
     * @param obj Object that should be saved
     * @param changeReferencedObjects boolean whether the save should happen recursively for all references / foreign keys
     * @throws NoSuchMethodException thrown when an entity cannot be found
     * @throws SQLException thrown when an SQL error occurs
     */
    public static void save(Object obj, boolean changeReferencedObjects) throws NoSuchMethodException, SQLException {
        log.info("--- saving - " + obj.getClass() + " - " + _getEntity(obj).getPrimaryKey().getValue(obj));
        _save(obj, changeReferencedObjects, null);

        // Update references, after all objects are created / updated
        _updateReference(obj, changeReferencedObjects, null);

    }

    /**
     * Internal method that updates the references / foreign keys of the specified object.
     * E.g. in 1:N - One teacher can teach multiple classes.
     * If the classes in a teacher object are changed and the teacher is updated, the remote column in the classes table will be updated
     * (Since that teacher for example does no longer teach a certain class). The second parameter "updateReferencesRecursively" should
     * be set with care, because it can overwrite changes involuntarily (previous save operations) if the references are not up-to-date.
     * @param obj Object of which the references should be updated
     * @param updateReferencesRecursively boolean whether the references should be updated recursively (also of other references)
     * @param localCache Collection<Object> should be set to null if there is no specific reason to do otherwise
     * @throws NoSuchMethodException thrown when an entity is not found
     * @throws SQLException thrown when an SQL exception is encountered
     */
    private static void _updateReference(Object obj, boolean updateReferencesRecursively, Collection<Object> localCache) throws NoSuchMethodException, SQLException {
        if (obj == null) {
            return;
        }

        _Entity ent = _getEntity(obj);

        if (localCache == null) {
            localCache = new ArrayList<>();
        } else {
            for (Object o: localCache) {
                if (!o.getClass().equals(obj.getClass())) { continue; }
                Object pkO = ent.getPrimaryKey().getValue(o);
                Object pkObj = ent.getPrimaryKey().getValue(obj);
                // object already saved
                if (pkO.equals(pkObj)) { return; }
            }
        }

        localCache.add(obj);

        for (_Field f: ent.get_externals()) {
            // create or update references of foreign keys
            if (updateReferencesRecursively) {
                Object referencedObject = f.getValue(obj);
                // if it's not a list only update the object otherwise update each element of the list
                if (!(referencedObject instanceof Collection<?>)) {
                    _updateReference(referencedObject, true, localCache);
                } else {
                    for (Object i : (Iterable) referencedObject) {
                        _updateReference(i, true, localCache);
                    }
                }
            }

            var temp = f.getValue(obj);
            if (temp instanceof Collection<?>) {
                for (Object o : (Iterable) temp) {
                    if (o != null) {
                        log.fine("updating references - " + obj.getClass() + " : " + _getEntity(obj).getPrimaryKey().getValue(obj) + " " + f.getRelation() + " " + f.getFieldType() + " : " + _getEntity(o).getPrimaryKey().getValue(o));
                    }
                }
            } else {
                Object pkOfReference = temp == null ? null : _getEntity(temp).getPrimaryKey().getValue(temp);
                log.fine("updating references - " + obj.getClass() + " : " + _getEntity(obj).getPrimaryKey().getValue(obj) + " " + f.getRelation() + " " + f.getFieldType() + " : " + pkOfReference);
            }

            f.updateReference(obj);
        }

        // remove foreign key objects from cache
        for (_Field f: ent.get_internals()) {
            if (f.isForeignKey() && _cache != null) {
                // Since the references are updated in another method, the cache can't know if the references changed ->
                // therefore if the save method is invoked, all objects whose references are updated need to be removed
                Object foreignObject = f.getValue(obj);
                if (foreignObject != null) {
                    log.fine("removing from cache: " + _getEntity(foreignObject).getTableName() + " : " + _getEntity(foreignObject).getPrimaryKey().getValue(foreignObject));
                    _cache.remove(foreignObject);

                    // if the foreign object is null and the CACHE of the primary object still contains a value,
                    // the foreign object in the cache of the object needs to be removed from the cache
                    // e.g.: class has teacher t.0 and sets it to null (without saving)
                    // -> teacher t.0 is being retrieved with Orm.get (and put into the cache)
                    // -> class is saved
                    // -> teacher t.0 will still be in cache
                    // to prevent this, check cache and remove the old foreign object teacher t.0
                } else if (_cache.contains(obj.getClass(), _getEntity(obj).getPrimaryKey().getValue(obj))) {
                    Object oldObject = _cache.get(obj.getClass(), _getEntity(obj).getPrimaryKey().getValue(obj));
                    Object oldForeignObject = f.getValue(oldObject);
                    if (oldForeignObject != null) {
                        log.fine("removing from cache: " + _getEntity(oldForeignObject).getTableName() + " : " + _getEntity(oldForeignObject).getPrimaryKey().getValue(oldForeignObject));
                        _cache.remove(oldForeignObject);
                    }
                }
            }
        }

        if(_cache != null) {
            // Since the references are updated in another method, the cache can't know if the references changed ->
            // therefore if the save method is invoked, all objects whose references are updated need to be removed
            log.fine("removing from cache: " +  _getEntity(obj).getTableName() + " : " + _getEntity(obj).getPrimaryKey().getValue(obj));
            _cache.remove(obj);
        }
    }

    /**
     * Saves an object and its foreign key objects. It is also possible to save the objects of the foreign keys if the second parameter
     * "changeReferencedObjects" is set to true. This way all foreign keys / references will be saved recursively.
     * ATTENTION: This method should not be used directly since it doesn't update the references / foreign key columns (key constraints) -
     * instead use the public save method.
     * @param obj Object that should be saved
     * @param changeReferencedObjects boolean whether the save should occur recursively for all foreign key objects / references
     * @param localCache Collection<Object> should be set to null if there is no specific reason to do otherwise
     * @throws NoSuchMethodException thrown when an entity is not found
     * @throws SQLException thrown when an SQL exception is encountered
     */
    private static void _save(Object obj, boolean changeReferencedObjects, Collection<Object> localCache) throws NoSuchMethodException, SQLException {

        if (obj == null) {
            return;
        }

        _Entity ent = _getEntity(obj);

        if (localCache == null) {
            localCache = new ArrayList<>();
        }


        for (Object o: localCache) {
            if (!o.getClass().equals(obj.getClass())) { continue; }
            Object pkO = ent.getPrimaryKey().getValue(o);
            Object pkObj = ent.getPrimaryKey().getValue(obj);
            // object already saved
            if (pkO.equals(pkObj)) { return; }
        }

        localCache.add(obj);

        if (changeReferencedObjects) {
            for (_Field f : ent.getFields()) {
                // create or update reference
                if (f.isForeignKey()) {
                    Object referencedObject = f.getValue(obj);
                    // if it's not a list only save the object otherwise save each element of the list
                    if (!(referencedObject instanceof Collection<?>)) {
                        _save(f.getValue(obj), true, localCache);
                    } else {
                        for (Object i : (Iterable) referencedObject) {
                            _save(i, true, localCache);
                        }
                    }
                }
            }
        }


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


        log.fine("saved - " + obj.getClass() + " : " + _getEntity(obj).getPrimaryKey().getValue(obj));

    }

    /**
     * Deletes an object
     * @param obj Object which should be deleted
     * @throws NoSuchMethodException thrown when an entity is not found
     * @throws SQLException thrown when an SQL exception is encountered
     */
    public static void delete(Object obj) throws NoSuchMethodException, SQLException {
        _Entity ent = _getEntity(obj);

        PreparedStatement cmd = getConnection().prepareStatement("DELETE FROM " + ent.getTableName() + " WHERE " + ent.getPrimaryKey().getColumnName() + " = ?");
        cmd.setObject(1, ent.getPrimaryKey().getValue(obj));
        cmd.execute();
        cmd.close();

        if(_cache != null) { _cache.remove(obj); }

    }

    /**
     * Creates an SQLQuery instance.
     * @param c Class
     * @return SQLQuery
     */
    public static SQLQuery from (Class c) {
        return new SQLQuery(c);
    }

    /** Creates an instance by its primary key.
     * @param c Class.
     * @param pk Primary key.
     * @return Object.
     */
    protected static Object _createObject(Class c, Object pk, Collection<Object> localCache) throws Exception {

        if (pk == null) {
            return null;
        }

        // search cache since a request might be saved
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

    /** Creates an object from a database result set
     * @param c Class
     * @param re Result set
     * @return Object
     */
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

            if (_cache != null) {
                log.fine("putting in cache " +  _getEntity(result).getTableName() + " : " + _getEntity(result).getPrimaryKey().getValue(result));
                _cache.put(result);
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        return result;
    }

    /** Creates an instance by its primary keys.
     * @param <T> Type
     * @param t Type class
     * @param pk Primary key
     * @return Object
     */
    public static <T> T get(Class<T> t, Object pk) {
        try {
            return (T) _createObject(t, pk, null);
        }
        catch (Exception ex) { return null; }
    }

    protected static Object searchCache(Class c, Object pk, Collection<Object> localCache) throws NoSuchMethodException {

        if (_cache != null && _cache.contains(c, pk)) {
            return _cache.get(c, pk);
        }

        if (localCache != null) {
            for (Object obj: localCache) {
                if (!obj.getClass().equals(c)) { continue; }
                if (_getEntity(c).getPrimaryKey().getValue(obj).equals(pk)) { return obj; }
            }
        }

        return null;
    }

}
