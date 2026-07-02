package net.astranetwork.astrashop.gui;

import net.astranetwork.astrashop.AstraShop;
import net.astranetwork.astrashop.shop.ShopItem;
import net.astranetwork.astrashop.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class GUIListener implements Listener {

    private final AstraShop plugin;

    public GUIListener(AstraShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof ShopHolders.MainMenuHolder || holder instanceof ShopHolders.CategoryMenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryHolder holder = top.getHolder();

        if (!(holder instanceof ShopHolders.MainMenuHolder) && !(holder instanceof ShopHolders.CategoryMenuHolder)) {
            return;
        }

        // Always cancel: this is a display-only GUI, real items never move through it.
        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(top)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir() || !clicked.hasItemMeta()) {
            return;
        }

        if (holder instanceof ShopHolders.MainMenuHolder) {
            handleMainMenuClick(player, clicked);
        } else {
            handleCategoryClick(player, (ShopHolders.CategoryMenuHolder) holder, clicked, event.getClick(), event.getSlot(), top.getSize());
        }
    }

    private void handleMainMenuClick(Player player, ItemStack clicked) {
        String categoryId = clicked.getItemMeta().getPersistentDataContainer()
                .get(GuiKeys.category(plugin), PersistentDataType.STRING);
        if (categoryId == null) {
            return;
        }
        Inventory inv = plugin.getCategoryGUI().build(categoryId, 0);
        if (inv == null) {
            player.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("category-not-found"),
                    plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }
        player.openInventory(inv);
    }

    private void handleCategoryClick(Player player, ShopHolders.CategoryMenuHolder holder, ItemStack clicked,
                                      ClickType click, int slot, int size) {
        int navRowStart = size - 9;

        if (slot >= navRowStart) {
            int offset = slot - navRowStart;
            if (offset == CategoryGUI.NAV_BACK_SLOT_OFFSET && clicked.getType() == org.bukkit.Material.BARRIER) {
                player.openInventory(plugin.getMainShopGUI().build());
                return;
            }
            if (offset == CategoryGUI.NAV_PREV_SLOT_OFFSET) {
                Inventory inv = plugin.getCategoryGUI().build(holder.getCategoryId(), holder.getPage() - 1);
                if (inv != null) player.openInventory(inv);
                return;
            }
            if (offset == CategoryGUI.NAV_NEXT_SLOT_OFFSET) {
                Inventory inv = plugin.getCategoryGUI().build(holder.getCategoryId(), holder.getPage() + 1);
                if (inv != null) player.openInventory(inv);
                return;
            }
            return;
        }

        String categoryId = clicked.getItemMeta().getPersistentDataContainer()
                .get(GuiKeys.category(plugin), PersistentDataType.STRING);
        String itemId = clicked.getItemMeta().getPersistentDataContainer()
                .get(GuiKeys.item(plugin), PersistentDataType.STRING);
        if (categoryId == null || itemId == null) {
            return;
        }
        ShopItem shopItem = plugin.getShopManager().getItem(categoryId, itemId);
        if (shopItem == null) {
            return;
        }

        if (plugin.getTransactionService().isOnCooldown(player)) {
            player.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("cooldown"),
                    plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }

        switch (click) {
            case LEFT -> plugin.getTransactionService().buy(player, shopItem, 1);
            case SHIFT_LEFT -> plugin.getTransactionService().buy(player, shopItem, 64);
            case RIGHT -> plugin.getTransactionService().sell(player, shopItem, false);
            case SHIFT_RIGHT -> plugin.getTransactionService().sell(player, shopItem, true);
            default -> {
                // ignore middle click, number keys, drop key, etc.
            }
        }

        // Refresh the GUI in place so updated dynamic prices are visible immediately.
        Inventory refreshed = plugin.getCategoryGUI().build(holder.getCategoryId(), holder.getPage());
        if (refreshed != null) {
            player.openInventory(refreshed);
        }
    }
}
