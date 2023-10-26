package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        HubPvP instance = HubPvP.instance();
        PvPManager pvpManager = instance.pvpManager();

        if (e.getEntity().getKiller() == null) return;

        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        if (!pvpManager.isInPvP(victim) || !pvpManager.isInPvP(killer)) return;

        int healthOnKill = instance.getConfig().getInt("health-on-kill");

        e.setKeepInventory(true);
        e.setKeepLevel(true);

        victim.getInventory().setHeldItemSlot(0);

        if (healthOnKill != -1) {
            killer.setHealth(Math.min(killer.getHealth() + healthOnKill, killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
            killer.sendMessage(StringUtil.colorize(instance.getConfig().getString("health-gained-message")
                    .replace("%extra%", String.valueOf(healthOnKill)).replace("%killed%", victim.getDisplayName())));
        }

        pvpManager.disablePvP(victim);

        victim.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.killed")).replace("%killer%", killer.getDisplayName()));

        killer.sendMessage(
                StringUtil.colorize(instance.getConfig().getString("lang.killed-other")).replace("%killed%", victim.getDisplayName()));

        e.setDeathMessage("");
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        HubPvP instance = HubPvP.instance();
        Player p = e.getPlayer();

        ConfigurationSection respawnSection = instance.getConfig().getConfigurationSection("respawn");

        if (!respawnSection.getBoolean("enabled")) return;

        if (respawnSection.getBoolean("use-world-spawn", false)) {
            e.setRespawnLocation(p.getWorld().getSpawnLocation());
        } else {
            Location spawn = new Location(
                    p.getWorld(),
                    respawnSection.getDouble("x"),
                    respawnSection.getDouble("y"),
                    respawnSection.getDouble("z"),
                    respawnSection.getInt("yaw"),
                    respawnSection.getInt("pitch")
            );
            e.setRespawnLocation(spawn);
        }
    }

}
