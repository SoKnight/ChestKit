package ru.soknight.chestkit.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import ru.soknight.chestkit.ChestKit;

public class DatabaseManager {
	
	private final Logger logger;
	private final ConnectionSource source;
	
    private final Dao<ReceiverProfile, String> dao;
    
    public DatabaseManager(ChestKit plugin, Database database) throws SQLException {
		this.logger = plugin.getLogger();
		this.source = database.getConnection();
		
		this.dao = DaoManager.createDao(source, ReceiverProfile.class);
    }
    
    public void shutdown() {
		try {
			source.close();
			logger.info("Database connection closed.");
		} catch (IOException e) {
			logger.severe("Failed to close database connection: " + e.getLocalizedMessage());
		}
	}
    
    /*
     * Player info
     */
    
    public boolean createProfile(ReceiverProfile profile) {
    	try {
			return this.dao.create(profile) != 0;
		} catch (SQLException e) {
			logger.severe("Failed to create profile for player '" + profile.getReceiver() + "': " + e.getMessage());
			return false;
		}
    }
	
	public ReceiverProfile getProfile(String player) {
		try {
			return this.dao.queryForId(player);
		} catch (SQLException e) {
			logger.severe("Failed to get profile of player '" + player + "': " + e.getMessage());
			return null;
		}
	}
	
	public boolean hasProfile(String player) {
		return getProfile(player) != null;
	}
	
	public boolean updateProfile(ReceiverProfile profile) {
		try {
			return this.dao.update(profile) != 0;
		} catch (SQLException e) {
			logger.severe("Failed to update profile of player '" + profile.getReceiver() + "': " + e.getMessage());
			return false;
		}
	}
	
}
