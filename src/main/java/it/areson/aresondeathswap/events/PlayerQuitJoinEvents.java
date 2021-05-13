package it.areson.aresondeathswap.events;

import it.areson.aresoncore.events.GeneralEventListener;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.Arena;
import it.areson.aresondeathswap.arena.ArenaManager;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.player.DeathswapPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

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

        ArenaManager arenaManager = plugin.getArenaManager();
        Optional<Arena> arenaOfPlayer = arenaManager.getArenaOfPlayer(deathswapPlayer);
        arenaOfPlayer.ifPresent(arena -> arena.removePlayer(deathswapPlayer));

        deathswapPlayerManager.saveDeathswapPlayer(player);
        deathswapPlayerManager.removeDeathswapPlayer(player);
    }
}
