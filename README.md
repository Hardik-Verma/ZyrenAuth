# ZyrenAuth – Secure Minecraft Authentication & Login Security Plugin

**ZyrenAuth** is a modern, robust, and privacy-focused authentication plugin/mod for Minecraft 1.21.1+ servers. Developed by _Pheonix, it provides strong account protection, seamless login experiences, and advanced security features for both Paper and Fabric server environments.

🔗 **Visit our Official Website for more details & an interactive Config Generator:** [https://zyrenauth.wuaze.com/](https://zyrenauth.wuaze.com/)
🔗 **Find updates & versions on Modrinth:** [https://modrinth.com/plugin/zyrenauth/versions](https://modrinth.com/plugin/zyrenauth/versions)

---

## ✨ Key Features

*   **Mandatory Captcha System on Join:**
    *   Players are now required to solve a dynamic captcha (either a math problem or an interactive item-click challenge) immediately upon joining the server.
    *   Features configurable expiry time and a maximum of 3 attempts before the player is kicked from the server.
    *   Players are safely teleported to a dedicated void-like zone (`0.5, 5.0, 0.5`) during the captcha phase to prevent interaction with the game world.
*   **Dedicated 2FA Setup GUI:**
    *   The `/za 2fa setup` command now opens an intuitive in-game GUI that clearly displays your 2FA secret key and QR code URI, making setup with popular authenticator apps much easier.
*   **Distinct Authentication Teleportation:**
    *   After successfully solving the captcha, players are seamlessly teleported to a separate, safe void (`0.5, 0.0, 0.5`) to proceed with the `/login` or `/register` prompts. This ensures a focused authentication process.
*   **Comprehensive Player State Management:**
    *   Player's original location, inventory, gamemode, health, food, experience, and even potion effects are now reliably saved upon entering the authentication flow and fully restored upon successful login/registration.
*   **Asynchronous Modrinth Update Checker:**
    *   The plugin now automatically checks Modrinth for the latest release version upon startup and notifies server owners in the console if an update is available.
*   **Dynamic On-Screen Authentication Prompts:** Players receive clear, persistent on-screen titles and subtitles for `/register` and `/login` commands, guiding them through the authentication process seamlessly until successfully logged in.
*   **Optional Two-Factor Authentication (2FA) via Google Authenticator:** Players can enable 2FA for an extra layer of security; compulsory for that player's future logins once enabled. Compatible with popular authenticator apps.
*   **Smart Player Authentication:**
    *   **For Online-Mode Servers (`online-mode=true`):** Automatically registers and logs in legitimate Minecraft accounts. No manual `/register` is required, and usernames are automatically updated.
    *   **For Servers in Alternative Authentication Modes (`online-mode=false`):** Registered players are automatically logged in. Unregistered players must register to ensure account security.
*   **Secure Logins:** Utilizes strong BCrypt password hashing and robust username validation.
*   **Flexible Storage:** Full support for **MySQL/MariaDB** for persistent, scalable data, with improved internal handling for encrypted file-based accounts as a robust fallback.
*   **Optional Email Features:** Supports email confirmation and password reset functionalities (requires MySQL and SMTP configuration).
*   **Improved Admin & Player Commands:** Unified `/za` admin command (alias for `/zyrenauthadmin`), revamped `/za help` and `/za status` with clearer, aesthetic chat-box layouts, and integrated 2FA subcommands.
*   **Streamlined File Management:** All plugin files (`config.json`, `accounts.json`, `secret.key`, `data/`, `logs/`) are now organized under `server_root/plugins/config/zyrenauth/` for cleaner server directories.

---

## 🐛 Bug Fixes & Improvements (v1.0.3)

*   **Fixed `/captcha <answer>` Command:** Correctly processes math captcha answers and proceeds to the login/register stage instead of opening the help menu.
*   **Eliminated Chat Spam:** Removed repetitive "You cannot take damage while in the authentication zone" messages during the frozen state.
*   **Prevented Pre-Auth Death & Bypass:** Players are now invulnerable to all damage while in the authentication flow, and respawning will always return them to the correct authentication stage.
*   **Improved Console Messaging:** Enhanced clarity and consistency of console logs.
*   **Code Stability:** Resolved numerous compilation errors related to duplicate method definitions, missing imports, and incorrect method calls across various classes, significantly improving plugin robustness.

---

## 🚀 Supported Platforms

*   **Paper:** 1.21.1+ (Place in `plugins/` folder)
*   **Fabric:** 1.21.1+ (Place in `mods/` folder, requires Fabric API)

---

## ⚡ Quick Start

1.  **Download** ZyrenAuth from [Modrinth](https://modrinth.com/plugin/zyrenauth/versions).
2.  Place the downloaded `.jar` file in your server’s `plugins/` (for Paper) or `mods/` (for Fabric) folder.
3.  Start and stop your server once. This will generate the `config.json` file at `server_root/plugins/config/zyrenauth/config.json`.
4.  **Configure `config.json`:**
    *   Edit the `config.json` file manually, or for an easier experience, use our [**Online Config Generator**](https://zyrenauth.wuaze.com/) to build your configuration.
    *   Set up MySQL/MariaDB and SMTP email options if desired (highly recommended for full features and security).
5.  Restart your server and enjoy secure authentication!

---

## ⚙️ Key Configuration Options (`config.json`)

**Location:** `server_root/plugins/config/zyrenauth/config.json`

ZyrenAuth's `config.json` is highly customizable. Here are some of the key options:

*   `requirePasswordConfirmation`, `freezeUnverifiedPlayers`, `autoLoginPremiumPlayers`: (boolean) General player experience settings.
*   `captchaEnabled`, `captchaType`, `captchaMathMin`, `captchaMathMax`, `captchaItemClickAmount`, `captchaExpiryMinutes`, `maxCaptchaAttempts`: (boolean, String, int) Captcha system settings.
*   `minPasswordLength`, `requireDigit`, `requireLowercase`, `requireUppercase`, `requireSpecialChar`: (int, boolean) Customizable password policy.
*   `bcryptStrength`: (int) Work factor for password hashing complexity (higher is more secure but slower).
*   `spawnWorld`, `spawnYaw`, `spawnPitch`: (String, float) World and orientation for authentication spawn points. (Note: X, Y, Z coordinates are now dynamically set for captcha and login/register stages).
*   `mysqlEnabled`, `mysqlHost`, `mysqlPort`, `mysqlDatabase`, `mysqlUser`, `mysqlPassword`: (boolean, String) MySQL/MariaDB database connection.
*   `emailFeaturesEnabled`, `smtpHost`, `smtpPort`, `smtpUsername`, `smtpPassword`, `smtpAuth`, `smtpStarttlsEnable`, `emailSenderAddress`: (boolean, String) Email feature and SMTP server details.
*   `emailConfirmationExpiryMinutes`, `passwordResetExpiryMinutes`: (int) Token expiry durations.
*   `webServerUrl`: (String) Base URL for links in email verification/password reset emails.
*   `maxLoginAttempts`, `lockoutDurationSeconds`: (int, long) Failed login lockout settings (MySQL only).
*   `antiAccountSharingEnabled`, `ipDeviceLockingEnabled`: (boolean) Advanced security features (MySQL only).
*   `totpIssuer`: (String) Issuer name displayed in authenticator apps for 2FA.
*   `msgCaptchaKick`, `msgCaptchaExpiredKick`: (String) Customizable messages for captcha failure and expiry kicks.

*For a full list of options and easy setup, use our [Online Config Generator](https://zyrenauth.wuaze.com/)!*

---

## 📜 Commands

*   `/register <password> <confirm>`: Create your account.
*   `/login <password>`: Log in to your account.
*   `/captcha <answer>`: Solve the current captcha challenge.
*   `/addemail <email>`: Link an email to your account (Requires MySQL & SMTP).
*   `/emailconfirm <token>`: Confirm your email.
*   `/resetpassword`: Initiate password reset (Requires MySQL & SMTP).
*   `/resetconfirm <token> <new_password> <confirm>`: Complete password reset.
*   `/za help`: Lists all commands.
*   `/za status`: Check plugin status (OP only).
*   `/za reload`: Reload plugin configuration (OP only, server restart recommended for major changes).
*   `/za migrate <player>`: Migrate old plaintext player data to encrypted format (OP only).
*   `/za delete <player> [confirm]`: Delete a player's account (OP only).
*   `/za 2fa setup`: Set up Two-Factor Authentication (opens GUI).
*   `/za 2fa confirm <code>`: Confirm 2FA setup or login.
*   `/za 2fa disable <code>`: Disable 2FA.
*   `/za 2fa status`: Check 2FA status.

---

## 🤝 Support & License

*   **Issues/Source Code:** [https://github.com/Hardik-Verma/ZyrenAuth](https://github.com/Hardik-Verma/ZyrenAuth)
*   **License:** [MIT License](LICENSE)
