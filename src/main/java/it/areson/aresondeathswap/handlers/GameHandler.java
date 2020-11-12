package it.areson.aresondeathswap.handlers;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.utils.Countdown;
import it.areson.aresondeathswap.utils.PlayerHolder;
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
        players.playerMassMover(AresonDeathSwap.getLobbyPlayers(), AresonDeathSwap.getAlivePlayers());
        gameCountdown = new Countdown(instance, randomTeleportTime(), () -> {
            if (instance.getServer().getOnlinePlayers().size() >= 2) {
                players.playerRotate();
                gameCountdown.resetCountdown(randomTeleportTime());
                gameCountdown.start();
            } else {
                instance.getServer().broadcastMessage(victoryMessage);
                players.playerMassMover(AresonDeathSwap.getAlivePlayers(), AresonDeathSwap.getLobbyPlayers());
                players.playerMassMover(AresonDeathSwap.getDeadPlayers(), AresonDeathSwap.getLobbyPlayers());
            }
        }, () -> {
            instance.getServer().broadcastMessage(victoryMessage);
            players.playerMassMover(AresonDeathSwap.getAlivePlayers(), AresonDeathSwap.getLobbyPlayers());
            players.playerMassMover(AresonDeathSwap.getDeadPlayers(), AresonDeathSwap.getLobbyPlayers());
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