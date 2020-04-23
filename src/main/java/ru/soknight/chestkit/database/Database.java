package ru.soknight.chestkit.database;

import java.io.File;
import java.sql.SQLException;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import ru.soknight.chestkit.ChestKit;
import ru.soknight.chestkit.configuration.Config;

public class Database {
	
	private final String url;
	private final boolean useSQLite;
	
	private String user;
	private String password;
	
	public Database(ChestKit plugin, Config config) throws Exception {
		this.useSQLite = config.getBoolean("database.use-sqlite", true);
		if(!useSQLite) {
			String host = config.getString("database.host", "localhost");
			String name = config.getString("database.name", "peconomy");
			int port = config.getInt("database.port", 3306);
			this.user = config.getString("database.user", "admin");
			this.password = config.getString("database.password", "peconomy");
			this.url = "jdbc:mysql://" + host + ":" + port + "/" + name;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} else {
			String file = config.getString("database.file", "peconomy.db");
			this.url = "jdbc:sqlite:" + plugin.getDataFolder().getPath() + File.separator + file;
			Class.forName("org.sqlite.JDBC").newInstance();
		}
		
		// Allowing only ORMLite errors logging
		System.setProperty("com.j256.ormlite.logger.type", "LOCAL");
		System.setProperty("com.j256.ormlite.logger.level", "ERROR");
				
		ConnectionSource source = getConnection();

		TableUtils.createTableIfNotExists(source, ReceiverProfile.class);
		
		source.close();
		
		plugin.getLogger().info("Database type " + (useSQLite ? "SQLite" : "MySQL") + " connected!");
	}
	
	public ConnectionSource getConnection() throws SQLException {
		return useSQLite ? new JdbcConnectionSource(url) : new JdbcConnectionSource(url, user, password);
	}
	
}
