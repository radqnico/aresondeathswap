package it.areson.aresondeathswap;

import it.areson.aresoncore.database.MySqlConfig;
import it.areson.aresoncore.database.MySqlConnection;
import it.areson.aresondeathswap.player.DeathswapPlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AresonDeathSwap extends JavaPlugin {

    public static AresonDeathSwap instance;

    private MySqlConnection mySqlConnection;
    private DeathswapPlayerManager deathswapPlayerManager;

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
    }

    @Override
    public void onDisable() {
        deathswapPlayerManager.saveAllPlayers();
        deathswapPlayerManager.unregisterEvents();
    }

}
