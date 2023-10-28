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

import java.util.Objects;

public class ItemSlotChangeListener implements Listener {

	@EventHandler
	public void onSlotChange(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		ItemStack held = e.getPlayer().getInventory().getItem(e.getNewSlot());
		HubPvP instance = HubPvP.instance();
		PvPManager pvpManager = instance.pvpManager();

		if (!p.hasPermission("hubpvp.use")) return;

		if (Objects.equals(held, pvpManager.getWeapon().getItemStack())) {
			if (pvpManager.getPlayerState(p) == PvPState.DISABLING) pvpManager.setPlayerState(p, PvPState.ON);
			if (pvpManager.getPlayerState(p) == PvPState.ENABLING) return;

			if (HubPvP.instance().getConfig().getStringList("disabled-worlds").contains(p.getWorld().getName())) {
				p.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.disabled-in-world")));
				return;
			}

			// Equipping
			if (!pvpManager.isInPvP(p)) {
				pvpManager.setPlayerState(p, PvPState.ENABLING);
				BukkitRunnable enableTask = new BukkitRunnable() {
					int time = instance.getConfig().getInt("enable-cooldown") + 1;

					public void run() {
						time--;
						if (pvpManager.getPlayerState(p) != PvPState.ENABLING || !held.isSimilar(pvpManager.getWeapon().getItemStack())) {
							pvpManager.removeTimer(p);
							cancel();
						} else if (time == 0) {
							pvpManager.enablePvP(p);
							pvpManager.removeTimer(p);
							cancel();
						} else {
							p.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.pvp-enabling").replaceAll("%time%", Integer.toString(time))));
						}
					}
				};
				pvpManager.putTimer(p, enableTask);
				enableTask.runTaskTimer(instance, 0L, 20L);
			}
		} else if (pvpManager.isInPvP(p)) {
			if (pvpManager.getPlayerState(p) == PvPState.ENABLING) pvpManager.setPlayerState(p, PvPState.OFF);
			if (pvpManager.getPlayerState(p) == PvPState.DISABLING) return;
			// Dequipping
			pvpManager.setPlayerState(p, PvPState.DISABLING);
			BukkitRunnable disableTask = new BukkitRunnable() {
				int time = instance.getConfig().getInt("disable-cooldown") + 1;

				public void run() {
					time--;
					if (pvpManager.getPlayerState(p) != PvPState.DISABLING || held != null && held.isSimilar(pvpManager.getWeapon().getItemStack())) {
						pvpManager.removeTimer(p);
						cancel();
					} else if (time == 0) {
						pvpManager.disablePvP(p);
						pvpManager.removeTimer(p);
						cancel();
					} else {
						p.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.pvp-disabling").replaceAll("%time%", Integer.toString(time))));
					}
				}
			};
			pvpManager.putTimer(p, disableTask);
			disableTask.runTaskTimer(instance, 0L, 20L);
		} else {
			// Not in PvP and not equipping
			pvpManager.setPlayerState(p, PvPState.OFF); // Ensure there isn't any lingering state
			pvpManager.removeTimer(p);
		}
	}

}
