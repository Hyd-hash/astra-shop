package net.astranetwork.astrashop.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class ShopHolders {

    private ShopHolders() {
    }

    public static class MainMenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class CategoryMenuHolder implements InventoryHolder {
        private Inventory inventory;
        private final String categoryId;
        private final int page;

        public CategoryMenuHolder(String categoryId, int page) {
            this.categoryId = categoryId;
            this.page = page;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public int getPage() {
            return page;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }
}
