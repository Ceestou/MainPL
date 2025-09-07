package org.com.mainpl;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModulosManager {

    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Boolean> modulos = new HashMap<>();

    public ModulosManager(MainPL plugin) {

        this.file = new File(plugin.getDataFolder(), "modulos.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);


        String[] nomesModulos = {
                "MensagemApoiadores",
                "EscolhaDisplay",
                "CraftingsManager",
                "GemasManager",
                "Pedenciar",
                "Loja",
                "Seguranca",
                "AutoBroadcast",
                "SuperBroadcast",
                "BauManager",
                "NickManager",
                "RankingManager",
                "MortesManager",
                "ConfigReloader",
                "HomeCommands",
                "RTP",
                "Remover",
                "Tpa",
                "PlayerJoinListener",
                "AntBedrockJava",
                "ConfiarSBau",
                "AcessarSBau",
                "Anunciar",
                "Warp",
                "Spawn",
                "Back",
                "Extras",
                "Utils",
                "UltraEnchants",
                "ComandoInvalido",
                "BlockWorlds",
                "RepStaff",
                "AtributosModificados"
        };
        Set<String> desativadosPorPadrao = new HashSet<>();
        desativadosPorPadrao.add("AntBedrockJava");
        desativadosPorPadrao.add("Seguranca");
        desativadosPorPadrao.add("AutoBroadcast");
        desativadosPorPadrao.add("UltraEnchants");
        desativadosPorPadrao.add("AtributosModificados");


        for (String modulo : nomesModulos) {
            boolean valor;
            if (desativadosPorPadrao.contains(modulo)) {
                valor = config.getBoolean(modulo, false);
            } else {
                valor = config.getBoolean(modulo, true);
            }
            modulos.put(modulo, valor);
            config.set(modulo, valor);
        }


        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean isEnabled(String modulo) {
        return modulos.getOrDefault(modulo, false);
    }


    public void setEnabled(String modulo, boolean enabled) {
        modulos.put(modulo, enabled);
        config.set(modulo, enabled);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Map<String, Boolean> getModulos() {
        return modulos;
    }
}
