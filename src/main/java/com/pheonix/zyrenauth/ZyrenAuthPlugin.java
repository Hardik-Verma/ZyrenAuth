package com.pheonix.zyrenauth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pheonix.zyrenauth.command.*;
import com.pheonix.zyrenauth.listener.PlayerRestrictionListener;
import com.pheonix.zyrenauth.manager.AuthManager;
import com.pheonix.zyrenauth.manager.DatabaseManager;
import com.pheonix.zyrenauth.util.EmailSender;
import com.pheonix.zyrenauth.util.ZyrenAuthConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ZyrenAuthPlugin extends JavaPlugin {

    private static ZyrenAuthPlugin instance;

    private ZyrenAuthConfig configObject;
    private DatabaseManager databaseManager;
    private EmailSender emailSender;
    private AuthManager authManager;

    public static ZyrenAuthPlugin getInstance() {
        return instance;
    }

    public ZyrenAuthConfig getZyrenConfig() {
        return configObject;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("[ZyrenAuth] Initializing...");

        this.configObject = loadJsonConfig();

        // MySQL optional
        if (configObject.isMysqlEnabled()) {
            this.databaseManager = new DatabaseManager(configObject);
            if (!databaseManager.isConnected()) {
                getLogger().severe("[ZyrenAuth] MySQL is enabled but connection failed. Running in non-persistent (memory-only) mode.");
                databaseManager = null; // Force file storage if DB fails
            }
        } else {
            getLogger().warning("[ZyrenAuth] MySQL is disabled in config. No data will be stored persistently.");
            databaseManager = null;
        }

        // Email optional (only available with MySQL)
        if (configObject.isEmailFeaturesEnabled() && databaseManager != null) {
            this.emailSender = new EmailSender(configObject);
        } else {
            getLogger().warning("[ZyrenAuth] Email features are disabled or MySQL is not active. Email commands will be unavailable.");
            emailSender = null;
        }

        this.authManager = new AuthManager(databaseManager, emailSender, configObject);

        Bukkit.getPluginManager().registerEvents(new PlayerRestrictionListener(authManager), this);
        registerCommands();

        // Re-check DB connection after all managers are set up, in case of late init issues
        if (databaseManager != null && !databaseManager.isConnected()) {
            getLogger().severe("[ZyrenAuth] Database not connected after initial setup, trying to reconnect...");
            databaseManager.connect();
            if (!databaseManager.isConnected()) {
                getLogger().severe("[ZyrenAuth] Failed to reconnect to database. Continuing in non-persistent mode.");
                // If it fails again, ensure databaseManager is null so AuthManager uses file storage
                // This state is already handled from initial setup, this is just a re-check
            } else {
                getLogger().info("[ZyrenAuth] Database reconnected successfully.");
            }
        }

        getLogger().info("[ZyrenAuth] Enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[ZyrenAuth] Server stopping, closing database connections.");
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().info("[ZyrenAuth] Cleaned up resources.");
    }

    private void registerCommands() {
        if (getCommand("register") != null) {
            getCommand("register").setExecutor(new RegisterCommand(authManager));
        }
        if (getCommand("login") != null) {
            getCommand("login").setExecutor(new LoginCommand(authManager));
        }
        if (getCommand("addemail") != null) {
            getCommand("addemail").setExecutor(new AddEmailCommand(authManager));
        }
        if (getCommand("emailconfirm") != null) {
            getCommand("emailconfirm").setExecutor(new EmailConfirmCommand(authManager));
        }
        if (getCommand("resetpassword") != null) {
            getCommand("resetpassword").setExecutor(new ResetPasswordCommand(authManager));
        }
        if (getCommand("resetconfirm") != null) {
            getCommand("resetconfirm").setExecutor(new ResetConfirmCommand(authManager));
        }
        if (getCommand("za") != null) {
            ZyrenAuthAdminCommand zaCommand = new ZyrenAuthAdminCommand(this, authManager);
            getCommand("za").setExecutor(zaCommand);
            getCommand("za").setTabCompleter(zaCommand); // Register tab completer
        }
    }

    private ZyrenAuthConfig loadJsonConfig() {
        File folder = getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File configFile = new File(folder, "config.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ZyrenAuthConfig loadedConfig = new ZyrenAuthConfig();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                loadedConfig = gson.fromJson(reader, ZyrenAuthConfig.class);
                getLogger().info("[ZyrenAuth] Configuration loaded from: " + configFile.getAbsolutePath());
                // Re-save to ensure any new default fields are added to the file
                try (FileWriter writer = new FileWriter(configFile)) {
                    gson.toJson(loadedConfig, writer);
                }
            } catch (IOException e) {
                getLogger().severe("[ZyrenAuth] Failed to read configuration. Using defaults. Error: " + e.getMessage());
            }
        } else {
            getLogger().info("[ZyrenAuth] Configuration file not found. Creating default config.");
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(loadedConfig, writer);
                getLogger().info("[ZyrenAuth] Default configuration saved to: " + configFile.getAbsolutePath());
            } catch (IOException e) {
                getLogger().severe("[ZyrenAuth] Failed to save default configuration. Error: " + e.getMessage());
            }
        }
        return loadedConfig;
    }
}
