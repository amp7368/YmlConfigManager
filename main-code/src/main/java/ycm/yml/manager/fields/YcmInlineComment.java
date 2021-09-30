package ycm.yml.manager.fields;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * comments a path in a Yaml file
 * This is the inline version which will write the comment
 * on the same line the path is specified on
 *
 * @author Apple (amp7368)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface YcmInlineComment {
    String value();
}
