package org.com.mainpl;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BauSManager {

    private final MainPL plugin;
    private final File file;
    private final YamlConfiguration config;

    public BauSManager(MainPL plugin) {
        this.plugin = plugin;

        File pasta = new File(plugin.getDataFolder(), "Data");
        if (!pasta.exists()) pasta.mkdirs();

        this.file = new File(pasta, "superbaus.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public Inventory getBau(String dono) {
        Inventory inv = Bukkit.createInventory(null, 27, "§fBaú de " + dono);
        List<ItemStack> items = (List<ItemStack>) config.getList(dono + ".itens");

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                inv.setItem(i, items.get(i));
            }
        }

        return inv;
    }

    public void saveBau(String dono, Inventory inv) {
        config.set(dono + ".itens", null);
        config.set(dono + ".itens", Arrays.asList(inv.getContents()));
        save();
    }

    public boolean addConfiavel(String dono, String confiavel) {
        List<String> confiaveis = getConfiaveis(dono);

        if (confiaveis.contains(confiavel)) return false;
        if (confiaveis.size() >= 7) return false;

        confiaveis.add(confiavel);
        config.set(dono + ".confiaveis", confiaveis);
        save();
        return true;
    }

    public boolean isConfiavel(String dono, String jogador) {
        List<String> confiaveis = getConfiaveis(dono);
        return confiaveis.contains(jogador);
    }

    public List<String> getConfiaveis(String dono) {
        List<String> confiaveis = config.getStringList(dono + ".confiaveis");
        return confiaveis != null ? confiaveis : new ArrayList<>();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isApoiador(String playerName) {
        UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();


        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            if (onlinePlayer.hasPermission("mainpl.comandos.*") ||
                    onlinePlayer.hasPermission("mainpl.comandos.sbau")) {
                return true;
            }
        }


        LuckPerms lp = LuckPermsProvider.get();
        User user = lp.getUserManager().loadUser(uuid).join();
        if (user == null) return false;

        String group = user.getPrimaryGroup().toLowerCase();
        var config = plugin.getConfig();


        if (config.isConfigurationSection("sbau") && config.contains("sbau." + group)) {
            return config.getBoolean("sbau." + group, false);
        }


        return config.getBoolean("mainpl.sbau", false);
    }



    public boolean removerConfiavel(String dono, String confiavel) {
        List<String> confiaveis = getConfiaveis(dono);
        boolean removido = confiaveis.removeIf(n -> n.equalsIgnoreCase(confiavel));

        if (removido) {
            config.set(dono + ".confiaveis", confiaveis);
            save();
            return true;
        }

        return false;
    }

}
