package it.areson.aresondeathswap.arena;

import org.bukkit.Location;
import org.bukkit.World;

public class ArenaUtils {

    public static Location getRandomLocationInRadius(World arenaWorld, int radiusFromSpawn) {
        final Location spawnLocation = arenaWorld.getSpawnLocation();
        final Location clone = spawnLocation.clone();

        double randomX = (Math.random() * radiusFromSpawn * 2) - radiusFromSpawn;
        double randomZ = (Math.random() * radiusFromSpawn * 2) - radiusFromSpawn;
        clone.add(randomX, 0, randomZ);
        int highestBlockYAt = arenaWorld.getHighestBlockYAt((int) randomX, (int) randomZ);
        clone.setY(highestBlockYAt + 1);
        return clone;
    }

}
