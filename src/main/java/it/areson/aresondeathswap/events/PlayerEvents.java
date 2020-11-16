package it.areson.aresondeathswap.events;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class PlayerEvents implements Listener {

    private final AresonDeathSwap aresonDeathSwap;

    public PlayerEvents(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        aresonDeathSwap.teleportToLobbySpawn(event.getPlayer());
        aresonDeathSwap.sounds.joinServer(event.getPlayer().getWorld().getSpawnLocation());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        aresonDeathSwap.arenas.forEach((arenaName, arena) -> arena.removePlayer(player));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        aresonDeathSwap.sounds.loser(player);
        aresonDeathSwap.removePlayerFromArenas(player);
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        World playerWorld = event.getPlayer().getLocation().getWorld();

        if (playerWorld != null) {
            ArrayList<Player> players = new ArrayList<>(event.getRecipients());
            players.forEach(player -> {
                World targetWorld = player.getLocation().getWorld();
                if (targetWorld != null && !targetWorld.getName().equals(playerWorld.getName())) {
                    event.getRecipients().remove(player);
                }
            });
        }
    }

}
