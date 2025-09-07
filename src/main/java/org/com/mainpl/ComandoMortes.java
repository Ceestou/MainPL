package org.com.mainpl;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ComandoMortes implements CommandExecutor {

    private final MortesManager mortesManager;
    private final MessageManager messages;
    private final MainPL plugin;

    public ComandoMortes(MortesManager mortesManager, MessageManager messages, MainPL plugin) {
        this.mortesManager = mortesManager;
        this.messages = messages;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(messages.getMessage("mortes_somente_jogadores"));
            return true;
        }

        List<String> mortes = mortesManager.getMortes(p);


        if (mortes.isEmpty()) {
            p.sendMessage(messages.getMessage("mortes_nenhuma"));
            return true;
        }

        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(p.getUniqueId());
        String grupo = (user != null) ? user.getPrimaryGroup() : "";

        String path = "mortes.back." + grupo.toLowerCase() + ".limit_mortes_list";
        int limite = plugin.getConfig().getInt(path, 1);

        p.sendMessage(messages.getMessage("mortes_titulo"));

        for (int i = 0; i < Math.min(limite, mortes.size()); i++) {
            p.sendMessage(messages.getMessage("mortes_item", "local", mortes.get(i)));
        }

        return true;
    }
}
