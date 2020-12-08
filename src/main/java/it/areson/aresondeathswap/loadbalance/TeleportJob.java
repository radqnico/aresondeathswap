package it.areson.aresondeathswap.loadbalance;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class TeleportJob implements Job {

    private Location toLocation;
    private Player player;
    private BiConsumer<? super Boolean, ? super Throwable> whenComplete;

    public TeleportJob(Player player, Location toLocation, BiConsumer<? super Boolean, ? super Throwable> whenComplete) {
        this.toLocation = toLocation;
        this.player = player;
        this.whenComplete = whenComplete;
    }

    @Override
    public void compute() {
        if (player != null) {
            toLocation.setYaw(player.getLocation().getYaw());
            toLocation.setPitch(player.getLocation().getPitch());
            player.teleportAsync(toLocation).whenComplete(whenComplete);
        }
    }
}
