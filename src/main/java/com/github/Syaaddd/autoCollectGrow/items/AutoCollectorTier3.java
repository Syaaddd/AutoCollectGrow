package com.github.Syaaddd.autoCollectGrow.items;

import com.github.Syaaddd.autoCollectGrow.machines.AutoCollectorMachine;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Tier 3 AutoCollector - Elite Collector
 * Radius: 20 blocks
 * High-performance collection with large storage
 */
public class AutoCollectorTier3 extends AutoCollectorMachine {

    private static final int TIER = 3;
    private static final int RADIUS = 20;

    public AutoCollectorTier3() {
        super(
            AutoCollectorTier1.ITEM_GROUP,
            createItem(),
            RecipeType.ENHANCED_CRAFTING_TABLE,
            createRecipe(),
            TIER,
            RADIUS
        );
    }

    private static SlimefunItemStack createItem() {
        return new SlimefunItemStack(
            "AUTO_COLLECTOR_TIER_3",
            createDisplayItem()
        );
    }

    private static ItemStack createDisplayItem() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§bAutoCollector §7(Tier 3)");
            List<String> lore = new ArrayList<>();
            lore.add("§7Elite item collector");
            lore.add("§7");
            lore.add("§8▪ §7Radius: §e20 blocks");
            lore.add("§8▪ §7Energy: §e50 J/tick");
            lore.add("§8▪ §7Capacity: §e5000 J");
            lore.add("§7");
            lore.add("§6§oCollects nearby items automatically");
            lore.add("§6§oand stores them for easy selling!");
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private static ItemStack[] createRecipe() {
        return new ItemStack[] {
            SlimefunItems.BILLON_INGOT, SlimefunItems.ADVANCED_CIRCUIT_BOARD, SlimefunItems.BILLON_INGOT,
            SlimefunItems.ELECTRIC_MOTOR, SlimefunItems.REINFORCED_ALLOY_INGOT, SlimefunItems.ELECTRIC_MOTOR,
            SlimefunItems.BILLON_INGOT, SlimefunItems.POWER_CRYSTAL, SlimefunItems.BILLON_INGOT
        };
    }

    @Override
    public @NotNull String getTierName() {
        return "Elite";
    }
}
