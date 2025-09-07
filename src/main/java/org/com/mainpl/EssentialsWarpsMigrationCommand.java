package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class EssentialsWarpsMigrationCommand implements CommandExecutor {

    private final MainPL plugin;
    private final File warpsFile;
    private final YamlConfiguration warpsConfig;

    public EssentialsWarpsMigrationCommand(MainPL plugin) {
        this.plugin = plugin;
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!warpsFile.exists()) {
            try { warpsFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.isOp()) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        sender.sendMessage("§aIniciando migração dos warps do Essentials...");

        File essentialsWarpsFolder = new File("plugins/Essentials/warps");
        if (!essentialsWarpsFolder.exists() || !essentialsWarpsFolder.isDirectory()) {
            sender.sendMessage("§cPasta de warps do Essentials não encontrada!");
            return true;
        }

        int migratedCount = 0;

        for (File warpFile : essentialsWarpsFolder.listFiles()) {
            if (!warpFile.getName().endsWith(".yml")) continue;

            YamlConfiguration warpYaml = YamlConfiguration.loadConfiguration(warpFile);
            String warpName = warpFile.getName().replace(".yml", "");

            if (!warpYaml.contains("world")) continue;

            String worldUUID = warpYaml.getString("world");
            String worldName = warpYaml.getString("world-name", worldUUID);

            double x = warpYaml.getDouble("x");
            double y = warpYaml.getDouble("y");
            double z = warpYaml.getDouble("z");
            float yaw = (float) warpYaml.getDouble("yaw");
            float pitch = (float) warpYaml.getDouble("pitch");

            String guiNome = "&e" + warpName;
            String guiLore = "&7Clique para ir até " + warpName;


            String path = "warps." + warpName;
            warpsConfig.set(path + ".world", worldUUID);
            warpsConfig.set(path + ".world-name", worldName);
            warpsConfig.set(path + ".x", x);
            warpsConfig.set(path + ".y", y);
            warpsConfig.set(path + ".z", z);
            warpsConfig.set(path + ".yaw", yaw);
            warpsConfig.set(path + ".pitch", pitch);


            warpsConfig.set(path + ".slot", -1);
            warpsConfig.set(path + ".item", "STONE");
            warpsConfig.set(path + ".nome", guiNome);
            warpsConfig.set(path + ".lore", java.util.Collections.singletonList(guiLore));
            warpsConfig.set(path + ".tipoWarp", "");

            migratedCount++;
        }

        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sender.sendMessage("§aMigração concluída! Warps migrados: " + migratedCount);
        return true;
    }
}
