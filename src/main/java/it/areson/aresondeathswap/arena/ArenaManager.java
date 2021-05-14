package it.areson.aresondeathswap.arena;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.utils.FileManager;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ArenaManager {

    private final HashMap<String, Arena> arenas;
    private final FileManager arenasFile;

    public ArenaManager(FileManager arenasFile) {
        this.arenas = new HashMap<>();
        this.arenasFile = arenasFile;
        readArenasFromFile();
    }

    private void readArenasFromFile() {
        List<String> allArenasStrings = arenasFile.getAllArenasStrings();
        for (String arenaString : allArenasStrings) {
            String[] split = arenaString.split(";");
            Arena newArenaAndLoadWorld = createNewArenaAndLoadWorld(AresonDeathSwap.instance, split[0], split[1], Integer.parseInt(split[2]));
            newArenaAndLoadWorld.open();
        }
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
        Arena removedArena = arenas.remove(arenaName);
        if (removedArena != null) {
            removedArena.unregisterListeners();
            removedArena.removeAllPlayersFromArena();
            removedArena.unloadArenaWorld();
        }
    }

    public void addArena(Arena arena) {
        String arenaName = arena.getArenaName();

        arenas.put(arenaName, arena);
        arenasFile.addArena(arenaName, arena.getArenaWorld().getName(), arena.getMinPlayers());
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
        arenaOfPlayer.ifPresent(arena -> {
            if (arena.getArenaStatus().equals(ArenaStatus.IN_GAME)) {
                deathswapPlayer.getActualPlayer().ifPresent(player -> {
                    player.teleport(Objects.requireNonNull(player.getServer().getWorld("world")).getSpawnLocation());
                });
            }
            arena.removePlayer(deathswapPlayer, true);
        });
    }

    public void unloadAllWorld() {
        for (Arena arena : arenas.values()) {
            arena.unloadArenaWorld();
        }
    }
}
