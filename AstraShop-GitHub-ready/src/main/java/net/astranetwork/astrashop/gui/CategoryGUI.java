package net.astranetwork.astrashop.gui;

import net.astranetwork.astrashop.AstraShop;
import net.astranetwork.astrashop.shop.PriceEngine;
import net.astranetwork.astrashop.shop.ShopCategory;
import net.astranetwork.astrashop.shop.ShopItem;
import net.astranetwork.astrashop.util.ColorUtil;
import net.astranetwork.astrashop.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryGUI {

    private final AstraShop plugin;

    public static final int NAV_BACK_SLOT_OFFSET = 4; // relative to last row start
    public static final int NAV_PREV_SLOT_OFFSET = 0;
    public static final int NAV_NEXT_SLOT_OFFSET = 8;

    public CategoryGUI(AstraShop plugin) {
        this.plugin = plugin;
    }

    public Inventory build(String categoryId, int page) {
        ShopCategory category = plugin.getShopManager().getCategory(categoryId);
        if (category == null) {
            return null;
        }

        int rows = Math.max(2, Math.min(6, plugin.getConfigManager().getCategoryMenuRows()));
        int size = rows * 9;
        int itemsPerPage = size - 9; // last row reserved for navigation
        List<ShopItem> allItems = new ArrayList<>(category.getItems().values());
        int maxPage = Math.max(0, (allItems.size() - 1) / itemsPerPage);
        page = Math.max(0, Math.min(page, maxPage));

        ShopHolders.CategoryMenuHolder holder = new ShopHolders.CategoryMenuHolder(categoryId, page);
        Map<String, String> titlePh = new HashMap<>();
        titlePh.put("category", ColorUtil.plain(ColorUtil.parse(category.getDisplayName())));
        Component title = ColorUtil.parse(plugin.getConfigManager().getCategoryTitleTemplate(), titlePh);
        Inventory inv = plugin.getServer().createInventory(holder, size, title);
        holder.setInventory(inv);

        // fill everything with filler first
        ItemStack filler = new ItemBuilder(plugin.getConfigManager().getFillerMaterial())
                .name(Component.text(" "))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build();
        for (int i = 0; i < size; i++) {
            inv.setItem(i, filler);
        }

        PriceEngine engine = plugin.getShopManager().getPriceEngine();
        NamespacedKey catKey = GuiKeys.category(plugin);
        NamespacedKey itemKey = GuiKeys.item(plugin);

        int start = page * itemsPerPage;
        int end = Math.min(allItems.size(), start + itemsPerPage);
        int slot = 0;
        for (int i = start; i < end; i++) {
            ShopItem shopItem = allItems.get(i);
            inv.setItem(slot, buildItemStack(shopItem, engine, catKey, itemKey));
            slot++;
        }

        int navRowStart = size - 9;
        // back to main menu
        inv.setItem(navRowStart + NAV_BACK_SLOT_OFFSET, new ItemBuilder(Material.BARRIER)
                .name(ColorUtil.parse("<light_purple>« Back to categories"))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        if (page > 0) {
            inv.setItem(navRowStart + NAV_PREV_SLOT_OFFSET, new ItemBuilder(Material.ARROW)
                    .name(ColorUtil.parse("<light_purple>« Previous page"))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }
        if (page < maxPage) {
            inv.setItem(navRowStart + NAV_NEXT_SLOT_OFFSET, new ItemBuilder(Material.ARROW)
                    .name(ColorUtil.parse("<light_purple>Next page »"))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }

        return inv;
    }

    private ItemStack buildItemStack(ShopItem shopItem, PriceEngine engine, NamespacedKey catKey, NamespacedKey itemKey) {
        double buyPrice = engine.getBuyPrice(shopItem);
        double sellPrice = engine.getSellPrice(shopItem);

        List<Component> lore = new ArrayList<>();
        if (shopItem.isBuyable() && buyPrice >= 0) {
            lore.add(ColorUtil.parse(plugin.getConfigManager().message("lore.buy-price"),
                    Map.of("price", plugin.getEconomyManager().isReady() ? plugin.getEconomyManager().format(buyPrice) : String.valueOf(buyPrice))));
        } else {
            lore.add(ColorUtil.parse(plugin.getConfigManager().message("lore.buy-price-unavailable")));
        }
        if (shopItem.isSellable() && sellPrice >= 0) {
            lore.add(ColorUtil.parse(plugin.getConfigManager().message("lore.sell-price"),
                    Map.of("price", plugin.getEconomyManager().isReady() ? plugin.getEconomyManager().format(sellPrice) : String.valueOf(sellPrice))));
        } else {
            lore.add(ColorUtil.parse(plugin.getConfigManager().message("lore.sell-price-unavailable")));
        }
        lore.add(ColorUtil.parse(shopItem.isDynamic()
                ? plugin.getConfigManager().message("lore.dynamic-tag")
                : plugin.getConfigManager().message("lore.fixed-tag")));
        lore.add(Component.empty());
        if (shopItem.isBuyable()) {
            lore.add(ColorUtil.parse(plugin.getConfigManager().message("lore.hint-left-click")));
            lore.add(ColorUtil.parse(plugin.getConfigManager().message("lore.hint-shift-left-click")));
        }
        if (shopItem.isSellable()) {
            lore.add(ColorUtil.parse(plugin.getConfigManager().message("lore.hint-right-click")));
            lore.add(ColorUtil.parse(plugin.getConfigManager().message("lore.hint-shift-right-click")));
        }

        String displayName = shopItem.getDisplayName();
        Component nameComponent = displayName != null
                ? ColorUtil.parse("<light_purple>" + displayName)
                : ColorUtil.parse("<light_purple>" + prettyMaterialName(shopItem.getMaterial()));

        return new ItemBuilder(shopItem.getMaterial())
                .name(nameComponent)
                .lore(lore)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .tag(catKey, shopItem.getCategoryId())
                .tag(itemKey, shopItem.getId())
                .build();
    }

    private String prettyMaterialName(Material material) {
        String[] parts = material.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(part.charAt(0)).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
