package it.areson.aresondeathswap.arena;

import it.areson.aresoncore.time.countdown.Countdown;
import it.areson.aresoncore.time.countdown.CountdownManager;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.Constants;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Arena {

    private final HashMap<DeathswapPlayer, Location> players;
    private final AresonDeathSwap aresonDeathSwap;
    private final String arenaName;
    private final String arenaWorldName;
    private World arenaWorld;
    private ArenaStatus arenaStatus;
    private int minPlayers;

    private Countdown countdownStarting;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName, String arenaWorldName, int minPlayers) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.arenaWorldName = arenaWorldName;
        this.minPlayers = minPlayers;

        this.arenaStatus = ArenaStatus.CLOSED;
        this.players = new HashMap<>();

        resetStartingCountdown();
    }

    public void resetStartingCountdown() {
        countdownStarting = new Countdown(arenaName + Constants.COUNTDOWN_PREGAME_SUFFIX, 30, 5);
    }

    private void startStartingCountdown() {
        CountdownManager.getInstance().startCountdown(countdownStarting);
    }

    public HashMap<DeathswapPlayer, Location> getPlayers() {
        return players;
    }

    public String getArenaName() {
        return arenaName;
    }

    public World getArenaWorld() {
        return arenaWorld;
    }

    public ArenaStatus getArenaStatus() {
        return arenaStatus;
    }

    public void open() {
        this.arenaStatus = ArenaStatus.OPEN;
    }

    public void addPlayer(DeathswapPlayer deathswapPlayer, Location previousLocation) {
        players.putIfAbsent(deathswapPlayer, previousLocation);
        openToStartingIfMinPlayersReached();
    }

    public void loadArenaWorld() {
        arenaWorld = aresonDeathSwap.getServer().createWorld(new WorldCreator(arenaWorldName));
        if (arenaWorld != null) {
            arenaWorld.setAutoSave(false);
        }
    }

    public void unloadArenaWorld() {
        if (aresonDeathSwap.getServer().unloadWorld(arenaWorld, false)) {
            aresonDeathSwap.getLogger().info("Unloaded arena world " + arenaName);
        } else {
            aresonDeathSwap.getLogger().info("CANNOT unload arena world " + arenaName);
        }
    }

    private void returnPlayerToPreviousLocation(DeathswapPlayer player) {
        player.getActualPlayer().ifPresent(player1 -> player1.teleport(players.get(player)));
    }

    public void removePlayer(DeathswapPlayer deathswapPlayer) {
        if (arenaStatus.equals(ArenaStatus.IN_GAME)) {
            returnPlayerToPreviousLocation(deathswapPlayer);
        }
        players.remove(deathswapPlayer);
        startingToOpenIfNotMinPlayersReach();
    }

    public void removeAllPlayersFromArena() {
        for (Map.Entry<DeathswapPlayer, Location> entry : players.entrySet()) {
            removePlayer(entry.getKey());
        }
        players.clear();
    }

    public boolean isMinPlayerReached() {
        return players.size() >= minPlayers;
    }

    public void openToStartingIfMinPlayersReached() {
        if (arenaStatus.equals(ArenaStatus.OPEN) && isMinPlayerReached()) {
            arenaStatus = ArenaStatus.STARTING;
            startStartingCountdown();
        }
    }

    public void startingToOpenIfNotMinPlayersReach() {
        if (arenaStatus.equals(ArenaStatus.STARTING) && !isMinPlayerReached()) {
            arenaStatus = ArenaStatus.OPEN;
        }
    }

    public boolean canNewPlayerJoin() {
        return arenaStatus.equals(ArenaStatus.OPEN) || arenaStatus.equals(ArenaStatus.STARTING);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Arena)) return false;
        Arena arena = (Arena) o;
        return Objects.equals(arenaName, arena.arenaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arenaName);
    }

    public void startGame() {
        if (arenaStatus.equals(ArenaStatus.STARTING)) {
            arenaStatus = ArenaStatus.IN_GAME;
        }
    }
}
