package com.pheonix.zyrenauth.util;

public class ZyrenAuthConfig {

    // MySQL Database Settings
    private String mysqlHost = "localhost";
    private String mysqlPort = "3306";
    private String mysqlDatabase = "zyrenauth";
    private String mysqlUser = "root";
    private String mysqlPassword = "your_mysql_password";

    // Email (SMTP) Settings
    private String smtpHost = "smtp.example.com";
    private String smtpPort = "587";
    private String smtpUsername = "your_email@example.com";
    private String smtpPassword = "your_email_password";
    private boolean smtpAuth = true;
    private boolean smtpStarttlsEnable = true;
    private String emailSenderAddress = "no-reply@zyrenauth.com";

    // Feature toggles
    private boolean mysqlEnabled = true;
    private boolean emailFeaturesEnabled = true;

    // Password Policy (now mostly unused, but kept for compatibility)
    private int minPasswordLength = 8;
    private boolean requireDigit = true;
    private boolean requireLowercase = true;
    private boolean requireUppercase = true;
    private boolean requireSpecialChar = true;
    private int bcryptStrength = 12;

    // Brute-Force Protection
    private int maxLoginAttempts = 5;
    private long lockoutDurationSeconds = 300;

    // Anti-Account Sharing
    private boolean antiAccountSharingEnabled = true;

    // IP & Device Locking
    private boolean ipDeviceLockingEnabled = true;

    // Email Confirmation & Password Reset
    private int emailConfirmationExpiryMinutes = 30;
    private int passwordResetExpiryMinutes = 60;
    private String webServerUrl = "http://localhost:8080/zyrenauth";

    // --- Getters ---
    public String getMysqlHost() { return mysqlHost; }
    public String getMysqlPort() { return mysqlPort; }
    public String getMysqlDatabase() { return mysqlDatabase; }
    public String getMysqlUser() { return mysqlUser; }
    public String getMysqlPassword() { return mysqlPassword; }

    public String getSmtpHost() { return smtpHost; }
    public String getSmtpPort() { return smtpPort; }
    public String getSmtpUsername() { return smtpUsername; }
    public String getSmtpPassword() { return smtpPassword; }
    public boolean isSmtpAuth() { return smtpAuth; }
    public boolean isSmtpStarttlsEnable() { return smtpStarttlsEnable; }
    public String getEmailSenderAddress() { return emailSenderAddress; }

    public boolean isMysqlEnabled() { return mysqlEnabled; }
    public boolean isEmailFeaturesEnabled() { return emailFeaturesEnabled; }

    public int getMinPasswordLength() { return minPasswordLength; }
    public boolean isRequireDigit() { return requireDigit; }
    public boolean isRequireLowercase() { return requireLowercase; }
    public boolean isRequireUppercase() { return requireUppercase; }
    public boolean isRequireSpecialChar() { return requireSpecialChar; }
    public int getBcryptStrength() { return bcryptStrength; }

    public int getMaxLoginAttempts() { return maxLoginAttempts; }
    public long getLockoutDurationSeconds() { return lockoutDurationSeconds; }

    public boolean isAntiAccountSharingEnabled() { return antiAccountSharingEnabled; }
    public boolean isIpDeviceLockingEnabled() { return ipDeviceLockingEnabled; }

    public int getEmailConfirmationExpiryMinutes() { return emailConfirmationExpiryMinutes; }
    public int getPasswordResetExpiryMinutes() { return passwordResetExpiryMinutes; }
    public String getWebServerUrl() { return webServerUrl; }

    // Friendly password requirements text used in messages
    public String getPasswordRequirementsMessage() {
        return "Your password must be at least 3 characters long.";
    }

    // --- Setters ---
    public void setMysqlHost(String mysqlHost) { this.mysqlHost = mysqlHost; }
    public void setMysqlPort(String mysqlPort) { this.mysqlPort = mysqlPort; }
    public void setMysqlDatabase(String mysqlDatabase) { this.mysqlDatabase = mysqlDatabase; }
    public void setMysqlUser(String mysqlUser) { this.mysqlUser = mysqlUser; }
    public void setMysqlPassword(String mysqlPassword) { this.mysqlPassword = mysqlPassword; }

    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }
    public void setSmtpPort(String smtpPort) { this.smtpPort = smtpPort; }
    public void setSmtpUsername(String smtpUsername) { this.smtpUsername = smtpUsername; }
    public void setSmtpPassword(String smtpPassword) { this.smtpPassword = smtpPassword; }
    public void setSmtpAuth(boolean smtpAuth) { this.smtpAuth = smtpAuth; }
    public void setSmtpStarttlsEnable(boolean smtpStarttlsEnable) { this.smtpStarttlsEnable = smtpStarttlsEnable; }
    public void setEmailSenderAddress(String emailSenderAddress) { this.emailSenderAddress = emailSenderAddress; }

    public void setMysqlEnabled(boolean mysqlEnabled) { this.mysqlEnabled = mysqlEnabled; }
    public void setEmailFeaturesEnabled(boolean emailFeaturesEnabled) { this.emailFeaturesEnabled = emailFeaturesEnabled; }

    public void setMinPasswordLength(int minPasswordLength) { this.minPasswordLength = minPasswordLength; }
    public void setRequireDigit(boolean requireDigit) { this.requireDigit = requireDigit; }
    public void setRequireLowercase(boolean requireLowercase) { this.requireLowercase = requireLowercase; }
    public void setRequireUppercase(boolean requireUppercase) { this.requireUppercase = requireUppercase; }
    public void setRequireSpecialChar(boolean requireSpecialChar) { this.requireSpecialChar = requireSpecialChar; }
    public void setBcryptStrength(int bcryptStrength) { this.bcryptStrength = bcryptStrength; }

    public void setMaxLoginAttempts(int maxLoginAttempts) { this.maxLoginAttempts = maxLoginAttempts; }
    public void setLockoutDurationSeconds(long lockoutDurationSeconds) { this.lockoutDurationSeconds = lockoutDurationSeconds; }

    public void setAntiAccountSharingEnabled(boolean antiAccountSharingEnabled) { this.antiAccountSharingEnabled = antiAccountSharingEnabled; }
    public void setIpDeviceLockingEnabled(boolean ipDeviceLockingEnabled) { this.ipDeviceLockingEnabled = ipDeviceLockingEnabled; }

    public void setEmailConfirmationExpiryMinutes(int emailConfirmationExpiryMinutes) { this.emailConfirmationExpiryMinutes = emailConfirmationExpiryMinutes; }
    public void setPasswordResetExpiryMinutes(int passwordResetExpiryMinutes) { this.passwordResetExpiryMinutes = passwordResetExpiryMinutes; }
    public void setWebServerUrl(String webServerUrl) { this.webServerUrl = webServerUrl; }
}
