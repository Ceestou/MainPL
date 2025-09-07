package org.com.mainpl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ComandoEscolherDisplay implements CommandExecutor {

    private final MainPL plugin;

    public ComandoEscolherDisplay(MainPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!plugin.getRankingManager().estaNoTop7(player)) {
            player.sendMessage("§cVocê precisa estar no TOP 7 para usar esse comando.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§eUse /escolher nick §7ou §e/escolher tag");
            return true;
        }

        String escolha = args[0].toLowerCase();
        if (!escolha.equals("nick") && !escolha.equals("tag")) {
            player.sendMessage("§cEscolha inválida. Use §e/escolher nick §cou §e/escolher tag");
            return true;
        }

        plugin.getEscolhaDisplayManager().setEscolha(player, escolha);
        player.sendMessage("§aVocê escolheu usar: " + escolha.toUpperCase());

        plugin.aplicarDisplayName(player);

        return true;
    }
}
