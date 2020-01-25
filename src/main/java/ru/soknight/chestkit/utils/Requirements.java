package ru.soknight.chestkit.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.soknight.chestkit.files.Config;

public class Requirements {

	public static boolean hasPermission(CommandSender sender, String permission) {
		if(!sender.hasPermission(permission)) {
			sender.sendMessage(Config.getMessage("error-no-permissions"));
			return false;
		} else return true;
	}
	
	public static boolean isPlayerRequired(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(Config.getMessage("error-only-for-players"));
			return false;
		} else return true;
	}
	
	public static boolean isPlayerOnline(OfflinePlayer op, CommandSender sender) {
		if(!op.isOnline()) {
			sender.sendMessage(Config.getMessage("error-player-not-found")
					.replace("%player%", op.getName()));
			return false;
		} else return true;
	}
	
}
