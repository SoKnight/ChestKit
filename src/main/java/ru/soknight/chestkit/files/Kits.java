package ru.soknight.chestkit.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import ru.soknight.chestkit.ChestKit;
import ru.soknight.chestkit.utils.Logger;
import ru.soknight.chestkit.utils.Utils;

public class Kits {
	
	private static File folder;
	public static Map<String, Kit> kits;
	public static Map<ItemStack, Kit> playerskits;
	
    public static void refresh() {
    	ChestKit instance = ChestKit.getInstance();
    	folder = new File(instance.getDataFolder(), "kits");
    	
        if(!folder.isDirectory()) {
        	folder.mkdirs();
        	
        	File example = new File(folder, "start.yml");
        	try {
                Files.copy(instance.getResource("start.yml"), example.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Logger.info("Generated new kits folder with kit example.");
            } catch (IOException e) { e.printStackTrace(); }
        }
        
        loadKits();
    }
    
    private static void loadKits() {
    	kits = new HashMap<>();
    	
    	File[] files = folder.listFiles();
    	if(files.length == 0) {
    		Logger.error("Kits files not found in " + folder.getPath() + ".");
    		return;
    	}
    	
    	Material material = Material.valueOf(Config.getString("item.material"));
    	String name = Config.getString("item.name");
    	List<String> lore = Utils.getColoredList(Config.config, "item.lore");
    	boolean enchant = Config.getBoolean("item.enchanted");
    	
    	int counter = 0;
    	for(File file : files) {
    		if(!file.getName().endsWith(".yml")) {
    			Logger.error("File " + file.getName() + " has invalid extension. It must have .yml extension.");
    			continue;
    		}
    		
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
    	    
    	    Kit kit = new Kit(id);
    	    kit.setDisplayname(displayname);
    	    kit.setDelay(delay);
    	    kit.setSingle(single);
    	    kit.setPermreq(permreq);
    	    kit.setPermission(permission);
    	    kit.setOpenable(openable);
    	    kit.setTitle(title);
    	    kit.setRows(rows);
    	    
    	    ItemStack item = new ItemStack(material, 1);
    	    ItemMeta meta = item.getItemMeta();
    	    
    	    List<String> customLore = new ArrayList<>();
    	    lore.forEach(s -> customLore.add(s.replace("%kit%", displayname)));
    	    meta.setLore(customLore);
    	    meta.setDisplayName(name);
    	    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
    	    if(enchant) meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
    	    
    	    item.setItemMeta(meta);
    	    kit.setItem(item);
    	    
    	    if(openable && config.contains("content")) {
    	    	ConfigurationSection content = config.getConfigurationSection("content");
    	    	kit = loadItems(content, kit);
    	    }
    	    
    	    if(config.contains("money-giving")) {
    	    	ConfigurationSection money = config.getConfigurationSection("money-giving");
    	    	kit.setDollars((float) money.getDouble("dollars", 0));
    	    	kit.setEuro((float) money.getDouble("euro", 0));
    	    	kit.setMoneyless(false);
    	    } else kit.setMoneyless(true);
    	    
    	    kits.put(id, kit);
    	    counter++;
    	}
    	
    	Logger.info("Loaded " + counter + " kits.");
    }
    
    private static Kit loadItems(ConfigurationSection config, Kit kit) {
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
    		
    		if(c.isSet("name")) meta.setDisplayName(c.getString("name").replace("&", "\u00A7"));
    		if(c.isSet("lore")) meta.setLore(Utils.getColoredList(c, "lore"));
    		
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
    
    private static String getColoredString(FileConfiguration config, String path, String def) {
    	return config.getString(path, def).replace("&", "\u00A7");
    }
	
}
