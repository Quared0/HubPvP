package me.quared.hubpvp;

import me.quared.hubpvp.bstats.Metrics;
import me.quared.hubpvp.commands.HubPvPCommand;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.listeners.*;
import me.quared.hubpvp.util.StringUtil;
import me.quared.itemguilib.ItemGuiLib;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class HubPvP extends JavaPlugin {

	private static HubPvP instance;

	private PvPManager pvpManager;


	public static HubPvP instance() {
		return instance;
	}

	@Override
	public void onDisable() {
		pvpManager.disable();
		getServer().getConsoleSender().sendMessage(StringUtil.colorize("&c" + getDescription().getName() + " v" + getDescription().getVersion() + " disabled."));
	}

	@Override
	public void onEnable() {
		instance = this;
		ItemGuiLib.setPluginInstance(this);
		pvpManager = new PvPManager();

		new Metrics(this, 11826); // bStats

		registerListeners();
		registerCommands();

		getConfig().options().copyDefaults(true);
		saveDefaultConfig();

		getLogger().info(StringUtil.colorize("&a" + getDescription().getName() + " v" + getDescription().getVersion() + " enabled."));
	}

	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new DamageListener(), this);
		pm.registerEvents(new DeathListener(), this);
		pm.registerEvents(new ItemSlotChangeListener(), this);
		pm.registerEvents(new PlayerJoinListener(), this);
		pm.registerEvents(new ProtectionListeners(), this);
	}

	private void registerCommands() {
		Objects.requireNonNull(getCommand("hubpvp")).setExecutor(new HubPvPCommand());
		Objects.requireNonNull(getCommand("hubpvp")).setTabCompleter(new HubPvPCommand());
	}

	public PvPManager pvpManager() {
		return pvpManager;
	}

}
