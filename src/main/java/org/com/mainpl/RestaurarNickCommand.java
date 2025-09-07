package org.com.mainpl;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class RestaurarNickCommand implements CommandExecutor {

    private final MainPL plugin;

    public RestaurarNickCommand(MainPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar esse comando.");
            return true;
        }

        plugin.getNickManager().removerNick(player.getName());
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());

        player.sendMessage(ChatColor.GREEN + "Seu nick foi restaurado para o original.");
        return true;
    }
}
