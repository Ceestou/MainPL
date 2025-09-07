package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class RepCommand implements CommandExecutor {
    private final MainPL plugin;
    private final RepManager repManager;
    private final Map<UUID, Integer> dailyReps = new HashMap<>();

    public RepCommand(MainPL plugin, RepManager repManager) {
        this.plugin = plugin;
        this.repManager = repManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§eUso correto: /rep <staffer>");
            return true;
        }


        String inputName = args[0];
        Player target = Bukkit.getPlayerExact(inputName);


        if (target == null && !inputName.startsWith(".")) {
            String bedrockName = "." + inputName;
            target = Bukkit.getPlayerExact(bedrockName);
        }

        if (target == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return true;
        }

        if (target == null || !target.hasPermission("mainpl.staff")) {
            player.sendMessage("§cEsse jogador não é um staffer válido.");
            return true;
        }

        if (target.getName().equalsIgnoreCase(player.getName())) {
            player.sendMessage("§cVocê não pode dar rep para si mesmo!");
            return true;
        }

        int maxDaily = plugin.getConfig().getInt("rep.max-daily", 3);
        int used = dailyReps.getOrDefault(player.getUniqueId(), 0);

        if (used >= maxDaily) {
            player.sendMessage("§cVocê já usou seu limite diário de reputações (" + maxDaily + ").");
            return true;
        }

        repManager.addRep(target.getName(), player.getName());
        dailyReps.put(player.getUniqueId(), used + 1);

        player.sendMessage("§aVocê deu 1 ponto de reputação para " + target.getName() + "!");
        target.sendMessage("§eVocê recebeu 1 ponto de reputação de " + player.getName() + "! Agora tem §a" + repManager.getReps(target.getName()) + "§e reps.");

        return true;
    }
}
