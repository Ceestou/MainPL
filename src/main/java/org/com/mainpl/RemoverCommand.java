package org.com.mainpl;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoverCommand implements CommandExecutor {

    private final MainPL plugin;

    public RemoverCommand(MainPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        MessageManager mm = new MessageManager(plugin);

        if (args.length != 1) {
            player.sendMessage(mm.getMessage("remover_uso", "label", label));
            return true;
        }

        String targetName = args[0];
        if (plugin.getBauManager().removerConfiavel(player.getName(), targetName)) {
            player.sendMessage(mm.getMessage("remover_sucesso", "target", targetName));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        } else {
            player.sendMessage(mm.getMessage("remover_falha", "target", targetName));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
        return true;
    }
}
