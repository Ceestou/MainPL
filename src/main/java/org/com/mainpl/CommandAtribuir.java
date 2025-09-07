package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.util.UUID;

public class CommandAtribuir implements CommandExecutor {

    private static final UUID DANO_EXTRA_UUID = UUID.randomUUID();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("mainpl.utils.*") && !player.hasPermission("mainpl.atribuir")) {
            player.sendMessage("§cVocê não tem permissão para criar receitas.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§eUso: /atribuir <atributo> <valor>");
            return true;
        }

        String atributoNome = args[0].toUpperCase();
        double valor;

        try {
            valor = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cValor inválido, use um número.");
            return true;
        }

        Attribute atributo;
        try {
            atributo = Attribute.valueOf(atributoNome);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cAtributo inválido! Use um dos disponíveis em:");
            for (Attribute att : Attribute.values()) {
                player.sendMessage("§7- " + att.name());
            }
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("§cVocê precisa segurar um item na mão.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage("§cEste item não pode ter atributos.");
            return true;
        }

        meta.removeAttributeModifier(atributo);

        AttributeModifier mod;
        try {
            Constructor<AttributeModifier> cons = AttributeModifier.class
                    .getConstructor(UUID.class, String.class, double.class, AttributeModifier.Operation.class);
            mod = cons.newInstance(DANO_EXTRA_UUID, "atributo_custom", valor, AttributeModifier.Operation.ADD_NUMBER);
        } catch (Exception e) {
            Bukkit.getLogger().severe("⚠️ Erro ao criar AttributeModifier: " + e.getMessage());
            return true;
        }

        meta.addAttributeModifier(atributo, mod);
        item.setItemMeta(meta);

        player.sendMessage("§aAtributo §e" + atributo.name() + " §afoi definido para §e" + valor);
        return true;
    }
}
