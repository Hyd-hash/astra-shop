package net.astranetwork.astrashop.tasks;

import net.astranetwork.astrashop.AstraShop;
import net.astranetwork.astrashop.shop.ShopCategory;
import net.astranetwork.astrashop.shop.ShopItem;

public class RegenTask implements Runnable {

    private final AstraShop plugin;

    public RegenTask(AstraShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfigManager().isRegenEnabled()) {
            return;
        }
        double step = plugin.getConfigManager().getRegenStep();
        for (ShopCategory category : plugin.getShopManager().getCategories().values()) {
            for (ShopItem item : category.getItems().values()) {
                plugin.getShopManager().getPriceEngine().applyRegenStep(item, step);
            }
        }
    }
}
