package net.astranetwork.astrashop.commands;

import net.astranetwork.astrashop.AstraShop;
import net.astranetwork.astrashop.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class ShopCommand implements CommandExecutor {

    private final AstraShop plugin;

    public ShopCommand(AstraShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("player-only"), plugin.getConfigManager().getPrefix(), Map.of()));
            return true;
        }
        if (!player.hasPermission("astrashop.use")) {
            player.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("no-permission"), plugin.getConfigManager().getPrefix(), Map.of()));
            return true;
        }
        if (!plugin.getEconomyManager().isReady()) {
            player.sendMessage(ColorUtil.parseWithPrefix(
                    plugin.getConfigManager().message("no-vault"), plugin.getConfigManager().getPrefix(), Map.of()));
            return true;
        }

        player.openInventory(plugin.getMainShopGUI().build());
        player.sendMessage(ColorUtil.parseWithPrefix(
                plugin.getConfigManager().message("shop-opened"), plugin.getConfigManager().getPrefix(), Map.of()));
        return true;
    }
}
