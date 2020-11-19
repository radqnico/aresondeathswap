package it.areson.aresondeathswap.events;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.api.PlayerEndGameEvent;
import it.areson.aresondeathswap.api.PlayerLoseEvent;
import it.areson.aresondeathswap.api.PlayerStartGameEvent;
import org.bukkit.GameMode;
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
        aresonDeathSwap.titles.sendLongTitle(event.getPlayer(), "join");
        aresonDeathSwap.restorePlayerState(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        aresonDeathSwap.removePlayerFromArenas(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        aresonDeathSwap.effects.deathStrike(player);
        player.setGameMode(GameMode.SPECTATOR);
        aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(
                aresonDeathSwap,
                () -> {
                    aresonDeathSwap.removePlayerFromArenas(player);
                    aresonDeathSwap.sounds.loser(player);
                    aresonDeathSwap.titles.sendLongTitle(player, "lose");
                    aresonDeathSwap.eventCall.callPlayerEndGame(player);
                },
                4
        );
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
