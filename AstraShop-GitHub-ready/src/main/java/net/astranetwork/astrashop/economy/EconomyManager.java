package net.astranetwork.astrashop.economy;

import net.astranetwork.astrashop.AstraShop;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final AstraShop plugin;
    private Economy economy;

    public EconomyManager(AstraShop plugin) {
        this.plugin = plugin;
    }

    /** @return true if an economy provider (e.g. EssentialsX via Vault) was found. */
    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                .getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isReady() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public double getBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public boolean has(OfflinePlayer player, double amount) {
        return economy.has(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        try {
            return economy.format(amount);
        } catch (Exception ex) {
            return String.format("%.2f", amount);
        }
    }
}
