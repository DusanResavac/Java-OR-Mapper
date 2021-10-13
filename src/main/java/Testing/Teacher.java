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
    @Getter
    @Setter
    @FieldAnnotation
    protected int salary;

    /** Hire date. */
    @Getter
    @Setter
    @FieldAnnotation(columnName = "HDATE")
    protected Calendar _hireDate;

    /** Classes. */
    protected ArrayList<SClass> _classes;

    /** Courses. */
    protected ArrayList<Course> _courses;

}
