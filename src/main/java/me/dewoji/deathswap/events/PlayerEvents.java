package me.dewoji.deathswap.events;

import me.dewoji.deathswap.DeathSwap;
import me.dewoji.deathswap.utils.Countdown;
import me.dewoji.deathswap.handlers.GameHandler;
import me.dewoji.deathswap.utils.PlayerHolder;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class PlayerEvents implements Listener {

    private static DeathSwap instance = DeathSwap.getInstance();
    private Countdown preGameCountdown;
    private GameHandler gameHandler;
    private PlayerHolder playerHolder = new PlayerHolder(instance);

    private ArrayList<Player> alivePlayers = DeathSwap.getAlivePlayers();
    private ArrayList<Player> lobbyPlayers = DeathSwap.getLobbyPlayers();
    private ArrayList<Player> deadPlayers = DeathSwap.getDeadPlayers();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        alivePlayers = DeathSwap.getAlivePlayers();
        lobbyPlayers = DeathSwap.getLobbyPlayers();
        deadPlayers = DeathSwap.getDeadPlayers();

        if(!lobbyPlayers.contains(e.getPlayer())) {
            lobbyPlayers.add(e.getPlayer());
            DeathSwap.setLobbyPlayers(lobbyPlayers);
        }
        if (lobbyPlayers.size() >= 2) {
            preGameCountdown = (new Countdown(instance, 20, () -> {
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
        Player player = e.getPlayer();
        alivePlayers = DeathSwap.getAlivePlayers();
        lobbyPlayers = DeathSwap.getLobbyPlayers();
        deadPlayers = DeathSwap.getDeadPlayers();

        if(alivePlayers.contains(player)) {
            alivePlayers.remove(e.getPlayer());
            DeathSwap.setAlivePlayers(alivePlayers);
        } else if(lobbyPlayers.contains(player)) {
            lobbyPlayers.remove(player);
            DeathSwap.setDeadPlayers(lobbyPlayers);
        } else if(deadPlayers.contains(player)) {
            deadPlayers.remove(player);
            DeathSwap.setDeadPlayers(deadPlayers);
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
        if(alivePlayers.contains(player)) {
            playerHolder.deathPlayerMover(player, DeathSwap.getAlivePlayers(), DeathSwap.getDeadPlayers());
        }
        if (gameHandler.isRunning()) {
            if(instance.getServer().getOnlinePlayers().size() < 2) {
                gameHandler.stop();
            }
        }
    }

    @EventHandler
    public void onPlayerJoinWorld(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        if(e.getFrom().getName().equalsIgnoreCase("world") && player.getWorld().getName().equalsIgnoreCase("gameWorld")) {

        }

    }
}
