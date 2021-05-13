package it.areson.aresondeathswap.arena;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Optional;

public class ArenaManager {

    private final HashMap<String, Arena> arenas;
    private final HashMap<DeathswapPlayer, Arena> playerArenas;

    public ArenaManager() {
        this.arenas = new HashMap<>();
        this.playerArenas = new HashMap<>();
    }

    public HashMap<String, Arena> getArenas() {
        return arenas;
    }

    public HashMap<DeathswapPlayer, Arena> getPlayerArenas() {
        return playerArenas;
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
            remove.kickAllPlayersFromArena();
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
        return Optional.ofNullable(playerArenas.get(player));
    }

    public void playerJoinArena(DeathswapPlayer deathswapPlayer, Arena arena, Location previousLocation) {
        arena.addPlayer(deathswapPlayer, previousLocation);
        playerArenas.put(deathswapPlayer, arena);
    }

    public void playerLeaveArena(DeathswapPlayer deathswapPlayer) {
        Arena arena = playerArenas.remove(deathswapPlayer);
        if (arena != null) {
            arena.removePlayer(deathswapPlayer);
        }
    }

    public void removePlayerFromAllArenas(DeathswapPlayer deathswapPlayer) {
        Optional<Arena> arenaOfPlayer = getArenaOfPlayer(deathswapPlayer);
        arenaOfPlayer.ifPresent(arena -> arena.removePlayer(deathswapPlayer));
    }

}
