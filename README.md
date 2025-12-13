# **ZyrenAuth**

A **secure and smart authentication plugin/mod** for **Minecraft 1.21.1+ servers**, made by **_Pheonix** (Website and app development). ZyrenAuth brings robust account protection, player privacy, and an intuitive login experience to both **Paper** and **Fabric** server environments.

---

## **Features**

*   **Smart Player Authentication:**
    * Supports both offline and online servers with a smart authentication system.
*   **Secure Logins:** Uses BCrypt for password hashing and validates usernames.
*   **Private Join:** Teleports players to a safe login area in world spawn, hiding their real location until they authenticate.
*   **Flexible Storage:** Supports **MySQL/MariaDB** for persistent data, or a simple **file-based fallback** (`accounts.json`).
*   **Optional Email:** Features like email confirmation and password reset (requires MySQL + SMTP).
*   **Admin Tools:** `/za` command for status, help, and config reload.

---

## **Supported**

-   **Paper:** 1.21.1+ (Plugins folder)
-   **Fabric:** 1.21.1+ (Mods folder, requires Fabric API)

---

## **Quick Start**

1.  **Download** `ZyrenAuth-1.0.1.jar` (or `ZyrenAuth-Fabric-1.0.1.jar`).
2.  Place in your server's `plugins/` (Paper) or `mods/` (Fabric, + Fabric API).
3.  Start/stop server once to generate `config.json`.
4.  Edit `config.json` for MySQL/SMTP (optional, but recommended for full features).
5.  Restart server.

---

## **Configuration**

Edit `config.json` (Paper: `plugins/ZyrenAuth/`; Fabric: `config/zyrenauth/`).

-   **`mysqlEnabled`**: Set `true` for MySQL.
-   **`emailFeaturesEnabled`**: Set `true` for email (requires MySQL + SMTP).
-   Fill in your **MySQL credentials** (host, port, user, pass).
-   Fill in your **SMTP email details** (host, port, username, **App Password** for Gmail).

---

## **Commands**

-   **`/register <pass> <confirm>`**: Create your account.
-   **`/login <pass>`**: Log in.
-   **`/addemail <email>`**: Link email (MySQL & SMTP only).
-   **`/emailconfirm <token>`**: Confirm email.
-   **`/resetpassword`**: Request password reset (MySQL & SMTP only).
-   **`/resetconfirm <token> <new_pass> <confirm>`**: Complete reset.
-   **`/za help`**: Lists all commands.
-   **`/za status`**: Plugin status (op only).
-   **`/za reload`**: Reload config (op only, restart recommended).

---

## **Support & License**

-   **Issues:** [GitHub Repository](https://github.com/Hardik-Verma/ZyrenAuth/issues)
-   **License:** [MIT License](LICENSE)
