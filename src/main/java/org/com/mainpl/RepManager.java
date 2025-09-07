package org.com.mainpl;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RepManager {
    private final MainPL plugin;
    private File file;
    private YamlConfiguration yml;

    public RepManager(MainPL plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "reps.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        yml = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getReps(String staffer) {
        return yml.getInt(staffer + ".total", 0);
    }

    public Map<String, Integer> getGiven(String staffer) {
        Map<String, Integer> map = new HashMap<>();
        if (yml.getConfigurationSection(staffer + ".given") == null) return map;
        for (String giver : yml.getConfigurationSection(staffer + ".given").getKeys(false)) {
            map.put(giver, yml.getInt(staffer + ".given." + giver, 0));
        }
        return map;
    }

    public void addRep(String staffer, String giver) {
        int total = getReps(staffer) + 1;
        yml.set(staffer + ".total", total);
        int given = yml.getInt(staffer + ".given." + giver, 0) + 1;
        yml.set(staffer + ".given." + giver, given);
        save();
    }
}
