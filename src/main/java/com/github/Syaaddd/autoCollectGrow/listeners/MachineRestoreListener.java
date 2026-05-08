package com.github.Syaaddd.autoCollectGrow.listeners;

import com.github.Syaaddd.autoCollectGrow.AutoCollectGrow;
import com.github.Syaaddd.autoCollectGrow.machines.AutoCollectorMachine;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 * Restores AutoCollector machine state (tasks + registry) when chunks are loaded.
 * This is necessary because the in-memory machine maps are cleared on server restart.
 */
public class MachineRestoreListener implements Listener {

    private final AutoCollectGrow plugin;

    public MachineRestoreListener(AutoCollectGrow plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        // Delay 1 tick so BlockStorage finishes loading chunk data before we read it
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            scanChunkForMachines(event.getChunk());
        }, 1L);
    }

    /**
     * Scans a chunk for AutoCollector machines and restores them.
     * Safe to call multiple times for the same chunk (duplicates are ignored).
     */
    public void scanChunkForMachines(Chunk chunk) {
        for (BlockState tileEntity : chunk.getTileEntities()) {
            Block block = tileEntity.getBlock();
            try {
                String id = BlockStorage.getLocationInfo(block.getLocation(), "id");
                if (AutoCollectorMachine.isAutoCollectorId(id)) {
                    AutoCollectorMachine.restoreMachineFromStorage(block, id);
                }
            } catch (Exception e) {
                plugin.getLogger().fine("Error scanning tile entity during chunk restore: " + e.getMessage());
            }
        }
    }
}
