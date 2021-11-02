package Testing;

import OrmFramework.EntityAnnotation;
import OrmFramework.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.List;

/** This is a student implementation (from School example).
 * Stolen from https://github.com/robbiblubber/SWE3.Demo.JAVA/blob/master/SWE3.Demo.SampleApp/src/swe3/demo/sampleapp/Student.java*/
@EntityAnnotation(tableName = "STUDENTS")
public class Student extends Person
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // protected members                                                                                                //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Grade. */
    @Getter
    @Setter
    protected int grade;


    @Getter
    @Setter
    @ManyToMany(assignmentTable = "STUDENTS_COURSES", remoteColumnName = "KSTUDENT")
    private List<Course> courses;
}
