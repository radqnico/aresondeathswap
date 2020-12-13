package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.Arena;
import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CDTaskSeries {

    private final AresonDeathSwap aresonDeathSwap;
    private final Runnable taskEnded;
    private final Runnable taskInterrupted;
    private final int timeBeforeShouting;
    private final String shoutingMessage;
    private final Arena arena;
    private final String startingMessage;
    private int countdownTime;
    private boolean isRunning;
    private final HashSet<Integer> taskIDs;

    public CDTaskSeries(AresonDeathSwap plugin, int countdownTime, Runnable taskEnded, Runnable taskInterrupted, int timeBeforeShouting, String shoutingMessage, Arena arena, String startingMessage) {
        aresonDeathSwap = plugin;
        this.countdownTime = countdownTime;
        this.taskEnded = taskEnded;
        this.taskInterrupted = taskInterrupted;
        this.timeBeforeShouting = timeBeforeShouting;
        this.shoutingMessage = ChatColor.translateAlternateColorCodes('&', shoutingMessage);
        isRunning = false;
        this.arena = arena;
        this.startingMessage = startingMessage;
        taskIDs = new HashSet<>();
    }

    private synchronized void sendMessages(String message) {
        if (arena != null) {
            List<Player> playersClone = new ArrayList<>(arena.getPlayers());
            playersClone.forEach(player -> {
                if (player != null) {
                    player.sendMessage(message);
                    aresonDeathSwap.sounds.tick(player);
                }
            });
        }
    }

    public synchronized void start() {
        if (!isRunning) {
            isRunning = true;
            final long offset = countdownTime - timeBeforeShouting;
            int counter = 0;
            for (long i = timeBeforeShouting; i >= 0; i--) {
                final long finalI = i;
                taskIDs.add(aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(
                        aresonDeathSwap,
                        () -> {
                            if (finalI <= 0) {
                                end();
                            } else {
                                sendMessages(shoutingMessage.replaceAll("%seconds%", finalI + ""));
                            }
                        },
                        20L * (counter + offset)
                ));
                counter++;
            }
            if (startingMessage != null) {
                sendMessages(startingMessage.replaceAll("%seconds%", countdownTime + ""));
            }
            if (arena != null) {
                aresonDeathSwap.getLogger().info("Started countdown as tasklist for '" + arena.getName() + "'");
            }
            aresonDeathSwap.getLogger().info("Tasks: " + aresonDeathSwap.getServer().getScheduler().getPendingTasks().stream().filter(task -> task.getOwner().equals(aresonDeathSwap)).map(BukkitTask::getTaskId).collect(Collectors.toList()));
        }
    }

    private void cancelAllTasks() {
        for (Integer id : taskIDs) {
            aresonDeathSwap.getServer().getScheduler().cancelTask(id);
        }
        taskIDs.clear();
    }

    private synchronized void end() {
        isRunning = false;
        if (arena != null) {
            aresonDeathSwap.getLogger().info("Ended countdown as tasklist for '" + arena.getName() + "'");
        }
        cancelAllTasks();
        aresonDeathSwap.getServer().getScheduler().runTask(aresonDeathSwap, taskEnded);
    }

    public synchronized void interrupt() {
        isRunning = false;
        if (arena != null) {
            aresonDeathSwap.getLogger().info("Interrupted countdown as tasklist for '" + arena.getName() + "'");
        }
        cancelAllTasks();
        aresonDeathSwap.getServer().getScheduler().runTask(aresonDeathSwap, taskInterrupted);
    }

    public void setCountdownTime(int countdownTime) {
        this.countdownTime = countdownTime;
    }
}
