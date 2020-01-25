package ru.soknight.chestkit.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import ru.soknight.chestkit.ChestKit;
import ru.soknight.chestkit.files.Config;
import ru.soknight.chestkit.utils.Logger;

public class Database {

	static String database_type;
	static String database_url;
	static String mysql_host;
	static String mysql_name;
	static String mysql_user;
	static String mysql_password;
	static String sqlite_file;
	static int mysql_port;
	
	public Database() throws Exception {
		database_type = Config.getString("database.type");
		switch(database_type) {
		case "mysql":
			mysql_host = Config.getString("database.host");
			mysql_name = Config.getString("database.name");
			mysql_user = Config.getString("database.user");
			mysql_password = Config.getString("database.password");
			mysql_port = Config.getInt("database.port");
			database_url = "jdbc:mysql://" + mysql_host + ":" + mysql_port + "/" + mysql_name;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			break;
		case "sqlite":
			sqlite_file = Config.getString("database.file");
			database_url = "jdbc:sqlite:" + ChestKit.getInstance().getDataFolder() + File.separator + sqlite_file;
			Class.forName("org.sqlite.JDBC").newInstance();
			break;
		default:
			Logger.error("Invalid type '" + database_type + "' of database in config.yml! Please, fix it.");
			break;
		}
		
		Connection connection = getConnection();
		Statement s = connection.createStatement();
		
		s.executeUpdate("CREATE TABLE IF NOT EXISTS playerdata (player TEXT, kit TEXT, date LONG);");
		
		s.close();
		connection.close();
		Logger.info("Database type " + database_type + " connected!");
	}
	
	public Connection getConnection() throws SQLException {
		if(database_type.equals("mysql"))
			return DriverManager.getConnection(database_url, mysql_user, mysql_password);
		else return DriverManager.getConnection(database_url);
	}
	
}
