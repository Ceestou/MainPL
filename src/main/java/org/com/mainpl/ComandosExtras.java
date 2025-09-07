package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComandosExtras implements CommandExecutor {

    private final MainPL plugin;
    private final MessageManager messages;

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public ComandosExtras(MainPL plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getMessage("somente_jogadores"));
            return true;
        }

        boolean isConsole = !(sender instanceof Player);

        String cmdName = command.getName().toLowerCase();
        String grupo = plugin.getPrimaryGroup(player).toLowerCase();

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));

        if (!isConsole) {
            if (!cfg.isConfigurationSection("comandos." + cmdName)
                    || !cfg.getConfigurationSection("comandos." + cmdName).getKeys(false).contains(grupo)
                    && !player.hasPermission("mainpl.comandos." + cmdName)
                    && !player.hasPermission("mainpl.comandos.*")) {

                player.sendMessage(messages.getMessage("sem_permissao"));
                return true;
            }
        }


        int delay = cfg.getInt("comandos." + cmdName + "." + grupo + ".delay_seconds", 0);
        int cooldown = cfg.getInt("comandos." + cmdName + "." + grupo + ".cooldown_seconds", 5);

        cooldowns.putIfAbsent(player.getUniqueId(), new HashMap<>());
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        long now = System.currentTimeMillis();
        if (playerCooldowns.containsKey(cmdName)) {
            long nextUse = playerCooldowns.get(cmdName);
            if (now < nextUse) {
                long remaining = (nextUse - now) / 1000;
                player.sendMessage(messages.getMessage("cooldown_comando", "seconds", String.valueOf(remaining)));
                return true;
            }
        }

        playerCooldowns.put(cmdName, now + cooldown * 1000L);

        if (delay <= 0) {
            executarComando(player, cmdName, args);
            return true;
        }

        new BukkitRunnable() {
            int segundos = delay;

            @Override
            public void run() {
                if (segundos <= 0) {
                    executarComando(player, cmdName, args);
                    cancel();
                    return;
                }
                player.sendMessage(messages.getMessage("comando_delay", "seconds", String.valueOf(segundos)));
                segundos--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }

    private void executarComando(Player player, String cmdName, String[] args) {
        switch (cmdName) {
            case "craft":
                player.openWorkbench(null, true);
                player.sendMessage(messages.getMessage("craft_sucesso"));
                break;

            case "lixo":
                Inventory lixo = Bukkit.createInventory(player, 27, "Lixo");
                player.openInventory(lixo);
                player.sendMessage(messages.getMessage("lixo_aberto"));
                break;

            case "fornalha":
                player.openInventory(Bukkit.createInventory(player, InventoryType.FURNACE, "Fornalha"));
                player.sendMessage(messages.getMessage("fornalha_sucesso"));
                break;

            case "hat":
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand != null && hand.getType() != Material.AIR) {
                    ItemStack hatItem = hand.clone();
                    hatItem.setAmount(1);
                    player.getInventory().setHelmet(hatItem);


                    hand.setAmount(hand.getAmount() - 1);
                    if (hand.getAmount() <= 0) {
                        player.getInventory().setItemInMainHand(null);
                    } else {
                        player.getInventory().setItemInMainHand(hand);
                    }

                    player.sendMessage(messages.getMessage("hat_sucesso"));
                } else {
                    player.sendMessage(messages.getMessage("hat_vazio"));
                }
                break;

            case "invsee":
                if (args.length == 0) {
                    player.sendMessage(messages.getMessage("invsee_uso"));
                    return;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(messages.getMessage("jogador_nao_encontrado"));
                    return;
                }
                player.openInventory(target.getInventory());
                player.sendMessage(messages.getMessage("invsee_sucesso", "player", target.getName()));
                break;
            case "luz":

                if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    player.sendMessage(messages.getMessage("luz_desligada"));
                } else {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.NIGHT_VISION,
                            Integer.MAX_VALUE,
                            0,
                            true,
                            false,
                            false
                    ));
                    player.sendMessage(messages.getMessage("luz_ligada"));
                }
                break;
            case "anvil":
                Inventory anvil = Bukkit.createInventory(player, InventoryType.ANVIL, "Bigorna");
                player.openInventory(anvil);
                player.sendMessage(messages.getMessage("anvil_aberto"));
                break;



        }
    }
}
