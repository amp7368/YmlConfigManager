package ycm.yml.manager;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;

/**
 * the base class to convert between ConfigObjects and files
 * The methods in this class are not static because
 * there might be additional settings for a Ycm in the future
 *
 * @author Apple (amp7368)
 * <p>
 * Annotations used:
 * @see YcmField
 * @see YcmInlineComment
 * @see YcmNewlineComment
 */
public class Ycm {
    /**
     * convert a file to a Config object
     *
     * @param inputFile the file to read into an object
     * @param output    the type of the expected output Object
     * @param <Config>  the type that will be returned
     * @return the output Object with values from the yml file
     * @throws IOException                   when there was an IOException reading from the file
     * @throws InvalidConfigurationException when there was an invalid yml structure
     */
    public <Config> Config toConfig(File inputFile, Class<Config> output) throws IOException, InvalidConfigurationException {
        CommentedConfiguration outputConfig = new CommentedConfiguration(inputFile);
        outputConfig.load();
        return toConfig(output, outputConfig, null);
    }

    /**
     * convert a file to a Config object
     *
     * @param output       the type of the expected output Object
     * @param inputConfig  the config provided when loading the yml file
     * @param inputSection a subSection provided when loading a section of the yml file
     * @param <Config>     the type that will be returned
     * @return the output Object with values from the yml file
     */
    public <Config> Config toConfig(Class<Config> output, Configuration inputConfig, ConfigurationSection inputSection) {
        Config outputObject;
        try {
            outputObject = output.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("%s does not have a constructor with no arguments for Ycm", output.getName()));
        }
        for (Field field : outputObject.getClass().getDeclaredFields()) {
            YcmField ycmField = field.getAnnotation(YcmField.class);
            field.trySetAccessible();
            if (ycmField != null) {
                Class<?> fieldType = field.getType();
                String fieldName = ycmField.pathname();
                if (fieldName.isBlank()) fieldName = field.getName();
                boolean isConfigSection;
                if (inputConfig != null) {
                    isConfigSection = inputConfig.isConfigurationSection(fieldName);
                } else {
                    isConfigSection = inputSection.isConfigurationSection(fieldName);
                }
                if (isConfigSection) {
                    try {
                        field.set(outputObject, toConfig(fieldType, null, Objects.requireNonNullElse(inputConfig, inputSection).getConfigurationSection(fieldName)));
                    } catch (IllegalAccessException ignored) {
                    }
                } else {
                    Object s = Objects.requireNonNullElse(inputConfig, inputSection).get(fieldName);
                    try {
                        field.set(outputObject, s);
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
        }
        return outputObject;
    }

    /**
     * convert the Config object to a yml file at outputFile's location
     *
     * @param input      the Config object
     * @param outputFile the file to write the Config object to
     * @param <Config>   the parameter Config object
     * @throws IOException when there was an IOException writing to the file
     */
    public <Config> void toFile(Config input, File outputFile) throws IOException {
        Class<?> inputClass = input.getClass();
        CommentedConfiguration outputConfig = toCommentedConfig(input);
        outputConfig.save(outputFile);
    }

    /**
     * convert the Config object to a CommentedConfiguration
     *
     * @param input    the input object
     * @param <Config> the type of the input object
     * @return the new CommentedConfiguration
     */
    public <Config> CommentedConfiguration toCommentedConfig(Config input) {
        CommentedConfiguration outputConfig = new CommentedConfiguration();
        for (Field field : input.getClass().getDeclaredFields()) {
            YcmField ycmField = field.getAnnotation(YcmField.class);
            field.trySetAccessible();
            try {
                if (ycmField != null) {
                    Class<?> fieldType = field.getType();
                    boolean isValueSimple = isValueSimple(fieldType);
                    String fieldName = ycmField.pathname();
                    if (fieldName.isBlank()) fieldName = field.getName();
                    Object fieldValue = fieldName.isEmpty() ? fieldName : field.get(input);
                    if (isValueSimple) {
                        outputConfig.set(fieldName, fieldValue);
                    } else {
                        CommentedConfiguration fieldConfig = toCommentedConfig(fieldValue);
                        for (Map.Entry<String, String> comment : fieldConfig.getCommentsInLine().entrySet()) {
                            outputConfig.addCommentInline(fieldName, comment.getValue(), comment.getKey());
                        }
                        for (Map.Entry<String, String> comment : fieldConfig.getCommentsNewLine().entrySet()) {
                            outputConfig.addCommentNewline(fieldName, comment.getValue(), comment.getKey());
                        }
                        outputConfig.createSection(fieldName, fieldConfig.getValues(false));
                    }
                    YcmInlineComment ycmInlineComment = field.getAnnotation(YcmInlineComment.class);
                    if (ycmInlineComment != null) {
                        outputConfig.addCommentInline(fieldName, ycmInlineComment.value());
                    }
                    YcmNewlineComment ycmNewlineComment = field.getAnnotation(YcmNewlineComment.class);
                    if (ycmNewlineComment != null) {
                        outputConfig.addCommentNewline(fieldName, ycmNewlineComment.value());
                    }
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return outputConfig;
    }

    /**
     * @param fieldType the type to check
     * @return true if the value is a yml primitive
     */
    private boolean isValueSimple(Class<?> fieldType) {
        return fieldType.isPrimitive() ||
                fieldType.equals(String.class) ||
                fieldType.equals(Boolean.class) ||
                fieldType.equals(Character.class) ||
                fieldType.equals(Byte.class) ||
                fieldType.equals(Short.class) ||
                fieldType.equals(Integer.class) ||
                fieldType.equals(Long.class) ||
                fieldType.equals(Float.class) ||
                fieldType.equals(Double.class);
    }

}
