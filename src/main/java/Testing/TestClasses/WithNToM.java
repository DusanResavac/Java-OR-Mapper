package Testing.TestClasses;

import OrmFramework.metamodel.Orm;
import Testing.Course;
import Testing.Gender;
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

        Student finalStudent = student;
        Student finalStudent1 = student1;
        Course finalCourse = course;
        course.setStudents(new ArrayList<>(){{add(finalStudent); add(finalStudent1);}});
        student.setCourses(new ArrayList<>(){{add(finalCourse); add(course1);}});

        Orm.saveWithNToMRelation(course, true);
        Orm.saveWithNToMRelation(student, true);
        Orm.save(student2);

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
