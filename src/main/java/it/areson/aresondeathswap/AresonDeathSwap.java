package it.areson.aresondeathswap;

import it.areson.aresondeathswap.commands.TestCommand;
import it.areson.aresondeathswap.events.PlayerEvents;
import it.areson.aresondeathswap.handlers.WorldHandler;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class AresonDeathSwap extends JavaPlugin {

    private static AresonDeathSwap instance;
    private static ArrayList<Player> alivePlayers = new ArrayList<>();
    private static ArrayList<Player> lobbyPlayers = new ArrayList<>();
    private static ArrayList<Player> deadPlayers = new ArrayList<>();
    private WorldHandler worldHandler = new WorldHandler(instance);

    @Override
    public void onEnable() {
        instance = this;

        registerEvents();

        saveConfig();
        saveDefaultConfig();

        World worldTest = new WorldCreator("worldTest").createWorld();
        worldTest.setAutoSave(false);
        new TestCommand(this);
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