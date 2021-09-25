package ycm.yml.manager.ycm;

import apple.utilities.request.AppleRequest;
import apple.utilities.request.AppleRequestQueue;
import apple.utilities.request.AppleRequestService;
import apple.utilities.request.SimpleExceptionHandler;
import apple.utilities.request.settings.RequestSettingsBuilder;
import apple.utilities.request.settings.RequestSettingsBuilderVoid;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public interface YcmConfigManager {
    AppleRequestQueue getScheduler();

    static <Callback> RequestSettingsBuilder<Callback> getIgnoreFailSettings(Consumer<Callback> callback, Callback returnVal) {
        RequestSettingsBuilder<Callback> requestSettings = new RequestSettingsBuilder<>();
        return requestSettings.addExceptionHandler(
                new SimpleExceptionHandler(new Class<?>[]{IOException.class, InvalidConfigurationException.class}, () -> callback.accept(returnVal)),
                1
        );
    }

    static RequestSettingsBuilderVoid getIgnoreFailSettingsVoid(Consumer<Boolean> callback) {
        RequestSettingsBuilderVoid requestSettings = new RequestSettingsBuilderVoid();
        requestSettings.addExceptionHandler(
                new SimpleExceptionHandler(new Class<?>[]{IOException.class, InvalidConfigurationException.class}, () -> callback.accept(false)),
                1
        );
        return requestSettings;
    }


    /**
     * schedule toConfig and callback null on errors
     *
     * @param inputFile the file to read into an object
     * @param output    the type of the expected output Object
     * @param <Config>  the type that will be returned
     * @param callback  the callback to run after the async finishes
     * @return the output Object with values from the yml file
     * @see YcmConfigManager#toConfig(File, Class) ()
     */
    default <Config> AppleRequestService.RequestHandler<Config> tryToConfigAsync(File inputFile, Class<Config> output, Consumer<Config> callback) {
        RequestSettingsBuilder<Config> requestSettings = getIgnoreFailSettings(callback, null);
        return toConfigAsync(inputFile, output, callback, requestSettings);
    }

    /**
     * schedule toConfig and do as specified on errors
     *
     * @param inputFile       the file to read into an object
     * @param output          the type of the expected output Object
     * @param <Config>        the type that will be returned
     * @param callback        the callback to run after the async finishes
     * @param requestSettings the settings for the scheduled task
     * @return the output Object with values from the yml file
     * @see #toConfig(File, Class)
     */
    default <Config> AppleRequestService.RequestHandler<Config> toConfigAsync(File inputFile, Class<Config> output, Consumer<Config> callback, RequestSettingsBuilder<Config> requestSettings) {
        return getScheduler().queue(() -> {
            try {
                return toConfig(inputFile, output);
            } catch (IOException | InvalidConfigurationException e) {
                throw new AppleRequest.AppleRuntimeRequestException(e);
            }
        }, callback, requestSettings);
    }

    /**
     * schedule toFile and callback false on errors
     *
     * @param input      the Config object
     * @param outputFile the file to write the Config object to
     * @param callback   the callback after the task finishes
     * @param <Config>   the parameter Config object
     * @see #toFile(Object, File)
     */
    default <Config> AppleRequestService.RequestHandler<Boolean> trytoFileAsync(Config input, File outputFile, Consumer<Boolean> callback) {
        RequestSettingsBuilderVoid requestSettings = getIgnoreFailSettingsVoid(callback);
        return toFileAsync(input, outputFile, callback, requestSettings);
    }

    /**
     * schedule toFile and do as specified on errors
     *
     * @param input           the Config object
     * @param outputFile      the file to write the Config object to
     * @param <Config>        the parameter Config object
     * @param callback        the callback after the task finishes
     * @param requestSettings the settings for the scheduled task
     * @see #toFile(Object, File)
     */
    default <Config> AppleRequestService.RequestHandler<Boolean> toFileAsync(Config input, File outputFile, Consumer<Boolean> callback, RequestSettingsBuilderVoid requestSettings) {
        return getScheduler().queueVoid(() -> {
            try {
                toFile(input, outputFile);
            } catch (IOException e) {
                throw new AppleRequest.AppleRuntimeRequestException(e);
            }
        }, () -> callback.accept(true), requestSettings);
    }

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
    <Config> Config toConfig(File inputFile, Class<Config> output) throws IOException, InvalidConfigurationException;

    /**
     * convert the Config object to a yml file at outputFile's location
     *
     * @param input      the Config object
     * @param outputFile the file to write the Config object to
     * @param <Config>   the parameter Config object
     * @throws IOException when there was an IOException writing to the file
     */
    <Config> void toFile(Config input, File outputFile) throws IOException;
}
