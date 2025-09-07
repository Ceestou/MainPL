package org.com.mainpl;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class EnchantUtil {

    private static Plugin plugin;

    public static void init(Plugin pl) {
        plugin = pl;
    }

    public static NamespacedKey keyFor(Enchantment ench) {
        return new NamespacedKey(plugin, "ue_ratio_" + ench.getKey().getKey());
    }




    public static void setRatio(ItemStack item, Enchantment ench, double ratioClamped) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        ratioClamped = Math.max(0d, Math.min(1d, ratioClamped));


        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyFor(ench), PersistentDataType.DOUBLE, ratioClamped);


        int eff = getEffectiveLevelFromRatio(ratioClamped);
        if (eff < 1) eff = 1;
        meta.addEnchant(ench, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);


        item.setItemMeta(meta);
        rewriteLore(item);
    }

    public static double getRatio(ItemStack item, Enchantment ench) {
        ItemMeta meta = (item != null) ? item.getItemMeta() : null;
        if (meta == null) return 0d;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Double v = pdc.get(keyFor(ench), PersistentDataType.DOUBLE);
        if (v != null) return Math.max(0d, Math.min(1d, v));

        int lvl = meta.getEnchantLevel(ench);
        int max = MainPL.get().getMaxLevel();
        if (max <= 0) return 0d;
        return Math.max(0d, Math.min(1d, (double) lvl / (double) max));
    }

    public static int getEffectiveLevel(ItemStack item, Enchantment ench) {
        double ratio = getRatio(item, ench);
        int max = MainPL.get().getMaxLevel();

        if (ratio == 0d) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int lvl = meta.getEnchantLevel(ench);
                if (lvl > 0) return lvl;
            }
        }

        return getEffectiveLevelFromRatio(ratio);
    }

    public static int getEffectiveLevelFromRatio(double ratio) {
        int max = MainPL.get().getMaxLevel();
        return (int) Math.round(ratio * max);
    }


    public static void rewriteLore(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;


        List<String> lines = new ArrayList<>();

        Map<Enchantment, Integer> effective = new HashMap<>();
        for (Enchantment e : meta.getEnchants().keySet()) {
            int eff = getEffectiveLevel(item, e);
            if (eff > 0) effective.put(e, eff);
        }


        List<Map.Entry<Enchantment,Integer>> ordered = effective.entrySet().stream()
                .sorted(Comparator.comparing(a -> a.getKey().getKey().getKey()))
                .collect(Collectors.toList());

        for (Map.Entry<Enchantment, Integer> en : ordered) {
            String name = pretty(en.getKey());
            int lvl = en.getValue();
            lines.add(ChatColor.GRAY + name + " " + lvl);
        }


        String contrib = contributionLine(item, effective);
        if (contrib != null) lines.add(contrib);

        meta.setLore(lines.isEmpty() ? null : lines);
        item.setItemMeta(meta);
    }

    private static String pretty(Enchantment e) {
        String k = e.getKey().getKey().replace('_', ' ');
        String[] parts = k.split(" ");
        for (int i=0;i<parts.length;i++) {
            parts[i] = parts[i].substring(0,1).toUpperCase(Locale.ROOT) + parts[i].substring(1);
        }
        return String.join(" ", parts);
    }

    private static String contributionLine(ItemStack item, Map<Enchantment,Integer> eff) {

        String type = item.getType().name();
        boolean armor = type.endsWith("_HELMET") || type.endsWith("_CHESTPLATE")
                || type.endsWith("_LEGGINGS") || type.endsWith("_BOOTS") || type.contains("ELYTRA");

        if (!armor) {
            int sharp = eff.getOrDefault(Enchantment.SHARPNESS, 0);
            if (sharp > 5) {
                double extra = (sharp - 5) * MainPL.get().getConfig().getDouble("formulas.sharpness.extra-per-level", 1.5);
                return ChatColor.AQUA + "+ " + format(extra) + " dano";
            }
            return null;
        } else {

            int prot = eff.getOrDefault(Enchantment.PROJECTILE_PROTECTION, 0);
            double perLevel = MainPL.get().getConfig().getDouble("formulas.protection.extra-percent-per-level", 0.04); // 4%
            double extraPct = 0d;
            if (prot > 4) extraPct += (prot - 4) * perLevel;

            if (extraPct > 0) {
                double capPiece = MainPL.get().getConfig().getDouble("formulas.protection.piece-cap", 0.40); // máx 40% por peça
                extraPct = Math.min(extraPct, capPiece);
                return ChatColor.AQUA + "Redução desta peça: " + formatPct(extraPct);
            }
            return null;
        }
    }

    private static String format(double v) {
        return (Math.abs(v - Math.round(v)) < 1e-9) ? String.valueOf((long)Math.round(v)) : String.format(Locale.US, "%.2f", v);
    }
    private static String formatPct(double v) {
        return String.format(Locale.US, "%.1f%%", v * 100.0);
    }
}
