package it.areson.aresondeathswap.managers;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FileManager {

    protected AresonDeathSwap aresonDeathSwap;
    protected FileConfiguration fileConfiguration;
    private final File file;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public FileManager(AresonDeathSwap plugin, String fileName) {
        aresonDeathSwap = plugin;
        file = new File(aresonDeathSwap.getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            aresonDeathSwap.saveResource(fileName, true);

        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public void save() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArena(String worldName) {
        List<String> arenas = fileConfiguration.getStringList(aresonDeathSwap.ARENAS_PATH);
        arenas.add(worldName);
        fileConfiguration.set(aresonDeathSwap.ARENAS_PATH, arenas);
        save();
    }

}

