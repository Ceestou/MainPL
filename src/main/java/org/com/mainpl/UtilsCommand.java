package org.com.mainpl;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.bukkit.Material.getMaterial;


public class UtilsCommand implements CommandExecutor, Listener {

    private final MainPL plugin;
    private final MessageManager messages;
    private final File file;
    private FileConfiguration config;

    private final Set<UUID> frozenPlayers = new HashSet<>();
    private File freezeFile;
    private final FileConfiguration freezeConfig;



    public UtilsCommand(MainPL plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;

        this.file = new File(plugin.getDataFolder(), "idsgive.yml");
        if (!file.exists()) {
            plugin.saveResource("idsgive.yml", false); // copia o arquivo do plugin se não existir
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();


        this.freezeFile = new File(dataFolder, "freeze.yml");
        if (!freezeFile.exists()) {
            try {
                freezeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        this.freezeConfig = YamlConfiguration.loadConfiguration(freezeFile);


    }

    public void setIdsConfig(YamlConfiguration newConfig) {
        config = newConfig;
    }






    private boolean hasPermission(Player player, String grupo, String permission) {

        if (player.hasPermission(permission)) return true;


        String[] parts = permission.split("\\.");
        if (parts.length > 2) {
            String wildcard = parts[0] + "." + parts[1] + ".*"; // ex: mainpl.utils.*
            if (player.hasPermission(wildcard)) return true;
        }


        return plugin.getConfig().getStringList("utils." + parts[2]).contains(grupo);
    }





    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        boolean isConsole = !(sender instanceof Player);
        Player player = sender instanceof Player ? (Player) sender : null;



        String grupo;
        if (isConsole) {
            grupo = "CONSOLE";
        } else {
            grupo = plugin.getPrimaryGroup(player);
        }

        switch (command.getName().toLowerCase()) {


            case "feed":
                if (!isConsole) {
                    if (!plugin.getConfig().getStringList("utils.feed").contains(grupo)
                            && !player.hasPermission("mainpl.utils.feed")
                            && !player.hasPermission("mainpl.utils.*")) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }
                Player feedTarget;
                if (args.length > 0) {
                    feedTarget = Bukkit.getPlayer(args[0]);
                } else {
                    if (isConsole) {
                        sender.sendMessage("§cUso: /feed <jogador>");
                        return true;
                    }
                    feedTarget = player;
                }
                if (feedTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }
                feedTarget.setFoodLevel(20);
                feedTarget.setSaturation(20f);
                feedTarget.sendMessage(messages.getMessage("feed_sucesso"));
                if (!isConsole && !feedTarget.equals(player)) {
                    player.sendMessage(messages.getMessage("feed_sucesso_target", "player", feedTarget.getName()));
                }
                break;


            case "heal":
                if (!isConsole) {
                    if (!plugin.getConfig().getStringList("utils.heal").contains(grupo)
                            && !player.hasPermission("mainpl.utils.heal")
                            && !player.hasPermission("mainpl.utils.*")) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }

                Player healTarget;
                if (args.length > 0) {
                    healTarget = Bukkit.getPlayer(args[0]);
                } else {
                    if (isConsole) {
                        sender.sendMessage("§cUso: /heal <jogador>");
                        return true;
                    }
                    healTarget = player;
                }
                if (healTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }
                healTarget.setHealth(healTarget.getMaxHealth());
                healTarget.setFireTicks(0);
                healTarget.removePotionEffect(PotionEffectType.POISON);
                healTarget.removePotionEffect(PotionEffectType.WITHER);
                healTarget.sendMessage(messages.getMessage("heal_sucesso"));
                if (!isConsole && !healTarget.equals(player)) player.sendMessage(messages.getMessage("heal_sucesso_target", "player", healTarget.getName()));
                break;


            case "gm":
                if (!isConsole) {
                    if (!plugin.getConfig().getStringList("utils.gm").contains(grupo)
                            && !player.hasPermission("mainpl.utils.gm")
                            && !player.hasPermission("mainpl.utils.*")) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }

                if (args.length == 0) {
                    sender.sendMessage(messages.getMessage("gamemode_invalido"));
                    return true;
                }

                GameMode mode;
                switch (args[0]) {
                    case "0": mode = GameMode.SURVIVAL; break;
                    case "1": mode = GameMode.CREATIVE; break;
                    case "2": mode = GameMode.ADVENTURE; break;
                    case "3": mode = GameMode.SPECTATOR; break;
                    default:
                        sender.sendMessage(messages.getMessage("gamemode_invalido"));
                        return true;
                }

                Player gmTarget;
                if (args.length > 1) {
                    gmTarget = Bukkit.getPlayer(args[1]);
                } else {
                    if (isConsole) {
                        sender.sendMessage("§cUso: /gm <0,1,2,3> <jogador>");
                        return true;
                    }
                    gmTarget = player;
                }

                if (gmTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }

                gmTarget.setGameMode(mode);
                gmTarget.sendMessage(messages.getMessage("gamemode_sucesso", "modo", args[0]));
                if (!isConsole && !gmTarget.equals(player)) {
                    player.sendMessage(messages.getMessage("gamemode_sucesso_target", "player", gmTarget.getName(), "modo", args[0]));
                }
                break;



            case "tphere":
                if (!isConsole) {
                    if (!plugin.getConfig().getStringList("utils.tphere").contains(grupo)
                            && !player.hasPermission("mainpl.utils.tphere")
                            && !player.hasPermission("mainpl.utils.*")) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }
                if (args.length == 0) {
                    sender.sendMessage(messages.getMessage("tphere_jogador_nao_encontrado"));
                    return true;
                }
                Player tphereTarget;
                if (args.length > 0) {
                    tphereTarget = Bukkit.getPlayer(args[0]);
                } else {
                    if (isConsole) {
                        sender.sendMessage("§cUso: /tphere <jogador>");
                        return true;
                    }
                    tphereTarget = player;
                }
                if (tphereTarget == null) {
                    sender.sendMessage(messages.getMessage("tphere_jogador_nao_encontrado"));
                    return true;
                }
                if (isConsole) {
                    sender.sendMessage("§cO console não pode usar /tphere sem especificar um jogador de origem.");
                    return true;
                }
                tphereTarget.teleport(player.getLocation());
                tphereTarget.sendMessage(messages.getMessage("tphere_sucesso", "player", player.getName()));
                player.sendMessage(messages.getMessage("tphere_sucesso_target", "player", tphereTarget.getName()));
                break;


            case "fly":
                if (!isConsole) {
                    if (!plugin.getConfig().getStringList("utils.fly").contains(grupo)
                            && !player.hasPermission("mainpl.utils.fly")
                            && !player.hasPermission("mainpl.utils.*")) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }
                Player flyTarget;
                if (args.length > 0) {
                    flyTarget = Bukkit.getPlayer(args[0]);
                } else {
                    if (isConsole) {
                        sender.sendMessage("§cUso: /fly <jogador>");
                        return true;
                    }
                    flyTarget = player;
                }
                if (flyTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }
                boolean fly = !flyTarget.getAllowFlight();
                flyTarget.setAllowFlight(fly);
                flyTarget.setFlying(fly);
                flyTarget.sendMessage(messages.getMessage(fly ? "fly_ativado" : "fly_desativado"));
                if (!isConsole && !flyTarget.equals(player)) player.sendMessage(messages.getMessage("fly_sucesso_target", "player", flyTarget.getName()));
                break;

            case "speed":
                if (!isConsole) {
                    if (!plugin.getConfig().getStringList("utils.speed").contains(grupo)
                            && !player.hasPermission("mainpl.utils.speed")
                            && !player.hasPermission("mainpl.utils.*")) {
                        player.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }

                Player speedTarget = null;
                float speed = 0.2f;

                if (args.length == 0) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cUso: /speed <jogador> <1-10> ou /speed <1-10>");
                        return true;
                    }
                    speedTarget = (Player) sender;
                    speedTarget.setWalkSpeed(speed);
                    speedTarget.sendMessage(messages.getMessage("speed_resetado"));
                    return true;
                }

                if (args.length == 1) {
                    try {

                        speed = Math.min(Math.max(Float.parseFloat(args[0]), 1), 10) / 10f;
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("§cUso: /speed <jogador> <1-10>");
                            return true;
                        }
                        speedTarget = (Player) sender;
                        speedTarget.setWalkSpeed(speed);
                        speedTarget.sendMessage(messages.getMessage("speed_definido", "valor", String.valueOf((int)(speed * 10))));
                    } catch (NumberFormatException e) {

                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                            return true;
                        }
                        speedTarget = target;
                        speedTarget.setWalkSpeed(speed);
                        speedTarget.sendMessage(messages.getMessage("speed_resetado_target", "player", target.getName()));
                    }
                } else {

                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                        return true;
                    }
                    try {
                        speed = Math.min(Math.max(Float.parseFloat(args[1]), 1), 10) / 10f;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(messages.getMessage("speed_valor_invalido"));
                        return true;
                    }
                    target.setWalkSpeed(speed);
                    target.sendMessage(messages.getMessage("speed_definido_target", "player", target.getName(), "valor", String.valueOf((int)(speed * 10))));
                }

