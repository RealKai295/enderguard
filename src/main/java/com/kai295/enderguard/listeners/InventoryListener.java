package com.kai295.enderguard.listeners;

import com.kai295.enderguard.EnderGuard;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryListener implements Listener {

    private final EnderGuard plugin;
    private List<Material> blockedMaterials;
    private String blockedMessage;

    public InventoryListener(EnderGuard plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        // Load blocked materials from config
        this.blockedMaterials = plugin.getConfig().getStringList("blocked-items").stream()
                .map(Material::matchMaterial)
                .collect(Collectors.toList());

        // Load blocked message
        this.blockedMessage = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("blocked-item-message", "&cYou cannot store this item in your Ender Chest."));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getClick() == ClickType.NUMBER_KEY) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.ENDER_CHEST) {
                PlayerInventory playerInventory = player.getInventory();
                ItemStack hotbarItem = playerInventory.getItem(event.getHotbarButton());

                if (isBlocked(hotbarItem)) {
                    event.setCancelled(true);
                    player.sendMessage(blockedMessage);
                    return;
                }
            }
        }

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.ENDER_CHEST) {
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (isBlocked(offHandItem)) {
                    event.setCancelled(true);
                    player.sendMessage(blockedMessage);
                    return; 
                }
            }
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.ENDER_CHEST) {
            if (isBlocked(cursorItem)) {
                event.setCancelled(true);
                player.sendMessage(blockedMessage);
                player.updateInventory();
                return;
            }
        }

        if (event.isShiftClick() && event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.ENDER_CHEST) {
            if (isBlocked(currentItem)) {
                event.setCancelled(true);
                player.sendMessage(blockedMessage);
                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) {
            return;
        }

        boolean draggingIntoEnderChest = event.getRawSlots().stream().anyMatch(slot -> slot < event.getInventory().getSize());

        if (draggingIntoEnderChest) {
            if (isBlocked(event.getOldCursor())) {
                event.setCancelled(true);
                ((Player) event.getWhoClicked()).sendMessage(blockedMessage);
            }
        }
    }

    private boolean isBlocked(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        if (blockedMaterials.contains(item.getType())) {
            return true;
        }

        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            if (meta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulker = (ShulkerBox) meta.getBlockState();
                for (ItemStack shulkerItem : shulker.getInventory().getContents()) {
                    if (isBlocked(shulkerItem)) { // Recursive call
                        return true;
                    }
                }
            }
        }

        if (item.getItemMeta() instanceof BundleMeta) {
            BundleMeta meta = (BundleMeta) item.getItemMeta();
            for (ItemStack bundleItem : meta.getItems()) {
                if (isBlocked(bundleItem)) { // Recursive call
                    return true;
                }
            }
        }

        return false;
    }

    public List<Material> getBlockedMaterials() {
        return blockedMaterials;
    }
}