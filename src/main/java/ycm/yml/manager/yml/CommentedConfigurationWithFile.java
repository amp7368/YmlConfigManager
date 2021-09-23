package ycm.yml.manager.yml;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import ycm.yml.manager.YcmUtil;

import java.io.File;
import java.io.IOException;

/**
 * a CommentedConfiguration, but with a file specified
 *
 * @author Apple (amp7368)
 */
public class CommentedConfigurationWithFile extends CommentedConfiguration {
    private File file;

    public CommentedConfigurationWithFile(File file, String... children) {
        this.file = YcmUtil.fileWithChildren(file, children);
    }

    public CommentedConfigurationWithFile(JavaPlugin plugin, String... children) {
        this.file = YcmUtil.fileWithChildren(plugin.getDataFolder(), children);
    }

    /**
     * load values into this YamlConfiguration with the specified file
     *
     * @throws IOException                   when there is an IOException reading from the file
     * @throws InvalidConfigurationException when there is an invalid yml found in the file
     */
    public void load() throws IOException, InvalidConfigurationException {
        this.load(file);
    }

    /**
     * saves values from this YamlConfiguration into the specified file
     *
     * @throws IOException when there is an IOException writing to the file
     */
    public void save() throws IOException {
        super.save(file);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
