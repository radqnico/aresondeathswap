package it.areson.aresondeathswap.player;

import it.areson.aresoncore.database.MySqlConnection;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.events.PlayerQuitJoinEvents;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeathswapPlayerManager {

    private final HashMap<String, DeathswapPlayer> onlinePlayers;
    private final DeathswapPlayerGateway gateway;
    private final AresonDeathSwap aresonDeathSwap;

    private final PlayerQuitJoinEvents playerQuitJoinEvents;

    public DeathswapPlayerManager(AresonDeathSwap aresonDeathSwap, MySqlConnection mySqlConnection, String tableName) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.onlinePlayers = new HashMap<>();
        this.gateway = new DeathswapPlayerGateway(mySqlConnection, tableName);
        playerQuitJoinEvents = new PlayerQuitJoinEvents(aresonDeathSwap);
        registerEvents();
    }

    public void registerEvents() {
        playerQuitJoinEvents.registerEvents();
    }

    public void unregisterEvents() {
        playerQuitJoinEvents.unregisterEvents();
    }

    public DeathswapPlayer getDeathswapPlayer(Player player) {
        return onlinePlayers.get(player.getName());
    }

    public DeathswapPlayer addDeathswapPlayer(Player player) {
        Optional<DeathswapPlayer> optionalDeathswapPlayer = gateway.getById(player.getName());
        DeathswapPlayer deathswapPlayer;
        deathswapPlayer = optionalDeathswapPlayer.orElseGet(() -> new DeathswapPlayer(player.getName(), 0, 0, 0, 0));
        onlinePlayers.put(player.getName(), deathswapPlayer);
        return deathswapPlayer;
    }

    public void removeDeathswapPlayer(Player player) {
        onlinePlayers.remove(player.getName());
    }

    public void saveDeathswapPlayer(Player player) {
        DeathswapPlayer deathswapPlayer = onlinePlayers.get(player.getName());
        if (deathswapPlayer != null) {
            if(gateway.upsert(deathswapPlayer)){
                aresonDeathSwap.getLogger().info("Player " + player.getName() + "' saved on DB");
            }else{
                aresonDeathSwap.getLogger().info("Player " + player.getName() + "' NOT saved on DB");
            }
        }

    }

    public void saveAllPlayers() {
        for (DeathswapPlayer deathswapPlayer : onlinePlayers.values()) {
            gateway.upsert(deathswapPlayer);
        }
    }

    public List<String> getOnlinePlayersNames() {
        return onlinePlayers.values().stream().map(DeathswapPlayer::getNickName).collect(Collectors.toList());
    }
}
