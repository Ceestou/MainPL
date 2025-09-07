package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Map;

public class RepInfoCommand implements CommandExecutor {
    private final RepManager repManager;

    public RepInfoCommand(RepManager repManager) {
        this.repManager = repManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String targetName;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cUso correto: /repinfo <nick>");
                return true;
            }
            targetName = player.getName();
        } else {
            targetName = args[0];
        }

        int reps = repManager.getReps(targetName);
        sender.sendMessage("§aReputação de §e" + targetName + "§a: " + reps);

        Map<String, Integer> given = repManager.getGiven(targetName);
        if (given.isEmpty()) {
            sender.sendMessage("§7Ninguém deu rep ainda.");
        } else {
            sender.sendMessage("§7Quem já deu rep:");
            for (Map.Entry<String, Integer> entry : given.entrySet()) {
                sender.sendMessage(" §f- " + entry.getKey() + ": " + entry.getValue());
            }
        }

        return true;
    }
}
