package edu.mit.media.inm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import edu.mit.media.inm.fragments.CollectionFragment;
import edu.mit.media.inm.fragments.NoteFragment;
import edu.mit.media.inm.fragments.PlantFragment;
import edu.mit.media.inm.fragments.PlanterFragment;
import edu.mit.media.inm.fragments.PotFragment;
import edu.mit.media.inm.fragments.PrefsFragment;
import edu.mit.media.inm.handlers.CollectionDataSource;
import edu.mit.media.inm.handlers.NoteDataSource;
import edu.mit.media.inm.handlers.PlantDataSource;
import edu.mit.media.inm.handlers.PreferenceHandler;
import edu.mit.media.inm.handlers.UserDataSource;
import edu.mit.media.inm.types.Collection;
import edu.mit.media.inm.types.User;
import edu.mit.media.inm.util.LoginUtil;
import edu.mit.media.inm.util.ScheduleClient;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener {
	private static String TAG = "MainActivity";
	private static int SPINNER_COUNT = 5;
	public ActionBar actionBar;
	private FragmentManager fm;
	private PreferenceHandler ph;
	private ScheduleClient scheduleClient;
	public LoginUtil login_util;
	
    private ArrayList<String> navSpinner;
    private List<Collection> collections;
    private MainNavigationAdapter adapter;
	
	private EasyTracker tracker;
	private long start_time;
	
	public String user_id;
	private String prompt;
	
	// Datasources for all activities to use
	public PlantDataSource plant_ds;
	public NoteDataSource note_ds;
	public UserDataSource user_ds;
	public CollectionDataSource collection_ds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Open all datasources
		plant_ds = new PlantDataSource(this);
		note_ds = new NoteDataSource(this);
		user_ds = new UserDataSource(this);
		collection_ds = new CollectionDataSource(this);
		plant_ds.open();
		note_ds.open();
		user_ds.open();
		collection_ds.open();
		
		FragmentManager.enableDebugLogging(true);
		fm = getFragmentManager();
		if (savedInstanceState == null) {
			fm.beginTransaction()
			.add(android.R.id.content, new PlanterFragment(), "planter")
			.commit();
		}
		
		
		ph = new PreferenceHandler(this);
		this.user_id = ph.server_id();
		scheduleClient = new ScheduleClient(this);
		scheduleClient.doBindService();

		Calendar cal = Calendar.getInstance();
		Long minute = Long.valueOf(60 * cal.get(Calendar.HOUR_OF_DAY)
				+ cal.get(Calendar.MINUTE));
		setUpNavigation(false);        
		tracker = EasyTracker.getInstance(this);
		tracker.send(MapBuilder
			      .createEvent(
			    		  ph.server_id(),
			    		  "access_main",
			              String.valueOf(minute),
			              minute)
			      .build());
		start_time = System.currentTimeMillis();
        login_util = new LoginUtil(this);
		login_util.pingServer();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.main, menu);
		// This is checking for log in status!
		if (!ph.IV().equals(PreferenceHandler.default_IV)){
			menu.removeItem(R.id.action_login);
		} else {
			menu.removeItem(R.id.action_new);
			menu.removeItem(R.id.action_settings);
			menu.removeItem(R.id.action_logout);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			newThing();
	        return true;
		case R.id.action_discard:
			Collection to_delete = this.collections.get(
					this.actionBar.getSelectedNavigationIndex() - 5);
			this.collection_ds.deleteCollection(to_delete);
			setUpNavigation(false);
	        return true;
		case R.id.action_refresh:
			login_util.pingServer();
			return true;
		case R.id.action_logout:
			login_util.clearAllDb();
			this.turnOnActionBarNav(false);
			refresh();
			return true;
		case R.id.action_login:
			login_util.loginDialog();
			return true;
		case R.id.action_about:
			String info = "Email joyc@mit.edu if you have any questions or bugs to report!";
			Toast.makeText(this, info, Toast.LENGTH_LONG).show();
			return true;
		case R.id.action_settings:
			fm.beginTransaction()
					.replace(android.R.id.content, new PrefsFragment())
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.addToBackStack("prefs").commit();
			this.turnOnActionBarNav(false);
			return true;
		case android.R.id.home:
			if (fm.getBackStackEntryCount() > 0) {
				confirmDialog();
			}
		}
		return false;
	}
	
	private void newThing(){
		fm.beginTransaction()
		.replace(android.R.id.content, new PotFragment(), "pot")
		.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
		.addToBackStack("pot").commit();
	}
	
	public void refresh(){
		Log.d(TAG, "Main Refresh");
		this.user_id = ph.server_id();

	    if (ph.prompt() && !ph.password().equals("None")){
			scheduleClient.checkAlarms();
		}
		
		int to_refresh = this.actionBar.getSelectedNavigationIndex();
		if (to_refresh > -1 && this.actionBar.getNavigationItemCount() > to_refresh){
			onNavigationItemSelected(to_refresh, to_refresh);
		}
		PlanterFragment planter_frag = (PlanterFragment) getFragmentManager()
				.findFragmentByTag("planter");
		if (planter_frag !=null){
			planter_frag.refresh();
		}
		refreshPlant();
		invalidateOptionsMenu();
	}
	
	public void refreshPlant(){
		PlantFragment plant_frag = (PlantFragment) getFragmentManager()
				.findFragmentByTag("plant");
		if (plant_frag != null){
			plant_frag.refresh();
		}
	}
	
	public void turnOnActionBarNav(boolean turnOn){
		if (!ph.IV().equals(PreferenceHandler.default_IV)){
			if (turnOn){
				actionBar.setDisplayHomeAsUpEnabled(false);
				actionBar.setHomeButtonEnabled(false);
		        actionBar.setDisplayShowTitleEnabled(false);
		        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			} else {
		        actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			}
		} else {
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionBar.setTitle("InMind");
		}
	}

	public void setUpNavigation(boolean collection_created){
        // Spinner title navigation
		actionBar = getActionBar();
		actionBar.setDisplayUseLogoEnabled(true);
        navSpinner = new ArrayList<String>();
        navSpinner.add("All Topics");
        navSpinner.add("Mine");   
        navSpinner.add("Shared with me");   
        navSpinner.add("Archived");   
        navSpinner.add("+ New Collection");   
        
        collections = this.collection_ds.getAllCollections();
        for (Collection c : collections){
        	navSpinner.add("> " + c.name);
        }

        adapter = new MainNavigationAdapter(this, navSpinner);
        actionBar.setListNavigationCallbacks(adapter, this);
        
        if (collection_created){
        	actionBar.setSelectedNavigationItem(collections.size()+SPINNER_COUNT-1);
        }
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		Log.d(TAG, "Spinner Item :" + itemPosition + " " + itemId);
		Set<String> users = new HashSet<String>();
		PlanterFragment planter_frag = (PlanterFragment) getFragmentManager()
				.findFragmentByTag("planter");

		if (planter_frag != null) {
			switch (itemPosition) {
			case 0:		// All
				planter_frag.refresh(false);
				return true;
			case 1:		// Mine
				users.add(ph.server_id());
				planter_frag.refresh(users);
				return true;
			case 2:		// Not mine
				for (User u : user_ds.getAllUsers()){
					if (!u.server_id.equals(ph.server_id())){
						users.add(u.server_id);
					}
				}
				planter_frag.refresh(users);
				return true;
			case 3:		// Archived
				planter_frag.refresh(true);
				return true;
			case 4:		// New Collection
				fm.beginTransaction()
				.replace(android.R.id.content, new CollectionFragment(), "collection")
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.addToBackStack("collection").commit();
				this.actionBar.setSelectedNavigationItem(0);
				return true;
			default:	// Collections
				Collection selected = this.collections.get(itemPosition-SPINNER_COUNT);
				planter_frag.refresh(selected);
				return true;
			}
		}
		return false;
	}
	
	public void promptDialog(boolean from_notification){
		if (!ph.prompt()){
	    	new AlertDialog.Builder(this)
	    	.setTitle("Welcome back!")
		    .setMessage("What would you like to do today?")
		    .setNegativeButton("Check on topics.", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	// Don't do anything
		        }
		    }).setPositiveButton("Start a new topic.", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            newThing();
		        }
		    }).show();
	    	return;
		}
		if (prompt == null || from_notification){
			AssetManager assetManager = getAssets();
		    InputStream ims;
			Random random = new Random();
			int line = random.nextInt(2);
			String filename = "quotes.txt";
			int lines = 63;
			if (line > 0){
				filename = "prompts.txt";
				lines = 98;
			}
			try {
				ims = assetManager.open(filename);
				line = random.nextInt(lines);
				BufferedReader br = new BufferedReader(new InputStreamReader(ims));
				for(int i = 0; i < line; ++i)
				  br.readLine();
				prompt = br.readLine();
			} catch (IOException e) {
				Toast.makeText(this, "Oops, failed to read file", Toast.LENGTH_SHORT).show();
			}
		}
    	new AlertDialog.Builder(this)
    	.setTitle("Consider...")
	    .setMessage(prompt)
	    .setNegativeButton("Ok.", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	// Don't do anything
	        }
	    }).setPositiveButton("That gave me an idea for a topic.", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            newThing();
	        }
	    }).show();
	}

	private void confirmDialog(){
		NoteFragment note = (NoteFragment) fm.findFragmentByTag("note");
		PotFragment pot = (PotFragment) fm.findFragmentByTag("pot");
		CollectionFragment collection = (CollectionFragment) fm.findFragmentByTag("collection");
		
		if ((note!= null && note.inProgress()) || 
			(pot!=null && pot.inProgress()) || 
			(collection!=null && collection.inProgress())){
			new AlertDialog.Builder(this)
		    .setTitle(R.string.confirm)
		    .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	goBack();
		        }
		    }).setPositiveButton("No", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            // Don't do anything
		        }
		    }).show();
		} else {
			goBack();
		}
	}
	
	public void goBack(){
		if (fm.getBackStackEntryCount() == 1){
			this.turnOnActionBarNav(true);
			refresh();
		}
		fm.popBackStack();
	}

	@Override
	public void onBackPressed() {
		// check to see if stack is empty
		if (fm.getBackStackEntryCount() > 0) {
			confirmDialog();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	  public void onStart() {
	    super.onStart();
	    tracker.activityStart(this);
	  }

	@Override
	public void onResume(){
		super.onResume();
		plant_ds.open();
		note_ds.open();
		user_ds.open();
		collection_ds.open();

		Intent intent = getIntent();
        Bundle extras = intent.getExtras();

    	Log.d(TAG, "OnResume");
        if (extras != null) {
        	Log.d(TAG, "EXTRA");
            if (extras.getBoolean("Prompt", false)){
            	intent.removeExtra("Prompt");
            	promptDialog(true);
            }
        }
	}
	
	@Override
	public void onPause(){
		plant_ds.close();
		note_ds.close();
		user_ds.close();
		collection_ds.close();
		super.onPause();
	}
	
	@Override
	  public void onStop() {
	    super.onStop();
	    tracker.activityStop(this);  // Add this method.
	    tracker.send(MapBuilder
	    	      .createTiming("engagement",
	                      System.currentTimeMillis()-this.start_time, 
	                      "main",
	                      ph.server_id())
	        .build()
	    );
    	if(scheduleClient != null)
    		scheduleClient.doUnbindService();
	  }
}
