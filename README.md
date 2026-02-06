# ZyrenAuth – Secure Minecraft Authentication & Login Security Plugin

**ZyrenAuth** is a modern, robust, and privacy-focused authentication plugin for Paper (Spigot-compatible) Minecraft 1.21.1+ servers. Developed by _Pheonix, ZyrenAuth provides strong account protection, seamless login experiences, two-factor authentication, secure data storage, and advanced anti-bot features.

🔗 **Official Website & Config Generator**: [https://zyrenauth.wuaze.com/](https://zyrenauth.wuaze.com/)  
🔗 **Modrinth Releases**: [https://modrinth.com/plugin/zyrenauth/versions](https://modrinth.com/plugin/zyrenauth/versions)

---

## ✨ Key Features

- **Mandatory Reverse Word Captcha on Join**
    - Every player must solve a reverse word captcha before logging in or registering.
    - Configurable expiry time & attempts; player is safely teleported to a void authentication zone during captcha.
- **In-Game 2FA Setup (Google Authenticator)**
    - `/za 2fa setup` opens an intuitive GUI with your 2FA secret key and QR code.
    - Optional for players—once enabled, required for login.
- **Accurate State Restoration**
    - Player's location, inventory, gamemode, health, food, experience, and potion effects are stored before authentication and fully restored after login/registration.
    - New registrations are always teleported to your world’s spawn in the correct gamemode.
- **Seamless Creative/Spectator Support**
    - New players default to the world’s gamemode, not forced to survival.
    - Health and hunger only set for survival/adventure.
- **Persistent On-Screen Prompts**
    - Players see clear, persistent titles and subtitles guiding them through login/registration until authentication completes.
- **Advanced Admin & Player Commands**
    - `/za` is the main admin and utility command, offering status, reload, migrate, delete, and 2FA subcommands.
- **MySQL/MariaDB & Secure File Storage**
    - Supports MySQL/MariaDB for persistent data on large servers.
    - Encrypted file-based accounts for small or backup servers.
- **Optional Email Confirmation & Reset**
    - (Requires MySQL+SMTP configuration) for email confirmation and password reset.
- **Automatic Modrinth Update Checker**
    - Notifies you in console when a new version is available.

---

## 🐞 Bug Fixes & Improvements (v1.0.3+)
- `/captcha <answer>` now correctly accepts and checks only reverse word captchas.
- Eliminated all dashboard/web/admin-token legacy code for less bloat and better security.
- Only one unfreeze per login or registration — no messy state.
- Persistent action titles (Registered, Logged In) now clear cleanly after success.
- Handles all gamemodes and edge cases. Surv/adventure get health/hunger, others don't.
- Authentication, registration, and account deletion flows are now admin-safe and robust.
- Server console logs and error handling improved.

---

## 🚀 Supported Platforms

- **Paper** (and compatible forks) 1.21.1+
  - Place ZyrenAuth `.jar` in `plugins/` folder.

---

## ⚡ Installation & Quick Start

1. Download ZyrenAuth from [Modrinth](https://modrinth.com/plugin/zyrenauth/versions).
2. Place the `.jar` in your `plugins/` folder.
3. Start and stop your server once to generate `config.json` at `plugins/config/zyrenauth/config.json`.
4. **Edit `config.json`** by hand or use the [Online Config Generator](https://zyrenauth.wuaze.com/).
    - Set up MySQL/MariaDB and SMTP email if you want email/password reset features.
5. Restart your server and enjoy secure authentication.

---

## ⚙️ Key Configuration Options

Location: `plugins/config/zyrenauth/config.json`

- `captchaEnabled`, `captchaExpiryMinutes`, `maxCaptchaAttempts`: Controls reverse word captcha.
- `minPasswordLength`, `requireDigit`, `requireLowercase`, `requireUppercase`, `requireSpecialChar`: Password policy.
- `mysqlEnabled`, `mysqlHost`, `mysqlDatabase`, ...: MySQL/MariaDB connection.
- `emailFeaturesEnabled`, ...: SMTP configuration.
- For more, see the in-file comments or [our docs](https://zyrenauth.wuaze.com/).

---

## 📜 Commands

- `/register <password> <confirm>`: Register an account.
- `/login <password>`: Log in to your account.
- `/captcha <answer>`: Solve your current reverse word captcha.
- `/za 2fa setup|confirm <code>|disable <code>|status`: Manage 2FA.
- `/addemail <email>` & `/emailconfirm <token>`: Email linking (MySQL only).
- `/resetpassword`, `/resetconfirm <token> <new> <confirm>`: Password reset.
- `/za status`, `/za reload`, `/za migrate <player>`, `/za delete <player> [confirm]`: Admin/OP commands.

Full command details and permissions are in [plugin.yml](plugin.yml).

---

## 🤝 Support & License

- **Issues & Source:** [https://github.com/Hardik-Verma/ZyrenAuth](https://github.com/Hardik-Verma/ZyrenAuth)
- **License:** [MIT License](LICENSE)
- **Help/Chat:** [Discord Support](https://discord.gg/hlai)

---

_Pheonix – https://pheonix.is-a.dev

