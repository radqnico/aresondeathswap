package it.areson.aresondeathswap;

import it.areson.aresoncore.database.MySqlConfig;
import it.areson.aresoncore.database.MySqlConnection;
import it.areson.aresondeathswap.arena.ArenaManager;
import it.areson.aresondeathswap.commands.CommandParser;
import it.areson.aresondeathswap.commands.arena.CreateCommand;
import it.areson.aresondeathswap.commands.arena.DeleteCommand;
import it.areson.aresondeathswap.commands.arena.OpenCommand;
import it.areson.aresondeathswap.commands.play.LeaveCommand;
import it.areson.aresondeathswap.commands.play.PlayCommand;
import it.areson.aresondeathswap.commands.situation.SituationCommand;
import it.areson.aresondeathswap.events.ChatEvents;
import it.areson.aresondeathswap.events.DeathEvents;
import it.areson.aresondeathswap.loot.LootConfigReader;
import it.areson.aresondeathswap.player.DeathswapPlayerManager;
import it.areson.aresondeathswap.player.LeaderboardsPlaceholders;
import it.areson.aresondeathswap.player.PlayerStatsPlaceholders;
import it.areson.aresondeathswap.utils.FileManager;
import it.areson.aresondeathswap.utils.MessageManager;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class AresonDeathSwap extends JavaPlugin {

    public static AresonDeathSwap instance;
    public MessageManager messages;
    public LootConfigReader loots;

    private MySqlConnection mySqlConnection;
    private DeathswapPlayerManager deathswapPlayerManager;

    private FileManager arenasFile;
    private FileManager configFile;

    private ArenaManager arenaManager;

    private DeathEvents deathEvents;
    private ChatEvents chatEvents;

    public MySqlConnection getMySqlConnection() {
        return mySqlConnection;
    }

    public DeathswapPlayerManager getDeathswapPlayerManager() {
        return deathswapPlayerManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        messages = new MessageManager(this, "messages.yml");

        saveDefaultConfig();

        configFile = new FileManager(this, "config.yml");

        mySqlConnection = new MySqlConnection(this, new MySqlConfig(
                Constants.MYSQL_HOST,
                Constants.MYSQL_USER,
                Constants.MYSQL_PASS,
                Constants.MYSQL_DB
        ));

        new PlayerStatsPlaceholders().register();
        new LeaderboardsPlaceholders().register();

        deathswapPlayerManager = new DeathswapPlayerManager(this, mySqlConnection, Constants.MYSQL_PLAYER_TABLE);

        arenasFile = new FileManager(this, "arenas.yml");
        arenaManager = new ArenaManager(arenasFile);

        deathEvents = new DeathEvents(this, this);
        chatEvents = new ChatEvents(this);

        loots = new LootConfigReader(this, "loot.yml");
        loots.readLoot();
        registerCommands();
    }

    private void registerCommands() {
        CommandParser arenaParser = new CommandParser(this);
        PluginCommand arena = getCommand("arena");
        if (arena == null) {
            getLogger().log(Level.SEVERE, "Cannot register arena commands");
            return;
        }
        try {
            arenaParser.addAresonCommand(new CreateCommand());
            arenaParser.addAresonCommand(new DeleteCommand());
            arenaParser.addAresonCommand(new OpenCommand());
            arenaParser.registerCommands();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        arena.setExecutor(arenaParser);
        arena.setTabCompleter(arenaParser);

        PluginCommand play = getCommand("play");
        if (play != null) {
            PlayCommand playCommand = new PlayCommand();
            play.setExecutor(playCommand);
            play.setTabCompleter(playCommand);
        }

        PluginCommand leave = getCommand("leave");
        if (leave != null) {
            LeaveCommand leaveCommand = new LeaveCommand();
            leave.setExecutor(leaveCommand);
            leave.setTabCompleter(leaveCommand);
        }

        PluginCommand situation = getCommand("situation");
        if (situation != null) {
            SituationCommand situationCommand = new SituationCommand();
            situation.setExecutor(situationCommand);
            situation.setTabCompleter(situationCommand);
        }
    }

    @Override
    public void onDisable() {
        arenaManager.unloadAllWorld();
        deathswapPlayerManager.saveAllPlayers();
        deathswapPlayerManager.unregisterEvents();
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public FileManager getConfigFile() {
        return configFile;
    }

    public Location getLobbySpawn() {
        return configFile.getLocation("spawn").orElseGet(() -> Objects.requireNonNull(getServer().getWorld("world")).getSpawnLocation());
    }
}
