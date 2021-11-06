package ycm.yml.manager.ycm;

import apple.utilities.request.AppleRequestQueue;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import ycm.yml.manager.fields.YcmField;
import ycm.yml.manager.fields.YcmInlineComment;
import ycm.yml.manager.fields.YcmNewlineComment;
import ycm.yml.manager.yml.CommentedConfiguration;
import ycm.yml.manager.yml.CommentedConfigurationWithFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

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
public class Ycm implements YcmConfigManager {
    private AppleRequestQueue scheduler = new YcmRequestService();

    public Ycm withScheduler(AppleRequestQueue scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    @Override
    public AppleRequestQueue getScheduler() {
        return scheduler;
    }


    @Override
    public <Config> Config toConfig(File inputFile, Class<Config> output) throws IOException, InvalidConfigurationException {
        CommentedConfigurationWithFile outputConfig = new CommentedConfigurationWithFile(inputFile);
        outputConfig.load();
        return toConfig(output, outputConfig);
    }

    /**
     * convert a file to a Config object
     *
     * @param output       the type of the expected output Object
     * @param inputSection a subSection provided when loading a section of the yml file
     * @param <Config>     the type that will be returned
     * @return the output Object with values from the yml file
     */
    private <Config> Config toConfig(Class<Config> output, ConfigurationSection inputSection) {
        Config outputObject;
        try {
            outputObject = output.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("%s does not have a constructor with no arguments for Ycm", output.getName()));
        }
        for (Field field : outputObject.getClass().getFields()) {
            YcmField ycmField = prepareYcmField(field);
            if (ycmField == null) continue;
            handleYcmFieldToObject(inputSection, outputObject, field, ycmField);
        }
        return outputObject;
    }

    /**
     * add a field to the outputObject
     *
     * @param inputConfig  a subSection provided when loading a section of the yml
     * @param outputObject the object being created
     * @param field        the field of the object to set the value of
     * @param ycmField     the annotation on the field
     * @param <Config>     the outputObject type
     */
    private <Config> void handleYcmFieldToObject(ConfigurationSection inputConfig, Config outputObject, Field field, YcmField ycmField) {
        Class<?> fieldType = field.getType();
        String fieldName = ycmField.pathname();
        if (fieldName.isEmpty()) fieldName = field.getName();
        Object fieldValue;
        if (inputConfig.isConfigurationSection(fieldName)) {
            fieldValue = toConfig(fieldType, inputConfig.getConfigurationSection(fieldName));
        } else {
            fieldValue = inputConfig.get(fieldName);
        }
        try {
            field.set(outputObject, fieldValue);
        } catch (IllegalAccessException ignored) {
        }
    }

    /**
     * just try to set the field accessible and get the annotation for it
     *
     * @param field the field to do this to
     * @return the annotation on the field
     */
    private YcmField prepareYcmField(Field field) {
        YcmField ycmField = field.getAnnotation(YcmField.class);
        try {
            field.setAccessible(true);
        } catch (SecurityException ignored) {
        }
        return ycmField;
    }


    @Override
    public <Config> void toFile(Config input, File outputFile) throws IOException {
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
    private <Config> CommentedConfiguration toCommentedConfig(Config input) {
        CommentedConfiguration outputConfig = new CommentedConfiguration();
        for (Field field : input.getClass().getFields()) {
            YcmField ycmField = prepareYcmField(field);
            if (ycmField == null) continue;
            handleYcmFieldToCommentedConfig(input, outputConfig, field, ycmField);
        }
        return outputConfig;
    }

    /**
     * convert a field to be represented in outputConfig
     *
     * @param input        the input object
     * @param outputConfig the config being built
     * @param field        the field currently being converted
     * @param ycmField     the annotation on field
     * @param <Config>     the type of the input object
     */
    private <Config> void handleYcmFieldToCommentedConfig(Config input, CommentedConfiguration outputConfig, Field field, YcmField ycmField) {
        Class<?> fieldType = field.getType();
        String fieldName = ycmField.pathname();
        if (fieldName.isEmpty()) fieldName = field.getName();
        Object fieldValue;
        try {
            fieldValue = field.get(input);
        } catch (IllegalAccessException ignored) {
            return;
        }
        boolean isValueSimple = isValueSimple(fieldType);
        if (isValueSimple) {
            outputConfig.set(fieldName, fieldValue);
        } else {
            addCommentsFromSubConfig(outputConfig, fieldName, fieldValue);
        }
        addCommentsToPath(outputConfig, field, fieldName);
    }

    /**
     * add comments to current path in outputConfig
     *
     * @param outputConfig the config being built
     * @param field        the field currently being converted
     * @param fieldName    the name of the field
     */
    private void addCommentsToPath(CommentedConfiguration outputConfig, Field field, String fieldName) {
        YcmInlineComment ycmInlineComment = field.getAnnotation(YcmInlineComment.class);
        YcmNewlineComment ycmNewlineComment = field.getAnnotation(YcmNewlineComment.class);
        if (ycmInlineComment != null) {
            outputConfig.addCommentInline(fieldName, ycmInlineComment.value());
        }
        if (ycmNewlineComment != null) {
            outputConfig.addCommentNewline(fieldName, ycmNewlineComment.value());
        }
    }

    /**
     * add comments from the subConfig created by the fieldValue
     *
     * @param outputConfig the config being built
     * @param fieldName    the name of the field
     * @param fieldValue   the value of the complex Object assigned to field
     */
    private void addCommentsFromSubConfig(CommentedConfiguration outputConfig, String fieldName, Object fieldValue) {
        CommentedConfiguration fieldConfig = toCommentedConfig(fieldValue);
        for (Map.Entry<String, String> comment : fieldConfig.getCommentsInLine().entrySet()) {
            outputConfig.addCommentInline(fieldName, comment.getValue(), comment.getKey());
        }
        for (Map.Entry<String, String> comment : fieldConfig.getCommentsNewLine().entrySet()) {
            outputConfig.addCommentNewline(fieldName, comment.getValue(), comment.getKey());
        }
        outputConfig.createSection(fieldName, fieldConfig.getValues(false));
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
