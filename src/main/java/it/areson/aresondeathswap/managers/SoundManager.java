package it.areson.aresondeathswap.managers;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SoundManager {

    public void join(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
    }

    public void join(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        }
    }

    public void startingGame(Player target) {
        target.playSound(target.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
    }

    public void startingGame(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.5f);
        }
    }
}
