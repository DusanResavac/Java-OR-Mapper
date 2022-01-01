package OrmFramework.metamodel;

import OrmFramework.RelationType;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static OrmFramework.metamodel.Orm.getConnection;

public class _Field {
    private _Entity entity;
    private Method _get;
    private Method _set;
    private String _name;
    private Class _fieldType;
    /*private Class _genericFieldAttribute;*/

    private Class _columnType;
    private String _columnName;
    private boolean _isPk = false;
    private boolean _isFk = false;
    private boolean _isNullable = true;
    // for foreign keys
    private boolean _isExternal = false;
    private String assignmentTable;
    private String remoteColumnName;
    private RelationType relation;


    public _Field(_Entity entity, String name) {
        this.entity = entity;
        this._name = name;
    }

    public _Field() {

    }

    /**
     * Returns an object that fits the field type / class by converting or casting the parameter based on its class
     * @param value Object
     * @param localCache Collection<Object>
     * @return Object converted to field type
     */
    public Object toFieldType (Object value, Collection<Object> localCache) throws Exception {

        if (_isFk) {
            return Orm._createObject(_fieldType, value, localCache);
        }

        // Problem with primitive data types - might not to be changed
        if (_fieldType.equals(boolean.class) || _fieldType.equals(Boolean.class)) {
            if (value.getClass().equals(int.class) || value.getClass().equals(Integer.class)) {
                return ((int) value) != 0;
            }
            if (value.getClass().equals(short.class) || value.getClass().equals(Short.class)) {
                return ((short) value) != 0;
            }
            if (value.getClass().equals(long.class) || value.getClass().equals(Long.class)) {
                return ((short) value) != 0;
            }
        }

        if (_fieldType.equals(short.class)) { return (short) value; }
        if (_fieldType.equals(int.class))   { return (int)   value; }
        if (_fieldType.equals(long.class))  { return (long)  value; }

        if (_fieldType.isEnum()) {
            if(value instanceof String) { return Enum.valueOf(_fieldType, (String) value); }
            return value == null ? null : _fieldType.getEnumConstants()[(int) value];
        }

        if (_fieldType.equals(Calendar.class) && value != null) {
            Calendar rval = Calendar.getInstance();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try
            {
                rval.setTime(f.parse(value.toString()));
                return rval;
            }
            catch (Exception ex) {
                System.err.println("_Field - toFieldType(): Error occurred while converting to Calender object");
            }
        }

        return value;
    }

    /**
     * Returns an object that fits the column type / class by converting or casting the parameter based on its class
     * @param obj Object
     * @return Object converted to column type
     */
    public Object toColumnType(Object obj) throws NoSuchMethodException {
        // if this field is a foreign key, convert the value of the primary key this field is referencing
        if (_isFk) {
            _Field fk = Orm._getEntity(_fieldType).getPrimaryKey();
            return fk.toColumnType(fk.getValue(obj));
        }

        if (_columnType.equals(Calendar.class)) {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return obj == null ? null : f.format(((Calendar) obj).getTime());
        }

        if (obj == null) { return null; }

        if (obj.getClass().isEnum()) {
            if (_columnType.equals(int.class) || _columnType.equals(Integer.class)) {
                return ((Enum) obj).ordinal();
            } else {
                return obj.toString();
            }
        }

        if (_fieldType.equals(_columnType)) { return obj; }

        if (obj.getClass().equals(boolean.class) || obj.getClass().equals(Boolean.class)) {
            if(_columnType.equals(int.class))   { return         (((boolean) obj) ? 1 : 0); }
            if(_columnType.equals(short.class)) { return (short) (((boolean) obj) ? 1 : 0); }
            if(_columnType.equals(long.class))  { return (long)  (((boolean) obj) ? 1 : 0); }
        }

        return obj;
    }


    public Object getValue(Object obj) {
        try
        {
            return _get.invoke(obj);
        }
        catch (Exception ex) { return null; }
    }

    public void setValue(Object obj, Object value) throws Exception {
        if (_columnType.equals(Calendar.class)) {
            if (value != null && value.getClass().equals(String.class)) {
                Calendar rval = Calendar.getInstance();
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (((String) value).length() == 10) { value = (((String) value) + " 12:00:00"); }

                try
                {
                    rval.setTime(f.parse(value.toString()));
                    value = rval;
                }
                catch(Exception ex) {
                    System.err.println("_Field - setValue(Object obj, Object value): Error occurred while converting to Calender object");
                }
            }
        }

        _set.invoke(obj, value);
    }

