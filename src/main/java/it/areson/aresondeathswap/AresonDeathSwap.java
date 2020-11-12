package it.areson.aresondeathswap;

import it.areson.aresondeathswap.commands.LoadWorldCommand;
import it.areson.aresondeathswap.commands.SetArenaCommand;
import it.areson.aresondeathswap.events.PlayerEvents;
import it.areson.aresondeathswap.handlers.GameHandler;
import it.areson.aresondeathswap.handlers.WorldHandler;
import it.areson.aresondeathswap.utils.Countdown;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class AresonDeathSwap extends JavaPlugin {

    public final String ARENA_PATH = "arena";
    public final int MIN_PLAYERS = 2;

    public HashMap<String, Boolean> joinableArenas;
    public ArrayList<Player> waitingPlayers;
    public HashMap<String, HashSet<Player>> arenasPlayers;
    public HashMap<String, Countdown> arenasCountdowns;

    private MessageManager messages;
    private FileManager dataFile;


    private static AresonDeathSwap instance;
    private static ArrayList<Player> alivePlayers = new ArrayList<>();
    private static ArrayList<Player> lobbyPlayers = new ArrayList<>();
    private static ArrayList<Player> deadPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        joinableArenas = new HashMap<>();
        waitingPlayers = new ArrayList<>();
        arenasPlayers = new HashMap<>();
        arenasCountdowns = new HashMap<>();

        messages = new MessageManager(this, "messages.yml");
        dataFile = new FileManager(this, "data.yml");
        loadArenaWorlds(dataFile);
        new SetArenaCommand(this, dataFile);
        new LoadWorldCommand(this);

        registerEvents();
        instance = this;
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(player -> player.kickPlayer("Server chiuso"));

        unloadArenaWorlds(dataFile);
    }

    public boolean loadArenaWorld(String worldName) {
        World world = new WorldCreator(worldName).createWorld();
        if (world != null) {
            world.setAutoSave(false);
            return true;
        } else {
            return false;
        }
    }

    private void loadArenaWorlds(FileManager dataFile) {
        ConfigurationSection arenaSection = dataFile.getFileConfiguration().getConfigurationSection(ARENA_PATH);

        if (!Objects.isNull(arenaSection)) {
            arenaSection.getKeys(false).forEach(arenaName -> {
                if (loadArenaWorld(arenaName)) {
                    joinableArenas.put(arenaName, false);
                    arenasPlayers.put(arenaName, new HashSet<>());

                    //Countdowns
                    Countdown countdown = new Countdown(this, 20,
                            () -> new GameHandler(
                                    this,
                                    13,
                                    30,
                                    getConfig().getString("messaggio_avviso_in_partita"),
                                    arenaName
                            ).startGame(),
                            () -> getServer().broadcastMessage(messages.getPlainMessage("countdown-starting-message")),
                            10,
                            messages.getPlainMessage("countdown-starting-message"),
                            arenaName
                    );
                    arenasCountdowns.put(arenaName, countdown);

                    getLogger().info("World " + arenaName + " loaded successfully");
                } else {
                    getLogger().info("Error while loading world " + arenaName);
                }
            });
        }
    }

    private void unloadArenaWorlds(FileManager dataFile) {
        ConfigurationSection arenaSection = dataFile.getFileConfiguration().getConfigurationSection(ARENA_PATH);

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

    public Optional<String> getFirstFreeArena() {
        return joinableArenas.entrySet().stream().filter(tuple -> !tuple.getValue()).map(Map.Entry::getKey).findFirst();
    }


    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
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
