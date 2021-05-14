package it.areson.aresondeathswap.loot;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class LootChest {

    private boolean isDouble;
    private int minItems;
    private int maxItems;
    private Chest left;
    private Chest right;
    private ArrayList<LootItem> possibleItems;
    private ArrayList<LootItem> selectedItems;

    public LootChest(int minItems, int maxItems, Chest chest) {
        this.isDouble = false;
        this.minItems = minItems;
        this.maxItems = maxItems;
        this.left = chest;
        this.right = null;
        this.possibleItems = new ArrayList<>();
        this.selectedItems = new ArrayList<>();
    }//LootChest

    public LootChest(int minItems, int maxItems, DoubleChest chest) {
        this.isDouble = true;
        this.minItems = minItems;
        this.maxItems = maxItems;
        this.left = (Chest) chest.getLeftSide();
        this.right = (Chest) chest.getRightSide();
        this.possibleItems = new ArrayList<>();
        this.selectedItems = new ArrayList<>();
    }//LootChest

    public void addPossibleItem(LootItem item) {
        possibleItems.add(item);
    }//addPossibleItem

    private void selectLoot() {
        int randomNum = ThreadLocalRandom.current().nextInt(minItems, maxItems + 1);
        if (isDouble)
            for (int i = 0; i < randomNum * 2; i++)
                selectedItems.add(extractOne());
        else
            for (int i = 0; i < randomNum; i++)
                selectedItems.add(extractOne());
    }//selectLoot

    private void putSelectedItems() {
        for (LootItem i : selectedItems)
            left.getInventory().addItem(i.getItem());
    }//putSelectedItems

    private void breakChest() {
        if (isDouble) {
            left.getBlock().setType(Material.AIR);
            right.getBlock().setType(Material.AIR);
        } else {
            left.getBlock().setType(Material.AIR);
        }
    }//breakChest

    private LootItem extractOne() {
        ArrayList<LootItem> toShuffle = new ArrayList<LootItem>(possibleItems);
        Collections.shuffle(toShuffle);
        int counter = 0;
        while (!toShuffle.get(counter).extract()) {
            counter++;
            if (counter >= toShuffle.size())
                counter = 0;
        }
        return toShuffle.get(counter);
    }//extractOne

    public void executeLoot() {
        selectLoot();
        putSelectedItems();
        breakChest();
    }//executeLoot

}
