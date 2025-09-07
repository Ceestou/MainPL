package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ConquistaListener implements Listener {

    private final JavaPlugin plugin;

    public ConquistaListener(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent e) {
        boolean desabilitar = plugin.getConfig().getBoolean("desabilitar-conquistas", false);

        if (desabilitar) {
            Player player = e.getPlayer();


            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.equals(player)) {
                        p.resetTitle();
                    }
                }
            }, 1L);
        }
    }
}
