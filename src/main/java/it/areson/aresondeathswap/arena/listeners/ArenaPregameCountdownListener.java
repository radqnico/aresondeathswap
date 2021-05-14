package it.areson.aresondeathswap.arena.listeners;

import it.areson.aresoncore.time.countdown.Countdown;
import it.areson.aresoncore.time.countdown.CountdownManager;
import it.areson.aresoncore.time.countdown.listeners.CountdownListener;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.Constants;
import it.areson.aresondeathswap.arena.Arena;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.utils.Message;
import it.areson.aresondeathswap.utils.MessageManager;
import it.areson.aresondeathswap.utils.Pair;
import it.areson.aresondeathswap.utils.SoundManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArenaPregameCountdownListener implements CountdownListener {

    private final Arena arena;

    public ArenaPregameCountdownListener(Arena arena) {
        this.arena = arena;
        CountdownManager.getInstance().registerListener(this);
    }

    private boolean checkIfThisArena(Countdown countdown) {
        String countdownName = countdown.getName();
        if (countdownName.contains(Constants.COUNTDOWN_PREGAME_SUFFIX)) {
            String arenaName = countdownName.split("_")[0];
            return arena.getArenaName().equals(arenaName);
        }
        return false;
    }

    @Override
    public void countdownFinished(Countdown countdown) {
        if (checkIfThisArena(countdown)) {
            arena.startGame();
            arena.resetStartingCountdown();

            arena.sendMessageToArenaPlayers(AresonDeathSwap.instance.messages.getPlainMessage(Message.GAME_STARTED));
        }
    }

    @Override
    public void countdownShoutRemainingSeconds(Countdown countdown) {
        if (checkIfThisArena(countdown)) {
            MessageManager messages = AresonDeathSwap.instance.messages;
            arena.sendMessageToArenaPlayers(messages.getPlainMessage(
                    Message.STARTING_CD_MESSAGE,
                    Pair.of("%seconds%", countdown.getCurrentRemaining() + "")
            ));
            List<Player> players = arena.getPlayers().keySet().stream().map(DeathswapPlayer::getActualPlayer).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            for (Player player : players) {
                SoundManager.winner(player);
            }
        }
    }
}
