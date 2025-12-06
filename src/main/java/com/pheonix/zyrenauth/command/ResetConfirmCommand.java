package com.pheonix.zyrenauth.command;

import com.pheonix.zyrenauth.manager.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetConfirmCommand implements CommandExecutor {

    private final AuthManager authManager;

    public ResetConfirmCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 3) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /resetconfirm <token> <new_password> <confirm_new_password>");
            return true;
        }

        String token = args[0];
        String newPassword = args[1];
        String confirmNewPassword = args[2];

        if (!newPassword.equals(confirmNewPassword)) {
            player.sendMessage(ChatColor.RED + "New passwords do not match!");
            return true;
        }

        authManager.confirmPasswordReset(player, token, newPassword);
        return true;
    }
}
