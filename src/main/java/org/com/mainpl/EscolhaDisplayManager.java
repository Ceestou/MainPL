package org.com.mainpl;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class EscolhaDisplayManager {
    private final MainPL plugin;
    private final File file;
    private final YamlConfiguration config;

    public EscolhaDisplayManager(MainPL plugin) {
        this.plugin = plugin;
        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.file = new File(dataFolder, "escolhas.yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("escolhas.yml", false);
            } catch (IllegalArgumentException e) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);

    }

    public void setEscolha(Player player, String escolha) {
        config.set(player.getUniqueId().toString(), escolha);
        salvar();
    }

    public String getEscolha(Player player) {
        return config.getString(player.getUniqueId().toString(), "tag");
    }

    private void salvar() {
        try {
            config.save(file);
        } catch (IOException ignored) {}
    }
}
