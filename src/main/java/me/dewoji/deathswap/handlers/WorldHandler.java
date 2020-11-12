package me.dewoji.deathswap.handlers;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldHandler {

    private JavaPlugin instance;
    private World gameWorld;

    public WorldHandler(JavaPlugin instance) {
        this.instance = instance;

    }

    public void generateWorld() {
        if (instance.getServer().getWorld("gameWorld") == null) {
            instance.getServer().createWorld(WorldCreator.name("gameWorld"));
        }
    }

    public void saveWorld() {

    }

    public void regenerateWorld() {
        gameWorld = instance.getServer().getWorld("gameWorld");

    }

}
