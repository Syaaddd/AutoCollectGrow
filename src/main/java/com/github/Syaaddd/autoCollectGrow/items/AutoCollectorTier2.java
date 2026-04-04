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
 * Tier 2 AutoCollector - Advanced Collector
 * Radius: 10 blocks
 * Improved collection range and efficiency
 */
public class AutoCollectorTier2 extends AutoCollectorMachine {

    private static final int TIER = 2;
    private static final int RADIUS = 10;

    public AutoCollectorTier2() {
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
            "AUTO_COLLECTOR_TIER_2",
            createDisplayItem()
        );
    }

    private static ItemStack createDisplayItem() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§aAutoCollector §7(Tier 2)");
            List<String> lore = new ArrayList<>();
            lore.add("§7Advanced item collector");
            lore.add("§7");
            lore.add("§8▪ §7Radius: §e10 blocks");
            lore.add("§8▪ §7Energy: §e25 J/tick");
            lore.add("§8▪ §7Capacity: §e2500 J");
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
            SlimefunItems.REINFORCED_ALLOY_INGOT, SlimefunItems.ADVANCED_CIRCUIT_BOARD, SlimefunItems.REINFORCED_ALLOY_INGOT,
            SlimefunItems.ELECTRIC_MOTOR, new ItemStack(Material.CHEST), SlimefunItems.ELECTRIC_MOTOR,
            SlimefunItems.REINFORCED_ALLOY_INGOT, SlimefunItems.BASIC_CIRCUIT_BOARD, SlimefunItems.REINFORCED_ALLOY_INGOT
        };
    }

    @Override
    public @NotNull String getTierName() {
        return "Advanced";
    }
}
