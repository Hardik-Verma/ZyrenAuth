# **ZyrenAuth**

A **secure and flexible authentication plugin** for **Paper Minecraft servers**, designed to provide robust account protection, player privacy, and an intuitive user experience. Developed by **_Pheonix**, ZyrenAuth is suitable for both small private servers and larger production environments.

---

## **Key Features**

### ✅ **Secure Core Authentication**

-   **BCrypt Hashing:** All player passwords are securely hashed using BCrypt, offering strong protection against brute-force attacks.
-   **Configurable Password Policy:** Easily adjust password strength requirements (defaulting to a minimum of 3 characters for user-friendliness).
-   **Optional Anti-Account Sharing:** Prevents multiple players from logging into the same account simultaneously, enhancing security.
-   **Optional IP/Device Locking:** Implement stricter security by restricting account access to trusted IP addresses or devices.

### ✅ **Privacy-Focused Login Experience**

-   **Safe Login Zone:** Upon joining, players are instantly teleported to a designated, safe authentication area (0, 0, 0 in their current world). This prevents their actual in-game location from being revealed before they authenticate.
-   **Player Restrictions:** While unauthenticated, players are completely frozen – unable to move, break/place blocks, chat, or interact with the world.
-   **Location Restoration:** After successfully registering or logging in, players are seamlessly returned to their last known location (or to the world spawn if they are new).

### ✅ **Flexible Data Storage**

-   **MySQL/MariaDB Support (Recommended):**
    -   Integrates with an external database for **persistent storage** of player accounts, security logs, email tokens, and IP restrictions.
    -   Enables all advanced security features.
-   **File-Based Storage (Fallback):**
    -   If MySQL is disabled or unavailable, ZyrenAuth automatically defaults to storing player accounts in `plugins/ZyrenAuth/accounts.json`.
    -   In this mode, core `/register` and `/login` functionality remains, while email-based features and advanced security (IP locking, brute-force) are gracefully disabled.

### ✅ **Optional Email Integration**

> **Note:** Requires MySQL/MariaDB to be enabled and configured correctly, along with SMTP settings.

-   **Account Linking:** Players can link their email addresses to their accounts for enhanced security and recovery options.
-   **Email Confirmation:** Implement email-based verification to confirm player email addresses via `/emailconfirm <token>`.
-   **Password Reset Functionality:** Players can initiate a password reset via `/resetpassword`, receiving a secure token to their linked email, which they can then use with `/resetconfirm` to set a new password.

### ✅ **Intuitive In-Game User Experience**

-   **Elegant Messaging:** All in-game prompts and responses are styled with clear text and colors for a professional and user-friendly interface.
-   **Clear Feedback:** Concise and helpful messages for successful actions or errors.
-   **Comprehensive Help:** The `/za help` command provides an easy-to-understand list of all player and admin commands.
-   **Admin Tools:** `/za status` offers a quick overview of the plugin's operational status, including storage mode and feature enablement.

---

## **Installation & Setup**

### **Requirements**

-   **Java 21** or later
-   **Paper 1.21.1** or any compatible Paper/Spigot/Purpur server version
-   **MySQL 8.0+** or **MariaDB 10.4+** (Highly Recommended for persistent data & full features)
-   An **SMTP Account** (e.g., Gmail App Password) for email features (optional, but required if `emailFeaturesEnabled` is true).

### **Quick Start**

