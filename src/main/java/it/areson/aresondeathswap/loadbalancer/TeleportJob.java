package it.areson.aresondeathswap.loadbalancer;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class TeleportJob implements Job {

    private final Location toLocation;
    private final Player player;
    private final BiConsumer<? super Boolean, ? super Throwable> whenComplete;

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
