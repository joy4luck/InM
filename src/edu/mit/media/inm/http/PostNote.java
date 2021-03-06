package edu.mit.media.inm.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.util.Log;
import edu.mit.media.inm.MainActivity;
import edu.mit.media.inm.R;
import edu.mit.media.inm.handlers.NoteDataSource;
import edu.mit.media.inm.handlers.UserDataSource;
import edu.mit.media.inm.types.Note;

public class PostNote extends PostThread{
	private String text;
	private String plant_id;

	public PostNote(int id, MainActivity ctx) {
		super(id, ctx);

		String server = ctx.getResources().getString(R.string.url_server);
		String notes = ctx.getResources().getString(R.string.uri_messages);
		this.uri = server + "/" + notes;
		
		Log.d(TAG, "URI to ping: " + this.uri);
	}

	List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
	
	public void setupParams(String text, String plant_id) {
		this.text = text;
		this.plant_id = plant_id;
		
		params.add(new BasicNameValuePair("text", text));
		params.add(new BasicNameValuePair("plant", plant_id));
	}

	@Override
	protected void onPostExecute(String result) {
		try{
			Log.d(TAG, result);
			DateTimeFormatter joda_ISO_parser = ISODateTimeFormat
					.dateTimeParser();
			JSONParser js = new JSONParser();
			JSONObject plant_data = (JSONObject) js.parse(result);
			String iso_string = (String) plant_data.get("created_at");
			long created_at = joda_ISO_parser.parseDateTime(iso_string)
					.getMillis();
			String server_id = (String) plant_data.get("server_id");

			// Save the note locally
			Note n = ctx.note_ds.createNote(
					ctx.user_ds.getUserAlias(ph.server_id()),
					created_at, 
					this.text,
					this.plant_id,
					server_id);
			
			Log.d(TAG, "Published " + n);                                        

			ctx.refresh();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}