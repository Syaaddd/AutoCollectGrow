package com.github.Syaaddd.autoCollectGrow.tasks;

import com.github.Syaaddd.autoCollectGrow.AutoCollectGrow;
import com.github.Syaaddd.autoCollectGrow.machines.AutoCollectorMachine;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Task that scans nearby containers and collects items
 */
public class CollectorTask extends BukkitRunnable {

    private final AutoCollectGrow plugin;
    private final AutoCollectorMachine machine;
    private final Block machineBlock;
    private final int radius;
    
    // Track chunks that need to stay loaded
    private final Set<Chunk> loadedChunks = new HashSet<>();

    public CollectorTask(AutoCollectGrow plugin, AutoCollectorMachine machine, Block machineBlock, int radius) {
        this.plugin = plugin;
        this.machine = machine;
        this.machineBlock = machineBlock;
        this.radius = radius;
    }

    @Override
    public void run() {
        try {
            // Check if machine still exists
            if (machineBlock == null || machineBlock.getType().isAir()) {
                plugin.getLogger().info("Machine block is air, cancelling task");
                cancel();
                return;
            }

            // Verify it's still the same machine type
            String blockId = BlockStorage.getLocationInfo(machineBlock.getLocation(), "id");
            if (blockId == null || !blockId.equals(machine.getId())) {
                plugin.getLogger().info("Machine ID mismatch, cancelling task. Expected: " + machine.getId() + ", Got: " + blockId);
                cancel();
                return;
            }
            
            // Check if machine is enabled
            String enabled = BlockStorage.getLocationInfo(machineBlock.getLocation(), "enabled");
            if ("false".equals(enabled)) {
                // Machine is disabled, skip collection
                return;
            }
            
            // Consume base energy every tick (machine running cost)
            int baseEnergy = machine.getEnergyConsumption();
            if (!consumeEnergy(baseEnergy)) {
                // Not enough energy, skip this tick
                plugin.getLogger().fine("Not enough energy to run machine. Skipping tick.");
                return;
            }

            // Ensure chunks are loaded before collecting
            ensureChunksLoaded();

            collectItems();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error in collector task", e);
        }
    }

    /**
     * Consume energy from the machine
     * @param energyNeeded Amount of energy to consume
     * @return true if energy was successfully consumed, false if not enough energy
     */
    private boolean consumeEnergy(int energyNeeded) {
        try {
            Location loc = machineBlock.getLocation();
            
            // Get current charge from BlockStorage
            String chargeStr = BlockStorage.getLocationInfo(loc, "energy-charge");
            int currentCharge = chargeStr != null ? Integer.parseInt(chargeStr) : 0;
            
            // Check if machine has enough energy
            if (currentCharge < energyNeeded) {
                // Not enough energy
                return false;
            }
            
            // Consume energy
            int newCharge = currentCharge - energyNeeded;
            BlockStorage.addBlockInfo(loc, "energy-charge", String.valueOf(newCharge));
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error consuming energy", e);
            return false;
        }
    }

