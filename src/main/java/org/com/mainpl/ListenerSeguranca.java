package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ListenerSeguranca implements Listener, CommandExecutor {

    private final JavaPlugin plugin;


    private final File opsFile;
    private final YamlConfiguration opsConfig;

    private final Map<UUID, String> codigosPorPlayer = new HashMap<>();
    private final Map<UUID, Integer> tentativasRestantes = new HashMap<>();
    private final Set<UUID> autenticados = new HashSet<>();

    private final File comandosOPFile;
    private final YamlConfiguration comandosOPConfig;
    private final List<String> apenasAutenticados;

    public ListenerSeguranca(JavaPlugin plugin) {
        this.plugin = plugin;




        this.comandosOPFile = new File(plugin.getDataFolder(), "comandosOP.yml");
        if (!comandosOPFile.exists()) {
            comandosOPFile.getParentFile().mkdirs();
            try {
                comandosOPFile.createNewFile();
                YamlConfiguration tempConfig = YamlConfiguration.loadConfiguration(comandosOPFile);
                tempConfig.set("apenasAutenticados", Arrays.asList("voar", "gamemode tree")); // Exemplo inicial
                tempConfig.save(comandosOPFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.comandosOPConfig = YamlConfiguration.loadConfiguration(comandosOPFile);
        this.apenasAutenticados = comandosOPConfig.getStringList("apenasAutenticados");


        this.opsFile = new File(plugin.getDataFolder(), "ops.yml");
        if (!opsFile.exists()) {
            opsFile.getParentFile().mkdirs();
            try {
                opsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.opsConfig = YamlConfiguration.loadConfiguration(opsFile);


        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                if (!player.isOp()) {
                    autenticados.remove(uuid);
                    continue;
                }




                if (autenticados.contains(uuid)) continue;


                if (opsConfig.contains("ops." + player.getName() + ".Senha")) {

                    String ipAtual = player.getAddress().getAddress().getHostAddress();


                    String ipSalvo = opsConfig.getString("ops." + player.getName() + ".IP", "");
                    if (ipAtual.equals(ipSalvo)) {
                        autenticados.add(player.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + "Seu IP foi reconhecido. Você está autenticado automaticamente.");
                        return;
                    }



                    tentativasRestantes.putIfAbsent(uuid, 3);


                    if (!codigosPorPlayer.containsKey(uuid)) {
                        player.sendMessage(ChatColor.RED + "🔐 Você se tornou OP! Digite sua senha de OP com /auth <senha> para ativar.");
                        codigosPorPlayer.put(uuid, "SENHA"); // flag só para marcar que mensagem já foi enviada
                    }

                    continue;
                }

                if (!codigosPorPlayer.containsKey(uuid)) {
                    String novoCodigo = gerarCodigoSeguro();
                    codigosPorPlayer.put(uuid, novoCodigo);
                    tentativasRestantes.put(uuid, 3);
                    player.sendMessage(ChatColor.RED + "🔐 Você se tornou OP! Digite /auth <código> para ativar.");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[SEGURANÇA] Código do OP " + player.getName() + ": " + ChatColor.YELLOW + novoCodigo);
                }
            }
        }, 0L, 20L);


    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String mensagem = event.getMessage().toLowerCase();

        if (!player.isOp()) {
            autenticados.remove(player.getUniqueId());
        }

        for (String cmd : apenasAutenticados) {
            String cmdLower = "/" + cmd.toLowerCase();
            if (mensagem.startsWith(cmdLower)) {

                if (!autenticados.contains(player.getUniqueId())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "⚠ Você precisa estar autenticado como OP para usar este comando!");
                    return;
                }
            }
        }
    }



    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            autenticados.remove(player.getUniqueId());
            return;
        }





        if (opsConfig.contains("ops." + player.getName())) {

            String ipAtual = player.getAddress().getAddress().getHostAddress();


            String ipSalvo = opsConfig.getString("ops." + player.getName() + ".IP", "");
            if (ipAtual.equals(ipSalvo)) {
                autenticados.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Seu IP foi reconhecido. Você está autenticado automaticamente.");
                return;
            }



            tentativasRestantes.put(player.getUniqueId(), 3);
            autenticados.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "🔐 Digite sua senha de OP com /auth <senha>.");
            return;
        }


        String novoCodigo = gerarCodigoSeguro();
        codigosPorPlayer.put(player.getUniqueId(), novoCodigo);
        tentativasRestantes.put(player.getUniqueId(), 3);
        autenticados.remove(player.getUniqueId());

        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[SEGURANÇA] Código do OP " + player.getName() + ": " + ChatColor.YELLOW + novoCodigo);
        player.sendMessage(ChatColor.RED + "🔐 Digite o código de segurança com /auth <código> para ativar seu OP.");
    }





    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;
        if (autenticados.contains(player.getUniqueId())) {
            player.sendMessage("§aVocê já está autenticado.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUso correto: /auth <senha ou código>");
            return true;
        }

        String codigoDigitado = args[0];
        autenticar(player, codigoDigitado);
        return true;
    }



    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && !autenticados.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "⚠ Você não pode quebrar blocos sem autenticação!");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!autenticados.contains(player.getUniqueId()) && player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "⚠ Você precisa se autenticar antes de interagir!");
        }
    }


    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (!autenticados.contains(player.getUniqueId()) && player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "⚠ Você precisa se autenticar antes de abrir inventários!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!autenticados.contains(player.getUniqueId()) && player.isOp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!autenticados.contains(player.getUniqueId()) && player.isOp()) {

            if (!event.getFrom().equals(event.getTo())) {
                event.setTo(event.getFrom());
            }
        }
    }

    public boolean autenticar(Player player, String codigoDigitado) {
        UUID uuid = player.getUniqueId();

        String senhaSalva = opsConfig.getString("ops." + player.getName() + ".Senha");

        if (senhaSalva != null) {
            if (senhaSalva.equals(codigoDigitado)) {
                autenticados.add(uuid);


                String ipAtual = player.getAddress().getAddress().getHostAddress();
                opsConfig.set("ops." + player.getName() + ".IP", ipAtual);
                try {
                    opsConfig.save(opsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                player.sendMessage(ChatColor.GREEN + "✅ Senha correta. Acesso liberado! Seu IP foi salvo para logins futuros.");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Senha incorreta!");
                int tentativas = tentativasRestantes.getOrDefault(uuid, 3) - 1;
                tentativasRestantes.put(uuid, tentativas);
                if (tentativas <= 0) {
                    player.kickPlayer(ChatColor.RED + "Você foi kickado por falhar na autenticação.");
                }
                return false;
            }
        }


        String codigoCorreto = codigosPorPlayer.get(uuid);
        if (codigoCorreto == null) {
            player.sendMessage(ChatColor.RED + "❌ Nenhum código ativo encontrado. Aguarde um momento ou relogue.");
            return false;
        }

        int tentativas = tentativasRestantes.getOrDefault(uuid, 3);
        if (codigoCorreto.equals(codigoDigitado)) {
            autenticados.add(uuid);
            tentativasRestantes.remove(uuid);
            codigosPorPlayer.remove(uuid);
            player.sendMessage(ChatColor.GREEN + "✅ Código aceito. Acesso liberado!");
            return true;
        } else {
            tentativasRestantes.put(uuid, tentativas - 1);
            player.sendMessage(ChatColor.RED + "Código incorreto! Tentativas restantes: " + (tentativas - 1));
            if (tentativas - 1 <= 0) {
                player.kickPlayer(ChatColor.RED + "Você foi kickado por falhar na autenticação.");
            }
            return false;
        }
    }



    private String gerarCodigoSeguro() {
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }

    public boolean isAutenticado(Player player) {
        return autenticados.contains(player.getUniqueId());
    }
}
