package org.com.mainpl;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.UUID;

public class BauListener implements Listener {

    private final MainPL plugin;


    public BauListener(MainPL plugin) {
        this.plugin = plugin;
    }


    private boolean isSuperBau(Player player) {
        if (player.getOpenInventory() == null) return false;
        String title = player.getOpenInventory().getTitle();
        return title != null && title.startsWith("§6Baú de ");
    }


    private String getDono(Player player) {
        String title = player.getOpenInventory().getTitle();
        return ChatColor.stripColor(title.replace("Baú de ", ""));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        InventoryView view = event.getView();
        String title = ChatColor.stripColor(view.getTitle());



        if (title.startsWith("Baú de ")) {
            String dono = title.replace("Baú de ", "");



            plugin.getBauManager().saveBau(dono, event.getInventory());

            HashMap<String, UUID> bausAbertos = plugin.getBauCommand().getBausAbertos();


            UUID uuidAbrindo = bausAbertos.get(dono);
            if (uuidAbrindo != null && uuidAbrindo.equals(player.getUniqueId())) {
                bausAbertos.remove(dono);
                player.sendMessage(ChatColor.YELLOW + "Você fechou seu baú.");

            }


        }
    }



    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (isSuperBau(player)) {
            String dono = getDono(player);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getBauManager().saveBau(dono, event.getInventory());
            }, 1L);
        }
    }


    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (isSuperBau(player)) {
            String dono = getDono(player);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getBauManager().saveBau(dono, event.getInventory());
            }, 1L);
        }
    }


}
