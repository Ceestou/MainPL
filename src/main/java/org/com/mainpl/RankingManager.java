package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RankingManager {

    private final MainPL plugin;
    private final MessageManager messages;
    private final File file;
    private final YamlConfiguration config;

    public final int TOP_LIMIT;
    private final Map<String, PlayerStats> playerStatsCache = new HashMap<>();
    private List<String> previousTop = new ArrayList<>();



    public RankingManager(MainPL plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.TOP_LIMIT = plugin.getConfig().getInt("TOP_LIMIT", 3);

        File dataFolder = new File(plugin.getDataFolder(), "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        file = new File(dataFolder, "playerKills.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }






        config = YamlConfiguration.loadConfiguration(file);
        loadStats();
        updateAllDisplayNames();
        loadPreviousTop();
    }
    public LinkedHashMap<String, PlayerStats> getTopPlayersWithStats(int limit) {
        List<Map.Entry<String, PlayerStats>> sorted = new ArrayList<>(playerStatsCache.entrySet());
        sorted.sort((a, b) -> {
            int cmp = Double.compare(b.getValue().kdr, a.getValue().kdr);
            return cmp != 0 ? cmp : Integer.compare(b.getValue().kills, a.getValue().kills);
        });

        LinkedHashMap<String, PlayerStats> top = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(limit, sorted.size()); i++) {
            String key = normalizeName(sorted.get(i).getKey());
            top.put(key, sorted.get(i).getValue());
        }
        return top;
    }


    public static class PlayerStats {
        int kills;
        int deaths;
        double kdr;

        public PlayerStats(int kills, int deaths) {
            this.kills = kills;
            this.deaths = deaths;
            this.kdr = deaths == 0 ? kills : ((double) kills) / deaths;
        }

        public void addKill() { kills++; updateKDR(); }
        public void addDeath() { deaths++; updateKDR(); }
        private void updateKDR() { kdr = deaths == 0 ? kills : ((double) kills) / deaths; }
    }

    private String normalizeName(String name) { return name.startsWith(".") ? name.substring(1) : name; }

    public void addKill(String player) { updateStats(player, true); }
    public void addDeath(String player) { updateStats(player, false); }

    private void updateStats(String player, boolean kill) {
        String key = normalizeName(player);
        PlayerStats stats = playerStatsCache.getOrDefault(key, new PlayerStats(0, 0));
        if (kill) stats.addKill(); else stats.addDeath();
        playerStatsCache.put(key, stats);
        save();
        updateTopRanking();
    }


    private void loadStats() {
        for (String key : config.getKeys(false)) {
            if (key.equals("previousTop")) continue;
            int kills = config.getInt(key + ".kills", 0);
            int deaths = config.getInt(key + ".deaths", 0);
            playerStatsCache.put(normalizeName(key), new PlayerStats(kills, deaths));
        }
    }


    private void save() {
        for (Map.Entry<String, PlayerStats> entry : playerStatsCache.entrySet()) {
            String player = entry.getKey();
            PlayerStats stats = entry.getValue();
            config.set(player + ".kills", stats.kills);
            config.set(player + ".deaths", stats.deaths);
            config.set(player + ".kdr", stats.kdr);
            config.set("previousTop", previousTop);
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    private void updateTopRanking() {
        List<Map.Entry<String, PlayerStats>> sorted = new ArrayList<>(playerStatsCache.entrySet());
        sorted.sort((a, b) -> {
            int cmp = Double.compare(b.getValue().kdr, a.getValue().kdr);
            return cmp != 0 ? cmp : Integer.compare(b.getValue().kills, a.getValue().kills);
        });

        List<String> newTop = new ArrayList<>();
        for (int i = 0; i < Math.min(TOP_LIMIT, sorted.size()); i++) newTop.add(normalizeName(sorted.get(i).getKey()));

        for (int i = 0; i < newTop.size(); i++) {
            String key = newTop.get(i);
            Player p = Bukkit.getPlayerExact(key);
            if (p == null) {
                p = Bukkit.getOnlinePlayers()
                        .stream().filter(pl -> normalizeName(pl.getName()).equalsIgnoreCase(key))
                        .findFirst().orElse(null);
            }

            if (p != null) {
                p.setDisplayName(getDisplayTag(i + 1) + p.getName());
                int oldPos = previousTop.indexOf(key);
                int newPos = i;

                if (oldPos == -1) {
                    enviarTopAviso("top_entrou", p, newPos);
                } else if (newPos < oldPos) {
                    enviarTopAviso("top_subiu", p, newPos);
                }
            }
        }

        for (String oldKey : previousTop) {
            Player p = Bukkit.getPlayerExact(oldKey);
            if (p == null) {
                p = Bukkit.getOnlinePlayers()
                        .stream().filter(pl -> normalizeName(pl.getName()).equalsIgnoreCase(oldKey))
                        .findFirst().orElse(null);
            }
            if (p != null) {

                List<String> rankingCompleto = getTopPlayers(playerStatsCache.size());
                int novaPos = rankingCompleto.indexOf(oldKey);

                if (novaPos != -1 && novaPos < TOP_LIMIT) {

                    p.setDisplayName(getDisplayTag(novaPos + 1) + p.getName());
                } else {

                    String tagPadrao = messages.getMessage("top_tag_padrao", "");
                    p.setDisplayName(tagPadrao + p.getName());
                }
            }
        }


        previousTop = new ArrayList<>(newTop);
        save();
    }

    public String getTagDoJogador(Player player) {
        if (!estaNoTop7(player)) {

            return messages.getMessage("top_tag_padrao", "");
        }

        int posicao = getPosicao(player);

        return messages.getMessage("top_" + posicao, "");
    }



    private void enviarTopAviso(String tipoMensagem, Player player, int pos) {
        // pega o config do plugin
        FileConfiguration cfg = plugin.getConfig();
        boolean titulo = cfg.getBoolean("topaviso.titulo");
        boolean subtitulo = cfg.getBoolean("topaviso.subtitulo");
        boolean chat = cfg.getBoolean("topaviso.chat");
        boolean actionbar = cfg.getBoolean("topaviso.actionbar");


        String msg = messages.getMessage(tipoMensagem,
                "player", player.getName(),
                "pos", getOrdinal(pos + 1));

        if (titulo) plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "br t " + msg);
        if (subtitulo) plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "br s " + msg);
        if (actionbar) plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "br a " + msg);
        if (chat) plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "br " + msg);
    }


    private void loadPreviousTop() {
        previousTop.clear();
        List<String> savedTop = config.getStringList("previousTop");
        if (savedTop != null) previousTop.addAll(savedTop);
    }

    public boolean estaNoTop7(Player player) {
        String key = normalizeName(player.getName());
        return getTopPlayers(TOP_LIMIT).contains(key);
    }

    public int getPosicao(Player player) {
        String key = normalizeName(player.getName());
        List<String> sorted = getTopPlayers(playerStatsCache.size());
        return sorted.indexOf(key) + 1;
    }

    private List<String> getTopPlayers(int limit) {
        List<Map.Entry<String, PlayerStats>> sorted = new ArrayList<>(playerStatsCache.entrySet());
        sorted.sort((a, b) -> {
            int cmp = Double.compare(b.getValue().kdr, a.getValue().kdr);
            return cmp != 0 ? cmp : Integer.compare(b.getValue().kills, a.getValue().kills);
        });
        List<String> list = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, sorted.size()); i++) list.add(normalizeName(sorted.get(i).getKey()));
        return list;
    }

    public void aplicarTagRanking(Player player) {
        String key = normalizeName(player.getName());


        List<String> rankingCompleto = getTopPlayers(playerStatsCache.size());
        int posicao = rankingCompleto.indexOf(key);

        if (posicao != -1 && posicao < TOP_LIMIT) {

            player.setDisplayName(getDisplayTag(posicao + 1) + player.getName());
        } else {

            String tagPadrao = messages.getMessage("top_tag_padrao", "");
            player.setDisplayName(tagPadrao + player.getName());
        }
    }


    public void updateAllDisplayNames() {
        for (Player p : Bukkit.getOnlinePlayers()) aplicarTagRanking(p);
    }

    private String getDisplayTag(int position) {

        String tag = messages.getMessage("top_" + position);
        return tag != null ? tag : "";
    }

    private String getOrdinal(int position) {

        String ordinal = messages.getMessage("ordinal_" + position);


        if (ordinal == null || ordinal.isEmpty()) {
            return String.valueOf(position);
        }

        return ordinal;
    }

}
