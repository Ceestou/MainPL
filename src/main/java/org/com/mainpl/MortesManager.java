package org.com.mainpl;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MortesManager {

    private final File file;
    private final YamlConfiguration config;
    private final MainPL plugin;

    public MortesManager(MainPL plugin) {
        this.plugin = plugin;

        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.file = new File(dataFolder, "mortes.yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("mortes.yml", false);
            } catch (IllegalArgumentException e) {
                try { file.createNewFile(); } catch (IOException ex) { ex.printStackTrace(); }
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }


    public void salvarMorte(Player player, Location loc) {
        String jogador = player.getName();
        String group = plugin.getPrimaryGroup(player);
        int limite = plugin.getConfig().getInt("mortes.limites." + group.toLowerCase(), 1);

        List<String> mortes = config.getStringList(jogador);

        String local = String.format("Mundo: %s, X: %.1f, Y: %.1f, Z: %.1f",
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());

        mortes.add(0, local);

        if (mortes.size() > limite) mortes = mortes.subList(0, limite);

        config.set(jogador, mortes);
        salvarArquivo();
    }


    public List<String> getMortes(Player player) {
        return config.getStringList(player.getName());
    }

    private void salvarArquivo() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
