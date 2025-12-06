package com.pheonix.zyrenauth.command;

import com.pheonix.zyrenauth.ZyrenAuthPlugin;
import com.pheonix.zyrenauth.manager.AuthManager;
import com.pheonix.zyrenauth.manager.DatabaseManager;
import com.pheonix.zyrenauth.util.ZyrenAuthConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZyrenAuthAdminCommand implements CommandExecutor, TabCompleter {

    private final ZyrenAuthPlugin plugin;
    private final AuthManager authManager;

    public ZyrenAuthAdminCommand(ZyrenAuthPlugin plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /za help – for everyone
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // /za status and /za reload require admin
        if ((sub.equals("status") || sub.equals("reload")) && !sender.hasPermission("zyrenauth.admin")) {
            sender.sendMessage(ChatColor.RED + "§l✖ " + ChatColor.DARK_RED + "You do not have permission to use that subcommand.");
            return true;
        }

        switch (sub) {
            case "status":
                handleStatus(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "╔═══════════════════════════════╗");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.AQUA + ChatColor.BOLD + "ZyrenAuth Commands" + ChatColor.DARK_AQUA + "              ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "╠═══════════════════════════════╣");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.GRAY + "Player Commands:" + ChatColor.DARK_AQUA + "               ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.YELLOW + "/register <password> <confirm>" + ChatColor.DARK_GRAY + " - Create account" + ChatColor.DARK_AQUA + " ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.YELLOW + "/login <password>" + ChatColor.DARK_GRAY + " - Log in to account" + ChatColor.DARK_AQUA + "   ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.YELLOW + "/addemail <email>" + ChatColor.DARK_GRAY + " - Link email (DB only)" + ChatColor.DARK_AQUA + "  ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.YELLOW + "/emailconfirm <token>" + ChatColor.DARK_GRAY + " - Confirm email" + ChatColor.DARK_AQUA + "  ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.YELLOW + "/resetpassword" + ChatColor.DARK_GRAY + " - Request password reset" + ChatColor.DARK_AQUA + " ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.YELLOW + "/resetconfirm <token> <new_pass> <confirm>" + ChatColor.DARK_GRAY + " - Reset pass" + ChatColor.DARK_AQUA + " ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "╠═══════════════════════════════╣");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.GRAY + "Admin Commands:" + ChatColor.DARK_AQUA + "                ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.YELLOW + "/za status" + ChatColor.DARK_GRAY + " - View plugin status " + ChatColor.RED + "(op)" + ChatColor.DARK_AQUA + "  ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "║ " + ChatColor.YELLOW + "/za reload" + ChatColor.DARK_GRAY + " - Reload config " + ChatColor.RED + "(op)" + ChatColor.DARK_AQUA + "     ║");
        sender.sendMessage(ChatColor.DARK_AQUA + "╚═══════════════════════════════╝");
    }

    private void handleStatus(CommandSender sender) {
        DatabaseManager db = plugin.getDatabaseManager();
        ZyrenAuthConfig cfg = plugin.getZyrenConfig();

        boolean dbEnabled = cfg.isMysqlEnabled();
        boolean dbConnected = db != null && db.isConnected();
        boolean emailEnabled = cfg.isEmailFeaturesEnabled();

        // Decide storage label
        String storage;
        if (dbEnabled && dbConnected) {
            storage = ChatColor.GREEN + "MySQL";
        } else if (dbEnabled) {
            storage = ChatColor.YELLOW + "MySQL (configured, not connected)";
        } else {
            storage = ChatColor.AQUA + "File (accounts.json)";
        }

        sender.sendMessage(ChatColor.AQUA + "╔═══════════════════════════════╗");
        sender.sendMessage(ChatColor.AQUA + "║ " + ChatColor.GOLD + ChatColor.BOLD + "ZyrenAuth Status" + ChatColor.DARK_AQUA + "              ║");
        sender.sendMessage(ChatColor.AQUA + "╠═══════════════════════════════╣");
        sender.sendMessage(ChatColor.AQUA + "║ " + ChatColor.GRAY + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion() + ChatColor.DARK_AQUA + "         ║");
        sender.sendMessage(ChatColor.AQUA + "║ " + ChatColor.GRAY + "Storage: " + storage + ChatColor.DARK_AQUA + " ║");
        sender.sendMessage(ChatColor.AQUA + "║ " + ChatColor.GRAY + "MySQL Enabled: " + ChatColor.WHITE + dbEnabled + ChatColor.DARK_AQUA + "         ║");
        sender.sendMessage(ChatColor.AQUA + "║ " + ChatColor.GRAY + "MySQL Connected: " + ChatColor.WHITE + dbConnected + ChatColor.DARK_AQUA + "       ║");
        sender.sendMessage(ChatColor.AQUA + "║ " + ChatColor.GRAY + "Email Features: " + ChatColor.WHITE + emailEnabled + ChatColor.DARK_AQUA + "        ║");
        sender.sendMessage(ChatColor.AQUA + "╚═══════════════════════════════╝");
    }

    private void handleReload(CommandSender sender) {
        // Implement a proper reload method in ZyrenAuthPlugin if needed,
        // which would re-initialize managers. For now, inform the admin.
        sender.sendMessage(ChatColor.YELLOW + "§l⚠ " + ChatColor.GOLD +
                "ZyrenAuth config is file-based. For now, please restart the server to fully apply changes.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String partial = args[0].toLowerCase();
            if ("help".startsWith(partial)) completions.add("help");
            if (sender.hasPermission("zyrenauth.admin")) {
                if ("status".startsWith(partial)) completions.add("status");
                if ("reload".startsWith(partial)) completions.add("reload");
            }
            return completions;
        }
        return null;
    }
}
