package Testing.TestClasses;

import OrmFramework.metamodel.Orm;
import Testing.Course;
import Testing.Gender;
import Testing.Student;
import Testing.Teacher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class WithNToM {

    public static void show() throws Exception {
        System.out.println("(5) Save courses with students");
        System.out.println("---------------------------------");

        Teacher teacher2 = new Teacher();
        teacher2.setId("t.2");
        teacher2.setSalary(20_000);
        teacher2.set_hireDate(new GregorianCalendar(2005, Calendar.JANUARY, 1));
        teacher2.setFirstName("George");
        teacher2.setName("Muster");
        Course course = new Course();
        course.setId("c.0");
        course.setName("First course");
        course.setTeacher(Orm.get(Teacher.class, "t.0"));
        Course course1 = new Course();
        course1.setId("c.1");
        course1.setName("Second course");
        course1.setTeacher(teacher2);

        Student student = new Student();
        student.setId("s.0");
        student.setFirstName("Gregor");
        student.setName("Onkh");
        student.setGender(Gender.MALE);
        student.setGrade(4);
        Student student1 = new Student();
        student1.setId("s.1");
        student1.setFirstName("Lutz");
        student1.setName("Mayer");
        student1.setGender(Gender.MALE);
        student1.setGrade(1);
        Student student2 = new Student();
        student2.setId("s.2");
        student2.setFirstName("Lilly");
        student2.setName("Huber");
        student2.setGender(Gender.FEMALE);
        student2.setGrade(2);

        course.setStudents(new ArrayList<>(){{add(student); add(student1);}});
        student.setCourses(new ArrayList<>(){{add(course); add(course1);}});
        student2.setCourses(new ArrayList<>(){{add(course1);}});

        /*
         *   AUTOMATICALLY
         **/

        // second parameter: create the students too.
        // notice that course1 is also going to be created, since "student" is enrolled in course as well as in course1
        // teacher2 will also be created (since he is the teacher of course1)
        Orm.save(course, true);
        System.out.println();
        // student doesn't have any courses that need to be created -> secondParameter = false
        Orm.save(student, false);
        System.out.println();
        // changing the second parameter to true would overwrite student's course1 entry (in students_courses),
        // since the course1 that was added to student2 doesn't have "student" in the students list
        Orm.save(student2, false);
        System.out.println();


        /*
        *   MANUALLY
        **/
        // Instead of creating references automatically, you can also do it manually:
        Course course2 = new Course();
        course2.setId("c.2");
        course2.setName("Some cool new course");
        Course course3 = new Course();
        course3.setId("c.3");
        course3.setName("Another cool course");
        Teacher teacher3 = new Teacher();
        teacher3.setId("t.3");
        teacher3.setFirstName("Andi");
        teacher3.setName("Kaiser");
        teacher3.setSalary(85_000);
        teacher3.set_hireDate(new GregorianCalendar(2013, Calendar.SEPTEMBER, 1));

        // to do it manually, you need to save the courses and the teacher first
        Orm.save(course2, false);
        System.out.println();
        Orm.save(course3, false);
        System.out.println();
        Orm.save(teacher3, false);
        System.out.println();

        // Now add the references
        student2.getCourses().add(course2);
        student2.getCourses().add(course3);
        course3.setTeacher(teacher3);
        course3.setStudents(new ArrayList<>(){{add(student2);}});

        // now save with references
        Orm.save(student2, false);
        System.out.println();
        Orm.save(course3, false);
        System.out.println();

        Student getStudent = Orm.get(Student.class, "s.0");
        Student getStudent1 = Orm.get(Student.class, "s.1");
        Student getStudent2 = Orm.get(Student.class, "s.2");
        Course getCourse = Orm.get(Course.class, "c.0");
        Course getCourse1 = Orm.get(Course.class, "c.1");

        System.out.println();
        System.out.printf("The student %s %s has the courses%n", getStudent.getFirstName(), getStudent.getName());
        for (Course c: getStudent.getCourses()) {
            System.out.println(c.getName());
        }

        System.out.println();

        System.out.printf("The student %s %s has the courses%n", getStudent1.getFirstName(), getStudent1.getName());
        for (Course c: getStudent1.getCourses()) {
            System.out.println(c.getName());
        }

        System.out.println();

        System.out.printf("The student %s %s has the courses%n", getStudent2.getFirstName(), getStudent2.getName());
        for (Course c: getStudent2.getCourses()) {
            System.out.println(c.getName());
        }

        System.out.println();

        System.out.printf("The course %s has the following students%n", getCourse.getName());
        for (Student s: getCourse.getStudents()) {
            System.out.println(s.getFirstName() + " " + s.getName());
        }

        System.out.println();

        System.out.printf("The course %s has the following students%n", getCourse1.getName());
        for (Student s: getCourse1.getStudents()) {
            System.out.println(s.getFirstName() + " " + s.getName());
        }

        System.out.println("\n");

    }
}
