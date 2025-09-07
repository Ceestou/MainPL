package org.com.mainpl;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ApoioMessagesManager {

    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public ApoioMessagesManager(JavaPlugin plugin) {
        this.plugin = plugin;
        createConfig();
    }

    private void createConfig() {

        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }


        configFile = new File(dataFolder, "mensagensapoiadores.yml");


        if (!configFile.exists()) {
            try {
                plugin.saveResource("mensagensapoiadores.yml", false);
            } catch (IllegalArgumentException e) {
                try {
                    configFile.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }


        config = YamlConfiguration.loadConfiguration(configFile);
    }


    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar mensagensapoiadores.yml");
            e.printStackTrace();
        }
    }

    public void setJoinMessage(String playerName, String message) {
        config.set("joinMessages." + playerName, message);
        saveConfig();
    }

    public String getJoinMessage(String playerName) {
        return config.getString("joinMessages." + playerName);
    }
}
