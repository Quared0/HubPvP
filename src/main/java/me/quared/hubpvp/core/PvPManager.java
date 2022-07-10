package me.quared.hubpvp.core;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.util.StringUtil;
import me.quared.itemguilib.items.CustomItem;
import org.bukkit.Material;
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

	private CustomItem weapon, helmet, chestplate, leggings, boots;

	public PvPManager() {
		playerPvpStates = new HashMap<>();
		oldPlayerDataList = new ArrayList<>();

		loadItems();
	}

	public void loadItems() {
		// Weapon
		weapon = getItemFromConfig("weapon");

		// Armor
		helmet = getItemFromConfig("helmet");
		chestplate = getItemFromConfig("chestplate");
		leggings = getItemFromConfig("leggings");
		boots = getItemFromConfig("boots");
	}

	public @Unmodifiable Map<Player, PvPState> playerPvpStates() {
		return playerPvpStates;
	}

	public void enablePvP(Player player) {
		playerState(player, PvPState.ON);

		if (getOldData(player) != null) oldPlayerDataList().remove(getOldData(player));
		oldPlayerDataList().add(new OldPlayerData(player, player.getInventory().getArmorContents(), player.getAllowFlight()));

		player.setAllowFlight(false);
		player.getInventory().setHelmet(helmet().getItemStack());
		player.getInventory().setChestplate(chestplate().getItemStack());
		player.getInventory().setLeggings(leggings().getItemStack());
		player.getInventory().setBoots(boots().getItemStack());

		player.sendMessage(StringUtil.colorize(HubPvP.instance().getConfig().getString("lang.pvp-enabled")));
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

		player.sendMessage(StringUtil.colorize(HubPvP.instance().getConfig().getString("lang.pvp-disabled")));
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
		disablePvP(p);
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

	public void giveWeapon(Player p) {
		p.getInventory().setItem(HubPvP.instance().getConfig().getInt("items.weapon.slot") - 1, weapon().getItemStack());
	}

	public CustomItem getItemFromConfig(String name) {
		HubPvP instance = HubPvP.instance();
		CustomItem item = new CustomItem(new ItemStack(Material.valueOf(instance.getConfig().getString("items." + name + ".material"))));

		String itemName = instance.getConfig().getString("items." + name + ".name");
		if (itemName != null && !itemName.isEmpty()) item.setName(StringUtil.colorize(itemName));

		List<String> lore = instance.getConfig().getStringList("items." + name + ".lore");
		if (!(lore.size() == 1 && lore.get(0).isEmpty())) item.addLore(StringUtil.colorize(lore));

		item.addFlags(ItemFlag.HIDE_UNBREAKABLE);
		item.setUnbreakable(true);

		return item;
	}

	public CustomItem boots() {
		return boots;
	}

	public CustomItem leggings() {
		return leggings;
	}

	public CustomItem chestplate() {
		return chestplate;
	}

	public CustomItem helmet() {
		return helmet;
	}

}
