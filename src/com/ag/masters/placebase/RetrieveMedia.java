package com.ag.masters.placebase;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.widget.Toast;

import com.ag.masters.placebase.model.DatabaseHelper;
import com.ag.masters.placebase.sqlite.Encounter;
import com.ag.masters.placebase.sqlite.Story;
import com.ag.masters.placebase.sqlite.User;
import com.ag.masters.placebase.view.CommentFragment;
import com.ag.masters.placebase.view.MediaFragment;

public class RetrieveMedia extends FragmentActivity implements
		ActionBar.TabListener {

	DatabaseHelper dbh;
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	
	// store references to the Fragments in the 
	// main class when they are created
	private MediaFragment mMediaFragment;
	private CommentFragment mCommentFragment;
	
	// passed in from MapActivity
	private Story story;
	private User user;
	
	private Encounter encounter;
	
	// determine which viewStub to sub
	private int mediaType = -1;
	
	/**
	 * ON CREATE
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_retrieve_media);
		
		dbh = new DatabaseHelper(this);
		
		// TODO: unpack user and story parcel
		Bundle data = getIntent().getExtras();

		if (data != null) {
			// get the story object
			Story tempStory = data.getParcelable("story");
			if (tempStory != null) {
				story = tempStory;
			}else {
				throw new RuntimeException("RetrieveMedia: story passed was null");
			}

			// get the user object
			User tempUser = data.getParcelable("user");
			if (tempUser != null) {
				user = tempUser;
			}else {
				throw new RuntimeException("RetrieveMedia: user passed was null");
			}
		}
		
		// ENCOUNTERS
		//create a new encounter object
		dbh.openDataBase();
		//dbh.getAllEncounters();
		encounter = new Encounter();
		// read encounters from database, and create a new encounter object
		
		// use this number to set the text for the view  
		dbh.close();
		
		// set the media type
		// TODO: load in the correct view stub, based on this value		
		mediaType = story.getMedia();
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the two
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					/** 
					 * when the view is swiped....
					 */
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		
		initMediaWidgets(mSectionsPagerAdapter.getItem(0));
		initCommentWidgets(mSectionsPagerAdapter.getItem(1));
		
	}
	
	private void initMediaWidgets(Fragment fragment) {
		// set onClickListeners for senseButtons
		// store them as global 
		
		
	}
	
	private void initCommentWidgets(Fragment fragment) {
		
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_retrieve_image, menu);
		return true;
	}

	/** 
	 * when A tab is selected....
	 */
	@Override
	public void onTabSelected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
		switchToSelected(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction) {
	}

	
	/**
	 * called whenever the fragment is swipped or tabbed to
	 * TODO: save encounter object for selected sense buttons when the user pages away
	 * 
	 * @param adapter position
	 */
	private void switchToSelected(int pos) {
		if (pos == 0) { 
			switchToMedia();
		} else if (pos == 1){ 
			switchToComments();
		}
	}
	
	private void switchToMedia() {
		Toast.makeText(this, "MEDIA TAB", Toast.LENGTH_SHORT).show();
	}
	
	private void switchToComments() {
		Toast.makeText(this, "COMMENTS TAB", Toast.LENGTH_SHORT).show();
	}
	
	
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// CALLED ONLY ONCE. DO THE SETUP HERE!!!
			// Return the Media or Comments fragment
			// pass mediaType as arg to set ViewStub for the particular media 
			// TODO: where do we program the controls for video and audio views?
			Fragment fragment = null;
			
			if (position == 0) {
				fragment = new MediaFragment();
				
				mMediaFragment = (MediaFragment) fragment;
				
				// pass an arg to MediaFragment so we can load the right stub
				Bundle args = new Bundle();
				// to determine the viewStub to inflate
				args.putInt("mediaType", mediaType);
				// to populate the fields
				args.putParcelable("story", story);
				args.putParcelable("user", user);
				args.putParcelable("encounter", encounter);
				fragment.setArguments(args);
				
				//Log.v(getClass().getSimpleName(), "Fragment 0 called in getItem");
				
			} else if (position == 1) {
				fragment = new CommentFragment();
				
				mCommentFragment = (CommentFragment) fragment;
				//Log.v(getClass().getSimpleName(), "Fragment 1 called in getItem");
			
			}

			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.story).toUpperCase();
			case 1:
				return getString(R.string.comments).toUpperCase();
			}
			return null;
		}
	}


}