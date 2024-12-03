package hu.kxtsoo.fungun;

import hu.kxtsoo.fungun.database.DatabaseManager;
import hu.kxtsoo.fungun.events.*;
import hu.kxtsoo.fungun.hooks.HookManager;
import hu.kxtsoo.fungun.manager.CommandManager;
import hu.kxtsoo.fungun.manager.CooldownManager;
import hu.kxtsoo.fungun.manager.SchedulerManager;
import hu.kxtsoo.fungun.util.ConfigUtil;
import hu.kxtsoo.fungun.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public final class FunGun extends JavaPlugin {

    private static FunGun instance;
    private ConfigUtil configUtil;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        instance = this;

        int pluginId = 23693;
        new Metrics(this, pluginId);

        this.configUtil = new ConfigUtil(this);
        ConfigUtil.configUtil = this.configUtil;

        SchedulerManager.run(new Runnable() {
            @Override
            public void run() {
                new HookManager().updateHooks();
            }
        });

        try {
            DatabaseManager.initialize(configUtil, this);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        CommandManager commandManager = new CommandManager(this);
        commandManager.registerSuggestions();
        commandManager.registerCommands();

        this.cooldownManager = new CooldownManager(this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this, cooldownManager), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDropItemListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerSwapHandItemsListener(), this);
        getServer().getPluginManager().registerEvents(new FallingBlockListener(), this);

        if (getConfig().getBoolean("update-checker.enabled", true)) {
            new UpdateChecker(this, configUtil, 5557);
        }

    }

    @Override
    public void onDisable() {
        try {
            DatabaseManager.close();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to close database", e);
        }
    }

    public static FunGun getInstance() {
        return instance;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}