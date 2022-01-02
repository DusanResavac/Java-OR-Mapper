import OrmFramework.metamodel.Orm;
import OrmFramework.metamodel.QueryOperations;
import OrmFramework.metamodel._Entity;
import OrmFramework.metamodel._Field;
import Testing.*;
import Testing.Subtests.Mausrad;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class OrmMetamodelTest {

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
    @DisplayName("Get entity teacher test")
    public void test() throws NoSuchMethodException {
        // Arrange
        _Entity ent = null;

        // Act
        ent = Orm._getEntity(Teacher.class);

        // Assert
        assertNotNull(ent);
        assertEquals("TEACHERS", ent.getTableName());
        // Person fields + Teacher fields
        assertEquals(9, ent.getFields().length);
    }

    @Test
    @DisplayName("Get entity student test")
    public void test2() throws NoSuchMethodException {
        // Arrange
        _Entity ent = null;

        // Act
        ent = Orm._getEntity(Student.class);

        // Assert
        assertNotNull(ent);
        assertEquals("STUDENTS", ent.getTableName());
        // Person fields + Teacher fields
        assertEquals(7, ent.getFields().length);
    }

    @Test
    @DisplayName("Get column type of student fields")
    public void test3() throws NoSuchMethodException {
        // Arrange
        _Entity ent = Orm._getEntity(Student.class);
        String columnTypeGender = null;
        String columnTypePK = null;
        String columnTypeGrade = null;

        // Act
        columnTypeGender = Orm.getColumnType(Arrays.stream(ent.get_internals())
                .filter(f -> f.getColumnName().equalsIgnoreCase("GENDER"))
                .collect(Collectors.toList()).get(0), false);
        columnTypeGrade = Orm.getColumnType(Arrays.stream(ent.get_internals())
                .filter(f -> f.getColumnName().equalsIgnoreCase("GRADE"))
                .collect(Collectors.toList()).get(0), false);
        columnTypePK = Orm.getColumnType(ent.getPrimaryKey(), true);

        // Assert
        assertNotNull(columnTypeGender);
        assertNotNull(columnTypeGrade);
        assertNotNull(columnTypePK);

        // Integer, because the column type was set to int.class in the Person class
        // If Nothing is set, the ORM creates and stores enums as Strings (TEXT in Database)
        assertEquals("INTEGER", columnTypeGender);
        assertEquals("INTEGER", columnTypeGrade);
        assertEquals("VARCHAR(24)", columnTypePK);
    }

    @Test
    @DisplayName("Update references of teacher's courses after they were deleted")
    public void test4() throws NoSuchMethodException, SQLException, NoSuchFieldException {
        // Arrange
        Teacher t = Orm.get(Teacher.class, "t.0");
        _Entity ent = Orm._getEntity(Teacher.class);
        ArrayList<Course> savedCourses = t.get_courses();
        t.set_courses(new ArrayList<>());
        _Field courseField = Arrays.stream(ent.get_externals())
                .filter(extField -> extField.getName().equals("_courses"))
                .collect(Collectors.toList()).get(0);

        // Act
        courseField.updateReference(t);
        t = Orm.get(Teacher.class, "t.0");
        List<Course> courses = Orm.from(Course.class)
                .is("KTEACHER", QueryOperations.EQUALS, "t.0")
                .get();

        // Assert
        assertNotNull(t);
        assertNotNull(courses);
        assertEquals(0, courses.size());
        assertEquals(0, t.get_courses().size());

        // Tidy up
        t.set_courses(savedCourses);
        courseField.updateReference(t);
    }

    @Test
    @DisplayName("Test filling of external field in 1:n relation")
    public void test5() throws Exception {
        // Arrange
        Teacher t = Orm.get(Teacher.class, "t.0");
        t.setClasses(null);
        _Field extClassesField = Arrays.stream(Orm._getEntity(t.getClass()).get_externals())
                .filter(f -> f.getName().equals("classes"))
                .collect(Collectors.toList()).get(0);

        // Act
        extClassesField.setValue(t, extClassesField.fill(new ArrayList<Teacher>(), t, extClassesField.getFieldType(), null));

        // Assert
        assertNotNull(t);
        // Classes should be filled
        assertEquals(2, t.getClasses().size());
    }

    @Test
    @DisplayName("Test filling of external field in n:m relation")
    public void test6() throws Exception {
        // Arrange
        Student s = Orm.get(Student.class, "s.2");
        s.setCourses(null);
        _Field extClassesField = Arrays.stream(Orm._getEntity(s.getClass()).get_externals())
                .filter(f -> f.getName().equals("courses"))
                .collect(Collectors.toList()).get(0);

        // Act
        extClassesField.setValue(s, extClassesField.fill(new ArrayList<Student>(), s, extClassesField.getFieldType(), null));

        // Assert
        assertNotNull(s);
        // Courses should be filled
        assertEquals(1, s.getCourses().size());
        // Course should have this student (s.2) and s.0
        assertEquals(2, s.getCourses().get(0).getStudents().size());
    }

    @Test
    @DisplayName("Test filling of external field in 1:1 relation")
    public void test7() throws Exception {
        // Arrange
        Mausrad mr = Orm.get(Mausrad.class, "mr.0");
        mr.setMaus(null);
        _Field extClassesField = Arrays.stream(Orm._getEntity(mr.getClass()).get_externals())
                .filter(f -> f.getName().equals("maus"))
                .collect(Collectors.toList()).get(0);

        // Act
        extClassesField.setValue(mr, extClassesField.setExternalField(mr, extClassesField.getFieldType(), null));

        // Assert
        assertNotNull(mr);
        // maus should be inserted
        assertNotNull(mr.getMaus());
        // maus should also have mausrad
        assertNotNull(mr.getMaus().getMausrad());
    }

    @Test
    @DisplayName("Test converting a field to column type")
    public void test8() throws Exception {
        // Arrange
        SClass c = Orm.get(SClass.class, "c.0");
        Student s = Orm.get(Student.class, "s.0");
        _Entity sClassEnt = Orm._getEntity(c);
        _Entity studentEnt = Orm._getEntity(s);

        _Field classId  = Arrays.stream(sClassEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("id"))
                .collect(Collectors.toList()).get(0);
        _Field teacher  = Arrays.stream(sClassEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("TEACHER"))
                .collect(Collectors.toList()).get(0);
        _Field grade = Arrays.stream(studentEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("GRADE"))
                .collect(Collectors.toList()).get(0);
        _Field gender = Arrays.stream(studentEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("GENDER"))
                .collect(Collectors.toList()).get(0);
        _Field bdate = Arrays.stream(studentEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("birthDate"))
                .collect(Collectors.toList()).get(0);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Act
        Object classIdColumn = classId.toColumnType(classId.getValue(c));
        Object teacherColumn = teacher.toColumnType(teacher.getValue(c));
        Object gradeColumn = grade.toColumnType(grade.getValue(s));
        Object genderColumn = gender.toColumnType(gender.getValue(s));
        Object bdateColumn = bdate.toColumnType(bdate.getValue(s));


        // Assert
        assertNotNull(classIdColumn);
        assertNotNull(teacherColumn);
        assertNotNull(gradeColumn);
        assertNotNull(genderColumn);
        assertNotNull(bdateColumn);

        assertEquals("c.0", classIdColumn);
        assertEquals("t.0", teacherColumn);
        assertEquals(1, gradeColumn);
        assertEquals(Gender.MALE.ordinal(), genderColumn);
        assertEquals(format.format(s.getBirthDate().getTime()), bdateColumn);
        assertEquals("1977-04-10 00:00:00", bdateColumn);
    }

    @Test
    @DisplayName("Test converting a field to field type")
    public void test9() throws Exception {
        // Arrange
        SClass c = Orm.get(SClass.class, "c.0");
        Student s = Orm.get(Student.class, "s.0");
        _Entity sClassEnt = Orm._getEntity(c);
        _Entity studentEnt = Orm._getEntity(s);

        _Field classId  = Arrays.stream(sClassEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("id"))
                .collect(Collectors.toList()).get(0);
        _Field teacher  = Arrays.stream(sClassEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("TEACHER"))
                .collect(Collectors.toList()).get(0);
        _Field grade = Arrays.stream(studentEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("GRADE"))
                .collect(Collectors.toList()).get(0);
        _Field gender = Arrays.stream(studentEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("GENDER"))
                .collect(Collectors.toList()).get(0);
        _Field bdate = Arrays.stream(studentEnt.get_internals())
                .filter(f -> f.getName().equalsIgnoreCase("birthDate"))
                .collect(Collectors.toList()).get(0);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<Object> localCache = new ArrayList<>();

        // Act
        Object classIdField = classId.toFieldType("c.0", localCache);
        Object teacherField = teacher.toFieldType("t.0", localCache);
        Object gradeField = grade.toFieldType(1, localCache);
        Object genderField = gender.toFieldType(Gender.MALE.ordinal(), localCache);
        Object bdateField = bdate.toFieldType("1977-04-10 00:00:00", localCache);


        // Assert
        assertNotNull(classIdField);
        assertNotNull(teacherField);
        assertNotNull(gradeField);
        assertNotNull(genderField);
        assertNotNull(bdateField);

        assertEquals(Teacher.class, teacherField.getClass());
        assertTrue(genderField instanceof Gender);
        assertTrue(bdateField instanceof Calendar);

        assertEquals("c.0", classIdField);
        assertEquals("t.0", ((Teacher) teacherField).getId());
        assertEquals(1, gradeField);
        assertEquals(Gender.MALE, genderField);
        assertEquals(new GregorianCalendar(1977, Calendar.APRIL, 10), bdateField);
    }
}
