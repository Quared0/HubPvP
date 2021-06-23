package me.quared.hubpvp;

import me.quared.hubpvp.commands.HubPvPCommand;
import me.quared.hubpvp.listeners.Listeners;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

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
        
        loadConfig();
    }
    
    public void loadConfig() {
        try {
            boolean changesMade = false;
            FileConfiguration config = getConfig();
            YamlConfiguration tmp = new YamlConfiguration();
            InputStream in = getClass().getResourceAsStream("/config.yml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            tmp.load(reader);
            
            for (String str : tmp.getKeys(true)) {
                if (!config.getKeys(true).contains(str)) {
                    config.set(str, tmp.get(str));
                    changesMade = true;
                }
            }
            if (changesMade) config.save(getDataFolder() + "/config.yml");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
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
