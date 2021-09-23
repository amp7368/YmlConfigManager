package ycm.yml.manager.ycm;

import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

/**
 * an interface to easily override YcmConfigManager and provide those methods
 * while asking only for the Ycm this object holds
 *
 * @author Apple (amp7368)
 */
public interface YcmHolder extends YcmConfigManager {
    Ycm getYcm();

    @Override
    default <Config> Config toConfig(File inputFile, Class<Config> output) throws IOException, InvalidConfigurationException {
        return getYcm().toConfig(inputFile, output);
    }

    @Override
    default <Config> void toFile(Config input, File outputFile) throws IOException {
        getYcm().toFile(input, outputFile);
    }
}
