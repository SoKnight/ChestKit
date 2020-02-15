package ru.soknight.chestkit.files;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lombok.Data;

@Data
public class Kit {

	private final String id;
	private String displayname, permission, title;
	private boolean single, permreq, openable, moneyless;
	private long delay;
	private int rows;
	private float dollars = 0, euro = 0;
	private Inventory content;
	private ItemStack item;
	
	public Kit(String id) {
		this.id = id;
	}
	
}
