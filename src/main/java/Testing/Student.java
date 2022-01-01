package Testing;

import OrmFramework.EntityAnnotation;
import OrmFramework.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;
import java.util.List;

/** This is a student implementation (from School example).
 * Stolen from https://github.com/robbiblubber/SWE3.Demo.JAVA/blob/master/SWE3.Demo.SampleApp/src/swe3/demo/sampleapp/Student.java*/
@EntityAnnotation(tableName = "STUDENTS")
@AllArgsConstructor
@NoArgsConstructor
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

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", firstName='" + firstName + '\'' +
                ", gender=" + gender +
                ", grade=" + grade +
                ", courses=" + courses +
                '}';
    }
}
