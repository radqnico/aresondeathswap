package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;

public class Countdown {

    private AresonDeathSwap aresonDeathSwap;

    private int countdownTime;
    private int currentValue;

    private int taskId;
    private boolean isRunning;
    private final Runnable taskEnded;
    private final Runnable taskInterrupted;
    private final int timeBeforeShouting;
    private final String shoutingMessage;
    private final String arenaName;
    private final String startingMessage;

    public Countdown(AresonDeathSwap plugin, int countdownTime, Runnable taskEnded, Runnable taskInterrupted, int timeBeforeShouting, String shoutingMessage, String arenaName, String startingMessage) {
        aresonDeathSwap = plugin;
        this.countdownTime = countdownTime;
        this.taskEnded = taskEnded;
        this.taskInterrupted = taskInterrupted;
        this.timeBeforeShouting = timeBeforeShouting;
        this.shoutingMessage = ChatColor.translateAlternateColorCodes('&', shoutingMessage);
        isRunning = false;
        taskId = 0;
        currentValue = 0;
        this.arenaName = arenaName;
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
                }
                if (currentValue <= timeBeforeShouting) {
                    sendMessages(shoutingMessage.replaceAll("%seconds%", currentValue + ""));
                }
                currentValue--;
            }, 0, 20);
        }
    }

    private void sendMessages(String message) {
        ArrayList<Player> players = aresonDeathSwap.arenasPlayers.get(arenaName);
        if (players != null) {
            players.forEach(player -> player.sendMessage(message));
        }
    }

    private void end() {
        isRunning = false;
        aresonDeathSwap.getServer().getScheduler().cancelTask(taskId);
        aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(AresonDeathSwap.getInstance(), taskEnded, 0);

    }

    public void interrupt() {
        if (isRunning) {
            isRunning = false;
            aresonDeathSwap.getServer().getScheduler().cancelTask(taskId);
            aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(AresonDeathSwap.getInstance(), taskInterrupted, 0);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void resetCountdown(int timeToSet) {
        countdownTime = timeToSet;
    }
}
