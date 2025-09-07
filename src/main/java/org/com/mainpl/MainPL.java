package org.com.mainpl;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.*;
import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class MainPL extends JavaPlugin {
    private JavaPlugin plugin;
    private LuckPerms luckPerms;
    private static final UUID DANO_EXTRA_UUID = UUID.fromString("a3c8e8f0-1234-4f5a-b67d-8a2c7e3f1234");
    private MensagensApoiadores apoioMessagesManager;
    private RankingManager rankingManager;
    public static MainPL instance;
    private EnchantAliases aliases;
    private final Map<UUID, Location> jailedPlayers = new HashMap<>();

    // Guarda os blocos que formam a jaula de cada jogador
    private final Map<UUID, List<Block>> jailBlocks = new HashMap<>();
    public final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private File bansFile;
    private FileConfiguration bansConfig;



    public MainPL() {
        // Não faça nada aqui que dependa de Bukkit
    }

    // Construtor que recebe a instância do JavaPlugin
    public MainPL(JavaPlugin plugin) {
        this.plugin = plugin;
        this.luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();

    }

    private final Map<UUID, String> codigosPorPlayer = new HashMap<>();
    private final Map<UUID, Integer> tentativasRestantes = new HashMap<>();
    private final Set<UUID> autenticados = new HashSet<>();
    private NickManager nickManager;
    private BauSManager bauManager;
    private SbauCommand sbauCommand;
    private EscolhaDisplayManager escolhaDisplayManager;
    ModulosManager modulosManager = new ModulosManager(this);
    private MessageManager messageManager;


    private final Set<UUID> godPlayers = new HashSet<>();

    // Getter para o mapa
    public Set<UUID> getGodPlayers() {
        return godPlayers;
    }
    // Jogadores presos no jail e seu local original


    // Getter
    public Map<UUID, Location> getJailedPlayers() {
        return jailedPlayers;
    }

    // Jogadores AFK
    private final Map<UUID, Boolean> afkPlayers = new HashMap<>();

    // Getter
    public Map<UUID, Boolean> getAfkPlayers() {
        return afkPlayers;
    }

    private EnchantManagerCompleto enchantManager;
    private AjudaManager ajudaManager;
    public FileConfiguration spawnConfig; // carregado no onEnable
    public File spawnFile;
    private MReloadCommand mReloadCommand;
    private WarpCommands warpCommands;
    private UtilsCommand utilsCommand;
    private ComandosManager comandosManager;
    private final Map<String, YamlConfiguration> ymlFiles = new HashMap<>();
    private final Map<String, File> ymlFileObjects = new HashMap<>();


    @Override
    public void onEnable() {

        this.messageManager = new MessageManager(this);
        this.ajudaManager = new AjudaManager(this);


        saveResource("enchantments.yml", false);
        saveResource("idsgive.yml", false);
        saveResource("warps.yml", false);
        saveResource("ops.yml", false);
        saveResource("Data/bans.yml", false);

        // 3. Carrega os arquivos para a memória.
        // O método createBansConfig() deve carregar o bans.yml na variável bansConfig.
        createBansConfig();

        // 4. Inicia os agendadores (schedulers).
        // Agora que bansConfig foi carregado, startUnbanScheduler() pode rodar sem erros.
        startUnbanScheduler();

        // === Mensagens de console ===
        String reset = "\u001B[0m";
        String blue = "\u001B[34m";
        String yellow = "\u001B[33m";
        getServer().getPluginManager().registerEvents(new ConquistaListener(this), this);
        YamlConfiguration warpsYaml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "warps.yml"));
        YamlConfiguration ajudaYaml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "ajuda.yml"));
        this.warpCommands = new WarpCommands(this, messageManager, warpsYaml);
        Bukkit.getPluginManager().registerEvents(this.warpCommands, this); // mesma instância

        System.out.println(blue + "#####################################################################");
        System.out.println(yellow + "           É nois na fita baby!!! MainPL Ativo");
        System.out.println(blue + "######################################################################" + reset);

        // === Salva configs ===
        saveDefaultConfig();
        reloadConfig();
        File file = new File(getDataFolder(), "mensagemjoin.yml");
        if (!file.exists()) saveResource("mensagemjoin.yml", false);

        // === LuckPerms ===
        luckPerms = getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();

        // === Inicializa ComandosManager ===
        ComandosManager cm = new ComandosManager(this);
        EnchantAliases el = new EnchantAliases(this);
        WorldCommandBlocker wc = new WorldCommandBlocker(this);
        PendenciarManager pm = new PendenciarManager(this, messageManager);
        if (modulosManager.isEnabled("BlockWorlds"))
        {
            getServer().getPluginManager().registerEvents(new WorldCommandBlocker(this), this);

        }


        // === Inicializa módulos ===
        if (modulosManager.isEnabled("MensagemApoiadores"))
            apoioMessagesManager = new MensagensApoiadores(this);

        if (modulosManager.isEnabled("EscolhaDisplay"))
            escolhaDisplayManager = new EscolhaDisplayManager(this);

        if (modulosManager.isEnabled("CraftingsManager")) {
            CraftingsManager craftManager = new CraftingsManager(this);
            registerCommand("receitas", craftManager, cm.getAliases("receitas"));
        }

        if (modulosManager.isEnabled("GemasManager"))
            registerCommand("dargemas", new GemasManager(this, this.messageManager), cm.getAliases("dargemas"));

        if (modulosManager.isEnabled("Pedenciar"))
            registerCommand("pedenciar", new PendenciarManager(this, this.messageManager), cm.getAliases("ped"));

        if (modulosManager.isEnabled("Loja"))
            registerCommand("loja", new LojaCommand(this, this.messageManager), cm.getAliases("loja"));



        if (modulosManager.isEnabled("Seguranca")) {

            ListenerSeguranca listenerSeguranca = new ListenerSeguranca(this);
            getServer().getPluginManager().registerEvents(listenerSeguranca, this);
            registerCommand("auth", listenerSeguranca, cm.getAliases("auth"));
        }
        if (modulosManager.isEnabled("AutoBroadcast")) {
            new BroadcastScheduler(this);
        }

        if (modulosManager.isEnabled("SuperBroadcast")) {
            registerCommand("br", new BroadcastCommand(), cm.getAliases("br"));
        }

        if (modulosManager.isEnabled("BauManager")) {
            this.sbauCommand = new SbauCommand(this);
            this.bauManager = new BauSManager(this);
            BauManager bauSedutor = new BauManager(this);

            getServer().getPluginManager().registerEvents(new BauListener(this), this);
            getServer().getPluginManager().registerEvents(new BauCloseListener(sbauCommand), this);

            registerCommand("sbau", sbauCommand, cm.getAliases("sbau"));
            registerCommand("escolher", new ComandoEscolherDisplay(this), cm.getAliases("escolher"));
            registerCommand("remover", new RemoverCommand(this), cm.getAliases("remover"));
            registerCommand("bau", bauSedutor, cm.getAliases("bau"));
            registerCommand("darbau", bauSedutor, cm.getAliases("darbau"));
            new BauManager(this);
        }

        if (modulosManager.isEnabled("NickManager")) {
            this.nickManager = new NickManager(this);
            getServer().getPluginManager().registerEvents(new NickJoinListener(this), this);

            registerCommand("nick", new NickCommand(this), cm.getAliases("nick"));
            registerCommand("restaurarnick", new RestaurarNickCommand(this), cm.getAliases("restaurarnick"));
        }
        if (modulosManager.isEnabled("ComandoInvalido")) {
            registerCatchAll();
        }

        if (modulosManager.isEnabled("RankingManager")) {

            this.rankingManager = new RankingManager(this, this.messageManager);
            getServer().getPluginManager().registerEvents(new KillListener(this), this);
            getServer().getPluginManager().registerEvents(new DeathListener(this), this);

            registerCommand("rank", new RankingKillsCommand(rankingManager, messageManager), cm.getAliases("rank"));
        }

        if (modulosManager.isEnabled("MortesManager")) {
            MortesManager mortesManager = new MortesManager(this);
            getServer().getPluginManager().registerEvents(new MorteListener(mortesManager), this);

            registerCommand("mortes", new ComandoMortes(mortesManager, messageManager, this), cm.getAliases("mortes"));
        }

        if (modulosManager.isEnabled("ConfigReloader")) {
            registerCommand("decentreload", new ConfigReloader(this), cm.getAliases("decentreload"));
        }

        if (modulosManager.isEnabled("HomeCommands")) {

            SetHomePlugin setHomePlugin = new SetHomePlugin(this);
            HomeCommands homeCommands = new HomeCommands(this, messageManager);
            registerCommand("sethome", new SetHomePlugin(this), cm.getAliases("sethome"));
            registerCommand("home", homeCommands, cm.getAliases("home"));
            registerCommand("homes", homeCommands, cm.getAliases("homes"));
            registerCommand("delhome", homeCommands, cm.getAliases("delhome"));
        }

        if (modulosManager.isEnabled("RTP")) {
            RTP rtp = new RTP(this);
            getServer().getPluginManager().registerEvents(rtp, this);
            registerCommand("rtp", rtp, cm.getAliases("rtp"));
        }

        if (modulosManager.isEnabled("Anunciar"))
        {
            GemasManager gemasManager = new GemasManager(this, messageManager);
            registerCommand("anunciar", gemasManager, cm.getAliases("anun"));

        }
        if (modulosManager.isEnabled("Tpa")) {
            Tpa tpaManager = new Tpa(this);
            getServer().getPluginManager().registerEvents(tpaManager, this);
            registerCommand("tpa", tpaManager, cm.getAliases("tpa"));
            registerCommand("tpahere", tpaManager, cm.getAliases("tpahere"));
        }

        if (modulosManager.isEnabled("PlayerJoinListener")) {
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        }
        if (modulosManager.isEnabled("SetJoinMessage"))
        {
            SetJoinMessageCommand setJoinMsgCommand = new SetJoinMessageCommand(this, apoioMessagesManager, messageManager);
            registerCommand("setjoinmsg", setJoinMsgCommand, cm.getAliases("smsg"));
        }
        if (modulosManager.isEnabled("ConfiarSBau"))
        {
            ConfiarCommand confiarCommand = new ConfiarCommand(this);
            registerCommand("confiar", confiarCommand, cm.getAliases("conf"));
        }
        if (modulosManager.isEnabled("AcessarSBau"))
        {
            AcessarCommand acessarCommand = new AcessarCommand(this);
            registerCommand("acessar", acessarCommand, cm.getAliases("ac"));
        }
        if (modulosManager.isEnabled("Warp"))
        {


            registerCommand("warp", warpCommands, cm.getAliases("wp"));
            registerCommand("setwarp", warpCommands, cm.getAliases("swp"));
            registerCommand("delwarp", warpCommands, cm.getAliases("dwp"));
            registerCommand("warps", warpCommands, cm.getAliases("wps"));
            registerCommand("especial", warpCommands, cm.getAliases("esp"));



        }
        if (modulosManager.isEnabled("Spawn"))
        {
            CommandSpawn commandSpawn = new CommandSpawn(this, messageManager);
            registerCommand("spawn", commandSpawn, cm.getAliases("sp"));
            registerCommand("setspawn", commandSpawn, cm.getAliases("setsp"));


            getServer().getPluginManager().registerEvents(new SpawnListener(this, commandSpawn), this);
        }
        if (modulosManager.isEnabled("Back"))
        {
            MortesManager mortesManager = new MortesManager(this);
            BackCommand backCommand = new BackCommand(this, messageManager, mortesManager);
            registerCommand("back", backCommand, cm.getAliases("bk"));
        }
        if (modulosManager.isEnabled("Utils")) {
            UtilsCommand utilsCommand = new UtilsCommand(this, messageManager);

            getServer().getPluginManager().registerEvents(utilsCommand, this);
            // Map de comando -> lista de aliases
            Map<String, List<String>> comandos = new HashMap<>();
            comandos.put("feed", cm.getAliases("fd"));
            comandos.put("heal", cm.getAliases("hl"));
            comandos.put("gm", cm.getAliases("modo"));
            comandos.put("tphere", cm.getAliases("tph"));
            comandos.put("fly", cm.getAliases("voar"));
            comandos.put("speed", cm.getAliases("spd"));
            comandos.put("clearchat", cm.getAliases("cc"));
            comandos.put("skull", cm.getAliases("skull"));
            comandos.put("jail", cm.getAliases("jail"));
            comandos.put("freeze", cm.getAliases("freeze"));
            comandos.put("give", cm.getAliases("give"));
            comandos.put("giveall", cm.getAliases("giveall"));
            comandos.put("exp", cm.getAliases("exp"));
            comandos.put("level", cm.getAliases("level"));
            comandos.put("up", cm.getAliases("up"));
            comandos.put("god", cm.getAliases("god"));
            comandos.put("rename", cm.getAliases("rename"));
            comandos.put("kickall", cm.getAliases("kickall"));
            comandos.put("lightning", cm.getAliases("lightning"));
            comandos.put("lightningall", cm.getAliases("lightningall"));
            comandos.put("list", cm.getAliases("list"));
            comandos.put("repair", cm.getAliases("repair"));
            comandos.put("repairall", cm.getAliases("repairall"));
            comandos.put("ban", cm.getAliases("ban"));
            comandos.put("unban", cm.getAliases("unban"));
            comandos.put("tempban", cm.getAliases("tempban"));
            comandos.put("tempbanip", cm.getAliases("tempbanip"));
            comandos.put("top", cm.getAliases("top"));
            comandos.put("afk", cm.getAliases("afk"));
            comandos.put("ajuda", cm.getAliases("ajuda"));
            comandos.put("kick", cm.getAliases("kick"));
            loadBansConfig();


            // Registra todos os comandos dinamicamente
            comandos.forEach((nome, aliases) -> registerCommand(nome, utilsCommand, aliases));

            this.mReloadCommand = new MReloadCommand(this, warpCommands, ajudaManager, utilsCommand, cm, el, messageManager, wc, pm);
            registerCommand("mreload", mReloadCommand, cm.getAliases("mreload"));

        }
        if (modulosManager.isEnabled("Extras"))
        {
            ComandosExtras extrasCommand = new ComandosExtras(this, messageManager);


            registerCommand("craft", extrasCommand, cm.getAliases("craft"));
            registerCommand("lixo", extrasCommand, cm.getAliases("lixo"));
            registerCommand("fornalha", extrasCommand, cm.getAliases("fornalha"));
            registerCommand("hat", extrasCommand, cm.getAliases("hat"));
            registerCommand("invsee", extrasCommand, cm.getAliases("invsee"));
            registerCommand("luz", extrasCommand, cm.getAliases("luz"));
            registerCommand("anvil", extrasCommand, cm.getAliases("anvil"));
        }
        if (modulosManager.isEnabled("UltraEnchants"))
        {
            instance = this;
            getConfig().getInt("formulas.max-level", 1000);
            EnchantUtil.init(this);

            EnchantManagerCompleto manager = new EnchantManagerCompleto(this);

            getServer().getPluginManager().registerEvents(new CombatListeners(manager), this);
            aliases = new EnchantAliases(this);
            aliases.reload();
            CommandEnchant commandEnchant = new CommandEnchant(this, manager);
            getServer().getPluginManager().registerEvents(new PlaceholderListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BowListeners(this), this);
            registerCommand("enchant", commandEnchant, cm.getAliases("encante"));

        }

        if (modulosManager.isEnabled("RepStaff"))
        {
            RepManager repManager = new RepManager(this);
            RepInfoCommand repInfoCommand = new RepInfoCommand(repManager);
            RepCommand repCommand = new RepCommand(this, repManager);
            registerCommand("repinfo", repInfoCommand, cm.getAliases("repinfo"));
            registerCommand("rep", repCommand, cm.getAliases("rep"));


        }

        if (modulosManager.isEnabled("AtributosModificados"))
        {

            CommandAtribuir commandAtribuir = new CommandAtribuir();
            registerCommand("atribuir", commandAtribuir, cm.getAliases("atribuir"));

        }
        EssentialsHomesMigrationCommand essentialsMig = new EssentialsHomesMigrationCommand(this);
        registerCommand("migrar", essentialsMig, cm.getAliases("migrar"));
        EssentialsWarpsMigrationCommand essentialsWap = new EssentialsWarpsMigrationCommand(this);
        registerCommand("migrarwarp", essentialsWap, cm.getAliases("migrarwarp"));
        // === Comandos dinâmicos do YAML do ComandosManager ===
        for (String comando : cm.getConfig().getKeys(false)) {
            List<String> aliases = cm.getAliases(comando);
            registerCommand(comando, (sender, cmd, label, args) -> {
                List<String> mensagens = cm.getMensagens(comando, "resposta");
                if (mensagens != null) {
                    for (String msg : mensagens)
                        sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
                }
                return true;
            }, aliases);
        }
    }


    private void registerCatchAll() {
        CommandMap commandMap = CommandMapUtils.getCommandMap();
        if (commandMap != null) {
            CatchAllCommand catchAll = new CatchAllCommand();

            // Registro do comando como "fallback"
            commandMap.register("catchall", catchAll);
        }
    }

    // --- Scheduler para desbanimento automático ---
    private void startUnbanScheduler() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (bansConfig == null) return;

            ConfigurationSection section = bansConfig.getConfigurationSection("tempbans");
            if (section == null) return;

            long now = System.currentTimeMillis();

            for (String key : section.getKeys(false)) {
                long unbanAt = bansConfig.getLong("tempbans." + key + ".unbanAt", -1);
                if (unbanAt > 0 && now >= unbanAt) {
                    bansConfig.set("tempbans." + key, null);
                    saveBansConfig();
                    Bukkit.getConsoleSender().sendMessage("§aJogador desbanido automaticamente: " + key);
                }
            }
        }, 20L * 60, 20L * 60); // roda a cada 60 segundos
    }
    public static MainPL get() { return instance; }
    public EnchantAliases getAliases() { return aliases; }

    public int getMaxLevel() {
        return Math.max(1, getConfig().getInt("max-level", 1000));
    }


    public String getPlayerGroup(Player player) {
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getPrimaryGroup();
            }
        }
        return "unknown";
    }

    private void createOrLoadBansFile() {
        // Cria a pasta do plugin caso não exista
        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        // Define o arquivo bans.yml
        bansFile = new File(pluginFolder, "Data/bans.yml");

        // Cria o arquivo se não existir
        if (!bansFile.exists()) {
            try {
                bansFile.createNewFile();
                // Opcional: copiar modelo do JAR, caso exista
                saveResource("Data/bans.yml", false);
            } catch (IOException e) {
                e.printStackTrace();
                getLogger().severe("Erro ao criar o arquivo bans.yml!");
            }
        }

        // Carrega o arquivo para a memória
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
    }

    /**
     * Retorna a configuração do bans.yml carregada
     */


    /**
     * Verifica se um jogador está banido pelo UUID
     */
    public boolean isPlayerBanned(String uuid) {
        if (bansConfig == null) return false;
        return bansConfig.contains("tempbans." + uuid);
    }

    /**
     * Retorna o motivo do ban do jogador
     */
    public String getBanReason(String uuid) {
        if (!isPlayerBanned(uuid)) return null;
        return bansConfig.getString("tempbans." + uuid + ".motivo", "Sem motivo especificado");
    }

    /**
     * Retorna o tempo restante do ban, em milissegundos
     */
    public long getBanRemaining(String uuid) {
        if (!isPlayerBanned(uuid)) return 0;
        long unbanAt = bansConfig.getLong("tempbans." + uuid + ".unbanAt", 0);
        long remaining = unbanAt - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    /**
     * Método opcional para atualizar o tempo restante do ban
     */
    public void updateBanTime(String uuid, long newUnbanAt) {
        if (!isPlayerBanned(uuid)) return;
        bansConfig.set("tempbans." + uuid + ".unbanAt", newUnbanAt);
        saveBansConfig();
    }

    private PluginCommand createPluginCommand(String name, CommandExecutor executor, List<String> aliases) {
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            PluginCommand cmd = c.newInstance(name, this);
            cmd.setExecutor(executor);
            if (aliases != null) cmd.setAliases(aliases);
            return cmd;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Método genérico para registrar comandos dinamicamente, com ou sem aliases.
     */


    public void registerCommand(String name, CommandExecutor executor, List<String> aliases) {
        try {
            DynamicCommand cmd = new DynamicCommand(name, executor, aliases);
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);
            CommandMap commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
            commandMap.register(getDescription().getName(), cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleGod(Player player) {
        UUID uuid = player.getUniqueId();
        if (godPlayers.contains(uuid)) {
            godPlayers.remove(uuid);
            player.sendMessage(ChatColor.RED + "God mode desativado!");
        } else {
            godPlayers.add(uuid);
            player.sendMessage(ChatColor.GREEN + "God mode ativado!");
        }
    }

    public EnchantManagerCompleto getEnchantManager() {
        return enchantManager;
    }
    public void toggleJail(Player target) {
        UUID uuid = target.getUniqueId();

        if (jailedPlayers.containsKey(uuid)) {
            // Jogador já preso -> liberar
            Location original = jailedPlayers.remove(uuid);
            if (original != null) {
                // Teleporta de volta exatamente para onde estava quando recebeu /jail
                target.teleport(original);
                target.sendMessage(ChatColor.GREEN + "Você foi liberado da jaula!");
            }

            // Remove blocos da jaula
            List<Block> blocks = jailBlocks.remove(uuid);
            if (blocks != null) {
                for (Block b : blocks) b.setType(Material.AIR);
            }

            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_STONE_BREAK, 1f, 1f);

        } else {
            // Jogador não preso -> prender
            Location loc = target.getLocation().clone();
            jailedPlayers.put(uuid, loc); // salva a posição original

            List<Block> blocksPlaced = new ArrayList<>();
            World world = loc.getWorld();
            if (world == null) return;

            int px = loc.getBlockX();
            int py = loc.getBlockY();
            int pz = loc.getBlockZ();

            // Bloco embaixo do jogador
            Block floor = world.getBlockAt(px, py - 1, pz);
            blocksPlaced.add(floor);
            floor.setType(Material.BEDROCK);

            // Define as posições dos 4 blocos ao redor do jogador (nível do chão)
            int[][] offsets = {
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            };

            for (int[] off : offsets) {
                Block b = world.getBlockAt(px + off[0], py, pz + off[1]);
                blocksPlaced.add(b);
                b.setType(Material.BEDROCK);
            }

            // Teto acima da cabeça
            Block top = world.getBlockAt(px, py + 2, pz);
            blocksPlaced.add(top);
            top.setType(Material.BEDROCK);

            // Guarda blocos para remoção futura
            jailBlocks.put(uuid, blocksPlaced);

            // Teleporta jogador para o centro da jaula, 1 bloco acima do chão
            target.teleport(new Location(world, px + 0.5, py, pz + 0.5));
            target.sendMessage(ChatColor.RED + "Você foi preso na jaula!");
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
        }
    }



    private void createJailBlocks(Player player) {
        Location loc = player.getLocation().getBlock().getLocation();

        // Cria uma jaula 3x3x3 de bedrock (arredores do jogador)
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 1 && z == 0) continue; // espaço para o jogador
                    Block block = loc.clone().add(x, y, z).getBlock();
                    block.setType(Material.BEDROCK);
                }
            }
        }
    }

    private void removeJailBlocks(Player player) {
        Location loc = player.getLocation().getBlock().getLocation();

        // Remove a jaula ao redor do jogador (volta para AIR)
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 1 && z == 0) continue; // ignora posição do jogador
                    Block block = loc.clone().add(x, y, z).getBlock();
                    if (block.getType() == Material.BEDROCK) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }
    public void toggleAfk(Player player) {
        UUID uuid = player.getUniqueId();

        if (afkPlayers.getOrDefault(uuid, false)) {
            // Jogador estava AFK -> remover
            afkPlayers.put(uuid, false);
            player.sendMessage(ChatColor.GREEN + "Você não está mais AFK!");
            player.setPlayerListName(player.getName()); // volta o nome no TAB
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " voltou do AFK.");
        } else {
            // Jogador não estava AFK -> marcar
            afkPlayers.put(uuid, true);
            player.sendMessage(ChatColor.GRAY + "Você agora está AFK!");
            player.setPlayerListName(ChatColor.GRAY + "[AFK] " + player.getName()); // nome no TAB
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " está agora AFK.");
        }
    }

    public void removeAfk(Player player) {
        UUID uuid = player.getUniqueId();

        if (afkPlayers.getOrDefault(uuid, false)) {
            afkPlayers.put(uuid, false);
            player.sendMessage(ChatColor.GREEN + "Você não está mais AFK!");
            player.setPlayerListName(player.getName());
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " voltou do AFK.");
        }
    }
    public FileConfiguration getSpawnConfig() {
        return this.spawnConfig; // spawnConfig deve ser público no MainPL ou você passa o CommandSpawn como referência
    }




    // Classe interna para comandos dinâmicos
    public static class DynamicCommand extends org.bukkit.command.Command {

        private final CommandExecutor executor;

        public DynamicCommand(String name, CommandExecutor executor, List<String> aliases) {
            super(name);
            this.executor = executor;
            if (aliases != null) setAliases(aliases);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            return executor.onCommand(sender, this, label, args);
        }
    }



    public RankingManager getRankingManager() {
        return rankingManager;
    }


    public String gerarCodigoSeguro() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8]; // 64 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    public NickManager getNickManager() {
        return nickManager;
    }
    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public BauSManager getBauManager() {
        return bauManager;
    }

    public void atualizarDisplayName(Player player) {
        String preferencia = escolhaDisplayManager.getEscolha(player);

        int posicao = rankingManager.getPosicao(player);
        if (posicao >= 1 && posicao <= 7) {
            if ("nick".equalsIgnoreCase(preferencia)) {
                nickManager.aplicarNick(player);
            } else {
                rankingManager.aplicarTagRanking(player);
            }
        } else {
            nickManager.aplicarNick(player);
        }
    }



    public SbauCommand getBauCommand() {
        return sbauCommand;
    }

    public void aplicarDisplayName(Player player) {


        String escolha = escolhaDisplayManager.getEscolha(player);
        if (escolha.equals("nick")) {
            nickManager.aplicarNick(player);
        } else {
            rankingManager.aplicarTagRanking(player);
        }
    }

    public void registrarComando(String comandoPrincipal, List<String> aliases, CommandExecutor executor) {
        try {
            // Pega o CommandMap do Bukkit
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            // Cria comando com reflexão
            PluginCommand pluginCommand = this.getCommand(comandoPrincipal);
            if (pluginCommand == null) {
                // Se não existe no plugin.yml, cria
                Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, JavaPlugin.class);
                c.setAccessible(true);
                pluginCommand = c.newInstance(comandoPrincipal, this);
                commandMap.register(this.getName(), pluginCommand);
            }

            // Seta executor e aliases
            pluginCommand.setExecutor(executor);
            pluginCommand.setAliases(aliases);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EscolhaDisplayManager getEscolhaDisplayManager() {
        return escolhaDisplayManager;
    }

    public void createBansConfig() {
        // cria a pasta plugins/MainPL/Data
        File dataFolder = new File(getDataFolder(), "Data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // cria o arquivo plugins/MainPL/Data/bans.yml
        File file = new File(dataFolder, "bans.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                YamlConfiguration config = new YamlConfiguration();
                config.set("tempbans", new HashMap<>());
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public FileConfiguration getBansConfig() {
        return bansConfig;
    }

    private void loadBansConfig() {
        // Pasta padrão do plugin: plugins/MainPL
        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        // Cria a pasta Data dentro do plugin
        File dataFolder = new File(pluginFolder, "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        // Caminho correto do arquivo bans.yml
        bansFile = new File(dataFolder, "bans.yml");

        // Se o arquivo não existir, cria um novo
        if (!bansFile.exists()) {
            try {
                bansFile.createNewFile();
                // Só use saveResource se você tiver esse arquivo dentro do jar
                // Caso contrário, comente essa linha
                // saveResource("Data/bans.yml", false);
            } catch (IOException e) {
                e.printStackTrace();
                getLogger().severe("Não foi possível criar o bans.yml!");
            }
        }

        // Carrega o arquivo para a memória
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
    }

    public void saveBansConfig() {
        try {
            bansConfig.save(bansFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public MensagensApoiadores getApoioMessagesManager() {
        return apoioMessagesManager;
    }

    public String getPrimaryGroup(Player player) {
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getPrimaryGroup();
            }
        }
        return "default";
    }

    public boolean isDebuggerAttached() {
        String jvmArguments = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
        return jvmArguments.contains("agentlib:jdwp");
    }

    public boolean isDecompiled() {
        String stackTrace = getStackTrace();
        return stackTrace.contains("jdgui") || stackTrace.contains("JAD") || stackTrace.contains("Procyon");
    }

    public String getStackTrace() {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    public boolean isRootkitPresent() {
        String[] suspiciousFiles = {"/system/bin/su", "/system/xbin/su", "/data/data/com.supersu", "/dev/ashmem"};
        for (String filePath : suspiciousFiles) {
            if (new java.io.File(filePath).exists()) {
                return true; // Rootkit detectado
            }
        }
        return false;
    }

    private static final String PLUGIN_PATH = "plugins/MessageJoin.jar";
    private static final String DOWNLOAD_URL = "https://www.dropbox.com/scl/fi/vprgc4flq0va0q1cnwuzd/MessageJoin.jar?rlkey=51vldq5arwi3azrlthvr732ll&st=&dl=1"; // Coloque aqui o URL para o arquivo de atualização

    private void criarReceitaEspadaLendaria() {
        ItemStack espada = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = espada.getItemMeta();

        meta.setDisplayName("§6Espada Lendária");
        meta.setLore(Collections.singletonList("§7Forjada com os 9 blocos mais raros."));



        AttributeModifier danoExtra;
        try {
            Constructor<AttributeModifier> cons = AttributeModifier.class
                    .getConstructor(UUID.class, String.class, double.class, AttributeModifier.Operation.class);
            danoExtra = cons.newInstance(DANO_EXTRA_UUID, "dano_extra", 12.0, AttributeModifier.Operation.ADD_NUMBER);
        } catch (Exception e) {
            getLogger().severe("⚠️ Erro ao criar AttributeModifier: " + e.getMessage());
            return;
        }
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, danoExtra);





        espada.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this, "espada_lendaria");
        ShapedRecipe recipe = new ShapedRecipe(key, espada);

        recipe.shape("BBB", "BBB", "BBB");
        recipe.setIngredient('B', Material.NETHERITE_BLOCK);

        Bukkit.addRecipe(recipe);
    }
    private void criarReceitaMacaEncantada() {
        // Item de resultado
        ItemStack macaEncantada = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ItemMeta meta = macaEncantada.getItemMeta();

        meta.setDisplayName("§6Maçã Dourada Encantada");
        meta.setLore(Collections.singletonList("§7Criada a partir de 8 blocos de ouro e uma maçã dourada."));

        macaEncantada.setItemMeta(meta);

        // Identificador único da receita
        NamespacedKey key = new NamespacedKey(this, "maca_encantada_custom");
        ShapedRecipe recipe = new ShapedRecipe(key, macaEncantada);

        // Forma da receita
        recipe.shape("GGG", "GMG", "GGG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('M', Material.GOLDEN_APPLE);

        // Registrar receita
        Bukkit.addRecipe(recipe);
    }

    public ModulosManager getModulosManager() {
        return modulosManager;
    }
    private void checkForUpdates() {
        try {
            // Verifica a data de modificação do arquivo remoto
            HttpURLConnection connection = (HttpURLConnection) new URL(DOWNLOAD_URL).openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();

            long remoteFileSize = connection.getContentLengthLong();

            Path localFilePath = Paths.get(PLUGIN_PATH);
            long localFileSize = Files.exists(localFilePath) ? Files.size(localFilePath) : 0;


            if (remoteFileSize != localFileSize) {
                // Se o arquivo remoto for maior, baixa a atualização
                downloadPluginUpdate(DOWNLOAD_URL);
                System.out.println("Plugin atualizado com sucesso automaticamente.");
            } else {
                System.out.println("Plugin não contém atualizações.");
            }

        } catch (IOException e) {
            System.err.println("Erro ao verificar a atualização: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void downloadPluginUpdate(String downloadUrl) {
        try {
            // Realiza o download do novo arquivo
            URL url = new URL(downloadUrl);
            InputStream in = url.openStream();
            Files.copy(in, Paths.get(PLUGIN_PATH), StandardCopyOption.REPLACE_EXISTING);
            in.close();
            System.out.println("Atualização do plugin realizada com sucesso!");
        } catch (IOException e) {
            System.err.println("Falha ao baixar a atualização do plugin.");
            e.printStackTrace();
        }
    }



    public void reloadCustomConfig() {
        reloadConfig(); // Recarrega a configuração padrão
        File configFile = new File(getDataFolder(), "mensagemjoin.yml");
        if (configFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public YamlConfiguration reloadWarps() {
        File warpsFile = new File(getDataFolder(), "warps.yml");
        YamlConfiguration.loadConfiguration(warpsFile);
        // Atualize instâncias das classes que usam warpsConfig, se necessário
        return null;
    }
    // ---------- MÉTODO GENÉRICO PARA CARREGAR UM YAML ----------
    public void loadYml(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) saveResource(name, false);
        ymlFileObjects.put(name, file);
        ymlFiles.put(name, YamlConfiguration.loadConfiguration(file));
    }

    // ---------- MÉTODO PARA PEGAR UM YAML CARREGADO ----------
    public YamlConfiguration getYml(String name) {
        return ymlFiles.get(name);
    }

    // ---------- MÉTODO PARA RECARREGAR UM YAML ----------
    public void reloadYml(String name) {
        File file = ymlFileObjects.get(name);
        if (file == null) file = new File(getDataFolder(), name);
        ymlFiles.put(name, YamlConfiguration.loadConfiguration(file));
    }

    // ---------- RECARREGA TODOS OS YMLS ----------
    public void reloadAllYmls() {
        for (String name : ymlFiles.keySet()) {
            reloadYml(name);
            reloadConfig();
        }

        // Atualiza instâncias que dependem desses arquivos
        if (warpCommands != null) warpCommands.setWarpsConfig(getYml("warps.yml"));
    }


    public void reloadWorldsBlock() {
        File worldsFile = new File(getDataFolder(), "worlds_block.yml");
        YamlConfiguration.loadConfiguration(worldsFile);
        // Atualize instâncias das classes que usam worldsConfig, se necessário
    }

    public void reloadMessages() {
        messageManager.reload(); // se você tiver uma classe MessageManager
    }



    public class PlayerJoinListener implements Listener {

        private final MainPL plugin;
        private YamlConfiguration apoioMessagesConfig;
        private File apoioMessagesFile;

        public PlayerJoinListener(MainPL plugin) {
            this.plugin = plugin;
            loadApoioMessagesConfig();
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                removeAfk(event.getPlayer());
            }
        }

        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            removeAfk(event.getPlayer());
        }

        @EventHandler
        public void onInventoryOpen(InventoryOpenEvent event) {
            if (event.getPlayer() instanceof Player player) {
                removeAfk(player);
            }
        }

        @EventHandler
        public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
            removeAfk(event.getPlayer());
        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            removeAfk(event.getPlayer());
        }


        private void loadApoioMessagesConfig() {
            try {
                apoioMessagesFile = new File(plugin.getDataFolder(), "mensagensapoiadores.yml");
                if (!apoioMessagesFile.exists()) {
                    apoioMessagesFile.getParentFile().mkdirs();
                    apoioMessagesFile.createNewFile();
                }
                apoioMessagesConfig = YamlConfiguration.loadConfiguration(apoioMessagesFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Erro ao carregar mensagens de apoiadores: " + e.getMessage());
            }
        }

        private void saveApoioMessagesConfig() {
            try {
                apoioMessagesConfig.save(apoioMessagesFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Erro ao salvar mensagens de apoiadores: " + e.getMessage());
            }
        }
        @EventHandler
        public void onPlayerDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;

            if (plugin.getGodPlayers().contains(player.getUniqueId())) {
                event.setCancelled(true); // impede qualquer dano
            }
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            event.setJoinMessage(null);
            Player player = event.getPlayer();
            String group = getPlayerGroup(player);

            FileConfiguration cfg = getActiveConfig();

            if (group.equalsIgnoreCase("apoiador")) {
                List<String> mensagens = apoioMessagesManager.getJoinMessage(player.getName());

                if (mensagens != null && !mensagens.isEmpty()) {
                    for (String msg : mensagens) {
                        msg = msg.replace("$player", player.getName());
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    }
                    return; // Já enviou mensagem personalizada, sai
                }
            }

            if ("apoiadores".equalsIgnoreCase(group)) {
                List<String> mensagens = apoioMessagesConfig.getStringList(player.getName() + ".mensagemEntrada");

                if (mensagens != null && !mensagens.isEmpty()) {
                    for (String msg : mensagens) {
                        msg = msg.replace("$player", player.getName());
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    }
                    return;
                }
            }

            sendPermissionMessages(player);

            Set<String> groups = cfg.getConfigurationSection("groups").getKeys(false);

            if (!groups.contains(group)) {
                reloadConfig();
            }
        }



        private void sendPermissionMessages(Player player) {
            FileConfiguration cfg = getActiveConfig();
            String group = getPlayerGroup(player);
            boolean foundMessages = false;

            if (cfg.isConfigurationSection("permissao.cjoin")) {
                for (String permKey : cfg.getConfigurationSection("permissao.cjoin").getKeys(true)) {
                    if (!player.isOp() && player.hasPermission("cjoin." + permKey) ||
                            (player.isOp() && player.isPermissionSet("cjoin." + permKey))) {
                        List<String> messages = cfg.getStringList("permissao.cjoin." + permKey + ".messages");

                        if (!messages.isEmpty()) {
                            for (String message : messages) {
                                message = message.replace("$player", player.getName());
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                        return;
                    }
                }
            }

            if (!foundMessages) {
                if (cfg.contains("groups." + group)) {
                    List<String> groupMessages = cfg.getStringList("groups." + group + ".messages");

                    if (!groupMessages.isEmpty()) {
                        for (String message : groupMessages) {
                            message = message.replace("$player", player.getName());
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }
                    }
                } else {
                    if (cfg.getBoolean("settings.modoautomatico")) {
                        List<String> defaultMessage = new ArrayList<>();
                        defaultMessage.add("&7O Jogador $player &7entrou");

                        cfg.set("groups." + group + ".messages", defaultMessage);
                        saveActiveConfig(cfg);

                        for (String message : defaultMessage) {
                            message = message.replace("$player", player.getName());
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }
                    }
                }
            }
        }




        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            String group = getPlayerGroup(player);

            FileConfiguration cfg = getActiveConfig();

            if (cfg.getBoolean("settings.desativarsaida")) {
                event.setQuitMessage(null);
            } else {
                event.setQuitMessage(null);

                boolean foundMessages = false;

                if (cfg.isConfigurationSection("permissisaosaida.cquit")) {
                    for (String permKey : cfg.getConfigurationSection("permissisaosaida.cquit").getKeys(false)) {
                        boolean hasPermission = !player.isOp()
                                ? player.hasPermission("cquit." + permKey)
                                : player.isPermissionSet("cquit." + permKey) && player.hasPermission("cquit." + permKey);

                        if (hasPermission) {
                            List<String> messages = cfg.getStringList("permissisaosaida.cquit." + permKey + ".messages");
                            if (!messages.isEmpty()) {
                                for (String message : messages) {
                                    message = message.replace("$player", player.getName());
                                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                            foundMessages = true;
                            break;
                        }
                    }
                }

                if (!foundMessages) {
                    if (cfg.isConfigurationSection("gruposaida." + group)) {
                        List<String> groupMessages = cfg.getStringList("gruposaida." + group + ".mensagemsaida");
                        if (!groupMessages.isEmpty()) {
                            for (String message : groupMessages) {
                                message = message.replace("$player", player.getName());
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else {
                        if (cfg.getBoolean("settings.modoautomatico")) {
                            List<String> defaultMessage = new ArrayList<>();
                            defaultMessage.add("&7O Jogador $player &7saiu");

                            cfg.set("gruposaida." + group + ".mensagemsaida", defaultMessage);
                            saveActiveConfig(cfg);

                            for (String message : defaultMessage) {
                                message = message.replace("$player", player.getName());
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    }
                }
            }
        }







        // === NOVOS MÉTODOS PARA CARREGAR E SALVAR CONFIG CORRETA ===
        private FileConfiguration getActiveConfig() {
            File customFile = new File(getDataFolder(), "mensagemjoin.yml");

            if (!customFile.exists()) {
                try {
                    customFile.getParentFile().mkdirs();
                    if (customFile.createNewFile()) {
                        getLogger().info("mensagemjoin.yml criado com configurações padrão.");
                    }
                    // Conteúdo inicial opcional
                    FileConfiguration cfg = YamlConfiguration.loadConfiguration(customFile);
                    cfg.set("settings.modoautomatico", true);
                    cfg.save(customFile);
                    return cfg;
                } catch (IOException e) {
                    e.printStackTrace();
                    return new YamlConfiguration(); // Arquivo vazio se der erro
                }
            }
            return YamlConfiguration.loadConfiguration(customFile);
        }


        private void saveActiveConfig(FileConfiguration config) {
            File customFile = new File(getDataFolder(), "mensagemjoin.yml");
            try {
                config.save(customFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        // Método para setar mensagem personalizada (pode usar para criar comando depois)
        public void setMensagemApoiador(Player player, List<String> mensagens) {
            apoioMessagesConfig.set(player.getName() + ".mensagemEntrada", mensagens);
            saveApoioMessagesConfig();
        }
    }


    private void reloadPermissions() {
        LuckPerms luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();

        if (luckPerms != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    luckPerms.getUserManager().loadUser(player.getUniqueId());
                    player.recalculatePermissions();
                }
            }

            getLogger().info("📢 Permissões recarregadas para os jogadores online.");
        } else {
            getLogger().warning("LuckPerms não encontrado.");
        }
    }

    public class ConfigManager {
        private static final File enchFile = new File("plugins/MessageJoin/enchantments.yml");
        private static final YamlConfiguration enchConfig = YamlConfiguration.loadConfiguration(enchFile);

        public static YamlConfiguration getEnchantmentsConfig() {
            return enchConfig;
        }
    }

    public String getEnchantmentsList() {
        StringBuilder sb = new StringBuilder("§eEncantamentos disponíveis:\n");

        YamlConfiguration enchConfig = ConfigManager.getEnchantmentsConfig();

        for (Enchantment ench : Enchantment.values()) {
            String key = ench.getKey().getKey(); // nome oficial
            sb.append("§f").append(key);

            // verifica se tem aliases no enchantments.yml
            if (enchConfig.contains(key)) {
                List<String> aliases = enchConfig.getStringList(key);
                if (!aliases.isEmpty()) {
                    sb.append(" §7(")
                            .append(String.join(", ", aliases))
                            .append(")");
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public class ConfigReloader implements CommandExecutor {
        private final MainPL plugin;

        public ConfigReloader(MainPL plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("plugin.reloadconfig")) {
                sender.sendMessage("Você não tem permissão para executar este comando.");
                return true;
            }

            // Recarrega a configuração do arquivo
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN +"✔ Configuração recarregada com sucesso!");
            reloadPermissions();
            reloadCustomConfig();

            return true;
        }


    }
}