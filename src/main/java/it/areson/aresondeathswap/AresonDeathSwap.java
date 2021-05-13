package it.areson.aresondeathswap;

import it.areson.aresoncore.database.MySqlConfig;
import it.areson.aresoncore.database.MySqlConnection;
import it.areson.aresondeathswap.arena.ArenaManager;
import it.areson.aresondeathswap.commands.CommandParser;
import it.areson.aresondeathswap.commands.arena.CreateCommand;
import it.areson.aresondeathswap.player.DeathswapPlayerManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class AresonDeathSwap extends JavaPlugin {

    public static AresonDeathSwap instance;

    private MySqlConnection mySqlConnection;
    private DeathswapPlayerManager deathswapPlayerManager;

    private ArenaManager arenaManager;

    public MySqlConnection getMySqlConnection() {
        return mySqlConnection;
    }

    public DeathswapPlayerManager getDeathswapPlayerManager() {
        return deathswapPlayerManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        mySqlConnection = new MySqlConnection(this, new MySqlConfig(
                Constants.MYSQL_HOST,
                Constants.MYSQL_USER,
                Constants.MYSQL_PASS,
                Constants.MYSQL_DB
        ));

        deathswapPlayerManager = new DeathswapPlayerManager(this, mySqlConnection, Constants.MYSQL_PLAYER_TABLE);

        arenaManager = new ArenaManager();

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
            arenaParser.registerCommands();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        arena.setExecutor(arenaParser);
        arena.setTabCompleter(arenaParser);
    }

    @Override
    public void onDisable() {
        deathswapPlayerManager.saveAllPlayers();
        deathswapPlayerManager.unregisterEvents();
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}
