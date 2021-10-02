package Testing;

import OrmFramework.EntityAnnotation;

import java.util.Calendar;

/** This is a student implementation (from School example).
 * Stolen from https://github.com/robbiblubber/SWE3.Demo.JAVA/blob/master/SWE3.Demo.SampleApp/src/swe3/demo/sampleapp/Student.java*/
@EntityAnnotation(tableName = "STUDENTS")
public class Student extends Person
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // protected members                                                                                                //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Salary. */
    protected int _grade;

    /** Hire date. */
    protected Calendar _hireDate;
}
