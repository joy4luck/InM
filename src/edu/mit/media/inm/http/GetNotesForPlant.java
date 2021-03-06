package edu.mit.media.inm.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.util.Log;
import android.widget.Toast;
import edu.mit.media.inm.MainActivity;
import edu.mit.media.inm.R;
import edu.mit.media.inm.types.Note;
import edu.mit.media.inm.types.Plant;

public class GetNotesForPlant extends GetThread {
	private static final String TAG = "GetNotes HTTP";

	public GetNotesForPlant(int id, MainActivity ctx, Plant p){
		super(id, ctx);

		String server = ctx.getResources().getString(R.string.url_server);
		String notes = ctx.getResources().getString(R.string.uri_messages);
		String check = ctx.getResources().getString(R.string.uri_check);

		// Collect the plant ids of plants loaded on this device.		
		StringBuilder query = new StringBuilder();
		try {
			query.append("pinged_at=");
			query.append(URLEncoder.encode(String.valueOf(ph.last_pinged()), charset));

			query.append("&");
			query.append("plants=");
			query.append(URLEncoder.encode(p.server_id, charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.uri = server + "/" + notes + "/" + check + "?" + query.toString();
		Log.d(TAG, "URI to ping: " + this.uri);
	}
	@Override
	protected void onPostExecute(Boolean result) {
		if (result){
			Log.d(TAG, "Refreshing.");
			ctx.refreshPlant();
		}
	}
	@Override
	protected void handleResults(String result) {
		HashSet<String> server_ids = new HashSet<String>();
		for (Note n : ctx.note_ds.getAllNotes()) {
			server_ids.add(n.server_id);
		}

		JSONParser js = new JSONParser();
		DateTimeFormatter joda_ISO_parser = ISODateTimeFormat
				.dateTimeParser();
		try {
			JSONArray notes_json = (JSONArray) js.parse(result);
			for (int i = 0; i < notes_json.size(); i++) {
				JSONObject note = (JSONObject) notes_json.get(i);
				long created_at = joda_ISO_parser.parseDateTime(
						(String) note.get("created_at"))
						.getMillis();
				
				String note_id = (String) note.get("server_id");
				if (!server_ids.contains(note_id)) {
					ctx.note_ds.createNote(
							ctx.user_ds.getUserAlias((String) note.get("user_id")),
							created_at,
							(String) note.get("text"),
							(String) note.get("plant_id"),
							note_id);
					if (ph.now() < created_at){
						ctx.plant_ds.setPlantShiny((String) note.get("plant_id"), true);
					}
				} else {
					server_ids.remove(note_id);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}