package ycm.yml.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * comments a path in a Yaml file
 * This is the newline version which will write the comment
 * on the line before the line that the path is specified on
 *
 * @author Apple (amp7368)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface YcmNewlineComment {
    String value();
}
