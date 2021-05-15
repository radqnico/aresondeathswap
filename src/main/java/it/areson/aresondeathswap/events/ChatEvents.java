package it.areson.aresondeathswap.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import it.areson.aresoncore.events.GeneralEventListener;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.Arena;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.stream.Collectors;

public class ChatEvents extends GeneralEventListener {

    public ChatEvents(JavaPlugin javaPlugin) {
        super(javaPlugin);
        registerEvents();
    }

//    @EventHandler(priority = EventPriority.HIGH)
//    public void onChat(AsyncChatEvent event) {
//        Player player = event.getPlayer();
//        DeathswapPlayer deathswapPlayer = AresonDeathSwap.instance.getDeathswapPlayerManager().getDeathswapPlayer(player);
//
//        Optional<Arena> arenaOfPlayer = AresonDeathSwap.instance.getArenaManager().getArenaOfPlayer(deathswapPlayer);
//        arenaOfPlayer.ifPresent(arena -> {
//            event.recipients().clear();
//            event.recipients().addAll(arena.getPlayers().keySet().stream().map(DeathswapPlayer::getActualPlayer).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
//        });
//    }
}
