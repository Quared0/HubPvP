package me.quared.hubpvp.core;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.util.StringUtil;
import me.quared.itemguilib.items.CustomItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PvPManager {

	private final Map<Player, PvPState> playerPvpStates;
	private final List<OldPlayerData> oldPlayerDataList;

	private final CustomItem weapon;

	public PvPManager() {
		FileConfiguration config = HubPvP.instance().getConfig();
		playerPvpStates = new HashMap<>();
		oldPlayerDataList = new ArrayList<>();

		// Weapon
		weapon = new CustomItem(new ItemStack(Material.valueOf(config.getString("weapon.material"))));
		weapon.setName(StringUtil.colorize(config.getString("weapon.name")));
		weapon.addLore(StringUtil.colorize(config.getStringList("weapon.lore")));
		weapon.addFlags(ItemFlag.HIDE_UNBREAKABLE);
		weapon.setUnbreakable(true);
	}

	public @Unmodifiable Map<Player, PvPState> playerPvpStates() {
		return playerPvpStates;
	}

	public void enablePvP(Player player) {
		playerState(player, PvPState.ON);

		if (getOldData(player) != null) oldPlayerDataList().remove(getOldData(player));
		oldPlayerDataList().add(new OldPlayerData(player, player.getInventory().getArmorContents(), player.getAllowFlight()));

		player.setAllowFlight(false);
		player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
		player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

		player.sendMessage(StringUtil.colorize(HubPvP.instance().getConfig().getString("pvp-enabled-message")));
	}

	public void disablePvP(Player player) {
		playerState(player, PvPState.OFF);

		OldPlayerData oldPlayerData = getOldData(player);
		if (oldPlayerData != null) {
			player.getInventory().setHelmet(oldPlayerData.armor()[3] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[3]);
			player.getInventory().setChestplate(oldPlayerData.armor()[2] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[2]);
			player.getInventory().setLeggings(oldPlayerData.armor()[1] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[1]);
			player.getInventory().setBoots(oldPlayerData.armor()[0] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[0]);
			player.setAllowFlight(oldPlayerData.canFly());
		}

		player.sendMessage(StringUtil.colorize(HubPvP.instance().getConfig().getString("pvp-disabled-message")));
	}

	public boolean isInPvP(Player player) {
		return playerState(player) == PvPState.ON || playerState(player) == PvPState.DISABLING;
	}

	public CustomItem weapon() {
		return weapon;
	}

	public void playerState(Player p, PvPState state) {
		playerPvpStates.put(p, state);
	}

	public PvPState playerState(Player p) {
		return playerPvpStates.get(p);
	}

	public void removePlayer(Player p) {
		playerPvpStates.remove(p);
	}

    public void disable() {
        for (Player p : playerPvpStates.keySet()) {
            if (isInPvP(p)) disablePvP(p);
        }
        playerPvpStates.clear();
    }

	public List<OldPlayerData> oldPlayerDataList() {
		return oldPlayerDataList;
	}

	public @Nullable OldPlayerData getOldData(Player p) {
		return oldPlayerDataList.stream().filter(data -> data.player().equals(p)).findFirst().orElse(null);
	}

}
