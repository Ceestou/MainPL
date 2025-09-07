package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PendenciarManager implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final MessageManager messages;

    private final File pendenciasFile;
    private final YamlConfiguration pendenciasConfig;

    private final File pendentesCmdFile;
    private YamlConfiguration pendentesCmdConfig;

    public PendenciarManager(JavaPlugin plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;



        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        pendenciasFile = new File(dataFolder, "pendencias.yml");
        if (!pendenciasFile.exists()) {
            try { pendenciasFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        pendenciasConfig = YamlConfiguration.loadConfiguration(pendenciasFile);


        pendentesCmdFile = new File(plugin.getDataFolder(), "pendentescmd.yml");
        if (!pendentesCmdFile.exists()) {
            try { pendentesCmdFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        pendentesCmdConfig = YamlConfiguration.loadConfiguration(pendentesCmdFile);


        if (!pendentesCmdConfig.contains("pedenciar.1")) {
            carregarPadraoPendentesCmd();
        }


        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setPendenteConfig(YamlConfiguration newConfig) {
        pendentesCmdConfig = newConfig;
    }

    private void carregarPadraoPendentesCmd() {
        pendentesCmdConfig.set("pedenciar.1", List.of(
                "darvip {player} apoiador 30",
                "fly {player}"
        ));
        pendentesCmdConfig.set("pedenciar.2", List.of(
                "msg {player} Bem-vindo de volta!"
        ));
        try {
            pendentesCmdConfig.save(pendentesCmdFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(messages.getMessage("pendenciar_sem_permissao"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messages.getMessage("pendenciar_uso"));
            return true;
        }

        String lista = args[0];
        String playerName = args[1];

        if (!pendentesCmdConfig.contains("pedenciar." + lista)) {
            sender.sendMessage(messages.getMessage("pendenciar_lista_inexistente"));
            return true;
        }

        List<String> comandosLista = pendentesCmdConfig.getStringList("pedenciar." + lista);


        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.isOnline()) {

            for (String cmdStr : comandosLista) {
                cmdStr = cmdStr.replace("{player}", target.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdStr);
            }
            target.sendMessage(messages.getMessage("pendenciar_executado"));
        } else {

            List<String> comandosPendentes = pendenciasConfig.getStringList(playerName);
            comandosPendentes.addAll(comandosLista);
            pendenciasConfig.set(playerName, comandosPendentes);
            salvarPendencias();
        }


        String confirm = messages.getMessage("pendenciar_aplicado")
                .replace("{lista}", lista)
                .replace("{player}", playerName);
        sender.sendMessage(confirm);

        return true;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String name = p.getName();

        if (!pendenciasConfig.contains(name)) return;

        List<String> comandos = new ArrayList<>(pendenciasConfig.getStringList(name));
        for (String cmd : comandos) {
            cmd = cmd.replace("{player}", name);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }


        pendenciasConfig.set(name, null);
        salvarPendencias();

        p.sendMessage(messages.getMessage("pendenciar_executado"));
    }

    private void salvarPendencias() {
        try {
            pendenciasConfig.save(pendenciasFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
