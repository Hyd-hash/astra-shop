package net.astranetwork.astrashop;

import net.astranetwork.astrashop.commands.AstraShopAdminCommand;
import net.astranetwork.astrashop.commands.ShopCommand;
import net.astranetwork.astrashop.config.ConfigManager;
import net.astranetwork.astrashop.economy.EconomyManager;
import net.astranetwork.astrashop.gui.CategoryGUI;
import net.astranetwork.astrashop.gui.GUIListener;
import net.astranetwork.astrashop.gui.MainShopGUI;
import net.astranetwork.astrashop.shop.ShopManager;
import net.astranetwork.astrashop.shop.TransactionService;
import net.astranetwork.astrashop.tasks.AutosaveTask;
import net.astranetwork.astrashop.tasks.RegenTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class AstraShop extends JavaPlugin {

    private ConfigManager configManager;
    private EconomyManager economyManager;
    private ShopManager shopManager;
    private TransactionService transactionService;

    private MainShopGUI mainShopGUI;
    private CategoryGUI categoryGUI;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        configManager.loadAll();

        this.economyManager = new EconomyManager(this);
        if (!economyManager.setup()) {
            getLogger().severe("=====================================================");
            getLogger().severe(" AstraShop could not find Vault or a hooked economy  ");
            getLogger().severe(" plugin (e.g. EssentialsX). The shop will not allow  ");
            getLogger().severe(" transactions until this is fixed.                  ");
            getLogger().severe("=====================================================");
        }

        this.shopManager = new ShopManager(this, configManager);
        shopManager.load();

        this.transactionService = new TransactionService(this);
        this.mainShopGUI = new MainShopGUI(this);
        this.categoryGUI = new CategoryGUI(this);

        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        ShopCommand shopCommand = new ShopCommand(this);
        getCommand("shop").setExecutor(shopCommand);

        AstraShopAdminCommand adminCommand = new AstraShopAdminCommand(this);
        getCommand("astrashop").setExecutor(adminCommand);
        getCommand("astrashop").setTabCompleter(adminCommand);

        long regenTicks = Math.max(20L, configManager.getRegenIntervalSeconds() * 20L);
        getServer().getScheduler().runTaskTimer(this, new RegenTask(this), regenTicks, regenTicks);

        long autosaveTicks = Math.max(20L, configManager.getAutosaveIntervalSeconds() * 20L);
        getServer().getScheduler().runTaskTimerAsynchronously(this, new AutosaveTask(this), autosaveTicks, autosaveTicks);

        getLogger().info("AstraShop enabled for Astra Network - " + shopManager.getCategories().size() + " categories loaded.");
    }

    @Override
    public void onDisable() {
        if (shopManager != null) {
            shopManager.savePriceData();
        }
    }

    public void reloadEverything() {
        configManager.loadAll();
        shopManager.reload();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public MainShopGUI getMainShopGUI() {
        return mainShopGUI;
    }

    public CategoryGUI getCategoryGUI() {
        return categoryGUI;
    }
}
