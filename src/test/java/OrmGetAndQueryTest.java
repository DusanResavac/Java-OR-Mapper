import OrmFramework.metamodel.Orm;
import OrmFramework.metamodel.QueryOperations;
import Testing.SClass;
import Testing.Student;
import Testing.Teacher;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class OrmGetAndQueryTest {

    @BeforeAll
    public static void beforeAll() throws SQLException, NoSuchMethodException {
        Orm.connect("jdbc:mariadb://localhost:3306/school?user=root&password=");
        // Very unfortunate, but class ordering seemed too much of a hassle and the get Test-class was always
        // executed first, which is why the tests of the save class are executed manually (and therefore multiple times)
        OrmSaveTest.beforeAll();
        var b = new OrmSaveTest();
        b.testSave();
        b.testSave2();
        b.testSave3();
        b.testSave4();
        b.testSave5();
        b.testSave6();
        b.testSave7();
    }

    @Test
    @DisplayName("Get all students")
    public void test1() throws SQLException, NoSuchFieldException, NoSuchMethodException {
        // Arrange
        List<Student> students = null;

        // Act
        students = Orm.from(Student.class).get();

        // Assert
        assertNotNull(students);
        assertEquals(3, students.size());
        assertEquals("s.0", students.get(0).getId());
        assertEquals("s.1", students.get(1).getId());
        assertEquals("s.2", students.get(2).getId());
    }

    @Test
    @DisplayName("Get all students who are in grade 2 or higher and are born before 1989")
    public void test2() throws SQLException, NoSuchFieldException, NoSuchMethodException {
        // Arrange
        List<Student> students = null;

        // Act
        students = Orm.from(Student.class)
                .is("Grade", QueryOperations.GREATER_THAN_OR_EQUAL, 2)
                .and()
                .is("BDATE", QueryOperations.LESS_THAN, new GregorianCalendar(1989, Calendar.JANUARY, 1))
                .get();

        // Assert
        assertNotNull(students);
        assertEquals(1, students.size());
        assertEquals("s.2", students.get(0).getId());
    }

    @Test
    @DisplayName("Get all classes except \"Apex Legends Masters\" and \"Clay Pottery\"")
    public void test3() throws SQLException, NoSuchFieldException, NoSuchMethodException {
        // Arrange
        List<SClass> classes = null;

        // Act
        classes = Orm.from(SClass.class)
                .isNot("Name", QueryOperations.IN, "Apex Legends Masters", "Clay Pottery")
                .get();

        // Assert
        assertNotNull(classes);
        assertEquals(1, classes.size());
        assertEquals("c.1", classes.get(0).getId());
    }

    @Test
    @DisplayName("Get the teacher who teaches the course \"TFT Economy Basics\"")
    public void test4() throws SQLException, NoSuchFieldException, NoSuchMethodException {
        // Arrange
        List<Teacher> teachers = null;

        // Act
        teachers = Orm.from(Teacher.class)
                .getWithRawSQL("t.", " t " +
                        "join courses c on (c.kteacher = t.id) " +
                        "where c.name = ?", "TFT Economy Basics");

        // Assert
        assertNotNull(teachers);
        assertEquals(1, teachers.size());
        assertEquals("t.0", teachers.get(0).getId());
    }
}
