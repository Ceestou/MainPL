package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpawnListener implements Listener {

    private final CommandSpawn commandSpawn;
    private final MainPL plugin;

    public SpawnListener(MainPL plugin, CommandSpawn commandSpawn) {
        this.plugin = plugin;
        this.commandSpawn = commandSpawn;
    }


    private Location getSpawn() {
        var spawnConfig = commandSpawn.getSpawnConfig();
        if (!spawnConfig.contains("spawn")) return null;

        World world = Bukkit.getWorld(spawnConfig.getString("spawn.world"));
        if (world == null) return null;

        return new Location(
                world,
                spawnConfig.getDouble("spawn.x"),
                spawnConfig.getDouble("spawn.y"),
                spawnConfig.getDouble("spawn.z"),
                (float) spawnConfig.getDouble("spawn.yaw"),
                (float) spawnConfig.getDouble("spawn.pitch")
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("spawn.join-spawn")) return;

        Location spawn = getSpawn();
        if (spawn != null) e.getPlayer().teleport(spawn);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!plugin.getConfig().getBoolean("spawn.death-spawn")) return;

        Location spawn = getSpawn();
        if (spawn != null) e.setRespawnLocation(spawn);
    }
}
