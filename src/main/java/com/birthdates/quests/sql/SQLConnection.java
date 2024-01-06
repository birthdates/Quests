package com.birthdates.quests.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * SQL connection pool
 */
public class SQLConnection {
    private final DataSource hikari;
    private final Executor executor;

    public SQLConnection(Logger logger, ConfigurationSection config) {
        if (config == null) {
            throw new IllegalStateException("Expected sql section in config, not found");
        }

        // Setup executor and connection pool (create default tables as well)
        executor = new SQLExecutor(logger);
        HikariConfig hikariConfig = new HikariConfig();
        String server = config.getString("Host");
        int port = config.getInt("Port");
        String db = config.getString("Database");
        String uri = String.format("jdbc:postgresql://%s:%d/%s?sslmode=disable", server, port, db);
        hikariConfig.setJdbcUrl(uri);
        hikariConfig.setUsername(config.getString("Username"));
        hikariConfig.setPassword(config.getString("Password"));
        hikariConfig.addDataSourceProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setLeakDetectionThreshold(60 * 5000);
        hikari = new HikariDataSource(hikariConfig);
        createTables();
    }

    public void unload() {
        if (hikari instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Try to create default tables
     */
    private void createTables() {
        String progressTable = """
                CREATE TABLE IF NOT EXISTS quest_progress (
                    userId UUID,
                    questId VARCHAR(32),
                    value NUMERIC,
                    expiry BIGINT,
                    status INTEGER DEFAULT 0,
                    PRIMARY KEY (userId, questId)
                )
                """;
        String questTable = """
                CREATE TABLE IF NOT EXISTS quests (
                  id VARCHAR(32) PRIMARY KEY,
                  questType INTEGER DEFAULT 0,
                  requiredAmount NUMERIC DEFAULT 1,
                  description TEXT,
                  permission VARCHAR(32) DEFAULT NULL,
                  icon VARCHAR(32) DEFAULT NULL,
                  target VARCHAR(32) DEFAULT NULL,
                  expiry BIGINT DEFAULT -1,
                  rewards TEXT []
                )
                """;
        String languageTable = """
                CREATE TABLE IF NOT EXISTS language (
                	key VARCHAR(48),
                	text TEXT,
                	language VARCHAR(8),
                	PRIMARY KEY (key, language)
                )
                """;
        try (var connection = hikari.getConnection()) {
            connection.createStatement().execute(questTable);
            connection.createStatement().execute(progressTable);
            connection.createStatement().execute(languageTable);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create tables", e);
        }
    }

    /**
     * Get connection from pool
     *
     * @return {@link  Connection}
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

    /**
     * Get executor for async queries
     *
     * @return {@link Executor}
     */
    public Executor getExecutor() {
        return executor;
    }
}
