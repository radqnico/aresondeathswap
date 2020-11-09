package me.dewoji.deathswap.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerHolder {

    JavaPlugin instance;

    public PlayerHolder(JavaPlugin instance) {
        this.instance = instance;
    }

    public void playerRotate() {
        ArrayList<Player> onlinePlayers = new ArrayList<>(instance.getServer().getOnlinePlayers());
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

        System.out.println("AAAA");
    }
}
