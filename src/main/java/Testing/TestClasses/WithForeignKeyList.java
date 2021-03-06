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
        SClass clazz2 = new SClass();
        clazz2.setName("Mathematics");
        clazz2.setId("c.2");
        clazz2.setTeacher(t);

        Orm.save(clazz, false);
        System.out.println();
        Orm.save(clazz2, false);
        System.out.println();
        /*
        TO TEST EXAMPLE SET NULLABLE IN SCLASS TO TRUE
        Example showing that tracking cache works for that specific edge case:
        class has teacher t.0 and sets it to null (without saving)
            -> teacher t.0 is being retrieved with Orm.get (and put into the cache)
            -> class is saved
            -> teacher t.0 will still be in cache
        to prevent this problem, the internals are checked and foreign keys are removed from the cache

        t = Orm.get(Teacher.class, "t.0");
        clazz2.setTeacher(null);
        Orm.save(clazz2, false);*/

        t = Orm.get(Teacher.class, "t.0");

        System.out.printf("%s %s teaches:\n", t.getFirstName(), t.getName());

        for (SClass c: t.getClasses()) {
            System.out.println(c.getName());
        }

        System.out.println("\n");
    }

}
