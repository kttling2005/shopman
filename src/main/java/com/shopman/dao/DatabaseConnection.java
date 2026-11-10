package com.shopman.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens JDBC connections (class "DatabaseConnection" in the report's detailed class diagram).
 *
 * Settings are looked up in this order, first match wins:
 *   1. {@link #configure(String, String, String)} (used by tests)
 *   2. environment variables SHOPMAN_DB_URL, SHOPMAN_DB_USERNAME, SHOPMAN_DB_PASSWORD, SHOPMAN_DB_DRIVER
 *   3. /db.properties on the classpath (src/main/resources/db.properties)
 */
public final class DatabaseConnection {

    private static volatile String url;
    private static volatile String username;
    private static volatile String password;

    private DatabaseConnection() {
    }

    public static void configure(String jdbcUrl, String user, String pass) {
        url = jdbcUrl;
        username = user;
        password = pass;
    }

    public static Connection getConnection() throws SQLException {
        if (url == null) {
            load();
        }
        return DriverManager.getConnection(url, username, password);
    }

    private static synchronized void load() throws SQLException {
        if (url != null) {
            return;
        }
        Properties p = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            throw new SQLException("Cannot read db.properties", e);
        }
        String u = firstNonBlank(System.getenv("SHOPMAN_DB_URL"), p.getProperty("db.url"));
        if (u == null) {
            throw new SQLException("Database URL is not configured (SHOPMAN_DB_URL or db.properties)");
        }
        username = firstNonBlank(System.getenv("SHOPMAN_DB_USERNAME"), p.getProperty("db.username"));
        password = firstNonBlank(System.getenv("SHOPMAN_DB_PASSWORD"), p.getProperty("db.password"));
        // Inside Tomcat, DriverManager does not discover a driver jar that lives in WEB-INF/lib,
        // which ends in "No suitable driver found". Loading the class registers the driver.
        String driver = firstNonBlank(System.getenv("SHOPMAN_DB_DRIVER"), p.getProperty("db.driver"));
        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC driver class " + driver + " not found - is the driver jar in WEB-INF/lib?", e);
            }
        }
        url = u;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return (b != null && !b.isBlank()) ? b : null;
    }
}
