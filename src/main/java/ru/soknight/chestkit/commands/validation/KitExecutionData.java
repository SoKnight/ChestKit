package ru.soknight.chestkit.commands.validation;

import org.bukkit.command.CommandSender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.soknight.lib.validation.CommandExecutionData;

@Getter
@AllArgsConstructor
public class KitExecutionData implements CommandExecutionData {

	private final CommandSender sender;
	private final String[] args;
	private final String kit;

}
