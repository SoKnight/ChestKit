package ru.soknight.chestkit.commands;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import ru.soknight.chestkit.database.DatabaseManager;
import ru.soknight.chestkit.database.PlayerInfo;
import ru.soknight.chestkit.files.Config;
import ru.soknight.chestkit.utils.KitsListConstructor;
import ru.soknight.chestkit.utils.Requirements;

public class CommandKits implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!Requirements.hasPermission(sender, "kits.list")) return true;
		
		if(args.length != 0) {
			if(!Requirements.hasPermission(sender, "kits.list.other")) return true;
			String name = args[0];
			OfflinePlayer op = Bukkit.getOfflinePlayer(name);
			
			if(!Requirements.isPlayerOnline(op, sender)) return true;
			Player p = op.getPlayer();
			
			PlayerInfo unit = DatabaseManager.getData(name);
			Map<String, Long> kits = unit.getKits();
			
			String kitslist = KitsListConstructor.newStringList(p, kits);
			if(kitslist == null) kitslist = "—";
			
			String message = Config.getMessage("kits-list-other").replace("%player%", name);
			sender.sendMessage(message.replace("%kits%", kitslist));
		} else {
			if(!Requirements.isPlayerRequired(sender)) return true;
			Player p = (Player) sender;
			String name = p.getName();
			
			PlayerInfo unit = DatabaseManager.getData(name);
			Map<String, Long> kits = unit.getKits();
			
			TextComponent kitslist = KitsListConstructor.newHoverList(p, kits);
			if(kitslist == null) {
				p.sendMessage(Config.getMessage("kits-empty"));
				return true; }
			
			BaseComponent[] message = getMessageWithHovers(kitslist);
			p.sendMessage(message);
		}
		
		return true;
	}
	
	private BaseComponent[] getMessageWithHovers(TextComponent kitsList) {
		String format = Config.getMessage("kits-list");
		
		String beforeStr = "", afterStr = "";
		if(format.startsWith("%kits%")) afterStr = format.substring(6);
		else if(format.endsWith("%kits%")) beforeStr = format.substring(0, format.length()-6);
		else {
			String[] parts = format.split("%kits%");
			beforeStr = parts[0];
			afterStr = parts[1];
		}
		
		TextComponent beforeText = new TextComponent(beforeStr);
		TextComponent afterText = new TextComponent(afterStr);
		
		BaseComponent[] output = new BaseComponent[] {beforeText, kitsList, afterText};
		return output;
	}
	
}
