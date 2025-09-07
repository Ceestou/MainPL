package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BackCommand implements CommandExecutor {

    private final MainPL plugin;
    private final MessageManager messages;
    private final MortesManager mortesManager;


    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public BackCommand(MainPL plugin, MessageManager messages, MortesManager mortesManager) {
        this.plugin = plugin;
        this.messages = messages;
        this.mortesManager = mortesManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getMessage("somente_jogadores"));
            return true;
        }

        UUID uuid = player.getUniqueId();
        String grupo = plugin.getPrimaryGroup(player).toLowerCase();


        if (!plugin.getConfig().contains("mortes.back." + grupo)) {
            player.sendMessage(messages.getMessage("back_sem_permissao"));
            return true;
        }


        int delay = plugin.getConfig().getInt("mortes.back." + grupo + ".back_delay_seconds", 4);
        int cooldownSeconds = plugin.getConfig().getInt("mortes.back." + grupo + ".back_cooldown_seconds", 30);


        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid)) {
            long nextUse = cooldowns.get(uuid);
            if (now < nextUse) {
                long remaining = (nextUse - now) / 1000;
                player.sendMessage(messages.getMessage("back_cooldown", "seconds", String.valueOf(remaining)));
                return true;
            }
        }

        String jogador = player.getName();
        List<String> mortes = mortesManager.getMortes(player);


        if (mortes.isEmpty()) {
            player.sendMessage(messages.getMessage("mortes_nenhuma"));
            return true;
        }

        int index = 0;
        if (args.length > 0) {
            try {
                index = Integer.parseInt(args[0]) - 1;
            } catch (NumberFormatException e) {
                player.sendMessage(messages.getMessage("back_numero_invalido"));
                return true;
            }
        }

        if (index < 0 || index >= mortes.size()) {
            player.sendMessage(messages.getMessage("back_fora_limite", "limite", String.valueOf(mortes.size())));
            return true;
        }


        String localStr = mortes.get(index);
        String[] partes = localStr.split(", ");
        String worldName = partes[0].split(": ")[1];
        double x = Double.parseDouble(partes[1].split(": ")[1]);
        double y = Double.parseDouble(partes[2].split(": ")[1]);
        double z = Double.parseDouble(partes[3].split(": ")[1]);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(messages.getMessage("spawn_mundo_inexistente"));
            return true;
        }

        Location targetLoc = new Location(world, x, y, z);


        if (delay <= 0) {
            player.teleport(targetLoc);
            player.sendMessage(messages.getMessage("back_teleportado", "pos", String.valueOf(index + 1)));
            cooldowns.put(uuid, System.currentTimeMillis() + cooldownSeconds * 1000L);
            return true;
        }

        player.sendMessage(messages.getMessage("home_teleport_delay", "home", "morte " + (index + 1)));
        Location initialLocation = player.getLocation();

        int finalIndex = index;
        new BukkitRunnable() {
            int countdown = delay;

            @Override
            public void run() {
                Location currentLocation = player.getLocation();
                if (!isSameBlock(initialLocation, currentLocation)) {
                    player.sendMessage(messages.getMessage("home_teleport_cancelado"));
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    player.teleport(targetLoc);
                    player.sendMessage(messages.getMessage("back_teleportado", "pos", String.valueOf(finalIndex + 1)));
                    cooldowns.put(uuid, System.currentTimeMillis() + cooldownSeconds * 1000L);
                    cancel();
                    return;
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 20L, 20L);

        return true;
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.getWorld().equals(loc2.getWorld())
                && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }
}
