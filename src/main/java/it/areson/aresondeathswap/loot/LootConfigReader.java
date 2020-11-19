package it.areson.aresondeathswap.loot;

import java.util.ArrayList;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.managers.FileManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class LootConfigReader extends FileManager {

	private ArrayList<LootItem> items;
	private int minItems;
	private int maxItems;

	public LootConfigReader(AresonDeathSwap instance, String nomeFile) {
		super(instance, nomeFile);
		items = new ArrayList<LootItem>();
	}//LootConfigReader

	public void readLoot() {
		minItems = getFileConfiguration().getInt("min-objects");
		maxItems = getFileConfiguration().getInt("max-objects");
		ConfigurationSection root = getFileConfiguration().getConfigurationSection("loot");
		for(String id : root.getKeys(false)) {
			ConfigurationSection itemSection = root.getConfigurationSection(id);
			Material material = Material.matchMaterial(itemSection.getString("material"));
			double probability = itemSection.getDouble("probability");
			int amount = 1;
			if(itemSection.isSet("amount")) {
				amount = itemSection.getInt("amount");
			}
			ItemStack ogg = new ItemStack(material, amount);
			ItemMeta meta = ogg.getItemMeta();
			if(itemSection.isSet("name")) {
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemSection.getString("name")));
			}
			if(itemSection.isSet("lore")) {
				ConfigurationSection confLore = itemSection.getConfigurationSection("lore");
				ArrayList<String> lore = new ArrayList<String>();
				for(String row : confLore.getKeys(false)) {
					lore.add(ChatColor.translateAlternateColorCodes('&', confLore.getString(row)));
				}
				meta.setLore(lore);
			}
			if(itemSection.isSet("enchants")) {
				ConfigurationSection confEnchants = itemSection.getConfigurationSection("enchants");
				for(String ench : confEnchants.getKeys(false)) {
					ConfigurationSection tmpEnch = confEnchants.getConfigurationSection(ench);
					Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(tmpEnch.getString("name")));
					int level = tmpEnch.getInt("level");
					meta.addEnchant(e, level, true);
				}
			}
			if(itemSection.isSet("unbreakable")) {
				boolean unbreakable = itemSection.getBoolean("unbreakable");
				meta.setUnbreakable(unbreakable);
			}
			ogg.setItemMeta(meta);
			if(material.equals(Material.POTION)) {
				String effect = itemSection.getString("effect");
				int duration = itemSection.getInt("duration");
				int amplifier = itemSection.getInt("amplifier");
				PotionMeta potionMeta = (PotionMeta)meta;
				PotionEffect e = new PotionEffect(PotionEffectType.getByName(effect.toUpperCase()), duration*20, amplifier);
				potionMeta.addCustomEffect(e, true);
				ogg.setItemMeta(potionMeta);
			}
			if(material.equals(Material.SPLASH_POTION)) {
				String effect = itemSection.getString("effect");
				int amplifier = itemSection.getInt("amplifier");
				PotionMeta potionMeta = (PotionMeta)meta;
				PotionEffect e = new PotionEffect(PotionEffectType.getByName(effect.toUpperCase()), 20, amplifier);
				potionMeta.addCustomEffect(e, true);
				ogg.setItemMeta(potionMeta);
			}
			if(material.equals(Material.LINGERING_POTION)) {
				String effect = itemSection.getString("effect");
				int duration = itemSection.getInt("duration");
				int amplifier = itemSection.getInt("amplifier");
				PotionMeta potionMeta = (PotionMeta)meta;
				PotionEffect e = new PotionEffect(PotionEffectType.getByName(effect.toUpperCase()), duration*20, amplifier);
				potionMeta.addCustomEffect(e, true);
				ogg.setItemMeta(potionMeta);
			}
			items.add(new LootItem(ogg, probability));
		}
	}

	public void newLootChest(Chest chest) {
		LootChest lc = new LootChest(minItems, maxItems, chest);
		for(LootItem i : items)
			lc.addPossibleItem(i);
		lc.executeLoot();
	}

	public void newLootChest(DoubleChest chest) {
		LootChest lc = new LootChest(minItems, maxItems, chest);
		for(LootItem i : items)
			lc.addPossibleItem(i);
		lc.executeLoot();
	}
}
