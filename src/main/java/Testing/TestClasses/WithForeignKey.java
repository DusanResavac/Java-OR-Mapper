package Testing.TestClasses;

import OrmFramework.metamodel.Orm;
import Testing.SClass;
import Testing.Teacher;

public class WithForeignKey {

    public static void show() throws Exception {
        System.out.println("(3) Load teacher and show classes");
        System.out.println("---------------------------------");

        Teacher t = Orm.get(Teacher.class, "t.0");
        SClass c = new SClass();
        c.setId("c.0");
        c.setName("Demonology 101");
        c.setTeacher(t);

        Orm.save(c);

        c = Orm.get(SClass.class, "c.0");
        System.out.printf("Teacher of %s is %s%n%n", c.getName(), c.getTeacher().getName());
    }

}
