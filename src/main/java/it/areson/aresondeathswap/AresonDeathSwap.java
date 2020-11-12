package it.areson.aresondeathswap;

import it.areson.aresondeathswap.commands.LoadWorldCommand;
import it.areson.aresondeathswap.commands.SetArenaCommand;
import it.areson.aresondeathswap.events.PlayerEvents;
import it.areson.aresondeathswap.handlers.WorldHandler;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Objects;

public final class AresonDeathSwap extends JavaPlugin {

    public final String ARENA_PATH = "arena";

    private FileManager dataFile;

    private static AresonDeathSwap instance;
    private static ArrayList<Player> alivePlayers = new ArrayList<>();
    private static ArrayList<Player> lobbyPlayers = new ArrayList<>();
    private static ArrayList<Player> deadPlayers = new ArrayList<>();
    private WorldHandler worldHandler = new WorldHandler(instance);

    @Override
    public void onEnable() {
        instance = this;


        registerEvents();
        saveDefaultConfig();

        dataFile = new FileManager(this, "data.yml");
        loadArenaWorlds(dataFile);
        new SetArenaCommand(this, dataFile);
        new LoadWorldCommand(this);
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(player -> player.kickPlayer("Server chiuso"));

        unloadArenaWorlds(dataFile);
    }

    public void loadArenaWorld(String worldName) {
        World world = new WorldCreator(worldName).createWorld();
        if (world != null) {
            world.setAutoSave(false);
        }
    }

    private void loadArenaWorlds(FileManager dataFile) {
        ConfigurationSection arenaSection = dataFile.getFileConfiguration().getConfigurationSection(ARENA_PATH);

        if (!Objects.isNull(arenaSection)) {
            arenaSection.getKeys(false).forEach(this::loadArenaWorld);
        }
    }

    private void unloadArenaWorlds(FileManager dataFile) {
        ConfigurationSection arenaSection = dataFile.getFileConfiguration().getConfigurationSection(ARENA_PATH);

        if (!Objects.isNull(arenaSection)) {
            arenaSection.getKeys(false).forEach(arenaName -> {
                if(getServer().unloadWorld(arenaName, false)) {
                    getLogger().info("World " + arenaName + " unloaded successfully");
                } else {
                    getLogger().info("Error while unloading world " + arenaName);
                }
            });
        }
    }



    public static void registerEvents() {
        getInstance().getServer().getPluginManager().registerEvents(new PlayerEvents(), instance);
    }

    public static AresonDeathSwap getInstance() {
        return instance;
    }

    public static ArrayList<Player> getAlivePlayers() {
        return alivePlayers;
    }

    public static void setAlivePlayers(ArrayList<Player> alivePlayers) {
        AresonDeathSwap.alivePlayers = alivePlayers;
    }

    public static ArrayList<Player> getLobbyPlayers() {
        return lobbyPlayers;
    }

    public static void setLobbyPlayers(ArrayList<Player> lobbyPlayers) {
        AresonDeathSwap.lobbyPlayers = lobbyPlayers;
    }

    public static ArrayList<Player> getDeadPlayers() {
        return deadPlayers;
    }

    public static void setDeadPlayers(ArrayList<Player> deadPlayers) {
        AresonDeathSwap.deadPlayers = deadPlayers;
    }

}
