package it.areson.aresondeathswap.arena;

import it.areson.aresoncore.time.countdown.Countdown;
import it.areson.aresoncore.time.countdown.listeners.CountdownListener;
import it.areson.aresondeathswap.Constants;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;

public class ArenaPregameCountdownListener implements CountdownListener {

    private final Arena arena;

    public ArenaPregameCountdownListener(Arena arena) {
        this.arena = arena;
    }

    private boolean checkIfThisArena(Countdown countdown) {
        String countdownName = countdown.getName();
        if (countdownName.contains(Constants.COUNTDOWN_PREGAME_SUFFIX)) {
            String arenaName = countdownName.split("_")[0];
            if (arena.getArenaName().equals(arenaName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void countdownFinished(Countdown countdown) {
        if (checkIfThisArena(countdown)) {
            arena.startGame();
            arena.resetStartingCountdown();
        }
    }

    @Override
    public void countdownShoutRemainingSeconds(Countdown countdown) {
        if (checkIfThisArena(countdown)) {
            Set<DeathswapPlayer> deathswapPlayers = arena.getPlayers().keySet();
            for (DeathswapPlayer deathswapPlayer : deathswapPlayers) {
                Optional<Player> actualPlayer = deathswapPlayer.getActualPlayer();
                actualPlayer.ifPresent(player -> player.sendMessage("Il gioco inizierà tra " + countdown.getCurrentRemaining() + "s. Preparati!"));
            }
        }
    }
}
