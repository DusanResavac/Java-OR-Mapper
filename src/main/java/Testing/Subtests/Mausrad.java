package Testing.Subtests;

import OrmFramework.EntityAnnotation;
import OrmFramework.FieldAnnotation;
import OrmFramework.OneToOne;
import OrmFramework.PrimaryKeyAnnotation;
import lombok.Getter;
import lombok.Setter;

@EntityAnnotation(tableName = "MAUSRAEDER")
public class Mausrad {

    @Getter
    @Setter
    @PrimaryKeyAnnotation
    private String id;

    @Getter
    @Setter
    @FieldAnnotation
    private Material material;

    @Setter
    @Getter
    @OneToOne(remoteColumnName = "mausrad", isInTable = false)
    private Maus maus;

}
