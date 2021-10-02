package OrmFramework;

import OrmFramework.metamodel._Entity;

import java.util.HashMap;
import java.util.Map;

public class Orm {

    private static final Map<Class, _Entity> _entities = new HashMap<>();

    public static _Entity _getEntity(Object o) throws NoSuchMethodException {
        Class c = (o instanceof Class) ? (Class) o : o.getClass();

        if (!_entities.containsKey(c)) {
            _entities.put(c, new _Entity(c));
        }

        return _entities.get(c);
    }

}
