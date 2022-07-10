package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.awt.*;

public class DeathListener implements Listener {

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		HubPvP instance = HubPvP.instance();
		PvPManager pvpManager = instance.pvpManager();

		if (e.getEntity().getKiller() != null) {
			Player victim = e.getEntity(); // would be called "Killed" but it's hard to differentiate between killer and killed
			Player killer = victim.getKiller();

			if (pvpManager.isInPvP(victim) && pvpManager.isInPvP(killer)) {
				int healthOnKill = instance.getConfig().getInt("health-on-kill");

				e.setKeepInventory(true);
				e.setKeepLevel(true);

				if (healthOnKill != -1) {
					killer.setHealth(Math.min(killer.getHealth() + healthOnKill, killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
					killer.sendMessage(StringUtil.colorize(instance.getConfig().getString("health-gained-message")
							.replace("%extra%", String.valueOf(healthOnKill)).replace("%killed%", victim.getDisplayName())));
				}

				pvpManager.disablePvP(victim);
				if (!instance.getConfig().getBoolean("respawn-at-spawn"))
					victim.teleport(victim.getLocation().add(0.0D, 1.0D, 0.0D));
				else
					victim.teleport(victim.getWorld().getSpawnLocation());

				victim.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.killed")).replace("%killer%", killer.getDisplayName()));

				killer.sendMessage(
						StringUtil.colorize(instance.getConfig().getString("lang.killed-other")).replace("%killed%", victim.getDisplayName()));

				e.setDeathMessage("");
			}
		}
	}

}
