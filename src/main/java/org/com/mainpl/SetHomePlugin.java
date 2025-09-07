package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class SetHomePlugin implements CommandExecutor, Listener {

    private final MainPL plugin;
    private final MessageManager messageManager;
    private final HashMap<UUID, String> confirmacoes = new HashMap<>();
    private final File homesFile;
    private final FileConfiguration homesConfig;

    public SetHomePlugin(MainPL plugin) {
        this.plugin = plugin;
        this.messageManager = new MessageManager(plugin);

        // Cria arquivo homes.yml dentro da pasta Data
        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.homesFile = new File(dataFolder, "homes.yml");
        if (!homesFile.exists()) {
            try {
                homesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.homesConfig = YamlConfiguration.loadConfiguration(homesFile);


        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    public static File getHomesFile(MainPL plugin) {
        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        return new File(dataFolder, "homes.yml");
    }

    public FileConfiguration getHomesConfig() {
        return homesConfig;
    }
    public File getHomesFile() {
        return homesFile;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getMessage("somente_jogadores"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messageManager.getMessage("sethome_uso", "label", label));
            return true;
        }

        String homeName = args[0].toLowerCase();
        UUID uuid = player.getUniqueId();
        String homePath = "homes." + uuid.toString() + "." + homeName;

        int maxHomes = getMaxHomesForGroup(player);

        if (homesConfig.isConfigurationSection("homes." + uuid.toString())) {
            Set<String> existingHomes = homesConfig.getConfigurationSection("homes." + uuid.toString()).getKeys(false);

            if (!existingHomes.contains(homeName) && existingHomes.size() >= maxHomes) {
                player.sendMessage(messageManager.getMessage("sethome_limite", "max", String.valueOf(maxHomes)));
                return true;
            }

            if (existingHomes.contains(homeName)) {
                if (confirmacoes.containsKey(uuid)) {
                    player.sendMessage(messageManager.getMessage("confirmacao_pendente"));
                    return true;
                }

                player.sendMessage(messageManager.getMessage("sethome_substituir_pergunta"));
                confirmacoes.put(uuid, homeName);
                return true;
            }
        }

        salvarHome(player, homeName, homePath, true);
        return true;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!confirmacoes.containsKey(uuid)) return;

        String resposta = event.getMessage().toLowerCase();
        String homeName = confirmacoes.get(uuid);
        String homePath = "homes." + uuid.toString() + "." + homeName;

        event.setCancelled(true);

        if (resposta.equals("sim") || resposta.equals("s")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                salvarHome(player, homeName, homePath, true);
                player.sendMessage(messageManager.getMessage("sethome_substituida", "home", homeName));
            });
        } else if (resposta.equals("não") || resposta.equals("nao") || resposta.equals("n")) {
            player.sendMessage(messageManager.getMessage("sethome_cancelada"));
        } else {
            player.sendMessage(messageManager.getMessage("resposta_invalida"));
            return;
        }

        confirmacoes.remove(uuid);
    }

    private void salvarHome(Player player, String homeName, String homePath, boolean enviarMensagem) {
        Location loc = player.getLocation();


        YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(homesFile);


        currentConfig.set(homePath + ".world", loc.getWorld().getUID().toString()); // UUID do mundo
        currentConfig.set(homePath + ".world-name", loc.getWorld().getName());     // Nome legível
        currentConfig.set(homePath + ".x", loc.getX());
        currentConfig.set(homePath + ".y", loc.getY());
        currentConfig.set(homePath + ".z", loc.getZ());
        currentConfig.set(homePath + ".yaw", loc.getYaw());
        currentConfig.set(homePath + ".pitch", loc.getPitch());
        currentConfig.set(homePath + ".nick", player.getName());

        try {
            currentConfig.save(homesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (enviarMensagem) {
            player.sendMessage(messageManager.getMessage("sethome_definida", "home", homeName));
        }
    }


    private int getMaxHomesForGroup(Player player) {
        String group = plugin.getPrimaryGroup(player).toLowerCase();
        String path = "maxhomes." + group;

        if (plugin.getConfig().contains(path)) {
            return plugin.getConfig().getInt(path, 4);
        } else {
            return plugin.getConfig().getInt("maxhomes.default", 4);
        }
    }
}
