package me.dewoji.deathswap.handlers;

import me.dewoji.deathswap.DeathSwap;
import me.dewoji.deathswap.utils.Countdown;
import me.dewoji.deathswap.utils.PlayerHolder;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class GameHandler {

    private JavaPlugin instance;
    private PlayerHolder players;
    private Countdown gameCountdown;

    private String shoutMessage;
    private int min;
    private int max;

    public GameHandler(JavaPlugin instance, int minimumTime, int maximumTime, String shoutMessage) {
        this.instance = instance;
        this.players = new PlayerHolder(instance);
        this.min = minimumTime;
        this.max = maximumTime;
        this.shoutMessage = shoutMessage;
    }

    public void startGame() {
        String victoryMessage = ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("messaggio_vittoria"));

        gameCountdown = new Countdown(instance, randomTeleportTime(), () -> {
            if (instance.getServer().getOnlinePlayers().size() >= 2) {
                players.playerRotate();
                gameCountdown.resetCountdown(randomTeleportTime());
                gameCountdown.start();
            } else {
                instance.getServer().broadcastMessage(victoryMessage);
                players.playerMassMover(DeathSwap.getAlivePlayers(), DeathSwap.getLobbyPlayers());
            }
        }, () -> {
            instance.getServer().broadcastMessage(victoryMessage);
            players.playerMassMover(DeathSwap.getAlivePlayers(), DeathSwap.getLobbyPlayers());
            players.playerMassMover(DeathSwap.getDeadPlayers(), DeathSwap.getLobbyPlayers());
        }, 10, shoutMessage);

        gameCountdown.start();
    }

    private int randomTeleportTime() {
        int randomNumber = (int) (Math.random() * (max - min + 1) + min);
        System.out.println(randomNumber);
        return randomNumber;
    }

    public boolean isRunning() {
        return gameCountdown.isRunning();
    }

    public void stop() {
        if(isRunning()) {
            gameCountdown.stopInterrupt();
        }
    }
}
