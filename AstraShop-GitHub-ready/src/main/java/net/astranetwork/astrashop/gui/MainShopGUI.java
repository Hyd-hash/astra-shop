package net.astranetwork.astrashop.gui;

import net.astranetwork.astrashop.AstraShop;
import net.astranetwork.astrashop.shop.ShopCategory;
import net.astranetwork.astrashop.util.ColorUtil;
import net.astranetwork.astrashop.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MainShopGUI {

    private final AstraShop plugin;

    public MainShopGUI(AstraShop plugin) {
        this.plugin = plugin;
    }

    public Inventory build() {
        int rows = plugin.getConfigManager().getMainMenuRows();
        int size = Math.max(9, Math.min(54, rows * 9));

        ShopHolders.MainMenuHolder holder = new ShopHolders.MainMenuHolder();
        Component title = ColorUtil.parse(plugin.getConfigManager().getMainTitle());
        Inventory inv = plugin.getServer().createInventory(holder, size, title);
        holder.setInventory(inv);

        ItemStack filler = new ItemBuilder(plugin.getConfigManager().getFillerMaterial())
                .name(Component.text(" "))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build();
        ItemStack accent = new ItemBuilder(plugin.getConfigManager().getAccentMaterial())
                .name(Component.text(" "))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build();
        for (int i = 0; i < size; i++) {
            // Pink accent border on the top and bottom rows, purple filler elsewhere,
            // giving the menu a clean pink/purple frame like the Astra Network branding.
            boolean borderRow = i < 9 || i >= size - 9;
            inv.setItem(i, borderRow ? accent : filler);
        }

        NamespacedKey catKey = GuiKeys.category(plugin);

        int autoSlot = 0;
        for (ShopCategory category : plugin.getShopManager().getCategories().values()) {
            int slot = category.getSlot();
            if (slot < 0 || slot >= size) {
                // find next free auto slot
                while (autoSlot < size && !isFiller(inv, autoSlot, filler)) {
                    autoSlot++;
                }
                slot = autoSlot;
            }
            if (slot < 0 || slot >= size) {
                plugin.getLogger().warning("No free slot for category '" + category.getId() + "' in main menu, skipping.");
                continue;
            }

            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.parse("<gray>" + category.getItems().size() + " items available"));
            lore.add(Component.empty());
            lore.add(ColorUtil.parse("<yellow>Click to browse"));

            ItemStack icon = new ItemBuilder(category.getIcon())
                    .name(ColorUtil.parse(category.getDisplayName()))
                    .lore(lore)
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .tag(catKey, category.getId())
                    .build();
            inv.setItem(slot, icon);
        }

        return inv;
    }

    private boolean isFiller(Inventory inv, int slot, ItemStack filler) {
        ItemStack current = inv.getItem(slot);
        return current != null && current.isSimilar(filler);
    }
}
