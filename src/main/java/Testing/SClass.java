package Testing;

import OrmFramework.EntityAnnotation;
import OrmFramework.FieldAnnotation;
import OrmFramework.ForeignKeyAnnotation;
import OrmFramework.PrimaryKeyAnnotation;
import lombok.Getter;
import lombok.Setter;

/** This class represents a class in the school model.
 * Stolen from https://github.com/robbiblubber/SWE3.Demo.JAVA/blob/master/SWE3.Demo.SampleApp/src/swe3/demo/sampleapp/SClass.java
 * */
@EntityAnnotation(tableName = "CLASSES")
public class SClass
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

    /** Teacher. */
    @Getter
    @Setter
    @ForeignKeyAnnotation(columnName = "KTEACHER")
    protected Teacher _teacher;
}
