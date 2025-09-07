package org.com.mainpl;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BauManager implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private FileConfiguration bausConfig;
    private File bausFile;
    private final Map<String, Map<Integer, Inventory>> baus = new HashMap<>();
    private final Map<String, Long> commandCooldowns = new HashMap<>();
    private final Map<String, Boolean> bauAbrindo = new HashMap<>();

    public BauManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadBausConfig();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    private void loadBausConfig() {
        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        bausFile = new File(dataFolder, "baus.yml");
        if (!bausFile.exists()) {
            try { bausFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        bausConfig = YamlConfiguration.loadConfiguration(bausFile);
    }

    private void saveBausConfig() {
        try {
            bausConfig.save(bausFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao salvar baus.yml");
            e.printStackTrace();
        }
    }

    private String getPrimaryGroup(Player player) {
        LuckPerms lp = LuckPermsProvider.get();
        User user = lp.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "default";
        return user.getPrimaryGroup().toLowerCase();
    }

    private boolean hasAcessoPrimeiroBau(Player player) {
        String group = getPrimaryGroup(player).toLowerCase();

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));

        if (!cfg.isConfigurationSection("baumanager")) return false;

        for (String key : cfg.getConfigurationSection("baumanager").getKeys(false)) {
            if (key.equalsIgnoreCase(group)) return true;
        }
        return false;
    }


    public boolean darBau(CommandSender executor, String targetName, String quantidadeStr) {

        if (!(executor instanceof ConsoleCommandSender) && (!(executor instanceof Player p) || !p.isOp())) {
            executor.sendMessage(ChatColor.RED + "Apenas operadores ou console podem usar este comando.");
            return false;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            executor.sendMessage(ChatColor.RED + "Jogador não encontrado.");
            return false;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(quantidadeStr);
            if (quantidade < 1) {
                executor.sendMessage(ChatColor.RED + "Quantidade inválida, deve ser no mínimo 1.");
                return false;
            }
        } catch (NumberFormatException e) {
            executor.sendMessage(ChatColor.RED + "Número inválido.");
            return false;
        }

        int bausAtuais = bausConfig.getInt(target.getName() + ".baus", 1);
        int proximoBau = bausAtuais + quantidade;

        bausConfig.set(target.getName() + ".baus", proximoBau);
        saveBausConfig();


        loadPlayerData(target.getName());


        Map<Integer, Inventory> playerBaus = baus.get(target.getName());
        for (int i = bausAtuais + 1; i <= proximoBau; i++) {
            if (!playerBaus.containsKey(i)) {
                playerBaus.put(i, Bukkit.createInventory(null, 54, ChatColor.AQUA + "Baú #" + i));
            }
        }

        savePlayerData(target.getName());

        executor.sendMessage(ChatColor.GREEN + "Você deu " + quantidade + " baú(s) para " + target.getName() + "!");
        target.sendMessage(ChatColor.GREEN + "Você recebeu " + quantidade + " baú(s)!");
        return true;
    }




    private int getDelay(Player player) {
        String group = getPrimaryGroup(player);
        return plugin.getConfig().getInt("baumanager." + group + ".delay", 2);
    }

    private int getCooldown(Player player) {
        String group = getPrimaryGroup(player);
        return plugin.getConfig().getInt("baumanager." + group + ".cooldown", 2);
    }


    private void loadPlayerData(String playerName) {
        int bausAtuais = bausConfig.getInt(playerName + ".baus", 0);
        if (bausAtuais < 1) {
            bausConfig.set(playerName + ".baus", 1);
            saveBausConfig();
            bausAtuais = 1;
        }

        Map<Integer, Inventory> playerBaus = baus.getOrDefault(playerName, new HashMap<>());

        for (int i = 1; i <= bausAtuais; i++) {
            if (!playerBaus.containsKey(i)) {
                Inventory inv = Bukkit.createInventory(null, 54, ChatColor.AQUA + "Baú #" + i);
                List<?> itemList = bausConfig.getList(playerName + ".bau" + i);
                if (itemList != null && !itemList.isEmpty()) inv.setContents(itemList.toArray(new ItemStack[0]));
                playerBaus.put(i, inv);
            }
        }

        baus.put(playerName, playerBaus);
    }


    private void savePlayerData(String playerName) {
        Map<Integer, Inventory> playerBaus = baus.get(playerName);
        if (playerBaus == null) return;

        int totalBaus = playerBaus.size();
        for (int i = 1; i <= totalBaus; i++) {
            Inventory inv = playerBaus.get(i);
            if (inv != null) {
                List<ItemStack> items = new ArrayList<>();
                for (ItemStack item : inv.getContents()) items.add(item);
                bausConfig.set(playerName + ".bau" + i, items);
            }
        }

        bausConfig.set(playerName + ".baus", totalBaus);
        saveBausConfig();
    }


    private void updatePlayerStatus(Player player) {
        if (hasAcessoPrimeiroBau(player)) {
            bausConfig.set(player.getName() + ".apoiador_bau", 1);
        } else {
            bausConfig.set(player.getName() + ".apoiador_bau", 0);
        }
        saveBausConfig();
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerStatus(player);
        loadPlayerData(player.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        savePlayerData(event.getPlayer().getName());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String name = player.getName();
        Inventory inv = event.getInventory();
        Map<Integer, Inventory> playerBaus = baus.get(name);
        if (playerBaus == null) return;

        for (Inventory bau : playerBaus.values()) {
            if (inv.equals(bau)) {
                savePlayerData(name);
                return;
            }
        }
    }


    private void openBau(Player player, int num) {
        String name = player.getName();

        if (bauAbrindo.getOrDefault(name, false)) {
            player.sendMessage(ChatColor.RED + "O baú já está abrindo!");
            return;
        }
        bauAbrindo.put(name, true);

        baus.putIfAbsent(name, new HashMap<>());
        Map<Integer, Inventory> playerBaus = baus.get(name);
        playerBaus.putIfAbsent(num, Bukkit.createInventory(null, 54, ChatColor.AQUA + "Baú #" + num));

        int delay = getDelay(player);

        for (int i = 0; i < delay; i++) {
            int finalI = delay - i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.GREEN + "Abrindo em " + finalI + "s..."));
            }, i * 20L);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.openInventory(playerBaus.get(num));
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
            player.spawnParticle(Particle.POOF, player.getLocation().add(0, 1, 0), 20, 0.4, 0.4, 0.4, 0.05);
            savePlayerData(name);
            bauAbrindo.put(name, false);
        }, delay * 20L);
    }




    private void openMenu(Player player) {
        String name = player.getName();
        int totalBaus = Math.max(bausConfig.getInt(name + ".baus", 0), 1);

        Inventory menu = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Seus Baús");

        for (int i = 1; i <= totalBaus; i++) {
            ItemStack item;
            if (i == 1 && bausConfig.getInt(name + ".apoiador_bau", 0) == 0) {
                item = new ItemStack(Material.BARRIER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "Baú #1 (Bloqueado)");
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Exclusivo para grupos no config.yml"));
                item.setItemMeta(meta);
            } else {
                item = new ItemStack(Material.CHEST);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "Baú #" + i);
                item.setItemMeta(meta);

            }
            menu.setItem(i - 1, item);

        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(ChatColor.GOLD + "Seus Baús")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String nome = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        int bau;
        try {
            bau = Integer.parseInt(nome.replace("Baú #", "").replace(" (Bloqueado)", ""));
        } catch (NumberFormatException e) { return; }


        if (bau == 1 && !hasAcessoPrimeiroBau(player)) {
            if (!bauAbrindo.getOrDefault(player.getName(), false)) {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1f, 1f);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.RED + "Você não pode abrir este baú."));
            }
            bauAbrindo.put(player.getName(), false);
            player.closeInventory();
            return;
        }

        if (bauAbrindo.getOrDefault(player.getName(), false)) return; // evita duplicata

        player.closeInventory();
        openBau(player, bau);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Comando apenas para jogadores.");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("darbau")) {

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Uso correto: /darbau <jogador> <quantidade>");
                return true;
            }

            String target = args[0];
            String quantidade = args[1];

            darBau(sender, target, quantidade);
            return true;
        }

        long now = System.currentTimeMillis();
        int cd = getCooldown(player) * 1000;
        if (commandCooldowns.containsKey(player.getName()) && now - commandCooldowns.get(player.getName()) < cd) {
            long restante = (cd - (now - commandCooldowns.get(player.getName()))) / 1000;
            player.sendMessage(ChatColor.RED + "Aguarde " + restante + "s para usar /bau novamente.");
            return true;
        }
        commandCooldowns.put(player.getName(), now);

        updatePlayerStatus(player);
        if (!baus.containsKey(player.getName())) loadPlayerData(player.getName());

        if (args.length == 0) {
            openMenu(player);
            return true;
        }

        try {
            int num = Integer.parseInt(args[0]);


            int totalBaus = Math.max(bausConfig.getInt(player.getName() + ".baus", 0), 1);
            if (num < 1 || num > totalBaus) {
                player.sendMessage(ChatColor.RED + "Você só possui " + totalBaus + " baú(s).");
                return true;
            }


            if (num == 1) {
                if (!hasAcessoPrimeiroBau(player)) {
                    player.sendMessage(ChatColor.RED + "Você não pode abrir este baú.");
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1f, 1f);
                    return true;
                }
            }



            openBau(player, num);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Número inválido.");
        }

        return true;
    }
}
