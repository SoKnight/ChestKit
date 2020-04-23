package ru.soknight.chestkit.configuration;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lombok.Data;

@Data
public class KitInstance {

	private final String id;
	private String displayname, permission, title;
	private boolean single, permreq, openable, moneyless;
	private long delay;
	private int rows;
	private Map<String, Float> rewards;
	private Inventory content;
	private ItemStack item;
	
	public KitInstance(String id) {
		this.id = id;
		this.rewards = new HashMap<>();
	}
	
	public void addReward(String currency, float amount) {
		this.rewards.put(currency, amount);
	}
	
	public float getReward(String currency) {
		return this.rewards.containsKey(currency) ? this.rewards.get(currency) : 0f;
	}
	
}
