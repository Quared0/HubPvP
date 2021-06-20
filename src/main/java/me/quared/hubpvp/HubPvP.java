package me.quared.hubpvp;

import me.quared.hubpvp.commands.HubPvPCommand;
import me.quared.hubpvp.listeners.Listeners;
import org.bukkit.plugin.java.JavaPlugin;

public final class HubPvP extends JavaPlugin {
    
    private static String version;
    private static HubPvP plugin;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        
        getServer().getConsoleSender().sendMessage(format("&a" + getDescription().getName() + " v" + getDescription().getVersion() + " enabled."));
        
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        
        getCommand("hubpvp").setExecutor(new HubPvPCommand());
        getCommand("hubpvp").setTabCompleter(new HubPvPCommand());
        
        saveDefaultConfig();
    }
    
    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(format("&" + getDescription().getName() + " v" + getDescription().getVersion() + " disabled."));
    }
    
    public static HubPvP getPlugin() {
        return plugin;
    }
    
    public String format(String text) {
        return text.replaceAll("&", "§");
    }
    
}
