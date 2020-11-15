package it.areson.aresondeathswap.events;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {

    private final AresonDeathSwap aresonDeathSwap;

    public PlayerEvents(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        aresonDeathSwap.teleportToLobbySpawn(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        aresonDeathSwap.arenas.forEach((arenaName, arena) -> arena.removePlayer(player));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        aresonDeathSwap.arenas.forEach((arenaName, arena) -> arena.removePlayer(player));
    }

}
