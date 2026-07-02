package net.astranetwork.astrashop.shop;

import net.astranetwork.astrashop.AstraShop;
import net.astranetwork.astrashop.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class ShopManager {

    private final AstraShop plugin;
    private final ConfigManager configManager;

    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();
    private PriceEngine priceEngine;
    private File dataFile;

    public ShopManager(AstraShop plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void load() {
        categories.clear();
        priceEngine = new PriceEngine(
                configManager.isDynamicPricingEnabled(),
                configManager.getGlobalStepPerUnit(),
                configManager.getGlobalMinMultiplier(),
                configManager.getGlobalMaxMultiplier(),
                configManager.getSellMargin(),
                configManager.getPriceDecimals()
        );

        FileConfiguration shops = configManager.getShops();
        ConfigurationSection categoriesSection = shops.getConfigurationSection("categories");
        if (categoriesSection == null) {
            plugin.getLogger().warning("shops.yml has no 'categories' section, shop will be empty.");
            return;
        }

        for (String categoryId : categoriesSection.getKeys(false)) {
            ConfigurationSection catSec = categoriesSection.getConfigurationSection(categoryId);
            if (catSec == null) continue;

            String displayName = catSec.getString("display-name", categoryId);
            Material icon = configManager.safeMaterial(catSec.getString("icon", "CHEST"), Material.CHEST);
            int slot = catSec.getInt("slot", -1);

            ShopCategory category = new ShopCategory(categoryId, displayName, icon, slot);

            ConfigurationSection itemsSection = catSec.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String itemId : itemsSection.getKeys(false)) {
                    ConfigurationSection itemSec = itemsSection.getConfigurationSection(itemId);
                    if (itemSec == null) continue;

                    String matName = itemSec.getString("material");
                    Material material = matName != null ? Material.matchMaterial(matName) : null;
                    if (material == null) {
                        plugin.getLogger().warning("Skipping item '" + itemId + "' in category '" + categoryId
                                + "': invalid material '" + matName + "'.");
                        continue;
                    }

                    boolean dynamic = itemSec.getBoolean("dynamic", true);
                    boolean buyable = itemSec.getBoolean("buyable", true);
                    boolean sellable = itemSec.getBoolean("sellable", true);
                    double baseBuy = itemSec.getDouble("base-buy", 1.0);
                    double baseSell = itemSec.getDouble("base-sell", dynamic ? -1 : baseBuy * configManager.getSellMargin());
                    double minMultOverride = itemSec.contains("min-multiplier") ? itemSec.getDouble("min-multiplier") : -1;
                    double maxMultOverride = itemSec.contains("max-multiplier") ? itemSec.getDouble("max-multiplier") : -1;
                    double stepOverride = itemSec.contains("step-per-unit") ? itemSec.getDouble("step-per-unit") : -1;
                    String displayNameItem = itemSec.getString("display-name", null);

                    ShopItem item = new ShopItem(itemId, categoryId, material, displayNameItem,
                            dynamic, buyable, sellable, baseBuy, baseSell,
                            minMultOverride, maxMultOverride, stepOverride);
                    category.addItem(item);
                }
            }

            categories.put(categoryId, category);
        }

        loadPriceData();
    }

    public Map<String, ShopCategory> getCategories() {
        return categories;
    }

    public ShopCategory getCategory(String id) {
        return categories.get(id);
    }

    public ShopItem getItem(String categoryId, String itemId) {
        ShopCategory cat = categories.get(categoryId);
        return cat == null ? null : cat.getItems().get(itemId);
    }

    public PriceEngine getPriceEngine() {
        return priceEngine;
    }

    // ---- runtime price persistence --------------------------------------

    private void loadPriceData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection multipliers = data.getConfigurationSection("multipliers");
        if (multipliers == null) {
            return;
        }
        for (ShopCategory category : categories.values()) {
            for (ShopItem item : category.getItems().values()) {
                if (multipliers.contains(item.getKey())) {
                    item.setCurrentMultiplier(multipliers.getDouble(item.getKey(), 1.0));
                }
            }
        }
    }

    public void savePriceData() {
        if (dataFile == null) {
            dataFile = new File(plugin.getDataFolder(), "data.yml");
        }
        YamlConfiguration data = new YamlConfiguration();
        for (ShopCategory category : categories.values()) {
            for (ShopItem item : category.getItems().values()) {
                if (item.isDynamic()) {
                    data.set("multipliers." + item.getKey(), item.getCurrentMultiplier());
                }
            }
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save AstraShop price data.yml", e);
        }
    }

    /** Reloads config + shop catalogue while keeping current runtime multipliers where the item still exists. */
    public void reload() {
        Map<String, Double> preserved = new LinkedHashMap<>();
        for (ShopCategory category : categories.values()) {
            for (ShopItem item : category.getItems().values()) {
                preserved.put(item.getKey(), item.getCurrentMultiplier());
            }
        }
        load();
        for (ShopCategory category : categories.values()) {
            for (ShopItem item : category.getItems().values()) {
                Double kept = preserved.get(item.getKey());
                if (kept != null) {
                    item.setCurrentMultiplier(kept);
                }
            }
        }
    }
}
