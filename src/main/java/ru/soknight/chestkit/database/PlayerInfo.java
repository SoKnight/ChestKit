package ru.soknight.chestkit.database;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class PlayerInfo {

	@Getter
	private final String name;
	@Getter
	private Map<String, Long> kits = new HashMap<>();
	
	public PlayerInfo(String name) {
		this.name = name;
	}
	
	public void setKitDate(String kit, long date) {
		kits.put(kit, date);
	}
	
}
