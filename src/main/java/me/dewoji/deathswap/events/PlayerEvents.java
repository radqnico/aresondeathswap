package me.dewoji.deathswap.events;

import me.dewoji.deathswap.DeathSwap;
import me.dewoji.deathswap.utils.TimerCountdown;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class PlayerEvents implements Listener {

    private static DeathSwap instance = DeathSwap.getInstance();
    private static ArrayList<Player> countdownPlayers = DeathSwap.getCountdownPlayers();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage(String.valueOf(countdownPlayers.size()));
        if (countdownPlayers.size() < 2) {
            if (!countdownPlayers.contains(e.getPlayer())) {
                countdownPlayers.add(e.getPlayer());
                DeathSwap.setCountdownPlayers(countdownPlayers);
            }
        } else if (countdownPlayers.size() == 2) {
            String message = ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("messaggio_pre_partita"));
            (new TimerCountdown(countdownPlayers, 20, () -> {
                for (Player p : countdownPlayers) {
                    p.sendMessage("Hai vinto");
                }
            }, message)).start();
        } else {
            e.getPlayer().kickPlayer("Lobby piena");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (countdownPlayers.contains(e.getPlayer())) {
            countdownPlayers.remove(e.getPlayer());
            DeathSwap.setCountdownPlayers(countdownPlayers);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if(countdownPlayers.contains(e.getEntity().getPlayer())) {
            countdownPlayers.remove(e.getEntity().getPlayer());
            DeathSwap.setCountdownPlayers(countdownPlayers);
        }
    }

}
