package Testing.TestClasses;

import OrmFramework.metamodel.Orm;
import Testing.Teacher;

public class ModifyObject {

    public static void show() {
        System.out.println("(2) Load and modify object");
        System.out.println("--------------------------");

        Teacher t = Orm.get(Teacher.class, "t.0");

        System.out.println("Salary for " + t.getFirstName() + " " + t.getName() + " is " + Integer.toString(t.getSalary()) + " Pesos.");

        System.out.println("Give raise of 12000.");
        t.setSalary(t.getSalary() + 12000);

        System.out.println("Salary for " + t.getFirstName() + " " + t.getName() + " is now " + Integer.toString(t.getSalary()) + " Pesos.");

        try {
            Orm.save(t, false);
            System.out.println();
        }
        catch(Exception ex) { ex.printStackTrace(); }

        System.out.println("\n");
    }
}
