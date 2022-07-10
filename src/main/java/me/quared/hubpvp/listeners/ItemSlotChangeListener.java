package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.core.PvPState;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemSlotChangeListener implements Listener {

	@EventHandler
	public void onSlotChange(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		ItemStack held = e.getPlayer().getInventory().getItem(e.getNewSlot());
		HubPvP instance = HubPvP.instance();
		PvPManager pvPManager = instance.pvpManager();

		if (!p.hasPermission("hubpvp.use")) return;

		if (held != null && held.isSimilar(pvPManager.weapon().getItemStack())) {
			if (pvPManager.playerState(p) == PvPState.DISABLING) pvPManager.playerState(p, PvPState.ON);
			// Equipping
			if (!pvPManager.isInPvP(p)) {
				pvPManager.playerState(p, PvPState.ENABLING);
				new BukkitRunnable() {
					int time = instance.getConfig().getInt("enable-cooldown") + 1;

					public void run() {
						time--;
						if (pvPManager.playerState(p) != PvPState.ENABLING || !held.isSimilar(pvPManager.weapon().getItemStack())) {
							cancel();
						} else if (time == 0) {
							pvPManager.enablePvP(p);
							cancel();
						} else {
							p.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.pvp-enabling").replaceAll("%time%", Integer.toString(time))));
						}
					}
				}.runTaskTimer(instance, 0L, 20L);
			}
		} else if (pvPManager.isInPvP(p)) {
			if (pvPManager.playerState(p) == PvPState.ENABLING) pvPManager.playerState(p, PvPState.OFF);
			// Dequipping
			pvPManager.playerState(p, PvPState.DISABLING);
			new BukkitRunnable() {
				int time = instance.getConfig().getInt("disable-cooldown") + 1;

				public void run() {
					time--;
					if (pvPManager.playerState(p) != PvPState.DISABLING || held != null && held.isSimilar(pvPManager.weapon().getItemStack())) {
						cancel();
					} else if (time == 0) {
						pvPManager.disablePvP(p);
						cancel();
					} else {
						p.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.pvp-disabling").replaceAll("%time%", Integer.toString(time))));
					}
				}
			}.runTaskTimer(instance, 0L, 20L);
		} else {
			// Not in PvP and not equipping
			pvPManager.playerState(p, PvPState.OFF); // Ensure there isn't any lingering state
		}
	}

}
