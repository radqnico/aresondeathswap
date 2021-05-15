package it.areson.aresondeathswap.events;

import it.areson.aresoncore.events.GeneralEventListener;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.Arena;
import it.areson.aresondeathswap.arena.ArenaManager;
import it.areson.aresondeathswap.loot.LootConfigReader;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.player.DeathswapPlayerManager;
import it.areson.aresondeathswap.utils.Message;
import it.areson.aresondeathswap.utils.PlayerUtils;
import it.areson.aresondeathswap.utils.SoundManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

public class PlayerEvents extends GeneralEventListener {

    private final AresonDeathSwap plugin;
    private HashMap<String, LocalDateTime> joinTimes;

    public PlayerEvents(AresonDeathSwap plugin) {
        super(plugin);
        this.plugin = plugin;
        this.joinTimes = new HashMap<>();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        DeathswapPlayer deathswapPlayer = plugin.getDeathswapPlayerManager().addDeathswapPlayer(event.getPlayer());
        joinTimes.put(deathswapPlayer.getNickName(), LocalDateTime.now());
        Player player = event.getPlayer();
        PlayerUtils.resetPlayerStatus(player);
        player.teleport(plugin.getLobbySpawn());

        PlayerUtils.sendLongTitle(player, Message.TITLE_JOIN, Message.TITLE_JOIN_SUB);
        SoundManager.joinServer(player.getLocation());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DeathswapPlayerManager deathswapPlayerManager = plugin.getDeathswapPlayerManager();

        DeathswapPlayer deathswapPlayer = deathswapPlayerManager.getDeathswapPlayer(player);
        long secondsPlayed = Duration.between(joinTimes.get(player.getName()), LocalDateTime.now()).getSeconds();
        deathswapPlayer.setSecondsPlayed(deathswapPlayer.getSecondsPlayed() + secondsPlayed);

        ArenaManager arenaManager = plugin.getArenaManager();
        Optional<Arena> arenaOfPlayer = arenaManager.getArenaOfPlayer(deathswapPlayer);
        arenaOfPlayer.ifPresent(arena -> arena.removePlayer(deathswapPlayer, true));

        deathswapPlayerManager.saveDeathswapPlayer(player);
        deathswapPlayerManager.removeDeathswapPlayer(player);
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getPlayer().getWorld().getName().equals("world")) {
            return;
        }
        AresonDeathSwap aresonDeathSwap = AresonDeathSwap.instance;
        if (event.getInventory().getHolder() instanceof Chest || event.getInventory().getHolder() instanceof DoubleChest) {
            LootConfigReader lootChest = aresonDeathSwap.loots;
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
                    SoundManager.openChest(loc);
                    aresonDeathSwap.loots.removeLootChest(loc);
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
}
