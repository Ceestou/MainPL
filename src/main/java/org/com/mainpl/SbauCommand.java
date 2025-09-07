package org.com.mainpl;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.LuckPermsProvider;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.UUID;

public class SbauCommand implements CommandExecutor, Listener {

    private final MainPL plugin;
    private final ModulosManager modulosManager;
    private final HashMap<String, UUID> bausAbertos = new HashMap<>();
    private final MessageManager messages;

    public SbauCommand(MainPL plugin) {
        this.plugin = plugin;
        this.modulosManager = plugin.getModulosManager();
        this.messages = new MessageManager(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!modulosManager.isEnabled("BauManager")) {
            if (sender instanceof Player player) {
                player.sendMessage(messages.getMessage("bau_modulo_desativado"));
            } else {
                sender.sendMessage(messages.getMessage("bau_modulo_desativado"));
            }
            return true;
        }

        if (!(sender instanceof Player player)) return false;

        String dono = player.getName();

        if (!canUseSbau(player)) {
            player.sendMessage(messages.getMessage("bau_apoiador_somente"));
            return true;
        }

        Inventory bau = plugin.getBauManager().getBau(dono);
        if (bau == null) {
            player.sendMessage(messages.getMessage("bau_nao_encontrado"));
            return true;
        }

        abrirBau(player, dono, bau);
        return true;
    }

    private void abrirBau(Player player, String dono, Inventory bau) {
        bausAbertos.put(dono, player.getUniqueId());
        player.openInventory(bau);
        player.sendMessage(messages.getMessage("bau_aberto"));
    }


    private boolean canUseSbau(Player player) {

        if (player.hasPermission("mainpl.comandos.*") || player.hasPermission("mainpl.comandos.sbau")) {
            return true;
        }


        LuckPerms lp = LuckPermsProvider.get();
        User user = lp.getUserManager().getUser(player.getUniqueId());
        if (user == null) return false;

        String group = user.getPrimaryGroup().toLowerCase();
        var config = plugin.getConfig();


        if (config.isConfigurationSection("sbau") && config.contains("sbau." + group)) {
            return config.getBoolean("sbau." + group, false);
        }


        return config.getBoolean("mainpl.sbau", false);
    }


    public HashMap<String, UUID> getBausAbertos() {
        return bausAbertos;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        bausAbertos.values().remove(player.getUniqueId());
    }

    public void onCloseInventory(Player player, InventoryView view) {
        String title = view.getTitle();
        if (title != null && title.startsWith("§fBaú de ")) {
            String dono = ChatColor.stripColor(title.replace("Baú de ", ""));
            UUID atual = bausAbertos.get(dono);
            if (atual != null && atual.equals(player.getUniqueId())) {
                bausAbertos.remove(dono);
                player.sendMessage(messages.getMessage("bau_fechado"));
            }
        }
    }
}
