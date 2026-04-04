package com.github.Syaaddd.autoCollectGrow.hooks;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Hook for ShopGUIPlus integration
 */
public class ShopGuiPlusHook {

    private Plugin shopGuiPlus;
    private boolean hooked = false;
    private Object apiInstance;
    private Method getItemPriceMethod;

    public ShopGuiPlusHook() {
        setupShopGuiPlus();
    }

    /**
     * Setup ShopGUIPlus hook
     */
    private void setupShopGuiPlus() {
        shopGuiPlus = Bukkit.getPluginManager().getPlugin("ShopGUIPlus");
        if (shopGuiPlus == null) {
            Bukkit.getLogger().warning("[AutoCollectGrow] ShopGUIPlus plugin not found!");
            return;
        }

        if (!shopGuiPlus.isEnabled()) {
            Bukkit.getLogger().warning("[AutoCollectGrow] ShopGUIPlus is not enabled!");
            return;
        }

        try {
            // Try different API class names
            Class<?> apiClass = null;
            
            // Try net.brcdev.shopgui.ShopGuiPlusApi first
            try {
                apiClass = Class.forName("net.brcdev.shopgui.ShopGuiPlusApi");
            } catch (ClassNotFoundException e) {
                // Try other possible API classes
                try {
                    apiClass = Class.forName("net.brcdev.shopgui.api.ShopGuiPlusApi");
                } catch (ClassNotFoundException e2) {
                    try {
                        apiClass = Class.forName("net.brcdev.shopgui.api.ShopApi");
                    } catch (ClassNotFoundException e3) {
                        Bukkit.getLogger().warning("[AutoCollectGrow] Could not find ShopGUIPlus API class!");
                        return;
                    }
                }
            }

            // Try to find getItemPrice method
            for (Method method : apiClass.getMethods()) {
                if (method.getName().contains("getItemPrice") || method.getName().contains("getPrice")) {
                    getItemPriceMethod = method;
                    break;
                }
            }

            if (getItemPriceMethod == null) {
                // Try to get API instance first
                for (Method method : apiClass.getMethods()) {
                    if (method.getName().equals("getInstance") || method.getName().equals("getApi")) {
                        try {
                            apiInstance = method.invoke(null);
                            // Now find methods in the instance
                            for (Method m : apiInstance.getClass().getMethods()) {
                                if (m.getName().contains("Price") || m.getName().contains("Sell")) {
                                    getItemPriceMethod = m;
                                    break;
                                }
                            }
                            break;
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                }
            }

            if (getItemPriceMethod != null || apiInstance != null) {
                hooked = true;
                Bukkit.getLogger().info("[AutoCollectGrow] ShopGUIPlus hooked successfully!");
            } else {
                Bukkit.getLogger().warning("[AutoCollectGrow] ShopGUIPlus API methods not found!");
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[AutoCollectGrow] Failed to hook ShopGUIPlus", e);
        }
    }

    /**
     * Get the price of an item through ShopGUIPlus
     */
    public double getItemPrice(ItemStack item) {
        if (!hooked) {
            return -1;
        }

        try {
            if (getItemPriceMethod != null) {
                Object result;
                if (apiInstance != null) {
                    result = getItemPriceMethod.invoke(apiInstance, item);
                } else {
                    result = getItemPriceMethod.invoke(null, item);
                }
                
                if (result instanceof Double) {
                    return (Double) result;
                } else if (result instanceof Number) {
                    return ((Number) result).doubleValue();
                }
            }
            return -1;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[AutoCollectGrow] Failed to get item price", e);
            return -1;
        }
    }

    /**
     * Check if ShopGUIPlus is available
     */
    public boolean isHooked() {
        return hooked && shopGuiPlus != null && shopGuiPlus.isEnabled();
    }

    /**
     * Get the ShopGUIPlus plugin instance
     */
    public Plugin getShopGuiPlus() {
        return shopGuiPlus;
    }
}
