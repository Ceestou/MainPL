package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AcessarCommand implements CommandExecutor {

    private final MainPL plugin;

    public AcessarCommand(MainPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Use: /acessar <nickApoiador>");
            return true;
        }

        String dono = args[0];




        if (!plugin.getBauManager().isConfiavel(dono, player.getName())) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para acessar o baú de " + dono);
            return true;
        }

        if (!plugin.getBauManager().isApoiador(dono)) {
            player.sendMessage(ChatColor.RED + "Este jogador não é mais apoiador. Você não pode acessar o baú dele.");
            return true;
        }
        Inventory bau = plugin.getBauManager().getBau(dono);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getOpenInventory() != null) {
                String title = ChatColor.stripColor(online.getOpenInventory().getTitle());
                if (title != null && title.equals("Baú de " + dono) && !online.getName().equals(player.getName())) {
                    online.closeInventory();
                    online.sendMessage(ChatColor.RED + "O baú foi acessado por outro jogador.");
                }
            }
        }


        player.openInventory(bau);
        return true;
    }

}
