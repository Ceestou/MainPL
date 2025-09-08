package org.com.mainpl;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.*;
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
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Field;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.UUID;

public class MainPL extends JavaPlugin {
    private JavaPlugin plugin;
    private LuckPerms luckPerms;
    private MensagensApoiadores apoioMessagesManager;
    private RankingManager rankingManager;
    public static MainPL instance;
    private EnchantAliases aliases;
    private final Map<UUID, Location> jailedPlayers = new HashMap<>();


    private final Map<UUID, List<Block>> jailBlocks = new HashMap<>();
    public final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private File bansFile;
    private FileConfiguration bansConfig;
    private String pendingUpdateMessage = null;

    public MainPL() {

    }


    public MainPL(JavaPlugin plugin) {
        this.plugin = plugin;
        this.luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();

    }
    private NickManager nickManager;
    private BauSManager bauManager;
    private SbauCommand sbauCommand;
    private EscolhaDisplayManager escolhaDisplayManager;
    ModulosManager modulosManager = new ModulosManager(this);
    private MessageManager messageManager;


    private final Set<UUID> godPlayers = new HashSet<>();

    public Set<UUID> getGodPlayers() {
        return godPlayers;
    }

    public Map<UUID, Location> getJailedPlayers() {
        return jailedPlayers;
    }
    private final Map<UUID, Boolean> afkPlayers = new HashMap<>();
    public Map<UUID, Boolean> getAfkPlayers() {
        return afkPlayers;
    }

    private EnchantManagerCompleto enchantManager;
    private AjudaManager ajudaManager;
    public FileConfiguration spawnConfig;
    public File spawnFile;
    private MReloadCommand mReloadCommand;
    private WarpCommands warpCommands;
    private UtilsCommand utilsCommand;
    private ComandosManager comandosManager;
    private final Map<String, YamlConfiguration> ymlFiles = new HashMap<>();
    private final Map<String, File> ymlFileObjects = new HashMap<>();

