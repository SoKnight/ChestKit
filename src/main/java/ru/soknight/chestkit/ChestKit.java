package ru.soknight.chestkit;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import ru.soknight.chestkit.commands.CommandChestkit;
import ru.soknight.chestkit.commands.CommandKit;
import ru.soknight.chestkit.commands.CommandKits;
import ru.soknight.chestkit.configuration.Config;
import ru.soknight.chestkit.configuration.KitsManager;
import ru.soknight.chestkit.database.Database;
import ru.soknight.chestkit.database.DatabaseManager;
import ru.soknight.chestkit.hook.ChestKitExpansion;
import ru.soknight.chestkit.listeners.KitOpenListener;
import ru.soknight.lib.configuration.Messages;

public class ChestKit extends JavaPlugin {

	protected DatabaseManager databaseManager;
	protected KitsManager kitsManager;
	
	protected Config pluginConfig;
	protected Messages messages;
	
    @Override
	public void onEnable() {
    	long start = System.currentTimeMillis();
    	
    	// Configs initialization
    	refreshConfigs();
        
    	// Database initialization
    	try {
			Database database = new Database(this, pluginConfig);
			this.databaseManager = new DatabaseManager(this, database);
		} catch (Exception e) {
			getLogger().severe("Failed to initialize database: " + e.getLocalizedMessage());
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
    	
		// Commands executors initialization
		registerCommands();
    	
    	// Kit open listener initialization
		registerListener();
    	
    	// Trying to hook into PAPI and Vault
    	hookInto();
    			
    	long time = System.currentTimeMillis() - start;
    	getLogger().info("Bootstrapped in " + time + " ms.");
    }
    
    @Override
	public void onDisable() {
		if(databaseManager != null) databaseManager.shutdown();
	}
    
    public void refreshConfigs() {
		this.pluginConfig = new Config(this);
		
		this.messages = new Messages(this, "messages.yml");
		this.messages.refresh();
		
		this.kitsManager = new KitsManager(this, pluginConfig);
	}
	
	public void registerCommands() {
		CommandChestkit commandChestkit = new CommandChestkit(this, messages);
		CommandKit commandKit = new CommandKit(databaseManager, kitsManager, pluginConfig, messages);
		CommandKits commandKits = new CommandKits(databaseManager, kitsManager, pluginConfig, messages);
		
		PluginCommand chestkit = getCommand("chestkit");
		PluginCommand kit = getCommand("kit");
		PluginCommand kits = getCommand("kits");
		
		chestkit.setExecutor(commandChestkit);
		chestkit.setTabCompleter(commandChestkit);
		
		kit.setExecutor(commandKit);
		kit.setTabCompleter(commandKit);
		
		kits.setExecutor(commandKits);
		kits.setTabCompleter(commandKits);
	}
	
	public void registerListener() {
		KitOpenListener openListener = new KitOpenListener(this, pluginConfig, messages);
    	getServer().getPluginManager().registerEvents(openListener, this);
	}
	
	private void hookInto() {
		if(pluginConfig.getBoolean("hook-into-papi")) {
			if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
				ChestKitExpansion expansion = new ChestKitExpansion(this, kitsManager, databaseManager);
				
				if(expansion.register())
					getLogger().info("Hooked into PlaceholdersAPI successfully.");
				else getLogger().warning("Hooking into PlaceholdersAPI failed.");
				
			} else getLogger().info("Couldn't find PlaceholdersAPI to hook into, ignoring it.");
		}
	}
	
}
