package com.kai295.enderguard;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {

    private final EnderGuard plugin;

    public CommandManager(EnderGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("enderguard.reload")) {
                plugin.reloadConfig();
                plugin.getInventoryListener().loadConfig();
                sender.sendMessage(ChatColor.GREEN + "EnderGuard's configuration has been successfully reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            }
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " reload");
        return true;
    }
}