package org.com.mainpl;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ComandosManager {

    private final File file;
    private YamlConfiguration config;

    public ComandosManager(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "Comandos.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        this.config = YamlConfiguration.loadConfiguration(file);

        if (!file.exists()) {
            try {
                file.createNewFile();
                String[][] comandosPadrao = {
                        {"receitas", "", "crafts"},
                        {"dargemas", "", "gemas"},
                        {"addvip", "", "darvip"},
                        {"loja", "", "shop"},
                        {"nick", "", "apelido"},
                        {"restaurarnick", "", "removnick"},
                        {"rank", "", "ranking"},
                        {"mortes", "", "death"},
                        {"decentreload", "", "reloadconfig"},
                        {"sethome", "", ""},
                        {"home", "", ""},
                        {"homes", "", ""},
                        {"delhome", "", ""},
                        {"rtp", "", ""},
                        {"tpa", "", ""},
                        {"tpahere", "", ""},
                        {"sbau", "", ""},
                        {"escolher", "", ""},
                        {"remover", "", ""},
                        {"setjoinmsg", "", ""},
                        {"confiar", "", ""},
                        {"acessar", "", ""},
                        {"anunciar", "", ""},
                        {"warp", "", ""},
                        {"delwarp", "", ""},
                        {"setwarp", "", ""},
                        {"warps", "", ""},
                        {"especial", "", ""},
                        {"spawn", "", ""},
                        {"setspawn", "", ""},
                        {"back", "", ""},
                        {"feed", "fd", ""},
                        {"heal", "hl", ""},
                        {"gm", "gm1", "gm2"},
                        {"tphere", "tph", ""},
                        {"fly", "", ""},
                        {"speed", "spd", ""},
                        {"clearchat", "cc", ""},
                        {"craft", "", ""},
                        {"lixo", "", ""},
                        {"fornalha", "", ""},
                        {"hat", "", ""},
                        {"invsee", "", ""},
                        {"skull", "", ""},
                        {"jail", "", ""},
                        {"enchant", "", ""},
                        {"give", "", ""},
                        {"giveall", "", ""},
                        {"exp", "", ""},
                        {"level", "", ""},
                        {"up", "", ""},
                        {"god", "", ""},
                        {"rename", "", ""},
                        {"kickall", "", ""},
                        {"lightning", "", ""},
                        {"lightningall", "", ""},
                        {"list", "", ""},
                        {"repair", "", ""},
                        {"repairall", "", ""},
                        {"ban", "", ""},
                        {"unban", "", ""},
                        {"tempban", "", ""},
                        {"tempbanip", "", ""},
                        {"top", "", ""},
                        {"luz", "", ""},
                        {"freeze", "", ""},
                        {"afk", "", ""},
                        {"ajuda", "", ""},
                        {"anvil", "", ""},
                        {"rep", "", ""},
                        {"repinfo", "", ""},
                        {"atribuir", "", ""},
                        {"auth", "", ""},
                        {"migrar", "", ""},
                        {"migrarwarp", "", ""}
                };

                for (String[] cmd : comandosPadrao) {
                    if (!cmd[2].isEmpty()) {
                        config.set(cmd[0] + ".aliases", Collections.singletonList(cmd[2]));
                    } else {
                        config.set(cmd[0] + ".aliases", Collections.emptyList());
                    }
                }

                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setComandosConfig(YamlConfiguration newConfig) {
        config = newConfig;
    }

    public List<String> getAliases(String comando) {
        return config.getStringList(comando + ".aliases");
    }

    public void setAliases(String comando, List<String> aliases) {
        config.set(comando + ".aliases", aliases);
        save();
    }

    public List<String> getMensagens(String comando, String chave) {
        return config.getStringList(comando + ".mensagens." + chave);
    }

    public void setMensagens(String comando, String chave, List<String> mensagens) {
        config.set(comando + ".mensagens." + chave, mensagens);
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
