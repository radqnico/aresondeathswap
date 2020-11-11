package me.dewoji.deathswap;

import me.dewoji.deathswap.events.PlayerEvents;
import me.dewoji.deathswap.handlers.WorldHandler;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class DeathSwap extends JavaPlugin {

    private static DeathSwap instance;
    private static ArrayList<Player> alivePlayers = new ArrayList<>();
    private static boolean theGameIsRunning = false;
    private WorldHandler worldHandler = new WorldHandler(instance);

    @Override
    public void onEnable() {
        instance = this;

        registerEvents();

        saveConfig();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {

    }

    public static void registerEvents() {
        getInstance().getServer().getPluginManager().registerEvents(new PlayerEvents(), instance);
    }

    public static DeathSwap getInstance() {
        return instance;
    }

    public static ArrayList<Player> getAlivePlayers() {
        return alivePlayers;
    }

    public static void setAlivePlayers(ArrayList<Player> alivePlayers) {
        DeathSwap.alivePlayers = alivePlayers;
    }

    public static boolean isTheGameIsRunning() {
        return theGameIsRunning;
    }

    public static void setTheGameIsRunning(boolean theGameIsRunning) {
        DeathSwap.theGameIsRunning = theGameIsRunning;
    }
}
