package net.astranetwork.astrashop.shop;

import net.astranetwork.astrashop.AstraShop;
import net.astranetwork.astrashop.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransactionService {

    private final AstraShop plugin;
    private final Map<UUID, Long> lastTransaction = new HashMap<>();

    public TransactionService(AstraShop plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(Player player) {
        long cooldown = plugin.getConfigManager().getTransactionCooldownMs();
        if (cooldown <= 0 || player.hasPermission("astrashop.bypasscooldown")) {
            return false;
        }
        long now = System.currentTimeMillis();
        Long last = lastTransaction.get(player.getUniqueId());
        if (last != null && now - last < cooldown) {
            return true;
        }
        lastTransaction.put(player.getUniqueId(), now);
        return false;
    }

    public void buy(Player player, ShopItem item, int amount) {
        if (!plugin.getEconomyManager().isReady()) {
            send(player, "no-vault", Map.of());
            return;
        }
        if (!item.isBuyable()) {
            send(player, "buy-fail-generic", Map.of());
            return;
        }

        PriceEngine engine = plugin.getShopManager().getPriceEngine();
        double totalCost = engine.getTotalBuyPrice(item, amount);

        double balance = plugin.getEconomyManager().getBalance(player);
        double minAfter = plugin.getConfigManager().getMinBalanceAfterPurchase();
        if (balance - totalCost < minAfter) {
            send(player, "buy-fail-money", Map.of("price", plugin.getEconomyManager().format(totalCost)));
            return;
        }

        ItemStack toGive = new ItemStack(item.getMaterial(), amount);
        PlayerInventory inv = player.getInventory();
        Map<Integer, ItemStack> leftover = inv.addItem(toGive);
        int leftoverAmount = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
        int actuallyAdded = amount - leftoverAmount;

        if (actuallyAdded <= 0) {
            send(player, "buy-fail-inventory-full", Map.of());
            return;
        }

        double actualCost = engine.getTotalBuyPrice(item, actuallyAdded);
        if (!plugin.getEconomyManager().withdraw(player, actualCost)) {
            // Roll back the items we just gave since payment failed.
            inv.removeItem(new ItemStack(item.getMaterial(), actuallyAdded));
            send(player, "buy-fail-generic", Map.of());
            return;
        }

        engine.applyBuyImpact(item, actuallyAdded);

        String itemName = displayName(item);
        send(player, "buy-success", Map.of(
                "amount", String.valueOf(actuallyAdded),
                "item", itemName,
                "price", plugin.getEconomyManager().format(actualCost)
        ));

        if (actuallyAdded < amount) {
            send(player, "buy-fail-inventory-full", Map.of());
        }
    }

    public void sell(Player player, ShopItem item, boolean sellAll) {
        if (!plugin.getEconomyManager().isReady()) {
            send(player, "no-vault", Map.of());
            return;
        }
        if (!item.isSellable()) {
            send(player, "sell-fail-generic", Map.of());
            return;
        }

        PlayerInventory inv = player.getInventory();
        int owned = countMatching(inv, item);
        if (owned <= 0) {
            send(player, "sell-fail-none", Map.of("item", displayName(item)));
            return;
        }

        int amount = sellAll ? owned : Math.min(1, owned);

        int removed = removeMatching(inv, item, amount);
        if (removed <= 0) {
            send(player, "sell-fail-none", Map.of("item", displayName(item)));
            return;
        }

        PriceEngine engine = plugin.getShopManager().getPriceEngine();
        double totalValue = engine.getTotalSellPrice(item, removed);

        plugin.getEconomyManager().deposit(player, totalValue);
        engine.applySellImpact(item, removed);

        send(player, "sell-success", Map.of(
                "amount", String.valueOf(removed),
                "item", displayName(item),
                "price", plugin.getEconomyManager().format(totalValue)
        ));
    }

    private int countMatching(PlayerInventory inv, ShopItem item) {
        int total = 0;
        for (ItemStack stack : inv.getStorageContents()) {
            if (stack != null && stack.getType() == item.getMaterial()) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private int removeMatching(PlayerInventory inv, ShopItem item, int amount) {
        int remaining = amount;
        ItemStack[] contents = inv.getStorageContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != item.getMaterial()) {
                continue;
            }
            int take = Math.min(stack.getAmount(), remaining);
            stack.setAmount(stack.getAmount() - take);
            if (stack.getAmount() <= 0) {
                contents[i] = null;
            }
            remaining -= take;
        }
        inv.setStorageContents(contents);
        return amount - remaining;
    }

    private String displayName(ShopItem item) {
        if (item.getDisplayName() != null) {
            return item.getDisplayName();
        }
        String[] parts = item.getMaterial().name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(part.charAt(0)).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private void send(Player player, String messageKey, Map<String, String> placeholders) {
        String raw = plugin.getConfigManager().message(messageKey);
        player.sendMessage(ColorUtil.parseWithPrefix(raw, plugin.getConfigManager().getPrefix(), placeholders));
    }
}
