package com.github.Syaaddd.autoCollectGrow.items;

import com.github.Syaaddd.autoCollectGrow.machines.AutoCollectorMachine;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Tier 1 AutoCollector - Basic Collector
 * Radius: 5 blocks
 * Entry-level machine for collecting items
 */
public class AutoCollectorTier1 extends AutoCollectorMachine {

    private static final int TIER = 1;
    private static final int RADIUS = 5;
    public static final ItemGroup ITEM_GROUP = new ItemGroup(
        new NamespacedKey("autocollectgrow", "autocollectors"),
        new CustomItemStack(Material.CHEST, "&6&lAutoCollect")
    );

    public AutoCollectorTier1() {
        super(
            ITEM_GROUP,
            createItem(),
            RecipeType.ENHANCED_CRAFTING_TABLE,
            createRecipe(),
            TIER,
            RADIUS
        );
    }

    private static SlimefunItemStack createItem() {
        return new SlimefunItemStack(
            "AUTO_COLLECTOR_TIER_1",
            createDisplayItem()
        );
    }

    private static ItemStack createDisplayItem() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6AutoCollector §7(Tier 1)");
            List<String> lore = new ArrayList<>();
            lore.add("§7Basic item collector");
            lore.add("§7");
            lore.add("§8▪ §7Radius: §e5 blocks");
            lore.add("§8▪ §7Energy: §e10 J/tick");
            lore.add("§8▪ §7Capacity: §e1000 J");
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
            new ItemStack(Material.IRON_BLOCK), SlimefunItems.BASIC_CIRCUIT_BOARD, new ItemStack(Material.IRON_BLOCK),
            new ItemStack(Material.CHEST), new ItemStack(Material.HOPPER), new ItemStack(Material.CHEST),
            new ItemStack(Material.IRON_BLOCK), SlimefunItems.ELECTRIC_MOTOR, new ItemStack(Material.IRON_BLOCK)
        };
    }

    @Override
    public @NotNull String getTierName() {
        return "Basic";
    }
}
