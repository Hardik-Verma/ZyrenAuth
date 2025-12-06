// src/main/java/com/pheonix/zyrenauth/command/RegisterCommand.java
package com.pheonix.zyrenauth.command;

import com.pheonix.zyrenauth.manager.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {

    private final AuthManager authManager;

    public RegisterCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /register <password> <confirm_password>");
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        if (!password.equals(confirmPassword)) {
            player.sendMessage(ChatColor.RED + "Passwords do not match!");
            return true;
        }

        authManager.registerPlayer(player, password);
        return true;
    }
}
