package org.com.mainpl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RankingKillsCommand implements CommandExecutor {

    private final RankingManager rankingManager;
    private final MessageManager messages;

    public RankingKillsCommand(RankingManager rankingManager, MessageManager messages) {
        this.rankingManager = rankingManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LinkedHashMap<String, RankingManager.PlayerStats> topPlayers = rankingManager.getTopPlayersWithStats(7);

        if (topPlayers.isEmpty()) {
            sender.sendMessage(messages.getMessage("ranking_vazio"));
            return true;
        }


        List<String> titulo = messages.getMessageList("ranking_titulo");
        for (String line : titulo) {
            sender.sendMessage(line);
        }

        int pos = 1;
        for (Map.Entry<String, RankingManager.PlayerStats> entry : topPlayers.entrySet()) {
            RankingManager.PlayerStats stats = entry.getValue();


            List<String> linhas = messages.getMessageList("ranking_linha");

            for (String linha : linhas) {
                linha = linha.replace("{pos}", String.valueOf(pos))
                        .replace("{player}", entry.getKey())
                        .replace("{kills}", String.valueOf(stats.kills))
                        .replace("{deaths}", String.valueOf(stats.deaths))
                        .replace("{kdr}", String.format("%.2f", stats.kdr))
                        .replace("{cor_pos}", getCorPosicao(pos))
                        .replace("{emoji}", getEmojiPosicao(pos));

                sender.sendMessage(linha);
            }
            pos++;
        }


        List<String> rodape = messages.getMessageList("ranking_rodape");
        for (String line : rodape) {
            sender.sendMessage(line);
        }

        return true;
    }

    private String getCorPosicao(int pos) {
        if (pos <= rankingManager.TOP_LIMIT) {
            return switch (pos) {
                case 1 -> messages.getMessage("top_1_cor", "§6");
                case 2 -> messages.getMessage("top_2_cor", "§7");
                case 3 -> messages.getMessage("top_3_cor", "§8");
                default -> messages.getMessage("top_padrao_cor", "§f");
            };
        } else {
            return messages.getMessage("top_padrao_cor", "§f");
        }
    }

    private String getEmojiPosicao(int pos) {
        if (pos <= rankingManager.TOP_LIMIT) {
            return switch (pos) {
                case 1 -> messages.getMessage("top_1_icon", "🏆");
                case 2 -> messages.getMessage("top_2_icon", "🥈");
                case 3 -> messages.getMessage("top_3_icon", "🥉");
                default -> messages.getMessage("top_padrao_emoji", "•");
            };
        } else {
            return messages.getMessage("top_padrao_emoji", "•");
        }
    }

}
