package net.astranetwork.astrashop.shop;

/**
 * Computes buy/sell prices for {@link ShopItem}s.
 *
 * Fixed items: always return their configured base-buy / base-sell.
 * Dynamic items: price = baseBuy * currentMultiplier, sell = buy * sellMargin.
 * The multiplier shifts a little per unit traded and slowly regenerates
 * back toward 1.0 over time (see {@link net.astranetwork.astrashop.tasks.RegenTask}).
 */
public class PriceEngine {

    private final boolean dynamicEnabled;
    private final double globalStepPerUnit;
    private final double globalMinMultiplier;
    private final double globalMaxMultiplier;
    private final double sellMargin;
    private final int priceDecimals;

    public PriceEngine(boolean dynamicEnabled, double globalStepPerUnit, double globalMinMultiplier,
                        double globalMaxMultiplier, double sellMargin, int priceDecimals) {
        this.dynamicEnabled = dynamicEnabled;
        this.globalStepPerUnit = globalStepPerUnit;
        this.globalMinMultiplier = globalMinMultiplier;
        this.globalMaxMultiplier = globalMaxMultiplier;
        this.sellMargin = sellMargin;
        this.priceDecimals = priceDecimals;
    }

    private double round(double value) {
        double factor = Math.pow(10, priceDecimals);
        return Math.round(value * factor) / factor;
    }

    public double getBuyPrice(ShopItem item) {
        if (!item.isBuyable()) {
            return -1;
        }
        if (!item.isDynamic() || !dynamicEnabled) {
            return round(item.getBaseBuyPrice());
        }
        return round(item.getBaseBuyPrice() * item.getCurrentMultiplier());
    }

    public double getSellPrice(ShopItem item) {
        if (!item.isSellable()) {
            return -1;
        }
        if (!item.isDynamic() || !dynamicEnabled) {
            return round(item.getFixedSellPrice());
        }
        double buy = item.getBaseBuyPrice() * item.getCurrentMultiplier();
        return round(buy * sellMargin);
    }

    public double getTotalBuyPrice(ShopItem item, int amount) {
        return round(getBuyPrice(item) * amount);
    }

    public double getTotalSellPrice(ShopItem item, int amount) {
        return round(getSellPrice(item) * amount);
    }

    private double stepFor(ShopItem item) {
        return item.getStepPerUnitOverride() >= 0 ? item.getStepPerUnitOverride() : globalStepPerUnit;
    }

    private double minFor(ShopItem item) {
        return item.getMinMultiplierOverride() >= 0 ? item.getMinMultiplierOverride() : globalMinMultiplier;
    }

    private double maxFor(ShopItem item) {
        return item.getMaxMultiplierOverride() >= 0 ? item.getMaxMultiplierOverride() : globalMaxMultiplier;
    }

    /** Called after a player buys `amount` of this item: price trends up. */
    public void applyBuyImpact(ShopItem item, int amount) {
        if (!item.isDynamic() || !dynamicEnabled) {
            return;
        }
        double newMult = item.getCurrentMultiplier() + (stepFor(item) * amount);
        item.setCurrentMultiplier(clamp(newMult, minFor(item), maxFor(item)));
    }

    /** Called after a player sells `amount` of this item: price trends down. */
    public void applySellImpact(ShopItem item, int amount) {
        if (!item.isDynamic() || !dynamicEnabled) {
            return;
        }
        double newMult = item.getCurrentMultiplier() - (stepFor(item) * amount);
        item.setCurrentMultiplier(clamp(newMult, minFor(item), maxFor(item)));
    }

    /** Nudges the multiplier a fraction of the way back to 1.0 (equilibrium). */
    public void applyRegenStep(ShopItem item, double regenStep) {
        if (!item.isDynamic() || !dynamicEnabled) {
            return;
        }
        double current = item.getCurrentMultiplier();
        double newMult = current + ((1.0 - current) * regenStep);
        item.setCurrentMultiplier(clamp(newMult, minFor(item), maxFor(item)));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
