package it.areson.aresondeathswap.events;

import it.areson.aresondeathswap.Arena;
import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class PlayerEvents implements Listener {

    private AresonDeathSwap aresonDeathSwap;

    public PlayerEvents(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        aresonDeathSwap.teleportToLobbySpawn(event.getPlayer());
        aresonDeathSwap.waitingPlayers.add(event.getPlayer());
        aresonDeathSwap.assignPlayersToArenaIfPossible();
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        aresonDeathSwap.waitingPlayers.remove(player);
        aresonDeathSwap.arenas.forEach((arenaName, arena) -> arena.removePlayer(player));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        aresonDeathSwap.arenas.forEach((arenaName, arena) -> arena.removePlayer(player));
        aresonDeathSwap.waitingPlayers.add(player);
    }

}
