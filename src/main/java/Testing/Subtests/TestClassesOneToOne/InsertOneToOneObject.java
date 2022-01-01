package Testing.Subtests.TestClassesOneToOne;

import OrmFramework.metamodel.Orm;
import Testing.Subtests.Material;
import Testing.Subtests.Maus;
import Testing.Subtests.Mausrad;

public class InsertOneToOneObject {

    public static void show() throws Exception {

        Mausrad mausrad = new Mausrad();
        mausrad.setId("mr.5");
        mausrad.setMaterial(Material.METALL);

        Maus maus = new Maus();
        maus.setId(1);
        maus.setMaterial(Material.PLASTIC);

        // even though mausrad doesn't contain the foreign key, it can still access and set maus
        mausrad.setMaus(maus);
        // it can even save the relation between mausrad and maus, even if it doesn't have the fk
        // second parameter to true, so that mouse is inserted into the database, before the relation is set
        Orm.save(mausrad, true);
        System.out.println();

        maus = Orm.get(Maus.class, 1);
        System.out.printf("Die Maus mit der ID %d und dem Material %s hat das Mausrad mit der ID %s und dem Material %s. %n%n",
                maus.getId(),
                maus.getMaterial(),
                maus.getMausrad().getId(),
                maus.getMausrad().getMaterial());

        System.out.println("\n");
    }
}
