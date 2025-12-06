// src/main/java/com/pheonix/zyrenauth/command/EmailConfirmCommand.java
package com.pheonix.zyrenauth.command;

import com.pheonix.zyrenauth.manager.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EmailConfirmCommand implements CommandExecutor {

    private final AuthManager authManager;

    public EmailConfirmCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /emailconfirm <token>");
            return true;
        }

        String token = args[0];
        authManager.confirmEmail(player, token);
        return true;
    }
}
