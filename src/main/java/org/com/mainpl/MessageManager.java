package org.com.mainpl;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {
    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration config;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mensagens.yml");

        if (!file.exists()) {
            criarArquivoMensagensPadrao();
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void setMessageConfig(YamlConfiguration newConfig) {
        config = newConfig;
    }

    private void criarArquivoMensagensPadrao() {
        try {
            plugin.getDataFolder().mkdirs();

            Map<String, Object> mensagensPadrao = new LinkedHashMap<>();


            mensagensPadrao.put("somente_jogadores", "&cEste comando só pode ser usado por jogadores.");
            mensagensPadrao.put("uso_correto", "&cUso correto: /{label} <jogador>");
            mensagensPadrao.put("cooldown", "&c⏳ Você precisa esperar {seconds} segundos para usar este comando novamente.");
            mensagensPadrao.put("jogador_nao_encontrado", "&cJogador não encontrado.");
            mensagensPadrao.put("nao_para_si", "&cVocê não pode enviar um pedido para você mesmo.");
            mensagensPadrao.put("pedido_enviado", "&aPedido de {tipo} enviado para {target}");
            mensagensPadrao.put("pedido_recebido", "&e{player} deseja {acao}. Digite &a's' &epara aceitar ou &c'n' &epara recusar.");
            mensagensPadrao.put("pedido_expirou_sender", "&cO pedido para {target} expirou.");
            mensagensPadrao.put("pedido_expirou_target", "&cO pedido de teleporte expirou.");
            mensagensPadrao.put("aceitou", "&aVocê aceitou o pedido. Teleportando {quem}...");
            mensagensPadrao.put("aceitou_solicitante", "&a{responder} aceitou seu pedido. Teleportando você...");
            mensagensPadrao.put("recusou", "&cVocê recusou o pedido de teleporte.");
            mensagensPadrao.put("recusou_solicitante", "&c{responder} recusou seu pedido de teleporte.");
            mensagensPadrao.put("tp_instantaneo", "&a✔ Você foi teleportado instantaneamente como apoiador!");
            mensagensPadrao.put("tp_contagem", "&e⏳ Teleportando em {seconds} segundos. Não se mexa!");
            mensagensPadrao.put("tp_cancelado", "&c✘ Teleporte cancelado porque você se moveu!");
            mensagensPadrao.put("tp_sucesso", "&a✔ Você foi teleportado!");
            mensagensPadrao.put("protecao_inicial", "&bVocê está protegido contra danos por 30 segundos!");
            mensagensPadrao.put("protecao_fim", "&bSua proteção inicial acabou.");
            mensagensPadrao.put("protecao_ataque", "&7Você não pode atacar jogadores enquanto está protegido.");
            mensagensPadrao.put("protecao_recebeu", "&7Você está temporariamente protegido contra ataques.");
            mensagensPadrao.put("anti_tpakill", "&c{damager} está tentando um possível tpakill! Recomendamos fuga!");
            mensagensPadrao.put("anti_tpakill_aviso", "&7Você não pode atacar {damaged} enquanto a proteção está ativa.");
            mensagensPadrao.put("extra_protecao", "&7Você não pode atacar {damaged} por 15 segundos devido à proteção anti-tpakill.");
            mensagensPadrao.put("fim_extra_protecao_damaged", "&bA proteção anti-tpakill contra {damager} acabou.");
            mensagensPadrao.put("fim_extra_protecao_damager", "&bA proteção anti-tpakill contra {damaged} acabou.");


            mensagensPadrao.put("sethome_uso", "&c❗ Use: /sethome <nome>");
            mensagensPadrao.put("sethome_limite", "&c❌ Você já atingiu o limite de {max} homes.");
            mensagensPadrao.put("sethome_substituir_pergunta", "&e⚠ Já existe uma home com esse nome. Deseja substituir? Responda no chat com 'sim' ou 'não'.");
            mensagensPadrao.put("sethome_substituida", "&a✔ Home '{home}' substituída com sucesso!");
            mensagensPadrao.put("sethome_cancelada", "&c❌ Substituição da home cancelada.");
            mensagensPadrao.put("sethome_definida", "&a✔ Home '{home}' definida com sucesso!");
            mensagensPadrao.put("confirmacao_pendente", "&eConfirmação pendente!");

            mensagensPadrao.put("bau_modulo_desativado", "&cO módulo de baús está desativado.");
            mensagensPadrao.put("bau_apoiador_somente", "&cApenas apoiadores podem usar este comando.");
            mensagensPadrao.put("bau_nao_encontrado", "&cSeu baú não foi encontrado.");
            mensagensPadrao.put("bau_aberto", "&aVocê abriu seu baú.");
            mensagensPadrao.put("bau_fechado", "&eVocê fechou seu baú.");


            mensagensPadrao.put("rtp_somente_jogadores", "&cEste comando só pode ser usado por jogadores.");
            mensagensPadrao.put("rtp_cooldown", "⏳ Você deve esperar {minutes}m {seconds}s para usar /rtp novamente.");
            mensagensPadrao.put("rtp_teleportando", "⏳ Teleportando aleatoriamente em {seconds} segundos. Não se mexa!");
            mensagensPadrao.put("rtp_cancelado_movimento", "✘ Teleporte cancelado porque você se moveu!");
            mensagensPadrao.put("rtp_falha", "❌ Não foi possível encontrar um local seguro para teleporte.");
            mensagensPadrao.put("rtp_sucesso", "✔ Você foi teleportado aleatoriamente!");


            mensagensPadrao.put("remover_uso", "&cUse: /remover <jogador>");
            mensagensPadrao.put("remover_sucesso", "&aVocê removeu {target} da sua lista de confiança.");
            mensagensPadrao.put("remover_falha", "&c{target} não está na sua lista de confiança.");


            mensagensPadrao.put("ranking_titulo", List.of("&6§l--- RANKING DE KILLS ---"));
            mensagensPadrao.put("ranking_linha", List.of(
                    "{cor_pos}{emoji}{pos}. {player}  §fKills: {kills} | Mortes: {deaths}",
                    "KDR: {kdr}"
            ));
            mensagensPadrao.put("ranking_rodape", List.of("&6§l-------------------------"));
            mensagensPadrao.put("top_1_icon", "§6🏆");
            mensagensPadrao.put("top_2_icon", "§7🥈");
            mensagensPadrao.put("top_3_icon", "§8🥉");
            mensagensPadrao.put("top_4_icon", "§f•");
            mensagensPadrao.put("top_5_icon", "§f•");
            mensagensPadrao.put("top_1", "§6🏆 [Top 1] 🏆");
            mensagensPadrao.put("top_2", "§7🥈 [Top 2] 🥈");
            mensagensPadrao.put("top_3", "§8🥉 [Top 3] 🥉");
            mensagensPadrao.put("top_1_cor", "§6");
            mensagensPadrao.put("top_2_cor", "§7");
            mensagensPadrao.put("top_3_cor", "§8");
            mensagensPadrao.put("top_tag_padrao", "");
            mensagensPadrao.put("top_padrao_cor", "§f");

            mensagensPadrao.put("top_padrao_emoji", "•");
            mensagensPadrao.put("ranking_vazio", "&cO ranking está vazio.");


            mensagensPadrao.put("top_entrou", "&c{player}&f alcançou o {pos}");
            mensagensPadrao.put("top_subiu", "&c{player}&f subiu para o {pos}");


            mensagensPadrao.put("ordinal_1", "1º");
            mensagensPadrao.put("ordinal_2", "2º");
            mensagensPadrao.put("ordinal_3", "3º");

            mensagensPadrao.put("loja_mensagem", "&aNossa loja: &f{url}");
            mensagensPadrao.put("loja_click", "&aClique aqui para acessar a loja!");
            mensagensPadrao.put("loja_url", "https://anarquiax.minecart.com.br/");


            mensagensPadrao.put("homes_sem_homes", "&c❌ Você não tem nenhuma home setada.");
            mensagensPadrao.put("homes_lista_titulo", "&6Suas homes:");
            mensagensPadrao.put("homes_lista_item", "&e- {home}");
            mensagensPadrao.put("delhome_uso", "&c❗ Use: /delhome <nome>");
            mensagensPadrao.put("delhome_nao_existe", "&c❌ Essa home não existe.");
            mensagensPadrao.put("delhome_sucesso", "&a✔ Home '{home}' deletada com sucesso!");
            mensagensPadrao.put("home_uso", "&c❗ Use: /home <nome>");
            mensagensPadrao.put("home_nao_existe", "&c❌ Essa home não existe.");
            mensagensPadrao.put("home_mundo_inexistente", "&c❌ O mundo salvo na sua home não existe mais.");
            mensagensPadrao.put("home_teleport_apoiador", "&a✔ Você foi teleportado para a home '{home}'!");
            mensagensPadrao.put("home_teleport_delay", "&e⏳ Teleportando para a home '{home}' em 4 segundos. Não se mexa!");
            mensagensPadrao.put("home_teleport_cancelado", "&c✘ Teleporte cancelado porque você se moveu!");


            mensagensPadrao.put("gemas_sem_permissao", "&cVocê não tem permissão para usar este comando.");
            mensagensPadrao.put("gemas_uso", "&cUso: /dargemas <jogador>");
            mensagensPadrao.put("gemas_dado_jogador_online", "&aVocê deu 11 gemas para {player}!");
            mensagensPadrao.put("gemas_recebido_online", "&aVocê recebeu +11 gemas!");
            mensagensPadrao.put("gemas_pendente_registrado", "&aO jogador está offline. Gemas pendentes registradas para {player}");
            mensagensPadrao.put("anunciar_sem_gemas", "&cVocê precisa de 7 gemas para anunciar. (Você tem: {saldo})");
            mensagensPadrao.put("anunciar_sucesso", "&aVocê usou 7 gemas para anunciar!");
            mensagensPadrao.put("gemas_recebidas_pendentes", "&aVocê recebeu {quantidade} gemas pendentes!");
            mensagensPadrao.put("anunciar_cooldown", "&cAguarde o Cooldown!");

            mensagensPadrao.put("pendenciar_sem_permissao", "&cApenas operadores podem usar este comando.");
            mensagensPadrao.put("pendenciar_uso", "&cUso: /pendenciar <lista> <jogador>");
            mensagensPadrao.put("pendenciar_lista_inexistente", "&cEssa lista de pendências não existe!");
            mensagensPadrao.put("pendenciar_aplicado", "&aPendências da lista {lista} aplicadas para {player}!");
            mensagensPadrao.put("pendenciar_executado", "&aTodos os comandos pendentes foram executados!");


            mensagensPadrao.put("mortes_somente_jogadores", "&cApenas jogadores podem usar este comando.");
            mensagensPadrao.put("mortes_nenhuma", "&eVocê ainda não morreu nenhuma vez.");
            mensagensPadrao.put("mortes_titulo", "&eÚltimos locais de morte:");
            mensagensPadrao.put("mortes_item", "&7- {local}");


            mensagensPadrao.put("bau_somente_jogadores", "&cComando apenas para jogadores.");
            mensagensPadrao.put("bau_bau_invalido", "&cVocê só pode abrir baús de 1 até {max}");
            mensagensPadrao.put("bau_numero_invalido", "&cNúmero inválido.");
            mensagensPadrao.put("bau_bloqueado", "&cVocê precisa ser um apoiador para acessar este baú.");

            mensagensPadrao.put("darbau_sem_permissao", "&cApenas o console ou operadores podem usar esse comando.");
            mensagensPadrao.put("darbau_uso", "&eUso correto: /darbau <nick>");
            mensagensPadrao.put("darbau_offline", "&cJogador offline.");
            mensagensPadrao.put("darbau_maximo", "&cJá tem o máximo de baús ({max}).");
            mensagensPadrao.put("darbau_sucesso", "&aConcedido novo baú. Total: {total}");
            mensagensPadrao.put("darbau_recebido", "&aVocê recebeu um novo baú! (/bau)");

            mensagensPadrao.put("setwarp_uso", "&cUse /setwarp <nome>");
            mensagensPadrao.put("setespecial_uso", "&cUse /especial <nomewarp> WarpEspecial");
            mensagensPadrao.put("setwarp_sucesso", "&aWarp {warp} criada com sucesso!");
            mensagensPadrao.put("setespecial_sucesso", "&aWarp {warp} foi definida como &6Especial &acom Sucesso!");


            mensagensPadrao.put("delwarp_uso", "&cUse /delwarp <nome>");
            mensagensPadrao.put("delwarp_nao_existe", "&cA warp {warp} não existe.");
            mensagensPadrao.put("delwarp_sucesso", "&aWarp {warp} deletada com sucesso!");

            mensagensPadrao.put("warp_uso", "&cUse /warp <nome>");
            mensagensPadrao.put("warp_nao_existe", "&cA warp {warp} não existe.");
            mensagensPadrao.put("warp_mundo_inexistente", "&cO mundo da warp {warp} não existe.");
            mensagensPadrao.put("warp_teleport", "&aVocê foi teleportado para a warp {warp}!");
            mensagensPadrao.put("warps_vazio", "&cWarp Vazia.");


            mensagensPadrao.put("setspawn_sucesso", "&aSpawn definido com sucesso!");
            mensagensPadrao.put("spawn_nao_definido", "&cO spawn ainda não foi definido!");
            mensagensPadrao.put("spawn_mundo_inexistente", "&cO mundo do spawn não existe!");

            mensagensPadrao.put("back_teleportado", "&a✔ Teleportado para sua morte #{pos}!");
            mensagensPadrao.put("back_numero_invalido", "&cVocê deve informar um número válido. Ex: /back 1");
            mensagensPadrao.put("back_fora_limite", "&cVocê só pode voltar até {limite} mortes atrás.");
            mensagensPadrao.put("back_sem_permissao", "&cSeu grupo não tem permissão para usar este comando.");
            mensagensPadrao.put("back_teleport_delay", "&eTeleportando para sua morte em {seconds} segundos. Não se mova!");
            mensagensPadrao.put("back_teleport_cancelado", "&cTeleport cancelado por movimento!");
            mensagensPadrao.put("back_cooldown", "&cAguarde para digitar denovo.");


            mensagensPadrao.put("feed_sucesso", "&aSua fome foi restaurada!");
            mensagensPadrao.put("heal_sucesso", "&aSua vida foi restaurada!");


            mensagensPadrao.put("gamemode_sucesso", "&aSeu modo de jogo foi alterado para {modo}!");
            mensagensPadrao.put("gamemode_invalido", "&cModo de jogo inválido!");


            mensagensPadrao.put("tphere_sucesso", "&aVocê teleportou {player} para você!");
            mensagensPadrao.put("tphere_jogador_nao_encontrado", "&cJogador não encontrado.");


            mensagensPadrao.put("fly_ativado", "&aFly ativado!");
            mensagensPadrao.put("fly_desativado", "&cFly desativado!");


            mensagensPadrao.put("speed_sucesso", "&aVelocidade alterada para {velocidade}!");


            mensagensPadrao.put("clearchat_sucesso", "&aChat limpo com sucesso!");
            mensagensPadrao.put("clearchat_sem_permissao", "&cVocê não tem permissão para limpar o chat.");

            mensagensPadrao.put("feed_sucesso_target", "&aVocê deu comida para {player}!");
            mensagensPadrao.put("heal_sucesso_target", "&aVocê curou {player}!");
            mensagensPadrao.put("gamemode_sucesso_target", "&aVocê mudou o gamemode de {player} para {modo}!");
            mensagensPadrao.put("tphere_sucesso_target", "&aVocê teleportou {player} até você!");
            mensagensPadrao.put("fly_sucesso_target", "&aVocê alterou o modo de voo de {player}!");
            mensagensPadrao.put("speed_sucesso_target", "&aVocê alterou a velocidade de {player} para {velocidade}!");
            mensagensPadrao.put("speed_valor_invalido", "&cO valor da velocidade deve ser de 1 a 10!");
            mensagensPadrao.put("sem_permissao", "&cVocê não tem permissão para usar este comando.");
            mensagensPadrao.put("cooldown_comando", "&cAguarde {seconds} segundos antes de usar este comando novamente.");

            mensagensPadrao.put("craft_sucesso", "&aCrafting virtual aberto com sucesso!");
            mensagensPadrao.put("lixo_sucesso", "&aInventário limpo com sucesso!");
            mensagensPadrao.put("lixo_aberto", "O Lixo foi aberto com sucesso");
            mensagensPadrao.put("fornalha_sucesso", "&aFornalha virtual aberta com sucesso!");
            mensagensPadrao.put("hat_sucesso", "&aVocê colocou o item na cabeça com sucesso!");
            mensagensPadrao.put("invsee_sucesso", "&aVocê está visualizando o inventário de {player}.");

            mensagensPadrao.put("invsee_uso", "&cUso correto: /invsee <jogador>");

            mensagensPadrao.put("comando_delay", "§eO comando será executado em §b{seconds}§e segundos...");


            mensagensPadrao.put("nenhum_item_mao", "&cVocê não está segurando nenhum item!");
            mensagensPadrao.put("enchant_sucesso", "&aItem encantado com sucesso!");
            mensagensPadrao.put("uso_give", "&cUso correto: /give <item> [quantidade] [jogador]");
            mensagensPadrao.put("item_invalido", "&cItem inválido!");
            mensagensPadrao.put("give_sucesso", "&aVocê deu &6{item} &apara o jogador &6{player}&a!");
            mensagensPadrao.put("giveall_sucesso", "&aTodos receberam &6{quantidade}x {item}&a!");
            mensagensPadrao.put("valor_invalido", "&cValor inválido!");
            mensagensPadrao.put("exp_sucesso", "&aVocê recebeu &6{quantidade} &aXP!");
            mensagensPadrao.put("exp_sucesso_target", "&aVocê deu &6{quantidade} &aXP para &6{player}&a!");
            mensagensPadrao.put("level_sucesso", "&aSeu level foi definido para &6{level}&a!");
            mensagensPadrao.put("level_sucesso_target", "&aVocê alterou o level de &6{player}&a para &6{level}&a!");
            mensagensPadrao.put("uso_rename", "&cUso correto: /rename <nome>");
            mensagensPadrao.put("rename_sucesso", "&aItem renomeado com sucesso!");
            mensagensPadrao.put("kickall_sucesso", "&aTodos os jogadores foram kickados com sucesso!");
            mensagensPadrao.put("ban_mensagem", "&cVocê foi banido do servidor!");
            mensagensPadrao.put("ban_sucesso", "&aO jogador &6{player} &afoi banido!");
            mensagensPadrao.put("unban_sucesso", "&aO jogador &6{player} &afoi desbanido!");
            mensagensPadrao.put("skull_sucesso", "&aVocê pegou a cabeça de &6{player}&a!");
            mensagensPadrao.put("toggle_jail_sucesso", "&aJail aplicado em &6{player}&a!");
            mensagensPadrao.put("toggle_afk_sucesso", "&aVocê agora está AFK!");
            mensagensPadrao.put("toggle_god_ativado", "&aGod ativado!");
            mensagensPadrao.put("toggle_god_desativado", "&cGod desativado!");
            mensagensPadrao.put("top_sucesso", "&aVocê foi teleportado para o topo!");
            mensagensPadrao.put("uso_tempban", "&cUso correto: /tempban <jogador> <tempo>");
            mensagensPadrao.put("uso_tempbanip", "&cUso correto: /tempbanip <ip> <tempo>");
            mensagensPadrao.put("uso_jail", "&cUso correto: /jail <jogador>");
            mensagensPadrao.put("nenhum_grupo", "&cNenhum Grupo está autorizado a usar este comando.");
            mensagensPadrao.put("comando_invalido_padrao", "&cEste comando está inválido! &fUse /ajuda.");

            mensagensPadrao.put("banmsg_permanente", List.of(
                    "&cVocê foi banido permanentemente!",
                    "&7Motivo: {motivo}"
            ));
            mensagensPadrao.put("banmsg_temporario", List.of(
                    "&cVocê foi banido temporariamente!",
                    "&7Motivo: {motivo}",
                    "&7Tempo restante: {remaining}"
            ));

            mensagensPadrao.put("kick_tempban", List.of(
                    "&cVocê acaba de ser banido!",
                    "&7Motivo: {motivo}",
                    "&7Tempo restante: {remaining}"
            ));
            mensagensPadrao.put("kick_permaban", List.of(
                    "&cVocê acaba de ser banido Permanentemente!",
                    "&7Motivo: {motivo}"
            ));
            mensagensPadrao.put("kick_tempban_ip", List.of(
                    "&cVocê acaba de ser banido via IP!",
                    "&7Motivo: {motivo}",
                    "&7Tempo restante: {remaining}"
            ));
            mensagensPadrao.put("kick_player_mensagem", List.of(
                    "&cVocê acaba de ser Kickado!",
                    "&7Motivo: {motivo}"
            ));
            mensagensPadrao.put("kick_player_sucesso", List.of(
                    "&cVocê acaba de ser Kickado!",
                    "&7Motivo: {motivo}"
            ));

            mensagensPadrao.put("kickall_mensagem", List.of(
                    "&cVocê acaba de ser Kickado!",
                    "&7Motivo: {motivo}"
            ));
            mensagensPadrao.put("freeze_removido", "&6O Congelamento foi removido para {player}");
            mensagensPadrao.put("freeze_desativado", "&f{player} &6O seu Congelamento foi removido");
            mensagensPadrao.put("freeze_ativado", "&6O Congelamento foi ativado para {player}");
            mensagensPadrao.put("freeze_ativado_target", "&f{player} &6O seu Congelamento foi ativado");
            mensagensPadrao.put("repairall_sucesso", "&aSeu inventário todo foi reparado com sucesso!");
            mensagensPadrao.put("repair_sucesso", "&aSeu item foi reparado com sucesso!");
            mensagensPadrao.put("speed_definido", "&aSua velocidade foi alterada para &6{valor}!");
            mensagensPadrao.put("speed_resetado_target", "&cSua velocidade foi resetada!");
            mensagensPadrao.put("speed_resetado", "&cVelocidade foi resetada!");
            mensagensPadrao.put("anvil_aberto", "&aAnvil foi aberta!");
            mensagensPadrao.put("luz_ligada", "&aLuz ficou ligada!");
            mensagensPadrao.put("luz_desligada", "&cLuz ficou desligada!");
            mensagensPadrao.put("hat_vazio", "&cSlot Vazio! Selecione outro slot!");

            YamlConfiguration yml = new YamlConfiguration();
            for (Map.Entry<String, Object> entry : mensagensPadrao.entrySet()) {
                yml.set("mensagens." + entry.getKey(), entry.getValue());
            }
            yml.save(file);

        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível criar mensagens.yml!");
            e.printStackTrace();
        }
    }

    public String getMessage(String key, String... placeholders) {
        String msg = config.getString("mensagens." + key, "&cMensagem não encontrada: " + key);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            msg = msg.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }


    public List<String> getMessageList(String key) {
        List<String> list = config.getStringList("mensagens." + key);
        if (list.isEmpty()) {

            String single = config.getString("mensagens." + key);
            if (single != null) list = List.of(org.bukkit.ChatColor.translateAlternateColorCodes('&', single));
        } else {

            for (int i = 0; i < list.size(); i++) {
                list.set(i, org.bukkit.ChatColor.translateAlternateColorCodes('&', list.get(i)));
            }
        }
        return list;
    }

    public List<String> getMessageListPlaceholder(String key, Map<String, String> placeholders) {
        List<String> list = new ArrayList<>(config.getStringList("mensagens." + key));
        if (list.isEmpty()) {
            String single = config.getString("mensagens." + key);
            if (single != null) list = new ArrayList<>(List.of(single)); // <<< aqui
        }

        for (int i = 0; i < list.size(); i++) {
            String msg = list.get(i);
            if (placeholders != null) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
                }
            }
            list.set(i, org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
        }

        return list;
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}
