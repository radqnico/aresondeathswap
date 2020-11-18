package it.areson.aresondeathswap.managers;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

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

    public void addArenaSpawnPoint(Player player) {
        addLocationToList(aresonDeathSwap.ARENAS_PATH + "." + player.getWorld().getName() + ".spawnLocations", player.getLocation());
    }

    public void removeArena(String arenaName) {
        fileConfiguration.set(aresonDeathSwap.ARENAS_PATH + "." + arenaName, null);
        save();
    }

    private double round(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    public Location getLocation(String path) {
        if (fileConfiguration.getString(path + ".world") == null) {
            return null;
        } else {
            String worldName = fileConfiguration.getString(path + ".world");
            if (worldName != null) {
                World world = aresonDeathSwap.getServer().getWorld(worldName);
                if (world != null) {
                    return new Location(
                            world,
                            fileConfiguration.getDouble(path + ".x"),
                            fileConfiguration.getDouble(path + ".y"),
                            fileConfiguration.getDouble(path + ".z"),
                            (float) fileConfiguration.getDouble(path + ".yaw"),
                            (float) fileConfiguration.getDouble(path + ".pitch")
                    );
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    protected void addLocationToList(String path, Location location) {
        if (!fileConfiguration.isConfigurationSection(path)) {
            fileConfiguration.createSection(path);
        }

        ConfigurationSection spawnLocations = fileConfiguration.getConfigurationSection(path);
        if (spawnLocations != null) {
            int index = spawnLocations.getKeys(false).size();
            String spawnPointPath = path + "." + index;

            if (location.getWorld() != null) {
                fileConfiguration.set(spawnPointPath + ".world", location.getWorld().getName());
                fileConfiguration.set(spawnPointPath + ".x", round(location.getX()));
                fileConfiguration.set(spawnPointPath + ".y", round(location.getY()));
                fileConfiguration.set(spawnPointPath + ".z", round(location.getZ()));
                fileConfiguration.set(spawnPointPath + ".yaw", round(location.getYaw()));
                fileConfiguration.set(spawnPointPath + ".pitch", round(location.getPitch()));
            } else {
                aresonDeathSwap.getLogger().severe("Cannot get world");
            }
        } else {
            aresonDeathSwap.getLogger().severe("Cannot get spawnLocations");
        }

        save();
    }

    public void setArenaMinPlayers(String worldName, int minPlayers) {
        fileConfiguration.set(aresonDeathSwap.ARENAS_PATH + "." + worldName + ".min-players", minPlayers);
        save();
    }

    public int getArenaMinPlayers(String worldName) {
        int minPlayers = fileConfiguration.getInt(aresonDeathSwap.ARENAS_PATH + "." + worldName + ".min-players");
        if (minPlayers < 2) {
            minPlayers = aresonDeathSwap.DEFAULT_MIN_PLAYERS;
        }
        return minPlayers;
    }
}

