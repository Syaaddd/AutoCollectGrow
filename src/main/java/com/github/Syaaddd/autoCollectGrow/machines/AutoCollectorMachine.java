package com.github.Syaaddd.autoCollectGrow.machines;

import com.github.Syaaddd.autoCollectGrow.AutoCollectGrow;
import com.github.Syaaddd.autoCollectGrow.gui.ChestCollectorGUI;
import com.github.Syaaddd.autoCollectGrow.tasks.CollectorTask;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for all AutoCollector machines
 */
public abstract class AutoCollectorMachine extends SlimefunItem {

    protected final int tier;
    protected final int radius;
    
    // Track active collectors: location -> task
    private static final Map<Location, CollectorTask> activeCollectors = new ConcurrentHashMap<>();
    // Track owners: location -> player UUID
    private static final Map<Location, UUID> owners = new ConcurrentHashMap<>();

    // Storage slots (middle area excluding borders and control buttons)
    // Rows 2-5: slots 10-16, 19-25, 28-34, 37-43 (7 slots x 4 rows = 28 slots)
    protected static final int[] STORAGE_SLOTS;
    static {
        STORAGE_SLOTS = new int[28];
        int idx = 0;
        // Row 2: 10-16
        for (int i = 10; i <= 16; i++) STORAGE_SLOTS[idx++] = i;
        // Row 3: 19-25
        for (int i = 19; i <= 25; i++) STORAGE_SLOTS[idx++] = i;
        // Row 4: 28-34
        for (int i = 28; i <= 34; i++) STORAGE_SLOTS[idx++] = i;
        // Row 5: 37-43
        for (int i = 37; i <= 43; i++) STORAGE_SLOTS[idx++] = i;
    }

    public AutoCollectorMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, 
                                 ItemStack[] recipe, int tier, int radius) {
        super(itemGroup, item, recipeType, recipe);
        
        this.tier = tier;
        this.radius = radius;
        
        // Add handlers
        addItemHandler(onPlace());
        addItemHandler(onBreak());
        
        // Register GUI
        registerGUI();
    }

    /**
     * Register the GUI for this machine
     */
    private void registerGUI() {
        new ChestCollectorGUI(getId(), getItem().getItemMeta().getDisplayName(), this);
    }

    @Nonnull
    private BlockPlaceHandler onPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@NotNull BlockPlaceEvent event) {
                Block block = event.getBlockPlaced();
                Player player = event.getPlayer();
                
                // Save owner
                UUID ownerUUID = player.getUniqueId();
                owners.put(block.getLocation(), ownerUUID);
                BlockStorage.addBlockInfo(block, "owner", ownerUUID.toString());
                BlockStorage.addBlockInfo(block, "tier", String.valueOf(tier));
                BlockStorage.addBlockInfo(block, "radius", String.valueOf(radius));
                BlockStorage.addBlockInfo(block, "auto-sell", "false");
                
                // Initialize storage slots
                BlockMenu menu = BlockStorage.getInventory(block);
                if (menu != null) {
                    for (int slot : STORAGE_SLOTS) {
                        menu.replaceExistingItem(slot, null);
                    }
                }
                
                // Start collection task
                startCollection(block);
                
                player.sendMessage("§6[AutoCollect] §aAutoCollector placed! Starting collection...");
            }

            public @NotNull String getName() {
                return "AutoCollectorPlace";
            }
        };
    }

    @Nonnull
    private BlockBreakHandler onBreak() {
        return new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack item, @NotNull java.util.List<ItemStack> drops) {
                Block block = event.getBlock();

                // Stop collection task
                stopCollection(block);

                // Remove owner
                owners.remove(block.getLocation());

                // Drop ONLY stored items from storage slots (not border/control buttons)
                BlockMenu menu = BlockStorage.getInventory(block);
                if (menu != null) {
                    for (int slot : STORAGE_SLOTS) {
                        ItemStack stored = menu.getItemInSlot(slot);
                        if (stored != null && !stored.getType().isAir()) {
                            block.getWorld().dropItemNaturally(block.getLocation(), stored);
                            menu.replaceExistingItem(slot, null);
                        }
                    }
                }
            }
        };
    }

    /**
     * Start the collection task for this block
     */
    public void startCollection(Block block) {
        // Cancel existing task if any
        stopCollection(block);
        
        // Create and start new task
        CollectorTask task = new CollectorTask(
            AutoCollectGrow.getInstance(),
            this,
            block,
            radius
        );
        task.runTaskTimer(AutoCollectGrow.getInstance(), 20L, 
            (long) AutoCollectGrow.getInstance().getConfigManager().getScanIntervalTicks());
        
        activeCollectors.put(block.getLocation(), task);
        
        AutoCollectGrow.getInstance().getLogger().info("Started collection at " + 
            block.getLocation().getBlockX() + "," + 
            block.getLocation().getBlockY() + "," + 
            block.getLocation().getBlockZ() + " radius=" + radius);
    }

    /**
     * Stop the collection task for this block
     */
    public void stopCollection(Block block) {
        CollectorTask task = activeCollectors.remove(block.getLocation());
        if (task != null) {
            task.cancel();
            AutoCollectGrow.getInstance().getLogger().info("Stopped collection at " + 
                block.getLocation().getBlockX() + "," + 
                block.getLocation().getBlockY() + "," + 
                block.getLocation().getBlockZ());
        }
    }

    /**
     * Get the owner of this machine
     */
    public UUID getOwner(Block block) {
        // Try from memory first
        UUID owner = owners.get(block.getLocation());
        if (owner != null) {
            return owner;
        }
        
        // Load from BlockStorage
        String ownerStr = BlockStorage.getLocationInfo(block.getLocation(), "owner");
        if (ownerStr != null && !ownerStr.isEmpty()) {
            try {
                return UUID.fromString(ownerStr);
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }

    /**
     * Get the scan radius for this tier
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Get the tier number
     */
    public int getTier() {
        return tier;
    }

    /**
     * Get energy capacity for this tier
     */
    public int getCapacity() {
        return tier * 1000;
    }

    /**
     * Get energy consumption per tick
     */
    public int getEnergyConsumption() {
        return tier * 10;
    }

    /**
     * Get the display name for this tier
     */
    @Nonnull
    public abstract String getTierName();
    
    /**
     * Check if this machine is currently active
     */
    public boolean isActive(Block block) {
        return activeCollectors.containsKey(block.getLocation());
    }
    
    /**
     * Get storage slots
     */
    public int[] getStorageSlots() {
        return STORAGE_SLOTS;
    }
}
