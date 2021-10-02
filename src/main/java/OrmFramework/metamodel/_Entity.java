package OrmFramework.metamodel;

import OrmFramework.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class _Entity {
    private _Field _primaryKey;
    private Class _member;
    private String _tableName;
    private _Field[] _fields;

    public _Entity(Class t) throws NoSuchMethodException {
        EntityAnnotation entityAttribute = (EntityAnnotation) t.getAnnotation(EntityAnnotation.class);

        if (entityAttribute == null || isNullOrWhiteSpace(entityAttribute.tableName())) {
            _tableName = t.getSimpleName().toUpperCase();
        } else {
            _tableName = entityAttribute.tableName();
        }

        _member = t;
        List<_Field> fields = new ArrayList<_Field>();

        // All fields (including private ones)
        for (Field reflectField: t.getDeclaredFields()) {
            if (reflectField.getAnnotation(IgnoreAnnotation.class) != null || Modifier.isStatic(reflectField.getModifiers())) {
                continue;
            }

            _Field field = getField(reflectField, t);
            if (field.getColumnName() == null || field.getColumnName().isBlank()) {
                field.setColumnName(reflectField.getName().toUpperCase());
            }
            if (field.getColumnType() == null || field.getColumnType().equals(Void.class)) {
                field.setColumnType(reflectField.getType());
            }

            fields.add(field);
        }

        _fields = fields.toArray(new _Field[0]);
    }

    private _Field getField(Field reflectField, Class t) throws NoSuchMethodException {
        _Field f = new _Field();
        f.setEntity(this);
        FieldAnnotation fieldAnno = reflectField.getAnnotation(FieldAnnotation.class);
        PrimaryKeyAnnotation primaryKeyAnno = reflectField.getAnnotation(PrimaryKeyAnnotation.class);
        ForeignKeyAnnotation foreignKeyAnno = reflectField.getAnnotation(ForeignKeyAnnotation.class);
        String getMethod = "";
        String setMethod = "";

        f.setName(reflectField.getName());
        f.setFieldType(reflectField.getType());

        // go through annotations
        if (fieldAnno != null) {
            f.setColumnName(fieldAnno.columnName());
            f.setColumnType(fieldAnno.columnType());
            f.setNullable(fieldAnno.nullable());
            getMethod = fieldAnno.getMethod();
            setMethod = fieldAnno.setMethod();
        } else if (primaryKeyAnno != null) {
            f.setColumnName(primaryKeyAnno.columnName());
            f.setColumnType(primaryKeyAnno.columnType());
            f.setPrimaryKey(true);
            f.setNullable(false);
            getMethod = primaryKeyAnno.getMethod();
            setMethod = primaryKeyAnno.setMethod();
            // reference
            _primaryKey = f;
        } else if (foreignKeyAnno != null) {
            f.setColumnName(foreignKeyAnno.columnName());
            f.setColumnType(foreignKeyAnno.columnType());
            f.setForeignKey(true);
            f.setNullable(foreignKeyAnno.nullable());

            getMethod = foreignKeyAnno.getMethod();
            setMethod = foreignKeyAnno.setMethod();
        }

        // set get-method and set-method name, if none was specified
        if (getMethod.isBlank()) {
            getMethod = f.getFieldType().equals(boolean.class) || f.getFieldType().equals(Boolean.class) ? "is" : "get";
            getMethod += Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
        }

        if (setMethod.isBlank()) {
            setMethod = "set" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
        }


        // Now look for the setter and getter method
        for (Method method: t.getMethods()) {
            // only check public and non-static methods
            if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
                if (method.getName().equalsIgnoreCase(getMethod)) {
                    f.setGetMethod(method);
                }
                if (method.getName().equalsIgnoreCase(setMethod)) {
                    f.setSetMethod(method);
                }
            }
        }

        if (f.getGetMethod() == null) {
            throw new NoSuchMethodException(String.format("Could not find the get-method: \"%s\" for the field: \"%s\"", getMethod, reflectField.getName()));
        }
        // Should not be necessary for the primary key?
        if (f.getSetMethod() == null && !f.isPrimaryKey()) {
            throw new NoSuchMethodException(String.format("Could not find the set-method: \"%s\" for the field: \"%s\"", setMethod, reflectField.getName()));
        }

        return f;
    }

    private boolean isNullOrWhiteSpace(String str) {
        return str == null || str.isBlank();
    }


    /*
     *
     * public Getter
     *
     */

    public _Field getPrimaryKey() {
        return _primaryKey;
    }

    public Class getMember() {
        return _member;
    }

    public String getTableName() {
        return _tableName;
    }

    public _Field[] getFields() {
        return _fields;
    }
}
