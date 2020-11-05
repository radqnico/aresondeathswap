package me.dewoji.deathswap.utils;

import me.dewoji.deathswap.DeathSwap;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TimerCountdown {

    private ArrayList<Player> countdownPlayers;
    private String message;
    private int countdownTime;
    private int currentValue;

    private Player firstPlayer;
    private Player secondPlayer;
    private Location firstPLocation;
    private Location secondPLocation;

    private int taskId;
    private boolean isRunning;
    private boolean theGameStarted;
    private Runnable stopTaskOk;
    private Runnable stopError;

    public TimerCountdown(ArrayList<Player> countdownPlayers, int countdownTime, boolean theGameStarted, Runnable stopTaskOk, String message) {
        this.countdownPlayers = countdownPlayers;
        this.countdownTime = countdownTime;
        this.currentValue = 0;
        this.taskId = 0;
        this.isRunning = false;
        this.stopTaskOk = stopTaskOk;
        this.message = message;
        this.theGameStarted = theGameStarted;
        this.stopError = () -> {
            for (Player p : this.countdownPlayers) {
                p.sendMessage("Non ci sono abbastanza player per startare, annullamento");
            }
        };
    }

    public void start() {
        isRunning = true;
        currentValue = countdownTime;
        taskId = DeathSwap.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(DeathSwap.getInstance(), () -> {
                    if (!theGameStarted) {
                        if(countdownPlayers.size() == 2) {
                            if (currentValue == 0) {
                                theGameStarted = true;
                            }
                            if (currentValue <= 10 || currentValue == 60) {
                                for (Player p : countdownPlayers) {
                                    p.sendMessage(message + " " + currentValue);
                                }
                            }

                            countdownPlayers = DeathSwap.getCountdownPlayers();
                            currentValue--;
                        } else {
                            stop(false);
                            return;
                        }
                    }
                    currentValue = countdownTime;
                    if (theGameStarted) {
                        if (currentValue == 0) {
                            if (countdownPlayers.size() == 2) {
                                currentValue = countdownTime;
                                firstPlayer = countdownPlayers.get(0);
                                secondPlayer = countdownPlayers.get(1);

                                firstPLocation = firstPlayer.getLocation();
                                secondPLocation = secondPlayer.getLocation();

                                firstPlayer.teleport(secondPLocation);
                                secondPlayer.teleport(firstPLocation);
                            }
                        }
                        if (currentValue <= 10 || currentValue == 60) {
                            for (Player p : countdownPlayers) {
                                p.sendMessage(message + " " + currentValue);
                            }
                        }
                        if (countdownPlayers.size() < 2) {
                            stop(true);
                            return;
                        }
                    }
                },
                0, 20);
    }

    public void stop(boolean ok) {
        isRunning = false;
        DeathSwap.getInstance().getServer().getScheduler().cancelTask(taskId);
        if (ok) {
            DeathSwap.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(DeathSwap.getInstance(), stopTaskOk, 0);
        } else {
            DeathSwap.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(DeathSwap.getInstance(), stopError, 0);
        }
    }
}
