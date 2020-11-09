package me.dewoji.deathswap.utils;

import me.dewoji.deathswap.DeathSwap;
import org.bukkit.plugin.java.JavaPlugin;

public class Countdown {

    private JavaPlugin instance;

    private int countdownTime;
    private int currentValue;

    private int taskId;
    private boolean isRunning;
    private Runnable stopTaskFinish;
    private Runnable stopTaskInterrupted;
    private int timeBeforeShouting;

    private String shoutingMessage;

    public Countdown(JavaPlugin instance, int countdownTime, Runnable stopTaskFinish, Runnable stopTaskInterrupted, int timeBeforeShouting, String shoutingMessage) {
        this.instance = instance;
        this.countdownTime = countdownTime;
        this.stopTaskFinish = stopTaskFinish;
        this.stopTaskInterrupted = stopTaskInterrupted;
        this.timeBeforeShouting = timeBeforeShouting;
        this.shoutingMessage = shoutingMessage;
        isRunning = false;
        taskId = 0;
        currentValue = 0;
    }

    public void start() {
        isRunning = true;
        currentValue = countdownTime;
        taskId = instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {
            if (currentValue == 0) {
                stopFinish();
            }
            if (currentValue <= timeBeforeShouting) {
                instance.getServer().broadcastMessage(shoutingMessage.replaceAll("%secs%", currentValue + ""));
            }
            currentValue--;
        }, 0, 20);
    }

    private void stopFinish() {
        isRunning = false;
        instance.getServer().getScheduler().cancelTask(taskId);
        instance.getServer().getScheduler().scheduleSyncDelayedTask(DeathSwap.getInstance(), stopTaskFinish, 0);

    }

    public void stopInterrupt() {
        isRunning = false;
        instance.getServer().getScheduler().cancelTask(taskId);
        instance.getServer().getScheduler().scheduleSyncDelayedTask(DeathSwap.getInstance(), stopTaskInterrupted, 0);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void resetCountdown(int timeToSet) {
        countdownTime = timeToSet;
    }
}
