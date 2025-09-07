package org.com.mainpl;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.util.*;

public class EnchantAliases {

    private final MainPL plugin;
    private final Map<String, Enchantment> aliasToEnchant = new HashMap<>();
    private YamlConfiguration yml;

    public EnchantAliases(MainPL plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        aliasToEnchant.clear();


        for (Enchantment e : Enchantment.values()) {
            if (e == null) continue;
            String key = e.getKey().getKey();
            String vanillaName = e.getName();
            aliasToEnchant.put(key.toLowerCase(Locale.ROOT), e);
            aliasToEnchant.put(vanillaName.toLowerCase(Locale.ROOT), e);
        }


        File f = new File(plugin.getDataFolder(), "enchantments.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);

        for (String path : yml.getKeys(false)) {
            String namespacedGuess = path.toLowerCase(Locale.ROOT)
                    .replace(' ', '_')
                    .replace('-', '_');
            Enchantment ench = findByAny(namespacedGuess);
            if (ench == null) continue;

            List<String> aliases = yml.getStringList(path);
            for (String a : aliases) {
                aliasToEnchant.put(a.toLowerCase(Locale.ROOT), ench);
            }
        }
    }

    public void setEnchantConfig(YamlConfiguration newConfig) {
        this.yml = newConfig;
        reload();
    }

    private Enchantment findByAny(String s) {

        Enchantment byKey = Enchantment.getByKey(NamespacedKey.minecraft(s));
        if (byKey != null) return byKey;
        Enchantment byName = Enchantment.getByName(s.toUpperCase(Locale.ROOT));
        return byName;
    }

    public Enchantment fromAlias(String alias) {
        if (alias == null) return null;
        return aliasToEnchant.get(alias.toLowerCase(Locale.ROOT));
    }
}
