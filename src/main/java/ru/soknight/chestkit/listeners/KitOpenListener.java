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
import ru.soknight.chestkit.commands.CommandKit.ReceivedKit;
import ru.soknight.chestkit.configuration.Config;
import ru.soknight.chestkit.configuration.KitInstance;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEcoAPI;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.command.tool.AmountFormatter;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.Wallet;

@AllArgsConstructor
public class KitOpenListener implements Listener {

	private static Map<InventoryView, String> views = new HashMap<>();
	
	private final ChestKit plugin;
	private final Messages messages;
	
	private final int delay;
	
	public KitOpenListener(ChestKit plugin, Config config, Messages messages) {
		this.plugin = plugin;
		this.messages = messages;
		
		this.delay = config.getInt("time-before-viewing");
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		Action action = e.getAction();
		if(!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;
		
		if(!e.getHand().equals(EquipmentSlot.HAND)) return;
		
		ItemStack item = e.getItem();
		if(item == null) return;
		
		if(!CommandKit.getCache().containsKey(item)) return;
		
		Player p = e.getPlayer();
		ReceivedKit info = CommandKit.getCache().get(item);
		CommandKit.getCache().remove(item);
		
		KitInstance kit = info.getKit();
		
		messages.sendFormatted(p, "kit.opened", "%kit%", kit.getDisplayname());
		
		PlayerInventory pinv = p.getInventory();
		if(pinv.contains(item)) pinv.remove(item);
		
		if(kit.isOpenable()) {
			Inventory inventory = kit.getContent();
			Inventory newinv = Bukkit.createInventory(p, kit.getRows() * 9, kit.getTitle());
			newinv.setContents(inventory.getContents().clone());
		
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				if(p != null) {
					InventoryView view = p.openInventory(newinv);
					views.put(view, kit.getDisplayname());
					
					if(!kit.isMoneyless())
						sendMoney(kit, p);
				}
			}, delay);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				if(!kit.isMoneyless())
					sendMoney(kit, p);
				messages.sendFormatted(p, "kit.closed.empty", "%kit%", kit.getDisplayname());
			}, delay);
		}
		
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
		
		if(inventoryIsEmpty(inventory))
			messages.sendFormatted(p, "kit.closed.empty", "%kit%", name);
		else {
			Location location = p.getLocation();
			World world = location.getWorld();
			
			for(ItemStack i : content)
				if(i != null) world.dropItem(location, i);
			
			messages.sendFormatted(p, "kit.closed.with-items", "%kit%", name);
		}
	}
	
	private void sendMoney(KitInstance kit, Player p) {
		String name = p.getName();
		
		if(Bukkit.getPluginManager().getPlugin("PEconomy") == null) {
			plugin.getLogger().severe("Failed to give money to " + name + ": PEconomy plugin not found.");
			return;
		}
		
		PEcoAPI api = PEconomy.getAPI();
		
		final Wallet wallet = api.hasWallet(name) ? api.getWallet(name) : new Wallet(name);
		
		kit.getRewards().forEach((currencyid, amount) -> {
			CurrencyInstance currency = api.getCurrencyByID(currencyid);
			if(currency == null) return;
			
			wallet.addAmount(currencyid, amount);
			
			messages.sendFormatted(p, "kit.reward",
					"%amount%", AmountFormatter.format(amount),
					"%currency%", currency.getSymbol());
		});
		
		api.updateWallet(wallet);
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
