package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.OldPlayerData;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.core.PvPState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		PvPManager pvPManager = HubPvP.instance().pvpManager();

		if (p.hasPermission("hubpvp.use")) {
			p.getInventory().setItem(HubPvP.instance().getConfig().getInt("slot") - 1, pvPManager.weapon().getItemStack());
		}

		pvPManager.oldPlayerDataList().add(new OldPlayerData(p, p.getInventory().getArmorContents(), p.getAllowFlight()));
		pvPManager.playerState(p, PvPState.OFF);

	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		PvPManager pvPManager = HubPvP.instance().pvpManager();

		pvPManager.removePlayer(p);
	}

}
