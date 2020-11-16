package it.areson.aresondeathswap.managers;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SoundManager {

    public void joinServer(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.MASTER, 1f, 1f);
    }

    public void joinServer(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.MASTER, 1f, 1f);
        }
    }

    public void joinArena(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1f, 1f);
    }

    public void joinArena(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1f, 1f);
        }
    }

    public void cannotJoinArena(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 0.5f, 1f);
    }

    public void cannotJoinArena(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 0.5f, 1f);
        }
    }

    public void startingGame(Player target) {
        target.playSound(target.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.MASTER, 1f, 1f);
    }

    public void startingGame(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.MASTER, 1f, 1f);
        }
    }

    public void startingGameInterrupted(Player target) {
        target.playSound(target.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 1f, 1f);
    }

    public void startingGameInterrupted(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 1f, 1f);
        }
    }

    public void gameStarted(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 0.4f, 1f);
    }

    public void gameStarted(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 0.4f, 1f);
        }
    }

    public void loser(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_MULE_AMBIENT, SoundCategory.MASTER, 0.4f, 1f);
    }

    public void loser(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.ENTITY_MULE_AMBIENT, SoundCategory.MASTER, 0.4f, 1f);
        }
    }

    public void winner(Player target) {
        target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.6f, 1f);
    }

    public void winner(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.6f, 1f);
        }
    }

    public void tick(Player target) {
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundCategory.MASTER, 0.6f, 1f);
    }

    public void tick(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundCategory.MASTER, 0.6f, 1f);
        }
    }
}
