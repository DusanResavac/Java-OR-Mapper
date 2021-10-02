package Testing;

import OrmFramework.FieldAnnotation;
import OrmFramework.IgnoreAnnotation;
import OrmFramework.PrimaryKeyAnnotation;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

/** This is a person implementation (from School example).
 * Stolen from https://github.com/robbiblubber/SWE3.Demo.JAVA/blob/master/SWE3.Demo.SampleApp/src/swe3/demo/sampleapp/Person.java
 */
public class Person {
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // protected static members                                                                                         //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Instance number counter.
     */
    protected static int _N = 1;


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // protected members                                                                                                //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * ID.
     */
    @Getter
    @Setter
    @PrimaryKeyAnnotation
    protected String _id;

    /**
     * Name.
     */
    @FieldAnnotation
    @Getter
    @Setter
    protected String _name;

    /**
     * First name.
     */
    @FieldAnnotation
    @Getter
    @Setter
    protected String _firstName;

    /**
     * Birth date.
     */
    @FieldAnnotation(columnName = "BDATE")
    @Getter
    @Setter
    protected Calendar _birthDate;

    /**
     * Gender.
     */
    @FieldAnnotation
    @Getter
    @Setter
    protected Gender _gender;

    /**
     * Instance number.
     */
    @IgnoreAnnotation
    protected int _instanceNumber = _N++;
}
