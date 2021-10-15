package Testing.TestClasses;

import OrmFramework.metamodel.Orm;
import Testing.SClass;
import Testing.Teacher;

public class WithForeignKeyList {

    public static void show() throws Exception {
        System.out.println("(4) Load teacher and show classes");
        System.out.println("---------------------------------");

        Teacher t = Orm.get(Teacher.class, "t.0");

        SClass clazz = new SClass();
        clazz.setName("Machine Learning 2");
        clazz.setId("c.1");
        clazz.setTeacher(t);

        Orm.save(clazz);

        t = Orm.get(Teacher.class, "t.0");

        System.out.printf("%s %s teaches:\n", t.getFirstName(), t.getName());

        for (SClass c: t.getClasses()) {
            System.out.println(c.getName());
        }

        System.out.println();
    }

}
