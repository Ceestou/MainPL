package org.com.mainpl;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MensagensApoiadores {

    private final File mensagensFile;
    private final YamlConfiguration mensagensConfig;

    public MensagensApoiadores(MainPL plugin) {
        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.mensagensFile = new File(dataFolder, "mensagensapoiadores.yml");
        if (!mensagensFile.exists()) {
            try {
                plugin.saveResource("mensagensapoiadores.yml", false); // tenta salvar do JAR
            } catch (IllegalArgumentException e) {
                try {
                    mensagensFile.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        this.mensagensConfig = YamlConfiguration.loadConfiguration(mensagensFile);

    }


    public void setJoinMessage(String playerName, String message) {

        List<String> mensagens = Arrays.asList(message.split("\n"));

        mensagensConfig.set("joinMessages." + playerName, mensagens);

        try {
            mensagensConfig.save(mensagensFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public List<String> getJoinMessage(String playerName) {
        return mensagensConfig.getStringList("joinMessages." + playerName);
    }
}
