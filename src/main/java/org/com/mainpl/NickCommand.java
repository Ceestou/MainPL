package org.com.mainpl;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public class NickCommand implements CommandExecutor {

    private final MainPL plugin;
    private final LuckPerms luckPerms;

    public NickCommand(MainPL plugin) {
        this.plugin = plugin;
        this.luckPerms = plugin.getLuckPerms();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar esse comando.");
            return true;
        }

        String group = plugin.getPrimaryGroup(player).toLowerCase();
        if (!isGrupoPermitido(player, group)) {
            player.sendMessage(ChatColor.RED + "Seu grupo não pode usar este comando.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Uso correto: /nick <novoNick>");
            return true;
        }

        String novoNick = ChatColor.translateAlternateColorCodes('&', args[0]);
        String semCor = ChatColor.stripColor(novoNick);

        if (semCor.length() > 16) {
            player.sendMessage(ChatColor.RED + "O nick não pode ter mais que 16 caracteres (sem contar cores).");
            return true;
        }

        setPlayerNick(player, novoNick);
        player.sendMessage(ChatColor.GREEN + "Seu nick foi alterado para: " + novoNick);
        return true;
    }

    private boolean isGrupoPermitido(Player player, String grupo) {

        if (player.hasPermission("mainpl.comandos.*") || player.hasPermission("mainpl.comandos.nick")) {
            return true;
        }


        var config = plugin.getConfig();
        List<String> gruposPermitidos = config.getStringList("nick.grupos");
        return gruposPermitidos.stream().anyMatch(g -> g.equalsIgnoreCase(grupo));
    }


    private void setPlayerNick(Player player, String nick) {
        player.setDisplayName(ChatColor.RESET + nick);
        player.setPlayerListName(ChatColor.RESET + nick);
        plugin.getNickManager().salvarNick(player.getName(), nick);
    }
}
