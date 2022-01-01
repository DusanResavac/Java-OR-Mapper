package OrmFramework;

public interface Cache {

    /** Retrieves an object from the cache
     * @param c Class
     * @param pk Primary key
     * @return Object */
    public Object get(Class c, Object pk);


    /** Inserts an object into the cache
     * @param obj Object
     */
    public void put(Object obj) throws NoSuchMethodException;


    /** Removes an object from the cache.
     * @param obj Object.
     */
    public void remove(Object obj) throws NoSuchMethodException;


    /** Returns whether the cache contains an object matching the primary key
     * @param c Class
     * @param pk Primary key
     * @return Returns TRUE if the object is in the Cache, otherwise returns FALSE
     */
    public boolean contains(Class c, Object pk);


    /** Returns whether the cache contains an object
     * @param obj Object
     * @return Returns TRUE if the object is in the Cache, otherwise returns FALSE
     */
    public boolean contains(Object obj) throws NoSuchMethodException;


    /** Returns whether an object has changed
     * @param obj Object
     * @return Returns TRUE if the object has changed or might have changed, returns FALSE if the object is unchanged
     */
    public boolean hasChanged(Object obj) throws NoSuchMethodException;
}
