package ru.soknight.chestkit.utils;

import ru.soknight.chestkit.ChestKit;

public class Logger {
	
	private static final ChestKit instance = ChestKit.getInstance();
	
    public static void info(String info) {
        instance.getLogger().info(info);
        return;
    }
    
    public static void warn(String warn) {
        instance.getLogger().warning(warn);
        return;
    }
    
    public static void error(String error) {
        instance.getLogger().severe(error);
        return;
    }
    
}
