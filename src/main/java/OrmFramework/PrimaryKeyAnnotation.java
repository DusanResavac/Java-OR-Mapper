package OrmFramework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKeyAnnotation {
    public String columnName() default "";
    public Class columnType() default Void.class;

    // Get getter and setter of field
    public String getMethod() default "";
    public String setMethod() default "";
}
