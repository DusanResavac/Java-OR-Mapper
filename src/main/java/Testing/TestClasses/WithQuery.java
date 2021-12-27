package Testing.TestClasses;

import OrmFramework.metamodel.Orm;
import OrmFramework.metamodel.QueryOperations;
import Testing.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class WithQuery {
    public static void show() throws Exception {
        System.out.println("(6) Look students up");
        System.out.println("---------------------------------");

        // Get all students
        List<Student> students = Orm.from(Student.class).get();

        System.out.println("*** Get all students");
        System.out.println(students);
        System.out.println("---");

        // Get all classes except for Maths class
        List<SClass> classes = Orm.from(SClass.class)
                .isNot("NAME", QueryOperations.LIKE, "Mathematics")
                .get();

        System.out.println("*** Get all classes except for Maths class");
        System.out.println(classes);
        System.out.println("---");

        // Select all teachers, who earn less than 100_000 Peso and were hired before september 2020
        List<Teacher> teachers = Orm.from(Teacher.class)
                .is("Salary", QueryOperations.LESS_THAN, 100_000)
                .and()
                .isNot("HDATE", QueryOperations.GREATER_THAN_OR_EQUAL, new GregorianCalendar(2020, Calendar.SEPTEMBER, 1))
                .get();

        System.out.println("*** Select all teachers, who earn less than 100_000 Peso and were hired before september 2020");
        System.out.println(teachers);
        System.out.println("---");

        // Select all students who visit the course called "First course" and whose gender is male
        List<Student> students2 = Orm.from(Student.class)
                .getWithRawSQL("s.", " s " +
                        "join students_courses sc on (s.ID = sc.KSTUDENT) " +
                        "join courses c on (sc.KCOURSE = c.ID) " +
                        "where c.NAME = ? and s.GENDER = ?", "First course", Gender.MALE.ordinal()); // Needs to be converted to database datatype

        System.out.println("*** Select all students who visit the course called \"First course\" and whose gender is male");
        System.out.println(students2);
        System.out.println("---");

        // Select all students who visit the course called "Second course"
        List<Student> students3 = Orm.from(Student.class)
                .getWithRawSQL("s.", " s " +
                        "join students_courses sc on (s.ID = sc.KSTUDENT) " +
                        "join courses c on (sc.KCOURSE = c.ID) " +
                        "where c.NAME = 'Second course'");

        System.out.println("*** Select all students who visit the course called \"Second course\"");
        System.out.println(students3);
        System.out.println("---");

        // Get all MALE students with a grade less or equal to 2 and all FEMALE students with a grade less or equal to 1
        // or whose name is "Franceska", "Lilly", "Emilia", or "Martina"
        List<Student> students4 = Orm.from(Student.class)
                .group()
                    .is("Gender", QueryOperations.EQUALS, Gender.MALE)
                    .and().is("Grade", QueryOperations.LESS_THAN_OR_EQUAL, 2)
                .groupEnd()
                .or()
                .group()
                    .is("Gender", QueryOperations.EQUALS, Gender.FEMALE)
                    .and()
                    .group()
                        .is("Grade", QueryOperations.LESS_THAN_OR_EQUAL, 1)
                        .or().is("FirstName", QueryOperations.IN, "Franceska", "Lilly", "Emilia", "Martina")
                    .groupEnd()
                .groupEnd()
                .get();

        System.out.println("*** Get all MALE students with a grade less or equal to 2 and all FEMALE students with a grade less or equal to 1\n" + "" +
                "*** or whose name is \"Franceska\", \"Lilly\", \"Emilia\", or \"Martina\"");
        System.out.println(students4);
        System.out.println("---");
    }
}
