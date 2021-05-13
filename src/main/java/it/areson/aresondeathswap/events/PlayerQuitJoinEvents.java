package it.areson.aresondeathswap.events;

import it.areson.aresoncore.events.GeneralEventListener;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.player.DeathswapPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

public class PlayerQuitJoinEvents extends GeneralEventListener {

    private final AresonDeathSwap plugin;
    private HashMap<String, LocalDateTime> joinTimes;

    public PlayerQuitJoinEvents(AresonDeathSwap plugin) {
        super(plugin);
        this.plugin = plugin;
        this.joinTimes = new HashMap<>();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        DeathswapPlayer deathswapPlayer = plugin.getDeathswapPlayerManager().addDeathswapPlayer(event.getPlayer());
        joinTimes.put(deathswapPlayer.getNickName(), LocalDateTime.now());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DeathswapPlayerManager deathswapPlayerManager = plugin.getDeathswapPlayerManager();

        DeathswapPlayer deathswapPlayer = deathswapPlayerManager.getDeathswapPlayer(player);
        long secondsPlayed = Duration.between(joinTimes.get(player.getName()), LocalDateTime.now()).getSeconds();
        deathswapPlayer.setSecondsPlayed(deathswapPlayer.getSecondsPlayed() + secondsPlayed);

        deathswapPlayerManager.saveDeathswapPlayer(player);
        deathswapPlayerManager.removeDeathswapPlayer(player);
    }
}
