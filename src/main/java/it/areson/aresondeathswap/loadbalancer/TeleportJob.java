package it.areson.aresondeathswap.loadbalancer;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportJob implements Job {

    private final AresonDeathSwap aresonDeathSwap;
    private final Location toLocation;
    private final Player player;
    private final Runnable whenComplete;

    public TeleportJob(AresonDeathSwap aresonDeathSwap, Player player, Location toLocation, Runnable whenComplete) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.toLocation = toLocation;
        this.player = player;
        this.whenComplete = whenComplete;
    }

    @Override
    public void compute() {
        if (player != null && player.isOnline() && !player.isDead()) {
            toLocation.setYaw(player.getLocation().getYaw());
            toLocation.setPitch(player.getLocation().getPitch());
            if (whenComplete != null) {
                player.teleport(toLocation);
                aresonDeathSwap.getServer().getScheduler().runTask(aresonDeathSwap, whenComplete);
            } else {
                player.teleport(toLocation);
            }
        }
    }
}
