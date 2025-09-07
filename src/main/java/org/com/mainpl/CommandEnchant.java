package org.com.mainpl;

import com.sun.tools.javac.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommandEnchant implements CommandExecutor {

    private final MainPL plugin;
    private final EnchantManagerCompleto manager;


    public CommandEnchant(MainPL plugin, EnchantManagerCompleto manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] raw) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }
        Player p = (Player) sender;
        String grupo = plugin.getPrimaryGroup(p).toLowerCase();
        var config = plugin.getConfig();
        List<String> gruposPermitidos = config.getStringList("enchant.grupos");

        if (!(p.hasPermission("mainpl.comandos.*") || p.hasPermission("mainpl.comandos.enchant")
                || gruposPermitidos.stream().anyMatch(g -> g.equalsIgnoreCase(grupo)))) {
            p.sendMessage(color(config.getString("messages.sem_permissao", "&cVocê não tem permissão.")));
            return true;
        }

        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType().isAir()) {
            p.sendMessage(color(plugin.getConfig().getString("messages.nenhum_item_mao", "&cSegure um item na mão.")));
            return true;
        }

        if (raw.length == 0) {
            p.sendMessage(color("&cUso: /enchant <alias nivel>[, alias nivel]..."));
            p.sendMessage(color("&7Ex.: /enchant af 800, aspc 1000"));
            return true;
        }

        String joined = String.join(" ", raw).replaceAll(",", " ");
        String[] toks = joined.trim().split("\\s+");

        if (toks.length % 2 != 0) {
            p.sendMessage(color("&cParâmetros inválidos. Use pares: <alias nivel>."));
            return true;
        }

        List<String> applied = new ArrayList<>();
        int max = plugin.getMaxLevel();

        for (int i = 0; i < toks.length; i += 2) {
            String alias = toks[i];
            String lvlStr = toks[i + 1];

            Enchantment ench = plugin.getAliases().fromAlias(alias);
            if (ench == null) {
                p.sendMessage("§cEncantamento inválido: " + alias);
                p.sendMessage(plugin.getEnchantmentsList());
                continue;
            }

            int level;
            try { level = Integer.parseInt(lvlStr); } catch (Exception e) {
                p.sendMessage(color("&cNível inválido para &f" + alias + "&c: &f" + lvlStr));
                continue;
            }

            double ratio = Math.max(0d, Math.min(1d, (double) level / (double) max));

            EnchantUtil.setRatio(inHand, ench, ratio);
            applied.add(ench.getKey().getKey() + " " + (int)Math.round(ratio * max));
        }

        if (!applied.isEmpty()) {
            manager.updateItemLore(inHand);
            p.sendMessage(color(plugin.getConfig().getString("messages.enchant_sucesso",
                    "&aEncantamentos aplicados: ")) + String.join(", ", applied));
        }
        return true;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
