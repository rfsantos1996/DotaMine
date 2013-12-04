package com.jabyftw.dotamine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Rafael
 */
public class CustomConfig {

    private final DotaMine pl;
    private final String name;
    private File file;
    private FileConfiguration fileConfig;

    public CustomConfig(DotaMine pl, String name) {
        this.pl = pl;
        this.name = name;
    }

    public FileConfiguration getCustomConfig() {
        if (fileConfig == null) {
            reloadCustomConfig();
        }
        return fileConfig;
    }

    public void reloadCustomConfig() {
        if (fileConfig == null) {
            file = new File(pl.getDataFolder(), name + ".yml");
        }
        fileConfig = YamlConfiguration.loadConfiguration(file);

        InputStream defConfigStream = pl.getResource(name + ".yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfig.setDefaults(defConfig);
        }
    }

    public void saveCustomConfig() {
        if (file == null) {
            file = new File(pl.getDataFolder(), name + ".yml");
        }
        try {
            getCustomConfig().options().copyDefaults(true);
            if (!file.exists()) {
                getCustomConfig().save(file);
            } else {
                if (getCustomConfig().getInt("DoNotChangeThis.ConfigVersion") != pl.version) {
                    getCustomConfig().save(file);
                    pl.getLogger().log(Level.WARNING, "Update your configuration!");
                }
            }
        } catch (IOException ex) {
            pl.getLogger().log(Level.WARNING, "Couldn't save .yml");
        }
    }
}
