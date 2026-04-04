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
 * Tier 4 AutoCollector - Quantum Collector
 * Radius: 50 blocks
 * Ultimate collection machine with maximum range
 */
public class AutoCollectorTier4 extends AutoCollectorMachine {

    private static final int TIER = 4;
    private static final int RADIUS = 50;

    public AutoCollectorTier4() {
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
            "AUTO_COLLECTOR_TIER_4",
            createDisplayItem()
        );
    }

    private static ItemStack createDisplayItem() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§dAutoCollector §7(Tier 4)");
            List<String> lore = new ArrayList<>();
            lore.add("§7Quantum item collector");
            lore.add("§7");
            lore.add("§8▪ §7Radius: §e50 blocks");
            lore.add("§8▪ §7Energy: §e100 J/tick");
            lore.add("§8▪ §7Capacity: §e10000 J");
            lore.add("§7");
            lore.add("§6§oCollects nearby items automatically");
            lore.add("§6§oand stores them for easy selling!");
            lore.add("");
            lore.add("§5§oUltimate collection power!");
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private static ItemStack[] createRecipe() {
        return new ItemStack[] {
            SlimefunItems.NETHER_ICE, SlimefunItems.PROGRAMMABLE_ANDROID, SlimefunItems.NETHER_ICE,
            SlimefunItems.PROGRAMMABLE_ANDROID, SlimefunItems.BILLON_INGOT, SlimefunItems.PROGRAMMABLE_ANDROID,
            SlimefunItems.NETHER_ICE, SlimefunItems.POWER_CRYSTAL, SlimefunItems.NETHER_ICE
        };
    }

    @Override
    public @NotNull String getTierName() {
        return "Quantum";
    }
}
