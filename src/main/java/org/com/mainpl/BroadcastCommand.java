package org.com.mainpl;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BroadcastCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Uso: /br <chat|title|subtitle|actionbar> <nick|all> <tempo>s <mensagem>");
            sender.sendMessage(ChatColor.RED + "Ou use atalhos: /br t <mensagem> (title), /br s <mensagem> (subtitle), /br a <mensagem> (actionbar)");
            sender.sendMessage(ChatColor.RED + "Ou: /br <mensagem> (envia chat para todos)");
            return true;
        }


        if (args.length >= 4 && (
                args[0].equalsIgnoreCase("chat") ||
                        args[0].equalsIgnoreCase("title") ||
                        args[0].equalsIgnoreCase("subtitle") ||
                        args[0].equalsIgnoreCase("actionbar"))) {

            String tipo = args[0].toLowerCase();
            String alvo = args[1];
            String tempoRaw = args[2];

            StringBuilder mensagemBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                mensagemBuilder.append(args[i]).append(" ");
            }

            String mensagem = mensagemBuilder.toString().trim();

            if (!tempoRaw.endsWith("s")) {
                sender.sendMessage(ChatColor.RED + "Tempo inválido. Use formato como '5s'.");
                return true;
            }

            int tempo;
            try {
                tempo = Integer.parseInt(tempoRaw.substring(0, tempoRaw.length() - 1));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Tempo inválido.");
                return true;
            }

            if (alvo.equalsIgnoreCase("all")) {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    enviar(tipo, target, mensagem, tempo);
                }
            } else {
                Player target = Bukkit.getPlayerExact(alvo);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Jogador não encontrado.");
                    return true;
                }
                enviar(tipo, target, mensagem, tempo);
            }

            return true;
        }


        if (args.length >= 2 && (
                args[0].equalsIgnoreCase("t") ||
                        args[0].equalsIgnoreCase("s") ||
                        args[0].equalsIgnoreCase("a"))) {

            String tipoCurto = args[0].toLowerCase();
            StringBuilder mensagemBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                mensagemBuilder.append(args[i]).append(" ");
            }

            String mensagem = ChatColor.translateAlternateColorCodes('&', mensagemBuilder.toString().trim());

            for (Player p : Bukkit.getOnlinePlayers()) {
                switch (tipoCurto) {
                    case "t" -> p.sendTitle(mensagem, "", 10, 5 * 20, 10);
                    case "s" -> p.sendTitle("", mensagem, 10, 5 * 20, 10);
                    case "a" -> p.spigot().sendMessage(
                            net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            new net.md_5.bungee.api.chat.TextComponent(mensagem)
                    );
                }
            }
            return true;
        }

        StringBuilder mensagemBuilder = new StringBuilder();
        for (String arg : args) {
            mensagemBuilder.append(arg).append(" ");
        }
        String mensagem = ChatColor.translateAlternateColorCodes('&', mensagemBuilder.toString().trim());

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(mensagem);
        }

        return true;
    }


    private void enviar(String tipo, Player target, String mensagem, int tempo) {
        String parsed = ChatColor.translateAlternateColorCodes('&', mensagem.replace("$player", target.getName()));

        switch (tipo) {
            case "chat":
                target.sendMessage(parsed);
                break;
            case "title":
                target.sendTitle(parsed, "", 10, tempo * 20, 10);
                break;
            case "subtitle":
                target.sendTitle("", parsed, 10, tempo * 20, 10);
                break;
            case "actionbar":
                target.spigot().sendMessage(
                        net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        new net.md_5.bungee.api.chat.TextComponent(parsed)
                );
                break;
            default:
                target.sendMessage(ChatColor.RED + "Tipo inválido: " + tipo);
                break;
        }
    }
}
