package it.areson.aresondeathswap.managers;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SoundManager {

    public void joinServer(Location target) {
        World world = target.getWorld();
        if (world != null) {
            world.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.MASTER, 1f, 1f);
        }
    }

    public void cannotJoinArena(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 0.5f, 1f);
    }

    public void startingGame(Player target) {
        target.playSound(target.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.MASTER, 1f, 1f);
    }

    public void startingGameInterrupted(Player target) {
        target.playSound(target.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 1f, 1f);
    }

    public void gameStarted(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.MASTER, 0.4f, 1f);
    }

    public void loser(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_RAVAGER_STUNNED, SoundCategory.MASTER, 0.4f, 1f);
    }

    public void winner(Player target) {
        target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.6f, 1f);
    }

    public void tick(Player target) {
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundCategory.MASTER, 0.6f, 1f);
    }

    public void teleport(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.MASTER, 0.6f, 1f);
    }
}
