package it.areson.aresondeathswap.loot;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.managers.FileManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class LootConfigReader extends FileManager {

    private ArrayList<LootItem> items;
    private int minItems;
    private int maxItems;
    private List<Location> lootChests;

    public LootConfigReader(AresonDeathSwap instance, String nomeFile) {
        super(instance, nomeFile);
        items = new ArrayList<>();
        lootChests = new ArrayList<>();
    }//LootConfigReader

    public void readLoot() {
        minItems = getFileConfiguration().getInt("min-objects");
        maxItems = getFileConfiguration().getInt("max-objects");
        ConfigurationSection root = getFileConfiguration().getConfigurationSection("loot");
        for (String id : root.getKeys(false)) {
            ConfigurationSection itemSection = root.getConfigurationSection(id);
            Material material = Material.matchMaterial(itemSection.getString("material"));
            double probability = itemSection.getDouble("probability");
            int amount = 1;
            if (itemSection.isSet("amount")) {
                amount = itemSection.getInt("amount");
            }
            ItemStack ogg = new ItemStack(material, amount);
            ItemMeta meta = ogg.getItemMeta();
            if (itemSection.isSet("name")) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemSection.getString("name")));
            }
            if (itemSection.isSet("lore")) {
                ConfigurationSection confLore = itemSection.getConfigurationSection("lore");
                ArrayList<String> lore = new ArrayList<String>();
                for (String row : confLore.getKeys(false)) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', confLore.getString(row)));
                }
                meta.setLore(lore);
            }
            if (itemSection.isSet("enchants")) {
                ConfigurationSection confEnchants = itemSection.getConfigurationSection("enchants");
                for (String ench : confEnchants.getKeys(false)) {
                    ConfigurationSection tmpEnch = confEnchants.getConfigurationSection(ench);
                    Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(tmpEnch.getString("name")));
                    int level = tmpEnch.getInt("level");
                    meta.addEnchant(e, level, true);
                }
            }
            if (itemSection.isSet("unbreakable")) {
                boolean unbreakable = itemSection.getBoolean("unbreakable");
                meta.setUnbreakable(unbreakable);
            }
            ogg.setItemMeta(meta);
            if (material.equals(Material.POTION)) {
                String effect = itemSection.getString("effect");
                int duration = itemSection.getInt("duration");
                int amplifier = itemSection.getInt("amplifier");
                PotionMeta potionMeta = (PotionMeta) meta;
                PotionEffect e = new PotionEffect(PotionEffectType.getByName(effect.toUpperCase()), duration * 20, amplifier);
                potionMeta.addCustomEffect(e, true);
                ogg.setItemMeta(potionMeta);
            }
            if (material.equals(Material.SPLASH_POTION)) {
                String effect = itemSection.getString("effect");
                int duration = itemSection.getInt("duration");
                int amplifier = itemSection.getInt("amplifier");
                PotionMeta potionMeta = (PotionMeta) meta;
                PotionEffect e = new PotionEffect(PotionEffectType.getByName(effect.toUpperCase()), duration * 20, amplifier);
                potionMeta.addCustomEffect(e, true);
                ogg.setItemMeta(potionMeta);
            }
            if (material.equals(Material.LINGERING_POTION)) {
                String effect = itemSection.getString("effect");
                int duration = itemSection.getInt("duration");
                int amplifier = itemSection.getInt("amplifier");
                PotionMeta potionMeta = (PotionMeta) meta;
                PotionEffect e = new PotionEffect(PotionEffectType.getByName(effect.toUpperCase()), duration * 20, amplifier);
                potionMeta.addCustomEffect(e, true);
                ogg.setItemMeta(potionMeta);
            }
            items.add(new LootItem(ogg, probability));
        }
    }

    public void newLootChest(Chest chest) {
        LootChest lc = new LootChest(minItems, maxItems, chest);
        for (LootItem i : items)
            lc.addPossibleItem(i);
        lc.executeLoot();
    }

    public void newLootChest(DoubleChest chest) {
        LootChest lc = new LootChest(minItems, maxItems, chest);
        for (LootItem i : items)
            lc.addPossibleItem(i);
        lc.executeLoot();
    }

    public void placeNewChestNear(Player player) {
        Location playerLocation = player.getLocation().clone();
        Random random = new Random();
        int dx = (random.nextBoolean() ? 1 : -1) * (2 + random.nextInt(2));
        int dz = (random.nextBoolean() ? 1 : -1) * (2 + random.nextInt(2));
        Location addedLocation = playerLocation.add(dx, 0, dz);
        int highestBlockYAt = player.getWorld().getHighestBlockYAt(addedLocation);
        addedLocation.setY(highestBlockYAt + 1);

        aresonDeathSwap.getLogger().warning("[LootChest] Spawning chest for " + player.getName() + " in location " + addedLocation.toString());

        Block block = addedLocation.getBlock();
        block.setType(Material.CHEST);
        lootChests.add(block.getLocation());
    }

    public boolean isLootChest(Location chestLocation) {
        return lootChests.stream().anyMatch(location -> {
            World locationWorld = location.getWorld();
            World chestLocationWorld = chestLocation.getWorld();
            if (locationWorld != null && chestLocationWorld != null) {
                if (chestLocation.getWorld().getName().equals(locationWorld.getName())) {
                    return chestLocation.toVector().distance(location.toVector()) < 1.;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        });
    }

    public void removeLootChest(Location chestLocation) {
        lootChests = lootChests.stream().filter(location -> {
            World locationWorld = location.getWorld();
            World chestLocationWorld = chestLocation.getWorld();
            if (locationWorld != null && chestLocationWorld != null) {
                if (chestLocation.getWorld().getName().equals(locationWorld.getName())) {
                    return chestLocation.toVector().distance(location.toVector()) >= 1.5;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }).collect(Collectors.toList());
    }

    public void removeChestOfWorld(String arenaName) {
        lootChests = lootChests.stream().filter(location -> !location.getWorld().getName().equals(arenaName)).collect(Collectors.toList());
    }
}
