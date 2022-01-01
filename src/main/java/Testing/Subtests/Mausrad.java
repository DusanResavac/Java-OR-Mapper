package Testing.Subtests;

import OrmFramework.*;
import lombok.Getter;
import lombok.Setter;

@EntityAnnotation(tableName = "MAUSRAEDER")
public class Mausrad {

    /**
     * Instance number counter.
     */
    protected static int _N = 1;

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

    @IgnoreAnnotation
    @Getter
    protected int _instanceNumber = _N++;
}
