package edu.mit.media.inm.http;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.widget.Toast;

import edu.mit.media.inm.MainActivity;
import edu.mit.media.inm.R;
import edu.mit.media.inm.handlers.PreferenceHandler;

public class GetIV extends GetThread {
	protected static final String TAG = "GetIV HTTP";
	
	private PreferenceHandler ph;

	public GetIV(int id, MainActivity ctx) {
		super(id, ctx);
		String server = ctx.getResources().getString(R.string.url_server);
		String users = ctx.getResources().getString(R.string.uri_users);
		String IV = ctx.getResources().getString(R.string.uri_IV);
		this.uri = server + "/" + users + "/" + IV;

		ph = new PreferenceHandler(ctx);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result){
			ctx.turnOnActionBarNav(true);
			ctx.invalidateOptionsMenu();
			Toast.makeText(ctx, "Login successful. Getting your data...", Toast.LENGTH_LONG).show();
			GetUsers user_thread = new GetUsers(this.id +1, ctx);
			user_thread.execute();
		} else {
			Toast.makeText(ctx, "Login Failed! Try again.", Toast.LENGTH_LONG).show();
	    	ph.setUsername("");
			ctx.refresh();
		}
	}

	@Override
	protected void handleResults(String result) {
		JSONParser js = new JSONParser();
		try {
			JSONObject iv = (JSONObject) js.parse(result);
			ph.set_IV((String)iv.get("IV"));
			ph.set_server_id((String)iv.get("server_id"));
			ph.set_POTD((String) iv.get("POTD_neut"),
					(String) iv.get("POTD_happy"),
					(String) iv.get("POTD_sad"));
			String iso_date = (String) iv.get("pinged_at");	
			if (iso_date != null){
				DateTimeFormatter joda_ISO_parser = ISODateTimeFormat
						.dateTimeParser();
				ph.set_now(joda_ISO_parser.parseDateTime(iso_date)
						.getMillis());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
