package org.com.mainpl;


import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class WorldCommandBlocker implements Listener {

    private final File worldsFile;
    private YamlConfiguration worldsConfig;

    public WorldCommandBlocker(MainPL plugin) {
        this.worldsFile = new File(plugin.getDataFolder(), "worlds_block.yml");

        if (!worldsFile.exists()) {
            try {
                worldsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);

        if (this.worldsConfig.getKeys(false).isEmpty()) {
            this.worldsConfig.set("world", Arrays.asList("voar", "gm3"));
            this.worldsConfig.set("world_nether", Arrays.asList("commandexemple", "gm2"));
            saveConfig();
        }


    }
    public void setWorldConfig(YamlConfiguration newConfig) {
        worldsConfig = newConfig;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        World world = player.getWorld();
        String command = e.getMessage().split(" ")[0].substring(1).toLowerCase(); // remove o /

        List<String> blockedCommands = worldsConfig.getStringList(world.getName());

        if (blockedCommands != null && blockedCommands.contains(command)) {
            e.setCancelled(true);
            player.sendMessage("§cVocê não pode usar esse comando neste mundo!");
        }
    }


    public void saveConfig() {
        try {
            worldsConfig.save(worldsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
