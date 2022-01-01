package Testing.TestClasses;

import OrmFramework.TrackingCache;
import OrmFramework.metamodel.Orm;
import Testing.Subtests.Maus;
import Testing.Teacher;

public class WithCache {

    public static void show() {
        System.out.println("(6) Cache demonstration");
        System.out.println("-----------------------");

        System.out.println("\rWithout cache:");
        Orm.set_cache(null);
        _showInstances();

        System.out.println("\rWith cache:");
        Orm.set_cache(new TrackingCache());
        _showInstances();

        System.out.println("\n");
    }

    /**
     * Shows instances
     */
    private static void _showInstances() {
        // split for easier readability in console
        for (int i = 0; i < 7; i++) {
            Maus m = Orm.get(Maus.class, 1);
            System.out.printf("Maus [%s] instance no: %d %n", m.getId(), m.get_instanceNumber());
        }
        for (int i = 0; i < 7; i++) {
            Teacher t = Orm.get(Teacher.class, "t.0");
            System.out.printf("Teacher [%s] instance no: %d %n", t.getId(), t.get_instanceNumber());
        }
    }
}
