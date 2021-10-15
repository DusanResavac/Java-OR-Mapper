package OrmFramework.metamodel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static OrmFramework.metamodel.Orm.getConnection;

public class _Field {
    private _Entity entity;
    private Method _get;
    private Method _set;
    private String _name;
    private Class _fieldType;
    private Class _genericFieldAttribute;

    private Class _columnType;
    private String _columnName;
    private boolean _isPk = false;
    private boolean _isFk = false;
    private boolean _isNullable = true;
    // for foreign keys on the n side
    private boolean _isExternal = false;


    public _Field(_Entity entity, String name) {
        this.entity = entity;
        this._name = name;
    }

    public _Field() {

    }

    /**
     * Returns an object that fits the field type / class by converting or casting the parameter based on its class
     * @param value
     * @return
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

        if (_fieldType.equals(Calendar.class)) {
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
     * @param obj
     * @return
     */
    public Object toColumnType(Object obj) throws NoSuchMethodException {
        // if this field is a foreign key, convert the value of the primary key this field is referencing
        if (_isFk) {
            _Field fk = Orm._getEntity(_fieldType).getPrimaryKey();
            return fk.toColumnType(fk.getValue(obj));
        }

        if (_columnType.equals(Calendar.class)) {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return f.format(((Calendar) obj).getTime());
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
            if (value.getClass().equals(String.class)) {
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
        String select = Orm._getEntity(c).getSQL(null) + " WHERE " + _columnName + " = ?";
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

    public Object fill (Object obj, Class c, Collection<Object> localCache) throws NoSuchMethodException, SQLException {
        String select = Orm._getEntity(c).getSQL(null) + " WHERE " + _columnName + " = ?";
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

    public Class getGenericFieldAttribute() {
        return _genericFieldAttribute;
    }

    public void setGenericFieldAttribute(Class _genericFieldAttribute) {
        this._genericFieldAttribute = _genericFieldAttribute;
    }

    public boolean isExternal() {
        return _isExternal;
    }

    public void setExternal(boolean _isExternal) {
        this._isExternal = _isExternal;
    }
}
