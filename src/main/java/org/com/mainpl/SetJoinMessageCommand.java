package org.com.mainpl;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SetJoinMessageCommand implements CommandExecutor {

    private final MainPL plugin;
    private final MensagensApoiadores apoioMessagesManager;
    private final MessageManager messageManager;

    public SetJoinMessageCommand(MainPL plugin, MensagensApoiadores apoioMessagesManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.apoioMessagesManager = apoioMessagesManager;
        this.messageManager = messageManager;
    }

    private boolean canUseSetJoinMsg(Player player) {
        String group = plugin.getPrimaryGroup(player).toLowerCase();
        List<String> allowedGroups = plugin.getConfig().getStringList("setmsgjoin");


        if (player.hasPermission("mainpl.comandos.*") || player.hasPermission("mainpl.comandos.setjoinmsg")) {
            return true;
        }


        return allowedGroups.contains(group);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getMessage("somente_jogadores"));
            return true;
        }

        String group = plugin.getPrimaryGroup(player).toLowerCase();


        if (!plugin.getConfig().contains("setmsgjoin")) {
            player.sendMessage(messageManager.getMessage("nenhum_grupo"));
            return true;
        }

        if (!canUseSetJoinMsg(player)) {
            player.sendMessage(ChatColor.RED + "Seu grupo não tem permissão para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Uso correto: /setjoinmsg <mensagem>");
            player.sendMessage(ChatColor.RED + "Use \\n para pular linha e $player para o nome.");
            return true;
        }

        String rawMessage = String.join(" ", args);


        String messageWithNewLines = rawMessage.replace("\\n", "\n");

        apoioMessagesManager.setJoinMessage(player.getName(), messageWithNewLines);

        player.sendMessage(ChatColor.GREEN + "Sua mensagem de entrada foi definida com sucesso!");


        String preview = messageWithNewLines.replace("$player", player.getName());
        for (String line : preview.split("\n")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }

        return true;
    }
}
