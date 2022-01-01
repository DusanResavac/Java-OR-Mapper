package Testing.Subtests;

import OrmFramework.*;
import lombok.Getter;
import lombok.Setter;

@EntityAnnotation(tableName = "Maeuse")
public class Maus {

    /**
     * Instance number counter.
     */
    protected static int _N = 1;

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

    @IgnoreAnnotation
    @Getter
    protected int _instanceNumber = _N++;
}
