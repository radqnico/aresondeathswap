package it.areson.aresondeathswap.managers;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FileManager {

    private final File file;
    protected AresonDeathSwap aresonDeathSwap;
    protected FileConfiguration fileConfiguration;

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

    public void addArena(String arenaName) {
        List<String> arenas = fileConfiguration.getStringList(aresonDeathSwap.ARENAS_PATH);
        if (!arenas.contains(arenaName)) {
            arenas.add(arenaName);
            fileConfiguration.set(aresonDeathSwap.ARENAS_PATH, arenas);
            save();
        }
    }

    public void removeArena(String arenaName) {
        List<String> arenas = fileConfiguration.getStringList(aresonDeathSwap.ARENAS_PATH);
        arenas.remove(arenaName);
        fileConfiguration.set(aresonDeathSwap.ARENAS_PATH, arenas);
        save();
    }

    public Optional<Location> getLocation(String path) {
        String worldName = fileConfiguration.getString(path + ".world");
        if (worldName != null) {
            World world = aresonDeathSwap.getServer().getWorld(worldName);
            if (world != null) {
                return Optional.of(new Location(
                        world,
                        fileConfiguration.getDouble(path + ".x"),
                        fileConfiguration.getDouble(path + ".y"),
                        fileConfiguration.getDouble(path + ".z"),
                        (float) fileConfiguration.getDouble(path + ".yaw"),
                        (float) fileConfiguration.getDouble(path + ".pitch")
                ));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public void setLocation(Location location, String path) {
        World world = location.getWorld();
        if (world != null) {
            fileConfiguration.set(path + ".world", world.getName());
            fileConfiguration.set(path + ".x", location.getX());
            fileConfiguration.set(path + ".y", location.getY());
            fileConfiguration.set(path + ".z", location.getZ());
            fileConfiguration.set(path + ".yaw", location.getYaw());
            fileConfiguration.set(path + ".pitch", location.getPitch());
        }
        save();
    }

}

