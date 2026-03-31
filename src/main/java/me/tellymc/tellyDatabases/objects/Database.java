package me.tellymc.tellyDatabases.objects;

import com.zaxxer.hikari.HikariConfig;

public abstract class Database {

    public abstract String getPoolName();
    public abstract String getHost();
    public int getPort() { return 3306; }
    public abstract String getDatabaseName();
    public abstract String getUsername();
    public abstract String getPassword();
    public int getMaxPoolSize() { return 10; }
    public int getMaxPoolIdle() { return 2; }
    public long getConnectionTimeout() { return 30000; }
    public long getIdleTimeout() { return 600000; }
    public long getMaxLifetime() { return 1800000; }
    public boolean getUseSsl() { return false; }
    public abstract String[] getTables();

    public HikariConfig getHikariConfig() {

        HikariConfig hikariConfig = new HikariConfig();

        String url = "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabaseName() +
                "?useSSL=" + getUseSsl() +
                "&requireSSL=" + getUseSsl() +
                "&allowPublicKeyRetrieval=" + (!getUseSsl() ? "true" : "false") +
                "&characterEncoding=UTF-8" +
                "&connectionCollation=utf8mb4_unicode_ci" +
                "&serverTimezone=UTC" +
                "&useUnicode=true";

        hikariConfig.setPoolName(getPoolName());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(getUsername());
        hikariConfig.setPassword(getPassword());
        hikariConfig.setMaximumPoolSize(getMaxPoolSize());
        hikariConfig.setMinimumIdle(getMaxPoolIdle());
        hikariConfig.setConnectionTimeout(getConnectionTimeout());
        hikariConfig.setIdleTimeout(getIdleTimeout());
        hikariConfig.setMaxLifetime(getMaxLifetime());

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");

        return hikariConfig;
    }
}