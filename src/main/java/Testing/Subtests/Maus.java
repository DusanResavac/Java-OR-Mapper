package Testing.Subtests;

import OrmFramework.EntityAnnotation;
import OrmFramework.FieldAnnotation;
import OrmFramework.OneToOne;
import OrmFramework.PrimaryKeyAnnotation;
import lombok.Getter;
import lombok.Setter;

@EntityAnnotation(tableName = "Maeuse")
public class Maus {

    @Getter
    @Setter
    @PrimaryKeyAnnotation
    private Integer id;

    @Getter
    @Setter
    @FieldAnnotation
    private Material material;

    @Getter
    @Setter
    @OneToOne(isInTable = true)
    private Mausrad mausrad;

}
