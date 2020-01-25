package ru.soknight.chestkit.files;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lombok.Data;

@Data
public class Kit {

	private final String id;
	private String displayname, permission, title;
	private boolean single, permreq;
	private long delay;
	private int rows;
	private Inventory content;
	private ItemStack item;
	
	public Kit(String id) {
		this.id = id;
	}
	
}
