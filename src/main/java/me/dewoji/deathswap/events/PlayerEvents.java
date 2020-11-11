package me.dewoji.deathswap.events;

import me.dewoji.deathswap.DeathSwap;
import me.dewoji.deathswap.utils.Countdown;
import me.dewoji.deathswap.handlers.GameHandler;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class PlayerEvents implements Listener {

    private static DeathSwap instance = DeathSwap.getInstance();
    private Countdown preGameCountdown;
    private GameHandler gameHandler;

    private ArrayList<Player> alivePlayers = DeathSwap.getAlivePlayers();


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if(!alivePlayers.contains(e.getPlayer())) {
            alivePlayers.add(e.getPlayer());
            DeathSwap.setAlivePlayers(alivePlayers);
        }
        if (alivePlayers.size() >= 2) {
            preGameCountdown = (new Countdown(instance, 11, () -> {
                gameHandler = new GameHandler(instance, 13, 30, instance.getConfig().getString("messaggio_avviso_in_partita"));
                gameHandler.startGame();
            }, () -> {
                instance.getServer().broadcastMessage(instance.getConfig().getString("messaggio_non_abbastanza_player"));
            },
                    10, instance.getConfig().getString("messaggio_pre_partita")));

            if (!preGameCountdown.isRunning()) {
                preGameCountdown.start();
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if(!alivePlayers.contains(e.getPlayer())) {
            alivePlayers.remove(e.getPlayer());
            DeathSwap.setAlivePlayers(alivePlayers);
        }
        if (preGameCountdown.isRunning()) {
            if (alivePlayers.size() < 2) {
                preGameCountdown.stopInterrupt();
            }
        }
        if(gameHandler.isRunning()) {
            if (instance.getServer().getOnlinePlayers().size() < 2) {
                gameHandler.stop();
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity().getPlayer();
        player.setGameMode(GameMode.SPECTATOR);
        if(!alivePlayers.contains(player)) {
            alivePlayers.remove(player);
            DeathSwap.setAlivePlayers(alivePlayers);
        }
        if (gameHandler.isRunning()) {
            if(instance.getServer().getOnlinePlayers().size() < 2) {
                gameHandler.stop();
            }
        }
    }

}
