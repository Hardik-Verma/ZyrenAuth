// src/main/java/com/pheonix/zyrenauth/manager/IAuthStorage.java
package com.pheonix.zyrenauth.manager;

import java.util.UUID;

public interface IAuthStorage {

    // Core Player Account Management
    boolean isPlayerRegistered(UUID uuid);
    boolean createPlayerAccount(UUID uuid, String username, String passwordHash, String ipAddress);
    String getPlayerHashedPassword(UUID uuid);
    boolean updatePlayerPassword(UUID uuid, String newPasswordHash);

    // Login/Logout Status
    boolean isAccountLoggedIn(UUID playerUuid);
    boolean markAccountLoggedIn(UUID playerUuid);
    boolean markAccountLoggedOut(UUID playerUuid);
    boolean updatePlayerLastLoginIp(UUID playerUuid, String ipAddress);

    // Email Management (can be no-op or in-memory if DB is off)
    String getPlayerEmail(UUID uuid);
    boolean setPlayerEmail(UUID uuid, String email);
    boolean isEmailRegistered(String email);
    boolean storeEmailConfirmationToken(UUID playerUuid, String email, String token, long expiryTime);
    String getEmailByConfirmationToken(UUID playerUuid, String token);
    boolean deleteEmailConfirmationToken(String token);

    // Password Reset (can be no-op or in-memory if DB is off)
    boolean storePasswordResetToken(UUID playerUuid, String token, long expiryTime);
    String getPasswordResetToken(UUID playerUuid);
    long getPasswordResetTokenExpiry(UUID playerUuid);
    boolean deletePasswordResetToken(UUID playerUuid);

    // IP Restrictions (can be no-op or in-memory if DB is off)
    boolean isIpRestricted(UUID playerUuid, String ipAddress);
    boolean addTrustedIp(UUID playerUuid, String ipAddress);
    boolean banIp(UUID playerUuid, String ipAddress);

    // Security Logging (can be no-op if DB is off)
    boolean logSecurityEvent(UUID playerUuid, String ipAddress, String eventType, String details);

    // For file-based storage, to explicitly save/load
    default void save() {}
    default void load() {}
    default void close() {}
}
