package it.areson.aresondeathswap.events;

import it.areson.aresondeathswap.Arena;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.utils.Countdown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Optional;

public class PlayerEvents implements Listener {

    private AresonDeathSwap aresonDeathSwap;
    private Countdown preGameCountdown;

    public PlayerEvents(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        aresonDeathSwap.teleportToLobbySpawn(event.getPlayer());

        Optional<Arena> firstFreeArena = aresonDeathSwap.getFirstFreeArena();

        if (firstFreeArena.isPresent()) {
            Arena arena = firstFreeArena.get();
            arena.addPlayer(event.getPlayer());
        } else {
            aresonDeathSwap.waitingPlayers.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        aresonDeathSwap.waitingPlayers.remove(player);
        aresonDeathSwap.arenas.forEach((arenaName, arena) -> {
            arena.removePlayer(player);
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
//        Player player = e.getEntity().getPlayer();
//        player.setGameMode(GameMode.SPECTATOR);
//        if (alivePlayers.contains(player)) {
////            playerHolder.deathPlayerMover(player, AresonDeathSwap.getAlivePlayers(), AresonDeathSwap.getDeadPlayers());
//        }
//        if (gameHandler.isRunning()) {
//            if (instance.getServer().getOnlinePlayers().size() < 2) {
//                gameHandler.stop();
//            }
//        }
    }

}
