# **ZyrenAuth**

A modern, modular authentication system for **Paper** servers, designed for both small private servers and serious production environments.

ZyrenAuth gives your players secure accounts, email-based recovery (optional), IP/device protection (optional), and a clean login flow that doesn't leak their real coordinates before they authenticate.

Created by **_Pheonix**.

---

## **Features**

### ✅ **Core Authentication**

-   **/register** and **/login** to protect player accounts
-   Passwords hashed with **BCrypt** for strong security
-   Configurable password policy (default: **at least 3 characters**, simple & user-friendly)
-   Optional **anti-account sharing** (prevents the same account from logging in from multiple locations)
-   Optional **IP/device locking** for stricter account security

### ✅ **Privacy-Friendly Login Flow**

-   On join, players are:
    -   Teleported to a safe **authentication location** (0, 0, 0 in their current world)
    -   Completely **frozen** (unable to move, break blocks, place blocks, chat, or interact)
-   After successful **/login** or **/register**:
    -   The player is **unfrozen**
    -   Teleported back to their **original location** (or the world spawn for new players)
-   This ensures players' actual in-game coordinates are not revealed until they have authenticated.

### ✅ **Flexible Storage Options**

-   **MySQL/MariaDB Mode** (Recommended for persistence and full features)
    -   Enables comprehensive security features: IP/device locking, email integration, security event logging, and brute-force protection.
    -   All account data is stored persistently in your MySQL/MariaDB database.
-   **File Mode (accounts.json)** (Fallback when MySQL is disabled or unavailable)
    -   **/register** and **/login** commands still function.
    -   Account data is stored in `plugins/ZyrenAuth/accounts.json`.
    -   Email-based features (confirmation, password reset) and advanced security (IP locking, brute-force) are automatically disabled in this mode.

### ✅ **Optional Email Integration**

> **Note:** Requires MySQL/MariaDB to be enabled and configured correctly, along with SMTP settings.

-   Players can **link an email** to their account for enhanced security.
-   **Email confirmation** via `/emailconfirm <token>` ensures valid email addresses.
-   Secure **password reset flow**:
    -   `/resetpassword` sends a unique token to the player's registered email.
    -   `/resetconfirm <token> <new_password> <confirm_new_password>` allows players to set a new password.
-   Ideal for servers requiring robust account recovery and verification.

### ✅ **Beautiful In-Game User Experience**

-   **Styled Auth Prompts:** Visually appealing login/registration messages with clear instructions.
-   **Clear Feedback:** Concise and helpful messages for successful actions or errors.
-   **Comprehensive Help:** `/za help` provides all player commands and outlines admin commands.
-   **Admin Tools:** `/za status` to view plugin health and configuration, and `/za reload` to apply config changes (requires server restart for full effect).

---

## **Commands**

### **Player Commands (Available to all players)**

-   **`/register <password> <confirm_password>`**
    -   **Description:** Create your secure ZyrenAuth account.
    -   **Example:** `/register MyPass123! MyPass123!`
-   **`/login <password>`**
    -   **Description:** Log in to your existing ZyrenAuth account.
    -   **Example:** `/login MyPass123!`
-   **`/addemail <email>`**
    -   **Description:** Link an email address to your account (requires MySQL & SMTP).
    -   **Example:** `/addemail myemail@example.com`
-   **`/emailconfirm <token>`**
    -   **Description:** Confirm your linked email address using the token sent to your inbox.
    -   **Example:** `/emailconfirm 1a2b3c4d5e6f7g8h`
-   **`/resetpassword`**
    -   **Description:** Request a password reset token to be sent to your linked email (requires MySQL & SMTP).
-   **`/resetconfirm <token> <new_password> <confirm_new_password>`**
    -   **Description:** Complete your password reset using the token and set a new password.
    -   **Example:** `/resetconfirm 1a2b3c4d5e6f7g8h NewPass456! NewPass456!`

### **Admin Commands (Requires `zyrenauth.admin` permission - default: OP)**

-   **`/za help`**
    -   **Description:** Displays a list of all ZyrenAuth commands and their usage.
-   **`/za status`**
    -   **Description:** Shows the current status of the ZyrenAuth plugin, including storage type (MySQL/File) and feature enablement.
