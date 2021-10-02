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
    protected String _id;

    /** Name. */
    @Getter
    @Setter
    @FieldAnnotation
    protected String _name;

    /** Active flag. */
    @Getter
    @Setter
    @FieldAnnotation
    protected boolean _active;

    /** Teacher. */
    @Getter
    @Setter
    @ForeignKeyAnnotation(columnName = "KTEACHER")
    protected Teacher _teacher;

    /** Students. */
    @IgnoreAnnotation // Temporary
    protected ArrayList<Student> _students;
}
