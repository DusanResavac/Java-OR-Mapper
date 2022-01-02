package OrmFramework;

import OrmFramework.metamodel.Orm;
import OrmFramework.metamodel._Field;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;

public class TrackingCache implements Cache {

    protected HashMap<Class, HashMap<Object, Object>> caches = new HashMap<>();
    protected HashMap<Class, HashMap<Object, String>> hashes = new HashMap<>();

    /** Gets the cache for a class
     * @param c Class
     * @return Cache
     */
    protected HashMap<Object, Object> getCache(Class c) {
        if (caches.containsKey(c)) {
            return caches.get(c);
        }

        HashMap<Object, Object> result = new HashMap<>();
        caches.put(c, result);

        return result;
    }

    /** Gets the hash store for a class
     * @param c Class
     * @return Hashes
     */
    protected HashMap<Object, String> getHash(Class c) {
        if (hashes.containsKey(c)) {
            return hashes.get(c);
        }

        HashMap<Object, String> result = new HashMap<>();
        hashes.put(c, result);

        return result;
    }


    /** Gets a hash for an object
     * @param obj Object
     * @return Hash
     * */
    protected String computeHash(Object obj) throws NoSuchMethodException {
        StringBuilder result = new StringBuilder();

        for (_Field i: Orm._getEntity(obj).get_internals()) {
            // if the field covers a foreign key that is still internal (one-to-one, or many-to-one), save the primary key
            if (i.isForeignKey()) {
                Object fkObject = i.getValue(obj);
                if (fkObject != null) { result.append(Orm._getEntity(fkObject).getPrimaryKey().getValue(fkObject).toString()); }
            } else {
                result.append(i.getColumnName()).append("=").append(i.getValue(obj)).append(";");
            }
        }

        for (_Field i: Orm._getEntity(obj).get_externals()) {
            Object externalObject = i.getValue(obj);
            if (externalObject == null) {
                continue;
            }

            // Watch out for 1:1 - one table doesn't have the other table's key -> the field is external, but not a list
            if (!(externalObject instanceof Collection<?>)) {
                result.append(i.getColumnName()).append("=").append(Orm._getEntity(externalObject).getPrimaryKey().getValue(externalObject).toString());
                continue;
            }
            Iterable fkObject = (Iterable) externalObject;

            result.append(i.getColumnName()).append("=");
            // iterate through object list and append each primary key value to the result
            for (Object k: fkObject) {
                result.append(Orm._getEntity(k).getPrimaryKey().getValue(k).toString()).append(",");
            }

        }

        return Hashing.sha256().hashString(result.toString(), StandardCharsets.UTF_8).toString();
    }


    /**
     * Puts an object into the cache
     * @param obj Object
     */
    @Override
    public void put(Object obj) throws NoSuchMethodException {
        if(obj != null) {
            getCache(obj.getClass()).put(Orm._getEntity(obj).getPrimaryKey().getValue(obj), obj);
            getHash(obj.getClass()).put(Orm._getEntity(obj).getPrimaryKey().getValue(obj), computeHash(obj));
        }
    }


    /** Removes an object from the cache
     * @param obj Object
     */
    @Override
    public void remove(Object obj) throws NoSuchMethodException {
        getCache(obj.getClass()).remove(Orm._getEntity(obj).getPrimaryKey().getValue(obj));
        getHash(obj.getClass()).remove(Orm._getEntity(obj).getPrimaryKey().getValue(obj));
    }


    /** Returns whether an object has changed
     * @param obj Object
     * @return Returns TRUE if the object has changed or might have changed, returns FALSE if the object is unchanged
     */
    @Override
    public boolean hasChanged(Object obj) throws NoSuchMethodException {
        HashMap<Object, String> h = getHash(obj.getClass());
        Object pk = Orm._getEntity(obj).getPrimaryKey().getValue(obj);

        if (h.containsKey(pk)) {
            return h.get(pk).equals(computeHash(obj));
        }

        return true;
    }

    /** Gets an object from the cache
     * @param c Class
     * @param pk Primary key
     * @return Object
     */
    @Override
    public Object get(Class c, Object pk) {
        HashMap<Object, Object> cache = getCache(c);

        if (cache.containsKey(pk)) {
            return cache.get(pk);
        }

        return null;
    }

    /** Returns whether the cache contains an object with the given primary key
     * @param c Class
     * @param pk Primary key
     * @return Returns TRUE if the object is in the Cache, otherwise returns FALSE
     */
    @Override
    public boolean contains(Class c, Object pk) {
        return getCache(c).containsKey(pk);
    }

    /** Returns whether the cache contains an object
     * @param obj Object
     * @return Returns TRUE if the object is in the Cache, otherwise returns FALSE
     */
    @Override
    public boolean contains(Object obj) throws NoSuchMethodException {
        return contains(obj.getClass(), Orm._getEntity(obj).getPrimaryKey().getValue(obj));
    }

}
