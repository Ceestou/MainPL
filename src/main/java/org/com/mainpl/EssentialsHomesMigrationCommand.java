package org.com.mainpl;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;


public class EssentialsHomesMigrationCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final File homesFile;
    private final YamlConfiguration homesConfig;

    public EssentialsHomesMigrationCommand(JavaPlugin plugin) {
        this.plugin = plugin;


        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();


        homesFile = new File(dataFolder, "homes.yml");
        if (!homesFile.exists()) {
            try {
                homesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.isOp()) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        sender.sendMessage("§aIniciando migração das homes do Essentials...");

        File userdataFolder = new File("plugins/Essentials/userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) {
            sender.sendMessage("§cPasta userdata do Essentials não encontrada!");
            return true;
        }

        int homesCount = 0;
        for (File playerFile : userdataFolder.listFiles()) {
            if (!playerFile.getName().endsWith(".yml")) continue;

            YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            String uuid = playerFile.getName().replace(".yml", "");
            String nick = playerConfig.contains("last-account-name") ?
                    playerConfig.getString("last-account-name") : uuid;

            if (!playerConfig.contains("homes")) continue;

            Map<String, Object> homes = playerConfig.getConfigurationSection("homes").getValues(false);
            for (String homeName : homes.keySet()) {

                if (playerConfig.getConfigurationSection("homes." + homeName) != null) {
                    var homeSection = playerConfig.getConfigurationSection("homes." + homeName);


                    homesConfig.createSection("homes." + uuid + "." + homeName, homeSection.getValues(false));


                    homesConfig.set("homes." + uuid + "." + homeName + ".nick", nick);

                    homesCount++;
                }
            }
        }

        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sender.sendMessage("§aMigração concluída! Homes migradas: " + homesCount);
        return true;
    }
}
