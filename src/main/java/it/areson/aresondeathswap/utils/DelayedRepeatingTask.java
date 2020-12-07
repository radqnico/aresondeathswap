package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DelayedRepeatingTask {

    private final AresonDeathSwap aresonDeathSwap;
    private final Runnable taskToRepeat;
    private final Optional<String> countDownMessage;
    private final List<Player> playersToNotify;
    private int everySeconds;
    private boolean isRunning;
    private int currentTimeRemaining;
    private BukkitRunnable callerTask;

    public DelayedRepeatingTask(AresonDeathSwap aresonDeathSwap, int everySeconds, Runnable taskToRepeat, Optional<String> countDownMessage, List<Player> playersToNotify) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.everySeconds = everySeconds;
        this.taskToRepeat = taskToRepeat;
        this.playersToNotify = playersToNotify;
        this.isRunning = false;
        this.countDownMessage = countDownMessage;
        currentTimeRemaining = 0;
        initCallerTask();
    }

    private void initCallerTask() {
        callerTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (isRunning) {
                        if (!callerTask.isCancelled()) {
                            if (currentTimeRemaining <= 0) {
                                callTask();
                                currentTimeRemaining = everySeconds;
                            } else {
                                if (currentTimeRemaining <= 10) {
                                    final List<Player> playersClone = new ArrayList<>(playersToNotify);
                                    countDownMessage.ifPresent(s ->
                                            playersClone.parallelStream().forEach(player -> {
                                                        if (player != null) {
                                                            player.sendMessage(s.replaceAll("%seconds%", currentTimeRemaining + ""));
                                                            aresonDeathSwap.sounds.tick(player);
                                                        }
                                                    }
                                            )
                                    );
                                }
                                currentTimeRemaining--;
                            }
                        } else {
                            stopRepeating();
                        }
                    } else {
                        stopRepeating();
                    }
                } catch (Exception e) {
                    System.out.println("RepeatingTask error :");
                    e.printStackTrace(System.out);
                }
            }
        };
    }

    public void setEverySeconds(int everySeconds) {
        this.everySeconds = everySeconds;
    }

    public synchronized void startRepeating() {
        if (!isRunning) {
            isRunning = true;
            currentTimeRemaining = everySeconds;
            callerTask.runTaskTimer(aresonDeathSwap, 0L, 20L);
            aresonDeathSwap.getLogger().info("Started repeatingTask taskId " + callerTask.getTaskId());
            aresonDeathSwap.getLogger().warning("Tasks: " + aresonDeathSwap.getServer().getScheduler().getPendingTasks().stream().filter(task -> task.getOwner().equals(aresonDeathSwap)).map(BukkitTask::getTaskId).collect(Collectors.toList()));
        }
    }

    public synchronized void stopRepeating() {
        if (isRunning) {
            isRunning = false;
            aresonDeathSwap.getLogger().info("Stopping repeatingTask taskId " + callerTask.getTaskId());
            callerTask.cancel();
            initCallerTask();
        }
    }

    private synchronized void callTask() {
        aresonDeathSwap.getServer().getScheduler().runTask(aresonDeathSwap, taskToRepeat);
        aresonDeathSwap.getLogger().info("Called internal repeatingTask of caller " + callerTask.getTaskId());
    }

    public boolean isRunning() {
        return isRunning;
    }
}
