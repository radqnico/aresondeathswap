package me.dewoji.deathswap;

import org.bukkit.plugin.java.JavaPlugin;

public final class DeathSwap extends JavaPlugin {

    private static DeathSwap instance;

    @Override
    public void onEnable() {
        instance = this;

    }

    @Override
    public void onDisable() {

    }
}