                break;



            case "clearchat":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.clearchat").contains(grupo)
                                && !player.hasPermission("mainpl.utils.clearchat")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                for (Player p : Bukkit.getOnlinePlayers())
                    for (int i = 0; i < 100; i++) p.sendMessage("");
                Bukkit.broadcastMessage(messages.getMessage("clearchat_sucesso"));
                break;
            case "afk":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.afk").contains(grupo)
                                && !player.hasPermission("mainpl.utils.afk")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                plugin.toggleAfk(player);
                break;




            case "skull":
                if (!plugin.getConfig().getStringList("utils.skull").contains(grupo)
                        && !player.hasPermission("mainpl.utils.skull")
                        && !player.hasPermission("mainpl.utils.*")) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                if (args.length == 0) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }
                Player skullTarget;
                if (args.length > 0) {
                    skullTarget = Bukkit.getPlayer(args[0]);
                } else {
                    if (isConsole) {
                        sender.sendMessage("§cUso: /skull <jogador>");
                        return true;
                    }
                    skullTarget = player;
                }
                if (skullTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }
                if (!isConsole) {
                    ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta meta = skull.getItemMeta();
                    meta.setDisplayName(skullTarget.getName());
                    skull.setItemMeta(meta);
                    player.getInventory().addItem(skull);
                    player.sendMessage(messages.getMessage("skull_sucesso", "player", skullTarget.getName()));

                } else {

                    sender.sendMessage("§aCabeça do jogador §e" + skullTarget.getName() + "§a poderia ser dada a um player.");
                }

                break;

            case "jail":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.jail").contains(grupo)
                                && !player.hasPermission("mainpl.utils.jail")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.RED + "Uso: /jail <jogador>");
                    return true;
                }
                Player jailTarget;
                if (args.length > 0) {
                    jailTarget = Bukkit.getPlayer(args[0]);
                } else {
                    if (isConsole) {
                        sender.sendMessage("§cUso: /jail <jogador>");
                        return true;
                    }
                    jailTarget = player;
                }
                if (jailTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }
                plugin.toggleJail(jailTarget);
                sender.sendMessage(ChatColor.GREEN + "Toggle jail aplicado em " + jailTarget.getName());
                break;

            case "give":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.give").contains(grupo)
                                && !player.hasPermission("mainpl.utils.give")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage("§cUso: /give <jogador> <item> [quantidade] [encantamentos...]");
                    return true;
                }

                String targetName = args[0];
                String materialName = args[1];
                int quantidade = 1;

                if (args.length >= 3 && args[2].matches("\\d+")) {
                    quantidade = Integer.parseInt(args[2]);
                }

                Player giveTarget = Bukkit.getPlayer(targetName);
                if (giveTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }

                Material mat = null;


                if (materialName.matches("\\d+")) {
                    int id = Integer.parseInt(materialName);
                    mat = getMaterialByID(id);
                }


                if (mat == null) {
                    for (String key : config.getKeys(false)) {
                        List<String> values = config.getStringList(key);
                        if (values.size() >= 2) {
                            String matEnglish = values.get(0);
                            String matPT = values.get(1);

                            if (matPT.equalsIgnoreCase(materialName) || matEnglish.equalsIgnoreCase(materialName)) {
                                mat = Material.matchMaterial(matEnglish.toUpperCase());
                                break;
                            }
                        }
                    }
                }


                if (mat == null) mat = Material.matchMaterial(materialName.toUpperCase());

                if (mat == null) {
                    sender.sendMessage(messages.getMessage("item_invalido"));
                    return true;
                }

                ItemStack giveItem = new ItemStack(mat, quantidade);


                int encStart = (args.length >= 3 && args[1].matches("\\d+")) ? 3 : 2;
                if (args.length > encStart) {
                    String[] encArgs = Arrays.copyOfRange(args, encStart, args.length);
                    if (encArgs.length % 2 != 0) {
                        sender.sendMessage(messages.getMessage("uso_give") + " &cEncantamentos inválidos.");
                        return true;
                    }

                    int max = plugin.getMaxLevel();
                    for (int i = 0; i < encArgs.length; i += 2) {
                        String alias = encArgs[i];
                        String lvlStr = encArgs[i + 1];

                        Enchantment ench = plugin.getAliases().fromAlias(alias);
                        if (ench == null) {
                            sender.sendMessage("§cEncantamento inválido: " + alias);
                            sender.sendMessage(plugin.getEnchantmentsList());
                            continue;
                        }


                        int level;
                        try { level = Integer.parseInt(lvlStr); } catch (Exception e) { continue; }

                        double ratio = Math.max(0d, Math.min(1d, (double) level / max));
                        EnchantUtil.setRatio(giveItem, ench, ratio);
                    }
                }

                giveTarget.getInventory().addItem(giveItem);
                sender.sendMessage(messages.getMessage("give_sucesso", "player", giveTarget.getName(), "item", materialName));
                break;



            case "giveall":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.giveall").contains(grupo)
                                && !player.hasPermission("mainpl.utils.giveall")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                if (args.length < 1) {
                    sender.sendMessage(messages.getMessage("uso_give"));
                    return true;
                }

                String matNameAll = args[0];
                int quantAll = 1;

                if (args.length >= 2 && args[1].matches("\\d+")) {
                    quantAll = Integer.parseInt(args[1]);
                }

                Material matAll = null;


                matAll = getMaterial(matNameAll);
                if (matAll == null) matAll = Material.matchMaterial(matNameAll.toUpperCase());


                if (matAll == null && matNameAll.matches("\\d+")) {
                    int id = Integer.parseInt(matNameAll);
                    matAll = getMaterialByID(id);
                }

                if (matAll == null) {
                    sender.sendMessage(messages.getMessage("item_invalido"));
                    return true;
                }

                ItemStack itemToGiveAll = new ItemStack(matAll, quantAll);


                int encStartAll = 2;
                if (!(args.length >= 2 && args[1].matches("\\d+"))) {
                    encStartAll = 1;
                }

                if (args.length > encStartAll) {
                    String[] encArgsAll = Arrays.copyOfRange(args, encStartAll, args.length);
                    if (encArgsAll.length % 2 != 0) {
                        sender.sendMessage(messages.getMessage("uso_give") + " &cEncantamentos inválidos.");
                        return true;
                    }

                    int max = plugin.getMaxLevel();
                    for (int i = 0; i < encArgsAll.length; i += 2) {
                        String alias = encArgsAll[i];
                        String lvlStr = encArgsAll[i + 1];

                        Enchantment ench = plugin.getAliases().fromAlias(alias);
                        if (ench == null) {
                            sender.sendMessage("§cEncantamento inválido: " + alias);
                            sender.sendMessage(plugin.getEnchantmentsList());
                            continue;
                        }

                        int level;
                        try {
                            level = Integer.parseInt(lvlStr);
                        } catch (Exception e) {
                            continue;
                        }

                        double ratio = Math.max(0d, Math.min(1d, (double) level / max));
                        EnchantUtil.setRatio(itemToGiveAll, ench, ratio);
                    }
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getInventory().addItem(itemToGiveAll.clone());
                }

                Bukkit.broadcastMessage(messages.getMessage(
                        "giveall_sucesso",
                        "item", matNameAll,
                        "quantidade", String.valueOf(quantAll)
                ));
                break;



            case "exp":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.exp").contains(grupo)
                                && !player.hasPermission("mainpl.utils.exp")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                int xp = 1;
                Player expTarget;

                if (args.length == 0) {
                    if (isConsole) {
                        sender.sendMessage("§cUso no console: /exp [quantidade] <jogador>");
                        return true;
                    }
                    expTarget = player;
                } else if (args.length == 1) {
                    if (args[0].matches("\\d+")) {
                        xp = Integer.parseInt(args[0]);
                        expTarget = isConsole ? null : player;
                        if (expTarget == null) {
                            sender.sendMessage("§cUso no console: /exp <quantidade> <jogador>");
                            return true;
                        }
                    } else {
                        expTarget = Bukkit.getPlayer(args[0]);
                        if (expTarget == null) {
                            sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                            return true;
                        }
                    }
                } else {
                    try {
                        xp = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(messages.getMessage("valor_invalido"));
                        return true;
                    }

                    expTarget = Bukkit.getPlayer(args[1]);
                    if (expTarget == null) {
                        sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                        return true;
                    }
                }

                expTarget.giveExp(xp);
                expTarget.sendMessage(messages.getMessage("exp_sucesso", "quantidade", String.valueOf(xp)));

                if (!expTarget.equals(sender)) {
                    sender.sendMessage(messages.getMessage("exp_sucesso_target",
                            "player", expTarget.getName(),
                            "quantidade", String.valueOf(xp)));
                }
                break;


            case "level":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.level").contains(grupo)
                                && !player.hasPermission("mainpl.utils.level")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                if (args.length == 0) {
                    sender.sendMessage(messages.getMessage("uso_level"));
                    return true;
                }

                Player levelTarget = player;
                int level = 0;
                boolean add = false;

                if (args.length == 1) {
                    String l = args[0];


                    if (l.startsWith("+")) {
                        add = true;
                        l = l.replace("+", "");
                    }

                    try {
                        level = Integer.parseInt(l);
                    } catch (NumberFormatException e) {

                        if (Bukkit.getPlayer(args[0]) != null) {
                            sender.sendMessage(messages.getMessage("uso_level"));
                        } else {
                            sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                        }
                        return true;
                    }
                }

                if (args.length >= 2) {
                    levelTarget = Bukkit.getPlayer(args[0]);
                    if (levelTarget == null) {
                        sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                        return true;
                    }
                    String l = args[1];
                    if (l.startsWith("+")) {
                        add = true;
                        l = l.replace("+", "");
                    }
                    try {
                        level = Integer.parseInt(l);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(messages.getMessage("valor_invalido"));
                        return true;
                    }
                }

                if (add) levelTarget.setLevel(levelTarget.getLevel() + level);
                else levelTarget.setLevel(level);

                levelTarget.sendMessage(messages.getMessage("level_sucesso", "level", String.valueOf(levelTarget.getLevel())));
                if (!levelTarget.equals(player))
                    sender.sendMessage(messages.getMessage("level_sucesso_target", "player", levelTarget.getName(), "level", String.valueOf(levelTarget.getLevel())));
                break;


            case "up":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.up").contains(grupo)
                                && !player.hasPermission("mainpl.utils.up")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                Location loc = player.getLocation();
                Block under = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());

                if (under.getType().isAir()) {
                    under.setType(Material.GLASS);
                }
                break;

            case "god":
                Player target2;

                if (args.length > 0) {

                    target2 = Bukkit.getPlayer(args[0]);
                    if (target2 == null) {
                        sender.sendMessage("§cJogador não encontrado.");
                        return true;
                    }
                } else {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cUso correto: /god <jogador>");
                        return true;
                    }
                    target2 = (Player) sender;
                }


                if (sender instanceof Player player2) {
                    if (!plugin.getConfig().getStringList("utils.god").contains(grupo)
                            && !player2.hasPermission("mainpl.utils.god")
                            && !player2.hasPermission("mainpl.utils.*")) {
                        player2.sendMessage(messages.getMessage("sem_permissao"));
                        return true;
                    }
                }

                plugin.toggleGod(target2);
                sender.sendMessage("§aGodMode alternado para " + target2.getName());
                break;


            case "rename":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.rename").contains(grupo)
                                && !player.hasPermission("mainpl.utils.rename")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                if (player.getInventory().getItemInMainHand() == null) {
                    player.sendMessage(messages.getMessage("nenhum_item_mao"));
                    return true;
                }
                if (args.length == 0) { player.sendMessage(messages.getMessage("uso_rename")); return true; }
                ItemStack renameItem = player.getInventory().getItemInMainHand();
                ItemMeta renameMeta = renameItem.getItemMeta();
                renameMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.join(" ", args)));
                renameItem.setItemMeta(renameMeta);
                player.sendMessage(messages.getMessage("rename_sucesso"));
                break;

            case "kickall":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.kickall").contains(grupo)
                                && !player.hasPermission("mainpl.utils.kickall")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    if (player != null) sender.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                // Pegar motivo se existir
                String motivoKickAll = "Sem motivo";
                if (args.length > 0) {
                    motivoKickAll = String.join(" ", args);
                }

                Map<String, String> placeholders4 = new HashMap<>();
                placeholders4.put("motivo", motivoKickAll);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (player == null || !p.equals(player)) {
                        String msg = String.join("\n", messages.getMessageListPlaceholder("kickall_mensagem", placeholders4));
                        p.kickPlayer(msg);
                    }
                }

                if (player != null) {
                    String msgSucesso = String.join("\n", messages.getMessageListPlaceholder("kickall_sucesso", placeholders4));
                    sender.sendMessage(msgSucesso);
                }
                break;

            case "kick":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.kick").contains(grupo)
                                && !player.hasPermission("mainpl.utils.kick")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    if (player != null) sender.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage(messages.getMessage("uso_correto", "label", label));
                    return true;
                }

                Player kickTarget = Bukkit.getPlayerExact(args[0]);
                if (kickTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }

                String motivoKick = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Sem motivo";

                Map<String, String> placeholders3 = new HashMap<>();
                placeholders3.put("motivo", motivoKick);
                placeholders3.put("player", kickTarget.getName());

                kickTarget.kickPlayer(String.join("\n", messages.getMessageListPlaceholder("kick_player_mensagem", placeholders3)));
                sender.sendMessage(String.join("\n", messages.getMessageListPlaceholder("kick_player_sucesso", placeholders3)));
                break;


            case "lightning":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.lightning").contains(grupo)
                                && !player.hasPermission("mainpl.utils.lightning")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                if (args.length == 0) { player.sendMessage(messages.getMessage("jogador_nao_encontrado")); return true; }
                Player lightTarget = Bukkit.getPlayer(args[0]);
                if (lightTarget == null) { player.sendMessage(messages.getMessage("jogador_nao_encontrado")); return true; }
                lightTarget.getWorld().strikeLightning(lightTarget.getLocation());
                break;

            case "lightningall":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.lightningall").contains(grupo)
                                && !player.hasPermission("mainpl.utils.lightningall")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                for (Player p : Bukkit.getOnlinePlayers()) p.getWorld().strikeLightning(p.getLocation());
                break;

            case "list":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.list").contains(grupo)
                                && !player.hasPermission("mainpl.utils.list")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                String nicks = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("ninguém");

                int total = Bukkit.getOnlinePlayers().size();

                List<String> listMessages = messages.getMessageList("list_online"); // pega todas as linhas do messages.yml
                for (String line : listMessages) {
                    line = ChatColor.translateAlternateColorCodes('&', line);
                    line = line.replace("{nicks}", nicks);
                    line = line.replace("{total}", String.valueOf(total));
                    player.sendMessage(line);
                }
                break;

            case "repair":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.repair").contains(grupo)
                                && !player.hasPermission("mainpl.utils.repair")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                if (player.getInventory().getItemInMainHand() != null) {
                    player.getInventory().getItemInMainHand().setDurability((short) 0);
                    player.sendMessage(messages.getMessage("repair_sucesso"));
                }
                break;

            case "repairall":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.repairall").contains(grupo)
                                && !player.hasPermission("mainpl.utils.repairall")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    player.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (ItemStack itemRepair : p.getInventory().getContents()) {
                        if (itemRepair != null) itemRepair.setDurability((short) 0);
                    }
                    p.sendMessage(messages.getMessage("repairall_sucesso"));
                }
                break;
            case "unban":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.unban").contains(grupo)
                                && !player.hasPermission("mainpl.utils.unban")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    sender.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                if (args.length == 0) {
                    sender.sendMessage("§cUso correto: /unban <jogador>");
                    return true;
                }

                String input = args[0];
                String keyToRemove = null;

                for (String key : plugin.getBansConfig().getConfigurationSection("tempbans").getKeys(false)) {
                    String nick = plugin.getBansConfig().getString("tempbans." + key + ".nick", "");
                    if (key.equalsIgnoreCase(input) || nick.equalsIgnoreCase(input)) {
                        keyToRemove = key;
                        break;
                    }
                }

                if (keyToRemove == null) {
                    sender.sendMessage("§cO jogador/nick informado não está banido!");
                    return true;
                }


                String targetNick = plugin.getBansConfig().getString("tempbans." + keyToRemove + ".nick", input);


                plugin.getBansConfig().set("tempbans." + keyToRemove, null);
                plugin.saveBansConfig();

                sender.sendMessage("§aO jogador §f" + targetNick + " §afoi desbanido com sucesso!");
                break;

            case "ban":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.ban").contains(grupo)
                                && !player.hasPermission("mainpl.utils.ban")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    sender.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUso correto: /ban <jogador> <motivo>");
                    return true;
                }

                Player banTarget = Bukkit.getPlayer(args[0]);
                String uuidStr = (banTarget != null ? String.valueOf(banTarget.getUniqueId()) : args[0]);
                String nick = (banTarget != null ? banTarget.getName() : args[0]);
                String motivoBan = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                if (banTarget != null) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("motivo", motivoBan);

                    String msg = String.join("\n", messages.getMessageListPlaceholder("kick_permaban", placeholders));
                    banTarget.kickPlayer(msg);
                }


                plugin.getBansConfig().set("tempbans." + uuidStr + ".nick", nick);
                plugin.getBansConfig().set("tempbans." + uuidStr + ".banType", "PLAYER");
                plugin.getBansConfig().set("tempbans." + uuidStr + ".bannedBy", sender.getName());
                plugin.getBansConfig().set("tempbans." + uuidStr + ".unbanAt", Long.MAX_VALUE);
                plugin.getBansConfig().set("tempbans." + uuidStr + ".local", banTarget != null ? banTarget.getWorld().getName() : "Desconhecido");
                plugin.getBansConfig().set("tempbans." + uuidStr + ".motivo", motivoBan);
                plugin.saveBansConfig();

                sender.sendMessage("§aVocê baniu " + nick + " permanentemente pelo motivo: §f" + motivoBan);
                break;

            case "tempban":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.tempban").contains(grupo)
                                && !player.hasPermission("mainpl.utils.tempban")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    sender.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUso correto: /tempban <jogador> <tempo> <motivo>");
                    return true;
                }

                Player tempTarget = Bukkit.getPlayer(args[0]);
                if (tempTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }

                long duration = parseDuration(args[1]); // aceita "10s", "10m", "2h", "1d"
                if (duration <= 0) {
                    sender.sendMessage(messages.getMessage("valor_invalido"));
                    return true;
                }

                long unbanAt = System.currentTimeMillis() + duration;
                String motivoTempBan = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("motivo", motivoTempBan);
                placeholders.put("remaining", args[1]); // ou tempo formatado, se quiser

                String msg = String.join("\n", messages.getMessageListPlaceholder("kick_tempban", placeholders));
                tempTarget.kickPlayer(msg);

                plugin.getBansConfig().set("tempbans." + tempTarget.getUniqueId() + ".nick", tempTarget.getName());
                plugin.getBansConfig().set("tempbans." + tempTarget.getUniqueId() + ".banType", "PLAYER");
                plugin.getBansConfig().set("tempbans." + tempTarget.getUniqueId() + ".bannedBy", sender.getName());
                plugin.getBansConfig().set("tempbans." + tempTarget.getUniqueId() + ".unbanAt", unbanAt);
                plugin.getBansConfig().set("tempbans." + tempTarget.getUniqueId() + ".local", tempTarget.getWorld().getName());
                plugin.getBansConfig().set("tempbans." + tempTarget.getUniqueId() + ".motivo", motivoTempBan);
                plugin.saveBansConfig();

                sender.sendMessage("§aVocê baniu temporariamente " + tempTarget.getName() +
                        " por §f" + args[1] + "§a. Motivo: §f" + motivoTempBan);
                break;

            case "tempbanip":
                if (!isConsole && (
                        !plugin.getConfig().getStringList("utils.tempbanip").contains(grupo)
                                && !player.hasPermission("mainpl.utils.tempbanip")
                                && !player.hasPermission("mainpl.utils.*")
                )) {
                    sender.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUso correto: /tempbanip <jogador> <tempo> <motivo>");
                    return true;
                }

                Player ipTarget = Bukkit.getPlayer(args[0]);
                if (ipTarget == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }

                String motivoIpBan = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                long ipDuration = parseDuration(args[1]);
                if (ipDuration <= 0) {
                    sender.sendMessage(messages.getMessage("valor_invalido"));
                    return true;
                }
                long ipUnbanAt = System.currentTimeMillis() + ipDuration;


                if (ipTarget != null) {
                    Map<String, String> placeholders2 = new HashMap<>();
                    placeholders2.put("motivo", motivoIpBan);
                    placeholders2.put("remaining", args[1]); // ou o tempo formatado

                    String msg2 = String.join("\n", messages.getMessageListPlaceholder("kick_tempban_ip", placeholders2));
                    ipTarget.kickPlayer(msg2);
                }


                String uuid = ipTarget.getUniqueId().toString();
                String ip = ipTarget.getAddress().getAddress().getHostAddress();

                plugin.getBansConfig().set("tempbans." + uuid + ".nick", ipTarget.getName());
                plugin.getBansConfig().set("tempbans." + uuid + ".banType", "IP"); // indica que é ban por IP
                plugin.getBansConfig().set("tempbans." + uuid + ".bannedBy", sender.getName());
                plugin.getBansConfig().set("tempbans." + uuid + ".unbanAt", ipUnbanAt);
                plugin.getBansConfig().set("tempbans." + uuid + ".local", ipTarget.getWorld().getName());
                plugin.getBansConfig().set("tempbans." + uuid + ".motivo", motivoIpBan);
                plugin.getBansConfig().set("tempbans." + uuid + ".ip", ip); // salva o IP aqui
                plugin.saveBansConfig();

                sender.sendMessage("§aVocê baniu temporariamente o jogador " + ipTarget.getName() +
                        " pelo IP (" + ip + ") por §f" + args[1] + "§a. Motivo: §f" + motivoIpBan);
                break;


            case "freeze":
                if (!plugin.getConfig().getStringList("utils.freeze").contains(grupo)
                        && !sender.hasPermission("mainpl.utils.freeze")
                        && !sender.hasPermission("mainpl.utils.*")) {
                    sender.sendMessage(messages.getMessage("sem_permissao"));
                    return true;
                }

                Player target;
                if (args.length > 0) {
                    target = Bukkit.getPlayer(args[0]);
                } else {
                    if (isConsole) {
                        sender.sendMessage("§cUso: /freeze <jogador>");
                        return true;
                    }
                    target = player;
                }
                if (target == null) {
                    sender.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return true;
                }

                UUID targetUUID = target.getUniqueId();


                if (frozenPlayers.contains(targetUUID)) {
                    frozenPlayers.remove(targetUUID);

                    sender.sendMessage(messages.getMessage("freeze_removido", "player", target.getName()));
                    target.sendMessage(messages.getMessage("freeze_desativado"));
                } else {
                    frozenPlayers.add(targetUUID);

                    sender.sendMessage(messages.getMessage("freeze_ativado", "player", target.getName()));
                    target.sendMessage(messages.getMessage("freeze_ativado_target"));
                }


                saveFrozen();
                break;
            case "ajuda":
                int page = 1;
                if (args.length >= 1) {
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cNúmero de página inválido!");
                        return true;
                    }
                }

                AjudaManager ajuda = new AjudaManager(plugin);
                int maxPage = ajuda.getMaxPage();

                if (page < 1 || page > maxPage) {
                    sender.sendMessage("§cPágina inexistente! Use entre 1 e " + maxPage);
                    return true;
                }

                sender.sendMessage(ajuda.getTitle() + " §7(" + page + "/" + maxPage + ")");
                for (String line : ajuda.getPage(page)) {
                    sender.sendMessage(line);
                }
                break;
        }

        return true;
    }


    private void saveFrozen() {

        Map<String, String> map = new HashMap<>();
        for (UUID uuid : frozenPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                map.put(uuid.toString(), player.getName());
            } else {

                map.put(uuid.toString(), freezeConfig.getString("frozen." + uuid.toString(), "Desconhecido"));
            }
        }

        freezeConfig.set("frozen", map);

        try {
            freezeConfig.save(freezeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String uuid = event.getUniqueId().toString();

        if (plugin.getBansConfig().contains("tempbans." + uuid)) {
            long unbanAt = plugin.getBansConfig().getLong("tempbans." + uuid + ".unbanAt");
            String motivo = plugin.getBansConfig().getString("tempbans." + uuid + ".motivo", "Sem motivo");

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("motivo", motivo);

            if (unbanAt == Long.MAX_VALUE) {

                String msg = String.join("\n", messages.getMessageListPlaceholder("banmsg_permanente", placeholders));
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, msg);
                return;
            }

            if (System.currentTimeMillis() < unbanAt) {

                long restante = unbanAt - System.currentTimeMillis();
                String tempoRestante = formatarTempo(restante);
                placeholders.put("remaining", tempoRestante);

                String msg = String.join("\n", messages.getMessageListPlaceholder("banmsg_temporario", placeholders));
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, msg);
            } else {

                plugin.getBansConfig().set("tempbans." + uuid, null);
                plugin.saveBansConfig();
            }
        }
    }


    private String formatarTempo(long millis) {
        long segundos = millis / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        long dias = horas / 24;

        segundos %= 60;
        minutos %= 60;
        horas %= 24;

        StringBuilder sb = new StringBuilder();
        if (dias > 0) sb.append(dias).append("d ");
        if (horas > 0) sb.append(horas).append("h ");
        if (minutos > 0) sb.append(minutos).append("m ");
        if (segundos > 0) sb.append(segundos).append("s");

        return sb.toString().trim();
    }




    // Bloquear movimento
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (frozenPlayers.contains(player.getUniqueId())) {

            event.setTo(event.getFrom());
        }
    }
    public Material getMaterialByID(int id) {
        String key = String.valueOf(id);
        if (!config.contains(key)) return null;

        List<String> values = config.getStringList(key);
        if (values.isEmpty()) return null;

        String matEnglish = values.get(0);
        return Material.matchMaterial(matEnglish.toUpperCase());
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (frozenPlayers.contains(player.getUniqueId())) {

            String msg = "§cVocê está congelado e não pode executar comandos!";
            player.sendMessage(msg);
            event.setCancelled(true);
        }
    }









    public long parseDuration(String input) {
        try {
            long time = 0;
            String number = input.replaceAll("[^0-9]", "");
            String unit = input.replaceAll("[0-9]", "").toLowerCase();

            long value = Long.parseLong(number);

            switch (unit) {
                case "s":
                    time = value * 1000L;
                    break;
                case "m":
                    time = value * 60 * 1000L;
                    break;
                case "h":
                    time = value * 60 * 60 * 1000L;
                    break;
                case "d":
                    time = value * 24 * 60 * 60 * 1000L;
                    break;
                default:
                    time = -1;
            }

            return time;
        } catch (NumberFormatException e) {
            return -1;
        }
    }





}
