package Testing.TestClasses;

import OrmFramework.metamodel.Orm;
import Testing.Course;
import Testing.Student;
import Testing.Teacher;

import java.util.ArrayList;

public class WithNToM {

    public static void show() throws Exception {
        System.out.println("(5) Save courses with students");
        System.out.println("---------------------------------");

        Course course = new Course();
        course.setId("c.0");
        course.setName("First course");
        course.setTeacher(Orm.get(Teacher.class, "t.0"));
        Course course1 = new Course();
        course1.setId("c.1");
        course1.setName("Second course");

        Student student = new Student();
        student.setId("s.0");
        student.setFirstName("Gregor");
        student.setName("Onkh");
        Student student1 = new Student();
        student1.setId("s.1");
        student1.setFirstName("Lutz");
        student1.setName("Mayer");

        Student finalStudent = student;
        Student finalStudent1 = student1;
        Course finalCourse = course;
        course.setStudents(new ArrayList<>(){{add(finalStudent); add(finalStudent1);}});
        student.setCourses(new ArrayList<>(){{add(finalCourse); add(course1);}});

        Orm.saveWithNToMRelation(course, true);
        Orm.saveWithNToMRelation(student, true);

        student = Orm.get(Student.class, "s.0");
        student1 = Orm.get(Student.class, "s.1");
        course = Orm.get(Course.class, "c.0");

        System.out.printf("The student %s %s has the courses%n", student.getFirstName(), student.getName());
        for (Course c: student.getCourses()) {
            System.out.println(c.getName());
        }

        System.out.println();

        System.out.printf("The student %s %s has the courses%n", student1.getFirstName(), student1.getName());
        for (Course c: student1.getCourses()) {
            System.out.println(c.getName());
        }

        System.out.println();

        System.out.printf("The course %s has the following students%n", course.getName());
        for (Student s: course.getStudents()) {
            System.out.println(s.getFirstName() + " " + s.getName());
        }

        System.out.println();

    }
}
