package it.areson.aresondeathswap;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import it.areson.aresondeathswap.commands.LeaveCommand;
import it.areson.aresondeathswap.commands.PlayCommand;
import it.areson.aresondeathswap.commands.SpawnCommand;
import it.areson.aresondeathswap.commands.admin.*;
import it.areson.aresondeathswap.enums.ArenaStatus;
import it.areson.aresondeathswap.listeners.PlayerEvents;
import it.areson.aresondeathswap.loot.LootConfigReader;
import it.areson.aresondeathswap.managers.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

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
    private MultiverseCore multiverseCore;

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
        multiverseCore = JavaPlugin.getPlugin(MultiverseCore.class);

        loadArenas(dataFile);

        new PlayCommand(this);
        new LeaveCommand(this);

        new SetArenaCommand(this, dataFile, multiverseCore);
        new DeleteArenaCommand(this, dataFile);
        new SetSpawnCommand(this, dataFile);
        new SpawnCommand(this);
        new TestCommand(this);
        new ForceSwapCommand(this);

        loot.readLoot();

        new PlayerEvents(this);
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(player -> player.kickPlayer("Server chiuso"));

        arenas.keySet().forEach(this::deleteArenaWorld);
    }

    public boolean resetArenaWorld(String arenaName) {
        String arenaWorld = arenaName + "Game";

        if (multiverseCore.getMVWorldManager().deleteWorld(arenaWorld)) {

            if (multiverseCore.getMVWorldManager().cloneWorld(arenaName, arenaWorld)) {
                MultiverseWorld mvWorld = multiverseCore.getMVWorldManager().getMVWorld(arenaWorld);

                if (mvWorld != null) {
                    mvWorld.getCBWorld().setAutoSave(false);
                    getLogger().info("World " + arenaWorld + " reset successfully");
                    return true;
                } else {
                    getLogger().severe("Error while getting MultiVerse world " + arenaWorld + " in resetArenaWorld");
                }
            } else {
                getLogger().severe("Error while loading MultiVerse world " + arenaWorld + " in resetArenaWorld");
            }
        } else {
            getLogger().severe("Error while deleting world " + arenaWorld + " in resetArenaWorld");
        }

        return false;
    }

    private void deleteArenaWorld(String worldName) {
        String arenaWorld = worldName + "Game";

        if (multiverseCore.getMVWorldManager().deleteWorld(arenaWorld)) {
            getLogger().info("World " + arenaWorld + " deleted successfully");
        } else {
            getLogger().info("Error while deleting world " + arenaWorld);
        }
    }

    private void loadArenas(FileManager dataFile) {
        List<String> configArenas = dataFile.getFileConfiguration().getStringList(ARENAS_PATH);
        configArenas.forEach(this::loadArenaByName);
    }

    public void loadArenaByName(String arenaName) {
        if (!arenas.containsKey(arenaName)) {
            List<String> configArenas = dataFile.getFileConfiguration().getStringList(ARENAS_PATH);

            if (configArenas.contains(arenaName)) {
                multiverseCore.getMVWorldManager().deleteWorld(arenaName + "Game");

                if (multiverseCore.getMVWorldManager().cloneWorld(arenaName, arenaName + "Game")) {
                    arenas.put(arenaName, new Arena(this, arenaName));
                    getLogger().info("World " + arenaName + "Game loaded successfully");
                } else {
                    getLogger().severe("Error while loading MultiVerse world " + arenaName);
                }
            } else {
                getLogger().severe("No arenas section found");
            }
        } else {
            getLogger().severe("Arena already intialized");
        }
    }

    public void restorePlayerState(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setTotalExperience(0);
        player.setExp(0);
        player.setLevel(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(10);
        player.getInventory().clear();
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
    }

    public CompletableFuture<Boolean> teleportToLobbySpawn(Player player) {
        World world = getServer().getWorld(MAIN_WORLD_NAME);
        restorePlayerState(player);
        if (world != null) {
            if (player.isOnline() && !player.isDead()) {
                Optional<Location> location = dataFile.getLocation("lobby-spawn");
                return location.map(player::teleportAsync).orElseGet(() -> player.teleportAsync(world.getSpawnLocation()));
            }
        } else {
            getLogger().severe("Cannot found main world");
        }
        return CompletableFuture.completedFuture(false);
    }

    public Optional<Location> getLobbyLocation() {
        World world = getServer().getWorld(MAIN_WORLD_NAME);
        if (world != null) {
            Optional<Location> location = dataFile.getLocation("lobby-spawn");
            if (location.isPresent()) {
                return location;
            } else {
                return Optional.of(world.getSpawnLocation());
            }
        } else {
            return Optional.empty();
        }
    }

    public void removePlayerFromArenas(Player player) {
        arenas.forEach((arenaName, arena) -> {
            if (arena.getPlayers().contains(player)) {
                if (arena.getArenaStatus().equals(ArenaStatus.InGame)) {
                    eventCall.callPlayerLose(player);
                    eventCall.callPlayerEndGame(player);
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
