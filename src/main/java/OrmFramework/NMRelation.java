package OrmFramework;

import OrmFramework.metamodel._Entity;
import OrmFramework.metamodel._Field;
import lombok.Getter;
import lombok.Setter;

public class NMRelation {
    @Getter @Setter
    _Entity eA = null;
    @Getter @Setter
    _Field fA = null;
    @Getter @Setter
    _Entity eB = null;
    @Getter @Setter
    _Field fB = null;

    public NMRelation(_Entity eA, _Field fA, _Entity eB, _Field fB) {
        this.eA = eA;
        this.fA = fA;
        this.eB = eB;
        this.fB = fB;
    }
}
