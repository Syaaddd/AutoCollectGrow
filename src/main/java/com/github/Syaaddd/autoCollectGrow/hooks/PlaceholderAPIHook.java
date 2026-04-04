package com.github.Syaaddd.autoCollectGrow.hooks;

import com.github.Syaaddd.autoCollectGrow.AutoCollectGrow;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for AutoCollectGrow
 * Provides custom placeholders for displaying AutoCollect stats
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final AutoCollectGrow plugin;

    public PlaceholderAPIHook(AutoCollectGrow plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "autocollect";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Syaaddd";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // Keep this expansion registered even if plugin reloads
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        switch (params.toLowerCase()) {
            case "total_profit":
                return getTotalProfit(player);
            
            case "total_items_collected":
                return getTotalItemsCollected(player);
            
            case "total_items_sold":
                return getTotalItemsSold(player);
            
            case "current_tier":
                return getCurrentTier(player);
            
            case "machine_radius":
                return getMachineRadius(player);
            
            case "auto_sell_status":
                return getAutoSellStatus(player);
            
            case "vault_balance":
                return getVaultBalance(player);
            
            case "economy_name":
                return getEconomyName();
            
            default:
                return null; // Placeholder not found
        }
    }

    /**
     * Get total profit placeholder
     * %autocollect_total_profit%
     */
    private String getTotalProfit(OfflinePlayer player) {
        // This would load from stored data
        // For now, return a placeholder value
        if (plugin.getVaultHook() != null && plugin.getVaultHook().isHooked()) {
            return plugin.getVaultHook().formatMoney(0.0);
        }
        return "0.00";
    }

    /**
     * Get total items collected placeholder
     * %autocollect_total_items_collected%
     */
    private String getTotalItemsCollected(OfflinePlayer player) {
        // Load from BlockStorage or database
        return "0";
    }

    /**
     * Get total items sold placeholder
     * %autocollect_total_items_sold%
     */
    private String getTotalItemsSold(OfflinePlayer player) {
        // Load from BlockStorage or database
        return "0";
    }

    /**
     * Get current machine tier placeholder
     * %autocollect_current_tier%
     */
    private String getCurrentTier(OfflinePlayer player) {
        // Load from player's placed machines
        return "None";
    }

    /**
     * Get machine radius placeholder
     * %autocollect_machine_radius%
     */
    private String getMachineRadius(OfflinePlayer player) {
        // Load from player's current machine
        return "0";
    }

    /**
     * Get auto-sell status placeholder
     * %autocollect_auto_sell_status%
     */
    private String getAutoSellStatus(OfflinePlayer player) {
        boolean enabled = plugin.getConfigManager().isAutoSellEnabled();
        return enabled ? "&aEnabled" : "&cDisabled";
    }

    /**
     * Get vault balance placeholder
     * %autocollect_vault_balance%
     */
    private String getVaultBalance(OfflinePlayer player) {
        if (plugin.getVaultHook() != null && plugin.getVaultHook().isHooked()) {
            return plugin.getVaultHook().formatMoney(plugin.getVaultHook().getBalance(player));
        }
        return "N/A";
    }

    /**
     * Get economy name placeholder
     * %autocollect_economy_name%
     */
    private String getEconomyName() {
        if (plugin.getVaultHook() != null && plugin.getVaultHook().isHooked()) {
            return plugin.getVaultHook().getEconomyName();
        }
        return "N/A";
    }
}
