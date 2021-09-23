package ycm.yml.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * tags a field in a ConfigObject for when it is converted to a file
 *
 * @author Apple (amp7368)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface YcmField {
    /**
     * @return the overridden pathname of a variable
     */
    String pathname() default "";
}
