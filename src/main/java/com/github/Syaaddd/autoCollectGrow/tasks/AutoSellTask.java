package com.github.Syaaddd.autoCollectGrow.tasks;

import com.github.Syaaddd.autoCollectGrow.AutoCollectGrow;
import com.github.Syaaddd.autoCollectGrow.hooks.ShopGuiPlusHook;
import com.github.Syaaddd.autoCollectGrow.hooks.VaultHook;
import com.github.Syaaddd.autoCollectGrow.machines.AutoCollectorMachine;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Scheduled task that automatically sells items from AutoCollector machines
 */
public class AutoSellTask extends BukkitRunnable {

    private final AutoCollectGrow plugin;

    public AutoSellTask(AutoCollectGrow plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            ShopGuiPlusHook shopHook = plugin.getShopGuiPlusHook();
            if (shopHook == null || !shopHook.isHooked()) {
                plugin.getLogger().warning("[AutoSell] ShopGuiPlus not available!");
                return;
            }

            VaultHook vaultHook = plugin.getVaultHook();
            if (vaultHook == null || !vaultHook.isHooked()) {
                plugin.getLogger().warning("[AutoSell] Vault not available!");
                return;
            }

            int machinesProcessed = 0;
            int totalItemsSold = 0;

            // Iterate through all registered machines
            for (Location loc : AutoCollectorMachine.getAllMachines()) {
                Block block = loc.getBlock();
                
                // Check if chunk is loaded
                if (!block.getChunk().isLoaded()) {
                    continue;
                }

                // Check if auto-sell is enabled
                String autoSellStatus = BlockStorage.getLocationInfo(loc, "auto-sell");
                if (!"true".equals(autoSellStatus)) {
                    continue;
                }

                // Check if machine is enabled
                String machineEnabled = BlockStorage.getLocationInfo(loc, "enabled");
                if ("false".equals(machineEnabled)) {
                    continue;
                }

                // Get owner
                String ownerStr = BlockStorage.getLocationInfo(loc, "owner");
                if (ownerStr == null) {
                    continue;
                }

                UUID ownerUUID;
                try {
                    ownerUUID = UUID.fromString(ownerStr);
                } catch (Exception e) {
                    continue;
                }

                BlockMenu menu = BlockStorage.getInventory(block);
                if (menu == null) {
                    continue;
                }

                OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
                int itemsSold = sellItemsFromMachine(menu, AutoCollectorMachine.STORAGE_SLOTS, shopHook, vaultHook, owner, loc);
                
                if (itemsSold > 0) {
                    totalItemsSold += itemsSold;
                    machinesProcessed++;
                    plugin.getLogger().info("[AutoSell] Sold " + itemsSold + " items from machine at " + 
                        loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
                }
            }

            // Log summary
            if (machinesProcessed > 0) {
                plugin.getLogger().info("[AutoSell] Task completed - Processed " + machinesProcessed + " machines, sold " + totalItemsSold + " items total");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[AutoSell] Error in auto-sell task", e);
        }
    }

    /**
     * Sell items from a specific machine
     * @return Number of items sold
     */
    private int sellItemsFromMachine(BlockMenu menu, int[] storageSlots, ShopGuiPlusHook shopHook, VaultHook vaultHook, OfflinePlayer owner, Location loc) {
        int itemsSold = 0;
        double totalValue = 0;

        try {
            for (int slot : storageSlots) {
                ItemStack item = menu.getItemInSlot(slot);
                if (item == null || item.getType().isAir()) {
                    continue;
                }

                double price = shopHook.getItemPrice(null, item);
                if (price > 0) {
                    totalValue += price * item.getAmount();
                    itemsSold += item.getAmount();
                    menu.replaceExistingItem(slot, null);
                }
            }

            if (itemsSold > 0 && totalValue > 0) {
                vaultHook.depositMoney(owner, totalValue);
                plugin.getLogger().fine("Auto-sold " + itemsSold + " items from machine at " + 
                    loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + 
                    " for $" + String.format("%.2f", totalValue));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error selling items from machine at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), e);
        }

        return itemsSold;
    }
}
