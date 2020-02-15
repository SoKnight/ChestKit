package ru.soknight.chestkit;

import java.text.DecimalFormat;

import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import ru.soknight.chestkit.database.DatabaseManager;
import ru.soknight.chestkit.database.PlayerInfo;
import ru.soknight.chestkit.files.Config.ListEntry.State;
import ru.soknight.chestkit.files.Kit;
import ru.soknight.chestkit.files.Kits;
import ru.soknight.chestkit.utils.Utils;

@AllArgsConstructor
public class ChestKitExpansion extends PlaceholderExpansion {

	private static final DecimalFormat df = new DecimalFormat("#0.00");
	
	private ChestKit plugin;
	
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
        	
        	if(!Kits.kits.containsKey(kitid)) return "";
            
            Kit kit = Kits.kits.get(kitid);
            PlayerInfo info = DatabaseManager.getData(name);
            
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
        		return String.valueOf(info.getKits().containsKey(kitid));
        	}
        	case "state": {
        		State state = State.AVAILABLE;
    			if(info.getKits().containsKey(kit.getId()))
    				if(kit.isSingle() && !p.hasPermission("kits.use.single")) state = State.SINGLE;
    				else if(!kit.isSingle()) {
    					long remained = Utils.getCooldown(info.getKits().get(kit.getId()), kit.getDelay());
    					if(remained != 0) state = State.COOLDOWNED;
    				}
    			if(state.equals(State.AVAILABLE) && kit.isPermreq() && !p.hasPermission(kit.getPermission()))
    				state = State.UNAVAILABLE;
        		return state.toString().toLowerCase();
        	}
        	case "permission": {
        		return kit.getPermission();
        	}
        	case "displayname": {
        		return kit.getDisplayname();
        	}
        	case "cooldown": {
        		return String.valueOf(Utils.getCooldown(info.getKits().get(kitid), kit.getDelay()));
        	}
        	case "delay": {
        		return String.valueOf(kit.getDelay());
        	}
        	case "dollars": {
        		return df.format(kit.getDollars());
        	}
        	case "euro": {
        		return df.format(kit.getEuro());
        	}
        	default:
        		break;
        	}
        }
        
        return null;
    }

}
