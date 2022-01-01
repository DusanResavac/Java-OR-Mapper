package Testing.TestClasses;

import OrmFramework.metamodel.Orm;
import Testing.Gender;
import Testing.Teacher;

import java.util.Calendar;

public class InsertObject {
    public static void show() {
        System.out.println("(1) Insert object");
        System.out.println("-----------------");

        Teacher t = new Teacher();

        t.setId("t.0");
        t.setFirstName("Jerry");
        t.setName("Mouse");
        t.setGender(Gender.MALE);

        Calendar bdate = Calendar.getInstance();
        bdate.set(1970, 8, 18);
        t.setBirthDate(bdate);

        Calendar hdate = Calendar.getInstance();
        hdate.set(2015, 6, 20);
        t.set_hireDate(hdate);

        t.setSalary(50000);

        try
        {
            Orm.save(t, false);
            System.out.println();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println("Failed to save: " + ex.getMessage());
        }

        System.out.println("\n");
    }
}
