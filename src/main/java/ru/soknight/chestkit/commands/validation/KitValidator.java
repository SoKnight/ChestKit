package ru.soknight.chestkit.commands.validation;

import lombok.AllArgsConstructor;
import ru.soknight.chestkit.configuration.KitsManager;
import ru.soknight.lib.validation.CommandExecutionData;
import ru.soknight.lib.validation.ValidationResult;
import ru.soknight.lib.validation.validator.Validator;

@AllArgsConstructor
public class KitValidator implements Validator {

	private final KitsManager kitsManager;
	private final String message;
	
	private final ValidationResult passed;
	private final ValidationResult skipped;
	
	public KitValidator(KitsManager kitsManager, String message) {
		this.kitsManager = kitsManager;
		this.message = message;
		
		this.passed = new ValidationResult(true);
		this.skipped = new ValidationResult(false);
	}
	
	@Override
	public ValidationResult validate(CommandExecutionData data) {
		if(!(data instanceof KitExecutionData))
			return skipped;
		
		KitExecutionData richdata = (KitExecutionData) data;
		String kit = richdata.getKit();
		
		ValidationResult failed = new ValidationResult(false, message.replace("%kit%", kit));
		if(kit == null || kit.equals("")) return failed;
		
		return kitsManager.getKits().containsKey(kit) ? passed : failed;
	}
	
}
