package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Tpa implements CommandExecutor, Listener {

    private final MainPL plugin;
    private final MessageManager messageManager;

    private final Map<UUID, TpaRequest> pendingRequests = new HashMap<>();
    private final Set<String> yesAliases = new HashSet<>(Arrays.asList("s", "sim", "yes", "y", "ok", "aceitar"));
    private final Set<String> noAliases = new HashSet<>(Arrays.asList("n", "não", "nao", "no", "cancelar", "recusar"));
    private final Set<UUID> invulnerablePlayers = new HashSet<>();
    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    public Tpa(MainPL plugin) {
        this.plugin = plugin;
        this.messageManager = new MessageManager(plugin);
    }

    private static class PlayerPair {
        private final UUID uuid1;
        private final UUID uuid2;

        public PlayerPair(UUID a, UUID b) {
            if (a.compareTo(b) < 0) {
                this.uuid1 = a;
                this.uuid2 = b;
            } else {
                this.uuid1 = b;
                this.uuid2 = a;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PlayerPair)) return false;
            PlayerPair other = (PlayerPair) o;
            return uuid1.equals(other.uuid1) && uuid2.equals(other.uuid2);
        }

        @Override
        public int hashCode() {
            return uuid1.hashCode() * 31 + uuid2.hashCode();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("somente_jogadores"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();


        long cooldown = getCooldownForGroup(player);

        long now = System.currentTimeMillis();
        if (cooldownMap.containsKey(playerId)) {
            long lastUsed = cooldownMap.get(playerId);
            long remaining = (lastUsed + cooldown) - now;
            if (remaining > 0) {
                player.sendMessage(messageManager.getMessage("cooldown", "seconds", String.valueOf(remaining / 1000)));
                return true;
            }
        }

        if (args.length != 1) {
            player.sendMessage(messageManager.getMessage("uso_correto", "label", label));
            return true;
        }

        String inputName = args[0].toLowerCase();
        Player target = Bukkit.getPlayerExact(inputName);

        if (target == null || !target.isOnline()) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                String cleanName = online.getName().toLowerCase().replaceFirst("^\\.", "");
                if (cleanName.startsWith(inputName)) {
                    target = online;
                    break;
                }
            }
        }

        if (target == null || !target.isOnline()) {
            player.sendMessage(messageManager.getMessage("jogador_nao_encontrado"));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(messageManager.getMessage("nao_para_si"));
            return true;
        }

        boolean isTpa = command.getName().equalsIgnoreCase("tpa");
        pendingRequests.put(target.getUniqueId(), new TpaRequest(player.getUniqueId(), isTpa));
        cooldownMap.put(playerId, now);

        player.sendMessage(messageManager.getMessage("pedido_enviado",
                "tipo", isTpa ? "TPA" : "TPAHERE",
                "target", target.getName()));

        target.sendMessage(messageManager.getMessage("pedido_recebido",
                "player", player.getName(),
                "acao", isTpa ? "se teleportar até você" : "que você se teleporte até ele"));


        UUID targetUUID = target.getUniqueId();
        String targetName = target.getName();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(targetUUID)) {
                pendingRequests.remove(targetUUID);
                player.sendMessage(messageManager.getMessage("pedido_expirou_sender", "target", targetName));
                Player t = Bukkit.getPlayer(targetUUID);
                if (t != null && t.isOnline()) {
                    t.sendMessage(messageManager.getMessage("pedido_expirou_target"));
                }
            }
        }, 30 * 20L);

        return true;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player responder = event.getPlayer();
        UUID responderUUID = responder.getUniqueId();
        if (!pendingRequests.containsKey(responderUUID)) return;

        String msg = event.getMessage().toLowerCase().trim();
        if (yesAliases.contains(msg)) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> processResponse(responder, true));
        } else if (noAliases.contains(msg)) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> processResponse(responder, false));
        }
    }

    private void processResponse(Player responder, boolean aceitou) {
        UUID responderUUID = responder.getUniqueId();
        TpaRequest request = pendingRequests.remove(responderUUID);

        if (request == null) {
            responder.sendMessage(messageManager.getMessage("jogador_nao_encontrado"));
            return;
        }

        Player requester = Bukkit.getPlayer(request.requesterUUID);
        if (requester == null || !requester.isOnline()) {
            responder.sendMessage(messageManager.getMessage("jogador_nao_encontrado"));
            return;
        }

        if (!aceitou) {
            responder.sendMessage(messageManager.getMessage("recusou"));
            requester.sendMessage(messageManager.getMessage("recusou_solicitante", "responder", responder.getName()));
            return;
        }

        if (request.isTpa) {
            responder.sendMessage(messageManager.getMessage("aceitou", "quem", requester.getName()));
            requester.sendMessage(messageManager.getMessage("aceitou_solicitante", "responder", responder.getName()));
            teleportWithDelay(requester, () -> responder.getLocation(), 5, true);
        } else {
            responder.sendMessage(messageManager.getMessage("aceitou", "quem", requester.getName()));
            requester.sendMessage(messageManager.getMessage("aceitou_solicitante", "responder", responder.getName()));
            teleportWithDelay(responder, () -> requester.getLocation(), 5, true);
        }
    }

    private void teleportWithDelay(Player player, LocationSupplier targetSupplier, int defaultDelaySeconds, boolean withProtection) {
        int delaySeconds = getTpaTimeInSeconds(player); // pega tempo da config por grupo

        if (delaySeconds == 0) {
            player.teleport(targetSupplier.get());
            player.sendMessage(messageManager.getMessage("tp_instantaneo"));
            if (withProtection) applyDamageProtection(player);
            return;
        }

        player.sendMessage(messageManager.getMessage("tp_contagem", "seconds", String.valueOf(delaySeconds)));
        Location initialLoc = player.getLocation();

        new BukkitRunnable() {
            int countdown = delaySeconds;

            @Override
            public void run() {
                if (!isSameBlock(initialLoc, player.getLocation())) {
                    player.sendMessage(messageManager.getMessage("tp_cancelado"));
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    player.teleport(targetSupplier.get());
                    player.sendMessage(messageManager.getMessage("tp_sucesso"));
                    if (withProtection) applyDamageProtection(player);
                    cancel();
                    return;
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }


    private int getTpaTimeInSeconds(Player player) {
        String group = plugin.getPrimaryGroup(player).toLowerCase();
        String path = "tempotpa." + group;

        String timeStr;
        if (plugin.getConfig().contains(path)) {
            timeStr = plugin.getConfig().getString(path);
        } else {
            timeStr = plugin.getConfig().getString("tempotpa.default", "5s");
        }

        return parseTimeToSeconds(timeStr);
    }


    private long getCooldownForGroup(Player player) {
        String group = plugin.getPrimaryGroup(player).toLowerCase();
        String path = "tempotpa." + group;

        String timeStr;
        if (plugin.getConfig().contains(path)) {
            timeStr = plugin.getConfig().getString(path);
        } else {
            timeStr = plugin.getConfig().getString("tempotpa.default", "5s");
        }

        return parseTimeToSeconds(timeStr) * 1000L;
    }


    private int parseTimeToSeconds(String time) {
        if (time == null || time.isEmpty()) return 5;
        time = time.toLowerCase().trim();
        try {
            if (time.endsWith("s")) {
                return Integer.parseInt(time.replace("s", ""));
            } else if (time.endsWith("m")) {
                return Integer.parseInt(time.replace("m", "")) * 60;
            } else if (time.endsWith("h")) {
                return Integer.parseInt(time.replace("h", "")) * 3600;
            } else {
                return Integer.parseInt(time);
            }
        } catch (NumberFormatException e) {
            return 5;
        }
    }


    private final Map<PlayerPair, Long> activeProtectionPairs = new HashMap<>();
    private final Map<PlayerPair, Long> postProtectionWindow = new HashMap<>();
    private final Set<PlayerPair> extraProtectionUsed = new HashSet<>();

    private void applyDamageProtection(Player player) {
        UUID playerId = player.getUniqueId();
        invulnerablePlayers.add(playerId);
        player.sendMessage(messageManager.getMessage("protecao_inicial"));

        Set<PlayerPair> pairs = new HashSet<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            UUID otherId = online.getUniqueId();
            if (otherId.equals(playerId)) continue;
            if (invulnerablePlayers.contains(otherId)) {
                PlayerPair pair = new PlayerPair(playerId, otherId);
                pairs.add(pair);
                activeProtectionPairs.put(pair, System.currentTimeMillis() + 30_000);
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            invulnerablePlayers.remove(playerId);
            player.sendMessage(messageManager.getMessage("protecao_fim"));
            for (PlayerPair pair : pairs) {
                activeProtectionPairs.remove(pair);
                postProtectionWindow.put(pair, System.currentTimeMillis() + 45_000);
            }
        }, 30 * 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        UUID damagedId = damaged.getUniqueId();
        UUID damagerId = damager.getUniqueId();
        PlayerPair pair = new PlayerPair(damagedId, damagerId);

        long now = System.currentTimeMillis();

        if (invulnerablePlayers.contains(damagedId) || invulnerablePlayers.contains(damagerId)) {
            event.setCancelled(true);
            damager.sendMessage(messageManager.getMessage("protecao_ataque"));
            damaged.sendMessage(messageManager.getMessage("protecao_recebeu"));
            return;
        }

        if (activeProtectionPairs.containsKey(pair)) {
            event.setCancelled(true);
            damaged.sendMessage(messageManager.getMessage("anti_tpakill", "damager", damager.getName()));
            damager.sendMessage(messageManager.getMessage("anti_tpakill_aviso", "damaged", damaged.getName()));
            return;
        }

        if (postProtectionWindow.containsKey(pair)) {
            long expiration = postProtectionWindow.get(pair);
            if (now <= expiration && !extraProtectionUsed.contains(pair)) {
                activeProtectionPairs.put(pair, now + 15_000);
                extraProtectionUsed.add(pair);

                event.setCancelled(true);
                damaged.sendMessage(messageManager.getMessage("anti_tpakill", "damager", damager.getName()));
                damager.sendMessage(messageManager.getMessage("extra_protecao", "damaged", damaged.getName()));

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    activeProtectionPairs.remove(pair);
                    damaged.sendMessage(messageManager.getMessage("fim_extra_protecao_damaged", "damager", damager.getName()));
                    damager.sendMessage(messageManager.getMessage("fim_extra_protecao_damager", "damaged", damaged.getName()));
                }, 15 * 20L);
            }
        }
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.getWorld().equals(loc2.getWorld())
                && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    private static class TpaRequest {
        private final UUID requesterUUID;
        private final boolean isTpa;

        public TpaRequest(UUID requesterUUID, boolean isTpa) {
            this.requesterUUID = requesterUUID;
            this.isTpa = isTpa;
        }
    }

    @FunctionalInterface
    private interface LocationSupplier {
        Location get();
    }
}
