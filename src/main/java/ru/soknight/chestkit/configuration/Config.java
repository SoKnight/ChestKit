package ru.soknight.chestkit.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import net.md_5.bungee.api.ChatColor;
import ru.soknight.chestkit.ChestKit;
import ru.soknight.chestkit.configuration.KitsListEntry.KitsListEntryState;
import ru.soknight.lib.configuration.AbstractConfiguration;

public class Config extends AbstractConfiguration {
	
    private final Map<KitsListEntryState, KitsListEntry> entries;
    
    public Config(ChestKit plugin) {
    	super(plugin, "config.yml");
    	
    	super.refresh();
    	
    	/*
    	 * Kits list preparing
    	 */
    	
    	this.entries = new HashMap<>();
        Set<String> entrylist = getFileConfig().getConfigurationSection("list-entries").getKeys(false);
        if(entrylist == null || entrylist.isEmpty()) return;
        
        for(String entry : entrylist) {
        	ConfigurationSection section = getFileConfig().getConfigurationSection("list-entries." + entry);
        	try {
        		KitsListEntryState state = KitsListEntryState.valueOf(entry.toUpperCase());
        		
				ChatColor color = ChatColor.valueOf(section.getString("color").toUpperCase());
				boolean bold = section.getBoolean("bold", false);
				boolean italic = section.getBoolean("italic", false);
				boolean strikethrough = section.getBoolean("strikethrough", false);
				boolean underlined = section.getBoolean("underlined", false);
				boolean useHover = section.getBoolean("use-hover", true);
				String hover = section.getString("hover", "").replace("&", "\u00A7");
				
				KitsListEntry listEntry = new KitsListEntry(
						color, bold, italic, strikethrough, underlined, useHover, hover
				);
				
				this.entries.put(state, listEntry);
			} catch (Exception e) {
				plugin.getLogger().severe("Failed to initialize '" + entry + "' list entry from config: " + e.getMessage());
				continue;
			}
        }
    }
    
    public KitsListEntry getKitsListEntry(KitsListEntryState state) {
    	return entries.get(state);
    }
    
}
