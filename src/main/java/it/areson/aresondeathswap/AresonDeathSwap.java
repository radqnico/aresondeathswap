package it.areson.aresondeathswap;

import it.areson.aresondeathswap.commands.LeaveCommand;
import it.areson.aresondeathswap.commands.PlayCommand;
import it.areson.aresondeathswap.commands.SpawnCommand;
import it.areson.aresondeathswap.commands.admin.*;
import it.areson.aresondeathswap.enums.ArenaStatus;
import it.areson.aresondeathswap.events.PlayerEvents;
import it.areson.aresondeathswap.loot.LootConfigReader;
import it.areson.aresondeathswap.managers.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class AresonDeathSwap extends JavaPlugin {

    public final String ARENAS_PATH = "arenas";
    public final int MIN_PLAYERS = getConfig().getInt("arena-min-players");
    public final int STARTING_TIME = getConfig().getInt("arena-starting-seconds");
    public final int MIN_SWAP_TIME_SECONDS = getConfig().getInt("arena-min-swap-seconds");
    public final int MAX_SWAP_TIME_SECONDS = getConfig().getInt("arena-max-swap-seconds");
    public final int MAX_ROUNDS = getConfig().getInt("arena-max-rounds");
    public final String MAIN_WORLD_NAME = "world";

    public HashMap<String, Arena> arenas;
    public MessageManager messages;
    public TitlesManager titles;
    public SoundManager sounds;
    public EffectManager effects;
    public EventCallManager eventCall;
    public LootConfigReader loot;

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
        loot = new LootConfigReader(this, "loot.yml");
        loadArenas(dataFile);

        new PlayCommand(this);
        new LeaveCommand(this);

        new SetArenaCommand(this, dataFile);
        new DeleteArenaCommand(this, dataFile);
        new LoadWorldCommand(this);
        new TpWorldCommand(this);
        new SetSpawnCommand(this, dataFile);
        new SpawnCommand(this);
        new TestChestCommand(this);

        loot.readLoot();

        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);

        StringBuilder str = new StringBuilder();
        getServer().getScheduler().scheduleSyncRepeatingTask(
                this,
                () -> {
                    getServer().getScheduler().getActiveWorkers().forEach(bukkitWorker -> str.append("Worker: " + bukkitWorker.getTaskId() +", "));
                    getLogger().warning(str.toString());
                },
                0,
                20
        );
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
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            getLogger().info("Correcly loaded world " + worldName);
            return true;
        } else {
            getLogger().severe("Error while loading world " + worldName);
            return false;
        }
    }

    public boolean deleteWorld(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteWorld(file);
                } else {
                    file.delete();
                }
            }
        }
        return (path.delete());
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

    public void restorePlayerState(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setTotalExperience(0);
        player.setExp(0);
        player.setLevel(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
    }

    public CompletableFuture<Boolean> teleportToLobbySpawn(Player player) {
        World world = getServer().getWorld(MAIN_WORLD_NAME);
        restorePlayerState(player);
        if (world != null) {
            Location lobbySpawn = dataFile.getLocation("lobby-spawn");
            if (lobbySpawn != null) {
                return player.teleportAsync(lobbySpawn);
            } else {
                return player.teleportAsync(world.getSpawnLocation());
            }
        } else {
            getLogger().severe("Cannot found main world");
        }
        return null;
    }

    public void removePlayerFromArenas(Player player) {
        arenas.forEach((arenaName, arena) -> {
            if (arena.getPlayers().contains(player)) {
                if (arena.getArenaStatus().equals(ArenaStatus.InGame)) {
                    eventCall.callPlayerLose(player);
                }
                arena.removePlayer(player);
            }
        });
    }

    public boolean playerIsInAnArena(Player player) {
        return arenas.entrySet().stream().anyMatch(arenaEntry -> arenaEntry.getValue().getPlayers().contains(player));
    }

    public Optional<String> getArenaNameFromPlayer(Player player) {
        return arenas.entrySet().stream().filter(entry -> entry.getValue().getPlayers().contains(player)).map(Map.Entry::getKey).findFirst();
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
