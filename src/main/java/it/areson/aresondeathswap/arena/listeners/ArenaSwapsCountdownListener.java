package it.areson.aresondeathswap.arena.listeners;

import it.areson.aresoncore.time.countdown.Countdown;
import it.areson.aresoncore.time.countdown.CountdownManager;
import it.areson.aresoncore.time.countdown.listeners.CountdownListener;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.Constants;
import it.areson.aresondeathswap.arena.Arena;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.utils.Message;
import it.areson.aresondeathswap.utils.Pair;
import it.areson.aresondeathswap.utils.SoundManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArenaSwapsCountdownListener implements CountdownListener {

    private final Arena arena;

    public ArenaSwapsCountdownListener(Arena arena) {
        this.arena = arena;
        CountdownManager.getInstance().registerListener(this);
    }

    private boolean checkIfThisArena(Countdown countdown) {
        String countdownName = countdown.getName();
        if (countdownName.contains(Constants.COUNTDOWN_SWAP_SUFFIX)) {
            String arenaName = countdownName.split("_")[0];
            return arena.getArenaName().equals(arenaName);
        }
        return false;
    }

    @Override
    public void countdownFinished(Countdown countdown) {
        if (checkIfThisArena(countdown)) {
            arena.swapPlayers();
            arena.resetSwapsCountdown();
            arena.startSwapsCountdown();
            arena.sendMessageToArenaPlayers(AresonDeathSwap.instance.messages.getPlainMessage(
                    Message.SWAP_PREPARE
            ));
        }
    }

    @Override
    public void countdownShoutRemainingSeconds(Countdown countdown) {
        if (checkIfThisArena(countdown)) {
            arena.sendMessageToArenaPlayers(AresonDeathSwap.instance.messages.getPlainMessage(
                    Message.SWAP_CD_MESSAGE,
                    Pair.of("%seconds%", countdown.getCurrentRemaining() + "")
            ));
            List<Player> players = arena.getPlayers().keySet().stream().map(DeathswapPlayer::getActualPlayer).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            for (Player player : players) {
                SoundManager.tick(player);
            }
        }
    }
}
