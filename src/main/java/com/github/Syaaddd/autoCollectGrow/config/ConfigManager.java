package com.github.Syaaddd.autoCollectGrow.config;

import com.github.Syaaddd.autoCollectGrow.AutoCollectGrow;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final AutoCollectGrow plugin;
    private FileConfiguration config;
    private File configFile;

    // Configuration defaults
    private final Map<String, Integer> tierRadius = new HashMap<>();
    private int scanIntervalTicks;
    private boolean autoSellEnabled;
    private int autoSellIntervalSeconds;
    private List<String> itemWhitelist;
    private List<String> itemBlacklist;
    private boolean enableSellHistory;
    private int maxSellHistorySize;
    private boolean enableSounds;
    private boolean enableMessages;
    private boolean sellSlimefunItems;

    public ConfigManager(AutoCollectGrow plugin) {
        this.plugin = plugin;
        setDefaults();
    }

    private void setDefaults() {
        // Default tier radius
        tierRadius.put("tier1", 5);
        tierRadius.put("tier2", 10);
        tierRadius.put("tier3", 20);
        tierRadius.put("tier4", 50);

        // Default settings
        scanIntervalTicks = 100; // 5 seconds
        autoSellEnabled = false;
        autoSellIntervalSeconds = 300; // 5 minutes
        enableSellHistory = true;
        maxSellHistorySize = 50;
        enableSounds = true;
        enableMessages = true;
        sellSlimefunItems = false; // Protect Slimefun items by default

        // Default item lists
        itemWhitelist = List.of("*"); // All items by default
        itemBlacklist = List.of(
            "BEDROCK",
            "BARRIER",
            "COMMAND_BLOCK",
            "STRUCTURE_BLOCK",
            "JIGSAW"
        );
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Load values from config
        loadConfiguration();
    }

    private void loadConfiguration() {
        // Tier radius settings
        for (Map.Entry<String, Integer> entry : tierRadius.entrySet()) {
            tierRadius.put(entry.getKey(), config.getInt("settings.tier-radius." + entry.getKey(), entry.getValue()));
        }

        // Scan settings
        scanIntervalTicks = config.getInt("settings.scan-interval-ticks", scanIntervalTicks);
        
        // Auto-sell settings
        autoSellEnabled = config.getBoolean("settings.auto-sell.enabled", autoSellEnabled);
        autoSellIntervalSeconds = config.getInt("settings.auto-sell.interval-seconds", autoSellIntervalSeconds);
        
        // Item filters
        itemWhitelist = config.getStringList("settings.item-whitelist");
        itemBlacklist = config.getStringList("settings.item-blacklist");
        
        // Sell history
        enableSellHistory = config.getBoolean("settings.sell-history.enabled", enableSellHistory);
        maxSellHistorySize = config.getInt("settings.sell-history.max-size", maxSellHistorySize);
        
        // Client settings
        enableSounds = config.getBoolean("settings.client.sounds", enableSounds);
        enableMessages = config.getBoolean("settings.client.messages", enableMessages);

        // Selling settings
        sellSlimefunItems = config.getBoolean("settings.selling.sell-slimefun-items", sellSlimefunItems);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }

    // Getters
    public int getTierRadius(String tier) {
        return tierRadius.getOrDefault(tier, 5);
    }

    public int getScanIntervalTicks() {
        return scanIntervalTicks;
    }

    public boolean isAutoSellEnabled() {
        return autoSellEnabled;
    }

    public int getAutoSellIntervalSeconds() {
        return autoSellIntervalSeconds;
    }

    public List<String> getItemWhitelist() {
        return itemWhitelist;
    }

    public List<String> getItemBlacklist() {
        return itemBlacklist;
    }

    public boolean isSellHistoryEnabled() {
        return enableSellHistory;
    }

    public int getMaxSellHistorySize() {
        return maxSellHistorySize;
    }

    public boolean isSoundsEnabled() {
        return enableSounds;
    }

    public boolean isMessagesEnabled() {
        return enableMessages;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Check if item is allowed
    public boolean isItemAllowed(String materialName) {
        // Check blacklist first
        if (itemBlacklist.contains(materialName)) {
            return false;
        }

        // Check whitelist
        if (itemWhitelist.contains("*")) {
            return true;
        }

        return itemWhitelist.contains(materialName);
    }

    // Check if Slimefun items can be sold
    public boolean canSellSlimefunItems() {
        return sellSlimefunItems;
    }
}
