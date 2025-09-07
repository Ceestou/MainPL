package org.com.mainpl;

import net.luckperms.api.messenger.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class MReloadCommand implements CommandExecutor {

    private final MainPL plugin;
    private final WarpCommands warpCommands;
    private final AjudaManager ajudaManager;
    private final UtilsCommand utilsCommand;
    private final ComandosManager comandosManager;
    private final EnchantAliases ultraEnchants;
    private final MessageManager messageManager;
    private final WorldCommandBlocker worldCommandBlocker;
    private final PendenciarManager pendenciarManager;

    public MReloadCommand(MainPL plugin, WarpCommands warpCommands, AjudaManager ajudaManager, UtilsCommand utilsCommand, ComandosManager comandosManager, EnchantAliases ultraEnchants, MessageManager messageManager, WorldCommandBlocker worldCommandBlocker, PendenciarManager pendenciarManager) {
        this.plugin = plugin;
        this.warpCommands = warpCommands;
        this.ajudaManager = ajudaManager;
        this.utilsCommand = utilsCommand;
        this.comandosManager = comandosManager;
        this.ultraEnchants = ultraEnchants;
        this.messageManager = messageManager;
        this.worldCommandBlocker = worldCommandBlocker;
        this.pendenciarManager = pendenciarManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (sender instanceof Player player) {
            if (!player.hasPermission("mainpl.reload")) {
                player.sendMessage("§cVocê não tem permissão para usar este comando.");
                return true;
            }
        }


        plugin.reloadAllYmls();

        if (plugin.getModulosManager().isEnabled("Warp") && warpCommands != null) {
            File warpsFile = new File(plugin.getDataFolder(), "warps.yml");
            YamlConfiguration warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
            warpCommands.setWarpsConfig(warpsConfig);
        }


        if (plugin.getModulosManager().isEnabled("Ajuda") && ajudaManager != null) {
            File ajudaFile = new File(plugin.getDataFolder(), "ajuda.yml");
            YamlConfiguration ajudaConfig = YamlConfiguration.loadConfiguration(ajudaFile);
            ajudaManager.setAjudaConfig(ajudaConfig);
        }

        if (plugin.getModulosManager().isEnabled("Utils") && utilsCommand != null) {
            File idsFile = new File(plugin.getDataFolder(), "idsgive.yml");
            YamlConfiguration idsConfig = YamlConfiguration.loadConfiguration(idsFile);
            utilsCommand.setIdsConfig(idsConfig);
        }

        File comandosFile = new File(plugin.getDataFolder(), "Comandos.yml");
        YamlConfiguration comandosConfig = YamlConfiguration.loadConfiguration(comandosFile);
        comandosManager.setComandosConfig(comandosConfig);

        if (plugin.getModulosManager().isEnabled("UltraEnchants") && utilsCommand != null) {
            File enchantConfig = new File(plugin.getDataFolder(), "enchantments.yml");
            YamlConfiguration enchantFile = YamlConfiguration.loadConfiguration(enchantConfig);
            ultraEnchants.setEnchantConfig(enchantFile);
        }

        File messageFile = new File(plugin.getDataFolder(), "mensagens.yml");
        YamlConfiguration messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        messageManager.setMessageConfig(messageConfig);

        if (plugin.getModulosManager().isEnabled("BlockWorlds") && utilsCommand != null) {
            File worldBlock = new File(plugin.getDataFolder(), "worlds_block.yml");
            YamlConfiguration worldBlockConfig = YamlConfiguration.loadConfiguration(worldBlock);
            worldCommandBlocker.setWorldConfig(worldBlockConfig);
        }

        if (plugin.getModulosManager().isEnabled("Pedenciar") && utilsCommand != null) {
            File pedencia = new File(plugin.getDataFolder(), "pendentescmd.yml");
            YamlConfiguration pedenciaConfig = YamlConfiguration.loadConfiguration(pedencia);
            pendenciarManager.setPendenteConfig(pedenciaConfig);
        }

        sender.sendMessage("§aTodos os arquivos de configuração foram recarregados!");
        return true;
    }
}
