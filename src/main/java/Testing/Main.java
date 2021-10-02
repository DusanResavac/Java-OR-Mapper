package Testing;

import OrmFramework.Orm;
import OrmFramework.metamodel._Entity;

public class Main {

    public static void main(String[] args) throws NoSuchMethodException {
        _Entity e = Orm._getEntity(Course.class);
    }

}
