package it.areson.aresondeathswap.arena;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Optional;

public class ArenaManager {

    private final HashMap<String, Arena> arenas;

    public ArenaManager() {
        this.arenas = new HashMap<>();
    }

    public HashMap<String, Arena> getArenas() {
        return arenas;
    }

    public Arena createNewArenaAndLoadWorld(AresonDeathSwap aresonDeathSwap, String arenaName, String arenaWorldName, int minPlayers) {
        Arena arena = new Arena(aresonDeathSwap, arenaName, arenaWorldName, minPlayers);
        arena.loadArenaWorld();
        addArena(arena);
        return arena;
    }

    public void removeArenaAndUnloadWorld(String arenaName) {
        Arena remove = arenas.remove(arenaName);
        if (remove != null) {
            remove.removeAllPlayersFromArena();
            remove.unloadArenaWorld();
        }
    }

    public void addArena(Arena arena) {
        arenas.put(arena.getArenaName(), arena);
    }

    public Optional<Arena> getArenaByName(String arenaName) {
        return Optional.ofNullable(arenas.get(arenaName));
    }

    public Optional<Arena> getArenaOfPlayer(DeathswapPlayer player) {
        for (Arena arena : arenas.values()) {
            HashMap<DeathswapPlayer, Location> players = arena.getPlayers();
            for (DeathswapPlayer arenaPlayer : players.keySet()) {
                if (player.getNickName().equalsIgnoreCase(arenaPlayer.getNickName())) {
                    return Optional.of(arena);
                }
            }
        }
        return Optional.empty();
    }

    public void removePlayerFromAllArenas(DeathswapPlayer deathswapPlayer) {
        Optional<Arena> arenaOfPlayer = getArenaOfPlayer(deathswapPlayer);
        arenaOfPlayer.ifPresent(arena -> arena.removePlayer(deathswapPlayer));
    }

}
