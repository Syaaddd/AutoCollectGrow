package com.github.Syaaddd.autoCollectGrow.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for item manipulation and serialization
 */
public class ItemUtils {

    /**
     * Check if an ItemStack is null or air
     */
    public static boolean isNullOrAir(ItemStack item) {
        return item == null || item.getType().isAir();
    }

    /**
     * Clone an ItemStack safely
     */
    public static ItemStack cloneItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        return item.clone();
    }

    /**
     * Set the display name of an item with color code translation
     */
    public static ItemStack setDisplayName(ItemStack item, String name) {
        if (item == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(name));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Set the lore of an item with color code translation
     */
    public static ItemStack setLore(ItemStack item, List<String> lore) {
        if (item == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(translateColorCodes(lore));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Add lore to an item with color code translation
     */
    public static ItemStack addLore(ItemStack item, String... lore) {
        if (item == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> currentLore = meta.getLore();
            if (currentLore == null) {
                currentLore = new ArrayList<>();
            }
            
            for (String line : lore) {
                currentLore.add(translateColorCodes(line));
            }
            
            meta.setLore(currentLore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Translate color codes in strings
     * Converts & to § for Minecraft color codes
     */
    public static String translateColorCodes(String text) {
        if (text == null) {
            return null;
        }
        
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Translate color codes in a list of strings
     */
    public static List<String> translateColorCodes(List<String> lore) {
        if (lore == null) {
            return null;
        }

        List<String> translated = new ArrayList<>();
        for (String line : lore) {
            translated.add(translateColorCodes(line));
        }
        return translated;
    }

    /**
     * Get the total amount of items in an array
     */
    public static int getTotalItemCount(ItemStack[] items) {
        int total = 0;
        for (ItemStack item : items) {
            if (item != null) {
                total += item.getAmount();
            }
        }
        return total;
    }

    /**
     * Count how many non-null items are in an array
     */
    public static int getNonEmptySlotCount(ItemStack[] items) {
        int count = 0;
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                count++;
            }
        }
        return count;
    }
}
