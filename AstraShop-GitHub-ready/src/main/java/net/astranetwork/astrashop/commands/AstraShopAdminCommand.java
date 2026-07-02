package net.astranetwork.astrashop.commands;

import net.astranetwork.astrashop.AstraShop;
import net.astranetwork.astrashop.shop.ShopItem;
import net.astranetwork.astrashop.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AstraShopAdminCommand implements CommandExecutor, TabCompleter {

    private final AstraShop plugin;

    public AstraShopAdminCommand(AstraShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("astrashop.admin")) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("no-permission"), plugin.getConfigManager().getPrefix(), Map.of()));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("setprice-usage"), plugin.getConfigManager().getPrefix(), Map.of()));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadEverything();
                sender.sendMessage(ColorUtil.parseWithPrefix(
                        plugin.getConfigManager().message("reload-success"), plugin.getConfigManager().getPrefix(), Map.of()));
            }
            case "save" -> {
                plugin.getShopManager().savePriceData();
                sender.sendMessage(ColorUtil.parseWithPrefix(
                        plugin.getConfigManager().message("save-success"), plugin.getConfigManager().getPrefix(), Map.of()));
            }
            case "setprice" -> handleSetPrice(sender, args);
            case "resetprice" -> handleResetPrice(sender, args);
            case "give" -> handleGive(sender, args);
            default -> sender.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("unknown-command"), plugin.getConfigManager().getPrefix(), Map.of()));
        }
        return true;
    }

    private void handleSetPrice(CommandSender sender, String[] args) {
        // /astrashop setprice <categoryId> <itemId> <buy|sell> <amount>
        if (args.length < 5) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("setprice-usage"), plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }
        ShopItem item = plugin.getShopManager().getItem(args[1], args[2]);
        if (item == null) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("item-not-found"), plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[4]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("setprice-usage"), plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }

        // For dynamic items this directly sets an equivalent multiplier so the
        // change persists correctly with the pricing engine's math.
        String type = args[3].equalsIgnoreCase("sell") ? "sell" : "buy";
        if (item.isDynamic()) {
            double baseBuy = item.getBaseBuyPrice();
            if (type.equals("buy")) {
                item.setCurrentMultiplier(baseBuy <= 0 ? 1.0 : amount / baseBuy);
            } else {
                double sellMargin = plugin.getConfigManager().getSellMargin();
                double impliedBuy = sellMargin <= 0 ? amount : amount / sellMargin;
                item.setCurrentMultiplier(baseBuy <= 0 ? 1.0 : impliedBuy / baseBuy);
            }
        } else {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    "{prefix}<yellow>That item uses fixed prices set in shops.yml. Edit the file and /astrashop reload instead.",
                    plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }

        sender.sendMessage(ColorUtil.parseWithPrefix(
                plugin.getConfigManager().message("setprice-success"), plugin.getConfigManager().getPrefix(),
                Map.of("type", type, "item", item.getId(), "price", String.valueOf(amount))));
    }

    private void handleResetPrice(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    "{prefix}<red>Usage: /astrashop resetprice <categoryId> <itemId>",
                    plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }
        ShopItem item = plugin.getShopManager().getItem(args[1], args[2]);
        if (item == null) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("item-not-found"), plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }
        item.setCurrentMultiplier(1.0);
        sender.sendMessage(ColorUtil.parseWithPrefix(
                plugin.getConfigManager().message("resetprice-success"), plugin.getConfigManager().getPrefix(),
                Map.of("item", item.getId())));
    }

    private void handleGive(CommandSender sender, String[] args) {
        // /astrashop give <player> <categoryId> <itemId> <amount>
        if (args.length < 5) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    "{prefix}<red>Usage: /astrashop give <player> <categoryId> <itemId> <amount>",
                    plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    "{prefix}<red>Player not found or not online.",
                    plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }
        ShopItem item = plugin.getShopManager().getItem(args[2], args[3]);
        if (item == null) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("item-not-found"), plugin.getConfigManager().getPrefix(), Map.of()));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[4]);
        } catch (NumberFormatException ex) {
            amount = 1;
        }
        target.getInventory().addItem(new ItemStack(item.getMaterial(), amount));
        sender.sendMessage(ColorUtil.parseWithPrefix(
                "{prefix}<green>Gave " + amount + "x " + item.getId() + " to " + target.getName() + ".",
                plugin.getConfigManager().getPrefix(), Map.of()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("reload", "save", "setprice", "resetprice", "give"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("setprice") || args[0].equalsIgnoreCase("resetprice"))) {
            options.addAll(plugin.getShopManager().getCategories().keySet());
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("setprice") || args[0].equalsIgnoreCase("resetprice"))) {
            var category = plugin.getShopManager().getCategory(args[1]);
            if (category != null) options.addAll(category.getItems().keySet());
        } else if (args.length == 4 && args[0].equalsIgnoreCase("setprice")) {
            options.addAll(List.of("buy", "sell"));
        }
        String prefix = args[args.length - 1].toLowerCase();
        options.removeIf(s -> !s.toLowerCase().startsWith(prefix));
        return options;
    }
}
