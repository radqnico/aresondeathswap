package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerHolder {

    private JavaPlugin instance;
    private ArrayList<Player> alivePlayers;
    private ArrayList<Player> lobbyPlayers;

    public PlayerHolder(JavaPlugin instance) {
        this.instance = instance;
        this.alivePlayers = AresonDeathSwap.getAlivePlayers();
        this.lobbyPlayers = AresonDeathSwap.getLobbyPlayers();
    }

    public void playerRotate() {
        ArrayList<Player> onlinePlayers = AresonDeathSwap.getAlivePlayers();
        List<Location> locations = onlinePlayers.stream().map(Player::getLocation).collect(Collectors.toList());

        int lastIndex = locations.size() - 1;
        Location lastLocation = locations.get(lastIndex);

        for (int i = 0; i < lastIndex; i++) {
            locations.set(i + 1, locations.get(i));
        }
        locations.set(0, lastLocation);

        for (int i = 0; i < onlinePlayers.size(); i++) {
            onlinePlayers.get(i).teleport(locations.get(i));
        }
    }

}
