package it.areson.aresondeathswap.events;

import it.areson.aresoncore.events.GeneralEventListener;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.Arena;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.utils.Message;
import it.areson.aresondeathswap.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class DeathEvents extends GeneralEventListener {

    private final AresonDeathSwap aresonDeathSwap;

    public DeathEvents(JavaPlugin javaPlugin, AresonDeathSwap aresonDeathSwap) {
        super(javaPlugin);
        this.aresonDeathSwap = aresonDeathSwap;
        registerEvents();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            double health = player.getHealth();
            if (health <= event.getDamage()) {
                event.setCancelled(true);
                DeathswapPlayer deathswapPlayer = aresonDeathSwap.getDeathswapPlayerManager().getDeathswapPlayer(player);
                Optional<Arena> arenaOfPlayer = aresonDeathSwap.getArenaManager().getArenaOfPlayer(deathswapPlayer);
                arenaOfPlayer.ifPresent(arena -> {
                    arena.removePlayer(deathswapPlayer, true);
                });
            }
        }
    }


}
