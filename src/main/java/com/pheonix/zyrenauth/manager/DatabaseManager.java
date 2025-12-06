// src/main/java/com/pheonix/zyrenauth/manager/DatabaseManager.java
package com.pheonix.zyrenauth.manager;

import com.pheonix.zyrenauth.ZyrenAuthPlugin;
import com.pheonix.zyrenauth.util.ZyrenAuthConfig;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private Connection connection;
    private final ZyrenAuthConfig config;

    public DatabaseManager(ZyrenAuthConfig config) {
        this.config = config;
        connect();
        createTables();
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + config.getMysqlHost() + ":" + config.getMysqlPort() +
                    "/" + config.getMysqlDatabase() + "?autoReconnect=true&useSSL=false";
            connection = DriverManager.getConnection(url, config.getMysqlUser(), config.getMysqlPassword());
            ZyrenAuthPlugin.getInstance().getLogger().info("Connected to MySQL database: " + config.getMysqlDatabase());
        } catch (SQLException | ClassNotFoundException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Failed to connect to MySQL database: " + e.getMessage());
            connection = null;
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error checking database connection: " + e.getMessage());
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                ZyrenAuthPlugin.getInstance().getLogger().info("Closed MySQL database connection.");
            }
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Failed to close MySQL database connection: " + e.getMessage());
        }
    }

    private void createTables() {
        if (!isConnected()) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Cannot create tables: Database not connected.");
            return;
        }
        try (Statement statement = connection.createStatement()) {

            statement.execute("CREATE TABLE IF NOT EXISTS `players` (" +
                    "`uuid` VARCHAR(36) PRIMARY KEY NOT NULL," +
                    "`username` VARCHAR(64) NOT NULL," +
                    "`password_hash` VARCHAR(60) NOT NULL," +
                    "`email` VARCHAR(255) DEFAULT NULL," +
                    "`last_login_ip` VARCHAR(45) DEFAULT NULL," +
                    "`is_logged_in` TINYINT(1) DEFAULT 0," +
                    "`registered_at` DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            statement.execute("CREATE TABLE IF NOT EXISTS `email_confirmation_tokens` (" +
                    "`token` VARCHAR(32) PRIMARY KEY NOT NULL," +
                    "`player_uuid` VARCHAR(36) NOT NULL," +
                    "`email` VARCHAR(255) NOT NULL," +
                    "`expiry_time` BIGINT NOT NULL," +
                    "FOREIGN KEY (`player_uuid`) REFERENCES `players`(`uuid`) ON DELETE CASCADE" +
                    ");");

            statement.execute("CREATE TABLE IF NOT EXISTS `password_reset_tokens` (" +
                    "`player_uuid` VARCHAR(36) PRIMARY KEY NOT NULL," +
                    "`token` VARCHAR(32) NOT NULL," +
                    "`expiry_time` BIGINT NOT NULL," +
                    "FOREIGN KEY (`player_uuid`) REFERENCES `players`(`uuid`) ON DELETE CASCADE" +
                    ");");

            statement.execute("CREATE TABLE IF NOT EXISTS `ip_restrictions` (" +
                    "`player_uuid` VARCHAR(36) NOT NULL," +
                    "`ip_address` VARCHAR(45) NOT NULL," +
                    "`is_trusted` TINYINT(1) DEFAULT 1," +
                    "PRIMARY KEY (`player_uuid`, `ip_address`)," +
                    "FOREIGN KEY (`player_uuid`) REFERENCES `players`(`uuid`) ON DELETE CASCADE" +
                    ");");

            statement.execute("CREATE TABLE IF NOT EXISTS `security_logs` (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "`timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "`player_uuid` VARCHAR(36)," +
                    "`ip_address` VARCHAR(45)," +
                    "`event_type` VARCHAR(100) NOT NULL," +
                    "`details` TEXT" +
                    ");");

            ZyrenAuthPlugin.getInstance().getLogger().info("Database tables checked/created successfully.");
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Failed to create database tables: " + e.getMessage());
        }
    }

    public boolean isPlayerRegistered(UUID uuid) {
        if (!isConnected()) return false;
        String sql = "SELECT COUNT(*) FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error checking if player " + uuid + " is registered: " + e.getMessage());
            return false;
        }
    }

    public boolean createPlayerAccount(UUID uuid, String username, String passwordHash, String ipAddress) {
        if (!isConnected()) return false;
        String sql = "INSERT INTO players (uuid, username, password_hash, last_login_ip, is_logged_in) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, username);
            ps.setString(3, passwordHash);
            ps.setString(4, ipAddress);
            ps.setBoolean(5, true);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error creating player account for " + username + ": " + e.getMessage());
            return false;
        }
    }

    public String getPlayerHashedPassword(UUID uuid) {
        if (!isConnected()) return null;
        String sql = "SELECT password_hash FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("password_hash") : null;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error getting password hash for player " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    public boolean updatePlayerPassword(UUID uuid, String newPasswordHash) {
        if (!isConnected()) return false;
        String sql = "UPDATE players SET password_hash = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, uuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error updating password for player " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public String getPlayerEmail(UUID uuid) {
        if (!isConnected()) return null;
        String sql = "SELECT email FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("email") : null;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error getting email for player " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    public boolean setPlayerEmail(UUID uuid, String email) {
        if (!isConnected()) return false;
        String sql = "UPDATE players SET email = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, uuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error setting email for player " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isEmailRegistered(String email) {
        if (!isConnected()) return false;
        String sql = "SELECT COUNT(*) FROM players WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error checking if email " + email + " is registered: " + e.getMessage());
            return false;
        }
    }

    public boolean storeEmailConfirmationToken(UUID playerUuid, String email, String token, long expiryTime) {
        if (!isConnected()) return false;

        String deleteSql = "DELETE FROM email_confirmation_tokens WHERE player_uuid = ?";
        try (PreparedStatement psDelete = connection.prepareStatement(deleteSql)) {
            psDelete.setString(1, playerUuid.toString());
            psDelete.executeUpdate();
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().warning("Failed to delete old email confirmation token for player " + playerUuid + ": " + e.getMessage());
        }

        String insertSql = "INSERT INTO email_confirmation_tokens (token, player_uuid, email, expiry_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setString(1, token);
            ps.setString(2, playerUuid.toString());
            ps.setString(3, email);
            ps.setLong(4, expiryTime);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error storing email confirmation token for player " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public String getEmailByConfirmationToken(UUID playerUuid, String token) {
        if (!isConnected()) return null;
        String sql = "SELECT email, expiry_time FROM email_confirmation_tokens WHERE player_uuid = ? AND token = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long expiry = rs.getLong("expiry_time");
                if (System.currentTimeMillis() < expiry) {
                    return rs.getString("email");
                } else {
                    ZyrenAuthPlugin.getInstance().getLogger().warning("Expired email confirmation token for player " + playerUuid);
                    deleteEmailConfirmationToken(token);
                }
            }
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error getting email by confirmation token for player " + playerUuid + ": " + e.getMessage());
        }
        return null;
    }

    public boolean deleteEmailConfirmationToken(String token) {
        if (!isConnected()) return false;
        String sql = "DELETE FROM email_confirmation_tokens WHERE token = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error deleting email confirmation token " + token + ": " + e.getMessage());
            return false;
        }
    }

    public boolean storePasswordResetToken(UUID playerUuid, String token, long expiryTime) {
        if (!isConnected()) return false;
        String upsertSql = "INSERT INTO password_reset_tokens (player_uuid, token, expiry_time) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE token = VALUES(token), expiry_time = VALUES(expiry_time)";
        try (PreparedStatement ps = connection.prepareStatement(upsertSql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, token);
            ps.setLong(3, expiryTime);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error storing password reset token for player " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public String getPasswordResetToken(UUID playerUuid) {
        if (!isConnected()) return null;
        String sql = "SELECT token, expiry_time FROM password_reset_tokens WHERE player_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long expiry = rs.getLong("expiry_time");
                if (System.currentTimeMillis() < expiry) {
                    return rs.getString("token");
                } else {
                    ZyrenAuthPlugin.getInstance().getLogger().warning("Expired password reset token for player " + playerUuid);
                    deletePasswordResetToken(playerUuid);
                }
            }
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error getting password reset token for player " + playerUuid + ": " + e.getMessage());
        }
        return null;
    }

    public long getPasswordResetTokenExpiry(UUID playerUuid) {
        if (!isConnected()) return 0;
        String sql = "SELECT expiry_time FROM password_reset_tokens WHERE player_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getLong("expiry_time") : 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error getting password reset token expiry for player " + playerUuid + ": " + e.getMessage());
            return 0;
        }
    }

    public boolean deletePasswordResetToken(UUID playerUuid) {
        if (!isConnected()) return false;
        String sql = "DELETE FROM password_reset_tokens WHERE player_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error deleting password reset token for player " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isAccountLoggedIn(UUID playerUuid) {
        if (!isConnected()) return false;
        String sql = "SELECT is_logged_in FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getBoolean("is_logged_in");
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error checking login status for player " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean markAccountLoggedIn(UUID playerUuid) {
        if (!isConnected()) return false;
        String sql = "UPDATE players SET is_logged_in = TRUE WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error marking player " + playerUuid + " as logged in: " + e.getMessage());
            return false;
        }
    }

    public boolean markAccountLoggedOut(UUID playerUuid) {
        if (!isConnected()) return false;
        String sql = "UPDATE players SET is_logged_in = FALSE WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error marking player " + playerUuid + " as logged out: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePlayerLastLoginIp(UUID playerUuid, String ipAddress) {
        if (!isConnected()) return false;
        String sql = "UPDATE players SET last_login_ip = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ipAddress);
            ps.setString(2, playerUuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error updating last login IP for player " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isIpRestricted(UUID playerUuid, String ipAddress) {
        if (!isConnected()) return false;
        String sql = "SELECT is_trusted FROM ip_restrictions WHERE player_uuid = ? AND ip_address = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, ipAddress);
            ResultSet rs = ps.executeQuery();
            return rs.next() && !rs.getBoolean("is_trusted");
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error checking IP restriction for player " + playerUuid + " with IP " + ipAddress + ": " + e.getMessage());
            return false;
        }
    }

    public boolean addTrustedIp(UUID playerUuid, String ipAddress) {
        if (!isConnected()) return false;
        String sql = "INSERT INTO ip_restrictions (player_uuid, ip_address, is_trusted) VALUES (?, ?, TRUE) " +
                "ON DUPLICATE KEY UPDATE is_trusted = TRUE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, ipAddress);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error adding trusted IP " + ipAddress + " for player " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean banIp(UUID playerUuid, String ipAddress) {
        if (!isConnected()) return false;
        String sql = "INSERT INTO ip_restrictions (player_uuid, ip_address, is_trusted) VALUES (?, ?, FALSE) " +
                "ON DUPLICATE KEY UPDATE is_trusted = FALSE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, ipAddress);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error banning IP " + ipAddress + " for player " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean logSecurityEvent(UUID playerUuid, String ipAddress, String eventType, String details) {
        if (!isConnected()) return false;
        String sql = "INSERT INTO security_logs (player_uuid, ip_address, event_type, details) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid != null ? playerUuid.toString() : null);
            ps.setString(2, ipAddress);
            ps.setString(3, eventType);
            ps.setString(4, details);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Error logging security event (Type: " + eventType + ", Player: " + playerUuid + "): " + e.getMessage());
            return false;
        }
    }
}
