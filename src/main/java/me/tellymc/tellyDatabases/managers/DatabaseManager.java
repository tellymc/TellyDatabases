package me.tellymc.tellyDatabases.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.tellymc.tellyDatabases.objects.Database;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private final Map<Database, HikariDataSource> dataSources = new HashMap<>();

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize(Database database) {

        HikariConfig hikariConfig = database.getHikariConfig();
        if (hikariConfig == null) return;

        try {

            HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            try (Connection connection = dataSource.getConnection()) {

                Statement statement = connection.createStatement();

                for (String table : database.getTables()) {
                    statement.executeUpdate(table);
                }
            }

            plugin.getLogger().info("Database initializing was successful");

            dataSources.put(database, dataSource);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed initializing database", e);
        }
    }

    public CompletableFuture<List<Map<String, Object>>> executeQuery(Database database, String sql, Object... params) {

        if (!isRunning(database)) return CompletableFuture.completedFuture(null);

        return CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> results = new ArrayList<>();

            try (Connection connection = getConnection(database);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }

                try (ResultSet rs = preparedStatement.executeQuery()) {
                    int columns = rs.getMetaData().getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columns; i++) {
                            row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                        }
                        results.add(row);
                    }
                }

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed executing query", e);
            }

            return results;
        });
    }

    public CompletableFuture<Void> executeUpdate(Database database, String sql, Object... params) {
        if (!isRunning(database)) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection(database);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed executing update", e);
            }
        });
    }

    private Connection getConnection(Database database) {

        HikariDataSource dataSource = dataSources.get(database);

        try {

            if (dataSource == null || dataSource.isClosed()) {
                throw new IllegalStateException("Not able to get connection because not connected to the database.");
            }

            return dataSource.getConnection();

        } catch (SQLException e) {

            throw new RuntimeException("Unable to get connection from the database manager.");

        }
    }

    public CompletableFuture<Boolean> reconnect(Database database) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            disconnect(database);

            try {

                initialize(database);
                HikariDataSource dataSource = dataSources.get(database);

                if (dataSource == null || dataSource.isClosed()) {
                    future.complete(false);
                    return;
                }

                try (Connection connection = dataSource.getConnection()) {
                    future.complete(true);
                }

            } catch (Exception e) {

                plugin.getLogger().log(Level.SEVERE, "Failed while reconnecting to the database.", e);
                future.complete(false);

            }
        });

        return future;
    }

    public void disconnect(Database database) {

        HikariDataSource dataSource = dataSources.get(database);

        try {

            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                plugin.getLogger().info("Database connection has been closed");
            }

        } catch (Exception e) {

            plugin.getLogger().log(Level.SEVERE, "Failed shutting down the database pool", e);

        } finally {

            dataSources.put(database, null);
        }
    }

    public boolean isRunning(Database database) {
        HikariDataSource dataSource = dataSources.get(database);
        return dataSource != null && !dataSource.isClosed();
    }
}