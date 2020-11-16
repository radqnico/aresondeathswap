package it.areson.aresondeathswap;

import it.areson.aresondeathswap.commands.admin.LoadWorldCommand;
import it.areson.aresondeathswap.commands.PlayCommand;
import it.areson.aresondeathswap.commands.admin.SetArenaCommand;
import it.areson.aresondeathswap.commands.admin.TpWorldCommand;
import it.areson.aresondeathswap.events.PlayerEvents;
import it.areson.aresondeathswap.managers.FileManager;
import it.areson.aresondeathswap.managers.MessageManager;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class AresonDeathSwap extends JavaPlugin {

    //TODO Permissions per comandi staff

    public final String ARENAS_PATH = "arenas";
    public final int MIN_PLAYERS = 2;
    public final int MIN_SWAP_TIME_SECONDS = 10;
    public final int MAX_SWAP_TIME_SECONDS = 15;
    public final String MAIN_WORLD_NAME = "world";

    public HashMap<String, Arena> arenas;
    public MessageManager messages;

    private FileManager dataFile;

    @Override
    public void onEnable() {
        arenas = new HashMap<>();
        messages = new MessageManager(this, "messages.yml");
        dataFile = new FileManager(this, "data.yml");
        loadArenas(dataFile);


        new PlayCommand(this);

        new SetArenaCommand(this, dataFile);
        new LoadWorldCommand(this);
        new TpWorldCommand(this);

        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(player -> player.kickPlayer("Server chiuso"));

        unloadArenaWorlds(dataFile);
    }

    public boolean reloadArenaWorld(String worldName) {
        if (getServer().unloadWorld(worldName, false)) {
            getLogger().info("Correcly unloaded world " + worldName);
            return loadArenaWorld(worldName);
        } else {
            getLogger().info("Error while unloading world " + worldName);
            return false;
        }
    }

    private boolean loadArenaWorld(String worldName) {
        World world = new WorldCreator(worldName).createWorld();
        if (world != null) {
            world.setAutoSave(false);
            getLogger().info("Correcly loaded world " + worldName);
            return true;
        } else {
            getLogger().severe("Error while loading world " + worldName);
            return false;
        }
    }

    private void loadArenas(FileManager dataFile) {
        List<String> arenas = dataFile.getFileConfiguration().getStringList(ARENAS_PATH);
        arenas.forEach(this::loadArenaByName);
    }

    public void loadArenaByName(String arenaName) {
        if (!arenas.containsKey(arenaName)) {
            ConfigurationSection arenaSection = dataFile.getFileConfiguration().getConfigurationSection(ARENAS_PATH + "." + arenaName);

            if (!Objects.isNull(arenaSection)) {
                if (loadArenaWorld(arenaName)) {
                    arenas.put(arenaName, new Arena(this, arenaName));
                    getLogger().info("World " + arenaName + " loaded successfully");
                } else {
                    getLogger().severe("Error while loading world " + arenaName);
                }
            } else {
                getLogger().warning("No arenas section found");
            }
        } else {
            getLogger().warning("Arena already intialized");
        }
    }

    private void unloadArenaWorlds(FileManager dataFile) {
        ConfigurationSection arenaSection = dataFile.getFileConfiguration().getConfigurationSection(ARENAS_PATH);

        if (!Objects.isNull(arenaSection)) {
            arenaSection.getKeys(false).forEach(arenaName -> {
                if (getServer().unloadWorld(arenaName, false)) {
                    getLogger().info("World " + arenaName + " unloaded successfully");
                } else {
                    getLogger().info("Error while unloading world " + arenaName);
                }
            });
        }
    }

    public void teleportToLobbySpawn(Player player) {
        World world = getServer().getWorld(MAIN_WORLD_NAME);
        if (world != null) {
            player.teleport(world.getSpawnLocation());
        } else {
            getLogger().severe("Cannot found main world");
        }
    }

}
