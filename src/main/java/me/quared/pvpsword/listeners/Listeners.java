package me.quared.pvpsword.listeners;

import me.quared.pvpsword.PvPSword;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Listeners implements Listener {
    
    public HashMap<Player, Integer> pvpTime = new HashMap();
    public HashMap<Player, BukkitRunnable> pvpTask = new HashMap();
    public ArrayList<Player> pvp = new ArrayList();
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        int slot = PvPSword.getPlugin().getConfig().getInt("slot");
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.spigot().setUnbreakable(true);
        swordMeta.setDisplayName(PvPSword.getPlugin().format(PvPSword.getPlugin().getConfig().getString("name")));
        swordMeta.setLore(Collections.singletonList(PvPSword.getPlugin().format("&7Hold to PvP!")));
        sword.setItemMeta(swordMeta);
        p.getInventory().setItem(slot - 1, sword);
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player vic = (Player)e.getEntity();
            Player dam = (Player)e.getDamager();
            String world = vic.getLocation().getWorld().getName();
    
            for (String s : PvPSword.getPlugin().getConfig().getStringList("disabled-worlds")) {
                if (s.equalsIgnoreCase(world)) {
                    e.setCancelled(true);
                }
            }
            
            if (!this.pvp.contains(vic) || !this.pvp.contains(dam)) {
                e.setCancelled(true);
            }
        }
        
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        PvPSword plugin = PvPSword.getPlugin();
        if (e.getEntity() instanceof Player || e.getEntity().getKiller() instanceof Player) {
            Player p = e.getEntity();
            Player killer = p.getKiller();
            p.setHealth(20.0D);
            this.pvp.remove(p);
            this.pvpTime.remove(p);
            this.pvpTask.remove(p);
            p.teleport(p.getLocation().add(0.0D, 1.0D, 0.0D));
    
            for (Player all : Bukkit.getOnlinePlayers()) {
                all.spigot().playEffect(p.getLocation().add(0.0D, 1.0D, 0.0D), Effect.CRIT, 0, 0, 0.2F, 0.2F, 0.2F, 0.3F, 80, 287);
            }
            
            p.getInventory().setHeldItemSlot(0);
            killer.sendMessage(plugin.format(plugin.getConfig().getString("killed-message")).replace("%killed%", p.getDisplayName()));
            p.sendMessage(
                    plugin.format(plugin.getConfig().getString("killed-other-message")).replace("%killer%", killer.getDisplayName()));
            p.getInventory().setHelmet(new ItemStack(Material.AIR));
            p.getInventory().setChestplate(new ItemStack(Material.AIR));
            p.getInventory().setLeggings(new ItemStack(Material.AIR));
            p.getInventory().setBoots(new ItemStack(Material.AIR));
            e.setDeathMessage(null);
        }
    }
    
    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent e) {
        final Player p = e.getPlayer();
        int slot = e.getNewSlot();
        int pvpSlot = PvPSword.getPlugin().getConfig().getInt("slot") - 1;
        if (slot == pvpSlot) {
            if (!this.pvp.contains(p) && !this.pvpTime.containsKey(p) && !this.pvpTask.containsKey(p)) {
                p.sendMessage(PvPSword.getPlugin().format(PvPSword.getPlugin().getConfig().getString("pvp-enabled-message")));
                this.pvpTime.put(p, PvPSword.getPlugin().getConfig().getInt("cooldown"));
                this.pvp.add(p);
                p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            }
        } else if (this.pvp.contains(p) && this.pvpTime.containsKey(p)) {
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
                            p.sendMessage(PvPSword.getPlugin().format(PvPSword.getPlugin().getConfig().getString("pvp-disabled-message")));
                            p.getInventory().setArmorContents(new ItemStack[4]);
                            this.cancel();
                        } else {
                            p.sendMessage(PvPSword.getPlugin().format(PvPSword.getPlugin().getConfig().getString("pvp-disabling-message").replaceAll("%time%", Integer.toString((Integer)Listeners.this.pvpTime.get(p)))));
                        }
                    }
                    
                }
            });
            (this.pvpTask.get(p)).runTaskTimer(PvPSword.getPlugin(), 0L, 20L);
        }
        
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (item.getType().equals(Material.DIAMOND_SWORD) && item.hasItemMeta()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(PvPSword.getPlugin().getConfig().getString("name")))) {
            e.setCancelled(true);
        }
        
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            e.setCancelled(true);
        }
        
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (item.getType().equals(Material.DIAMOND_SWORD)
                && item.hasItemMeta()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(PvPSword.getPlugin().getConfig().getString("name")))) {
            e.setCancelled(true);
        }
        
    }
    
}
