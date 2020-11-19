package it.areson.aresondeathswap;

import it.areson.aresondeathswap.commands.LeaveCommand;
import it.areson.aresondeathswap.commands.admin.DeleteArenaCommand;
import it.areson.aresondeathswap.commands.admin.LoadWorldCommand;
import it.areson.aresondeathswap.commands.PlayCommand;
import it.areson.aresondeathswap.commands.admin.SetArenaCommand;
import it.areson.aresondeathswap.commands.admin.TpWorldCommand;
import it.areson.aresondeathswap.enums.ArenaStatus;
import it.areson.aresondeathswap.events.PlayerEvents;
import it.areson.aresondeathswap.managers.*;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class AresonDeathSwap extends JavaPlugin {

    public final String ARENAS_PATH = "arenas";
    public final int MIN_PLAYERS = getConfig().getInt("arena-min-players");
    public final int STARTING_TIME = getConfig().getInt("arena-starting-seconds");
    public final int MIN_SWAP_TIME_SECONDS = getConfig().getInt("arena-min-swap-seconds");
    public final int MAX_SWAP_TIME_SECONDS = getConfig().getInt("arena-max-swap-seconds");
    public final String MAIN_WORLD_NAME = "world";

    public HashMap<String, Arena> arenas;
    public MessageManager messages;
    public TitlesManager titles;
    public SoundManager sounds;
    public EffectManager effects;
    public EventCallManager eventCall;

    private FileManager dataFile;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        arenas = new HashMap<>();
        sounds = new SoundManager();
        effects = new EffectManager(this);
        messages = new MessageManager(this, "messages.yml");
        dataFile = new FileManager(this, "data.yml");
        titles = new TitlesManager(messages);
        eventCall = new EventCallManager(this);
        loadArenas(dataFile);


        new PlayCommand(this);
        new LeaveCommand(this);

        new SetArenaCommand(this, dataFile);
        new DeleteArenaCommand(this, dataFile);
        new LoadWorldCommand(this);
        new TpWorldCommand(this);

        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(player -> player.kickPlayer("Server chiuso"));

        unloadArenaWorlds(dataFile);
    }

    public void reloadArenaWorld(String worldName) {
        World world = getServer().getWorld(worldName);
        if(world!=null){
            while(world.getPlayers().size()>0){
                for (Player player : world.getPlayers()) {
                    teleportToLobbySpawn(player);
                }
            }
            if (getServer().unloadWorld(worldName, false)) {
                getLogger().info("Correcly unloaded world " + worldName);
                loadArenaWorld(worldName);
            } else {
                getLogger().info("Error while unloading world " + worldName);
            }
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

    private boolean unloadArenaWorld(String worldName) {
        if (getServer().unloadWorld(worldName, false)) {
            getLogger().info("World " + worldName + " unloaded successfully");
            return true;
        } else {
            getLogger().info("Error while unloading world " + worldName);
            return false;
        }
    }

    private void loadArenas(FileManager dataFile) {
        List<String> arenas = dataFile.getFileConfiguration().getStringList(ARENAS_PATH);
        arenas.forEach(this::loadArenaByName);
    }

    public void loadArenaByName(String arenaName) {
        if (!arenas.containsKey(arenaName)) {
            List<String> arenas = dataFile.getFileConfiguration().getStringList(ARENAS_PATH);

            if (arenas.contains(arenaName)) {
                if (loadArenaWorld(arenaName)) {
                    this.arenas.put(arenaName, new Arena(this, arenaName));
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
        List<String> arenas = dataFile.getFileConfiguration().getStringList(ARENAS_PATH);
        arenas.forEach(this::unloadArenaWorld);
    }

    public void teleportToLobbySpawn(Player player) {
        World world = getServer().getWorld(MAIN_WORLD_NAME);
        if (world != null) {
            player.teleport(world.getSpawnLocation());
        } else {
            getLogger().severe("Cannot found main world");
        }
    }

    public void removePlayerFromArenas(Player player) {
        arenas.forEach((arenaName, arena) -> {
            if (arena.getPlayers().contains(player)) {
                if(arena.getArenaStatus().equals(ArenaStatus.InGame)){
                    eventCall.callPlayerLose(player);
                }
                arena.removePlayer(player);
            }
        });
    }

    public boolean playerIsInAnArena(Player player) {
        return arenas.entrySet().stream().anyMatch(arenaEntry -> arenaEntry.getValue().getPlayers().contains(player));
    }

    public void kickPlayersFromWorld(String worldName) {
        World world = getServer().getWorld(worldName);
        if (world != null) {
            world.getPlayers().forEach(this::teleportToLobbySpawn);
        } else {
            getLogger().severe("Cannot find world " + worldName);
        }
    }

}
