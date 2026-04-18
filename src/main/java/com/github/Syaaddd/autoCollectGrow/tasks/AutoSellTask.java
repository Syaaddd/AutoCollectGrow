package com.github.Syaaddd.autoCollectGrow.tasks;

import com.github.Syaaddd.autoCollectGrow.AutoCollectGrow;
import com.github.Syaaddd.autoCollectGrow.hooks.ShopGuiPlusHook;
import com.github.Syaaddd.autoCollectGrow.hooks.VaultHook;
import com.github.Syaaddd.autoCollectGrow.machines.AutoCollectorMachine;
import com.github.Syaaddd.autoCollectGrow.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
                return;
            }

            VaultHook vaultHook = plugin.getVaultHook();
            if (vaultHook == null || !vaultHook.isHooked()) {
                return;
            }

            // ShopGUIPlus REQUIRES a Player object for permission checks
            // Skip auto-sell if no players are online
            Player referencePlayer = findOnlinePlayer();
            if (referencePlayer == null) {
                plugin.getLogger().fine("[AutoSell] No players online - skipping auto-sell (ShopGUIPlus requires online player for pricing)");
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

                OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
                BlockMenu menu = BlockStorage.getInventory(block);
                if (menu == null) {
                    continue;
                }

                int itemsSold = sellItemsFromMachine(menu, AutoCollectorMachine.STORAGE_SLOTS, shopHook, vaultHook, owner, loc, referencePlayer);
                
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
     * Find any online player to use as reference for ShopGUIPlus pricing
     */
    private Player findOnlinePlayer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            return player; // Just return the first online player
        }
        return null; // No players online
    }

    /**
     * Sell items from a specific machine
     * @return Number of items sold
     */
    private int sellItemsFromMachine(BlockMenu menu, int[] storageSlots, ShopGuiPlusHook shopHook, VaultHook vaultHook, OfflinePlayer owner, Location loc, Player referencePlayer) {
        int itemsSold = 0;
        int slimefunItemsSkipped = 0;
        double totalValue = 0;
        boolean protectSlimefunItems = !plugin.getConfigManager().canSellSlimefunItems();

        try {
            for (int slot : storageSlots) {
                ItemStack item = menu.getItemInSlot(slot);
                if (item == null || item.getType().isAir()) {
                    continue;
                }

                // Skip Slimefun items if protection is enabled (default)
                if (protectSlimefunItems && ItemUtils.isSlimefunItem(item)) {
                    slimefunItemsSkipped += item.getAmount();
                    plugin.getLogger().fine("[AutoSell] Skipped Slimefun item: " + ItemUtils.getSlimefunItemId(item) + 
                        " (amount: " + item.getAmount() + ") at slot " + slot);
                    continue;
                }

                // getItemPrice returns the unit price (for 1 item).
                // Multiply by amount to get the total for this slot, same as /sell hand.
                double unitPrice = shopHook.getItemPrice(referencePlayer, item);
                if (unitPrice > 0) {
                    totalValue += unitPrice * item.getAmount();
                    itemsSold += item.getAmount();
                    menu.replaceExistingItem(slot, null);
                }
            }

            // Log skipped Slimefun items
            if (slimefunItemsSkipped > 0) {
                plugin.getLogger().info("[AutoSell] Skipped " + slimefunItemsSkipped + 
                    " Slimefun item(s) from machine at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
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