    /**
     * Ensure all chunks within radius are loaded
     */
    private void ensureChunksLoaded() {
        Location center = machineBlock.getLocation();
        int chunkRadius = (int) Math.ceil((double) radius / 16.0); // Convert block radius to chunk radius

        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                int chunkX = center.getBlockX() / 16 + x;
                int chunkZ = center.getBlockZ() / 16 + z;
                
                Chunk chunk = center.getWorld().getChunkAt(chunkX, chunkZ);
                
                // Force chunk to stay loaded (persistent chunk loading)
                if (!loadedChunks.contains(chunk)) {
                    try {
                        // Try Paper's addPluginChunkTicket first (best method)
                        chunk.addPluginChunkTicket(plugin);
                        loadedChunks.add(chunk);
                        plugin.getLogger().fine("Added plugin chunk ticket at " + chunkX + "," + chunkZ);
                    } catch (NoSuchMethodError | AbstractMethodError e) {
                        // Fallback to setForceLoaded for Spigot
                        try {
                            chunk.setForceLoaded(true);
                            loadedChunks.add(chunk);
                            plugin.getLogger().fine("Force loaded chunk at " + chunkX + "," + chunkZ);
                        } catch (Exception ex) {
                            // Last resort: just load it temporarily
                            chunk.load();
                            loadedChunks.add(chunk);
                            plugin.getLogger().fine("Loaded chunk at " + chunkX + "," + chunkZ);
                        }
                    }
                }
            }
        }
    }

    /**
     * Release all chunk tickets when task is cancelled
     */
    @Override
    public void cancel() {
        // Release all plugin chunk tickets
        for (Chunk chunk : loadedChunks) {
            try {
                chunk.removePluginChunkTicket(plugin);
                plugin.getLogger().fine("Removed plugin chunk ticket");
            } catch (Exception e) {
                try {
                    chunk.setForceLoaded(false);
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
        int ticketCount = loadedChunks.size();
        loadedChunks.clear();
        
        plugin.getLogger().info("Collector task cancelled, released " + ticketCount + " chunk tickets");
        super.cancel();
    }

    /**
     * Main collection logic
     */
    private void collectItems() {
        Location center = machineBlock.getLocation();
        int maxItems = machine.getMaxItemsPerScan();
        int collected = 0;

        // Get the machine's BlockMenu
        BlockMenu machineMenu = BlockStorage.getInventory(machineBlock);
        if (machineMenu == null) {
            plugin.getLogger().warning("BlockMenu is null for machine at " + center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ());
            return;
        }

        // Collect dropped items from the ground in radius
        int allowedForDropped = maxItems > 0 ? maxItems : Integer.MAX_VALUE;
        collected += collectDroppedItems(center, machineMenu, allowedForDropped);

        // Stop if limit already reached
        if (maxItems > 0 && collected >= maxItems) {
            plugin.getLogger().fine("Scan limit reached (" + maxItems + ") after collecting dropped items");
            return;
        }

        // Scan blocks in sphere radius and collect from containers
        int minX = center.getBlockX() - radius;
        int maxX = center.getBlockX() + radius;
        int minY = Math.max(0, center.getBlockY() - radius);
        int maxY = Math.min(255, center.getBlockY() + radius);
        int minZ = center.getBlockZ() - radius;
        int maxZ = center.getBlockZ() + radius;

        int containersScanned = 0;

        outerLoop:
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    // Check if block is within sphere
                    double distance = Math.sqrt(
                        Math.pow(x - center.getBlockX(), 2) +
                        Math.pow(y - center.getBlockY(), 2) +
                        Math.pow(z - center.getBlockZ(), 2)
                    );

                    if (distance > radius) {
                        continue;
                    }

                    Location loc = new Location(center.getWorld(), x, y, z);
                    Block block = loc.getBlock();

                    // Skip the machine block itself
                    if (block.equals(machineBlock)) {
                        continue;
                    }

                    // Check if block is a container
                    BlockState state = block.getState();
                    if (state instanceof Container container) {
                        containersScanned++;
                        int remaining = maxItems > 0 ? maxItems - collected : Integer.MAX_VALUE;
                        collected += collectFromContainer(container, machineMenu, remaining);

                        // Stop scanning if limit reached
                        if (maxItems > 0 && collected >= maxItems) {
                            plugin.getLogger().fine("Scan limit reached (" + maxItems + ") during container scan");
                            break outerLoop;
                        }
                    }
                }
            }
        }

        if (containersScanned > 0 || collected > 0) {
            plugin.getLogger().fine("Scanned " + containersScanned + " containers, collected " + collected + (maxItems > 0 ? "/" + maxItems : "") + " items");
        }
    }

    /**
     * Collect dropped items from the ground in radius, up to maxAllowed items
     */
    private int collectDroppedItems(Location center, BlockMenu machineMenu, int maxAllowed) {
        int collected = 0;
        double radiusSquared = radius * radius;

        Collection<Entity> nearbyEntities;
        try {
            nearbyEntities = center.getWorld().getNearbyEntities(
                center,
                radius,
                radius,
                radius,
                entity -> entity instanceof Item
            );
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting nearby entities", e);
            return 0;
        }

        if (nearbyEntities == null || nearbyEntities.isEmpty()) {
            return 0;
        }

        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Item itemEntity)) {
                continue;
            }

            double distanceSquared = entity.getLocation().distanceSquared(center);
            if (distanceSquared > radiusSquared) {
                continue;
            }

            ItemStack itemStack = itemEntity.getItemStack();
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }

            String materialName = itemStack.getType().name();
            if (!plugin.getConfigManager().isItemAllowed(materialName)) {
                continue;
            }

            // Limit how many items we take from this stack
            int canTake = Math.min(itemStack.getAmount(), maxAllowed - collected);
            ItemStack toCollect = canTake < itemStack.getAmount() ? itemStack.asQuantity(canTake) : itemStack;

            ItemStack remaining = machineMenu.pushItem(toCollect, machine.getStorageSlots());

            if (remaining == null) {
                // All of toCollect was stored
                collected += canTake;
                if (canTake == itemStack.getAmount()) {
                    itemEntity.remove();
                } else {
                    itemStack.setAmount(itemStack.getAmount() - canTake);
                    itemEntity.setItemStack(itemStack);
                }
            } else if (remaining.getAmount() < toCollect.getAmount()) {
                // Partial storage
                int moved = toCollect.getAmount() - remaining.getAmount();
                collected += moved;
                itemStack.setAmount(itemStack.getAmount() - moved);
                if (itemStack.getAmount() <= 0) {
                    itemEntity.remove();
                } else {
                    itemEntity.setItemStack(itemStack);
                }
            }

            if (collected >= maxAllowed) {
                break;
            }
        }

        return collected;
    }

    /**
     * Collect items from a single container into machine storage, up to maxAllowed items
     */
    private int collectFromContainer(Container sourceContainer, BlockMenu machineMenu, int maxAllowed) {
        int collected = 0;
        Inventory sourceInventory = sourceContainer.getInventory();

        for (int i = 0; i < sourceInventory.getSize(); i++) {
            ItemStack item = sourceInventory.getItem(i);

            if (item == null || item.getType().isAir()) {
                continue;
            }

            String materialName = item.getType().name();
            if (!plugin.getConfigManager().isItemAllowed(materialName)) {
                continue;
            }

            // Limit how many items we take from this slot
            int canTake = Math.min(item.getAmount(), maxAllowed - collected);
            ItemStack toCollect = canTake < item.getAmount() ? item.asQuantity(canTake) : item;

            ItemStack remaining = machineMenu.pushItem(toCollect, machine.getStorageSlots());

            if (remaining == null) {
                // All of toCollect was stored
                collected += canTake;
                if (canTake == item.getAmount()) {
                    sourceInventory.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - canTake);
                    sourceInventory.setItem(i, item);
                }
            } else if (remaining.getAmount() < toCollect.getAmount()) {
                // Partial storage
                int moved = toCollect.getAmount() - remaining.getAmount();
                collected += moved;
                item.setAmount(item.getAmount() - moved);
                sourceInventory.setItem(i, item.getAmount() > 0 ? item : null);
            } else {
                // Machine storage is full, stop
                break;
            }

            if (collected >= maxAllowed) {
                break;
            }
        }

        return collected;
    }
}
