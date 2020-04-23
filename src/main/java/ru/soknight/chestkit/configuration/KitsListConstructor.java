package ru.soknight.chestkit.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import ru.soknight.chestkit.configuration.KitsListEntry.KitsListEntryState;
import ru.soknight.chestkit.date.DateFormatter;

public class KitsListConstructor {
	
	private final Config config;
	private final KitsManager kitsManager;
	
	private final String separator;
	
	public KitsListConstructor(Config config, KitsManager kitsManager) {
		this.separator = config.getColoredString("list-separator");
		this.kitsManager = kitsManager;
		this.config = config;
	}
	
	public String newStringList(Player p, Map<String, Long> kits) {
		Collection<KitInstance> allkits = kitsManager.getKits().values();
		if(allkits.isEmpty()) return null;
		
		String output = "";
		
		for(KitInstance kit : allkits) {
			String text = kit.getDisplayname();
			
			KitsListEntryState state = KitsListEntryState.AVAILABLE;
			if(kits.containsKey(kit.getId()))
				if(kit.isSingle() && !p.hasPermission("kits.use.single"))
					state = KitsListEntryState.SINGLE;
				else if(!kit.isSingle()) {
					long remained = getCooldown(kits.get(kit.getId()), kit.getDelay());
					if(remained != 0) state = KitsListEntryState.COOLDOWNED;
				}
			if(state.equals(KitsListEntryState.AVAILABLE) && kit.isPermreq() && !p.hasPermission(kit.getPermission()))
				state = KitsListEntryState.UNAVAILABLE;
			
			KitsListEntry entry = config.getKitsListEntry(state);
			text = entry.applyStyle(text);
			
			if(!output.equals("")) output += separator;
			output += text;
		}
		return output;
	}

	public TextComponent newHoverList(Player p, Map<String, Long> kits) {
		Set<String> allkits = kitsManager.getKits().keySet();
		if(allkits.isEmpty()) return null;
		
		TextComponent separator = new TextComponent(this.separator);
		List<TextComponent> components = new ArrayList<>();
		
		for(String k : allkits) {
			KitInstance kit = kitsManager.getKits().get(k);
			TextComponent text = new TextComponent(kit.getDisplayname());
			
			KitsListEntryState state = KitsListEntryState.AVAILABLE;
			long remained = 0;
			if(kits.containsKey(k))
				if(kit.isSingle() && !p.hasPermission("kits.use.single")) state = KitsListEntryState.SINGLE;
				else if(!kit.isSingle()) {
					remained = getCooldown(kits.get(k), kit.getDelay());
					if(remained != 0) state = KitsListEntryState.COOLDOWNED;
				}
			if(state.equals(KitsListEntryState.AVAILABLE) && kit.isPermreq() && !p.hasPermission(kit.getPermission()))
				state = KitsListEntryState.UNAVAILABLE;
			
			KitsListEntry entry = config.getKitsListEntry(state);
			String cooldown = remained == 0 ? "" : DateFormatter.getFormatedTime(config, remained);
			text = entry.applyStyle(text, kit, cooldown);
			
			if(!components.isEmpty()) components.add(separator);
			components.add(text);
		}
		
		BaseComponent[] output = new BaseComponent[components.size()];
		for(int i = 0; i < components.size(); i++)
			output[i] = components.get(i);
		
		return new TextComponent(output);
	}
	
	private long getCooldown(long kit, long delay) {
		if((Long) delay == null) delay = 0;
		long current = System.currentTimeMillis() / 60000;
		
		long passed = current - kit / 60000;
		long remained = delay - passed;
		if(remained < 0) remained = 0;
		return remained;
	}
	
}
