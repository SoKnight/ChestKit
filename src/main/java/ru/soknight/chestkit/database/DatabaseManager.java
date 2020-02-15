package ru.soknight.chestkit.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import ru.soknight.chestkit.ChestKit;
import ru.soknight.chestkit.utils.Logger;

public class DatabaseManager {
	
    private static Map<String, PlayerInfo> data = new HashMap<>();
	
    public static void loadFromDatabase() {
    	Database db = ChestKit.getInstance().getDatabase();
		String query = "SELECT * FROM playerdata;";
		 
		try {
			Connection connection = db.getConnection();
			Statement statement = connection.createStatement();
			
			ResultSet output = statement.executeQuery(query);
			Logger.info("Loading data from database...");
			long start = System.currentTimeMillis();
			while(output.next()) {
				String name = output.getString("player");
				String kit = output.getString("kit");
				long date = output.getLong("date");
				PlayerInfo unit = getData(name);
				unit.setKitDate(kit, date);
				setData(name, unit);
			}
			long current = System.currentTimeMillis();
			Logger.info("Loaded " + data.size() + " entries. Time took: " + (current - start) + " ms.");
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
	public static void saveToDatabase() {
		if(data.isEmpty()) return;
		 
		Database db = ChestKit.getInstance().getDatabase();
		String query = "INSERT INTO playerdata (player, kit, date) VALUES (?, ?, ?);";
		
		try {
			Connection connection = db.getConnection();
			Statement statement = connection.createStatement();
			
			statement.executeUpdate("DELETE FROM playerdata;");
			statement.close();
			
			PreparedStatement stm = connection.prepareStatement(query);
			
			for(String name : data.keySet()) {
				PlayerInfo u = data.get(name);
				Map<String, Long> kits = u.getKits();
				if(kits.isEmpty()) continue;
				
				for(String kit : kits.keySet()) {
					stm.setString(1, name);
					stm.setString(2, kit);
					stm.setLong(3, kits.get(kit));
					stm.execute();
					stm.clearParameters();
				}
			}
			
			Logger.info(data.size() + " entries saved to database.");
			data.clear();
			stm.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	 
	/**
	 * Checking data exist in database
	 * @param name - name of player
	 * @return Exist data of player in database or not
	 */
	public static boolean isExist(String name) {
		return data.containsKey(name);
	}
	
	/**
	 * Getting exist or new data of target player
	 * @param name - name of player
	 * @return Exist or new data of player
	 */
	public static PlayerInfo getData(String name) {
		return data.containsKey(name) ? data.get(name) : new PlayerInfo(name);
	}
	
	/**
	 * Refresh data of player
	 * @param name - name of player
	 * @param data - data of player
	 */
	public static void setData(String name, PlayerInfo data) {
		DatabaseManager.data.put(name, data);
	}
	
}
