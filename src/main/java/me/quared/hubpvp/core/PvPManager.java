package me.quared.hubpvp.core;

import lombok.Getter;
import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.util.StringUtil;
import me.quared.itemguilib.items.CustomItem;
import me.quared.itemguilib.items.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PvPManager {

	private final Map<Player, PvPState> playerPvpStates;
	private final Map<Player, BukkitRunnable> currentTimers;
	private final List<OldPlayerData> oldPlayerDataList;

	private CustomItem weapon, helmet, chestplate, leggings, boots;

	public PvPManager() {
		playerPvpStates = new HashMap<>();
		currentTimers = new HashMap<>();
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

	public CustomItem getItemFromConfig(String name) {
		HubPvP instance = HubPvP.instance();
		String material = instance.getConfig().getString("items." + name + ".material");
		if (material == null) {
			instance.getLogger().warning("Material for item " + name + " is null!");
			return null;
		}
		CustomItem item = CustomItemManager.get().createCustomItem(new ItemStack(Material.valueOf(material)));

		String itemName = instance.getConfig().getString("items." + name + ".name");
		if (itemName != null && !itemName.isEmpty()) item.setName(StringUtil.colorize(itemName));

		List<String> lore = instance.getConfig().getStringList("items." + name + ".lore");
		if (!(lore.size() == 1 && lore.get(0).isEmpty())) item.addLore(StringUtil.colorize(lore));

		List<String> enchants = instance.getConfig().getStringList("items." + name + ".enchantments");
		if (enchants != null && !enchants.isEmpty()) {
			for (String enchant : enchants) {
				String[] split = enchant.split(":");
				Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(split[0].toLowerCase()));
				if (enchantment == null) {
					HubPvP.instance().getLogger().warning("Could not find enchantment " + split[0]);
					continue;
				}
				item.getItemStack().addEnchantment(enchantment, Integer.parseInt(split[1]));
			}
		}

		item.addFlags(ItemFlag.HIDE_UNBREAKABLE);
		item.setUnbreakable(true);

		return item;
	}

	public void enablePvP(Player player) {
		setPlayerState(player, PvPState.ON);

		if (getOldData(player) != null) getOldPlayerDataList().remove(getOldData(player));
		getOldPlayerDataList().add(new OldPlayerData(player, player.getInventory().getArmorContents(), player.getAllowFlight()));

		player.setAllowFlight(false);
		player.getInventory().setHelmet(getHelmet().getItemStack());
		player.getInventory().setChestplate(getChestplate().getItemStack());
		player.getInventory().setLeggings(getLeggings().getItemStack());
		player.getInventory().setBoots(getBoots().getItemStack());

		player.sendMessage(StringUtil.colorize(HubPvP.instance().getConfig().getString("lang.pvp-enabled")));
	}

	public void setPlayerState(Player p, PvPState state) {
		playerPvpStates.put(p, state);
	}

	public @Nullable OldPlayerData getOldData(Player p) {
		return oldPlayerDataList.stream().filter(data -> data.player().equals(p)).findFirst().orElse(null);
	}

	public void removePlayer(Player p) {
		disablePvP(p);
		playerPvpStates.remove(p);
	}

	public void disablePvP(Player player) {
		setPlayerState(player, PvPState.OFF);

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

	public void disable() {
		for (Player p : playerPvpStates.keySet()) {
			if (isInPvP(p)) disablePvP(p);
		}
		playerPvpStates.clear();
	}

	public boolean isInPvP(Player player) {
		return getPlayerState(player) == PvPState.ON || getPlayerState(player) == PvPState.DISABLING;
	}

	public PvPState getPlayerState(Player p) {
		return playerPvpStates.get(p);
	}

	public void giveWeapon(Player p) {
		p.getInventory().setItem(HubPvP.instance().getConfig().getInt("items.weapon.slot") - 1, getWeapon().getItemStack());
	}

	public void putTimer(Player p, BukkitRunnable timerTask) {
		if (getCurrentTimers().containsKey(p)) {
			getCurrentTimers().get(p).cancel();
		}
		getCurrentTimers().put(p, timerTask);
	}

	public void removeTimer(Player p) {
		if (getCurrentTimers().containsKey(p)) {
			getCurrentTimers().get(p).cancel();
		}
		getCurrentTimers().remove(p);
	}

}
