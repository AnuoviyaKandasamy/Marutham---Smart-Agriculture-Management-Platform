package com.marutham.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static final HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
            }
            
            HikariConfig config = new HikariConfig();

            String dbUrl = System.getenv("DB_URL");
            if (dbUrl == null) dbUrl = props.getProperty("db.url", "jdbc:mysql://localhost:3306/marutham_db");
            
            String dbUser = System.getenv("DB_USER");
            if (dbUser == null) dbUser = props.getProperty("db.user", "root");
            
            String dbPass = System.getenv("DB_PASS");
            if (dbPass == null) dbPass = props.getProperty("db.password", "password");

            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPass);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(20000);
            config.setLeakDetectionThreshold(15000);

            dataSource = new HikariDataSource(config);
            logger.info("Database Connection Pool initialized successfully using config from db.properties or env.");
        } catch (Exception e) {
            logger.error("Error initializing HikariCP", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Database Connection Pool closed.");
        }
    }
}