package ru.soknight.chestkit.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

public class Utils {
	
	public static List<String> getColoredList(ConfigurationSection config, String path) {
		List<String> list = config.getStringList(path);
		if(list.isEmpty()) return list;
		
		List<String> colored = new ArrayList<>();
		for(String s : list) colored.add(s.replace("&", "\u00A7"));
		return colored;
	}
	
	public static long getCooldown(long kit, long delay) {
		if((Long) delay == null) delay = 0;
		long current = System.currentTimeMillis() / 60000;
		
		long passed = current - (kit / 60000);
		long remained = delay - passed;
		if(remained < 0) remained = 0;
		return remained;
	}
	
}
