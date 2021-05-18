package it.areson.aresondeathswap.player;

import it.areson.aresoncore.database.MySqlConnection;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.events.PlayerEvents;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;

public class DeathswapPlayerManager {

    private final HashMap<String, DeathswapPlayer> onlinePlayers;
    private final DeathswapPlayerGateway gateway;
    private final AresonDeathSwap aresonDeathSwap;
    private final PlayerEvents playerPlayerEvents;

    public DeathswapPlayerManager(AresonDeathSwap aresonDeathSwap, MySqlConnection mySqlConnection, String tableName) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.onlinePlayers = new HashMap<>();
        this.gateway = new DeathswapPlayerGateway(mySqlConnection, tableName);
        playerPlayerEvents = new PlayerEvents(aresonDeathSwap);
        registerEvents();
    }

    public DeathswapPlayerGateway getGateway() {
        return gateway;
    }

    public HashMap<String, DeathswapPlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void registerEvents() {
        playerPlayerEvents.registerEvents();
    }

    public void unregisterEvents() {
        playerPlayerEvents.unregisterEvents();
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
            if (gateway.upsert(deathswapPlayer)) {
                aresonDeathSwap.getLogger().info("Player " + player.getName() + "' saved on DB");
            } else {
                aresonDeathSwap.getLogger().info("Player " + player.getName() + "' NOT saved on DB");
            }
        }
    }

    public void saveDeathswapPlayer(DeathswapPlayer deathswapPlayer) {
        if (deathswapPlayer != null) {
            if (gateway.upsert(deathswapPlayer)) {
                aresonDeathSwap.getLogger().info("Player " + deathswapPlayer.getNickName() + "' saved on DB");
            } else {
                aresonDeathSwap.getLogger().info("Player " + deathswapPlayer.getNickName() + "' NOT saved on DB");
            }
        }
    }

    public void saveAllPlayers() {
        for (DeathswapPlayer deathswapPlayer : onlinePlayers.values()) {
            gateway.upsert(deathswapPlayer);
        }
    }
}