1.  **Download** the latest `ZyrenAuth-1.0.0.jar` from the [releases page](https://github.com/Hardik-Verma/ZyrenAuth/releases).
2.  Place the `ZyrenAuth-1.0.0.jar` file into your server's `plugins/` folder.
3.  **Start your server** once. This will generate the `plugins/ZyrenAuth/` folder and a default `config.json` file inside it.
4.  **Stop your server**.
5.  Edit `plugins/ZyrenAuth/config.json` with your MySQL and SMTP credentials (if enabling these features). Refer to the **Configuration** section below for details.
6.  **Start your server again**.

---

## **Configuration**

The main configuration file is located at `plugins/ZyrenAuth/config.json`.

**Important Security Note:** Never share your `config.json` file publicly (e.g., on GitHub) as it contains sensitive database and email credentials.

### **MySQL/MariaDB Configuration (Optional)**

To enable persistent storage and advanced security features, configure your database:

1.  **Install MySQL/MariaDB Server:** If you don't have one, download and install MySQL Community Server or MariaDB.
2.  **Create a Database and User:** Open your MySQL client (e.g., MySQL Workbench, phpMyAdmin, or command line) and run these SQL commands:

    ```sql
    CREATE DATABASE zyrenauth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE USER 'zyrenauth'@'localhost' IDENTIFIED BY 'YourStrongPasswordHere!';
    GRANT ALL PRIVILEGES ON zyrenauth.* TO 'zyrenauth'@'localhost';
    FLUSH PRIVILEGES;
    ```
    -   **Replace `YourStrongPasswordHere!`** with a strong, unique password.
    -   If your Minecraft server is on a different machine than MySQL, change `'localhost'` to `'%'` (for any host) or the specific IP of your Minecraft server.
3.  **Edit `config.json`:**
    -   Set `"mysqlEnabled": true`
    -   Update the following fields with your database details:
        ```json
        "mysqlEnabled": true,
        "mysqlHost": "127.0.0.1",   // Or your MySQL server IP
        "mysqlPort": "3306",      // Or your MySQL server port (e.g., 2040, 3307)
        "mysqlDatabase": "zyrenauth",
        "mysqlUser": "zyrenauth",
        "mysqlPassword": "YourStrongPasswordHere!",
        ```

### **SMTP (Email) Configuration (Optional)**

> Required if `emailFeaturesEnabled` is true in `config.json`. Gmail is a common choice.

1.  **Generate a Gmail App Password (Recommended):** If using Gmail, enable 2-Step Verification on your Google Account, then generate a 16-character App Password (under Security -> App passwords). This is more secure than using your regular Gmail password.
2.  **Edit `config.json`:**
    -   Set `"emailFeaturesEnabled": true`
    -   Update the following fields with your SMTP details and Gmail App Password:
        ```json
        "emailFeaturesEnabled": true,
        "smtpHost": "smtp.gmail.com", // Or your SMTP server
        "smtpPort": "587", // Common ports: 465 (SSL), 587 (TLS)
        "smtpUsername": "yourgmail@gmail.com", // Your email for sending
        "smtpPassword": "your_16_char_app_password", // The generated App Password
        "smtpAuth": true,
        "smtpStarttlsEnable": true,
        "emailSenderAddress": "yourgmail@gmail.com", // The 'From' address in emails
        ```

### **Other Settings**

-   **`minPasswordLength`**: Minimum characters for passwords (default: 3).
-   **`bcryptStrength`**: BCrypt work factor (higher = more secure but slower).
-   **`maxLoginAttempts` / `lockoutDurationSeconds`**: Brute-force protection settings.
-   **`antiAccountSharingEnabled` / `ipDeviceLockingEnabled`**: Enable/disable these advanced security features.

---

## **Commands**

### **Player Commands**

-   **`/register <password> <confirm_password>`**: Create your secure ZyrenAuth account.
-   **`/login <password>`**: Log in to your existing ZyrenAuth account.
-   **`/addemail <email>`**: Link an email address to your account (requires MySQL & SMTP).
-   **`/emailconfirm <token>`**: Confirm your linked email address using the token sent to your inbox.
-   **`/resetpassword`**: Request a password reset token to be sent to your linked email (requires MySQL & SMTP).
-   **`/resetconfirm <token> <new_password> <confirm_new_password>`**: Complete your password reset using the token and set a new password.

### **Admin Commands (Requires `zyrenauth.admin` permission - default: OP)**

-   **`/za help`**: Displays a list of all ZyrenAuth commands and their usage.
-   **`/za status`**: Shows the current status of the ZyrenAuth plugin, including storage type (MySQL/File) and feature enablement.
-   **`/za reload`**: Reloads the plugin's configuration from `config.json`. (Note: A full server restart is currently recommended for all changes to take effect.)

---

## **Support**

For any issues or questions, please open an issue on the [GitHub repository](https://github.com/Hardik-Verma/ZyrenAuth/issues).

---

## **License**

This project is licensed under the [MIT License](LICENSE).
