package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH) // WORK ON WHITELIST/BLACKLISTED WORLDS
	public void onDamage(EntityDamageByEntityEvent e) {
		HubPvP instance = HubPvP.instance();
		PvPManager pvpManager = instance.pvpManager();
		if (e.getEntity() instanceof Player damager && e.getDamager() instanceof Player damaged) {
			World world = damager.getLocation().getWorld();

			if (instance.getConfig().getStringList("disabled-worlds").contains(world.getName())) e.setCancelled(true);

			e.setCancelled(!pvpManager.isInPvP(damager) || !pvpManager.isInPvP(damaged));
		}
	}

}
