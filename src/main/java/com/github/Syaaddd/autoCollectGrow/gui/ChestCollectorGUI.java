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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
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

        // Top row (0-8)
        for (int i = 0; i < 9; i++) {
            addItem(i, glass);
            addMenuClickHandler(i, (p, slot, item, action) -> false);
        }
        
        // Bottom row (45-53) - EXCLUDING control button slots
        // Control buttons at: 45, 46, 49, 50, 53
        // Border only at: 47, 48, 51, 52
        int[] bottomBorderSlots = {47, 48, 51, 52};
        for (int slot : bottomBorderSlots) {
            addItem(slot, glass);
            addMenuClickHandler(slot, (p, slot2, item, action) -> false);
        }
        
        // Left and right columns (excluding top and bottom rows)
        // Left: 9, 18, 27, 36
        // Right: 17, 26, 35, 44
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
        
        // Spawn ambient particles if machine is enabled
        String machineStatus = BlockStorage.getLocationInfo(block.getLocation(), "enabled");
        boolean isEnabled = !"false".equals(machineStatus);
        if (isEnabled) {
            spawnAmbientParticles(block.getLocation());
        }
    }

    /**
     * Spawn ambient particles around active machine
     */
    private void spawnAmbientParticles(Location location) {
        // Spawn a few happy villager particles around the machine
        Location center = location.clone().add(0.5, 1.0, 0.5);
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            double x = Math.cos(angle) * 0.6;
            double z = Math.sin(angle) * 0.6;
            double y = 0.3 + (Math.random() * 0.5);
            
            location.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                center.clone().add(x, y, z),
                1,
                0.1, 0.1, 0.1,
                0.01
            );
        }
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
        String machineStatus = BlockStorage.getLocationInfo(block.getLocation(), "enabled");
        boolean machineEnabled = !"false".equals(machineStatus);
        String energyCharge = BlockStorage.getLocationInfo(block.getLocation(), "energy-charge");
        int charge = energyCharge != null ? Integer.parseInt(energyCharge) : 0;
        int maxCapacity = machine.getCapacity();
        int consumption = machine.getEnergyConsumption();
        
        ItemStack infoButton = new CustomItemStack(
            Material.BOOK,
            "§b§lMachine Info",
            "",
            "§7Tier: §e" + (tier != null ? tier : "?"),
            "§7Radius: §e" + (radius != null ? radius : "?") + " blocks",
            "§7Stored Items: §e" + countStoredItems(menu),
            "",
            "§7Status: " + (machineEnabled ? "§aActive" : "§cInactive"),
            "§7Energy: §e" + charge + " / " + maxCapacity + " ⚡",
            "§7Consumption: §e" + consumption + " ⚡/tick",
            "§7Auto-Sell: " + (autoSellEnabled ? "§aOn" : "§cOff")
        );
        menu.replaceExistingItem(INFO_SLOT, infoButton);
        menu.addMenuClickHandler(INFO_SLOT, (p, slot, item, action) -> false);

        // Machine ON/OFF toggle button (replaces upgrade button)
        ItemStack powerButton = new CustomItemStack(
            machineEnabled ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK,
            machineEnabled ? "§a§lMACHINE: ON" : "§c§lMACHINE: OFF",
            "",
            "§7Click to toggle machine power",
            "",
            machineEnabled ? "§e● Currently Active" : "§8○ Currently Inactive",
            "§7Collecting items: " + (machineEnabled ? "§aYes" : "§cNo")
        );
        menu.replaceExistingItem(UPGRADE_SLOT, powerButton);
        menu.addMenuClickHandler(UPGRADE_SLOT, (p, slot, item, action) -> {
            toggleMachinePower(p, block);
            menu.reload();
            return false;
        });

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

            double price = shopHook.getItemPrice(player, item);
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
     * Toggle machine power (on/off)
     */
    private void toggleMachinePower(Player player, Block block) {
        String currentStatus = BlockStorage.getLocationInfo(block.getLocation(), "enabled");
        boolean enabled = !"false".equals(currentStatus); // Default to enabled

        BlockStorage.addBlockInfo(block, "enabled", String.valueOf(!enabled));

        AutoCollectGrow plugin = AutoCollectGrow.getInstance();
        if (enabled) {
            // Machine turned OFF
            player.sendMessage("§6[AutoCollect] §cMachine turned OFF! Collection stopped.");
            
            // Stop the collector task
            machine.stopCollection(block);
            
            // Spawn redstone particles (off effect)
            spawnPowerParticles(block.getLocation(), false);
        } else {
            // Machine turned ON
            player.sendMessage("§6[AutoCollect] §aMachine turned ON! Collection started.");
            
            // Start the collector task
            machine.startCollection(block);
            
            // Spawn emerald particles (on effect)
            spawnPowerParticles(block.getLocation(), true);
        }
    }

    /**
     * Spawn power particles around the machine
     */
    private void spawnPowerParticles(Location location, boolean isOn) {
        Particle particle = isOn ? Particle.HAPPY_VILLAGER : Particle.SMOKE;
        Location particleLoc = location.clone().add(0.5, 1.2, 0.5);
        
        // Spawn particles in a circle around the block
        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI * i) / 20;
            double x = Math.cos(angle) * 0.8;
            double z = Math.sin(angle) * 0.8;
            
            Location spawnLoc = particleLoc.clone().add(x, 0, z);
            location.getWorld().spawnParticle(
                particle,
                spawnLoc,
                1,
                0.1, 0.1, 0.1,
                0.02
            );
        }
        
        // Spawn vertical particles
        for (int i = 0; i < 10; i++) {
            location.getWorld().spawnParticle(
                particle,
                particleLoc.clone().add(0, i * 0.15, 0),
                1,
                0.2, 0.1, 0.2,
                0.02
            );
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
