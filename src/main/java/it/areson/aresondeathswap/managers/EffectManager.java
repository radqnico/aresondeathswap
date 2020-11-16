package it.areson.aresondeathswap.managers;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class EffectManager {

    private AresonDeathSwap aresonDeathSwap;

    public EffectManager(AresonDeathSwap aresonDeathSwap) {
        this.aresonDeathSwap = aresonDeathSwap;
    }

    public void winFirework(Player player) {
        FireworkEffect effect = FireworkEffect.builder().trail(false).flicker(false).withColor(Color.RED).withFade(Color.ORANGE).with(FireworkEffect.Type.BALL).build();
        Firework fw = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(effect);
        meta.setPower(1);
        fw.setFireworkMeta(meta);
        fw.detonate();
    }

    public void deathStrike(Player player) {
        player.getWorld().strikeLightningEffect(player.getLocation());
    }

    public void joinedArena(Player player) {
        World world = aresonDeathSwap.getServer().getWorld(aresonDeathSwap.MAIN_WORLD_NAME);
        if (world != null) {
            world.spawnParticle(Particle.COMPOSTER, player.getLocation().clone().add(new Vector(0, 1, 0)), 100, 0.3, 0.7, 0.3);
        }
    }

}
