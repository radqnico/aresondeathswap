package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.Arena;
import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.ChatColor;

public class Countdown {

    private final AresonDeathSwap aresonDeathSwap;

    private int countdownTime;
    private int currentValue;

    private int taskId;
    private boolean isRunning;
    private final Runnable taskEnded;
    private final Runnable taskInterrupted;
    private final int interruptDelaySeconds;
    private final int timeBeforeShouting;
    private final String shoutingMessage;
    private final Arena arena;
    private final String startingMessage;

    public Countdown(AresonDeathSwap plugin, int countdownTime, Runnable taskEnded, Runnable taskInterrupted, int interruptDelaySeconds, int timeBeforeShouting, String shoutingMessage, Arena arena, String startingMessage) {
        aresonDeathSwap = plugin;
        this.countdownTime = countdownTime;
        this.taskEnded = taskEnded;
        this.taskInterrupted = taskInterrupted;
        this.interruptDelaySeconds = interruptDelaySeconds;
        this.timeBeforeShouting = timeBeforeShouting;
        this.shoutingMessage = ChatColor.translateAlternateColorCodes('&', shoutingMessage);
        isRunning = false;
        taskId = 0;
        currentValue = 0;
        this.arena = arena;
        this.startingMessage = startingMessage;
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            currentValue = countdownTime;
            sendMessages(startingMessage.replaceAll("%seconds%", countdownTime + ""));

            taskId = aresonDeathSwap.getServer().getScheduler().scheduleSyncRepeatingTask(aresonDeathSwap, () -> {
                if (currentValue == 0) {
                    end();
                } else {
                    if (currentValue <= timeBeforeShouting) {
                        sendMessages(shoutingMessage.replaceAll("%seconds%", currentValue + ""));
                    }

                    currentValue--;
                }
            }, 0, 20);
        }
    }

    private void sendMessages(String message) {
        arena.getPlayers().forEach(player -> {
            player.sendMessage(message);
            aresonDeathSwap.sounds.tick(player);
        });
    }

    private void end() {
        isRunning = false;
        aresonDeathSwap.getServer().getScheduler().cancelTask(taskId);
        aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(aresonDeathSwap, taskEnded, 0);
    }

    public void interrupt() {
        if (isRunning) {
            isRunning = false;
            aresonDeathSwap.getServer().getScheduler().cancelTask(taskId);
            aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(aresonDeathSwap, taskInterrupted, interruptDelaySeconds * 20);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setTime(int timeToSet) {
        countdownTime = timeToSet;
    }
}
