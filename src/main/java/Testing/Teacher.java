package Testing;

import OrmFramework.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Calendar;

/** This is a teacher implementation (from School example). */
@EntityAnnotation(tableName = "TEACHERS")
public class Teacher extends Person
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // protected members                                                                                                //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Salary. */
    @Setter
    @FieldAnnotation
    protected int _salary;

    /** Hire date. */
    @Getter
    @Setter
    @FieldAnnotation(columnName = "HDATE")
    protected Calendar _hireDate;

    /** Classes. */
    @Getter
    @Setter
    @ForeignKeyAnnotation(columnName = "KTEACHER", columnType = SClass.class)
    protected ArrayList<SClass> _classes;

    /** Courses. */
    @Getter
    @Setter
    @ForeignKeyAnnotation(columnName = "KTEACHER", columnType = Course.class)
    protected ArrayList<Course> _courses;

}
