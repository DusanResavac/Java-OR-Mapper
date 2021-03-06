package Testing;

import OrmFramework.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/** This class represents a course in the school model.
 * https://github.com/robbiblubber/SWE3.Demo.JAVA/blob/master/SWE3.Demo.SampleApp/src/swe3/demo/sampleapp/Course.java
 * */
@EntityAnnotation(tableName = "COURSES")
public class Course
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // protected members                                                                                                //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** ID. */
    @Getter
    @Setter
    @PrimaryKeyAnnotation
    protected String id;

    /** Name. */
    @Getter
    @Setter
    @FieldAnnotation
    protected String name;

    /** Active flag. */
    @Getter
    @Setter
    @FieldAnnotation
    protected boolean active;

    /** Teacher. */
    @Getter
    @Setter
    @ManyToOne(columnName = "KTEACHER")
    protected Teacher teacher;

    /** Students. */
    @Getter
    @Setter
    @ManyToMany(assignmentTable = "STUDENTS_COURSES", remoteColumnName = "KCOURSE")
    protected ArrayList<Student> students;

    @Override
    public String toString() {
        return "Course{" +
                "name='" + name + '\'' +
                '}';
    }
}
