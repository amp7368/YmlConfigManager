package ycm.yml.manager.example;

import org.bukkit.plugin.java.JavaPlugin;
import ycm.yml.manager.ycm.YcmFolder;

import java.util.Map;
import java.util.logging.Level;

public class ExampleYcmFolder {
    private final YcmFolder<ExampleYcmConfig> folder;
    private final JavaPlugin plugin;
    private ExampleYcmConfig first;

    public ExampleYcmFolder(JavaPlugin plugin) {
        this.folder = new YcmFolder<>(ExampleYcmConfig.class, plugin, "exampleYcm");
        this.plugin = plugin;
    }

    public void doTest() {
        initialize();
        save();
        print();
        modify();
        print();
        save();
    }

    public void doSpeedTest() {
        initialize();
        long start = System.currentTimeMillis();
        save();
        System.out.println("finished saving " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        saveAsync(start);
        System.out.println("started async " + (System.currentTimeMillis() - start));
    }

    private void modify() {
        first.valueA = "THIS IS A NEW VALUE FOR A";
    }

    public void initialize() {
        folder.clearConfigs();
        folder.saveCachedConfigs();
        for (int i = 0; i < 50; i++) {
            ExampleYcmConfig config = new ExampleYcmConfig();
            config.valueA = "valueA" + i;
            config.pathB = "valueB" + i;
            config.pathC = 3 + i;
            ExampleYcmConfig.PathD pathD = new ExampleYcmConfig.PathD();
            pathD.pathF = "valueF" + i;
            pathD.pathG = "valueG" + i;
            pathD.pathH = 5 + i;
            config.pathD = pathD;
            ExampleYcmConfigPathE pathE = new ExampleYcmConfigPathE();
            pathE.pathI = "valueI" + i;
            pathE.pathJ = "valueJ" + i;
            pathE.pathK = 9 + i;
            config.pathE = pathE;
            config.randomField = "randomValue" + i;
            config.setFilename(i + "---" + config.getFilename());
            folder.addConfig(config);
            if (first == null) first = config;
        }
    }

    public void save() {
        folder.saveCachedConfigs();
    }

    private void saveAsync(long start) {
        folder.saveCachedConfigsAsync((c) -> {
            System.out.println("finished async " + (System.currentTimeMillis() - start));
        });
    }

    public void print() {
        for (ExampleYcmConfig s : folder.getAllConfig().values()) {
            plugin.getLogger().log(Level.INFO, s.toString());
        }
    }

    public Map<String, ExampleYcmConfig> getAll() {
        return folder.getAllConfig();
    }

    public Map<String, ExampleYcmConfig> reloadAll() {
        return folder.reloadAllConfig();
    }
}
