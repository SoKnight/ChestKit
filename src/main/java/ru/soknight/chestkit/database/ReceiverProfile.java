package ru.soknight.chestkit.database;

import java.util.HashMap;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "playerdata")
public class ReceiverProfile {

	@DatabaseField(id = true)
	private String receiver;
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private HashMap<String, Long> kits;
	
	public ReceiverProfile(String receiver) {
		this.receiver = receiver;
		this.kits = new HashMap<>();
	}
	
	public void setKitDate(String kit, long date) {
		kits.put(kit, date);
	}
	
}
