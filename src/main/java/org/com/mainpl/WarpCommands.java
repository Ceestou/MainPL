package org.com.mainpl;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WarpCommands implements CommandExecutor, Listener {

    private final MainPL plugin;
    private final MessageManager messages;
    private final File warpsFile;
    private YamlConfiguration warpsConfig;

    public WarpCommands(MainPL plugin, MessageManager messages, YamlConfiguration warpsConfig) {
        this.plugin = plugin;
        this.messages = messages;

        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!warpsFile.exists()) {
            try {
                warpsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }

    public void setWarpsConfig(YamlConfiguration newConfig) {
        warpsConfig = newConfig;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getMessage("somente_jogadores"));
            return true;
        }

        String cmdName = command.getName().toLowerCase();
        String grupo = plugin.getPlayerGroup(player); // retorna o grupo LuckPerms do jogador

        switch (cmdName) {


            case "setwarp":
                if (!(player.hasPermission("mainpl.comandos.*") || player.hasPermission("mainpl.comandos.setwarp"))) {
                    List<String> gruposWarp = plugin.getConfig().getStringList("warps.cmdwarps");
                    if (!gruposWarp.contains(grupo)) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }

                if (args.length == 0) {
                    player.sendMessage(messages.getMessage("setwarp_uso"));
                    return true;
                }

                String warpName = args[0].toLowerCase();
                Location loc = player.getLocation();


                World world = loc.getWorld();
                warpsConfig.set("warps." + warpName + ".world", world.getUID().toString()); // UUID
                warpsConfig.set("warps." + warpName + ".world-name", world.getName()); // Nome legível
                warpsConfig.set("warps." + warpName + ".x", loc.getX());
                warpsConfig.set("warps." + warpName + ".y", loc.getY());
                warpsConfig.set("warps." + warpName + ".z", loc.getZ());
                warpsConfig.set("warps." + warpName + ".yaw", loc.getYaw());
                warpsConfig.set("warps." + warpName + ".pitch", loc.getPitch());
                warpsConfig.set("warps." + warpName + ".tipoWarp", ""); // padrão: sem restrição de grupo


                if (!warpsConfig.contains("warps." + warpName + ".item")) {
                    warpsConfig.set("warps." + warpName + ".slot", -1); // -1 = automático (ordem sequencial no GUI)
                    warpsConfig.set("warps." + warpName + ".item", "STONE"); // ícone padrão
                    warpsConfig.set("warps." + warpName + ".nome", "&e" + warpName); // nome padrão colorido
                    warpsConfig.set("warps." + warpName + ".lore", Arrays.asList("&7Clique para ir até " + warpName)); // lore padrão
                }

                salvar();

                player.sendMessage(messages.getMessage("setwarp_sucesso", "warp", warpName));
                return true;


            case "delwarp":
                if (!(player.hasPermission("mainpl.comandos.*") || player.hasPermission("mainpl.comandos.delwarp"))) {
                    List<String> gruposWarp = plugin.getConfig().getStringList("warps.cmdwarps");
                    if (!gruposWarp.contains(grupo)) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }

                if (args.length == 0) {
                    player.sendMessage(messages.getMessage("delwarp_uso"));
                    return true;
                }

                warpName = args[0].toLowerCase();

                if (!warpsConfig.contains("warps." + warpName)) {
                    player.sendMessage(messages.getMessage("delwarp_nao_existe", "warp", warpName));
                    return true;
                }

                warpsConfig.set("warps." + warpName, null);
                salvar();

                player.sendMessage(messages.getMessage("delwarp_sucesso", "warp", warpName));
                return true;


            case "warp":


                if (!(player.hasPermission("mainpl.comandos.*") || player.hasPermission("mainpl.comandos.warps"))) {
                    List<String> gruposWarp = plugin.getConfig().getStringList("warps.cmdwarps");
                    if (!gruposWarp.contains(grupo)) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }

                if (args.length == 0) {
                    player.sendMessage(messages.getMessage("warp_uso"));
                    return true;
                }

                warpName = args[0].toLowerCase();
                if (!warpsConfig.contains("warps." + warpName)) {
                    player.sendMessage(messages.getMessage("warp_nao_existe", "warp", warpName));
                    return true;
                }


                String tipoWarp = warpsConfig.getString("warps." + warpName + ".tipoWarp", "");
                if (!tipoWarp.isEmpty() && !(player.hasPermission("mainpl.comandos.*") || player.hasPermission("mainpl.comandos.warps"))) {
                    List<String> gruposEspeciais = plugin.getConfig().getStringList("warps.warpEspecial." + tipoWarp);
                    if (!gruposEspeciais.contains(grupo)) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }


                long now = System.currentTimeMillis();
                plugin.cooldowns.putIfAbsent(player.getUniqueId(), new HashMap<>());
                Map<String, Long> playerCooldowns = plugin.cooldowns.get(player.getUniqueId());



                String delayStr = plugin.getConfig().getString("warps.delays." + grupo, plugin.getConfig().getString("warps.delays.default_delay"));



                String cooldownStr = plugin.getConfig().getString("warps.cooldowns." + grupo, plugin.getConfig().getString("warps.cooldowns.default_cooldown"));

                long delay = parseTime(delayStr);
                long cooldown = parseTime(cooldownStr);


                if (playerCooldowns.containsKey(warpName)) {
                    long nextUse = playerCooldowns.get(warpName);
                    if (now < nextUse) {
                        long remaining = (nextUse - now) / 1000;
                        player.sendMessage(messages.getMessage("cooldown_comando", "seconds", String.valueOf(remaining)));
                        return true;
                    }
                }




                playerCooldowns.put(warpName, now + cooldown);


                new BukkitRunnable() {
                    long segundos = delay / 1000;

                    @Override
                    public void run() {
                        if (segundos <= 0) {
                            World world = Bukkit.getWorld(warpsConfig.getString("warps." + warpName + ".world-name"));

                            if (world == null) {
                                player.sendMessage(messages.getMessage("warp_mundo_inexistente", "warp", warpName));
                                cancel();
                                return;
                            }

                            Location warpLoc = new Location(
                                    world,
                                    warpsConfig.getDouble("warps." + warpName + ".x"),
                                    warpsConfig.getDouble("warps." + warpName + ".y"),
                                    warpsConfig.getDouble("warps." + warpName + ".z"),
                                    (float) warpsConfig.getDouble("warps." + warpName + ".yaw"),
                                    (float) warpsConfig.getDouble("warps." + warpName + ".pitch")
                            );

                            player.teleport(warpLoc);
                            player.sendMessage(messages.getMessage("warp_teleport", "warp", warpName));
                            cancel();
                            return;
                        }

                        player.sendMessage(messages.getMessage("comando_delay", "seconds", String.valueOf(segundos)));
                        segundos--;
                    }
                }.runTaskTimer(plugin, 0L, 20L);

                return true;


            case "warps":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cApenas jogadores podem usar este comando.");
                    return true;
                }
                Player p = (Player) sender;

                if (!(p.hasPermission("mainpl.comandos.*") || p.hasPermission("mainpl.comandos.warpslist"))) {
                    List<String> gruposWarp = plugin.getConfig().getStringList("warps.cmdwarps");
                    if (!gruposWarp.contains(grupo)) {
                        p.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }


                long now2 = System.currentTimeMillis();
                plugin.cooldowns.putIfAbsent(p.getUniqueId(), new HashMap<>());
                Map<String, Long> playerCooldowns2 = plugin.cooldowns.get(p.getUniqueId());
                String comando = "warps";

                long cooldown2 = parseTime(plugin.getConfig().getString("warps.cooldowns." + grupo,
                        plugin.getConfig().getString("warps.cooldowns.default_cooldown")));

                if (playerCooldowns2.containsKey(comando)) {
                    long nextUse = playerCooldowns2.get(comando);
                    if (now2 < nextUse) {
                        long remaining = (nextUse - now2) / 1000;
                        p.sendMessage(messages.getMessage("cooldown_comando", "seconds", String.valueOf(remaining)));
                        return true;
                    }
                }

                playerCooldowns2.put(comando, now2 + cooldown2);

                ConfigurationSection warpsSection = this.warpsConfig.getConfigurationSection("warps");

                if (warpsSection == null || warpsSection.getKeys(false).isEmpty()) {
                    p.sendMessage(messages.getMessage("warps_vazio"));
                    return true;
                }


                int rows = warpsConfig.getInt("warpsSettings.rols", 6); // default 6 linhas
                if (rows < 1) rows = 1;
                if (rows > 6) rows = 6;
                int size = rows * 9;

                Inventory inv = Bukkit.createInventory(null, size, "§6Warps");
                int autoSlot = 0;

                for (String warp : warpsSection.getKeys(false)) {
                    int slot = warpsConfig.getInt("warps." + warp + ".slot", -1);

                    if (slot == -1) {
                        slot = autoSlot;
                        autoSlot++;
                    }

                    String itemName = warpsConfig.getString("warps." + warp + ".item", "STONE");
                    String nome = ChatColor.translateAlternateColorCodes('&', warpsConfig.getString("warps." + warp + ".nome", warp));
                    List<String> lore = warpsConfig.getStringList("warps." + warp + ".lore");

                    Material mat = Material.matchMaterial(itemName);
                    if (mat == null) mat = Material.STONE;

                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(nome);

                    List<String> loreColorida = new ArrayList<>();
                    for (String l : lore) {
                        loreColorida.add(ChatColor.translateAlternateColorCodes('&', l));
                    }
                    meta.setLore(loreColorida);
                    item.setItemMeta(meta);

                    inv.setItem(slot, item);
                }

                p.openInventory(inv);
                return true;





            case "especial":
                List<String> gruposEspecial = plugin.getConfig().getStringList("warps.setwarps");
                if (!gruposEspecial.contains(grupo)) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage(messages.getMessage("setespecial_uso"));
                    return true;
                }

                warpName = args[0].toLowerCase();
                String tipo = args[1];

                if (!warpsConfig.contains("warps." + warpName)) {
                    player.sendMessage(messages.getMessage("warp_nao_existe", "warp", warpName));
                    return true;
                }

                warpsConfig.set("warps." + warpName + ".tipoWarp", tipo);
                salvar();

                player.sendMessage(messages.getMessage("setespecial_sucesso", "warp", warpName) + " Tipo especial: " + tipo);
                return true;
        }

        return false;
    }
    private long parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return 0L;

        timeStr = timeStr.toLowerCase().trim();
        long multiplier = 1000L;

        if (timeStr.endsWith("s")) multiplier = 1000L;
        else if (timeStr.endsWith("m")) multiplier = 60 * 1000L;
        else if (timeStr.endsWith("h")) multiplier = 60 * 60 * 1000L;
        else if (timeStr.endsWith("d")) multiplier = 24 * 60 * 60 * 1000L;

        try {
            long value = Long.parseLong(timeStr.replaceAll("[^0-9]", ""));
            return value * multiplier;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }


    private void salvar() {
        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (!e.getView().getTitle().equalsIgnoreCase("§6Warps")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String nomeWarp = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        ConfigurationSection warpsSection = warpsConfig.getConfigurationSection("warps");
        if (warpsSection == null) return;

        for (String warp : warpsSection.getKeys(false)) {
            String nome = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    warpsConfig.getString("warps." + warp + ".nome", warp)));

            if (!nome.equalsIgnoreCase(nomeWarp)) continue;

            String grupo = plugin.getPlayerGroup(p);
            long now = System.currentTimeMillis();
            plugin.cooldowns.putIfAbsent(p.getUniqueId(), new HashMap<>());
            Map<String, Long> playerCooldowns = plugin.cooldowns.get(p.getUniqueId());


            long delay = parseTime(plugin.getConfig().getString("warps.delays." + grupo,
                    plugin.getConfig().getString("warps.delays.default_delay")));
            long cooldown = parseTime(plugin.getConfig().getString("warps.cooldowns." + grupo,
                    plugin.getConfig().getString("warps.cooldowns.default_cooldown")));


            if (playerCooldowns.containsKey(warp)) {
                long nextUse = playerCooldowns.get(warp);
                if (now < nextUse) {
                    long remaining = (nextUse - now) / 1000;
                    p.sendMessage(messages.getMessage("cooldown_comando", "seconds", String.valueOf(remaining)));
                    return;
                }
            }


            playerCooldowns.put(warp, now + cooldown);

            World w = Bukkit.getWorld(warpsConfig.getString("warps." + warp + ".world-name"));
            if (w == null) {
                p.sendMessage(messages.getMessage("warp_mundo_inexistente", "warp", warp));
                return;
            }

            Location destino = new Location(
                    w,
                    warpsConfig.getDouble("warps." + warp + ".x"),
                    warpsConfig.getDouble("warps." + warp + ".y"),
                    warpsConfig.getDouble("warps." + warp + ".z"),
                    (float) warpsConfig.getDouble("warps." + warp + ".yaw"),
                    (float) warpsConfig.getDouble("warps." + warp + ".pitch")
            );

            p.closeInventory();


            new BukkitRunnable() {
                long segundos = delay / 1000;

                @Override
                public void run() {
                    if (segundos <= 0) {
                        p.teleport(destino);
                        p.sendMessage(messages.getMessage("warp_teleport", "warp", warp));
                        cancel();
                        return;
                    }

                    p.sendMessage(messages.getMessage("comando_delay", "seconds", String.valueOf(segundos)));
                    segundos--;
                }
            }.runTaskTimer(plugin, 0L, 20L);

            return;
        }
    }
}