-   **`/za reload`**
    -   **Description:** Reloads the plugin's configuration from `config.json`. (Note: A full server restart is currently recommended for all changes to take effect.)

---

## **Installation & Setup**

### **1. Requirements**

-   **Java 21** or later
-   **Paper 1.21.1** or any compatible Paper/Spigot/Purpur server version
-   **MySQL 8.0+** or **MariaDB 10.4+** (Highly Recommended for persistent data & full features)
-   An **SMTP Account** (e.g., Gmail App Password) for email features (optional, but required if `emailFeaturesEnabled` is true).

### **2. Server Setup (Basic)**

1.  **Download** the latest `ZyrenAuth-1.0.0.jar` from the [releases page](https://github.com/_Pheonix/ZyrenAuth/releases) (or wherever you host it).
2.  Place the `ZyrenAuth-1.0.0.jar` file into your server's `plugins/` folder.
3.  **Start your server** once. This will generate the `plugins/ZyrenAuth/` folder and a default `config.json` inside it.
4.  **Stop your server**.

### **3. MySQL/MariaDB Configuration (Highly Recommended)**

1.  **Install MySQL/MariaDB Server:** If you don't have one, download and install MySQL Community Server or MariaDB.
    -   MySQL: [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/)
    -   MariaDB: [https://mariadb.org/download/](https://mariadb.org/download/)
2.  **Create a Database and User:** Open your MySQL client (e.g., MySQL Workbench, phpMyAdmin, or command line) and run these SQL commands:

    ```sql
    CREATE DATABASE zyrenauth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE USER 'zyrenauth'@'localhost' IDENTIFIED BY 'YourStrongPasswordHere!';
    GRANT ALL PRIVILEGES ON zyrenauth.* TO 'zyrenauth'@'localhost';
    FLUSH PRIVILEGES;
    ```
    -   **Replace `YourStrongPasswordHere!`** with a strong, unique password.
    -   If your Minecraft server is on a different machine than MySQL, change `'localhost'` to `'%'` (for any host) or the specific IP of your Minecraft server.

3.  **Edit `plugins/ZyrenAuth/config.json`:**
    -   Set `"mysqlEnabled": true`
    -   Update the following fields with your database details:
        ```json
        "mysqlHost": "127.0.0.1",   // Or your MySQL server IP
        "mysqlPort": "3306",      // Or your MySQL server port (e.g., 2040, 3307)
        "mysqlDatabase": "zyrenauth",
        "mysqlUser": "zyrenauth",
        "mysqlPassword": "YourStrongPasswordHere!",
        ```

### **4. SMTP (Email) Configuration (Optional)**

> Required if `emailFeaturesEnabled` is true in `config.json`. Gmail is a common choice.

1.  **Generate a Gmail App Password:**
    -   Go to your Google Account ([https://myaccount.google.com](https://myaccount.google.com/)).
    -   Navigate to **Security**.
    -   Ensure **2-Step Verification** is turned **On**.
    -   Under "Signing in to Google", click on **App passwords**.
    -   Follow the prompts to create a new app password for "Mail" on an "Other (Custom name)" device (e.g., `ZyrenAuth`).
    -   Copy the generated **16-character password** (without spaces).
2.  **Edit `plugins/ZyrenAuth/config.json`:**
    -   Set `"emailFeaturesEnabled": true`
    -   Update the following fields with your SMTP details and Gmail App Password:
        ```json
        "smtpHost": "smtp.gmail.com",
        "smtpPort": "587", // Use 465 for SSL, 587 for TLS (Gmail uses 587/TLS)
        "smtpUsername": "yourgmail@gmail.com",
        "smtpPassword": "your_16_char_app_password", // The generated App Password
        "smtpAuth": true,
        "smtpStarttlsEnable": true,
        "emailSenderAddress": "yourgmail@gmail.com", // Or a no-reply email
        ```

### **5. Final Server Start**

1.  Save all changes to `config.json`.
2.  **Start your server**.
3.  Check the console for `[ZyrenAuth] Connected to MySQL database: zyrenauth` and `[ZyrenAuth] EmailSender session initialized...` messages to confirm successful setup.

---

## **Support**

For any issues or questions, please open an issue on the [GitHub repository](https://github.com/_Pheonix/ZyrenAuth/issues).

---

## **License**

This project is licensed under the [MIT License](LICENSE).

---
