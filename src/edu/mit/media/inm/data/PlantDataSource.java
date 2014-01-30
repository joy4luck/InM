package edu.mit.media.inm.data;

import java.util.ArrayList;
import java.util.List;

import edu.mit.media.inm.plant.Plant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PlantDataSource {

	private static String TAG = "PlantDataSource";
	
	// Database fields
	private SQLiteDatabase database;
	private PlantSQLite dbHelper;
	private String[] allColumns = { PlantSQLite.COLUMN_ID,
			PlantSQLite.COLUMN_AUTHOR,
			PlantSQLite.COLUMN_ARCHIVED, 
			PlantSQLite.COLUMN_DATE,
			PlantSQLite.COLUMN_PASSPHRASE, 
			PlantSQLite.COLUMN_POT, PlantSQLite.COLUMN_SALT,
			PlantSQLite.COLUMN_SERVER_ID, PlantSQLite.COLUMN_SHARED_WITH,
			PlantSQLite.COLUMN_STATUS,PlantSQLite.COLUMN_TITLE };

	public PlantDataSource(Context context) {
		dbHelper = new PlantSQLite(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Plant createPlant(String author, boolean archived,
			long date, String pass, int pot_color, String salt, String server_id,
			String share, int status, String title) {
		ContentValues values = new ContentValues();
		values.put(PlantSQLite.COLUMN_AUTHOR, author);
<<<<<<< HEAD
		values.put(PlantSQLite.COLUMN_ARCHIVED, archived ? 1:0);
=======
		values.put(PlantSQLite.COLUMN_ARCHIVED, 0);
>>>>>>> a74c796b84cec2884039ccf15712fbca830ab243
		values.put(PlantSQLite.COLUMN_DATE, date);
		values.put(PlantSQLite.COLUMN_PASSPHRASE, pass);
		values.put(PlantSQLite.COLUMN_POT, pot_color);
		values.put(PlantSQLite.COLUMN_SALT, salt);
		values.put(PlantSQLite.COLUMN_SERVER_ID, server_id);
		values.put(PlantSQLite.COLUMN_SHARED_WITH, share);
		values.put(PlantSQLite.COLUMN_STATUS, status);
		values.put(PlantSQLite.COLUMN_TITLE, title);
		long insertId = database.insert(PlantSQLite.TABLE_PLANT, null, values);

		// Get the entered plant back out as a Plant object
		Cursor cursor = database.query(PlantSQLite.TABLE_PLANT, allColumns,
				PlantSQLite.COLUMN_ID + " = " + insertId, null, null, null,
				null);
		cursor.moveToFirst();
		Plant newPlant = cursorToPlant(cursor);
		cursor.close();
		return newPlant;
	}

	public void deletePlant(Plant plant) {
		long id = plant.id;
		Log.i(TAG, "Plant deleted with id: " + id);
		database.delete(PlantSQLite.TABLE_PLANT, PlantSQLite.COLUMN_ID
				+ " = " + id, null);
	}
	
	public Plant getPlant(long id) {
		Log.i(TAG, "Trying to find plant with id: " + id);
		Cursor cursor = database.query(PlantSQLite.TABLE_PLANT, allColumns,
				PlantSQLite.COLUMN_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		Plant plant = cursorToPlant(cursor);
		cursor.close();
		return plant;
	}

	public List<Plant> getAllPlants() {
		List<Plant> Plants = new ArrayList<Plant>();

		Cursor cursor = database.query(PlantSQLite.TABLE_PLANT, allColumns,
				null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Plant Plant = cursorToPlant(cursor);
			Plants.add(Plant);
			cursor.moveToNext();
		}
		cursor.close();
		return Plants;
	}
	
	public List<Plant> getUserStories(String username) {
		List<Plant> Plants = new ArrayList<Plant>();

		Cursor cursor = database.query(PlantSQLite.TABLE_PLANT, allColumns,
				PlantSQLite.COLUMN_AUTHOR + " = '" + username + "'", null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Plant Plant = cursorToPlant(cursor);
			Plants.add(Plant);
			cursor.moveToNext();
		}
		cursor.close();
		return Plants;
	}

	public void updatePlant(String server_id, int status, boolean archived){
		ContentValues values = new ContentValues();
		values.put(PlantSQLite.COLUMN_STATUS, status);
		values.put(PlantSQLite.COLUMN_ARCHIVED, archived ? 1 : 0);

		database.update(PlantSQLite.TABLE_PLANT,
				values, PlantSQLite.COLUMN_SERVER_ID + " = ?",
				new String[]{server_id,});
	}
	
	private Plant cursorToPlant(Cursor cursor) {
		Plant Plant = new Plant();
		Plant.id = cursor.getLong(0);
		Plant.author = cursor.getString(1);
		Plant.archived = cursor.getLong(2) == 1;
		Plant.date = cursor.getLong(3);
		Plant.passphrase = cursor.getString(4);
		Plant.pot = cursor.getInt(5);
		Plant.salt = cursor.getString(6);
		Plant.server_id = cursor.getString(7);
		Plant.shared_with = cursor.getString(8);
		Plant.status = cursor.getInt(9);
		Plant.title = cursor.getString(10);
		return Plant;
	}
}
