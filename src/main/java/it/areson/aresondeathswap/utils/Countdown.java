package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.Arena;
import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Countdown {

    private final AresonDeathSwap aresonDeathSwap;
    private final BukkitRunnable taskEnded;
    private final BukkitRunnable taskInterrupted;
    private BukkitRunnable taskMain;
    private final int interruptDelaySeconds;
    private final int timeBeforeShouting;
    private final String shoutingMessage;
    private final Arena arena;
    private final String startingMessage;
    private int countdownTime;
    private int currentValue;
    private boolean isRunning;

    public Countdown(AresonDeathSwap plugin, int countdownTime, BukkitRunnable taskEnded, BukkitRunnable taskInterrupted, int interruptDelaySeconds, int timeBeforeShouting, String shoutingMessage, Arena arena, String startingMessage) {
        aresonDeathSwap = plugin;
        this.countdownTime = countdownTime;
        this.taskEnded = taskEnded;
        this.taskInterrupted = taskInterrupted;
        this.interruptDelaySeconds = interruptDelaySeconds;
        this.timeBeforeShouting = timeBeforeShouting;
        this.shoutingMessage = ChatColor.translateAlternateColorCodes('&', shoutingMessage);
        isRunning = false;
        currentValue = 0;
        this.arena = arena;
        this.startingMessage = startingMessage;
        initTask();
    }

    private void initTask() {
        taskMain = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (isRunning) {
                        if (currentValue <= 0) {
                            end();
                        } else {
                            if (currentValue <= timeBeforeShouting) {
                                sendMessages(shoutingMessage.replaceAll("%seconds%", currentValue + ""));
                            }

                            currentValue--;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Countdown error :");
                    e.printStackTrace(System.out);
                }
            }
        };
    }

    public synchronized void start() {
        if (!isRunning) {
            isRunning = true;
            currentValue = countdownTime;
            sendMessages(startingMessage.replaceAll("%seconds%", countdownTime + ""));
            taskMain.runTaskTimer(aresonDeathSwap, 0L, 20L);
            aresonDeathSwap.getLogger().info("Started countdown taskId " + taskMain.getTaskId());
            aresonDeathSwap.getLogger().info("Tasks: " + aresonDeathSwap.getServer().getScheduler().getPendingTasks().stream().filter(task -> task.getOwner().equals(aresonDeathSwap)).map(BukkitTask::getTaskId).collect(Collectors.toList()));
        }
    }

    private synchronized void sendMessages(String message) {
        List<Player> playersClone = new ArrayList<>(arena.getPlayers());
        playersClone.forEach(player -> {
            if (player != null) {
                player.sendMessage(message);
                aresonDeathSwap.sounds.tick(player);
            }
        });
    }

    private synchronized void end() {
        isRunning = false;
        aresonDeathSwap.getLogger().info("Ending countdown taskId " + taskMain.getTaskId());
        taskMain.cancel();
        aresonDeathSwap.getServer().getScheduler().cancelTask(taskMain.getTaskId());
        initTask();
        taskEnded.runTask(aresonDeathSwap);
    }

    public synchronized void interrupt() {
        if (isRunning) {
            isRunning = false;
            aresonDeathSwap.getLogger().info("Interrupting countdown taskId " + taskMain.getTaskId());
            taskMain.cancel();
            aresonDeathSwap.getServer().getScheduler().cancelTask(taskMain.getTaskId());
            initTask();
            taskInterrupted.runTaskLater(aresonDeathSwap, interruptDelaySeconds);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setTime(int timeToSet) {
        countdownTime = timeToSet;
    }
}
