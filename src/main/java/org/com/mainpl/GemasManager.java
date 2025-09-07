package org.com.mainpl;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class GemasManager implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final MessageManager messages;
    private final File gemasFile;
    private final YamlConfiguration gemasConfig;
    private static final File USER_CACHE_FILE = new File("usercache.json");

    private final HashMap<UUID, Long> anunciarCooldown = new HashMap<>(); // Para controlar cooldown

    public GemasManager(JavaPlugin plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;


        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();


        this.gemasFile = new File(dataFolder, "Gemas.yml");
        if (!gemasFile.exists()) {
            try {
                plugin.saveResource("Gemas.yml", false);
                File tempFile = new File(plugin.getDataFolder(), "Gemas.yml");
                if (tempFile.exists()) tempFile.renameTo(gemasFile);
            } catch (IllegalArgumentException e) {
                try { gemasFile.createNewFile(); } catch (IOException ex) { ex.printStackTrace(); }
            }
        }

        this.gemasConfig = YamlConfiguration.loadConfiguration(gemasFile);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("dargemas")) {


            boolean apenasOp = plugin.getConfig().getBoolean("gemas.dargemas.permissao-op", true);
            if (!(sender instanceof ConsoleCommandSender) &&
                    (apenasOp && (!(sender instanceof Player p) || !p.isOp()))) {
                sender.sendMessage(messages.getMessage("gemas_sem_permissao"));
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage(messages.getMessage("gemas_uso"));
                return true;
            }

            String inputName = args[0];
            String finalName = detectarBedrock(inputName);

            int quantiaRecebe = plugin.getConfig().getInt("gemas.dargemas.quantia", 11);

            Player online = Bukkit.getPlayer(finalName);

            if (online != null) {
                int atuais = gemasConfig.getInt("saldo." + finalName, 0);
                gemasConfig.set("saldo." + finalName, atuais + quantiaRecebe);
                salvar();

                online.sendMessage(messages.getMessage("gemas_recebido_online"));
                sender.sendMessage(messages.getMessage("gemas_dado_jogador_online", "player", finalName));
            } else {
                int pendente = gemasConfig.getInt("pendentes." + finalName, 0);
                gemasConfig.set("pendentes." + finalName, pendente + quantiaRecebe);
                salvar();

                sender.sendMessage(messages.getMessage("gemas_pendente_registrado", "player", finalName));
            }

            return true;
        }

        if (cmd.getName().equalsIgnoreCase("anunciar")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messages.getMessage("somente_jogadores"));
                return true;
            }

            String finalName = detectarBedrock(player.getName());

            try { gemasConfig.load(gemasFile); } catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }

            int saldo = gemasConfig.getInt("saldo." + finalName, 0);
            int custo = plugin.getConfig().getInt("gemas.anunciar.custo", 7);
            int cooldown = plugin.getConfig().getInt("gemas.anunciar.cooldown", 60);


            long agora = System.currentTimeMillis();
            if (anunciarCooldown.containsKey(player.getUniqueId())) {
                long ultimo = anunciarCooldown.get(player.getUniqueId());
                if ((agora - ultimo) < (cooldown * 1000L)) {
                    long restante = ((cooldown * 1000L) - (agora - ultimo)) / 1000;
                    player.sendMessage(messages.getMessage("anunciar_cooldown", "segundos", String.valueOf(restante)));
                    return true;
                }
            }

            if (saldo < custo) {
                player.sendMessage(messages.getMessage("anunciar_sem_gemas", "saldo", String.valueOf(saldo)));
                return true;
            }

            gemasConfig.set("saldo." + finalName, saldo - custo);
            salvar();

            String msg = String.join(" ", args);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "br s " + msg);
            player.sendMessage(messages.getMessage("anunciar_sucesso"));

            anunciarCooldown.put(player.getUniqueId(), agora);

            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String finalName = detectarBedrock(p.getName());

        if (gemasConfig.contains("pendentes." + finalName)) {
            int pendente = gemasConfig.getInt("pendentes." + finalName, 0);
            int saldo = gemasConfig.getInt("saldo." + finalName, 0);
            gemasConfig.set("saldo." + finalName, saldo + pendente);
            gemasConfig.set("pendentes." + finalName, null);
            salvar();

            p.sendMessage(messages.getMessage("gemas_recebidas_pendentes", "quantidade", String.valueOf(pendente)));
        }
    }

    private String detectarBedrock(String nick) {
        if (nick.startsWith(".")) return nick;
        if (USER_CACHE_FILE.exists()) {
            try (FileReader reader = new FileReader(USER_CACHE_FILE)) {
                JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    String savedName = obj.get("name").getAsString();
                    if (savedName.equalsIgnoreCase("." + nick)) return "." + nick;
                }
            } catch (IOException | JsonParseException e) {
                Bukkit.getLogger().warning("[GEMAS] Erro ao ler usercache.json: " + e.getMessage());
            }
        }
        return nick;
    }

    private void salvar() {
        try { gemasConfig.save(gemasFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public int getGemas(Player player) {
        String finalName = detectarBedrock(player.getName());
        return gemasConfig.getInt("saldo." + finalName, 0);
    }
}
