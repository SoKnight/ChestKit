package ru.soknight.chestkit.commands;

import org.bukkit.command.CommandSender;

import ru.soknight.chestkit.ChestKit;
import ru.soknight.lib.command.ExtendedCommandExecutor;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.validator.PermissionValidator;
import ru.soknight.lib.validation.validator.Validator;

public class CommandChestkit extends ExtendedCommandExecutor {

	private final ChestKit plugin;
	private final Messages messages;
	
	public CommandChestkit(ChestKit plugin, Messages messages) {
		super(messages);
		
		this.plugin = plugin;
		this.messages = messages;
		
		String permmsg = messages.get("error.no-permissions");
		
		Validator permval = new PermissionValidator("kits.reload", permmsg);
		
		super.addValidators(permval);
	}
	
	@Override
	public void executeCommand(CommandSender sender, String[] args) {
		if(!validateExecution(sender, args)) return;
		
		plugin.refreshConfigs();
		plugin.registerCommands();
		plugin.registerListener();
		
		messages.getAndSend(sender, "reloaded");
	}
	
}
