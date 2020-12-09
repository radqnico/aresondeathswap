package it.areson.aresondeathswap.listeners;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.loadbalancer.LoadBalancer;
import it.areson.aresondeathswap.loot.LootConfigReader;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Optional;

public class PlayerEvents implements Listener {

    private final AresonDeathSwap aresonDeathSwap;

    public PlayerEvents(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;
    }

    @EventHandler
    public void tickStartEvent(ServerTickStartEvent event) {
        LoadBalancer.LAST_TICK_START_TIME = System.currentTimeMillis();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        aresonDeathSwap.teleportToLobbySpawn(event.getPlayer());
        aresonDeathSwap.sounds.joinServer(event.getPlayer().getWorld().getSpawnLocation());
        aresonDeathSwap.titles.sendLongTitle(event.getPlayer(), "join");
        aresonDeathSwap.restorePlayerState(event.getPlayer());
        for (int i = 0; i < 20; i++) {
            event.getPlayer().sendMessage(" ");
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        aresonDeathSwap.removePlayerFromArenas(player);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        aresonDeathSwap.effects.deathStrike(player);

        event.setDeathMessage(null);
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Optional<Location> lobbyLocation = aresonDeathSwap.getLobbyLocation();
        lobbyLocation.ifPresent(event::setRespawnLocation);

        aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(aresonDeathSwap, () -> {
            aresonDeathSwap.removePlayerFromArenas(event.getPlayer());
            aresonDeathSwap.sounds.loser(event.getPlayer());
            aresonDeathSwap.titles.sendLongTitle(event.getPlayer(), "lose");
        }, 5);
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

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Chest || event.getInventory().getHolder() instanceof DoubleChest) {
            LootConfigReader lootChest = aresonDeathSwap.loot;
            Location loc = event.getInventory().getLocation();
            if (loc != null) {
                if (lootChest.isLootChest(loc)) {
                    loc.add(new Vector(0.5, 1, 0.5));
                    // TODO SoundManager.chestOpen(loc);
                    if (event.getInventory().getHolder() instanceof Chest) {
                        lootChest.newLootChest((Chest) event.getInventory().getHolder());
                    } else {
                        lootChest.newLootChest((DoubleChest) event.getInventory().getHolder());
                    }
                    aresonDeathSwap.sounds.openChest(loc);
                    aresonDeathSwap.loot.removeLootChest(loc);
                    World world = loc.getWorld();
                    if (world != null) {
                        world.spawnParticle(Particle.TOTEM, loc, 30, 0, 0, 0, 0.35, null, true);
                    } else {
                        aresonDeathSwap.getLogger().warning("Errore nel mondo della Loot Chest.");
                    }
                    event.setCancelled(true);
                }
            } else {
                aresonDeathSwap.getLogger().warning("Errore nuella location della Loot Chest.");
            }

        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

}
