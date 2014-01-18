package edu.mit.media.inm.plant;

import java.util.List;

import edu.mit.media.inm.R;
import edu.mit.media.inm.data.PlantDataSource;
import edu.mit.media.inm.prefs.PreferenceHandler;
import edu.mit.media.inm.prefs.PrefsFragment;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlanterFragment extends Fragment {
	private static final String TAG = "TellFragment";

	private Activity ctx;
	private String username;
	private PlantDataSource datasource;
	private HorizontalScrollView planter;
	private LinearLayout my_plants;

	private Button new_plant_btn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "OnCreateView");
		
		ctx = this.getActivity();
		PreferenceHandler ph = new PreferenceHandler(ctx);
		username = ph.username();

		View rootView = inflater.inflate(R.layout.fragment_planter, container,
				false);

		datasource = new PlantDataSource(ctx);
		datasource.open();

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		new_plant_btn = (Button) getView().findViewById(R.id.new_plant);
		new_plant_btn.setOnClickListener(new View.OnClickListener() {
			// Switch to pot fragment
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PotFragment())
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack("pot").commit();
		        ctx.getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		});

		planter = (HorizontalScrollView) getView().findViewById(R.id.planter);
		my_plants = (LinearLayout) getView().findViewById(R.id.my_plants);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		datasource.open();

		List<Plant> values = datasource.getAllStories();
		
		if (values.size() == 0){
			// If there are no plants to display, show a message instead.
			planter.setVisibility(View.INVISIBLE);
		} else if (my_plants.getChildAt(0)!=null){
			// If there are child elements, remove them so we can refresh.
			my_plants.removeAllViews();
		}
		for (Plant p : values){
			// Set up the plant container
			LinearLayout plant = new LinearLayout(ctx);
			plant.setOrientation(LinearLayout.VERTICAL);
			plant.setTag(p);
			plant.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Plant clicked_plant = (Plant) v.getTag();
	                ctx.getFragmentManager().beginTransaction()
					.replace(android.R.id.content, PlantFragment.newInstance(clicked_plant))
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.addToBackStack("plant").commit();

			        ctx.getActionBar().setDisplayHomeAsUpEnabled(true);
				}
		    });
			my_plants.addView(plant);

			// Choose a plant image
			ImageView image = new ImageView(ctx);
			image.setImageResource(R.drawable.demo_plant);
			plant.addView(image);
			
			// Label the plant with its topic
			TextView text = new TextView(ctx);
			text.setPadding(10, 10, 10, 10);
			text.setLayoutParams(
					new LayoutParams(300,
							LayoutParams.MATCH_PARENT));
			text.setText(p.title);
			text.setGravity(Gravity.CENTER_HORIZONTAL);
			plant.addView(text);
		}
	}

	@Override
	public void onPause() {
		datasource.close();
		super.onPause();
	}
}