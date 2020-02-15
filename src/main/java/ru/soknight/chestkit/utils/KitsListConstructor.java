package ru.soknight.chestkit.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import ru.soknight.chestkit.files.Config;
import ru.soknight.chestkit.files.Config.ListEntry;
import ru.soknight.chestkit.files.Config.ListEntry.State;
import ru.soknight.chestkit.files.Kit;
import ru.soknight.chestkit.files.Kits;

public class KitsListConstructor {
	
	public static String newStringList(Player p, Map<String, Long> kits) {
		Collection<Kit> allkits = Kits.kits.values();
		if(allkits.isEmpty()) return null;
		
		String separator = Config.getString("list-separator").replace("&", "\u00A7");
		String output = "";
		
		for(Kit kit : allkits) {
			String text = kit.getDisplayname();
			
			State state = State.AVAILABLE;
			if(kits.containsKey(kit.getId()))
				if(kit.isSingle() && !p.hasPermission("kits.use.single")) state = State.SINGLE;
				else if(!kit.isSingle()) {
					long remained = Utils.getCooldown(kits.get(kit.getId()), kit.getDelay());
					if(remained != 0) state = State.COOLDOWNED;
				}
			if(state.equals(State.AVAILABLE) && kit.isPermreq() && !p.hasPermission(kit.getPermission()))
				state = State.UNAVAILABLE;
			
			ListEntry entry = Config.entries.get(state.toString());
			text = entry.applyStyle(text);
			if(!output.equals("")) output += separator;
			output += text;
		}
		return output;
	}

	public static TextComponent newHoverList(Player p, Map<String, Long> kits) {
		Set<String> allkits = Kits.kits.keySet();
		if(allkits.isEmpty()) return null;
		
		TextComponent separator = new TextComponent(Config.getString("list-separator").replace("&", "\u00A7"));
		List<TextComponent> components = new ArrayList<>();
		
		for(String k : allkits) {
			Kit kit = Kits.kits.get(k);
			TextComponent text = new TextComponent(kit.getDisplayname());
			
			State state = State.AVAILABLE;
			long remained = 0;
			if(kits.containsKey(k))
				if(kit.isSingle() && !p.hasPermission("kits.use.single")) state = State.SINGLE;
				else if(!kit.isSingle()) {
					remained = Utils.getCooldown(kits.get(k), kit.getDelay());
					if(remained != 0) state = State.COOLDOWNED;
				}
			if(state.equals(State.AVAILABLE) && kit.isPermreq() && !p.hasPermission(kit.getPermission()))
				state = State.UNAVAILABLE;
			
			ListEntry entry = Config.entries.get(state.toString());
			String cooldown = remained == 0 ? "" : DateFormatter.getFormatedTime(remained);
			text = entry.applyStyle(text, kit, cooldown);
			if(!components.isEmpty()) components.add(separator);
			components.add(text);
		}
		
		BaseComponent[] output = new BaseComponent[components.size()];
		for(int i = 0; i < components.size(); i++)
			output[i] = components.get(i);
		return new TextComponent(output);
	}
	
}
