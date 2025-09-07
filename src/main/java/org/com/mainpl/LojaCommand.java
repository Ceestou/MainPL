package org.com.mainpl;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LojaCommand implements CommandExecutor {

    private final MainPL plugin;
    private final MessageManager messages;

    public LojaCommand(MainPL plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getMessage("somente_jogadores"));
            return true;
        }


        player.sendMessage(messages.getMessage("loja_mensagem"));


        String lojaUrl = messages.getMessage("loja_url");


        TextComponent clickMessage = new TextComponent(messages.getMessage("loja_click"));
        clickMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, lojaUrl));

        player.spigot().sendMessage(clickMessage);
        return true;
    }
}
