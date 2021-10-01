package ycm.yml.manager.ycm;

import apple.utilities.request.AppleRequest;
import apple.utilities.request.settings.RequestSettingsBuilder;
import apple.utilities.request.settings.RequestSettingsBuilderVoid;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import ycm.yml.manager.YcmUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * a Ycm wrapper with the folder for multiple configs
 *
 * @author Apple (amp7368)
 */
public class YcmFolder<Config extends YcmFileNameable> implements YcmHolder {
    private Ycm ycm = new Ycm();

    private final Class<Config> output;
    private final File folder;

    private HashMap<String, Config> configs = null;

    public YcmFolder(Class<Config> output, File folder, String... children) {
        this.output = output;
        this.folder = YcmUtil.fileWithChildren(folder, children);
    }

    public YcmFolder(Class<Config> output, JavaPlugin plugin, String... children) {
        this(output, plugin.getDataFolder(), children);
    }

    public YcmFolder<Config> withYcm(Ycm ycm) {
        this.ycm = ycm;
        return this;
    }

    /**
     * gets the cached configs,
     * or if this is the first time getting them,
     * load the configs, then return them
     * note: when loading, there may be null elements in the Config folder for failed configs
     *
     * @return the configs represented in this folder
     */
    public synchronized Map<String, Config> getAllConfig() {
        if (configs == null) {
            return reloadAllConfig();
        }
        return new HashMap<>(configs);
    }

    /**
     * @param callback the callback to handle the configs represented in this folder
     * @see #reloadAllConfig()
     */
    public synchronized void reloadAllConfigAsync(Consumer<Map<String, Config>> callback) {
        if (configs == null) {
            RequestSettingsBuilder<Map<String, Config>> settings = YcmConfigManager.getIgnoreFailSettings(callback, null);
            getScheduler().queue(this::reloadAllConfig, callback, settings);
        } else {
            callback.accept(new HashMap<>(configs));
        }
    }

    /**
     * (re)load the configs, then return them
     * note: when loading, there may be null elements in the Config folder for failed configs
     *
     * @return the configs represented in this folder
     */
    public Map<String, Config> reloadAllConfig() {
        File[] filesInFolder = folder.listFiles();
        if (filesInFolder == null) return Collections.emptyMap();
        HashMap<String, Config> configs = new HashMap<>();
        for (File file : filesInFolder) {
            String fileName = file.getName();
            String configName = fileName.substring(0, fileName.lastIndexOf("."));
            Config config = toConfigCaught(file);
            if (config != null) config.setFilename(configName);
            configs.put(configName, config);
        }
        synchronized (this) {
            this.configs = new HashMap<>(configs);
        }
        return configs;
    }

    private Config toConfigCaught(File file) {
        // no sync(this) because we don't deal with any fields
        try {
            return ycm.toConfig(file, output);
        } catch (IOException | InvalidConfigurationException e) {
            return null;
        }
    }

    /**
     * @param callback handle the list of config files that failed
     *                 will be empty on successful execution
     * @see #saveCachedConfigs()
     */
    public synchronized void saveCachedConfigsAsync(Consumer<List<File>> callback) {
        if (configs == null) {
            callback.accept(Collections.emptyList());
        } else {
            getScheduler().queue(this::saveCachedConfigs, callback);
        }
    }

    /**
     * @return the list of config files that failed
     * will be empty on successful execution
     */
    public synchronized List<File> saveCachedConfigs() {
        if (this.configs == null) this.configs = new HashMap<>();
        return this.saveCachedConfigs(configs.values());
    }

    /**
     * @param configsToSave the configs to save
     * @param callback      handle the list of config files that failed
     *                      will be empty on successful execution
     * @see #saveCachedConfigs()
     */
    public synchronized void saveCachedConfigsAsync(Collection<Config> configsToSave, Consumer<List<File>> callback) {
        getScheduler().queue(() -> this.saveCachedConfigs(configsToSave), callback);
    }

    /**
     * @param configsToSave the configs to save
     * @return the list of config files that failed
     * will be empty on successful execution
     */
    public synchronized List<File> saveCachedConfigs(Collection<Config> configsToSave) {
        List<File> fails = null;
        for (Config config : configsToSave) {
            try {
                toFile(config);
            } catch (IOException e) {
                if (fails == null) fails = new ArrayList<>();
                fails.add(new File(folder, config.getFilename() + ".yml"));
            }
        }

        return fails == null ? Collections.emptyList() : fails;
    }

    /**
     * saves the input to a file
     *
     * @param input    the input to save
     * @param callback the callback to handle true if the save did not throw an exception
     */
    public void toFileAsync(Config input, Consumer<Boolean> callback) {
        RequestSettingsBuilderVoid settings = YcmConfigManager.getIgnoreFailSettingsVoid(callback);
        getScheduler().queueVoid(() -> {
            try {
                toFile(input);
            } catch (IOException e) {
                throw new AppleRequest.AppleRuntimeRequestException(e);
            }
        }, () -> callback.accept(true), settings);
    }

    /**
     * saves the input to a file
     *
     * @param input the input to save
     * @throws IOException when there was an IOException writing to the file
     */
    public void toFile(Config input) throws IOException {
        ycm.toFile(input, new File(folder, input.getFilename() + ".yml"));
    }

    @Override
    public Ycm getYcm() {
        return ycm;
    }

    /**
     * the point of doing this async is to make sure you're
     * definitely waiting <bold>at all</bold> in case it happens to be saving something atm
     *
     * @param config the config to add to the cache
     */
    public void addConfigAsync(Config config) {
        getScheduler().queueVoid(() -> addConfig(config));
    }

    /**
     * add the config to the cache
     *
     * @param config the config to add to the cache
     */
    public synchronized void addConfig(Config config) {
        if (this.configs == null) this.configs = new HashMap<>();
        this.configs.put(config.getFilename(), config);
    }

    /**
     * the point of doing this async is to make sure you're
     * definitely waiting <bold>at all</bold> in case it happens to be saving something atm
     *
     * @param config the config remove from to the cache
     */
    public void removeConfigAsync(Config config) {
        getScheduler().queueVoid(() -> removeConfig(config));
    }

    /**
     * remove the config from the cache
     *
     * @param config the config to remove from the cache
     */
    public synchronized void removeConfig(Config config) {
        if (this.configs == null) this.configs = new HashMap<>();
        this.configs.remove(config.getFilename());
    }

    /**
     * the point of doing this async is to make sure you're
     * definitely waiting <bold>at all</bold> in case it happens to be saving something atm
     *
     * @param config the config to remove from the cache
     */
    public void removeConfigAsync(String config) {
        getScheduler().queueVoid(() -> removeConfig(config));
    }

    /**
     * remove the config from the cache
     *
     * @param config the config to remove from the cache
     */
    public synchronized void removeConfig(String config) {
        if (this.configs == null) this.configs = new HashMap<>();
        this.configs.remove(config);
    }

    /**
     * the point of doing this async is to make sure you're
     * definitely waiting <bold>at all</bold> in case it happens to be saving something atm
     */
    public synchronized void clearConfigsAsync() {
        getScheduler().queueVoid(this::clearConfigs);
    }

    /**
     * clears the cache of configs
     */
    public synchronized void clearConfigs() {
        this.configs = new HashMap<>();
    }
}
