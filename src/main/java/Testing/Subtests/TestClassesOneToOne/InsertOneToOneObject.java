package Testing.Subtests.TestClassesOneToOne;

import OrmFramework.metamodel.Orm;
import Testing.Subtests.Material;
import Testing.Subtests.Maus;
import Testing.Subtests.Mausrad;

public class InsertOneToOneObject {

    public static void show() throws Exception {
        Maus maus = new Maus();
        maus.setId(1);

        Mausrad mausrad = new Mausrad();
        mausrad.setId("mr.5");
        mausrad.setMaterial(Material.METALL);

        Orm.save(mausrad);

        maus.setMausrad(mausrad);
        maus.setMaterial(Material.PLASTIC);

        Orm.save(maus);

        maus = Orm.get(Maus.class, 1);
        System.out.printf("Die Maus mit der ID %d und dem Material %s hat das Mausrad mit der ID %s und dem Material %s. %n%n",
                maus.getId(),
                maus.getMaterial(),
                maus.getMausrad().getId(),
                maus.getMausrad().getMaterial());
    }
}
