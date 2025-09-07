package org.com.mainpl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class DeathListener implements Listener {

    private final MainPL plugin;


    private final Map<String, Map<String, Long>> lastDeaths = new HashMap<>();


    private final long cooldown;

    public DeathListener(MainPL plugin) {
        this.plugin = plugin;


        String delayConfig = plugin.getConfig().getString("delay_kill", "30s");
        this.cooldown = parseTimeToMillis(delayConfig);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        Player killer = dead.getKiller();


        plugin.getRankingManager().addDeath(dead.getName());

        if (killer == null || killer == dead) return;

        long now = System.currentTimeMillis();
        Map<String, Long> deathsByDead = lastDeaths.computeIfAbsent(dead.getName(), k -> new HashMap<>());
        Long lastDeathTime = deathsByDead.get(killer.getName());

        if (lastDeathTime == null || now - lastDeathTime > cooldown) {

            deathsByDead.put(killer.getName(), now);
        } else {

        }
    }


    private long parseTimeToMillis(String input) {
        try {
            input = input.toLowerCase().trim();
            if (input.endsWith("s")) {
                return Long.parseLong(input.replace("s", "")) * 1000;
            } else if (input.endsWith("m")) {
                return Long.parseLong(input.replace("m", "")) * 60 * 1000;
            } else {

                return Long.parseLong(input) * 1000;
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("delay_kill inválido no config.yml, usando 30s padrão!");
            return 30 * 1000;
        }
    }
}
