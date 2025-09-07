package org.com.mainpl;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ConfiarCommand implements CommandExecutor {

    private final MainPL plugin;

    public ConfiarCommand(MainPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Use: /confiar <jogador>");
            return true;
        }

        String targetName = args[0];
        if (plugin.getBauManager().addConfiavel(player.getName(), targetName)) {
            player.sendMessage(ChatColor.GREEN + "Você confiou seu baú a " + targetName);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        } else {
            player.sendMessage(ChatColor.RED + "Não foi possível adicionar. Limite atingido ou já está na lista.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
        return true;
    }
}
