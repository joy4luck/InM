package edu.mit.media.inm.plant;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.mit.media.inm.MainActivity;
import edu.mit.media.inm.R;
import edu.mit.media.inm.data.NoteAdapter;
import edu.mit.media.inm.data.PlantDataSource;
import edu.mit.media.inm.data.NoteDataSource;
import edu.mit.media.inm.data.UserDataSource;
import edu.mit.media.inm.note.Note;
import edu.mit.media.inm.note.NoteFragment;
import edu.mit.media.inm.prefs.PreferenceHandler;
import edu.mit.media.inm.util.FileUtil;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PlantFragment extends Fragment {
	private static final String TAG = "PlantActivity";

	private Activity ctx;
	private View rootView;
	private PlantDataSource datasource;
	private Plant plant;
	private TextView show_info;
	private TextView info_text;
	
	private ListView notes_view;
	
	private CommandBoxFragment cmd_box_frag;
	
	public static PlantFragment newInstance(Plant p) {
        PlantFragment f = new PlantFragment();

        Bundle args = new Bundle();
        args.putParcelable("plant", p);
        f.setArguments(args);

        return f;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.plant = (Plant) (getArguments() != null ? getArguments().get("plant") : 1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ctx = this.getActivity();
		rootView = inflater.inflate(R.layout.fragment_plant, container,
				false);
		
		datasource = new PlantDataSource(ctx);
		datasource.open();

		setupInfoView();
		setupNotes();

		cmd_box_frag = CommandBoxFragment.newInstance(plant);
		getFragmentManager().beginTransaction()
			.replace(R.id.control_space, cmd_box_frag)
			.setTransition(0)
			.commit();
		
		return rootView;
	}

	private void setupInfoView(){
		// Toggle visibility of plant data
		OnClickListener listener = new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				info_text.setVisibility(info_text.isShown()
                        ? View.GONE
                        : View.VISIBLE );
				if (info_text.isShown()){
					show_info.setText(R.string.hide_info);
				} else{
					show_info.setText(R.string.show_info);
				}
			}
		};
		info_text = (TextView) rootView.findViewById(R.id.info_text);
		info_text.setOnClickListener(listener);
		show_info = (TextView) rootView.findViewById(R.id.show_info);
		show_info.setOnClickListener(listener);
		
		// Load plant data.
		StringBuilder info_string = new StringBuilder();

		// Pretty Print date
		info_string.append("Created at: ");
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		info_string.append(df.format(new Date(plant.date)));
		info_string.append("\n");

		// Pretty Print friends shared with
		info_string.append("Shared with: ");
		UserDataSource user_data = new UserDataSource(ctx);
		user_data.open();
		for (String s: plant.shared_with.split(",")){
			if (!s.trim().isEmpty()){
				info_string.append("\n\t");
				info_string.append(user_data.getUserAlias(s));	
			}		
		}

		info_text.setText(info_string.toString());
	}
	
	private void setupNotes(){
		notes_view = (ListView) rootView.findViewById(R.id.notes);
		NoteDataSource nds = new NoteDataSource(ctx);
		nds.open();
		List<Note> notes = nds.getPlantNotes(plant.server_id);
		nds.close();
		NoteAdapter note_adapter = new NoteAdapter(ctx, notes, plant);
		notes_view.setAdapter(note_adapter);
	}
	
	public void refresh(){
		setupNotes();
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
		cmd_box_frag.updatePlant();
		datasource.close();
		super.onPause();
	}
}