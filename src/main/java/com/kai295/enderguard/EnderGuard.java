package com.kai295.enderguard;

import com.kai295.enderguard.listeners.InventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnderGuard extends JavaPlugin {

    private InventoryListener inventoryListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.inventoryListener = new InventoryListener(this);
        getServer().getPluginManager().registerEvents(inventoryListener, this);

        getCommand("enderguard").setExecutor(new CommandManager(this));

        new UpdateChecker(this, 126578).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info("EnderGuard is up to date.");
            } else {
                getLogger().info("There is a new update available for EnderGuard.");
            }
        });

        getLogger().info("EnderGuard has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EnderGuard has been disabled.");
    }

    public void reload() {
        reloadConfig();
        getLogger().info("EnderGuard configuration reloaded.");
    }

    public InventoryListener getInventoryListener() {
        return inventoryListener;
    }
}