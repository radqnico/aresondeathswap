package me.dewoji.deathswap.events;

import me.dewoji.deathswap.DeathSwap;
import me.dewoji.deathswap.utils.Countdown;
import me.dewoji.deathswap.utils.GameHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {

    private static DeathSwap instance = DeathSwap.getInstance();
    private Countdown preGameCountdown;
    private GameHandler gameHandler;


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (instance.getServer().getOnlinePlayers().size() >= 2) {
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
        if (preGameCountdown.isRunning()) {
            if (instance.getServer().getOnlinePlayers().size() < 2) {
                preGameCountdown.stopInterrupt();
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (gameHandler.isRunning()) {
            e.getEntity().getPlayer().kickPlayer("Sei morto");
        }
    }

}
