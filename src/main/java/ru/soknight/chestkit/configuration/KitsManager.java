package ru.soknight.chestkit.configuration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import lombok.Getter;
import ru.soknight.chestkit.ChestKit;

public class KitsManager {
	
	@Getter private Map<String, KitInstance> kits;
	private final Map<ItemStack, KitInstance> playersKits;
	
	private final File folder;
	private final Logger logger;
	private final Config config;
	
	public KitsManager(ChestKit plugin, Config config) {
		this.folder = new File(plugin.getDataFolder(), "kits");
		this.logger = plugin.getLogger();
		this.config = config;
    	
        if(!folder.isDirectory()) {
        	folder.mkdirs();
        	
        	File example = new File(folder, "start.yml");
        	try {
                Files.copy(plugin.getResource("start.yml"), example.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Generated new kits folder with example kit.");
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
		
		this.playersKits = new HashMap<>();
		
		loadKits();
	}
	
	public KitInstance getLinkedKit(ItemStack item) {
		return this.playersKits.get(item);
	}
	
	public boolean isLinked(ItemStack item) {
		return this.playersKits.containsKey(item);
	}
	
	public void linkKit(ItemStack item, KitInstance kit) {
		this.playersKits.put(item, kit);
	}
    
    private void loadKits() {
    	this.kits = new HashMap<>();
    	
    	File[] files = this.folder.listFiles((FilenameFilter) (dir, name) -> name.endsWith(".yml"));
    	
    	if(files.length == 0) {
    		logger.severe("Kits files not found in: " + folder.getPath());
    		return;
    	}
    	
    	Material material = Material.valueOf(config.getString("item.material").toUpperCase());
    	String name = config.getColoredString("item.name");
    	List<String> lore = config.getColoredList("item.lore");
    	boolean enchant = config.getBoolean("item.enchanted");
    	
    	ItemStack baseItem = new ItemStack(material, 1);
	    ItemMeta baseItemMeta = baseItem.getItemMeta();
	    
	    baseItemMeta.setDisplayName(name);
	    baseItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
	    if(enchant) baseItemMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
	    
	    baseItem.setItemMeta(baseItemMeta);
    	
    	int counter = 0;
    	for(File file : files) {
    		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    		
    		String id = config.getString("id");
    		String displayname = getColoredString(config, "displayname", id);
    		long delay = config.getLong("delay", 1);
    		boolean single = config.getBoolean("single", false);
    	    boolean permreq = config.getBoolean("permission-required", false);
    	    boolean openable = config.getBoolean("openable", true);
    	    String permission = config.getString("permission", "chestkit.kit." + id);
    		String title = getColoredString(config, "interface.title", displayname);
    	    int rows = config.getInt("interface.rows", 6);
    	    
    	    KitInstance kit = new KitInstance(id);
    	    kit.setDisplayname(displayname);
    	    kit.setDelay(delay);
    	    kit.setSingle(single);
    	    kit.setPermreq(permreq);
    	    kit.setPermission(permission);
    	    kit.setOpenable(openable);
    	    kit.setTitle(title);
    	    kit.setRows(rows);
    	    
    	    ItemStack item = baseItem.clone();
    	    ItemMeta meta = item.getItemMeta();
    	    
    	    List<String> customLore = new ArrayList<>();
    	    lore.forEach(s -> customLore.add(s.replace("%kit%", displayname)));
    	    meta.setLore(customLore);
    	    
    	    item.setItemMeta(meta);
    	    kit.setItem(item);
    	    
    	    if(openable && config.contains("content")) {
    	    	ConfigurationSection content = config.getConfigurationSection("content");
    	    	kit = loadItems(content, kit);
    	    }
    	    
    	    if(config.contains("money-giving")) {
    	    	ConfigurationSection money = config.getConfigurationSection("money-giving");
    	    	Set<String> currencies = money.getKeys(false);
    	    	
    	    	if(currencies != null && !currencies.isEmpty()) {
    	    		for(String c : currencies)
    	    			kit.addReward(c, (float) money.getDouble(c, 0));
    	    		kit.setMoneyless(false);
    	    	}
    	    } else kit.setMoneyless(true);
    	    
    	    kits.put(id, kit);
    	    counter++;
    	}
    	
    	logger.info("Loaded " + counter + " kits.");
    }
    
    private KitInstance loadItems(ConfigurationSection config, KitInstance kit) {
    	Set<String> items = config.getKeys(false);
    	
    	Inventory inventory = Bukkit.createInventory(null, kit.getRows() * 9);
    	kit.setContent(inventory);
    	if(items.isEmpty()) return kit;
    	
    	for(String i : items) {
    		ConfigurationSection c = config.getConfigurationSection(i);
    		
    		Material material = Material.valueOf(c.getString("material").toUpperCase());
    		int count = c.getInt("count", 1);
    		
    		ItemStack item = new ItemStack(material, count);
    		ItemMeta meta = item.getItemMeta();
    		
    		if(c.isSet("name")) meta.setDisplayName(getColoredString(config, "name", ""));
    		if(c.isSet("lore")) meta.setLore(getColoredList(c, "lore"));
    		
    		if(c.isSet("enchantments")) {
    			List<String> enchs = c.getStringList("enchantments");
    			if(!enchs.isEmpty())
    				for(String s : enchs) {
    					String[] parts = s.split(";");
    					Enchantment ench = Enchantment.getByName(parts[0].toUpperCase());
    					
    					int level = Integer.parseInt(parts[1]);
    					meta.addEnchant(ench, level, true);
    				}
    		}
    		
    		item.setItemMeta(meta);
    		
    		if(material.toString().endsWith("POTION")) {
    			PotionMeta potionMeta = (PotionMeta) meta;
    			if(c.isSet("potion-effects")) {
    				List<String> effects = c.getStringList("potion-effects");
        			if(!effects.isEmpty())
        				for(String e : effects) {
        					String[] parts = e.split(";");
        					PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
        					
        					int amplifier = Integer.parseInt(parts[1]);
        					int duration = Integer.parseInt(parts[2]) * 20;
        					
        					PotionEffect effect = new PotionEffect(type, duration, amplifier);
        					potionMeta.addCustomEffect(effect, true);
        				}
    			}
    			item.setItemMeta(potionMeta);
    		}
    		
    		if(c.isSet("slot")) {
    			int slot = c.getInt("slot");
    			inventory.setItem(slot, item);
    		}
    		
    		if(c.isSet("slots")) {
    			List<Short> slots = c.getShortList("slots");
    			for(short s : slots) inventory.setItem(s, item);
    		}
    		
    	}
    	
    	kit.setContent(inventory);
    	return kit;
    }
    
    private String getColoredString(ConfigurationSection config, String path, String def) {
    	return config.getString(path, def).replace("&", "\u00A7");
    }
    
    private List<String> getColoredList(ConfigurationSection config, String path) {
		List<String> list = config.getStringList(path);
		if(list.isEmpty()) return list;
		
		List<String> colored = new ArrayList<>();
		for(String s : list) colored.add(s.replace("&", "\u00A7"));
		return colored;
	}
	
}
