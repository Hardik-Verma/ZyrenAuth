package com.pheonix.zyrenauth.listener;

import com.pheonix.zyrenauth.manager.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class PlayerRestrictionListener implements Listener {

    private final AuthManager authManager;

    public PlayerRestrictionListener(AuthManager authManager) {
        this.authManager = authManager;
    }

    // --- Join / Quit -> map Fabric join/leave handling ---

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        authManager.handlePlayerJoin(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        authManager.handlePlayerLeave(player);
    }

    // --- Movement restriction ---

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
                sendAuthReminder(player);
                // Teleport them back to the fixed auth location (0,0,0)
                Location authLocation = new Location(player.getWorld(), 0.5, 64, 0.5);
                event.setTo(authLocation);
            }
        }
    }

    // --- Block break / place ---

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            sendAuthReminder(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            sendAuthReminder(player);
            event.setCancelled(true);
        }
    }

    // --- Interactions ---

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            sendAuthReminder(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (isFrozen(player)) {
                sendAuthReminder(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (isFrozen(player)) {
                sendAuthReminder(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (isFrozen(player)) {
                sendAuthReminder(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (isFrozen(player)) {
                sendAuthReminder(player);
                event.setCancelled(true);
            }
        }
    }

    // --- Item use (drop, consume, etc.) ---

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            sendAuthReminder(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            sendAuthReminder(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            sendAuthReminder(player);
            event.setCancelled(true);
        }
    }

    // --- Chat restriction (like ServerMessageEvents.CHAT_MESSAGE) ---

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            sendAuthReminder(player);
            event.setCancelled(true);
        }
    }

    // --- Helpers ---

    private boolean isFrozen(Player player) {
        UUID uuid = player.getUniqueId();
        return authManager.isPlayerFrozen(uuid);
    }

    private void sendAuthReminder(Player player) {
        if (isFrozen(player)) {
            player.sendMessage(ChatColor.DARK_AQUA + "§l⚠ " + ChatColor.AQUA + "Please log in or register to interact with the world.");
            player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/login <password>" + ChatColor.GRAY + " or " + ChatColor.YELLOW + "/register <password> <confirm_password>");
        }
    }
}
