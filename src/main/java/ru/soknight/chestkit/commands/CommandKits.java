package ru.soknight.chestkit.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import ru.soknight.chestkit.configuration.Config;
import ru.soknight.chestkit.configuration.KitsListConstructor;
import ru.soknight.chestkit.configuration.KitsManager;
import ru.soknight.chestkit.database.DatabaseManager;
import ru.soknight.chestkit.database.ReceiverProfile;
import ru.soknight.lib.command.ExtendedCommandExecutor;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.validator.PermissionValidator;
import ru.soknight.lib.validation.validator.Validator;

public class CommandKits extends ExtendedCommandExecutor {
	
	private final KitsListConstructor listConstructor;
	private final DatabaseManager databaseManager;
	
	private final Messages messages;
	
	public CommandKits(DatabaseManager databaseManager, KitsManager kitsManager, Config config, Messages messages) {
		super(messages);
		
		this.listConstructor = new KitsListConstructor(config, kitsManager);
		this.databaseManager = databaseManager;
		
		this.messages = messages;
		
		String permmsg = messages.get("error.no-permissions");
		
		Validator permval = new PermissionValidator("kits.list", permmsg);
		
		super.addValidators(permval);
	}

	@Override
	public void executeCommand(CommandSender sender, String[] args) {
		if(!validateExecution(sender, args)) return;
		
		if(args.length != 0) {
			if(!sender.hasPermission("kits.list.other")) {
				messages.getAndSend(sender, "error.no-permissions");
				return;
			}
			
			String name = args[0];
			OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
			
			if(offline == null || !offline.isOnline()) {
				messages.sendFormatted(sender, "error.player-not-online", "%player%", name);
				return;
			}
			
			Player p = offline.getPlayer();
			
			ReceiverProfile profile = databaseManager.getProfile(name);
			if(profile == null) profile = new ReceiverProfile(name);
			
			Map<String, Long> kits = profile.getKits();
			
			String kitsList = listConstructor.newStringList(p, kits);
			if(kitsList == null || kitsList.isEmpty()) {
				messages.getAndSend(sender, "kits.empty");
				return;
			}
			
			messages.sendFormatted(sender, "kits.list.other", "%player%", name, "%kits%", kitsList);
		} else {
			if(!(sender instanceof Player)) {
				messages.getAndSend(sender, "error.only-for-players");
				return;
			}
			
			Player p = (Player) sender;
			String name = p.getName();
			
			ReceiverProfile profile = databaseManager.getProfile(name);
			if(profile == null) profile = new ReceiverProfile(name);
			
			Map<String, Long> kits = profile.getKits();
			
			TextComponent kitsList = listConstructor.newHoverList(p, kits);
			if(kitsList == null) {
				messages.getAndSend(sender, "kits.empty");
				return;
			}
			
			BaseComponent[] message = getMessageWithHovers(kitsList);
			p.spigot().sendMessage(message);
		}
	}
	
	private BaseComponent[] getMessageWithHovers(TextComponent kitsList) {
		String format = messages.get("kits.list.self");
		
		String beforeStr = "", afterStr = "";
		if(format.startsWith("%kits%")) afterStr = format.substring(6);
		else if(format.endsWith("%kits%")) beforeStr = format.substring(0, format.length() - 6);
		else {
			String[] parts = format.split("%kits%");
			beforeStr = parts[0];
			afterStr = parts.length > 0 ? parts[1] : "";
		}
		
		TextComponent beforeText = new TextComponent(beforeStr);
		TextComponent afterText = new TextComponent(afterStr);
		
		BaseComponent[] output = new BaseComponent[] { beforeText, kitsList, afterText };
		return output;
	}
	
	@Override
	public List<String> executeTabCompletion(CommandSender sender, String[] args) {
		if(args.length != 1) return null;
		if(!sender.hasPermission("kits.list.other")) return null;
		
		List<String> output = new ArrayList<>();
		
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		String arg = args[0].toLowerCase();
		players.parallelStream()
				.filter(p -> p.getName().toLowerCase().startsWith(arg))
				.forEach(p -> output.add(p.getName()));
		
		return output;
	}
	
}
