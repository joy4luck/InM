package edu.mit.media.inm.note;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import edu.mit.media.inm.R;
import edu.mit.media.inm.data.NoteDataSource;
import edu.mit.media.inm.http.PostNote;
import edu.mit.media.inm.plant.Plant;
import edu.mit.media.inm.prefs.PreferenceHandler;
import edu.mit.media.inm.util.AesUtil;

public class NoteFragment extends Fragment {
	private static final String TAG = "NoteFragment";

	private Activity ctx;
	private View rootView;
	private NoteDataSource datasource;
	private Plant plant;
	private TextView note_text;
	private PreferenceHandler ph;
	
	private InputMethodManager imm;
	
	public static NoteFragment newInstance(Plant p) {
        NoteFragment f = new NoteFragment();
        Bundle args = new Bundle();
        args.putParcelable("plant", p);
        f.setArguments(args);

        return f;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		this.plant = (Plant) (getArguments() != null ? getArguments().get("plant") : 1);
		
		this.ctx = getActivity();

        imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ctx = this.getActivity();
		ph = new PreferenceHandler(ctx);

		rootView = inflater.inflate(R.layout.mini_fragment_note, container,
				false);
		
		note_text = (TextView) rootView.findViewById(R.id.note_text);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
		
		datasource = new NoteDataSource(ctx);
		datasource.open();
		
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.compose, menu);
	    super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_done:
			String encryptedText = encryptNote();
			
			PostNote http_client = new PostNote(0, ctx);
    		http_client.setupParams(encryptedText, plant.server_id);
            Toast.makeText(getActivity(), "Publishing to server...", Toast.LENGTH_LONG)
                            .show();
            http_client.execute();
            
            imm.hideSoftInputFromWindow(note_text.getWindowToken(), 0);
			
			getFragmentManager().popBackStack();

			return true;
		}
		return false;
	}
	
	private String encryptNote(){
		String IV = ph.IV();
		String pass = plant.passphrase;
		String salt = plant.salt;
		String plain_text = note_text.getText().toString();

		Log.d(TAG, "IV "+ IV.length());
		
		AesUtil util = new AesUtil();
        String encrypt = util.encrypt(salt, IV, pass, plain_text);
        return encrypt;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		ctx.getActionBar().setTitle(this.plant.title);
		datasource.open();
	}
	
	@Override
	public void onPause() {
		datasource.close();
		super.onPause();
	}
}