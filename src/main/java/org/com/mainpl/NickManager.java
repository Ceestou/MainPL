package org.com.mainpl;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class NickManager {

    private final File file;
    private final YamlConfiguration config;

    public NickManager(MainPL plugin) {
        File pasta = new File(plugin.getDataFolder(), "Data");
        if (!pasta.exists()) pasta.mkdirs();

        file = new File(pasta, "apelidos.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void salvarNick(String nomeReal, String apelido) {
        config.set(nomeReal, apelido);
        salvar();
    }

    public void removerNick(String nomeReal) {
        config.set(nomeReal, null);
        salvar();
    }

    public String getApelido(String nomeReal) {
        return config.getString(nomeReal);
    }

    public boolean temApelido(String nomeReal) {
        return config.contains(nomeReal);
    }

    public void aplicarNick(Player player) {
        String apelido = getApelido(player.getName());
        if (apelido != null) {
            player.setDisplayName(ChatColor.RESET + apelido);
            player.setPlayerListName(ChatColor.RESET + apelido);
        }
    }

    private void salvar() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
