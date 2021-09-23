package ycm.yml.manager.ycm;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import ycm.yml.manager.YcmUtil;

import java.io.File;
import java.io.IOException;

/**
 * a Ycm wrapper with the file specified
 *
 * @author Apple (amp7368)
 */
public class YcmFile<Config> implements YcmHolder {
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
        return this.toConfig(file, output);
    }

    public void toFile(Config input) throws IOException {
        this.toFile(input, file);
    }

    @Override
    public Ycm getYcm() {
        return ycm;
    }
}
