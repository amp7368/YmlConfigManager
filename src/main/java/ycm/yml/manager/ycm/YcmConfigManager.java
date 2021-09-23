package ycm.yml.manager.ycm;

import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

public interface YcmConfigManager {
    <Config> Config toConfig(File inputFile, Class<Config> output) throws IOException, InvalidConfigurationException;

    <Config> void toFile(Config input, File outputFile) throws IOException;
}