    @Override
    public void onEnable() {
        checkForUpdate();
        this.messageManager = new MessageManager(this);
        this.ajudaManager = new AjudaManager(this);
        saveResource("enchantments.yml", false);
        saveResource("idsgive.yml", false);
        saveResource("warps.yml", false);
        saveResource("ops.yml", false);
        saveResource("Data/bans.yml", false);
        createBansConfig();
        startUnbanScheduler();
        String reset = "\u001B[0m";
        String blue = "\u001B[95m";
        String yellow = "\u001B[33m";
        getServer().getPluginManager().registerEvents(new ConquistaListener(this), this);
        YamlConfiguration warpsYaml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "warps.yml"));
        YamlConfiguration ajudaYaml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "ajuda.yml"));
        this.warpCommands = new WarpCommands(this, messageManager, warpsYaml);
        Bukkit.getPluginManager().registerEvents(this.warpCommands, this); // mesma instância


        System.out.println(yellow + "");
        System.out.println(yellow + "");
        System.out.println(blue + "  __  __       _       ____  _      ");
        System.out.println(blue + " |  \\/  | __ _(_)_ __ |  _ \\| |     ");
        System.out.println(blue + " | |\\/| |/ _` | | '_ \\| |_) | |     ");
        System.out.println(blue + " | |  | | (_| | | | | |  __/| |___  ");
        System.out.println(blue + " |_|  |_|\\__,_|_|_| |_|_|   |_____| ");
        System.out.println(blue + "                                     ");
        System.out.println(blue + " É nois na fita baby!!! MainPL Ativo");
        System.out.println(blue + "" + reset);
        saveDefaultConfig();
        reloadConfig();
        File file = new File(getDataFolder(), "mensagemjoin.yml");
        if (!file.exists()) saveResource("mensagemjoin.yml", false);
        luckPerms = getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
        ComandosManager cm = new ComandosManager(this);
        EnchantAliases el = new EnchantAliases(this);
        WorldCommandBlocker wc = new WorldCommandBlocker(this);
        PendenciarManager pm = new PendenciarManager(this, messageManager);
        if (modulosManager.isEnabled("BlockWorlds"))
        {
            getServer().getPluginManager().registerEvents(new WorldCommandBlocker(this), this);

        }
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


            commandMap.register("catchall", catchAll);
        }
    }


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
        }, 20L * 60, 20L * 60);
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


    public boolean isPlayerBanned(String uuid) {
        if (bansConfig == null) return false;
        return bansConfig.contains("tempbans." + uuid);
    }

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

            Location original = jailedPlayers.remove(uuid);
            if (original != null) {
                target.teleport(original);
                target.sendMessage(ChatColor.GREEN + "Você foi liberado da jaula!");
            }


            List<Block> blocks = jailBlocks.remove(uuid);
            if (blocks != null) {
                for (Block b : blocks) b.setType(Material.AIR);
            }

            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_STONE_BREAK, 1f, 1f);

        } else {

            Location loc = target.getLocation().clone();
            jailedPlayers.put(uuid, loc);

            List<Block> blocksPlaced = new ArrayList<>();
            World world = loc.getWorld();
            if (world == null) return;

            int px = loc.getBlockX();
            int py = loc.getBlockY();
            int pz = loc.getBlockZ();


            Block floor = world.getBlockAt(px, py - 1, pz);
            blocksPlaced.add(floor);
            floor.setType(Material.BEDROCK);

            int[][] offsets = {
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            };

            for (int[] off : offsets) {
                Block b = world.getBlockAt(px + off[0], py, pz + off[1]);
                blocksPlaced.add(b);
                b.setType(Material.BEDROCK);
            }

            Block top = world.getBlockAt(px, py + 2, pz);
            blocksPlaced.add(top);
            top.setType(Material.BEDROCK);

            jailBlocks.put(uuid, blocksPlaced);

            target.teleport(new Location(world, px + 0.5, py, pz + 0.5));
            target.sendMessage(ChatColor.RED + "Você foi preso na jaula!");
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
        }
    }

    public void toggleAfk(Player player) {
        UUID uuid = player.getUniqueId();

        if (afkPlayers.getOrDefault(uuid, false)) {

            afkPlayers.put(uuid, false);
            player.sendMessage(ChatColor.GREEN + "Você não está mais AFK!");
            player.setPlayerListName(player.getName());
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " voltou do AFK.");
        } else {
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

    public NickManager getNickManager() {
        return nickManager;
    }
    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public BauSManager getBauManager() {
        return bauManager;
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

    public EscolhaDisplayManager getEscolhaDisplayManager() {
        return escolhaDisplayManager;
    }

    public void createBansConfig() {

        File dataFolder = new File(getDataFolder(), "Data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }


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

        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }


        File dataFolder = new File(pluginFolder, "Data");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        bansFile = new File(dataFolder, "bans.yml");
        if (!bansFile.exists()) {
            try {
                bansFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                getLogger().severe("Não foi possível criar o bans.yml!");
            }
        }
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
    }

    public void saveBansConfig() {
        try {
            bansConfig.save(bansFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ModulosManager getModulosManager() {
        return modulosManager;
    }

    private void checkForUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/Ceestou/MainPL/releases/latest");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                conn.disconnect();

                String json = content.toString();
                String rawVersion = json.split("\"tag_name\":\"")[1].split("\"")[0];
                final String remoteVersionFinal = rawVersion.startsWith("v") || rawVersion.startsWith("V")
                        ? rawVersion.substring(1)
                        : rawVersion;

                String localVersion = getDescription().getVersion();

                // Debug: mostra no console
                getLogger().info("[UPDATE CHECK] Versão local: " + localVersion);
                getLogger().info("[UPDATE CHECK] Última versão no GitHub: " + remoteVersionFinal);

                if (!remoteVersionFinal.equals(localVersion)) {
                    String updateArt =
                            "\n" + // 👈 linha em branco no topo
                                    "     _  _____ _   _   _    _     ___ _____   _    ____  /\\/| ___  \n" +
                                    "    / \\|_   _| | | | / \\  | |   |_ _|__  /  / \\  / ___||/\\/ / _ \\ \n" +
                                    "   / _ \\ | | | | | |/ _ \\ | |    | |  / /  / _ \\| |     /_\\| | | |\n" +
                                    "  / ___ \\| | | |_| / ___ \\| |___ | | / /_ / ___ \\ |___ / _ \\ |_| |\n" +
                                    " /_/   \\_\\_|  \\___/_/   \\_\\_____|___/____/_/   \\_\\____/_/ \\_\\___/ \n" +
                                    "                                                   )_)            \n" +
                                    "\n" +
                                    " ____ ___ ____  ____   ___  _   _ _____     _______ _     \n" +
                                    " |  _ \\_ _/ ___||  _ \\ / _ \\| \\ | |_ _\\ \\   / / ____| |    \n" +
                                    " | | | | |\\___ \\| |_) | | | |  \\| || | \\ \\ / /|  _| | |    \n" +
                                    " | |_| | | ___) |  __/| |_| | |\\  || |  \\ V / | |___| |___ \n" +
                                    " |____/___|____/|_|    \\___/|_| \\_|___|  \\_/  |_____|_____| \n" +
                                    "                                                            ";

                    getLogger().warning(updateArt);
                    getLogger().warning(">> Nova versão disponível: " + remoteVersionFinal + " (local: " + localVersion + ")");
                    getLogger().warning(">> Baixe em: https://www.spigotmc.org/resources/mainpl-%E2%AD%90-1-14-x-1-21-x-%E2%AD%90.128702/");
                    getLogger().warning(">> Entre em nosso Discord: https://discord.gg/HhkTKberpQ");
                    pendingUpdateMessage = "§c[MainPL] Há uma atualização disponível: §e"
                            + remoteVersionFinal + " §c(local: " + localVersion + ")\n"
                            + "\n§7 Entre em nosso Discord: §b§nhttps://discord.gg/HhkTKberpQ\n"
                            + "\n§6 Link para Download: §e§nhttps://www.spigotmc.org/resources/mainpl-%E2%AD%90-1-14-x-1-21-x-%E2%AD%90.128702/";

                    // Avisar ops online
                    Bukkit.getScheduler().runTask(this, () -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.isOp()) {
                                p.sendMessage(pendingUpdateMessage);
                            }
                        }
                    });

                } else {
                    getLogger().info("[UPDATE CHECK] MainPL está na versão mais recente (" + localVersion + ")");
                    getLogger().info("[UPDATE CHECK] Entre em nosso discord! https://discord.gg/HhkTKberpQ");

                }

            } catch (Exception e) {
                getLogger().warning("Erro ao verificar atualização: " + e.getMessage());
                e.printStackTrace();
            }
        });
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

    public void reloadCustomConfig() {
        reloadConfig();
        File configFile = new File(getDataFolder(), "mensagemjoin.yml");
        if (configFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        }
    }





    public YamlConfiguration getYml(String name) {
        return ymlFiles.get(name);
    }


    public void reloadYml(String name) {
        File file = ymlFileObjects.get(name);
        if (file == null) file = new File(getDataFolder(), name);
        ymlFiles.put(name, YamlConfiguration.loadConfiguration(file));
    }


    public void reloadAllYmls() {
        for (String name : ymlFiles.keySet()) {
            reloadYml(name);
            reloadConfig();
        }


        if (warpCommands != null) warpCommands.setWarpsConfig(getYml("warps.yml"));
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
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            event.setJoinMessage(null);
            Player player = event.getPlayer();
            if (player.isOp() && pendingUpdateMessage != null) {
                player.sendMessage(pendingUpdateMessage);
            }
            String group = getPlayerGroup(player);

            FileConfiguration cfg = getActiveConfig();

            if (group.equalsIgnoreCase("apoiador")) {
                List<String> mensagens = apoioMessagesManager.getJoinMessage(player.getName());

                if (mensagens != null && !mensagens.isEmpty()) {
                    for (String msg : mensagens) {
                        msg = msg.replace("$player", player.getName());
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    }
                    return;
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
                    return new YamlConfiguration();
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

            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN +"✔ Configuração recarregada com sucesso!");
            reloadPermissions();
            reloadCustomConfig();

            return true;
        }


    }
}