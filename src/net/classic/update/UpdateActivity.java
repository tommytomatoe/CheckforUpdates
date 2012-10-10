package net.classic.update;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class UpdateActivity extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_update, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_feedback:
	        	sendEmail();
	        case R.id.menu_settings:
        		break;
	    }
	    return super.onOptionsItemSelected(item);
	}    

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Fragment returns a particuar view depending on the position of tab
         * this is how you'd set this up
         */
        @Override
        public Fragment getItem(int i) {
        	Fragment fragment;

        	switch (i) {
        		case 0: fragment = new UpdateFragment();
        				break;
        		case 1: return FileBrowserFragment.newInstance(i);
        		case 2: fragment = new AboutFragment();
        				break;
        		case 3: fragment = new AboutFragment2();
        				break;
        		default: fragment = new UpdateFragment();
        	}
        	return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.title_section1).toUpperCase();
                case 1: return getString(R.string.title_section2).toUpperCase();
                case 2: return getString(R.string.title_section3).toUpperCase();
                case 3: return getString(R.string.title_section4).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A fragment for the update view.
     */
    public static class UpdateFragment extends Fragment {
        public UpdateFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	
        	View view = inflater.inflate(R.layout.update_frag, container, false);
        	
        	Button button = (Button)view.findViewById(R.id.update_check_btn);
            
        	button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	Activity activity = getActivity();
                	Intent i = new Intent(activity, Update.class);
    	    		startActivity(i);
                }
            });
            
            return view;
        }
    }
    
    /**
     * A fragment for file browser view
     */
    public static class FileBrowserFragment extends ListFragment {
        int mNum;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static FileBrowserFragment newInstance(int num) {
            FileBrowserFragment f = new FileBrowserFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.file_browser_list, container, false);
            View tv = v.findViewById(R.id.text);
            ((TextView)tv).setText("Example of how file browser might look");
            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setListAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, FILES));
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Log.i("FragmentList", "Item clicked: " + id);
        }
    }
    
    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     * 
     * Inflate from xml file
     */
    public static class AboutFragment extends Fragment {
        public AboutFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	
        	View view = inflater.inflate(R.layout.about_frag, container, false);
        	TextView textview = (TextView)view.findViewById(R.id.about_text);
        	textview.setText("About: Copyright 2012 Tommy Nguyen");
        	
        	return view;
        	
        }
    }
    
    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     * 
     * Example of how it is done in CODE:
     */
    public static class AboutFragment2 extends Fragment {
        public AboutFragment2() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	
        	TextView textview = new TextView(getActivity());
        	
        	textview.setGravity(Gravity.CENTER);
        	textview.setText("About: Copyright 2012 Tommy Nguyen");
        	
        	return textview;
        	
        }
    }
    
    public void sendEmail() {
    	/* Create the Intent */
    	final Intent email = new Intent(android.content.Intent.ACTION_SENDTO);

    	String content;

    	content = "mailto:tommytomatoe@gmail.com" + 
    	          "?subject=[Feedback] Rom Utility";
    	content = content.replace(" ", "%20");
    	Uri uri = Uri.parse(content);

    	email.setData(uri);

    	/* Send it off to the Activity-Chooser */
    	startActivityForResult(Intent.createChooser(email, "Compose mail"), 1);
    }
    
    public static final String[] FILES = 
    {
    		"File 1", "File 2", "File 3", "File 4", "File 5", "File 6"
    };
}
