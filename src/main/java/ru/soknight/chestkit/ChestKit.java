package ru.soknight.chestkit;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import ru.soknight.chestkit.commands.CommandChestkit;
import ru.soknight.chestkit.commands.CommandKit;
import ru.soknight.chestkit.commands.CommandKits;
import ru.soknight.chestkit.database.Database;
import ru.soknight.chestkit.database.DatabaseManager;
import ru.soknight.chestkit.files.Config;
import ru.soknight.chestkit.files.Kits;
import ru.soknight.chestkit.listeners.ItemUseListener;
import ru.soknight.chestkit.utils.Logger;

public class ChestKit extends JavaPlugin {
	
	@Getter
	private static ChestKit instance;
	@Getter
	private Database database;

    @Override
	public void onEnable() {
    	instance = this;
    	
    	// Refreshing configs
    	Config.refresh();
    	Kits.refresh();
        
    	// Database initialization
    	try {
			database = new Database();
			DatabaseManager.loadFromDatabase();
		} catch (Exception e) {
			Logger.error("Connection to database failed. See stacktrace below.");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
    	
    	// Commands registration
    	PluginCommand kit = getCommand("kit"), kits = getCommand("kits"), chestkit = getCommand("chestkit");
    	kit.setExecutor(new CommandKit());		kit.setTabCompleter(new CommandKit());
    	kits.setExecutor(new CommandKits());	chestkit.setExecutor(new CommandChestkit());
    	
    	// Listener registration
    	getServer().getPluginManager().registerEvents(new ItemUseListener(), this);
    	
    	// Try hook into PAPI
    	if(Config.getBoolean("hook-papi")) hookIntoPapi();
    	
        Logger.info("Enabled!");
    }
    
    private void hookIntoPapi() {
		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            boolean hooked = new ChestKitExpansion(this).register();
            if(hooked) Logger.info("Hooked into PlaceholdersAPI.");
            else Logger.warn("Hooking to PlaceholdersAPI failed, may be expansion already registered.");
		} else Logger.info("PlaceholdersAPI not found, hooking cancelled.");
	}
    
    @Override
	public void onDisable() {
    	DatabaseManager.saveToDatabase();
    	Logger.info("Disabled!");
    }
	
}
