package it.areson.aresondeathswap.loot;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LootItem {

    private ItemStack item;
    private double probability;

    public LootItem(ItemStack item, double probability) {
        this.item = item;
        this.probability = probability;
        if (probability > 1)
            this.probability = 1.0;
        if (probability < 0)
            this.probability = 0.0;
    }//LootItem

    public ItemStack getItem() {
        return item;
    }//getItem

    public ItemMeta getItemMeta() {
        return item.getItemMeta();
    }//getItemMeta

    public void setItemMeta(ItemMeta itemMeta) {
        item.setItemMeta(itemMeta);
    }//setItemMeta

    public boolean extract() {
        double random = Math.random();
        if (random <= probability)
            return true;
        return false;
    }//extract

}
