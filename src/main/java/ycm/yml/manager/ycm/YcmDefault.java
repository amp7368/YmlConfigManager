package ycm.yml.manager.ycm;

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

    public static <Config> void toFile(Config input, File outputFile) throws IOException {
        defaultYcm.toFile(input, outputFile);
    }
}
