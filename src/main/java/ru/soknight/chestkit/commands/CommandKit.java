package ru.soknight.chestkit.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.soknight.chestkit.commands.validation.KitExecutionData;
import ru.soknight.chestkit.commands.validation.KitValidator;
import ru.soknight.chestkit.configuration.Config;
import ru.soknight.chestkit.configuration.KitInstance;
import ru.soknight.chestkit.configuration.KitsManager;
import ru.soknight.chestkit.database.DatabaseManager;
import ru.soknight.chestkit.database.ReceiverProfile;
import ru.soknight.chestkit.date.DateFormatter;
import ru.soknight.lib.command.ExtendedCommandExecutor;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.CommandExecutionData;
import ru.soknight.lib.validation.validator.ArgsCountValidator;
import ru.soknight.lib.validation.validator.PermissionValidator;
import ru.soknight.lib.validation.validator.Validator;

public class CommandKit extends ExtendedCommandExecutor {

	@Getter private static Map<ItemStack, ReceivedKit> cache = new HashMap<>();
	
	private final DatabaseManager databaseManager;
	private final KitsManager kitsManager;
	
	private final Messages messages;
	private final Config config;
	
	public CommandKit(DatabaseManager databaseManager, KitsManager kitsManager, Config config, Messages messages) {
		super(messages);
		
		this.databaseManager = databaseManager;
		this.kitsManager = kitsManager;
		
		this.messages = messages;
		this.config = config;
		
		String permmsg = messages.get("error.no-permissions");
		String argsmsg = messages.get("kit.failed.not-specified");
		String kitmsg = messages.get("kit.failed.unknown");
		
		Validator permval = new PermissionValidator("kits.use", permmsg);
		Validator argsval = new ArgsCountValidator(1, argsmsg);
		Validator kitval = new KitValidator(kitsManager, kitmsg);
		
		super.addValidators(permval, argsval, kitval);
	}
	
	@Override
	public void executeCommand(CommandSender sender, String[] args) {
		String kitid = null;
		if(args.length != 0) kitid = args[0];
		
		CommandExecutionData data = new KitExecutionData(sender, args, kitid);
		if(!validateExecution(data)) return;
		
		Player p;
		boolean other = false;
		
		if(args.length > 1) {
			if(!sender.hasPermission("kits.use.other")) {
				messages.getAndSend(sender, "error.no-permissions");
				return;
			}
			
			OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);
			if(offline == null || offline != null && !offline.isOnline()) {
				messages.sendFormatted(sender, "error.player-not-online", "%player%", args[1]);
				return;
			}
			
			p = offline.getPlayer();
			other = true;
		} else
			if(!(sender instanceof Player)) {
				messages.getAndSend(sender, "error.only-for-players");
				return;
			} else p = (Player) sender;
		
		String name = p.getName();
		
		// Checks for full inventory
		Inventory inventory = p.getInventory();
		if(inventory.firstEmpty() == -1) {
			if(other)
				messages.sendFormatted(sender, "kit.failed.full-inventory.other", "%player%", name);
			else messages.getAndSend(sender, "kit.failed.full-inventory.self");
			return;
		}
		
		KitInstance kit = kitsManager.getKits().get(kitid);
		
		if(!other) {
			// Checks for kit permission
			if(kit.isPermreq() && !p.hasPermission(kit.getPermission())) {
				messages.getAndSend(p, "kit.failed.permission");
				return;
			}
			
			boolean has = databaseManager.hasProfile(name);
			ReceiverProfile profile = has ? databaseManager.getProfile(name) : new ReceiverProfile(name);
					
			// Checking for cooldown
			if(!p.hasPermission("kits.use.bypass") && profile.getKits().containsKey(kitid)) {
				if(kit.isSingle() && !p.hasPermission("kits.use.single")) {
					messages.getAndSend(p, "kit.failed.single");
					return;
				} else {
					long remained = getCooldown(profile.getKits().get(kitid), kit.getDelay());
					if(remained != 0) {
						String time = DateFormatter.getFormatedTime(config, remained);
						messages.sendFormatted(p, "kit.failed.cooldown", "%time%", time);
						return;
					}
				}
			}
			
			profile.setKitDate(kitid, System.currentTimeMillis());
			if(has)
				databaseManager.updateProfile(profile);
			else databaseManager.createProfile(profile);
		}
		
		ReceivedKit info = new ReceivedKit(p, kit);
		
		ItemStack item = kit.getItem().clone();
		ItemMeta meta = item.getItemMeta();
		
		List<String> lore = new ArrayList<>();
		meta.getLore().forEach(s -> lore.add(s.replace("%receiver%", name)));
		meta.setLore(lore);
		
		item.setItemMeta(meta);
		inventory.addItem(item);
		
		cache.put(item, info);
		
		if(other)
			messages.sendFormatted(sender, "kit.received.other",
					"%player%", name,
					"%kit%", kit.getDisplayname());
		
		messages.sendFormatted(sender, "kit.received.self", "%kit%", kit.getDisplayname());
	}
	
	@Override
	public List<String> executeTabCompletion(CommandSender sender, String[] args) {
		if(!validateTabCompletion(sender, args)) return null;
		
		List<String> output = new ArrayList<>();
		
		if(args.length == 1) {
			Set<String> kits = kitsManager.getKits().keySet();
			String arg = args[0].toLowerCase();
			kits.parallelStream()
					.filter(k -> k.toLowerCase().startsWith(arg))
					.forEach(k -> output.add(k));
		} else if(args.length == 2 && sender.hasPermission("kits.use.other")) {
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			String arg = args[1].toLowerCase();
			players.parallelStream()
					.filter(p -> p.getName().toLowerCase().startsWith(arg))
					.forEach(p -> output.add(p.getName()));
		}
		
		return output;
	}
	
	@Getter
	@AllArgsConstructor
	public static class ReceivedKit {
		private Player receiver;
		private KitInstance kit;
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
