package ru.soknight.chestkit.hook;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import ru.soknight.chestkit.ChestKit;
import ru.soknight.chestkit.configuration.KitInstance;
import ru.soknight.chestkit.configuration.KitsListEntry.KitsListEntryState;
import ru.soknight.chestkit.configuration.KitsManager;
import ru.soknight.chestkit.database.DatabaseManager;
import ru.soknight.chestkit.database.ReceiverProfile;

@AllArgsConstructor
public class ChestKitExpansion extends PlaceholderExpansion {
	
	private final ChestKit plugin;
	private final KitsManager kitsManager;
	private final DatabaseManager databaseManager;
	
	@Override
	public String getAuthor() {
		return "SoKnight";
	}

	@Override
	public String getIdentifier() {
		return "chestkit";
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}
	
	@Override
    public String onPlaceholderRequest(Player p, String id){
        if(p == null) return "";

        String[] parts = id.split("_");
        String name = p.getName();
        
        if(parts.length > 1) {
        	String kitid = parts[0];
        	String subid = parts[1];
        	
        	KitInstance kit = kitsManager.getKits().get(kitid);
        	if(kit == null)
        		return ChatColor.RED + "UNKNOWN KIT";
            
            ReceiverProfile profile = databaseManager.getProfile(name);
            
        	switch (subid) {
        	case "moneyless": {
        		return String.valueOf(kit.isMoneyless());
        	}
        	case "single": {
        		return String.valueOf(kit.isSingle());
        	}
        	case "openable": {
        		return String.valueOf(kit.isOpenable());
        	}
        	case "permreq": {
        		return String.valueOf(kit.isPermreq());
        	}
        	case "received": {
        		return String.valueOf(profile.getKits().containsKey(kitid));
        	}
        	case "state": {
        		KitsListEntryState state = KitsListEntryState.AVAILABLE;
    			if(profile.getKits().containsKey(kit.getId()))
    				if(kit.isSingle() && !p.hasPermission("kits.use.single"))
    					state = KitsListEntryState.SINGLE;
    				else if(!kit.isSingle()) {
    					long remained = getCooldown(profile.getKits().get(kit.getId()), kit.getDelay());
    					if(remained != 0) state = KitsListEntryState.COOLDOWNED;
    				}
    			if(state.equals(KitsListEntryState.AVAILABLE) && kit.isPermreq() && !p.hasPermission(kit.getPermission()))
    				state = KitsListEntryState.UNAVAILABLE;
    			
        		return state.toString().toLowerCase();
        	}
        	case "permission": {
        		return kit.getPermission();
        	}
        	case "displayname": {
        		return kit.getDisplayname();
        	}
        	case "cooldown": {
        		return String.valueOf(getCooldown(profile.getKits().get(kitid), kit.getDelay()));
        	}
        	case "delay": {
        		return String.valueOf(kit.getDelay());
        	}
        	default:
        		break;
        	}
        }
        
        return null;
    }
	
	private long getCooldown(long kit, long delay) {
		if((Long) delay == null) delay = 0;
		long current = System.currentTimeMillis() / 60000;
		
		long passed = current - kit / 60000;
		long remained = delay - passed;
		if(remained < 0) remained = 0;
		return remained;
	}

}
