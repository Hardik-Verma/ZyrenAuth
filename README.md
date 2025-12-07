# **ZyrenAuth**

A **secure and flexible authentication plugin** for **Paper Minecraft servers**, crafted by **_Pheonix** (Website and app development). ZyrenAuth brings robust account protection, player privacy, and an intuitive login experience to your Minecraft servers.

---

## **Key Features**

### ✅ **Intelligent Authentication Flow**

-   **Premium Servers (`online-mode=true`):**
    -   **Registered Players:** Automatically logged in.
    -   **Unregistered Players:** Automatically registered and logged in. No manual `/register` required.
    -   **Automatic Username Migration:** Player IGNs are automatically updated in storage if they change (e.g., case changes).
-   **Cracked Servers (`online-mode=false`):**
    -   **Registered Players:** Automatically logged in.
    -   **Unregistered Players:** *Still require manual `/register`*. This is crucial for security in offline mode, as UUIDs are easily spoofable.
-   **Username Validation:** Prevents registering usernames that only differ by case.

### ✅ **Secure Core Authentication**

-   **BCrypt Hashing:** All player passwords are securely hashed using BCrypt.
-   **Configurable Password Policy:** Easily adjust password strength (default: minimum 3 characters).
-   **Optional Anti-Account Sharing:** Prevents multiple logins from the same account.
-   **Optional IP/Device Locking:** Restricts account access to trusted IPs/devices.

### ✅ **Privacy-Focused Login Experience**

-   **Safe Login Zone:** Players teleport to a designated authentication area (0, 0, 0) on join, hiding their real coordinates until they authenticate.
-   **Player Restrictions:** Unauthenticated players are frozen – unable to move, interact, or chat.
-   **Location Restoration:** After successful authentication, players are seamlessly returned to their last known location (or world spawn if new).

### ✅ **Flexible Data Storage**

-   **MySQL/MariaDB Support (Recommended):**
    -   Integrates with a database for **persistent storage** of player accounts, security logs, email tokens, and IP restrictions.
    -   Enables all advanced security features.
-   **File-Based Storage (Fallback):**
    -   If MySQL is disabled or unavailable, ZyrenAuth automatically defaults to storing player accounts in `plugins/ZyrenAuth/accounts.json`.
    -   Core `/register` and `/login` functionality remains, while email-based features and advanced security are gracefully disabled.

### ✅ **Optional Email Integration**

> **Note:** Requires MySQL/MariaDB to be enabled and configured correctly, along with SMTP settings.

-   **Account Linking:** Players can link their email addresses for recovery.
-   **Email Confirmation:** Verify email addresses via `/emailconfirm <token>`.
-   **Password Reset Functionality:** Players can initiate a password reset via `/resetpassword`, receiving a secure token to their linked email, which they then use with `/resetconfirm`.

### ✅ **Intuitive In-Game User Experience**

-   **Elegant Messaging:** All in-game prompts and responses are styled with clear text and colors.
-   **Clear Feedback:** Concise and helpful messages for actions or errors.
-   **Comprehensive Help:** The `/za help` command provides an easy-to-understand list of all player and admin commands.
-   **Admin Tools:** `/za status` offers a quick overview of the plugin's operational status.

---

## **Commands**

### **Player Commands**

-   **`/register <password> <confirm>`**: Create your secure ZyrenAuth account.
-   **`/login <password>`**: Log in to your existing ZyrenAuth account.
-   **`/addemail <email>`**: Link an email address to your account (requires MySQL & SMTP).
-   **`/emailconfirm <token>`**: Confirm your linked email address using the token sent to your inbox.
-   **`/resetpassword`**: Request a password reset token (requires MySQL & SMTP).
-   **`/resetconfirm <token> <new_pass> <confirm>`**: Complete your password reset.

### **Admin Commands** (Requires `zyrenauth.admin` permission - default: OP)

-   **`/za help`**: Displays a list of all ZyrenAuth commands.
-   **`/za status`**: Shows plugin status, storage, and features.
-   **`/za reload`**: Reloads config (restart recommended for full effect).

---

## **Installation & Setup**

### **Requirements**

-   **Java 21** or later
-   **Paper 1.21.1** or any compatible Paper/Spigot/Purpur server version
-   **MySQL 8.0+** or **MariaDB 10.4+** (Recommended for persistent data & full features)
-   An **SMTP Account** (e.g., Gmail App Password) for email features (optional).

### **Quick Start**

1.  **Download** `ZyrenAuth-1.0.1.jar` from the [releases page](https://github.com/Hardik-Verma/ZyrenAuth/releases).
2.  Place the `ZyrenAuth-1.0.1.jar` file into your server's `plugins/` folder.
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

1.  **Install MySQL/MariaDB Server:** If you don't have one, install MySQL Community Server or MariaDB.
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
    ```json
    "mysqlEnabled": true, // Set to true to enable MySQL storage
    "mysqlHost": "127.0.0.1",   // Your MySQL server IP
    "mysqlPort": "3306",      // Your MySQL server port
    "mysqlDatabase": "zyrenauth",
    "mysqlUser": "zyrenauth",
    "mysqlPassword": "YourStrongPasswordHere!",
    ```

### **SMTP (Email) Configuration (Optional)**

> Required if `emailFeaturesEnabled` is true in `config.json`. Gmail is a common choice.

1.  **Generate a Gmail App Password (Recommended):** If using Gmail, enable 2-Step Verification on your Google Account, then create a 16-character App Password (under Security -> App passwords). This is more secure than using your regular Gmail password.
2.  **Edit `config.json`:**
    ```json
    "emailFeaturesEnabled": true, // Set to true to enable email features
    "smtpHost": "smtp.gmail.com", // Or your SMTP server
    "smtpPort": "587", // Common ports: 465 (SSL), 587 (TLS)
    "smtpUsername": "yourgmail@gmail.com", // Your email for sending
    "smtpPassword": "your_16_char_app_password", // Your App Password or SMTP password
    "smtpAuth": true,
    "smtpStarttlsEnable": true,
    "emailSenderAddress": "yourgmail@gmail.com", // The 'From' address in emails
    ```

### **Other Settings**

-   **`autoLoginPremiumPlayers`**: Default `true`. Controls auto-registration/login for premium players on `online-mode=true` servers, and auto-login for registered players on `online-mode=false` servers.
-   `minPasswordLength`: Minimum characters for passwords (default: 3).
-   `bcryptStrength`: BCrypt work factor.
-   `maxLoginAttempts` / `lockoutDurationSeconds`**: Brute-force protection settings.
-   `antiAccountSharingEnabled` / `ipDeviceLockingEnabled`**: Toggle advanced security.

---

## **Support**

For any issues or questions, please open an issue on the [GitHub repository](https://github.com/Hardik-Verma/ZyrenAuth/issues).

---

## **License**

This project is licensed under the [MIT License](LICENSE).
