package Testing.TestClasses;

import OrmFramework.metamodel.Orm;
import OrmFramework.metamodel.QueryOperations;
import Testing.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

public class WithQuery {
    public static void show() throws Exception {
        System.out.println("(7) Query demonstration");
        System.out.println("---------------------------------");

        // Get all students
        List<Student> students = Orm.from(Student.class).get();

        System.out.println("*** Get all students");
        //System.out.println(students);
        System.out.println(students.stream().map(s -> s.getFirstName() + " " + s.getName()).collect(Collectors.joining("\n")));
        System.out.println("---");

        // Get all classes except for Maths class
        List<SClass> classes = Orm.from(SClass.class)
                .isNot("NAME", QueryOperations.LIKE, "Mathematics")
                .get();

        System.out.println("*** Get all classes except for Maths class");
        //System.out.println(classes);
        System.out.println(classes.stream().map(c -> String.format("%s lectured by %s %s", c.getName(), c.getTeacher().getFirstName(), c.getTeacher().getName())).collect(Collectors.joining("\n")));
        System.out.println("---");

        // Select all teachers who earn less than 100_000 Peso and were hired before july 2015
        List<Teacher> teachers = Orm.from(Teacher.class)
                .is("Salary", QueryOperations.LESS_THAN, 100_000)
                .and()
                .isNot("HDATE", QueryOperations.GREATER_THAN_OR_EQUAL, new GregorianCalendar(2015, Calendar.JULY, 1))
                .get();

        System.out.println("*** Select all teachers who earn less than 100_000 Peso and were hired before july 2015");
        //System.out.println(teachers);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(teachers.stream().map(t -> String.format("%s %s earns %d Peso and was hired %s", t.getFirstName(), t.getName(), t.getSalary(), format.format(t.get_hireDate().getTime()))).collect(Collectors.joining("\n")));
        System.out.println("---");

        // Select all students who visit the course called "First course" and whose gender is male
        List<Student> students2 = Orm.from(Student.class)
                .getWithRawSQL("s.", " s " +
                        "join students_courses sc on (s.ID = sc.KSTUDENT) " +
                        "join courses c on (sc.KCOURSE = c.ID) " +
                        "where c.NAME = ? and s.GENDER = ?", "First course", Gender.MALE.ordinal()); // Needs to be converted to database datatype

        System.out.println("*** Select all students who visit the course called \"First course\" and whose gender is male");
        //System.out.println(students2);
        System.out.println(students2.stream()
                .map(s ->
                        String.format("%s %s is %s and visits ", s.getFirstName(), s.getName(), s.getGender()) +
                                s.getCourses().stream().map(c -> c.getName()).collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n")));
        System.out.println("---");

        // Select all students who visit the course called "Second course"
        List<Student> students3 = Orm.from(Student.class)
                .getWithRawSQL("s.", " s " +
                        "join students_courses sc on (s.ID = sc.KSTUDENT) " +
                        "join courses c on (sc.KCOURSE = c.ID) " +
                        "where c.NAME = 'Second course'");

        System.out.println("*** Select all students who visit the course called \"Second course\"");
        //System.out.println(students3);
        System.out.println(students3.stream()
                .map(s ->
                        String.format("%s %s is %s and visits ", s.getFirstName(), s.getName(), s.getGender()) +
                                s.getCourses().stream().map(c -> c.getName()).collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n")));
        System.out.println("---");

        // Get all MALE students who are in a grade less or equal to 2 and all FEMALE students in a grade less or equal to 1
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

        System.out.println("*** Get all MALE students who are in a grade less or equal to 2 and all FEMALE students in a grade less or equal to 1\n" + "" +
                "*** or whose name is \"Franceska\", \"Lilly\", \"Emilia\", or \"Martina\"");
        //System.out.println(students4);
        System.out.println(students4.stream().map(s -> String.format("%s %s is %s and is in grade %d", s.getFirstName(), s.getName(), s.getGender(), s.getGrade())).collect(Collectors.joining("\n")));
        System.out.println("---");


        System.out.println("\n");
    }
}
