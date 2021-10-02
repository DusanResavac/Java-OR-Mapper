package OrmFramework.metamodel;

import java.lang.reflect.Method;

public class _Field {
    private _Entity entity;
    private Method _get;
    private Method _set;
    private String _name;
    private Class _fieldType;

    private Class _columnType;
    private String _columnName;
    private boolean _isPk = false;
    private boolean _isFk = false;
    private boolean _isNullable = true;

    public _Field(_Entity entity, String name) {
        this.entity = entity;
        this._name = name;
    }

    public _Field() {

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
}
