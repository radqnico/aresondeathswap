package it.areson.aresondeathswap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class FileManager {

    protected JavaPlugin plugin;
    protected FileConfiguration fileConfiguration;
    private final File file;
    private final String arenaPath;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public FileManager(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        file = new File(this.plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            this.plugin.saveResource(fileName, true);

        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        arenaPath = "arena.";
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    private double round(double number) {
        return Math.round(number * 100.0) / 100.0;
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

    public Optional<Location> getLocation(String path) {
        if (fileConfiguration.getString(path + ".world") == null) {
            return Optional.empty();
        } else {
            String world = fileConfiguration.getString(path + ".world");
            if (world != null) {
                return Optional.of(new Location(plugin.getServer().getWorld(world), fileConfiguration.getDouble(path + ".x"), fileConfiguration.getDouble(path + ".y"), fileConfiguration.getDouble(path + ".z"), (float) fileConfiguration.getDouble(path + ".yaw"), (float) fileConfiguration.getDouble(path + ".pitch")));
            } else {
                return Optional.empty();
            }
        }
    }

    public void setLocation(String path, Location location) {
        fileConfiguration.set(path + ".world", Objects.requireNonNull(location.getWorld()).getName());
        fileConfiguration.set(path + ".x", round(location.getX()));
        fileConfiguration.set(path + ".y", round(location.getY()));
        fileConfiguration.set(path + ".z", round(location.getZ()));
        fileConfiguration.set(path + ".yaw", round(location.getYaw()));
        fileConfiguration.set(path + ".pitch", round(location.getPitch()));
        save();
    }

    public void setArenaLocation(Location location) {
        World world = location.getWorld();

        if(world != null) {
            String commonPath = arenaPath + world.getName();

            setLocation(commonPath, location);
        }
    }

}

