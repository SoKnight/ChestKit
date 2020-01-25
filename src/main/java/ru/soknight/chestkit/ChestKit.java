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
    	Config.refresh();
    	Kits.refresh();
        
    	try {
			database = new Database();
		} catch (Exception e) {
			Logger.error("Connection to database failed. See stacktrace below.");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
    	DatabaseManager.loadFromDatabase();
    	
    	PluginCommand kit = getCommand("kit"), kits = getCommand("kits"), chestkit = getCommand("chestkit");
    	kit.setExecutor(new CommandKit());		kit.setTabCompleter(new CommandKit());
    	kits.setExecutor(new CommandKits());	chestkit.setExecutor(new CommandChestkit());
    	
    	getServer().getPluginManager().registerEvents(new ItemUseListener(), this);
    	
        Logger.info("Enabled!");
    }
    
    @Override
	public void onDisable() {
    	DatabaseManager.saveToDatabase();
    	Logger.info("Disabled!");
    }
	
}
