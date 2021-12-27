package Testing;

import OrmFramework.*;
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
    protected String id;

    /** Name. */
    @Getter
    @Setter
    @FieldAnnotation
    protected String name;

    /** Teacher. */
    @Getter
    @Setter
    @ManyToOne(columnName = "KTEACHER")
    protected Teacher teacher;

    @Override
    public String toString() {
        return "SClass{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", teacher=" + teacher.getId() +
                '}';
    }
}
