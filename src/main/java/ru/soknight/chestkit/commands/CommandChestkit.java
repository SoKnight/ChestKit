package ru.soknight.chestkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.soknight.chestkit.files.Config;
import ru.soknight.chestkit.files.Kits;
import ru.soknight.chestkit.utils.Requirements;

public class CommandChestkit implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!Requirements.hasPermission(sender, "kits.reload")) return true;
		
		Config.refresh();
		Kits.refresh();
		
		sender.sendMessage(Config.getMessage("reloaded-success"));
		return true;
	}
	
}
