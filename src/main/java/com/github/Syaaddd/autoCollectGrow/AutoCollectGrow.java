package com.github.Syaaddd.autoCollectGrow;

import com.github.Syaaddd.autoCollectGrow.config.ConfigManager;
import com.github.Syaaddd.autoCollectGrow.hooks.PlaceholderAPIHook;
import com.github.Syaaddd.autoCollectGrow.hooks.ShopGuiPlusHook;
import com.github.Syaaddd.autoCollectGrow.hooks.VaultHook;
import com.github.Syaaddd.autoCollectGrow.items.AutoCollectorTier1;
import com.github.Syaaddd.autoCollectGrow.items.AutoCollectorTier2;
import com.github.Syaaddd.autoCollectGrow.items.AutoCollectorTier3;
import com.github.Syaaddd.autoCollectGrow.items.AutoCollectorTier4;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public final class AutoCollectGrow extends JavaPlugin implements SlimefunAddon {

    private static AutoCollectGrow instance;
    private ConfigManager configManager;
    private VaultHook vaultHook;
    private ShopGuiPlusHook shopGuiPlusHook;
    private PlaceholderAPIHook placeholderAPIHook;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config system
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Check for required dependencies
        if (!getServer().getPluginManager().isPluginEnabled("Slimefun")) {
            getLogger().severe("§cSlimefun is required! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            getLogger().severe("§cVault is required! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize hooks
        setupVaultHook();
        setupShopGuiPlusHook();
        setupPlaceholderAPIHook();

        // Register Slimefun items
        registerItems();

        // Setup bStats metrics
        setupMetrics();

        getLogger().info("AutoCollectGrow has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Unregister placeholders
        if (placeholderAPIHook != null) {
            placeholderAPIHook.unregister();
        }

        // Save data
        if (configManager != null) {
            configManager.saveConfig();
        }

        getLogger().info("AutoCollectGrow has been disabled.");
    }

    private void setupVaultHook() {
        vaultHook = new VaultHook();
        if (!vaultHook.isHooked()) {
            getLogger().warning("Vault economy not found! Selling will not work properly.");
        } else {
            getLogger().info("Vault economy hooked successfully.");
        }
    }

    private void setupShopGuiPlusHook() {
        if (getServer().getPluginManager().isPluginEnabled("ShopGUIPlus")) {
            shopGuiPlusHook = new ShopGuiPlusHook();
            if (shopGuiPlusHook.isHooked()) {
                getLogger().info("ShopGUIPlus hooked successfully.");
            } else {
                getLogger().warning("ShopGUIPlus hook failed! Selling will not work properly.");
            }
        } else {
            getLogger().info("ShopGUIPlus not found. Selling feature will be disabled.");
        }
    }

    private void setupPlaceholderAPIHook() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIHook = new PlaceholderAPIHook(this);
            if (placeholderAPIHook.register()) {
                getLogger().info("PlaceholderAPI hooked successfully.");
            } else {
                getLogger().warning("PlaceholderAPI hook failed!");
            }
        } else {
            getLogger().info("PlaceholderAPI not found. Placeholder features will be disabled.");
        }
    }

    private void registerItems() {
        try {
            // Register all tier AutoCollectors
            new AutoCollectorTier1().register(this);
            new AutoCollectorTier2().register(this);
            new AutoCollectorTier3().register(this);
            new AutoCollectorTier4().register(this);

            getLogger().info("Registered 4 AutoCollector tiers.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register AutoCollector items!", e);
        }
    }

    private void setupMetrics() {
        int pluginId = 22222; // Replace with actual bStats plugin ID
        Metrics metrics = new Metrics(this, pluginId);
        
        // Add custom charts if needed
        metrics.addCustomChart(new org.bstats.charts.SimplePie("tier_used", () -> "unknown"));
    }

    @NotNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return "https://github.com/Syaaddd/AutoCollectGrow/issues";
    }

    // Getters
    public static AutoCollectGrow getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public ShopGuiPlusHook getShopGuiPlusHook() {
        return shopGuiPlusHook;
    }
}
