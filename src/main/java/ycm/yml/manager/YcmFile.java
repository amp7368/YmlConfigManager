package ycm.yml.manager;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * a Ycm wrapper with the file specified
 *
 * @author Apple (amp7368)
 */
public class YcmFile<Config> {
    private Ycm ycm;
    private final Class<Config> output;
    private final File file;

    public YcmFile(Class<Config> output, JavaPlugin plugin, String... children) {
        this.output = output;
        this.file = YcmUtil.fileWithChildren(plugin.getDataFolder(), children);
    }

    public YcmFile(Class<Config> output, File file, String... children) {
        this.output = output;
        this.file = YcmUtil.fileWithChildren(file, children);
    }

    public YcmFile<Config> withYcm(Ycm ycm) {
        this.ycm = ycm;
        return this;
    }

    public Config toConfig() throws IOException, InvalidConfigurationException {
        return ycm.toConfig(file, output);
    }

    public Config toConfig(Configuration inputConfig, ConfigurationSection inputSection) {
        return ycm.toConfig(output, inputConfig, inputSection);
    }

    public void toFile(Config input) throws IOException {
        ycm.toFile(input, file);
    }

    public CommentedConfiguration toCommentedConfig(Config input) {
        return ycm.toCommentedConfig(input);
    }
}
