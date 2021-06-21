package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Listeners implements Listener {
    
    public HashMap<Player, Integer> pvpTime = new HashMap<>();
    public HashMap<Player, BukkitRunnable> pvpTask = new HashMap<>();
    public HashMap<Player, Integer> pvpTime2 = new HashMap<>();
    public HashMap<Player, BukkitRunnable> pvpTask2 = new HashMap<>();
    public ArrayList<Player> pvp = new ArrayList<>();
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        int slot = HubPvP.getPlugin().getConfig().getInt("slot");
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        p.getInventory().setArmorContents(new ItemStack[4]);
        p.getInventory().setHeldItemSlot(0);
        try {
            swordMeta.spigot().setUnbreakable(true);
        } catch (NoSuchMethodError ignored) {
            try {
                ItemMeta.class.getMethod("setUnbreakable", Boolean.class).invoke(swordMeta, true);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored2) {
            
            }
        }
        p.setMetadata("pvp", new FixedMetadataValue(HubPvP.getPlugin(), false));
        swordMeta.setDisplayName(HubPvP.getPlugin().format(HubPvP.getPlugin().getConfig().getString("name")));
        swordMeta.setLore(Collections.singletonList(HubPvP.getPlugin().format("&7Hold to PvP!")));
        sword.setItemMeta(swordMeta);
        p.getInventory().setItem(slot - 1, sword);
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player p = (Player) e.getEntity();
            Player dam = (Player) e.getDamager();
            String world = p.getLocation().getWorld().getName();
    
            Set<String> set = new HashSet<>(HubPvP.getPlugin().getConfig().getStringList("disabled-worlds"));
            for (String s : set) {
                if (s.equalsIgnoreCase(world)) {
                    e.setCancelled(true);
                }
            }
            
            if (!this.pvp.contains(p) || !this.pvp.contains(dam)) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        HubPvP plugin = HubPvP.getPlugin();
        if (e.getEntity().getKiller() != null) {
            Player p = e.getEntity();
            Player killer = p.getKiller();
            
            if (p.getMetadata("pvp").get(0).asBoolean() &&
                    killer.getMetadata("pvp").get(0).asBoolean()) {
                if (plugin.getConfig().getInt("health-on-kill") != -1) {
                    killer.setHealth(clamp(killer.getHealth() + plugin.getConfig().getInt("health-on-kill"), 0.0, killer.getMaxHealth()));
                    killer.sendMessage(plugin.format(plugin.getConfig().getString("health-gained-message")
                            .replace("%extra%", plugin.getConfig().getString("health-on-kill")).replace("%killed%", p.getDisplayName())));
                }
                p.setHealth(p.getMaxHealth());
    
                this.pvp.remove(p);
                this.pvpTime.remove(p);
                this.pvpTask.remove(p);
                this.pvpTime2.remove(p);
                this.pvpTask2.remove(p);
                if (!plugin.getConfig().getBoolean("respawn-at-spawn"))
                    p.teleport(p.getLocation().add(0.0D, 1.0D, 0.0D));
                else
                    p.teleport(p.getWorld().getSpawnLocation());
    
                p.getInventory().setHeldItemSlot(0);
                p.sendMessage(plugin.format(plugin.getConfig().getString("killed-message")).replace("%killer%", killer.getDisplayName()));
                killer.sendMessage(
                        plugin.format(plugin.getConfig().getString("killed-other-message")).replace("%killed%", p.getDisplayName()));
                p.getInventory().setHelmet(new ItemStack(Material.AIR));
                p.getInventory().setChestplate(new ItemStack(Material.AIR));
                p.getInventory().setLeggings(new ItemStack(Material.AIR));
                p.getInventory().setBoots(new ItemStack(Material.AIR));
                p.setMetadata("pvp", new FixedMetadataValue(HubPvP.getPlugin(), false));
                e.setDeathMessage(null);
            }
        }
    }
    
    /**
     * Clamp Double values to a given range
     *
     * @param value     the value to clamp
     * @param min       the minimum value
     * @param max       the maximum value
     * @return          the clamped value
     */
    public double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent e) {
        final Player p = e.getPlayer();
        int slot = e.getNewSlot();
        int pvpSlot = HubPvP.getPlugin().getConfig().getInt("slot") - 1;
        HubPvP plugin = HubPvP.getPlugin();
        if (slot == pvpSlot) {
            if (plugin.getConfig().getInt("enable-cooldown") > 0) {
                if (Listeners.this.pvpTask.containsKey(p)) {
                    Listeners.this.pvpTask.get(p).cancel();
                    Listeners.this.pvpTask.remove(p);
                    pvpTime.put(p, HubPvP.getPlugin().getConfig().getInt("cooldown") + 1);
                    pvp.add(p);
        
                    return;
                }
                this.pvpTime2.put(p, HubPvP.getPlugin().getConfig().getInt("cooldown") + 1);
                this.pvpTask2.put(p, new BukkitRunnable() {
                    public void run() {
                        if (!Listeners.this.pvpTime2.containsKey(p)) {
                            this.cancel();
                        } else {
                            Listeners.this.pvpTime2.put(p, Listeners.this.pvpTime2.get(p) - 1);
                            if (Listeners.this.pvpTime2.get(p) == 0) {
                                Listeners.this.pvpTime2.remove(p);
                                Listeners.this.pvpTask2.remove(p);
                                p.sendMessage(HubPvP.getPlugin().format(HubPvP.getPlugin().getConfig().getString("pvp-enabled-message")));
                                Listeners.this.pvpTime.put(p, HubPvP.getPlugin().getConfig().getInt("cooldown") + 1);
                                Listeners.this.pvp.add(p);
                    
                                p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                                p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                                p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                                p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                    
                                p.setMetadata("pvp", new FixedMetadataValue(HubPvP.getPlugin(), true));
                                p.setHealth(p.getMaxHealth());
                                this.cancel();
                            } else {
                                p.sendMessage(HubPvP.getPlugin().format(HubPvP.getPlugin().getConfig().getString("pvp-enabling-message").replaceAll("%time%", Integer.toString(Listeners.this.pvpTime2.get(p)))));
                            }
                        }
            
                    }
                });
                (this.pvpTask2.get(p)).runTaskTimer(HubPvP.getPlugin(), 0L, 20L);
            } else {
                pvp.add(p);
    
                p.sendMessage(HubPvP.getPlugin().format(HubPvP.getPlugin().getConfig().getString("pvp-enabled-message")));
                
                p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    
                p.setMetadata("pvp", new FixedMetadataValue(HubPvP.getPlugin(), true));
                p.setHealth(p.getMaxHealth());
            }
//            p.sendMessage(HubPvP.getPlugin().format(HubPvP.getPlugin().getConfig().getString("pvp-enabled-message")));
//            if (this.pvpTask.containsKey(p))
//                this.pvpTask.get(p).cancel();
//            this.pvpTask.remove(p);
//            this.pvpTime.put(p, HubPvP.getPlugin().getConfig().getInt("cooldown") + 1);
//            this.pvp.add(p);
//            p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
//            p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
//            p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
//            p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
//            p.setMetadata("pvp", new FixedMetadataValue(HubPvP.getPlugin(), true));
        } else if (this.pvp.contains(p) && this.pvpTime.containsKey(p)) {
            if (plugin.getConfig().getInt("disable-cooldown") > 0) {
                this.pvpTask.put(p, new BukkitRunnable() {
                    public void run() {
                        if (!Listeners.this.pvpTime.containsKey(p)) {
                            this.cancel();
                        } else {
                            Listeners.this.pvpTime.put(p, Listeners.this.pvpTime.get(p) - 1);
                            if (Listeners.this.pvpTime.get(p) == 0) {
                                Listeners.this.pvpTime.remove(p);
                                Listeners.this.pvpTask.remove(p);
                                Listeners.this.pvp.remove(p);
                                p.sendMessage(HubPvP.getPlugin().format(HubPvP.getPlugin().getConfig().getString("pvp-disabled-message")));
                                p.getInventory().setArmorContents(new ItemStack[4]);
                                p.setMetadata("pvp", new FixedMetadataValue(HubPvP.getPlugin(), false));
                                p.setHealth(p.getMaxHealth());
                                this.cancel();
                            } else {
                                p.sendMessage(HubPvP.getPlugin().format(HubPvP.getPlugin().getConfig().getString("pvp-disabling-message").replaceAll("%time%", Integer.toString(Listeners.this.pvpTime.get(p)))));
                            }
                        }
            
                    }
                });
                (this.pvpTask.get(p)).runTaskTimer(HubPvP.getPlugin(), 0L, 20L);
            } else {
                Listeners.this.pvp.remove(p);
                p.sendMessage(HubPvP.getPlugin().format(HubPvP.getPlugin().getConfig().getString("pvp-disabled-message")));
                p.getInventory().setArmorContents(new ItemStack[4]);
                p.setMetadata("pvp", new FixedMetadataValue(HubPvP.getPlugin(), false));
                p.setHealth(p.getMaxHealth());
            }
        } else if (pvpTask2.containsKey(p)) {
            pvpTask2.get(p).cancel();
            pvpTask2.remove(p);
            pvpTime2.put(p, HubPvP.getPlugin().getConfig().getInt("cooldown") + 1);
            pvp.remove(p);
        }
    }
    
    private void pvpOn(Player p) {
    
    }
    
    private void pvpOff(Player p) {
    
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item.getType().equals(Material.DIAMOND_SWORD) && item.hasItemMeta()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(HubPvP.getPlugin().getConfig().getString("name")))) {
            e.setCancelled(true);
        }
        
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack item = e.getItemDrop().getItemStack();
        if (item.getType().equals(Material.DIAMOND_SWORD)
                && item.hasItemMeta()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(HubPvP.getPlugin().getConfig().getString("name")))) {
            e.setCancelled(true);
        }
    }
    
}
