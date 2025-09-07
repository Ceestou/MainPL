package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BroadcastScheduler {

    private final JavaPlugin plugin;
    private final ConfigurationSection automensagensSection;
    private final long intervaloMillis;

    private int mensagemAtual = 1;

    public BroadcastScheduler(JavaPlugin plugin) {
        this.plugin = plugin;

        if (!plugin.getConfig().contains("automensagens")) {
            plugin.getLogger().warning("Não há configuração de automensagens no config.yml!");
            automensagensSection = null;
            intervaloMillis = 0;
            return;
        }

        automensagensSection = plugin.getConfig().getConfigurationSection("automensagens");

        String intervaloStr = automensagensSection.getString("intervalo", "5m");
        intervaloMillis = parseTimeToMillis(intervaloStr);

        startTask();
    }

    private void startTask() {
        if (automensagensSection == null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                enviarMensagemAtual();
                mensagemAtual++;

                ConfigurationSection mensagensSec = automensagensSection.getConfigurationSection("mensagens");
                if (mensagensSec == null || !mensagensSec.contains(String.valueOf(mensagemAtual))) {
                    mensagemAtual = 1;
                }
            }
        }.runTaskTimer(plugin, intervaloMillis / 50, intervaloMillis / 50); // ticks = ms / 50
    }

    private void enviarMensagemAtual() {
        if (automensagensSection == null) return;

        ConfigurationSection mensagensSec = automensagensSection.getConfigurationSection("mensagens");
        if (mensagensSec == null) return;

        ConfigurationSection msgSection = mensagensSec.getConfigurationSection(String.valueOf(mensagemAtual));
        if (msgSection == null) return;

        boolean enviarTitulo = msgSection.getBoolean("Titulo", false);
        boolean enviarActionBar = msgSection.getBoolean("ActionBar", false);
        boolean enviarSubtitle = msgSection.getBoolean("Subtitle", false);
        boolean enviarChat = msgSection.getBoolean("chat", true);

        List<String> linhas = msgSection.getStringList("linhas");
        if (linhas.isEmpty()) return;


        StringBuilder mensagemCompleta = new StringBuilder();
        for (String linha : linhas) {
            mensagemCompleta.append(ChatColor.translateAlternateColorCodes('&', linha)).append("\n");
        }
        String mensagemFinal = mensagemCompleta.toString().trim();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (enviarChat) player.sendMessage(mensagemFinal);
            if (enviarSubtitle) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "br s " + mensagemFinal);
            if (enviarTitulo) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "br t " + mensagemFinal);
            if (enviarActionBar) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "br a " + mensagemFinal);
        }
    }

    private long parseTimeToMillis(String time) {
        time = time.toLowerCase().trim();
        try {
            if (time.endsWith("s")) {
                return Long.parseLong(time.replace("s", "")) * 1000;
            } else if (time.endsWith("m")) {
                return Long.parseLong(time.replace("m", "")) * 60 * 1000;
            } else if (time.endsWith("h")) {
                return Long.parseLong(time.replace("h", "")) * 3600 * 1000;
            } else {
                return Long.parseLong(time) * 1000;
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Intervalo inválido em automensagens! Usando 5m padrão.");
            return 5 * 60 * 1000;
        }
    }
}
