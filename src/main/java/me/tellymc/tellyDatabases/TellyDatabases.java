package me.tellymc.tellyDatabases;

import me.tellymc.tellyDatabases.managers.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TellyDatabases {

    private final JavaPlugin plugin;
    private DatabaseManager databaseManager;

    public TellyDatabases(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        this.databaseManager = new DatabaseManager(plugin);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}