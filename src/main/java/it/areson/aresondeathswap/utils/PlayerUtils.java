package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

public class PlayerUtils {

    public static void resetPlayerStatus(Player player) {
        player.setInvulnerable(false);
        player.setAllowFlight(false);
        player.setInvisible(false);
        player.setExp(0);
        player.setLevel(0);
        player.setFlying(false);
        player.setHealth(20);
        player.setFireTicks(0);
        player.setGlowing(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setSaturation(20);
        player.setFoodLevel(20);
        Collection<PotionEffect> activePotionEffects = player.getActivePotionEffects();
        for (PotionEffect potionEffect : activePotionEffects) {
            player.removePotionEffect(potionEffect.getType());
        }
    }

    public static void playerDeadStatus(Player player) {
        player.setInvulnerable(true);
        player.setInvisible(true);
        player.setExp(0);
        player.setLevel(0);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setHealth(20);
        player.setFireTicks(0);
        player.setGlowing(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setSaturation(20);
        player.setFoodLevel(20);
        Collection<PotionEffect> activePotionEffects = player.getActivePotionEffects();
        for (PotionEffect potionEffect : activePotionEffects) {
            player.removePotionEffect(potionEffect.getType());
        }
    }

    public static void sendShortTitle(Player player, String titleKey, String subTitleKey) {
        player.sendTitle(AresonDeathSwap.instance.messages.getPlainMessageNoPrefix(titleKey), AresonDeathSwap.instance.messages.getPlainMessageNoPrefix(subTitleKey), 20, 30, 20);
    }

    public static void sendLongTitle(Player player, String titleKey, String subTitleKey) {
        player.sendTitle(AresonDeathSwap.instance.messages.getPlainMessageNoPrefix(titleKey), AresonDeathSwap.instance.messages.getPlainMessageNoPrefix(subTitleKey), 20, 60, 20);
    }

    public static void giveInitialKit(Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.addItem(new ItemStack(Material.BUCKET));
        inventory.addItem(new ItemStack(Material.OAK_BOAT));
    }

}
