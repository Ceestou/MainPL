package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommandSpawn implements CommandExecutor {

    private final MainPL plugin;
    private final MessageManager messages;
    private final File spawnFile;
    private final FileConfiguration spawnConfig;

    public CommandSpawn(MainPL plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;

        this.spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        if (!spawnFile.exists()) {
            try {
                spawnFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getMessage("somente_jogadores"));
            return true;
        }

        String cmdName = command.getName().toLowerCase();
        String grupo = plugin.getPrimaryGroup(player);

        switch (cmdName) {


            case "setspawn":
                if (!plugin.getConfig().getStringList("spawn.setspawn").contains(grupo)) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                Location loc = player.getLocation();
                spawnConfig.set("spawn.world", loc.getWorld().getName());
                spawnConfig.set("spawn.x", loc.getX());
                spawnConfig.set("spawn.y", loc.getY());
                spawnConfig.set("spawn.z", loc.getZ());
                spawnConfig.set("spawn.yaw", loc.getYaw());
                spawnConfig.set("spawn.pitch", loc.getPitch());

                salvar();
                player.sendMessage(messages.getMessage("setspawn_sucesso"));
                return true;


            case "spawn":
                if (!spawnConfig.contains("spawn")) {
                    player.sendMessage(messages.getMessage("spawn_nao_definido"));
                    return true;
                }

                World world = Bukkit.getWorld(spawnConfig.getString("spawn.world"));
                if (world == null) {
                    player.sendMessage(messages.getMessage("spawn_mundo_inexistente"));
                    return true;
                }

                Location spawnLoc = new Location(
                        world,
                        spawnConfig.getDouble("spawn.x"),
                        spawnConfig.getDouble("spawn.y"),
                        spawnConfig.getDouble("spawn.z"),
                        (float) spawnConfig.getDouble("spawn.yaw"),
                        (float) spawnConfig.getDouble("spawn.pitch")
                );


                long now = System.currentTimeMillis();
                plugin.cooldowns.putIfAbsent(player.getUniqueId(), new HashMap<>());
                Map<String, Long> playerCooldowns = plugin.cooldowns.get(player.getUniqueId());


                String delayStr = plugin.getConfig().getString("spawn.delays." + grupo,
                        plugin.getConfig().getString("spawn.delays.default_delay", "5s"));
                long delay = parseTime(delayStr);

                String cooldownStr = plugin.getConfig().getString("spawn.cooldowns." + grupo,
                        plugin.getConfig().getString("spawn.cooldowns.default_cooldown", "10s"));
                long cooldown = parseTime(cooldownStr);


                if (playerCooldowns.containsKey("spawn")) {
                    long nextUse = playerCooldowns.get("spawn");
                    if (now < nextUse) {
                        long remaining = (nextUse - now) / 1000;
                        player.sendMessage(messages.getMessage("cooldown_comando", "seconds", String.valueOf(remaining)));
                        return true;
                    }
                }


                playerCooldowns.put("spawn", now + cooldown);


                new BukkitRunnable() {
                    long segundos = delay / 1000;

                    @Override
                    public void run() {
                        if (segundos <= 0) {
                            player.teleport(spawnLoc);
                            player.sendMessage(messages.getMessage("warp_teleport", "warp", "spawn"));
                            cancel();
                            return;
                        }

                        player.sendMessage(messages.getMessage("comando_delay", "seconds", String.valueOf(segundos)));
                        segundos--;
                    }
                }.runTaskTimer(plugin, 0L, 20L);

                return true;
        }

        return false;
    }
    private long parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return 0L;

        timeStr = timeStr.toLowerCase().trim();
        long multiplier = 1000L;

        if (timeStr.endsWith("s")) multiplier = 1000L;
        else if (timeStr.endsWith("m")) multiplier = 60 * 1000L;
        else if (timeStr.endsWith("h")) multiplier = 60 * 60 * 1000L;
        else if (timeStr.endsWith("d")) multiplier = 24 * 60 * 60 * 1000L;

        try {
            long value = Long.parseLong(timeStr.replaceAll("[^0-9]", ""));
            return value * multiplier;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public FileConfiguration getSpawnConfig() {
        return this.spawnConfig;
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.getWorld().equals(loc2.getWorld())
                && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    private void salvar() {
        try {
            spawnConfig.save(spawnFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
