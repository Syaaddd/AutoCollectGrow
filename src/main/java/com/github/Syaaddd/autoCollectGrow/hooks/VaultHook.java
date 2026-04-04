package com.github.Syaaddd.autoCollectGrow.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

/**
 * Hook for Vault economy integration
 */
public class VaultHook {

    private Economy economy;
    private boolean hooked = false;

    public VaultHook() {
        setupEconomy();
    }

    /**
     * Setup Vault economy connection
     */
    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("[AutoCollectGrow] Vault plugin not found!");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getLogger().warning("[AutoCollectGrow] No economy service found!");
            return;
        }

        economy = rsp.getProvider();
        if (economy != null) {
            hooked = true;
            Bukkit.getLogger().info("[AutoCollectGrow] Vault economy hooked: " + economy.getName());
        } else {
            Bukkit.getLogger().warning("[AutoCollectGrow] Vault economy provider is null!");
        }
    }

    /**
     * Deposit money to a player
     */
    public boolean depositMoney(OfflinePlayer player, double amount) {
        if (!hooked || economy == null) {
            Bukkit.getLogger().warning("[AutoCollectGrow] Economy not available for deposit!");
            return false;
        }

        try {
            economy.depositPlayer(player, amount);
            Bukkit.getLogger().info("[AutoCollectGrow] Deposited " + economy.format(amount) + " to " + player.getName());
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[AutoCollectGrow] Failed to deposit money to " + player.getName(), e);
            return false;
        }
    }

    /**
     * Get player's balance
     */
    public double getBalance(OfflinePlayer player) {
        if (!hooked || economy == null) {
            return 0;
        }

        try {
            return economy.getBalance(player);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Format amount with currency symbol
     */
    public String formatMoney(double amount) {
        if (!hooked || economy == null) {
            return String.format("%.2f", amount);
        }

        return economy.format(amount);
    }

    /**
     * Check if economy is available
     */
    public boolean isHooked() {
        return hooked && economy != null;
    }

    /**
     * Get the economy provider name
     */
    public String getEconomyName() {
        if (!hooked || economy == null) {
            return "Unknown";
        }

        return economy.getName();
    }

    /**
     * Check if economy has the specified amount
     */
    public boolean hasEnough(OfflinePlayer player, double amount) {
        if (!hooked || economy == null) {
            return false;
        }

        return economy.has(player, amount);
    }

    /**
     * Withdraw money from a player
     */
    public boolean withdrawMoney(OfflinePlayer player, double amount) {
        if (!hooked || economy == null) {
            return false;
        }

        try {
            economy.withdrawPlayer(player, amount);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[AutoCollectGrow] Failed to withdraw money from " + player.getName(), e);
            return false;
        }
    }
}
