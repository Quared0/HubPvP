package me.quared.pvpsword;

import me.quared.pvpsword.commands.PvPSwordCommand;
import me.quared.pvpsword.listeners.Listeners;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPSword extends JavaPlugin {
    
    private static String version;
    private static PvPSword plugin;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        
        getServer().getConsoleSender().sendMessage(format("&aPvPSword v" + getDescription().getVersion() + " enabled."));
        
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        
        getCommand("pvpsword").setExecutor(new PvPSwordCommand());
        getCommand("pvpsword").setTabCompleter(new PvPSwordCommand());
    }
    
    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(format("&cPvPSword v" + getDescription().getVersion() + " disabled."));
    }
    
    public static PvPSword getPlugin() {
        return plugin;
    }
    
    public String format(String text) {
        return text.replaceAll("&", "§");
    }
    
}
