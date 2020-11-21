package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DelayedRepeatingTask {

    private final AresonDeathSwap aresonDeathSwap;
    private final Runnable taskToRepeat;
    private final Optional<String> countDownMessage;
    private final List<Player> playersToNotify;
    private int everySeconds;
    private boolean isRunning;
    private int currentTimeRemaining;
    private int callerTaskId = 0;

    public DelayedRepeatingTask(AresonDeathSwap aresonDeathSwap, int everySeconds, Runnable taskToRepeat, Optional<String> countDownMessage, List<Player> playersToNotify) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.everySeconds = everySeconds;
        this.taskToRepeat = taskToRepeat;
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
                            callTask();
                            currentTimeRemaining = everySeconds;
                        } else {
                            if (currentTimeRemaining <= 10) {
                                countDownMessage.ifPresent(s ->
                                        playersToNotify.parallelStream().forEach(player -> {
                                                    player.sendMessage(s.replaceAll("%seconds%", currentTimeRemaining + ""));
                                                    aresonDeathSwap.sounds.tick(player);
                                                }
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

    public void stopRepeating() {
        isRunning = false;
        if (callerTaskId != 0) {
            aresonDeathSwap.getServer().getScheduler().cancelTask(callerTaskId);
            callerTaskId = 0;
        }
    }

    private void callTask() {
        aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(
                aresonDeathSwap,
                taskToRepeat,
                0
        );
    }

    public boolean isRunning() {
        return isRunning;
    }
}
