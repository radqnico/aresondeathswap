package it.areson.aresondeathswap.events;

import it.areson.aresondeathswap.handlers.GameHandler;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.utils.Countdown;
import it.areson.aresondeathswap.utils.PlayerHolder;
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

    private static AresonDeathSwap instance = AresonDeathSwap.getInstance();
    private Countdown preGameCountdown;
    private GameHandler gameHandler;
    private PlayerHolder playerHolder = new PlayerHolder(instance);

    private ArrayList<Player> alivePlayers = AresonDeathSwap.getAlivePlayers();
    private ArrayList<Player> lobbyPlayers = AresonDeathSwap.getLobbyPlayers();
    private ArrayList<Player> deadPlayers = AresonDeathSwap.getDeadPlayers();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        alivePlayers = AresonDeathSwap.getAlivePlayers();
        lobbyPlayers = AresonDeathSwap.getLobbyPlayers();
        deadPlayers = AresonDeathSwap.getDeadPlayers();

        if(!lobbyPlayers.contains(e.getPlayer())) {
            lobbyPlayers.add(e.getPlayer());
            AresonDeathSwap.setLobbyPlayers(lobbyPlayers);
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
        alivePlayers = AresonDeathSwap.getAlivePlayers();
        lobbyPlayers = AresonDeathSwap.getLobbyPlayers();
        deadPlayers = AresonDeathSwap.getDeadPlayers();

        if(alivePlayers.contains(player)) {
            alivePlayers.remove(e.getPlayer());
            AresonDeathSwap.setAlivePlayers(alivePlayers);
        } else if(lobbyPlayers.contains(player)) {
            lobbyPlayers.remove(player);
            AresonDeathSwap.setDeadPlayers(lobbyPlayers);
        } else if(deadPlayers.contains(player)) {
            deadPlayers.remove(player);
            AresonDeathSwap.setDeadPlayers(deadPlayers);
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
            playerHolder.deathPlayerMover(player, AresonDeathSwap.getAlivePlayers(), AresonDeathSwap.getDeadPlayers());
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
