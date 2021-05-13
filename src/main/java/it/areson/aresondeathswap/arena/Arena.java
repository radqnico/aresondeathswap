package it.areson.aresondeathswap.arena;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.player.DeathswapPlayer;

import java.util.ArrayList;
import java.util.List;

public class Arena {

    private AresonDeathSwap aresonDeathSwap;

    private String arenaName;
    private String arenaWorldName;

    private ArenaStatus arenaStatus;
    private List<DeathswapPlayer> players;
    private int minPlayers;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName, String arenaWorldName, int minPlayers) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.arenaWorldName = arenaWorldName;
        this.minPlayers = minPlayers;

        this.arenaStatus = ArenaStatus.CLOSED;
        this.players = new ArrayList<>();
    }

    public boolean addPlayer(DeathswapPlayer deathswapPlayer) {
        long count = players.parallelStream()
                .filter(dsPlayer -> dsPlayer.getNickName().equalsIgnoreCase(deathswapPlayer.getNickName()))
                .count();
        if (count > 0) {
            return false;
        }
        return players.add(deathswapPlayer);
    }

    public void removePlayer(DeathswapPlayer deathswapPlayer) {
        players.remove(deathswapPlayer);
    }

}
