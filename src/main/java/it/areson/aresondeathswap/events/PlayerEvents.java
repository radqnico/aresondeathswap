package it.areson.aresondeathswap.events;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.handlers.GameHandler;
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
import java.util.HashSet;
import java.util.Optional;

public class PlayerEvents implements Listener {

    private AresonDeathSwap aresonDeathSwap;

    public PlayerEvents(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;
    }

    private static AresonDeathSwap instance = AresonDeathSwap.getInstance();
    private Countdown preGameCountdown;
    private GameHandler gameHandler;
    private PlayerHolder playerHolder = new PlayerHolder(instance);

    private ArrayList<Player> alivePlayers = AresonDeathSwap.getAlivePlayers();
    private ArrayList<Player> lobbyPlayers = AresonDeathSwap.getLobbyPlayers();
    private ArrayList<Player> deadPlayers = AresonDeathSwap.getDeadPlayers();

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        aresonDeathSwap.teleportToLobbySpawn(event.getPlayer());

        Optional<String> firstFreeArena = aresonDeathSwap.getFirstFreeArena();

        if (firstFreeArena.isPresent()) {
            String arenaName = firstFreeArena.get();
            HashSet<Player> arenaPlayers = aresonDeathSwap.arenasPlayers.get(arenaName);

            if (arenaPlayers != null) {
                arenaPlayers.add(event.getPlayer());


                //Countdown should start
                Countdown countdown = aresonDeathSwap.arenasCountdowns.get(arenaName);

                if (countdown != null) {
                    if (arenaPlayers.size() >= aresonDeathSwap.MIN_PLAYERS) {
                        if (!countdown.isRunning()) {
                            countdown.start();
                            //TODO Messaggio
                        }
                    }
                } else {
                    //Inconsistenza
                }
            } else {
                //Arena non trovata
            }
        } else {
            aresonDeathSwap.waitingPlayers.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        aresonDeathSwap.waitingPlayers.remove(player);
        aresonDeathSwap.arenasPlayers.forEach((arenaName, arenaPlayers) -> {
            if(arenaPlayers.contains(player)) {
                arenaPlayers.remove(player);
                Countdown countdown = aresonDeathSwap.arenasCountdowns.get(arenaName);
                if(countdown != null) {
                    if(arenaPlayers.size() < aresonDeathSwap.MIN_PLAYERS) {
                        countdown.interrupt();
                    }
                } else {
                    //TODO inconsistenza
                }
            }

        });

//        if (gameHandler.isRunning()) {
//            if (instance.getServer().getOnlinePlayers().size() < 2) {
//                gameHandler.stop();
//            }
//        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity().getPlayer();
        player.setGameMode(GameMode.SPECTATOR);
        if (alivePlayers.contains(player)) {
//            playerHolder.deathPlayerMover(player, AresonDeathSwap.getAlivePlayers(), AresonDeathSwap.getDeadPlayers());
        }
        if (gameHandler.isRunning()) {
            if (instance.getServer().getOnlinePlayers().size() < 2) {
                gameHandler.stop();
            }
        }
    }

}
