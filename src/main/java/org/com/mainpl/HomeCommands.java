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
import java.util.Set;
import java.util.UUID;

public class HomeCommands implements CommandExecutor {

    private final MainPL plugin;
    private final MessageManager messages;

    public HomeCommands(MainPL plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getMessage("somente_jogadores"));
            return true;
        }

        UUID uuid = player.getUniqueId();
        String uuidPath = "homes." + uuid;


        File homesFile = new File(plugin.getDataFolder(), "Data/homes.yml");
        FileConfiguration homesConfig = YamlConfiguration.loadConfiguration(homesFile);

        if (command.getName().equalsIgnoreCase("homes")) {
            if (!homesConfig.isConfigurationSection(uuidPath)) {
                player.sendMessage(messages.getMessage("homes_sem_homes"));
                return true;
            }

            Set<String> homes = homesConfig.getConfigurationSection(uuidPath).getKeys(false);
            if (homes.isEmpty()) {
                player.sendMessage(messages.getMessage("homes_sem_homes"));
                return true;
            }

            player.sendMessage(messages.getMessage("homes_lista_titulo"));
            for (String homeName : homes) {
                player.sendMessage(messages.getMessage("homes_lista_item", "home", homeName));
            }
            return true;
        }


        if (command.getName().equalsIgnoreCase("delhome")) {
            if (args.length == 0) {
                player.sendMessage(messages.getMessage("delhome_uso"));
                return true;
            }

            String homeName = args[0].toLowerCase();
            String homePath = uuidPath + "." + homeName;

            if (!homesConfig.contains(homePath)) {
                player.sendMessage(messages.getMessage("delhome_nao_existe"));
                return true;
            }

            homesConfig.set(homePath, null);
            try {
                homesConfig.save(homesFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            player.sendMessage(messages.getMessage("delhome_sucesso", "home", homeName));
            return true;
        }


        if (command.getName().equalsIgnoreCase("home")) {
            if (args.length == 0) {
                player.sendMessage(messages.getMessage("home_uso"));
                return true;
            }

            String homeName = args[0].toLowerCase();
            String homePath = uuidPath + "." + homeName;

            if (!homesConfig.contains(homePath)) {
                player.sendMessage(messages.getMessage("home_nao_existe"));
                return true;
            }

            UUID worldUUID = UUID.fromString(homesConfig.getString(homePath + ".world"));
            World world = Bukkit.getWorld(worldUUID);
            if (world == null) {
                player.sendMessage(messages.getMessage("home_mundo_inexistente"));
                return true;
            }

            Location homeLoc = new Location(
                    world,
                    homesConfig.getDouble(homePath + ".x"),
                    homesConfig.getDouble(homePath + ".y"),
                    homesConfig.getDouble(homePath + ".z"),
                    (float) homesConfig.getDouble(homePath + ".yaw"),
                    (float) homesConfig.getDouble(homePath + ".pitch")
            );

            String group = plugin.getPrimaryGroup(player);

            if (group.equalsIgnoreCase("apoiador")) {
                player.teleport(homeLoc);
                player.sendMessage(messages.getMessage("home_teleport_apoiador", "home", homeName));
            } else {
                player.sendMessage(messages.getMessage("home_teleport_delay", "home", homeName));

                Location initialLocation = player.getLocation();

                new BukkitRunnable() {
                    int countdown = 4;

                    @Override
                    public void run() {
                        Location currentLocation = player.getLocation();
                        if (!isSameBlock(initialLocation, currentLocation)) {
                            player.sendMessage(messages.getMessage("home_teleport_cancelado"));
                            cancel();
                            return;
                        }

                        if (countdown <= 0) {
                            player.teleport(homeLoc);
                            player.sendMessage(messages.getMessage("home_teleport_apoiador", "home", homeName));
                            cancel();
                            return;
                        }

                        countdown--;
                    }
                }.runTaskTimer(plugin, 20L, 20L);
            }

            return true;
        }

        return false;
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.getWorld().equals(loc2.getWorld())
                && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }
}
