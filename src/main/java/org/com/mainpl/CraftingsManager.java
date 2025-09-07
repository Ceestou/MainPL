package org.com.mainpl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CraftingsManager implements CommandExecutor, Listener {

    private final MainPL plugin;
    private final Map<String, ItemStack[]> receitasMap = new LinkedHashMap<>();
    private final Map<String, ItemStack> resultadoMap = new LinkedHashMap<>();


    private final int[] slotsCraft = {
            1, 2, 3,   // linha 1
            4, 5, 6,   // linha 2
            7, 8, 9    // linha 3
    };

    public CraftingsManager(MainPL plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        registrarReceitas();
    }

    private void carregarReceitasAntigas() {

        ItemStack macaEncantada = criarItem(Material.ENCHANTED_GOLDEN_APPLE, "§6Maçã Dourada Encantada",
                Collections.singletonList("§7Criada a partir de 8 blocos de ouro e uma maçã dourada."));
        ShapedRecipe receitaMaca = new ShapedRecipe(new NamespacedKey(plugin, "maca_encantada_custom"), macaEncantada);
        receitaMaca.shape("GGG","GMG","GGG");
        receitaMaca.setIngredient('G', Material.GOLD_BLOCK);
        receitaMaca.setIngredient('M', Material.GOLDEN_APPLE);
        Bukkit.addRecipe(receitaMaca);

        receitasMap.put("Maçã Dourada Encantada", new ItemStack[]{
                new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.GOLD_BLOCK),
                new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.GOLDEN_APPLE), new ItemStack(Material.GOLD_BLOCK),
                new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.GOLD_BLOCK)
        });
        resultadoMap.put("Maçã Dourada Encantada", macaEncantada);





        ItemStack tridente = criarItem(Material.TRIDENT, "§6Tridente", null);
        ShapedRecipe tridenteRecipe = new ShapedRecipe(new NamespacedKey(plugin, "trident_old"), tridente);
        tridenteRecipe.shape(" I ","ISI"," S ");
        tridenteRecipe.setIngredient('I', Material.NETHERITE_INGOT);
        tridenteRecipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(tridenteRecipe);

        receitasMap.put("Tridente", new ItemStack[]{
                null, new ItemStack(Material.NETHERITE_INGOT), null,
                new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.STICK), new ItemStack(Material.NETHERITE_INGOT),
                null, new ItemStack(Material.STICK), null
        });
        resultadoMap.put("Tridente", tridente);


        ItemStack nameTag = criarItem(Material.NAME_TAG, "§6Name Tag", null);
        ShapedRecipe nameTagRecipe = new ShapedRecipe(new NamespacedKey(plugin, "nametag_old"), nameTag);
        nameTagRecipe.shape(" S ","P  ","   ");
        nameTagRecipe.setIngredient('S', Material.COPPER_BLOCK);
        nameTagRecipe.setIngredient('P', Material.PAPER);
        Bukkit.addRecipe(nameTagRecipe);

        receitasMap.put("Name Tag", new ItemStack[]{
                null, new ItemStack(Material.COPPER_BLOCK), null,
                new ItemStack(Material.PAPER), null, null,
                null, null, null
        });
        resultadoMap.put("Name Tag", nameTag);


        ItemStack saddle = criarItem(Material.SADDLE, "§6Sela", null);
        ShapedRecipe saddleRecipe = new ShapedRecipe(new NamespacedKey(plugin, "saddle_old"), saddle);
        saddleRecipe.shape("LLL","LSL"," I ");
        saddleRecipe.setIngredient('L', Material.LEATHER);
        saddleRecipe.setIngredient('S', Material.STRING);
        saddleRecipe.setIngredient('I', Material.NETHERITE_INGOT);
        Bukkit.addRecipe(saddleRecipe);

        receitasMap.put("Sela", new ItemStack[]{
                new ItemStack(Material.LEATHER), new ItemStack(Material.LEATHER), new ItemStack(Material.LEATHER),
                new ItemStack(Material.LEATHER), new ItemStack(Material.STRING), new ItemStack(Material.LEATHER),
                null, new ItemStack(Material.NETHERITE_INGOT), null
        });
        resultadoMap.put("Sela", saddle);


        ItemStack capaceteCorrente = criarItem(Material.CHAINMAIL_HELMET, "§6Capacete de Corrente", null);
        ShapedRecipe receitaCapacete = new ShapedRecipe(new NamespacedKey(plugin, "capacete_corrente_simples"), capaceteCorrente);
        receitaCapacete.shape("CL");
        receitaCapacete.setIngredient('C', Material.CHAIN);
        receitaCapacete.setIngredient('L', Material.LEATHER_HELMET);
        Bukkit.addRecipe(receitaCapacete);
        receitasMap.put("Capacete de Corrente", new ItemStack[]{
                new ItemStack(Material.CHAIN), new ItemStack(Material.LEATHER_HELMET)
        });
        resultadoMap.put("Capacete de Corrente", capaceteCorrente);


        ItemStack peitoralCorrente = criarItem(Material.CHAINMAIL_CHESTPLATE, "§6Peitoral de Corrente", null);
        ShapedRecipe receitaPeitoral = new ShapedRecipe(new NamespacedKey(plugin, "peitoral_corrente_simples"), peitoralCorrente);
        receitaPeitoral.shape("CL");
        receitaPeitoral.setIngredient('C', Material.CHAIN);
        receitaPeitoral.setIngredient('L', Material.LEATHER_CHESTPLATE);
        Bukkit.addRecipe(receitaPeitoral);
        receitasMap.put("Peitoral de Corrente", new ItemStack[]{
                new ItemStack(Material.CHAIN), new ItemStack(Material.LEATHER_CHESTPLATE)
        });
        resultadoMap.put("Peitoral de Corrente", peitoralCorrente);


        ItemStack calcaCorrente = criarItem(Material.CHAINMAIL_LEGGINGS, "§6Calça de Corrente", null);
        ShapedRecipe receitaCalca = new ShapedRecipe(new NamespacedKey(plugin, "calca_corrente_simples"), calcaCorrente);
        receitaCalca.shape("CL");
        receitaCalca.setIngredient('C', Material.CHAIN);
        receitaCalca.setIngredient('L', Material.LEATHER_LEGGINGS);
        Bukkit.addRecipe(receitaCalca);
        receitasMap.put("Calça de Corrente", new ItemStack[]{
                new ItemStack(Material.CHAIN), new ItemStack(Material.LEATHER_LEGGINGS)
        });
        resultadoMap.put("Calça de Corrente", calcaCorrente);


        ItemStack botasCorrente = criarItem(Material.CHAINMAIL_BOOTS, "§6Botas de Corrente", null);
        ShapedRecipe receitaBotas = new ShapedRecipe(new NamespacedKey(plugin, "botas_corrente_simples"), botasCorrente);
        receitaBotas.shape("CL");
        receitaBotas.setIngredient('C', Material.CHAIN);
        receitaBotas.setIngredient('L', Material.LEATHER_BOOTS);
        Bukkit.addRecipe(receitaBotas);
        receitasMap.put("Botas de Corrente", new ItemStack[]{
                new ItemStack(Material.CHAIN), new ItemStack(Material.LEATHER_BOOTS)
        });
        resultadoMap.put("Botas de Corrente", botasCorrente);

    }


    private void carregarReceitasCustom() {
        File file = new File(plugin.getDataFolder(), "craftings.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("craftings");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            List<String> shape = config.getStringList("craftings." + key + ".shape");
            String resultType = config.getString("craftings." + key + ".result.type");
            if (resultType == null) continue;

            Material resultMat = Material.valueOf(resultType);
            ItemStack result = new ItemStack(resultMat);
            ItemMeta meta = result.getItemMeta();
            if (meta != null) {
                if (config.contains("craftings." + key + ".result.name")) meta.setDisplayName(config.getString("craftings." + key + ".result.name"));
                if (config.contains("craftings." + key + ".result.lore")) meta.setLore(config.getStringList("craftings." + key + ".result.lore"));
                result.setItemMeta(meta);
            }

            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, key), result.clone());

            String r1 = shape.size() > 0 ? shape.get(0) : "___";
            String r2 = shape.size() > 1 ? shape.get(1) : "___";
            String r3 = shape.size() > 2 ? shape.get(2) : "___";
            recipe.shape(r1.replace('_', ' '), r2.replace('_', ' '), r3.replace('_', ' '));

            ItemStack[] matrix = new ItemStack[9];
            ConfigurationSection ingr = config.getConfigurationSection("craftings." + key + ".ingredients");
            if (ingr != null) {
                for (String letter : ingr.getKeys(false)) {
                    char ch = letter.charAt(0);
                    Material mat = Material.valueOf(ingr.getString(letter));
                    recipe.setIngredient(ch, mat);

                    int idx = ch - 'A';
                    if (idx >= 0 && idx < 9) {
                        matrix[idx] = new ItemStack(mat);
                    }
                }
            }


            receitasMap.put(key, matrix);
            resultadoMap.put(key, result);
        }
    }


    private void registrarReceitas() {
        if (plugin.getConfig().getBoolean("receitas_antigas", true)) {
            carregarReceitasAntigas();
        }
        carregarReceitasCustom();


    }



    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 1 && args[0].equalsIgnoreCase("create")) {
            if (!player.hasPermission("mainpl.utils.*") && !player.hasPermission("mainpl.createrecipes")) {
                player.sendMessage("§cVocê não tem permissão para criar receitas.");
                return true;
            }
            Inventory inv = Bukkit.createInventory(player, InventoryType.WORKBENCH, "§bCriar Receita");
            player.openInventory(inv);
            return true;
        }

        int size = ((resultadoMap.size() / 9) + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, "§6Receitas do Servidor");

        for (String nome : resultadoMap.keySet()) {
            ItemStack resultado = resultadoMap.get(nome);
            ItemStack icon = resultado.clone();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6" + nome);
                icon.setItemMeta(meta);
            }
            gui.addItem(icon);
        }


        player.openInventory(gui);
        return true;
    }

    private ItemStack criarItem(Material material, String nome, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(nome);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }


    @EventHandler
    public void aoFecharInventario(InventoryCloseEvent e) {
        String title = e.getView().getTitle();
        if (!title.equals("§bCriar Receita")) return;
        if (!(e.getPlayer() instanceof Player)) return;

        Inventory inv = e.getInventory();


        ItemStack result = inv.getItem(0);
        if (result == null || result.getType() == Material.AIR) return;


        ItemStack[] matrix = new ItemStack[9];
        for (int i = 0; i < slotsCraft.length; i++) {
            matrix[i] = inv.getItem(slotsCraft[i]);
        }


        String recipeId = result.getType().name().toLowerCase() + "_" + System.currentTimeMillis();


        List<String> shape = new ArrayList<>();
        StringBuilder row = new StringBuilder(3);
        Map<Character, Material> ingredients = new LinkedHashMap<>(); // mantém ordem
        for (int i = 0; i < 9; i++) {
            char letter = (char) ('A' + i); // A..I
            ItemStack it = matrix[i];
            if (i % 3 == 0) row = new StringBuilder(3);
            if (it == null || it.getType() == Material.AIR) {
                row.append('_'); // vazio
            } else {
                row.append(letter);
                ingredients.put(letter, it.getType());
            }
            if (i % 3 == 2) shape.add(row.toString());
        }


        File file = new File(plugin.getDataFolder(), "craftings.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("craftings." + recipeId + ".shape", shape);
        for (Map.Entry<Character, Material> en : ingredients.entrySet()) {
            config.set("craftings." + recipeId + ".ingredients." + en.getKey(), en.getValue().name());
        }
        config.set("craftings." + recipeId + ".result.type", result.getType().name());
        if (result.hasItemMeta()) {
            ItemMeta meta = result.getItemMeta();
            if (meta.hasDisplayName()) config.set("craftings." + recipeId + ".result.name", meta.getDisplayName());
            if (meta.hasLore()) config.set("craftings." + recipeId + ".result.lore", meta.getLore());
        }
        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        receitasMap.put(recipeId, matrix);
        resultadoMap.put(recipeId, result.clone());

        ShapedRecipe shaped = new ShapedRecipe(new NamespacedKey(plugin, recipeId), result.clone());

        String r1 = shape.size() > 0 ? shape.get(0).replace('_', ' ') : "   ";
        String r2 = shape.size() > 1 ? shape.get(1).replace('_', ' ') : "   ";
        String r3 = shape.size() > 2 ? shape.get(2).replace('_', ' ') : "   ";

        shaped.shape(r1, r2, r3);

        for (Map.Entry<Character, Material> en : ingredients.entrySet()) {
            shaped.setIngredient(en.getKey(), en.getValue());
        }


        String[] shapedRows = new String[3];

        for (int i = 0; i < 3; i++) {
            String r = shape.size() > i ? shape.get(i) : "___";
            shapedRows[i] = r.replace('_', ' '); // Bukkit usa espaço para slot vazio
        }
        shaped.shape(shapedRows[0], shapedRows[1], shapedRows[2]);

        for (Map.Entry<Character, Material> en : ingredients.entrySet()) {
            shaped.setIngredient(en.getKey(), en.getValue());
        }
        Bukkit.addRecipe(shaped);
        carregarReceitasCustom();

        ((Player) e.getPlayer()).sendMessage("§aReceita salva como §e" + recipeId);
    }




    @EventHandler
    public void aoClicarInventario(InventoryClickEvent e) {
        String title = e.getView().getTitle();

        if (title.equals("§6Receitas do Servidor")) {
            e.setCancelled(true);

            if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()) {
                Player player = (Player) e.getWhoClicked();
                String clickedName = e.getCurrentItem().getItemMeta().getDisplayName().replace("§6", "");

                if (receitasMap.containsKey(clickedName)) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> abrirCrafting(player, clickedName), 1L);
                }
            }
        }


        else if (title.startsWith("§6") && !title.equals("§6Receitas do Servidor")) {
            e.setCancelled(true);
        }
    }


    private void abrirCrafting(Player player, String nomeReceita) {
        Inventory craftGui = Bukkit.createInventory(null, InventoryType.WORKBENCH, "§6" + nomeReceita);
        ItemStack[] matriz = receitasMap.get(nomeReceita);

        if (matriz != null) {
            for (int i = 0; i < matriz.length; i++) {
                if (matriz[i] != null) {
                    craftGui.setItem(slotsCraft[i], matriz[i]);
                }
            }

            ItemStack result = resultadoMap.get(nomeReceita);
            if (result != null) {
                craftGui.setItem(0, result.clone());
            }
        }

        player.openInventory(craftGui);
    }
}
