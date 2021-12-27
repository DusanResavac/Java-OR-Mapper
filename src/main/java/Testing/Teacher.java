package Testing;

import OrmFramework.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
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
    @Getter
    @Setter
    @OneToMany(remoteColumnName = "KTEACHER")
    protected ArrayList<SClass> classes;

    /** Courses. */
    @Getter
    @Setter
    @OneToMany(remoteColumnName = "KTEACHER")
    protected ArrayList<Course> _courses;

    @Override
    public String toString() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        return "Teacher{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", firstName='" + firstName + '\'' +
                ", birthDate=" + format1.format(birthDate.getTime()) +
                ", gender=" + gender +
                ", salary=" + salary +
                ", _hireDate=" + format1.format(_hireDate.getTime()) +
                ", classes=" + classes +
                ", _courses=" + _courses +
                '}';
    }
}
