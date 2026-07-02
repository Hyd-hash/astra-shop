package net.astranetwork.astrashop.shop;

import org.bukkit.Material;

/**
 * A single tradable item within a category. Holds its static config plus
 * (for dynamic items) the runtime multiplier that drifts with trading.
 */
public class ShopItem {

    private final String id;
    private final String categoryId;
    private final Material material;
    private final String displayName; // nullable -> falls back to material name
    private final boolean dynamic;
    private final boolean buyable;
    private final boolean sellable;

    private final double baseBuyPrice;
    private final double fixedSellPrice; // only meaningful when !dynamic

    // Per-item overrides of the global dynamic pricing engine, -1 = use global default
    private final double minMultiplierOverride;
    private final double maxMultiplierOverride;
    private final double stepPerUnitOverride;

    // Runtime state for dynamic items, persisted in data.yml
    private double currentMultiplier = 1.0;

    public ShopItem(String id, String categoryId, Material material, String displayName,
                     boolean dynamic, boolean buyable, boolean sellable,
                     double baseBuyPrice, double fixedSellPrice,
                     double minMultiplierOverride, double maxMultiplierOverride, double stepPerUnitOverride) {
        this.id = id;
        this.categoryId = categoryId;
        this.material = material;
        this.displayName = displayName;
        this.dynamic = dynamic;
        this.buyable = buyable;
        this.sellable = sellable;
        this.baseBuyPrice = baseBuyPrice;
        this.fixedSellPrice = fixedSellPrice;
        this.minMultiplierOverride = minMultiplierOverride;
        this.maxMultiplierOverride = maxMultiplierOverride;
        this.stepPerUnitOverride = stepPerUnitOverride;
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    /** Unique key used for data persistence and lookups: categoryId:itemId */
    public String getKey() {
        return categoryId + ":" + id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isBuyable() {
        return buyable;
    }

    public boolean isSellable() {
        return sellable;
    }

    public double getBaseBuyPrice() {
        return baseBuyPrice;
    }

    public double getFixedSellPrice() {
        return fixedSellPrice;
    }

    public double getMinMultiplierOverride() {
        return minMultiplierOverride;
    }

    public double getMaxMultiplierOverride() {
        return maxMultiplierOverride;
    }

    public double getStepPerUnitOverride() {
        return stepPerUnitOverride;
    }

    public double getCurrentMultiplier() {
        return currentMultiplier;
    }

    public void setCurrentMultiplier(double currentMultiplier) {
        this.currentMultiplier = currentMultiplier;
    }
}
