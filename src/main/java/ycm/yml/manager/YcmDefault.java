package ycm.yml.manager;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

/**
 * a static version of Ycm that just uses the default config
 *
 * @author Apple (amp7368)
 */
public class YcmDefault {
    private static final Ycm defaultYcm = new Ycm();

    public static <Config> Config toConfig(File inputFile, Class<Config> output) throws IOException, InvalidConfigurationException {
        return defaultYcm.toConfig(inputFile, output);
    }

    public static <Config> Config toConfig(Class<Config> output, Configuration inputConfig, ConfigurationSection inputSection) {
        return defaultYcm.toConfig(output, inputConfig, inputSection);
    }

    public static <Config> void toFile(Config input, File outputFile) throws IOException {
        defaultYcm.toFile(input, outputFile);
    }

    public static <Config> CommentedConfiguration toCommentedConfig(Config input) {
        return defaultYcm.toCommentedConfig(input);
    }

}
