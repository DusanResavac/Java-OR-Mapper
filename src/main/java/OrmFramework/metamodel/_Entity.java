package OrmFramework.metamodel;

import OrmFramework.*;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class _Entity {
    private _Field _primaryKey;
    private Class _member;
    private String _tableName;
    private _Field[] _fields;
    @Getter
    @Setter
    private _Field[] _internals;
    @Getter
    @Setter
    private _Field[] _externals;

    public _Entity(Class t) {
        EntityAnnotation entityAttribute = (EntityAnnotation) t.getAnnotation(EntityAnnotation.class);

        if (entityAttribute == null || isNullOrWhiteSpace(entityAttribute.tableName())) {
            _tableName = t.getSimpleName().toUpperCase();
        } else {
            _tableName = entityAttribute.tableName();
        }

        _member = t;
        List<_Field> fields = new ArrayList<_Field>();

        // get all Fields (including private and inherited ones)
        List<Field> fieldList = getAllFields(t);

        for (Field reflectField: fieldList) {
            if (reflectField.getAnnotation(IgnoreAnnotation.class) != null ||
                    Modifier.isStatic(reflectField.getModifiers()) ||
                    Modifier.isTransient(reflectField.getModifiers())) {
                continue;
            }

            _Field field = null;
            try {
                field = getField(reflectField, t);
            } catch (NoSuchMethodException e) {
                System.out.println(e.getMessage());
                continue;
            }
            if (field.getColumnName() == null || field.getColumnName().isBlank()) {
                field.setColumnName(reflectField.getName().toUpperCase());
            }
            if (field.getColumnType() == null || field.getColumnType().equals(Void.class)) {
                field.setColumnType(reflectField.getType());
            }
            // Check whether the field is external (not in database) - ignore if its oneToOne
            if (field.isForeignKey() && reflectField.getAnnotation(OneToOne.class) == null) {
                //field.setExternal(Collection.class.isAssignableFrom(field.getFieldType()));
                field.setExternal(field.getRelation().equals(RelationType.ONE_TO_MANY) || field.getRelation().equals(RelationType.MANY_TO_MANY));
            }

            fields.add(field);
        }

        _fields = fields.toArray(new _Field[0]);

        set_internals(fields.stream().filter(f -> !f.isExternal()).toArray(_Field[]::new));
        set_externals(fields.stream().filter(_Field::isExternal).toArray(_Field[]::new));
    }


    public List<Field> getAllFields(Class c) {
        if (c == null) {
            return null;
        }

        ArrayList<Field> fields = new ArrayList<>(Arrays.asList(c.getDeclaredFields()));
        var returnedFields = getAllFields(c.getSuperclass());
        if (returnedFields != null) {
            fields.addAll(returnedFields);
        }

        return fields;
    }

    private _Field getField(Field reflectField, Class c) throws NoSuchMethodException {
        _Field f = new _Field();
        f.setEntity(this);
        FieldAnnotation fieldAnno = reflectField.getAnnotation(FieldAnnotation.class);
        PrimaryKeyAnnotation primaryKeyAnno = reflectField.getAnnotation(PrimaryKeyAnnotation.class);
        OneToOne oneToOne = reflectField.getAnnotation(OneToOne.class);
        OneToMany oneToMany = reflectField.getAnnotation(OneToMany.class);
        ManyToOne manyToOne = reflectField.getAnnotation(ManyToOne.class);
        ManyToMany manyToMany = reflectField.getAnnotation(ManyToMany.class);
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
        } else if (oneToOne != null) {
            // TODO: Change to remote column name if not in table
            f.setRemoteColumnName(oneToOne.remoteColumnName());
            f.setColumnType(oneToOne.columnType());
            f.setForeignKey(true);
            f.setNullable(oneToOne.nullable());
            f.setExternal(!oneToOne.isInTable());
            f.setRelation(RelationType.ONE_TO_ONE);

            getMethod = oneToOne.getMethod();
            setMethod = oneToOne.setMethod();
        } else if (oneToMany != null) {
            // TODO: Change to remote column name
            f.setRemoteColumnName(oneToMany.remoteColumnName());
            ParameterizedType t = (ParameterizedType) reflectField.getGenericType();
            f.setFieldType((Class) t.getActualTypeArguments()[0]);
            f.setForeignKey(true);
            f.setNullable(oneToMany.nullable());
            f.setRelation(RelationType.ONE_TO_MANY);

            getMethod = oneToMany.getMethod();
            setMethod = oneToMany.setMethod();
        } else if (manyToOne != null) {
            f.setColumnName(manyToOne.columnName());
            f.setColumnType(manyToOne.columnType());
            f.setRelation(RelationType.MANY_TO_ONE);
            f.setForeignKey(true);
            f.setNullable(manyToOne.nullable());

            getMethod = manyToOne.getMethod();
            setMethod = manyToOne.setMethod();
        } else if (manyToMany != null) {
            ParameterizedType t = (ParameterizedType) reflectField.getGenericType();
            f.setFieldType((Class) t.getActualTypeArguments()[0]);
            f.setAssignmentTable(manyToMany.assignmentTable());
            f.setRemoteColumnName(manyToMany.remoteColumnName());
            f.setForeignKey(true);
            f.setRelation(RelationType.MANY_TO_MANY);

            getMethod = manyToMany.getMethod();
            setMethod = manyToMany.setMethod();
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
        for (Method method: c.getMethods()) {
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

    /** Gets the entity SQL.
     * @param prefix Prefix.
     * @return SQL string. */
    public String getSQL(String prefix) {
        if (prefix == null) { prefix = ""; }
        String rval = "SELECT ";
        for (int i = 0; i < _internals.length; i++) {
            if (i > 0) { rval += ", "; }
            rval += prefix.trim() + _internals[i].getColumnName();
        }
        rval += " FROM " + _tableName;

        return rval;
    }
}
