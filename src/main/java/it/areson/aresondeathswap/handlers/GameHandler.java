package it.areson.aresondeathswap.handlers;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.utils.Countdown;
import it.areson.aresondeathswap.utils.PlayerHolder;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class GameHandler {

    private final AresonDeathSwap aresonDeathSwap;
    private final PlayerHolder players;
    private Countdown gameCountdown;
    private final String arenaName;

    private String shoutMessage;
    private int min;
    private int max;

    public GameHandler(AresonDeathSwap plugin, int minimumTime, int maximumTime, String shoutMessage, String arenaName) {
        aresonDeathSwap = plugin;
        this.players = new PlayerHolder(plugin);
        this.min = minimumTime;
        this.max = maximumTime;
        this.shoutMessage = shoutMessage;
        this.arenaName = arenaName;
    }

    public void startGame() {
        World world = aresonDeathSwap.getServer().getWorld(arenaName);
        if(world != null) {
            HashSet<Player> players = aresonDeathSwap.arenasPlayers.get(arenaName);
            if(players != null) {
                players.forEach(player -> player.teleport(world.getSpawnLocation()));
            } else {
                //TODO Incosistenza
            }
        } else {
            //TODO inconsistenza
        }


//        String victoryMessage = ChatColor.translateAlternateColorCodes('&', aresonDeathSwap.getConfig().getString("messaggio_vittoria"));
//        gameCountdown = new Countdown(aresonDeathSwap, randomTeleportTime(), () -> {
//            if (aresonDeathSwap.getServer().getOnlinePlayers().size() >= 2) {
//                players.playerRotate();
//                gameCountdown.resetCountdown(randomTeleportTime());
//                gameCountdown.start();
//            } else {
//                aresonDeathSwap.getServer().broadcastMessage(victoryMessage);
//                players.playerMassMover(AresonDeathSwap.getAlivePlayers(), AresonDeathSwap.getLobbyPlayers());
//                players.playerMassMover(AresonDeathSwap.getDeadPlayers(), AresonDeathSwap.getLobbyPlayers());
//            }
//        }, () -> {
//            aresonDeathSwap.getServer().broadcastMessage(victoryMessage);
//            players.playerMassMover(AresonDeathSwap.getAlivePlayers(), AresonDeathSwap.getLobbyPlayers());
//            players.playerMassMover(AresonDeathSwap.getDeadPlayers(), AresonDeathSwap.getLobbyPlayers());
//        }, 10, shoutMessage);
//
//        gameCountdown.start();
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
        if (isRunning()) {
            gameCountdown.interrupt();
        }
    }
}
