package org.com.mainpl;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class PlaceholderListener implements Listener {

    private final MainPL plugin;

    public PlaceholderListener(MainPL plugin) {
        this.plugin = plugin;
    }


    private String parsePlaceholders(Player player, String message) {
        if (message == null) return "";

        if (message.contains("{tag_top_k}")) {
            String tag = plugin.getRankingManager().getTagDoJogador(player);
            message = message.replace("{tag_top_k}", tag != null ? tag : "§7Jogador");
        }

        message = message.replace("{player}", player.getName());
        return message;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String format = event.getFormat();
        format = parsePlaceholders(player, format);
        event.setFormat(format);
    }
}
