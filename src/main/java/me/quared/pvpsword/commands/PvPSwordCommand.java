package me.quared.pvpsword.commands;

import me.quared.pvpsword.PvPSword;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.io.Console;
import java.util.Collections;
import java.util.List;

public class PvPSwordCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
    
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    PvPSword plugin = PvPSword.getPlugin();
                    plugin.reloadConfig();
                    p.sendMessage(plugin.format(plugin.getConfig().getString("reload-message")));
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid arguments. Use: /" + label + " <reload>");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Invalid usage. Use: /" + label + " <args>");
            }
        } else if (sender instanceof ConsoleCommandSender) {
            ConsoleCommandSender p = ((ConsoleCommandSender) sender);
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    PvPSword plugin = PvPSword.getPlugin();
                    plugin.reloadConfig();
                    p.sendMessage(plugin.format(plugin.getConfig().getString("reload-message")));
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid arguments. Use: /" + label + " <reload>");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Invalid usage. Use: /" + label + " <args>");
            }
        }
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length <= 1) {
            return Collections.singletonList("reload");
        }
        return null;
    }
}
