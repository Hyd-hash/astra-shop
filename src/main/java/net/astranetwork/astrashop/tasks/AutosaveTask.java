package net.astranetwork.astrashop.tasks;

import net.astranetwork.astrashop.AstraShop;

public class AutosaveTask implements Runnable {

    private final AstraShop plugin;

    public AutosaveTask(AstraShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getShopManager().savePriceData();
    }
}
