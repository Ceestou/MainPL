package org.com.mainpl;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AjudaManager {

    private final File file;
    private YamlConfiguration config;
    private final JavaPlugin plugin;



    public AjudaManager(MainPL plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ajuda.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                config = YamlConfiguration.loadConfiguration(file);
                setDefaults();
                config.save(file);
            } catch (IOException e) {
                throw new RuntimeException("Não foi possível criar ajuda.yml", e);
            }
        } else {
            config = YamlConfiguration.loadConfiguration(file);
        }
    }
    public void setAjudaConfig(YamlConfiguration newConfig) {
        config = newConfig;
    }



    private void setDefaults() {
        config.addDefault("title", "&6&lAjuda");
        config.addDefault("lines_per_page", 7);

        config.addDefault("pages.1", List.of(
                "&aBem-vindo ao servidor!",
                "&aUse /ajuda <pagina> para navegar nas páginas",
                "&aPágina de exemplo 1"
        ));


        config.options().copyDefaults(true);
    }

    public String getTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("title", "&6&lAjuda"));
    }

    public int getLinesPerPage() {
        return config.getInt("lines_per_page", 7);
    }

    public List<String> getPage(int page) {
        if (!config.contains("pages." + page)) return Collections.emptyList();
        List<String> lines = config.getStringList("pages." + page);
        List<String> colored = new ArrayList<>();
        for (String line : lines) {
            colored.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return colored;
    }

    public int getMaxPage() {
        if (!config.contains("pages")) return 1;
        return config.getConfigurationSection("pages").getKeys(false).size();
    }
}
