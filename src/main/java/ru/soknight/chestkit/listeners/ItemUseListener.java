package ru.soknight.chestkit.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.soknight.chestkit.ChestKit;
import ru.soknight.chestkit.commands.CommandKit;
import ru.soknight.chestkit.commands.CommandKit.KitInfo;
import ru.soknight.chestkit.files.Config;
import ru.soknight.chestkit.files.Kit;

public class ItemUseListener implements Listener {

	private static Map<InventoryView, String> views = new HashMap<>();
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		Action action = e.getAction();
		if(!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;
		
		ItemStack item = e.getItem();
		if(item == null) return;
		
		if(!e.getHand().equals(EquipmentSlot.HAND)) return;
		if(!CommandKit.items.containsKey(item)) return;
		
		Player p = e.getPlayer();
		KitInfo info = CommandKit.items.get(item);
		CommandKit.items.remove(item);
		
		Kit kit = info.getKit();
		Inventory inventory = kit.getContent();
		PlayerInventory pinv = p.getInventory();
		
		if(pinv.contains(item)) pinv.remove(item);
		
		Inventory newinv = Bukkit.createInventory(p, kit.getRows() * 9, kit.getTitle());
		newinv.setContents(inventory.getContents().clone());
		
		p.sendMessage(Config.getMessage("kit-opened").replace("%kit%", kit.getDisplayname()));
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(ChestKit.getInstance(), () -> {
			if(p != null) {
				InventoryView view = p.openInventory(newinv);
				views.put(view, kit.getDisplayname());
			}
		}, Config.getInt("time-before-viewing"));
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		InventoryView view = e.getView();
		if(!views.containsKey(view)) return;
		
		HumanEntity p = view.getPlayer();
		Inventory inventory = view.getTopInventory();
		ItemStack[] content = inventory.getContents();
		String name = views.get(view);
		views.remove(view);
		
		if(inventoryIsEmpty(inventory)) {
			p.sendMessage(Config.getMessage("kit-closed-empty").replace("%kit%", name));
		} else {
			Location location = p.getLocation();
			World world = location.getWorld();
			
			for(ItemStack i : content) if(i != null) world.dropItem(location, i);
			
			p.sendMessage(Config.getMessage("kit-closed-with-items").replace("%kit%", name));
		}
	}
	
	private boolean inventoryIsEmpty(Inventory i) {
		for(ItemStack item : i.getContents())
			if(item != null) return false;
		return true;
	}
	
	@Getter
	@AllArgsConstructor
	private class OpenedKit {
		private final String name;
		private final ItemStack item;
	}
	
}
