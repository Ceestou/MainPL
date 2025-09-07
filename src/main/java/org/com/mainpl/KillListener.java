package org.com.mainpl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class KillListener implements Listener {

    private final MainPL plugin;


    private final Map<String, Map<String, Long>> lastKills = new HashMap<>();


    private final long cooldown;

    public KillListener(MainPL plugin) {
        this.plugin = plugin;


        String delayConfig = plugin.getConfig().getString("delay_kill", "30s");
        this.cooldown = parseTimeToMillis(delayConfig);
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();


        plugin.getRankingManager().addDeath(victim.getName());

        if (killer == null || killer == victim) return;

        long now = System.currentTimeMillis();
        Map<String, Long> killsByKiller = lastKills.computeIfAbsent(killer.getName(), k -> new HashMap<>());
        Long lastKillTime = killsByKiller.get(victim.getName());

        if (lastKillTime == null || now - lastKillTime > cooldown) {

            plugin.getRankingManager().addKill(killer.getName());
            killsByKiller.put(victim.getName(), now);
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
