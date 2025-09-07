package org.com.mainpl;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class RTP implements CommandExecutor, Listener {

    private final MainPL plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final MessageManager mm;

    public RTP(MainPL plugin) {
        this.plugin = plugin;
        this.mm = new MessageManager(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            teleportRandomlyWithDelay(player);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(mm.getMessage("rtp_somente_jogadores"));
            return true;
        }

        Player player = (Player) sender;
        String group = plugin.getPrimaryGroup(player).toLowerCase();


        long cooldownTempo = getCooldownFromConfig(group);
        if (cooldownTempo > 0) {
            long now = System.currentTimeMillis() / 1000;
            if (cooldowns.containsKey(player.getUniqueId())) {
                long lastUse = cooldowns.get(player.getUniqueId());
                long remaining = (lastUse + cooldownTempo) - now;

                if (remaining > 0) {
                    long minutes = remaining / 60;
                    long seconds = remaining % 60;
                    player.sendMessage(mm.getMessage("rtp_cooldown",
                            "minutes", String.valueOf(minutes),
                            "seconds", String.valueOf(seconds)));
                    return true;
                }
            }
            cooldowns.put(player.getUniqueId(), now);
        }

        teleportRandomlyWithDelay(player);
        return true;
    }

    private long getCooldownFromConfig(String group) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        if (cfg.isConfigurationSection("rtp") && cfg.isConfigurationSection("rtp.cooldown")) {
            if (cfg.getConfigurationSection("rtp.cooldown").getKeys(false).contains(group)) {
                String value = cfg.getString("rtp.cooldown." + group);
                return parseTime(value);
            }
        }

        return 30 * 60;
    }

    private int getDelayFromConfig(String group) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        if (cfg.isConfigurationSection("rtp") && cfg.isConfigurationSection("rtp.delay")) {
            if (cfg.getConfigurationSection("rtp.delay").getKeys(false).contains(group)) {
                String value = cfg.getString("rtp.delay." + group);
                return (int) parseTime(value);
            }
        }

        return 5;
    }


    private long parseTime(String timeStr) {
        try {
            timeStr = timeStr.toLowerCase().trim();
            if (timeStr.endsWith("s")) {
                return Long.parseLong(timeStr.replace("s", ""));
            } else if (timeStr.endsWith("m")) {
                return Long.parseLong(timeStr.replace("m", "")) * 60;
            } else if (timeStr.endsWith("h")) {
                return Long.parseLong(timeStr.replace("h", "")) * 3600;
            } else {
                return Long.parseLong(timeStr);
            }
        } catch (Exception e) {
            return 5;
        }
    }

    public void teleportRandomlyWithDelay(Player player) {
        String group = plugin.getPrimaryGroup(player).toLowerCase();
        int delaySeconds = getDelayFromConfig(group);

        player.sendMessage(mm.getMessage("rtp_teleportando", "seconds", String.valueOf(delaySeconds)));

        Location initialLoc = player.getLocation();

        new BukkitRunnable() {
            int countdown = delaySeconds;

            @Override
            public void run() {
                if (!isSameBlock(initialLoc, player.getLocation())) {
                    player.sendMessage(mm.getMessage("rtp_cancelado_movimento"));
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    Location targetLoc = findSafeRandomLocation(player);
                    if (targetLoc == null) {
                        player.sendMessage(mm.getMessage("rtp_falha"));
                    } else {
                        player.teleport(targetLoc);
                        player.sendMessage(mm.getMessage("rtp_sucesso"));
                    }
                    cancel();
                    return;
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.getWorld().equals(loc2.getWorld())
                && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    private Location findSafeRandomLocation(Player player) {
        World world = player.getWorld();
        WorldBorder border = world.getWorldBorder();
        double borderSize = border.getSize() / 2;
        Location borderCenter = border.getCenter();

        int maxAttempts = 200;
        int minRadius = 100;
        int minHeight = 80;

        for (int i = 0; i < maxAttempts; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = minRadius + (random.nextDouble() * borderSize);

            int x = (int) (borderCenter.getX() + Math.cos(angle) * distance);
            int z = (int) (borderCenter.getZ() + Math.sin(angle) * distance);

            int y = world.getHighestBlockYAt(x, z);
            if (y < minHeight) continue;
            y += 1;

            Location loc = new Location(world, x + 0.5, y, z + 0.5);

            if (!isLocationSafe(loc)) continue;
            if (!hasSufficientSpace(loc)) continue;

            return loc;
        }

        return null;
    }

    private boolean hasSufficientSpace(Location loc) {
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                    if (!block.getType().isAir() && !block.isPassable()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean isLocationSafe(Location loc) {
        World world = loc.getWorld();
        Block blockUnder = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        Block blockAt = world.getBlockAt(loc);
        Block blockAbove = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());

        if (!blockUnder.getType().isSolid()) return false;
        if (blockUnder.isLiquid()) return false;
        if (!blockAt.getType().isAir() && !blockAt.isPassable()) return false;
        if (!blockAbove.getType().isAir() && !blockAbove.isPassable()) return false;

        return true;
    }
}
