package org.com.mainpl;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BowListeners implements Listener {

    private final MainPL plugin;
    private final NamespacedKey kPower, kPunch, kFlame;

    public BowListeners(MainPL plugin) {
        this.plugin = plugin;
        kPower = new NamespacedKey(plugin, "ue_arrow_power");
        kPunch = new NamespacedKey(plugin, "ue_arrow_punch");
        kFlame = new NamespacedKey(plugin, "ue_arrow_flame");
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getProjectile() instanceof Arrow)) return;

        Player p = (Player) e.getEntity();
        Arrow arrow = (Arrow) e.getProjectile();

        int power = EnchantUtil.getEffectiveLevel(e.getBow(), Enchantment.POWER);
        int punch = EnchantUtil.getEffectiveLevel(e.getBow(), Enchantment.PUNCH);
        int flame = EnchantUtil.getEffectiveLevel(e.getBow(), Enchantment.FLAME);


        PersistentDataContainer pdc = arrow.getPersistentDataContainer();
        pdc.set(kPower, PersistentDataType.INTEGER, power);
        pdc.set(kPunch, PersistentDataType.INTEGER, punch);
        pdc.set(kFlame, PersistentDataType.INTEGER, flame);
    }

    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getDamager();
        PersistentDataContainer pdc = arrow.getPersistentDataContainer();

        Integer power = pdc.get(kPower, PersistentDataType.INTEGER);
        Integer punch = pdc.get(kPunch, PersistentDataType.INTEGER);
        Integer flame = pdc.get(kFlame, PersistentDataType.INTEGER);

        if (power != null && power > 5) {
            double per = plugin.getConfig().getDouble("formulas.power.extra-per-level", 0.5);
            double extra = (power - 5) * per;
            e.setDamage(e.getDamage() + extra);
        }

        if (punch != null && punch > 2 && e.getEntity() instanceof LivingEntity) {

        }

        if (flame != null && flame > 1 && e.getEntity() instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) e.getEntity();
            int baseTicks = 100 * flame;
            le.setFireTicks(Math.max(le.getFireTicks(), baseTicks));
        }
    }
}
