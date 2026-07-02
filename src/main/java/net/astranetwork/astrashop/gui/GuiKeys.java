package net.astranetwork.astrashop.gui;

import net.astranetwork.astrashop.AstraShop;
import org.bukkit.NamespacedKey;

public final class GuiKeys {

    private GuiKeys() {
    }

    public static NamespacedKey category(AstraShop plugin) {
        return new NamespacedKey(plugin, "shop_category");
    }

    public static NamespacedKey item(AstraShop plugin) {
        return new NamespacedKey(plugin, "shop_item");
    }

    public static NamespacedKey action(AstraShop plugin) {
        return new NamespacedKey(plugin, "shop_action");
    }
}
