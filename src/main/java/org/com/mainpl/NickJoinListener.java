package org.com.mainpl;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NickJoinListener implements Listener {

    private final MainPL plugin;

    public NickJoinListener(MainPL plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.aplicarDisplayName(event.getPlayer());
    }

}
