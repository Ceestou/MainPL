package org.com.mainpl;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static org.com.mainpl.EnchantUtil.getEffectiveLevel;

public class EnchantManagerCompleto {

    private final MainPL plugin;

    public EnchantManagerCompleto(MainPL plugin) {
        this.plugin = plugin;
    }

    // ===================== DANO ARMAS =====================
    public double getExtraDamage(ItemStack item) {
        if (item == null) return 0.0;
        double extra = 0.0;
        extra += getDamagePerLevel(item, Enchantment.SHARPNESS, 5, "sharpness");
        extra += getDamagePerLevel(item, Enchantment.SMITE, 5, "smite");
        extra += getDamagePerLevel(item, Enchantment.BANE_OF_ARTHROPODS, 5, "bane");
        return extra;
    }

    private double getDamagePerLevel(ItemStack item, Enchantment ench, int vanillaMax, String configPath) {
        int lvl = getEffectiveLevel(item, ench);
        if (lvl <= vanillaMax) return 0;
        double per = plugin.getConfig().getDouble("formulas." + configPath + ".extra-per-level", 1.5);
        return (lvl - vanillaMax) * per;
    }

    // ===================== KNOCKBACK =====================
    public double getExtraKnockback(ItemStack item) {
        if (item == null) return 0.0;
        int kb = getEffectiveLevel(item, Enchantment.KNOCKBACK);
        if (kb <= 2) return 0.0;
        double per = plugin.getConfig().getDouble("formulas.knockback.extra-per-level", 0.2);
        return (kb - 2) * per;
    }

    // ===================== FIRE ASPECT =====================
    public int getFireTicks(ItemStack item) {
        if (item == null) return 0;
        int fire = getEffectiveLevel(item, Enchantment.FIRE_ASPECT);
        if (fire <= 2) return 0;
        return 80 * fire;
    }

    // ===================== ARMADURA =====================
    public double getDamageReduction(Player player, double baseDamage, EntityDamageEvent e) {
        double totalReduction = 0.0;
        double perLevel = plugin.getConfig().getDouble("formulas.protection.extra-percent-per-level", 0.04);
        double capTotal = plugin.getConfig().getDouble("formulas.protection.total-cap", 0.90);

        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (piece == null) continue;

            totalReduction += getReductionPerLevel(piece, Enchantment.PROTECTION, 4, perLevel);

            switch (e.getCause()) {
                case PROJECTILE -> totalReduction += getReductionPerLevel(piece, Enchantment.PROJECTILE_PROTECTION, 4, perLevel);
                case FIRE, LAVA, FIRE_TICK, HOT_FLOOR -> totalReduction += getReductionPerLevel(piece, Enchantment.FIRE_PROTECTION, 4, perLevel);
                case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> totalReduction += getReductionPerLevel(piece, Enchantment.BLAST_PROTECTION, 4, perLevel);
                default -> {}
            }

            // Thorns
            int thorns = getEffectiveLevel(piece, Enchantment.THORNS);
            if (thorns > 3) {
                double chance = plugin.getConfig().getDouble("formulas.thorns.extra-chance-per-level", 0.15);
                totalReduction += (thorns - 3) * chance;
            }

            // Durabilidade e Mending
            int unbreaking = getEffectiveLevel(piece, Enchantment.UNBREAKING);
            if (unbreaking > 3) totalReduction += 0;
            int mending = getEffectiveLevel(piece, Enchantment.MENDING);
        }

