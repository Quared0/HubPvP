package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class ProtectionListeners implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		PvPManager pvPManager = HubPvP.instance().pvpManager();
		if (item == null) return;

		if (pvPManager.isInPvP(p)) {
			if (item.isSimilar(pvPManager.weapon().getItemStack())) {
				e.setCancelled(true);
			} else if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItemDrop().getItemStack();
		PvPManager pvPManager = HubPvP.instance().pvpManager();

		if (pvPManager.isInPvP(p)) {
			if (item.isSimilar(pvPManager.weapon().getItemStack())) {
				e.setCancelled(true);
			} else if (item.getType().toString().toLowerCase().contains("armor")) { // very bad way of doing this, feel free to make a new branch to update
				e.setCancelled(true);
			}
		}
	}

}
