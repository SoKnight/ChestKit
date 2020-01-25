package ru.soknight.chestkit.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import ru.soknight.chestkit.ChestKit;
import ru.soknight.chestkit.files.Config.ListEntry.State;
import ru.soknight.chestkit.utils.Logger;

public class Config {
    
    public static FileConfiguration config;
    public static Map<String, ListEntry> entries;
    
    public static void refresh() {
    	ChestKit instance = ChestKit.getInstance();
        if(!instance.getDataFolder().isDirectory()) 
        	instance.getDataFolder().mkdirs();
        
        File file = new File(instance.getDataFolder() + File.separator + "config.yml");
        if(!file.exists()) {
            try { 
                Files.copy(instance.getResource("config.yml"), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Logger.info("Generated new config file.");
            } catch (IOException e) { e.printStackTrace(); }
        }

        config = YamlConfiguration.loadConfiguration(file);
        
        entries = new HashMap<>();
        Set<String> entrylist = config.getConfigurationSection("list-entries").getKeys(false);
        if(entrylist.isEmpty()) return;
        
        for(String e : entrylist) {
        	ConfigurationSection section = config.getConfigurationSection("list-entries." + e);
        	try {
        		State state = State.valueOf(e.toUpperCase());
        		
				ChatColor color = ChatColor.valueOf(section.getString("color").toUpperCase());
				boolean bold = section.getBoolean("bold", false);
				boolean italic = section.getBoolean("italic", false);
				boolean strikethrough = section.getBoolean("strikethrough", false);
				boolean underlined = section.getBoolean("underlined", false);
				boolean useHover = section.getBoolean("use-hover", true);
				String hover = section.getString("hover", "").replace("&", "\u00A7");
				
				ListEntry entry = new ListEntry(color, bold, italic, strikethrough, underlined, useHover, hover);
				Config.entries.put(state.name(), entry);
			} catch (Exception ex) {
				Logger.error("Error while initialization " + e + " list entry from config: " + ex.getMessage());
				continue;
			}
        	
        }
    }
    
    public static String getMessage(String section) {
        return getString("messages." + section);
    }
    
    public static String getString(String path) {
        return config.getString(path, "Not found string " + path + " in config file.").replace("&", "\u00A7");
    }
    
    public static int getInt(String path) {
    	return config.getInt(path, 0);
    }
    
    public static boolean getBoolean(String path) {
    	return config.getBoolean(path, true);
    }
    
    @Data
    @NoArgsConstructor
    public static class ListEntry {
    	private ChatColor color = ChatColor.WHITE;
    	private boolean bold = false, italic = false, strikethrough = false, underlined = false, useHover = true;
    	private String hover;
    	
    	public ListEntry(ChatColor color, boolean bold, boolean italic, boolean strikethrough, boolean underlined, boolean useHover, String hover) {
    		this.color = color;
    		this.bold = bold;
    		this.italic = italic;
    		this.strikethrough = strikethrough;
    		this.underlined = underlined;
    		this.useHover = useHover;
    		this.hover = hover;
    	}
    	
    	public TextComponent applyStyle(TextComponent text, Kit kit, String cooldown) {
    		text.setColor(color);
    		text.setBold(bold);
    		text.setItalic(italic);
    		text.setStrikethrough(strikethrough);
    		text.setUnderlined(underlined);
    		if(useHover) {
    			String customHover = hover.replace("%id%", kit.getId())
    					.replace("%kit%", kit.getDisplayname()).replace("%time%", cooldown);
    			text.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(customHover)));
    		}
			return text;
    	}
    	
    	public String applyStyle(String text) {
    		text = color + text;
    		if(bold) text = ChatColor.BOLD + text;
    		if(italic) text = ChatColor.ITALIC + text;
    		if(strikethrough) text = ChatColor.STRIKETHROUGH + text;
    		if(underlined) text = ChatColor.UNDERLINE + text;
    		return text;
    	}
    	
    	public enum State {
    		AVAILABLE, COOLDOWNED, SINGLE, UNAVAILABLE;
    	}
    	
    }
    
}
