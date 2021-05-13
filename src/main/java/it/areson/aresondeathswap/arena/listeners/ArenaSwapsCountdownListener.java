package it.areson.aresondeathswap.arena.listeners;

import it.areson.aresoncore.time.countdown.Countdown;
import it.areson.aresoncore.time.countdown.CountdownManager;
import it.areson.aresoncore.time.countdown.listeners.CountdownListener;
import it.areson.aresondeathswap.Constants;
import it.areson.aresondeathswap.arena.Arena;

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
        }
    }

    @Override
    public void countdownShoutRemainingSeconds(Countdown countdown) {
        if (checkIfThisArena(countdown)) {
            String message = "Lo swap avverrà tra " + countdown.getCurrentRemaining() + "s. Preparati!";
            arena.sendMessageToArenaPlayers(message);
        }
    }
}
