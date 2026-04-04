package com.github.Syaaddd.autoCollectGrow.gui;

import com.github.Syaaddd.autoCollectGrow.AutoCollectGrow;
import com.github.Syaaddd.autoCollectGrow.hooks.ShopGuiPlusHook;
import com.github.Syaaddd.autoCollectGrow.hooks.VaultHook;
import com.github.Syaaddd.autoCollectGrow.machines.AutoCollectorMachine;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * GUI for the AutoCollector machines
 */
public class ChestCollectorGUI extends BlockMenuPreset {

    // GUI Layout constants
    private static final int SELL_ALL_SLOT = 45;
    private static final int AUTO_SELL_SLOT = 46;
    private static final int INFO_SLOT = 49;
    private static final int UPGRADE_SLOT = 50;
    private static final int HISTORY_SLOT = 53;

    private final AutoCollectorMachine machine;

    public ChestCollectorGUI(String id, String title, AutoCollectorMachine machine) {
        super(id, title);
        this.machine = machine;
    }

    @Override
    public void init() {
        // Add border - prevent taking
        ItemStack glass = new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, "§7");
        
        // Top row
        for (int i = 0; i < 9; i++) {
            addItem(i, glass);
            addMenuClickHandler(i, (p, slot, item, action) -> false);
        }
        // Bottom row
        for (int i = 45; i < 54; i++) {
            addItem(i, glass);
            addMenuClickHandler(i, (p, slot, item, action) -> false);
        }
        // Left and right columns
        for (int i = 9; i < 45; i += 9) {
            addItem(i, glass);
            addMenuClickHandler(i, (p, slot, item, action) -> false);
            addItem(i + 8, glass);
            addMenuClickHandler(i + 8, (p, slot, item, action) -> false);
        }
    }

    public int[] getSlotsAccessedByHopping() {
        // Allow hopping into storage slots only (0-44)
        return machine.getStorageSlots();
    }

    public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
        return getSlotsAccessedByHopping();
    }

    @Override
    public boolean canOpen(@NotNull Block block, @NotNull Player player) {
        // Check permission
        if (!player.hasPermission("autocollect.use")) {
            player.sendMessage("§6[AutoCollect] §cNo permission!");
            return false;
        }
        
        // Admin can always open
        if (player.hasPermission("autocollect.admin")) {
            return true;
        }
        
        // Check if player is owner
        String ownerStr = BlockStorage.getLocationInfo(block.getLocation(), "owner");
        if (ownerStr != null) {
            try {
                UUID ownerUUID = UUID.fromString(ownerStr);
                if (player.getUniqueId().equals(ownerUUID)) {
                    return true;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        
        player.sendMessage("§6[AutoCollect] §cOnly the owner can open this!");
        return false;
    }

    @Override
    public void newInstance(@NotNull BlockMenu menu, @NotNull Block block) {
        // Update control buttons when GUI is opened
        updateControlButtons(menu, block);
    }

    /**
     * Update control buttons
     */
    private void updateControlButtons(BlockMenu menu, Block block) {
        AutoCollectGrow plugin = AutoCollectGrow.getInstance();
        
        // Sell All button
        ItemStack sellAllButton = new CustomItemStack(
            Material.GOLD_INGOT,
            "§6§lSELL ALL",
            "",
            "§7Click to sell all items",
            "§7in storage",
            "",
            "§eLeft-click to sell"
        );
        menu.replaceExistingItem(SELL_ALL_SLOT, sellAllButton);
        menu.addMenuClickHandler(SELL_ALL_SLOT, (p, slot, item, action) -> {
            sellAllItems(p, menu, block);
            return false;
        });

        // Auto-Sell toggle button
        String autoSellStatus = BlockStorage.getLocationInfo(block.getLocation(), "auto-sell");
        boolean autoSellEnabled = "true".equals(autoSellStatus);
        ItemStack autoSellButton = new CustomItemStack(
            autoSellEnabled ? Material.LIME_DYE : Material.RED_DYE,
            "§eAuto-Sell: " + (autoSellEnabled ? "§aEnabled" : "§cDisabled"),
            "",
            "§7Click to toggle auto-sell",
            "§7Current: §e" + plugin.getConfigManager().getAutoSellIntervalMinutes() + " min"
        );
        menu.replaceExistingItem(AUTO_SELL_SLOT, autoSellButton);
        menu.addMenuClickHandler(AUTO_SELL_SLOT, (p, slot, item, action) -> {
            toggleAutoSell(p, block);
            menu.reload();
            return false;
        });

        // Info button
        String tier = BlockStorage.getLocationInfo(block.getLocation(), "tier");
        String radius = BlockStorage.getLocationInfo(block.getLocation(), "radius");
        ItemStack infoButton = new CustomItemStack(
            Material.BOOK,
            "§b§lMachine Info",
            "",
            "§7Tier: §e" + (tier != null ? tier : "?"),
            "§7Radius: §e" + (radius != null ? radius : "?") + " blocks",
            "§7Stored Items: §e" + countStoredItems(menu),
            "",
            "§7Auto-Sell: " + (autoSellEnabled ? "§aOn" : "§cOff")
        );
        menu.replaceExistingItem(INFO_SLOT, infoButton);
        menu.addMenuClickHandler(INFO_SLOT, (p, slot, item, action) -> false);

        // Upgrade button
        int tierNum = tier != null ? Integer.parseInt(tier) : 1;
        if (tierNum < 4) {
            ItemStack upgradeButton = new CustomItemStack(
                Material.EXPERIENCE_BOTTLE,
                "§a§lUpgrade to Tier " + (tierNum + 1),
                "",
                "§7Click to upgrade",
                "§7Cost: Coming soon"
            );
            menu.replaceExistingItem(UPGRADE_SLOT, upgradeButton);
            menu.addMenuClickHandler(UPGRADE_SLOT, (p, slot, item, action) -> {
                p.sendMessage("§6[AutoCollect] §eUpgrade feature coming soon!");
                return false;
            });
        } else {
            ItemStack maxedButton = new CustomItemStack(
                Material.NETHER_STAR,
                "§d§lMAX TIER",
                "",
                "§7Maximum tier reached!"
            );
            menu.replaceExistingItem(UPGRADE_SLOT, maxedButton);
            menu.addMenuClickHandler(UPGRADE_SLOT, (p, slot, item, action) -> false);
        }

        // History button
        ItemStack historyButton = new CustomItemStack(
            Material.CLOCK,
            "§e§lSell History",
            "",
            "§7Click to view history",
            "",
            "§eComing soon!"
        );
        menu.replaceExistingItem(HISTORY_SLOT, historyButton);
        menu.addMenuClickHandler(HISTORY_SLOT, (p, slot, item, action) -> {
            p.sendMessage("§6[AutoCollect] §eSell history coming soon!");
            return false;
        });
    }

    /**
     * Sell all items in storage
     */
    private void sellAllItems(Player player, BlockMenu menu, Block block) {
        AutoCollectGrow plugin = AutoCollectGrow.getInstance();
        ShopGuiPlusHook shopHook = plugin.getShopGuiPlusHook();
        VaultHook vaultHook = plugin.getVaultHook();

        if (shopHook == null || !shopHook.isHooked()) {
            player.sendMessage("§6[AutoCollect] §cShopGUIPlus not available! Install ShopGUIPlus first.");
            return;
        }

        if (vaultHook == null || !vaultHook.isHooked()) {
            player.sendMessage("§6[AutoCollect] §cVault economy not available!");
            return;
        }

        // Get owner
        String ownerStr = BlockStorage.getLocationInfo(block.getLocation(), "owner");
        if (ownerStr == null) {
            player.sendMessage("§6[AutoCollect] §cNo owner found for this machine!");
            return;
        }

        UUID ownerUUID;
        try {
            ownerUUID = UUID.fromString(ownerStr);
        } catch (Exception e) {
            player.sendMessage("§6[AutoCollect] §cInvalid owner data!");
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);

        // Calculate total value and sell items
        double totalValue = 0;
        int itemsSold = 0;

        for (int slot : machine.getStorageSlots()) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            double price = shopHook.getItemPrice(item);
            if (price > 0) {
                totalValue += price * item.getAmount();
                itemsSold += item.getAmount();
                // Clear the slot
                menu.replaceExistingItem(slot, null);
            }
        }

        if (totalValue <= 0 || itemsSold == 0) {
            player.sendMessage("§6[AutoCollect] §cNo items to sell! Put items in storage first.");
            return;
        }

        // Deposit money to owner
        if (vaultHook.depositMoney(owner, totalValue)) {
            player.sendMessage("§6[AutoCollect] §aSold " + itemsSold + " items for " + 
                vaultHook.formatMoney(totalValue) + "!");
            
            // Play sound
            if (plugin.getConfigManager().isSoundsEnabled()) {
                player.playSound(player.getLocation(), "ENTITY_VILLAGER_YES", 1.0f, 1.0f);
            }
        } else {
            player.sendMessage("§6[AutoCollect] §cFailed to deposit money!");
        }
    }

    /**
     * Toggle auto-sell mode
     */
    private void toggleAutoSell(Player player, Block block) {
        String currentStatus = BlockStorage.getLocationInfo(block.getLocation(), "auto-sell");
        boolean enabled = "true".equals(currentStatus);
        
        BlockStorage.addBlockInfo(block, "auto-sell", String.valueOf(!enabled));
        
        AutoCollectGrow plugin = AutoCollectGrow.getInstance();
        if (!enabled) {
            player.sendMessage("§6[AutoCollect] §aAuto-sell enabled! Selling every " + 
                plugin.getConfigManager().getAutoSellIntervalMinutes() + " minutes.");
        } else {
            player.sendMessage("§6[AutoCollect] §cAuto-sell disabled.");
        }
    }

    /**
     * Count stored items
     */
    private int countStoredItems(BlockMenu menu) {
        int count = 0;
        for (int slot : machine.getStorageSlots()) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item != null) {
                count += item.getAmount();
            }
        }
        return count;
    }
}
