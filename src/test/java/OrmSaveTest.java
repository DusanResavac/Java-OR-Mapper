import OrmFramework.metamodel.Orm;
import Testing.*;
import Testing.Subtests.Material;
import Testing.Subtests.Maus;
import Testing.Subtests.Mausrad;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrmSaveTest {

    @BeforeAll
    public static void beforeAll() throws SQLException {
        Orm.connect("jdbc:mariadb://localhost:3306/school?user=root&password=");
        Orm.clearTables("school");
    }

    @Test
    @Order(1)
    @DisplayName("Save student object which doesn't have references")
    public void testSave() throws SQLException, NoSuchMethodException {
        // Arrange
        Student s = new Student();
        s.setId("s.0");
        s.setFirstName("Gregor");
        s.setName("Onkh");
        s.setGrade(1);
        s.setGender(Gender.MALE);
        s.setBirthDate(new GregorianCalendar(1977, Calendar.APRIL, 10));

        // Act
        Orm.save(s, false);
        s = Orm.get(Student.class, "s.0");

        // Assert
        assertNotNull(s);
        assertEquals("Gregor", s.getFirstName());
        assertEquals("Onkh", s.getName());
        assertEquals(1, s.getGrade());
        assertEquals(Gender.MALE, s.getGender());
        assertEquals(new GregorianCalendar(1977, Calendar.APRIL, 10), s.getBirthDate());
    }

    @Test
    @Order(2)
    @DisplayName("Save teacher object which doesn't have references")
    public void testSave2() throws SQLException, NoSuchMethodException {
        // Arrange
        Teacher t = new Teacher();
        t.setId("t.0");
        t.setFirstName("Connor");
        t.setName("Colquhoun");
        t.setGender(Gender.MALE);
        t.setBirthDate(new GregorianCalendar(1996, Calendar.JULY, 26));
        t.set_hireDate(new GregorianCalendar(2015, Calendar.FEBRUARY, 1));


        // Act
        Orm.save(t, false);
        t = Orm.get(Teacher.class, "t.0");

        // Assert
        assertNotNull(t);
        assertEquals("Connor", t.getFirstName());
        assertEquals("Colquhoun", t.getName());
        assertEquals(Gender.MALE, t.getGender());
        assertEquals(new GregorianCalendar(1996, Calendar.JULY, 26), t.getBirthDate());
        assertEquals(new GregorianCalendar(2015, Calendar.FEBRUARY, 1), t.get_hireDate());
    }

    @Test
    @Order(3)
    @DisplayName("Save sclass object with references 1:n from the n-side manually")
    public void testSave3() throws SQLException, NoSuchMethodException {
        // Arrange
        SClass c = new SClass();
        c.setId("c.0");
        c.setName("Apex Legends Masters");
        c.setTeacher(Orm.get(Teacher.class, "t.0"));


        // Act
        Orm.save(c, false);
        c = Orm.get(SClass.class, "c.0");

        // Assert
        assertNotNull(c);
        assertEquals("t.0", c.getTeacher().getId());
        assertEquals("Connor", c.getTeacher().getFirstName());
        assertEquals("Colquhoun", c.getTeacher().getName());
        assertEquals("Apex Legends Masters", c.getName());
        assertEquals("c.0", c.getId());
    }

    @Test
    @Order(4)
    @DisplayName("Save teacher object with references 1:n from the \"1\"-side manually")
    public void testSave4() throws SQLException, NoSuchMethodException {
        // Arrange
        Teacher t = Orm.get(Teacher.class, "t.0");
        SClass c1 = new SClass();
        c1.setId("c.1");
        c1.setName("TFT Noobs");
        c1.setTeacher(t);
        t.getClasses().add(c1);


        // Act
        Orm.save(c1, false);
        Orm.save(t, false);
        t = Orm.get(Teacher.class, "t.0");

        // Assert
        assertNotNull(t);
        assertEquals("t.0", t.getId());
        assertEquals(2, t.getClasses().size());
        assertEquals("Apex Legends Masters", t.getClasses().get(0).getName());
        assertEquals("TFT Noobs", t.getClasses().get(1).getName());
    }

    @Test
    @Order(5)
    @DisplayName("Save course object with references n:m manually")
    public void testSave5() throws SQLException, NoSuchMethodException {
        // Arrange
        Course c = new Course();
        c.setId("c.0");
        c.setName("Apex Movement 101");
        c.setTeacher(Orm.get(Teacher.class, "t.0"));
        c.setActive(true);
        Student s1 = new Student();
        s1.setId("s.1");
        s1.setFirstName("Katarina");
        s1.setName("Huber");
        s1.setGrade(4);
        s1.setGender(Gender.FEMALE);
        s1.setBirthDate(new GregorianCalendar(1989, Calendar.AUGUST, 21));


        // Act
        Orm.save(c, false);
        c.setStudents(new ArrayList<>(){{ add(Orm.get(Student.class, "s.0")); add(s1); }});
        Orm.save(s1, false);
        Orm.save(c, false);
        c = Orm.get(Course.class, "c.0");

        // Assert
        assertNotNull(c);
        assertEquals("c.0", c.getId());
        assertEquals(2, c.getStudents().size());
        assertEquals("s.0", c.getStudents().get(0).getId());
        assertEquals("s.1", c.getStudents().get(1).getId());
    }

    @Test
    @Order(6)
    @DisplayName("Save course object with references 1:n automatically")
    public void testSave6() throws SQLException, NoSuchMethodException {
        // Arrange
        Maus m = new Maus();
        m.setId(1);
        m.setMaterial(Material.PLASTIC);
        Mausrad mr = new Mausrad();
        mr.setId("mr.0");
        mr.setMaterial(Material.METALL);
        m.setMausrad(mr);

        // Act
        Orm.save(m, true);
        m = Orm.get(Maus.class, 1);

        // Assert
        assertNotNull(m);
        assertEquals(1, m.getId());
        assertEquals("mr.0", m.getMausrad().getId());
        assertEquals(Material.PLASTIC, m.getMaterial());
        assertEquals(Material.METALL, m.getMausrad().getMaterial());
    }

    @Test
    @Order(7)
    @DisplayName("Save course object with references n:m automatically")
    public void testSave7() throws SQLException, NoSuchMethodException {
        // Arrange
        Course c1 = new Course();
        c1.setId("c.1");
        c1.setName("TFT Economy Basics");
        c1.setTeacher(Orm.get(Teacher.class, "t.0"));
        c1.setActive(true);
        Student s2 = new Student();
        s2.setId("s.2");
        s2.setFirstName("Nyatasha");
        s2.setName("Nyanners");
        s2.setGrade(2);
        s2.setGender(Gender.FEMALE);
        s2.setBirthDate(new GregorianCalendar(1988, Calendar.MARCH, 12));
        c1.setStudents(new ArrayList<>(){{ add(Orm.get(Student.class, "s.0")); add(s2); }});

        // Act
        Orm.save(c1, true);
        c1 = Orm.get(Course.class, "c.1");

        // Assert
        assertNotNull(c1);
        assertEquals("c.1", c1.getId());
        assertEquals(2, c1.getStudents().size());
        assertEquals("s.0", c1.getStudents().get(0).getId());
        assertEquals("s.2", c1.getStudents().get(1).getId());
    }
}
