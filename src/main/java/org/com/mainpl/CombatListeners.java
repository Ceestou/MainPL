package org.com.mainpl;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class CombatListeners implements Listener {

    private final EnchantManagerCompleto manager;

    public CombatListeners(EnchantManagerCompleto manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null) return;

        double extra = manager.getExtraDamage(hand);
        e.setDamage(e.getDamage() + extra);

        double knockback = manager.getExtraKnockback(hand);
        if (knockback > 0 && e.getEntity() instanceof LivingEntity target) {
            Vector dir = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
            target.setVelocity(target.getVelocity().add(dir.multiply(knockback)));
        }

        int fireTicks = manager.getFireTicks(hand);
        if (fireTicks > 0 && e.getEntity() instanceof LivingEntity target) {
            target.setFireTicks(Math.max(target.getFireTicks(), fireTicks));
        }
    }

    @EventHandler
    public void onAnyDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player pl = (Player) e.getEntity();
        e.setDamage(manager.getDamageReduction(pl, e.getDamage(), e));
    }
}
