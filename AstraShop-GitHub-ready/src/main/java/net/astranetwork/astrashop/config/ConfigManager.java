package net.astranetwork.astrashop.config;

import net.astranetwork.astrashop.AstraShop;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final AstraShop plugin;

    private FileConfiguration config;
    private FileConfiguration messages;
    private File shopsFile;
    private FileConfiguration shops;

    public ConfigManager(AstraShop plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        messages = loadOrCreate("messages.yml");
        shopsFile = new File(plugin.getDataFolder(), "shops.yml");
        if (!shopsFile.exists()) {
            plugin.saveResource("shops.yml", false);
        }
        shops = YamlConfiguration.loadConfiguration(shopsFile);
    }

    private FileConfiguration loadOrCreate(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        // Merge in any new keys added in future plugin updates without wiping user edits.
        try (InputStream defStream = plugin.getResource(name)) {
            if (defStream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defStream, StandardCharsets.UTF_8));
                cfg.setDefaults(defaults);
            }
        } catch (IOException ignored) {
        }
        return cfg;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getShops() {
        return shops;
    }

    public File getShopsFile() {
        return shopsFile;
    }

    // ---- typed convenience getters -------------------------------------

    public String getPrefix() {
        return config.getString("prefix", "");
    }

    public String message(String path) {
        return messages.getString(path, path);
    }

    public boolean isDynamicPricingEnabled() {
        return config.getBoolean("dynamic-pricing.enabled", true);
    }

    public double getGlobalStepPerUnit() {
        return config.getDouble("dynamic-pricing.step-per-unit", 0.001);
    }

    public double getGlobalMinMultiplier() {
        return config.getDouble("dynamic-pricing.min-multiplier", 0.25);
    }

    public double getGlobalMaxMultiplier() {
        return config.getDouble("dynamic-pricing.max-multiplier", 4.0);
    }

    public double getSellMargin() {
        return config.getDouble("dynamic-pricing.sell-margin", 0.75);
    }

    public boolean isRegenEnabled() {
        return config.getBoolean("dynamic-pricing.regen.enabled", true);
    }

    public int getRegenIntervalSeconds() {
        return config.getInt("dynamic-pricing.regen.interval-seconds", 300);
    }

    public double getRegenStep() {
        return config.getDouble("dynamic-pricing.regen.step", 0.05);
    }

    public int getAutosaveIntervalSeconds() {
        return config.getInt("autosave-interval-seconds", 300);
    }

    public long getTransactionCooldownMs() {
        return config.getLong("transaction-cooldown-ms", 150);
    }

    public int getPriceDecimals() {
        return config.getInt("economy.price-decimals", 2);
    }

    public double getMinBalanceAfterPurchase() {
        return config.getDouble("economy.min-balance-after-purchase", 0.0);
    }

    public int getMainMenuRows() {
        return config.getInt("gui.main-rows", 3);
    }

    public int getCategoryMenuRows() {
        return config.getInt("gui.category-rows", 6);
    }

    public String getMainTitle() {
        return config.getString("gui.main-title", "Astra Shop");
    }

    public String getCategoryTitleTemplate() {
        return config.getString("gui.category-title", "{category}");
    }

    public Material getFillerMaterial() {
        return safeMaterial(config.getString("gui.filler-item", "PURPLE_STAINED_GLASS_PANE"), Material.PURPLE_STAINED_GLASS_PANE);
    }

    public Material getAccentMaterial() {
        return safeMaterial(config.getString("gui.accent-item", "PINK_STAINED_GLASS_PANE"), Material.PINK_STAINED_GLASS_PANE);
    }

    public Material safeMaterial(String name, Material fallback) {
        if (name == null) {
            return fallback;
        }
        Material m = Material.matchMaterial(name);
        return m != null ? m : fallback;
    }
}
