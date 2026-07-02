package net.astranetwork.astrashop.shop;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.Map;

public class ShopCategory {

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int slot; // slot in main menu, -1 = auto
    private final Map<String, ShopItem> items = new LinkedHashMap<>();

    public ShopCategory(String id, String displayName, Material icon, int slot) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.slot = slot;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    public Map<String, ShopItem> getItems() {
        return items;
    }

    public void addItem(ShopItem item) {
        items.put(item.getId(), item);
    }
}