    /**
     * Fills list of the 1 element in an 1:n relation
     * @param list list to be filled with the n elements.
     * @param obj the 1 object.
     * @param c the class of the n elements.
     * @param localCache caching to prevent accidental recursions.
     * @return list with the n elements.
     * @throws NoSuchMethodException
     * @throws SQLException
     */
    public List fill (List list, Object obj, Class c, Collection<Object> localCache) throws NoSuchMethodException, SQLException {
        String select;

        if (relation.equals(RelationType.MANY_TO_MANY)) {
            _Entity fillEnt = Orm._getEntity(c);

            String fillRemoteColumnName = null;

            for (_Field external: fillEnt.get_externals()) {
                if (external.getRelation().equals(RelationType.MANY_TO_MANY) && external.getAssignmentTable().equals(assignmentTable)) {
                    fillRemoteColumnName = external.getRemoteColumnName();
                }
            }

            select = fillEnt.getSQL(null) + " WHERE " + fillEnt.getPrimaryKey().getColumnName() + " IN (SELECT " + fillRemoteColumnName + " FROM " + assignmentTable + " WHERE " + remoteColumnName + " = ?)";
        } else {
            select = Orm._getEntity(c).getSQL(null) + " WHERE " + remoteColumnName + " = ?";
        }

        PreparedStatement prepStmt = getConnection().prepareStatement(select);
        prepStmt.setObject(1, entity.getPrimaryKey().getValue(obj));

        ResultSet resultSet = prepStmt.executeQuery();

        // loop through all n elements.
        while (resultSet.next()) {
            try {
                // Create n element or retrieve from cache
                Object oneOfNObjects = Orm._createObject(c, resultSet, localCache);
                // add n element
                list.getClass().getMethod("add", Object.class).invoke(list, oneOfNObjects);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    /**
     * In an 1:1 relation sets the external field of the table which doesn't store
     * the foreign key in the table
     * @param obj - Object
     * @param c - Class
     * @param localCache - Collection<Object> Cache
     * @return Object
     * @throws NoSuchMethodException
     * @throws SQLException
     */
    public Object setExternalField (Object obj, Class c, Collection<Object> localCache) throws NoSuchMethodException, SQLException {
        String select = Orm._getEntity(c).getSQL(null) + " WHERE " + remoteColumnName + " = ?";
        PreparedStatement prepStmt = getConnection().prepareStatement(select);
        prepStmt.setObject(1, entity.getPrimaryKey().getValue(obj));

        ResultSet resultSet = prepStmt.executeQuery();

        // expect only one other element
        if (resultSet.next()) {
            try {
                // Create 1 element or retrieve from cache
                return Orm._createObject(c, resultSet, localCache);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Update references of object (external fields). The foreign key columns of existing entries will be set to null and
     * then updated.
     * @param obj Object
     */
    public void updateReference(Object obj) throws NoSuchMethodException, SQLException {
        if (!_isExternal) return;

        _Entity externalEntity = Orm._getEntity(_fieldType);
        Object pk = entity.getPrimaryKey().toColumnType(entity.getPrimaryKey().getValue(obj));

        // update other side if it is external (not in the own table)
        if (relation.equals(RelationType.ONE_TO_ONE) || relation.equals(RelationType.ONE_TO_MANY)) {
            _Field remoteField = null;

            // get Field by name
            for (_Field f : externalEntity.get_internals()) {
                if (f.getColumnName().equalsIgnoreCase(remoteColumnName)) {
                    remoteField = f;
                    break;
                }
            }

            // delete current connection
            if (remoteField.isNullable()) {
                PreparedStatement cmd = Orm.getConnection().prepareStatement("UPDATE " + externalEntity.getTableName() + " SET " + remoteColumnName + " = NULL WHERE " + remoteColumnName + " = ?");
                cmd.setObject(1, pk);
                cmd.execute();
                cmd.close();
            }
        }

        switch (relation) {
            case MANY_TO_MANY -> {
                // delete all existing entries for this object and create them again
                // remoteColumnName = KCOURSE for example
                PreparedStatement cmd = Orm.getConnection().prepareStatement("DELETE FROM " + assignmentTable + " WHERE " + remoteColumnName + " = ?");
                cmd.setObject(1, pk);
                cmd.execute();
                cmd.close();


                if (getValue(obj) != null) {
                    // externalRemoteField = e.g. External field of student that saves the n:m relation (remoteColumnName = KSTUDENT)
                    _Field externalRemoteField = null;
                    for (_Field f: externalEntity.get_externals()) {
                        if (f.assignmentTable.equalsIgnoreCase(assignmentTable)) {
                            externalRemoteField = f;
                            break;
                        }
                    }

                    for (Object i : (Iterable) getValue(obj)) {
                        cmd = Orm.getConnection().prepareStatement("INSERT INTO " + assignmentTable + "(" + remoteColumnName + ", " + externalRemoteField.remoteColumnName + ") VALUES (?, ?)");
                        cmd.setObject(1, pk);
                        cmd.setObject(2, externalEntity.getPrimaryKey().toColumnType(externalEntity.getPrimaryKey().getValue(i)));
                        cmd.execute();
                        cmd.close();
                    }
                }
            }
            case ONE_TO_MANY -> {
                Object externalObject = getValue(obj);

                if (externalObject != null) {
                    for (Object i : (Iterable) externalObject) {
                        PreparedStatement cmd = Orm.getConnection().prepareStatement("UPDATE " + externalEntity.getTableName() + " SET " + remoteColumnName + " = ? WHERE " + externalEntity.getPrimaryKey().getColumnName() + " = ?");
                        cmd.setObject(1, pk);
                        cmd.setObject(2, externalEntity.getPrimaryKey().toColumnType(externalEntity.getPrimaryKey().getValue(i)));
                        cmd.execute();
                        cmd.close();
                    }
                }
            }
            case ONE_TO_ONE -> {
                Object externalPk = externalEntity.getPrimaryKey().toColumnType(externalEntity.getPrimaryKey().getValue(getValue(obj)));

                // Only update to new relation if such a relation exists
                if (externalPk != null) {
                    PreparedStatement cmd = Orm.getConnection().prepareStatement("UPDATE " + externalEntity.getTableName() + " SET " + remoteColumnName + " = ? WHERE " + externalEntity.getPrimaryKey().getColumnName() + " = ?");
                    cmd.setObject(1, pk);
                    cmd.setObject(2, externalPk);
                    cmd.execute();
                    cmd.close();
                }

            }
        }
    }

    public _Entity getEntity() {
        return entity;
    }

    public void setEntity(_Entity entity) {
        this.entity = entity;
    }

    public Method getGetMethod() {
        return _get;
    }

    public void setGetMethod(Method _get) {
        this._get = _get;
    }

    public Method getSetMethod() {
        return _set;
    }

    public void setSetMethod(Method _set) {
        this._set = _set;
    }

    public String getName() {
        return _name;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public Class getFieldType() {
        return _fieldType;
    }

    public void setFieldType(Class _fieldType) {
        this._fieldType = _fieldType;
    }

    public Class getColumnType() {
        return _columnType;
    }

    public void setColumnType(Class _columnType) {
        this._columnType = _columnType;
    }

    public String getColumnName() {
        return _columnName;
    }

    public void setColumnName(String _columnName) {
        this._columnName = _columnName;
    }

    public boolean isPrimaryKey() {
        return _isPk;
    }

    public void setPrimaryKey(boolean _isPk) {
        this._isPk = _isPk;
    }

    public boolean isForeignKey() {
        return _isFk;
    }

    public void setForeignKey(boolean _isFk) {
        this._isFk = _isFk;
    }

    public boolean isNullable() {
        return _isNullable;
    }

    public void setNullable(boolean _isNullable) {
        this._isNullable = _isNullable;
    }

    /*public Class getGenericFieldAttribute() {
        return _genericFieldAttribute;
    }

    public void setGenericFieldAttribute(Class _genericFieldAttribute) {
        this._genericFieldAttribute = _genericFieldAttribute;
    }*/

    public boolean isExternal() {
        return _isExternal;
    }

    public void setExternal(boolean _isExternal) {
        this._isExternal = _isExternal;
    }

    public String getAssignmentTable() {
        return assignmentTable;
    }

    public void setAssignmentTable(String assignmentTable) {
        this.assignmentTable = assignmentTable;
    }

    public String getRemoteColumnName() {
        return remoteColumnName;
    }

    public void setRemoteColumnName(String remoteColumnName) {
        this.remoteColumnName = remoteColumnName;
    }

    public RelationType getRelation() {
        return relation;
    }

    public void setRelation(RelationType relation) {
        this.relation = relation;
    }


}
