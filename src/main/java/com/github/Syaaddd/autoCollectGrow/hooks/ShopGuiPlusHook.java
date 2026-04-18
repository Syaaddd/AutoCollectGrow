package com.github.Syaaddd.autoCollectGrow.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

            // Try to find getItemStackPriceSell method (takes Player and ItemStack)
            for (Method method : apiClass.getMethods()) {
                if (method.getName().equals("getItemStackPriceSell")) {
                    // Check if it has the correct signature (Player, ItemStack)
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length == 2 && 
                        paramTypes[0].getName().equals("org.bukkit.entity.Player") &&
                        paramTypes[1].getName().equals("org.bukkit.inventory.ItemStack")) {
                        getItemPriceMethod = method;
                        break;
                    }
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
                                if (m.getName().equals("getItemStackPriceSell")) {
                                    Class<?>[] paramTypes = m.getParameterTypes();
                                    if (paramTypes.length == 2 && 
                                        paramTypes[0].getName().equals("org.bukkit.entity.Player") &&
                                        paramTypes[1].getName().equals("org.bukkit.inventory.ItemStack")) {
                                        getItemPriceMethod = m;
                                        break;
                                    }
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
     * Get the unit sell price (per 1 item) through ShopGUIPlus.
     * Always passes a single-item clone so the API returns the price for exactly 1 unit,
     * matching how /sell hand computes its per-item rate before multiplying by amount.
     * @param player The player selling the item (for price modifiers/permissions)
     * @param item The item stack (amount is ignored; call site multiplies by actual amount)
     * @return The sell price for 1 item, or -1 if unavailable
     */
    public double getItemPrice(Player player, ItemStack item) {
        if (!hooked) {
            return -1;
        }

        try {
            // Clone with amount=1 so the API always returns the unit price.
            // This avoids double-counting when the caller multiplies by item.getAmount().
            ItemStack singleItem = item.clone();
            singleItem.setAmount(1);

            if (getItemPriceMethod != null) {
                Object result;
                if (apiInstance != null) {
                    result = getItemPriceMethod.invoke(apiInstance, player, singleItem);
                } else {
                    result = getItemPriceMethod.invoke(null, player, singleItem);
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
