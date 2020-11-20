package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class DelayedRepeatingTask {

    private AresonDeathSwap aresonDeathSwap;
    private int everySeconds;
    private boolean isRunning;
    private int currentTimeRemaining;

    private int callerTaskId = 0;

    private Runnable taskToRepeat;

    private boolean isAsync;

    private Optional<String> countDownMessage;
    private List<Player> playersToNotify;

    public DelayedRepeatingTask(AresonDeathSwap aresonDeathSwap, int everySeconds, Runnable taskToRepeat, Optional<String> countDownMessage, List<Player> playersToNotify, boolean isAsync) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.everySeconds = everySeconds;
        this.taskToRepeat = taskToRepeat;
        this.isAsync = isAsync;
        this.playersToNotify = playersToNotify;
        this.isRunning = false;
        this.countDownMessage = countDownMessage;
        currentTimeRemaining = 0;
    }

    public void setEverySeconds(int everySeconds) {
        this.everySeconds = everySeconds;
    }

    public void startRepeating() {
        isRunning = true;
        currentTimeRemaining = everySeconds;
        callerTaskId = aresonDeathSwap.getServer().getScheduler().scheduleSyncRepeatingTask(
                aresonDeathSwap,
                () -> {
                    if (isRunning) {
                        if (currentTimeRemaining <= 0) {
                            callTask(isAsync);
                            currentTimeRemaining = everySeconds;
                        } else {
                            if(currentTimeRemaining<10){
                                countDownMessage.ifPresent(s ->
                                        playersToNotify.parallelStream().forEach(player ->
                                                player.sendMessage(s.replaceAll("%seconds%", (1 + currentTimeRemaining) + ""))
                                        )
                                );
                            }
                            currentTimeRemaining--;
                        }
                    }
                },
                0,
                20
        );
    }

    private void stopRepeating() {
        isRunning = false;
        aresonDeathSwap.getServer().getScheduler().cancelTask(callerTaskId);
    }

    private void callTask(boolean isAsync) {
        if (isAsync) {
            aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(
                    aresonDeathSwap,
                    taskToRepeat,
                    0
            );
        } else {
            aresonDeathSwap.getServer().getScheduler().runTaskAsynchronously(
                    aresonDeathSwap,
                    taskToRepeat
            );
        }
    }

}
