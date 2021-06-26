package me.quared.hubpvp;

import me.quared.hubpvp.commands.HubPvPCommand;
import me.quared.hubpvp.listeners.Listeners;
import org.bukkit.plugin.java.JavaPlugin;

public final class HubPvP extends JavaPlugin {
    
    private static HubPvP plugin;
    private Listeners listeners;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        
        getServer().getConsoleSender().sendMessage(format("&a" + getDescription().getName() + " v" + getDescription().getVersion() + " enabled."));
        
        listeners = new Listeners();
        getServer().getPluginManager().registerEvents(listeners, this);
        
        getCommand("hubpvp").setExecutor(new HubPvPCommand());
        getCommand("hubpvp").setTabCompleter(new HubPvPCommand());
        
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
    }
    
    @Override
    public void onDisable() {
        listeners.pvpTask.clear();
        listeners.pvpTask2.clear();
        listeners.pvp.clear();
        listeners.flying.clear();
        getServer().getConsoleSender().sendMessage(format("&c" + getDescription().getName() + " v" + getDescription().getVersion() + " disabled."));
    }
    
    public static HubPvP getPlugin() {
        return plugin;
    }
    
    public String format(String text) {
        return text.replaceAll("&", "§");
    }
    
}
