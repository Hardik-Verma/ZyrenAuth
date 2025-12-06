// src/main/java/com/pheonix/zyrenauth/command/AddEmailCommand.java
package com.pheonix.zyrenauth.command;

import com.pheonix.zyrenauth.manager.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddEmailCommand implements CommandExecutor {

    private final AuthManager authManager;

    public AddEmailCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length < 1) { // Changed to < 1 as email can contain spaces (greedyString)
            player.sendMessage(ChatColor.YELLOW + "Usage: /addemail <email>");
            return true;
        }

        String email = String.join(" ", args); // Reconstruct email for greedyString
        authManager.handleEmailAddition(player, email);
        return true;
    }
}