        totalReduction = Math.min(totalReduction, capTotal);
        return baseDamage * (1.0 - totalReduction);
    }

    private double getReductionPerLevel(ItemStack piece, Enchantment ench, int vanillaMax, double perLevel) {
        int lvl = getEffectiveLevel(piece, ench);
        if (lvl <= vanillaMax) return 0.0;
        return (lvl - vanillaMax) * perLevel;
    }
    public double getEffectiveEfficiency(ItemStack item) {
        if (item == null) return 0;
        return getEffectiveLevel(item, Enchantment.EFFICIENCY);
    }



    // ===================== FERRAMENTAS =====================
    public double getMiningSpeed(ItemStack item) {
        if (item == null) return 1.0;
        int efficiency = getEffectiveLevel(item, Enchantment.EFFICIENCY);

        double per = plugin.getConfig().getDouble("formulas.efficiency.extra-per-level", 0.5);
        return 1.0 + efficiency * per;
    }


    private double getVanillaBreakTime(Block block, ItemStack tool) {
        Material type = block.getType();
        String toolType = tool.getType().name();


        if (type.name().contains("LOG") || type == Material.OAK_PLANKS || type == Material.BOOKSHELF) {
            if (toolType.endsWith("_AXE")) return 0.8;
            return 1.2;
        }


        if (type.name().contains("STONE") || type.name().endsWith("_ORE")) {
            if (toolType.endsWith("_PICKAXE")) return 1.5;
            return 5.0;
        }


        if (type == Material.DIRT || type == Material.GRASS_BLOCK || type == Material.SAND || type == Material.GRAVEL) {
            if (toolType.endsWith("_SHOVEL")) return 0.5;
            return 1.0;
        }


        return 1.0;
    }


    public double getExtraDrops(ItemStack item) {
        if (item == null) return 0.0;
        int fortune = getEffectiveLevel(item, Enchantment.FORTUNE);
        if (fortune <= 3) return 0.0;
        double per = plugin.getConfig().getDouble("formulas.fortune.extra-per-level", 0.25);
        return (fortune - 3) * per;
    }

    public boolean hasSilkTouch(ItemStack item) {
        return getEffectiveLevel(item, Enchantment.SILK_TOUCH) > 0;
    }

    // ===================== DURABILIDADE =====================
    public double getDurabilityMultiplier(ItemStack item) {
        if (item == null) return 1.0;
        int unbreaking = getEffectiveLevel(item, Enchantment.UNBREAKING);
        if (unbreaking <= 3) return 1.0;
        double per = plugin.getConfig().getDouble("formulas.unbreaking.extra-percent-per-level", 0.05);
        double bonus = (unbreaking - 3) * per;
        return 1.0 - Math.min(bonus, 0.95);
    }

    public boolean shouldConsumeDurability(ItemStack item) {
        double multiplier = getDurabilityMultiplier(item);
        return Math.random() < multiplier;
    }

    // ===================== ARCO =====================
    public double getBowExtraDamage(ItemStack bow) {
        if (bow == null) return 0;
        int power = getEffectiveLevel(bow, Enchantment.POWER);
        if (power <= 5) return 0;
        double per = plugin.getConfig().getDouble("formulas.power.extra-per-level", 1.5);
        return (power - 5) * per;
    }

    public double getArrowKnockback(ItemStack bow) {
        if (bow == null) return 0;
        int punch = getEffectiveLevel(bow, Enchantment.PUNCH);
        if (punch <= 2) return 0;
        double per = plugin.getConfig().getDouble("formulas.punch.extra-per-level", 0.2);
        return (punch - 2) * per;
    }

    public int getArrowFireTicks(ItemStack bow) {
        if (bow == null) return 0;
        int flame = getEffectiveLevel(bow, Enchantment.FLAME);
        if (flame <= 1) return 0;
        return 80 * flame;
    }

    public boolean hasInfinity(ItemStack bow) {
        return getEffectiveLevel(bow, Enchantment.INFINITY) > 0;
    }

    // ===================== TRIDENTES =====================
    public double getTridentExtraDamage(ItemStack trident) {
        if (trident == null) return 0;
        int impaling = getEffectiveLevel(trident, Enchantment.IMPALING);
        if (impaling <= 5) return 0;
        double per = plugin.getConfig().getDouble("formulas.impaling.extra-per-level", 1.5);
        return (impaling - 5) * per;
    }

    public boolean hasRiptide(ItemStack trident) {
        return getEffectiveLevel(trident, Enchantment.RIPTIDE) > 0;
    }

    public boolean hasChanneling(ItemStack trident) {
        return getEffectiveLevel(trident, Enchantment.CHANNELING) > 0;
    }

    public boolean hasLoyalty(ItemStack trident) {
        return getEffectiveLevel(trident, Enchantment.LOYALTY) > 0;
    }

    // ===================== LORE =====================
    public void updateItemLore(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();
        for (Enchantment ench : item.getEnchantments().keySet()) {
            int lvl = getEffectiveLevel(item, ench);
            double bonus = 0;

            // Calcular bônus dependendo do tipo do encantamento
            switch (ench.getKey().getKey()) {
                case "sharpness" -> bonus = getDamagePerLevel(item, Enchantment.SHARPNESS, 5, "sharpness");
                case "smite" -> bonus = getDamagePerLevel(item, Enchantment.SMITE, 5, "smite");
                case "bane_of_arthropods" -> bonus = getDamagePerLevel(item, Enchantment.BANE_OF_ARTHROPODS, 5, "bane");
                case "fire_aspect" -> bonus = getFireTicks(item)/20.0;
                case "knockback" -> bonus = getExtraKnockback(item);
                case "arrow_damage" -> bonus = getBowExtraDamage(item);
                case "arrow_knockback" -> bonus = getArrowKnockback(item);
                case "arrow_fire" -> bonus = getArrowFireTicks(item)/20.0;
                case "impaling" -> bonus = getTridentExtraDamage(item);
                default -> bonus = 0;
            }

            lore.add(ench.getKey().getKey() + " " + lvl + (bonus > 0 ? " (+" + String.format("%.1f", bonus) + ")" : ""));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
