package com.pheonix.zyrenauth.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pheonix.zyrenauth.ZyrenAuthPlugin;
import com.pheonix.zyrenauth.util.EmailSender;
import com.pheonix.zyrenauth.util.ZyrenAuthConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class AuthManager {

    // May be null if mysqlEnabled=false or connection failed
    private final DatabaseManager databaseManager;
    // May be null if emailFeaturesEnabled=false
    private final EmailSender emailSender;
    private final ZyrenAuthConfig config;

    private final ConcurrentHashMap<UUID, Boolean> awaitingLogin = new ConcurrentHashMap<>(); // true = login, false = register
    private final ConcurrentHashMap<UUID, Integer> failedLoginAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> ipLockoutTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> accountLockoutTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> playerCurrentIp = new ConcurrentHashMap<>();

    private final Set<UUID> frozenPlayers = Collections.synchronizedSet(new HashSet<>());

    // ---------- Simple file-based storage when MySQL is disabled ----------
    private final boolean usingFileStorage;
    private final File accountsFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static class FileAccount {
        String username;
        String passwordHash;
        String email;
        String lastLoginIp;
        boolean loggedIn;
        // Store last known location for file storage
        double lastX, lastY, lastZ;
        float lastYaw, lastPitch;
        String lastWorld;
    }

    private final Map<UUID, FileAccount> fileAccounts = new ConcurrentHashMap<>();
    private final Map<String, UUID> fileEmailIndex = new ConcurrentHashMap<>();

    // For file-based storage, these tokens are in-memory only (not persistent across restarts)
    private final Map<UUID, String> fileEmailTokens = new ConcurrentHashMap<>();
    private final Map<UUID, Long> fileEmailTokenExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, String> fileResetTokens = new ConcurrentHashMap<>();
    private final Map<UUID, Long> fileResetTokenExpiry = new ConcurrentHashMap<>();
    // ----------------------------------------------------------------------

    // Login staging: teleport to auth location and restore after login
    private final Map<UUID, Location> preLoginLocations = new ConcurrentHashMap<>();

    public AuthManager(DatabaseManager databaseManager, EmailSender emailSender, ZyrenAuthConfig config) {
        this.databaseManager = databaseManager;
        this.emailSender = emailSender;
        this.config = config;

        this.usingFileStorage = (databaseManager == null);
        if (usingFileStorage) {
            ZyrenAuthPlugin plugin = ZyrenAuthPlugin.getInstance();
            this.accountsFile = new File(plugin.getDataFolder(), "accounts.json");
            loadFileAccounts();
            plugin.getLogger().info("[ZyrenAuth] Using file-based storage (accounts.json).");
        } else {
            this.accountsFile = null; // No file storage if MySQL is active
        }

        ZyrenAuthPlugin.getInstance().getLogger().info("[ZyrenAuth] AuthManager initialized.");
    }

    // ------------------------------------------------------------------------
    // File storage helpers
    // ------------------------------------------------------------------------

    private void loadFileAccounts() {
        if (!accountsFile.getParentFile().exists()) {
            accountsFile.getParentFile().mkdirs();
        }
        if (!accountsFile.exists()) {
            saveFileAccounts(); // Create an empty file if it doesn't exist
            return;
        }
        try (FileReader reader = new FileReader(accountsFile)) {
            Type type = new TypeToken<Map<String, FileAccount>>() {}.getType();
            Map<String, FileAccount> raw = gson.fromJson(reader, type);
            if (raw != null) {
                fileAccounts.clear();
                fileEmailIndex.clear();
                for (Map.Entry<String, FileAccount> e : raw.entrySet()) {
                    UUID uuid = UUID.fromString(e.getKey());
                    fileAccounts.put(uuid, e.getValue());
                    if (e.getValue().email != null && !e.getValue().email.isEmpty()) {
                        fileEmailIndex.put(e.getValue().email.toLowerCase(Locale.ROOT), uuid);
                    }
                }
            }
        } catch (Exception e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("[ZyrenAuth] Failed to load accounts.json: " + e.getMessage());
        }
    }

    private void saveFileAccounts() {
        if (accountsFile == null) return; // Only save if using file storage
        try (FileWriter writer = new FileWriter(accountsFile)) {
            Map<String, FileAccount> raw = new LinkedHashMap<>();
            for (Map.Entry<UUID, FileAccount> e : fileAccounts.entrySet()) {
                raw.put(e.getKey().toString(), e.getValue());
            }
            gson.toJson(raw, writer);
        } catch (Exception e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("[ZyrenAuth] Failed to save accounts.json: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // Join / Leave
    // ------------------------------------------------------------------------

    public void handlePlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "0.0.0.0";

        playerCurrentIp.put(uuid, ip);

        // When using MySQL, keep full security features
        if (!usingFileStorage && databaseManager != null) {
            if (config.isAntiAccountSharingEnabled() && databaseManager.isAccountLoggedIn(uuid)) {
                player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "This account is already logged in from another location.");
                player.kickPlayer("Account already in use.");
                databaseManager.logSecurityEvent(uuid, ip, "Anti-Account Sharing", "Attempted login while account already active.");
                return;
            }

            if (config.isIpDeviceLockingEnabled() && databaseManager.isIpRestricted(uuid, ip)) {
                player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Your IP/device is not allowed to access this account.");
                player.kickPlayer("IP/Device Restricted.");
                databaseManager.logSecurityEvent(uuid, ip, "IP/Device Restriction", "Attempted login from restricted IP.");
                return;
            }

            if (isAccountLocked(uuid) || isIpLocked(ip)) {
                player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Too many failed attempts. Please try again later.");
                player.kickPlayer("Temporarily locked out.");
                databaseManager.logSecurityEvent(uuid, ip, "Brute-Force Lockout Active", "Player attempted login while locked out.");
                return;
            }
        }

        // Freeze until auth
        freezePlayer(uuid);

        boolean registered = usingFileStorage ? fileAccounts.containsKey(uuid)
                : (databaseManager != null && databaseManager.isPlayerRegistered(uuid));

        // Store current location or spawn if new, then teleport to auth point
        if (registered) {
            Location lastLoc = getPlayerLastLocation(uuid);
            if (lastLoc != null) {
                preLoginLocations.put(uuid, lastLoc);
            } else {
                preLoginLocations.put(uuid, player.getLocation()); // Fallback if no last location data
            }
        } else {
            preLoginLocations.put(uuid, player.getLocation()); // Store current location, will be new spawn
        }

        // Teleport to a safe, isolated spot (0,0,0 of current world)
        // This hides their true location until login and prevents revealing coords
        Location authLocation = new Location(player.getWorld(), 0.5, 64, 0.5); // Center of block, safe Y
        player.teleport(authLocation);
        player.setFlying(true); // Prevent falling during auth
        player.setAllowFlight(true);

        if (registered) {
            player.sendMessage(ChatColor.DARK_AQUA + "╔═══════════════════════════════╗");
            player.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.AQUA + "Welcome Back, " + ChatColor.GOLD + name + ChatColor.AQUA + "!" + ChatColor.DARK_AQUA + "           ║");
            player.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.WHITE + "Please log in using: " + ChatColor.YELLOW + "/login <password>" + ChatColor.DARK_AQUA + "  ║");
            player.sendMessage(ChatColor.DARK_AQUA + "╚═══════════════════════════════╝");
            awaitingLogin.put(uuid, true);
        } else {
            player.sendMessage(ChatColor.DARK_AQUA + "╔═══════════════════════════════╗");
            player.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.AQUA + "Welcome, " + ChatColor.GOLD + name + ChatColor.AQUA + "!" + ChatColor.DARK_AQUA + "                ║");
            player.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.WHITE + "Create your account with: " + ChatColor.YELLOW + "/register <password> <confirm>" + ChatColor.DARK_AQUA + " ║");
            player.sendMessage(ChatColor.DARK_AQUA + "╚═══════════════════════════════╝");
            awaitingLogin.put(uuid, false);
        }

        ZyrenAuthPlugin.getInstance().getLogger().info(
                "Player " + name + " (" + uuid + ") joined from IP " + ip +
                        ". Awaiting " + (awaitingLogin.get(uuid) ? "login" : "registration") + ". Player frozen and teleported to auth spot.");
    }

    public void handlePlayerLeave(Player player) {
        UUID uuid = player.getUniqueId();
        awaitingLogin.remove(uuid);
        playerCurrentIp.remove(uuid);
        unfreezePlayer(uuid); // Ensure player is unfrozen
        preLoginLocations.remove(uuid); // Remove pre-login location

        if (!usingFileStorage && databaseManager != null) {
            databaseManager.markAccountLoggedOut(uuid);
        } else {
            FileAccount acc = fileAccounts.get(uuid);
            if (acc != null) {
                acc.loggedIn = false;
                // Save current location on disconnect if not logged in, or if using file storage
                // This is a bit tricky: if they are logged in, their location is already saved.
                // If they are not logged in, this is their last known location before auth.
                // For simplicity, we save their location when they join (preLoginLocations) and restore it.
                // If they leave while frozen, their preLoginLocation will be used next time.
                // If they leave *after* logging in, their normal game save will handle it.
                saveFileAccounts();
            }
        }

        ZyrenAuthPlugin.getInstance().getLogger().info(
                "Player " + player.getName() + " (" + uuid + ") disconnected.");
    }

    // ------------------------------------------------------------------------
    // Registration / Login
    // ------------------------------------------------------------------------

    public boolean registerPlayer(Player player, String password) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        String ip = playerCurrentIp.getOrDefault(uuid, "0.0.0.0");

        if (databaseManager == null) {
            player.sendMessage(ChatColor.GOLD + "§l⚠ " + ChatColor.GRAY +
                    "Authentication is running without a database. Registration is not persistent.");
        }

        boolean alreadyRegistered = usingFileStorage ? fileAccounts.containsKey(uuid)
                : (databaseManager != null && databaseManager.isPlayerRegistered(uuid));

        if (alreadyRegistered) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "You are already registered. Use " + ChatColor.YELLOW + "/login" + ChatColor.DARK_RED + " instead.");
            return false;
        }

        if (!isValidPassword(password)) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + config.getPasswordRequirementsMessage());
            return false;
        }

        String hashedPassword = hashPassword(password);

        boolean success;
        if (usingFileStorage) {
            FileAccount acc = new FileAccount();
            acc.username = name;
            acc.passwordHash = hashedPassword;
            acc.email = null;
            acc.lastLoginIp = ip;
            acc.loggedIn = true;
            // For new players, their "last location" is where they joined before auth teleport
            Location initialLoc = preLoginLocations.getOrDefault(uuid, player.getWorld().getSpawnLocation());
            acc.lastX = initialLoc.getX();
            acc.lastY = initialLoc.getY();
            acc.lastZ = initialLoc.getZ();
            acc.lastYaw = initialLoc.getYaw();
            acc.lastPitch = initialLoc.getPitch();
            acc.lastWorld = initialLoc.getWorld().getName();

            fileAccounts.put(uuid, acc);
            saveFileAccounts();
            success = true;
        } else {
            success = databaseManager.createPlayerAccount(uuid, name, hashedPassword, ip);
        }

        if (success) {
            player.sendMessage(ChatColor.DARK_AQUA + "╔═══════════════════════════════╗");
            player.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.GREEN + "§l✔ Registration Successful!" + ChatColor.DARK_AQUA + "      ║");
            player.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.WHITE + "Welcome, " + ChatColor.GOLD + name + ChatColor.WHITE + "! You're now logged in." + ChatColor.DARK_AQUA + "  ║");
            player.sendMessage(ChatColor.DARK_AQUA + "╚═══════════════════════════════╝");
            awaitingLogin.remove(uuid);

            if (!usingFileStorage && databaseManager != null) {
                databaseManager.markAccountLoggedIn(uuid);
                databaseManager.addTrustedIp(uuid, ip);
            } else {
                FileAccount acc = fileAccounts.get(uuid);
                if (acc != null) {
                    acc.loggedIn = true;
                    acc.lastLoginIp = ip;
                    saveFileAccounts();
                }
            }

            unfreezePlayer(uuid);
            restorePlayerLocation(player); // Restore location after successful registration
            ZyrenAuthPlugin.getInstance().getLogger().info(
                    "Player " + name + " (" + uuid + ") registered successfully and returned to their location.");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Registration failed due to a server error. Please try again.");
            if (!usingFileStorage && databaseManager != null) {
                databaseManager.logSecurityEvent(uuid, ip, "Registration Failed", "Database error during registration.");
            }
            return false;
        }
    }

    public boolean loginPlayer(Player player, String password) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        String ip = playerCurrentIp.getOrDefault(uuid, "0.0.0.0");

        if (databaseManager == null) {
            player.sendMessage(ChatColor.GOLD + "§l⚠ " + ChatColor.GRAY +
                    "Authentication is running without a database. Login is not persistent.");
        }

        boolean registered = usingFileStorage ? fileAccounts.containsKey(uuid)
                : (databaseManager != null && databaseManager.isPlayerRegistered(uuid));

        if (!registered) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "You are not registered yet. Use " + ChatColor.YELLOW + "/register" + ChatColor.DARK_RED + " first.");
            return false;
        }

        String storedHash;
        if (usingFileStorage) {
            FileAccount acc = fileAccounts.get(uuid);
            storedHash = (acc != null) ? acc.passwordHash : null;
        } else {
            storedHash = databaseManager.getPlayerHashedPassword(uuid);
        }

        if (storedHash == null) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Could not load your password. Please contact an administrator.");
            if (!usingFileStorage && databaseManager != null) {
                databaseManager.logSecurityEvent(uuid, ip, "Login Failed (DB)", "Could not retrieve hashed password.");
            }
            return false;
        }

        if (verifyPassword(password, storedHash)) {
            player.sendMessage(ChatColor.DARK_AQUA + "╔═══════════════════════════════╗");
            player.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.GREEN + "§l✔ Login Successful!" + ChatColor.DARK_AQUA + "           ║");
            player.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.WHITE + "Welcome back, " + ChatColor.GOLD + name + ChatColor.WHITE + "! Enjoy your stay." + ChatColor.DARK_AQUA + " ║");
            player.sendMessage(ChatColor.DARK_AQUA + "╚═══════════════════════════════╝");
            awaitingLogin.remove(uuid);
            failedLoginAttempts.remove(uuid);

            if (usingFileStorage) {
                FileAccount acc = fileAccounts.get(uuid);
                if (acc != null) {
                    acc.loggedIn = true;
                    acc.lastLoginIp = ip;
                    saveFileAccounts();
                }
            } else if (databaseManager != null) {
                databaseManager.markAccountLoggedIn(uuid);
                databaseManager.updatePlayerLastLoginIp(uuid, ip);
            }

            unfreezePlayer(uuid);
            restorePlayerLocation(player); // Restore location after successful login
            ZyrenAuthPlugin.getInstance().getLogger().info(
                    "Player " + name + " (" + uuid + ") logged in successfully and returned to their location.");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "The password you entered is not correct.");
            // Only enforce brute-force protection when using DB
            if (!usingFileStorage) {
                incrementFailedLoginAttempt(player);
                if (databaseManager != null) {
                    databaseManager.logSecurityEvent(uuid, ip, "Login Failed (Password)", "Incorrect password provided.");
                }
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // Password policy + hashing
    // ------------------------------------------------------------------------

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 3;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(config.getBcryptStrength()));
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("[ZyrenAuth] Invalid BCrypt hash: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // Brute‑force protection (DB only)
    // ------------------------------------------------------------------------

    private void incrementFailedLoginAttempt(Player player) {
        if (usingFileStorage || databaseManager == null) {
            return; // only meaningful with DB
        }

        UUID uuid = player.getUniqueId();
        String ip = playerCurrentIp.getOrDefault(uuid, "0.0.0.0");

        failedLoginAttempts.merge(uuid, 1, Integer::sum);
        int attempts = failedLoginAttempts.get(uuid);

        if (attempts >= config.getMaxLoginAttempts()) {
            long unlockTime = System.currentTimeMillis() + config.getLockoutDurationSeconds() * 1000L;
            accountLockoutTimestamps.put(uuid, unlockTime);
            ipLockoutTimestamps.put(ip, unlockTime);
            databaseManager.logSecurityEvent(uuid, ip, "Brute-force lockout",
                    "Account locked for " + config.getLockoutDurationSeconds() + " seconds.");
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Too many failed attempts. You are temporarily locked out.");
            player.kickPlayer("Too many failed login attempts.");
        } else {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Failed login attempts: " +
                    ChatColor.WHITE + attempts + ChatColor.GRAY + "/" + config.getMaxLoginAttempts());
        }
    }

    public boolean isAccountLocked(UUID uuid) {
        Long unlockTime = accountLockoutTimestamps.get(uuid);
        if (unlockTime != null && System.currentTimeMillis() < unlockTime) {
            return true;
        } else if (unlockTime != null) {
            accountLockoutTimestamps.remove(uuid);
        }
        return false;
    }

    public boolean isIpLocked(String ip) {
        Long unlockTime = ipLockoutTimestamps.get(ip);
        if (unlockTime != null && System.currentTimeMillis() < unlockTime) {
            return true;
        } else if (unlockTime != null) {
            ipLockoutTimestamps.remove(ip);
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Email add / confirm (DB only)
    // ------------------------------------------------------------------------

    public boolean handleEmailAddition(Player player, String email) {
        if (emailSender == null) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Email features are disabled on this server.");
            return false;
        }
        if (usingFileStorage || databaseManager == null) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Email features require MySQL to be enabled and connected.");
            return false;
        }

        UUID uuid = player.getUniqueId();
        String ip = playerCurrentIp.getOrDefault(uuid, "0.0.0.0");

        if (!isValidEmail(email)) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "That doesn't look like a valid email address.");
            return false;
        }
        if (databaseManager.isEmailRegistered(email)) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "This email is already linked to another account.");
            return false;
        }

        String token = generateSecureToken();
        long expiryTime = System.currentTimeMillis() + config.getEmailConfirmationExpiryMinutes() * 60L * 1000L;

        if (databaseManager.storeEmailConfirmationToken(uuid, email, token, expiryTime)) {
            String message =
                    "Hello " + player.getName() + ",\n\n" +
                            "You requested to link this email to your ZyrenAuth account.\n" +
                            "Confirm it by using this token in-game:\n\n" +
                            "/emailconfirm " + token + "\n\n" +
                            "This token will expire in " + config.getEmailConfirmationExpiryMinutes() + " minutes.\n\n" +
                            "If you did not request this, you can safely ignore this email.\n\n" +
                            "Sincerely,\nZyrenAuth";

            boolean sent = emailSender.sendEmail(email, "ZyrenAuth Email Confirmation", message);
            if (sent) {
                player.sendMessage(ChatColor.GREEN + "§l✔ " + ChatColor.AQUA + "A confirmation email has been sent to " +
                        ChatColor.GOLD + email + ChatColor.AQUA + ". Please check your inbox.");
                databaseManager.logSecurityEvent(uuid, ip, "Email Addition", "Confirmation email sent to " + email);
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Could not send the confirmation email. Please try again later.");
                databaseManager.logSecurityEvent(uuid, ip, "Email Addition Failed",
                        "Failed to send confirmation email to " + email);
                return false;
            }
        }

        player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Could not store the confirmation token. Please try again.");
        return false;
    }

    public boolean confirmEmail(Player player, String token) {
        if (usingFileStorage || databaseManager == null) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Email confirmation is not available without MySQL.");
            return false;
        }

        UUID uuid = player.getUniqueId();
        String ip = playerCurrentIp.getOrDefault(uuid, "0.0.0.0");
        String storedEmail = databaseManager.getEmailByConfirmationToken(uuid, token);

        if (storedEmail != null) {
            if (databaseManager.setPlayerEmail(uuid, storedEmail)) {
                databaseManager.deleteEmailConfirmationToken(token);
                player.sendMessage(ChatColor.GREEN + "§l✔ " + ChatColor.AQUA + "Your email " +
                        ChatColor.GOLD + storedEmail + ChatColor.AQUA + " has been confirmed.");
                databaseManager.logSecurityEvent(uuid, ip, "Email Confirmed", "Email " + storedEmail + " confirmed.");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Could not save your email. Please contact an administrator.");
                databaseManager.logSecurityEvent(uuid, ip, "Email Confirmation Failed",
                        "Failed to set email in DB after token validation.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "This confirmation token is invalid or has expired.");
            databaseManager.logSecurityEvent(uuid, ip, "Email Confirmation Failed",
                    "Invalid or expired token provided.");
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Password reset (DB only)
    // ------------------------------------------------------------------------

    public boolean handlePasswordReset(Player player) {
        if (emailSender == null) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Password reset via email is not available on this server.");
            return false;
        }
        if (usingFileStorage || databaseManager == null) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Password reset features require MySQL to be enabled and connected.");
            return false;
        }

        UUID uuid = player.getUniqueId();
        String name = player.getName();
        String ip = playerCurrentIp.getOrDefault(uuid, "0.0.0.0");
        String email = databaseManager.getPlayerEmail(uuid);

        if (email == null || email.isEmpty()) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED +
                    "Your account does not have an email linked. Add one first with " + ChatColor.YELLOW + "/addemail <email>" + ChatColor.DARK_RED + ".");
            return false;
        }

        String token = generateSecureToken();
        long expiryTime = System.currentTimeMillis() + config.getPasswordResetExpiryMinutes() * 60L * 1000L;

        if (databaseManager.storePasswordResetToken(uuid, token, expiryTime)) {
            String message =
                    "Hello " + name + ",\n\n" +
                            "You requested a password reset for your ZyrenAuth account.\n" +
                            "In-game, use the following command:\n\n" +
                            "/resetconfirm " + token + " <new_password> <confirm_new_password>\n\n" +
                            "This token will expire in " + config.getPasswordResetExpiryMinutes() + " minutes.\n\n" +
                            "If you did not request this, you can ignore this email.\n\n" +
                            "Sincerely,\nZyrenAuth";

            boolean sent = emailSender.sendEmail(email, "ZyrenAuth Password Reset", message);
            if (sent) {
                player.sendMessage(ChatColor.GREEN + "§l✔ " + ChatColor.AQUA + "A password reset token has been sent to " +
                        ChatColor.GOLD + email + ChatColor.AQUA + ".");
                databaseManager.logSecurityEvent(uuid, ip, "Password Reset Request", "Reset email sent to " + email);
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Could not send the password reset email. Please try again later.");
                databaseManager.logSecurityEvent(uuid, ip, "Password Reset Failed",
                        "Failed to send reset email to " + email);
                return false;
            }
        }

        player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Could not store the reset token. Please try again.");
        return false;
    }

    public boolean confirmPasswordReset(Player player, String token, String newPassword) {
        if (usingFileStorage || databaseManager == null) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Password reset via token is only available when MySQL is enabled.");
            return false;
        }

        UUID uuid = player.getUniqueId();
        String ip = playerCurrentIp.getOrDefault(uuid, "0.0.0.0");

        if (!isValidPassword(newPassword)) {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + config.getPasswordRequirementsMessage());
            return false;
        }

        String storedToken = databaseManager.getPasswordResetToken(uuid);
        long expiryTime = databaseManager.getPasswordResetTokenExpiry(uuid);

        if (storedToken != null && storedToken.equals(token) && System.currentTimeMillis() < expiryTime) {
            String hashed = hashPassword(newPassword);
            if (databaseManager.updatePlayerPassword(uuid, hashed)) {
                databaseManager.deletePasswordResetToken(uuid);
                player.sendMessage(ChatColor.GREEN + "§l✔ " + ChatColor.AQUA + "Your password has been updated successfully.");
                databaseManager.logSecurityEvent(uuid, ip, "Password Reset Confirmed", "Password updated successfully.");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "Could not update your password. Please contact an administrator.");
                databaseManager.logSecurityEvent(uuid, ip, "Password Reset Failed",
                        "Database error updating password.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "This password reset token is invalid or has expired.");
            databaseManager.logSecurityEvent(uuid, ip, "Password Reset Failed",
                    "Invalid or expired token provided for reset.");
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Freeze helpers
    // ------------------------------------------------------------------------

    public void freezePlayer(UUID uuid) {
        frozenPlayers.add(uuid);
        // PlayerRestrictionListener will handle teleporting to 0,0,0 and keeping them there
    }

    public void unfreezePlayer(UUID uuid) {
        frozenPlayers.remove(uuid);
        // PlayerRestrictionListener will handle allowing movement again
    }

    public boolean isPlayerFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }

    // ------------------------------------------------------------------------
    // Location Management
    // ------------------------------------------------------------------------

    private Location getPlayerLastLocation(UUID uuid) {
        if (usingFileStorage) {
            FileAccount acc = fileAccounts.get(uuid);
            if (acc != null && acc.lastWorld != null) {
                World world = Bukkit.getWorld(acc.lastWorld);
                if (world != null) {
                    return new Location(world, acc.lastX, acc.lastY, acc.lastZ, acc.lastYaw, acc.lastPitch);
                }
            }
        }
        // Fallback or if using DB, let Bukkit handle it
        return null; // For DB, player.getLastLocation() is implicitly handled by MC saves
    }

    private void restorePlayerLocation(Player player) {
        UUID uuid = player.getUniqueId();
        Location restoredLocation = preLoginLocations.remove(uuid); // Get and remove stored location

        if (restoredLocation != null) {
            player.teleport(restoredLocation);
            player.setAllowFlight(false); // Disable flight after teleport
            player.setFlying(false);
            if (usingFileStorage) { // Also update file storage if applicable
                FileAccount acc = fileAccounts.get(uuid);
                if (acc != null) {
                    acc.lastX = restoredLocation.getX();
                    acc.lastY = restoredLocation.getY();
                    acc.lastZ = restoredLocation.getZ();
                    acc.lastYaw = restoredLocation.getYaw();
                    acc.lastPitch = restoredLocation.getPitch();
                    acc.lastWorld = restoredLocation.getWorld().getName();
                    saveFileAccounts();
                }
            }
        } else {
            // If no specific location was stored (e.g., first join, or error), teleport to world spawn
            player.teleport(player.getWorld().getSpawnLocation());
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                Pattern.CASE_INSENSITIVE).matcher(email).matches();
    }
}
