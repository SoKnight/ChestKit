package ru.soknight.chestkit.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.soknight.chestkit.database.DatabaseManager;
import ru.soknight.chestkit.database.PlayerInfo;
import ru.soknight.chestkit.files.Config;
import ru.soknight.chestkit.files.Kit;
import ru.soknight.chestkit.files.Kits;
import ru.soknight.chestkit.utils.DateFormatter;
import ru.soknight.chestkit.utils.Requirements;
import ru.soknight.chestkit.utils.Utils;

public class CommandKit implements CommandExecutor, TabCompleter {

	public static Map<ItemStack, KitInfo> items = new HashMap<>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!Requirements.hasPermission(sender, "kits.use")) return true;
		
		// Cheching for kit arg exist
		if(args.length == 0) {
			sender.sendMessage(Config.getMessage("kit-failed-not-specified"));
			return true; }
		
		// Cheching for valid kit
		String id = args[0];
		if(!Kits.kits.containsKey(id)) {
			sender.sendMessage(Config.getMessage("kit-failed-not-found").replace("%kit%", id));
			return true; }
		
		Player p;
		String name = "";
		boolean byadmin = false;
		
		if(args.length >= 2) {
			if(!Requirements.hasPermission(sender, "kits.use.other")) return true;
			name = args[1];
			OfflinePlayer op = Bukkit.getOfflinePlayer(name);
			if(!Requirements.isPlayerOnline(op, sender)) return true;
			p = op.getPlayer();
			byadmin = true;
		} else {
			if(!Requirements.isPlayerRequired(sender)) return true;
			p = (Player) sender;
			name = p.getName();
		}
		
		// Checking for kit permission
		Kit kit = Kits.kits.get(id);
		if(kit.isPermreq() && !p.hasPermission(kit.getPermission())) {
			p.sendMessage(Config.getMessage("kit-failed-permission"));
			return true; }
		
		// Checking for full inventory
		Inventory inventory = p.getInventory();
		if(inventory.firstEmpty() == -1) {
			if(!byadmin) sender.sendMessage(Config.getMessage("kit-failed-full-inventory"));
			else sender.sendMessage(Config.getMessage("kit-failed-full-inventory-other").replace("%player%", name));
			return true; }
		
		PlayerInfo unit = null;
		if(!byadmin) unit = DatabaseManager.getData(name);
		
		// Checking for cooldown
		if(!byadmin && !p.hasPermission("kits.use.bypass") && unit.getKits().containsKey(id)) {
			if(kit.isSingle()) {
				p.sendMessage(Config.getMessage("kit-failed-single"));
				return true;
			} else {
				long remained = Utils.getCooldown(unit.getKits().get(id), kit.getDelay());
				if(remained != 0) {
					String time = DateFormatter.getFormatedTime(remained);
					p.sendMessage(Config.getMessage("kit-failed-cooldown").replace("%time%", time));
					return true; }
			}
		}
		
		if(!byadmin) {
			unit.setKitDate(id, System.currentTimeMillis());
			DatabaseManager.setData(name, unit); }
		
		KitInfo info = new KitInfo(p, kit);
		ItemStack item = kit.getItem().clone();
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		for(String s : meta.getLore()) lore.add(s.replace("%receiver%", name));
		meta.setLore(lore);
		item.setItemMeta(meta);
		inventory.addItem(item);
		items.put(item, info);
		
		if(byadmin) sender.sendMessage(Config.getMessage("kit-received-other")
				.replace("%kit%", kit.getDisplayname()).replace("%player%", name));
		p.sendMessage(Config.getMessage("kit-received").replace("%kit%", kit.getDisplayname()));
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length == 1) {
			List<String> output = new ArrayList<>();
			String str = args[0].toLowerCase();
			
			for(String kit : Kits.kits.keySet())
				if(kit.toLowerCase().startsWith(str)) output.add(kit);
			
			return output;
		}
		return null;
	}
	
	@Getter
	@AllArgsConstructor
	public static class KitInfo {
		private Player player;
		private Kit kit;
	}
	
}
