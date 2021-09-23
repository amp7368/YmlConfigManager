package ycm.yml.manager.ycm;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import ycm.yml.manager.YcmUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    public Map<String, Config> getAllConfig() {
        if (configs == null) {
            return reloadAllConfig();
        }
        return new HashMap<>(configs);
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
        this.configs = new HashMap<>(configs);
        return configs;
    }

    private Config toConfigCaught(File file) {
        try {
            return ycm.toConfig(file, output);
        } catch (IOException | InvalidConfigurationException e) {
            return null;
        }
    }

    /**
     * @return the list of config files that failed
     * will be empty on successful execution
     */
    public List<File> saveCachedConfigs() {
        if (this.configs == null) this.configs = new HashMap<>();
        return this.saveCachedConfigs(configs.values());
    }

    /**
     * @param configsToSave the configs to save
     * @return the list of config files that failed
     * will be empty on successful execution
     */
    public List<File> saveCachedConfigs(Collection<Config> configsToSave) {
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

    public void addConfig(Config config) {
        if (this.configs == null) this.configs = new HashMap<>();
        this.configs.put(config.getFilename(), config);
    }

    public void removeConfig(Config config) {
        if (this.configs == null) this.configs = new HashMap<>();
        this.configs.remove(config.getFilename());
    }

    public void removeConfig(String config) {
        if (this.configs == null) this.configs = new HashMap<>();
        this.configs.remove(config);
    }
}
