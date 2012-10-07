package net.classic.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Update extends FragmentActivity {
	Fragment mMenuDownload;
	Fragment mMenuDefault;
	Fragment mMenuDownloadNow;
	Fragment mMenuInstall;
	
	public Update activity = this;
	
	public static String TAG = "UpdateCheck";
	public String versionCurrent = "";
	public String versionNew = "";
	public String updateURL = "http://classictomatoe.net/androidfiles/version";
	
	public String romName = "Debug_v";
    public String dlURL = "http://classictomatoe.net/androidfiles/Debug_v";
    public String update = "update_version.txt";
    
    public String changelog = "changelog.txt";
    public String changeURL = "http://classictomatoe.net/androidfiles/changelog";	
    
	File sdRoot = Environment.getExternalStorageDirectory();
	
	// Special layout things we need
	public TextView changeLog;
    public TextView checkedVersion;
    public Boolean dataCheck;
    
    public Button btnCheckUpdate;
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        	case R.id.overflow_download:
                if (isOnline() == true) {
                	new UpdateCCPAsync().execute(dlURL + versionNew + ".zip");;
                } else {
                	btnCheckUpdate.setText(getResources().getString(R.string.network_error));
                	showDialog();
                }
        		break; 
        	case R.id.overflow_check_again:
        		if (isOnline() == true) {
                    new checkVersion().execute("");
                } else {
                	showDialog();
                }
        		break; 
        	case R.id.overflow_install:
        		try {
        			//opens the downloaded up for install
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory()
                        + "/newrom/"
                        + romName + versionNew + ".zip")),
                        "application/vnd.android.package-archive");
                    startActivity(intent);
        		} catch (Exception e) {
        			Log.e(TAG, "error, cannot find apk");
        		}
        		break;
        	case android.R.id.home:
        		Intent intent = new Intent(getBaseContext(), UpdateActivity.class);
        		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
        		break;
        	default:
        		return true;
    	}
    	return super.onOptionsItemSelected(item);
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	requestWindowFeature(Window.FEATURE_PROGRESS);
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	super.onCreate(savedInstanceState);

        //setting some display        
    	setProgressBarVisibility(false);
        setProgressBarIndeterminateVisibility(false);
        
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.update_view);
        btnCheckUpdate= (Button)findViewById(R.id.button_check_update);
        checkedVersion = (TextView)findViewById(R.id.text_new_version);  
        changeLog = (TextView)findViewById(R.id.change_log);
        
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mMenuDefault = fm.findFragmentByTag("m1");
        if (mMenuDefault == null) {
            mMenuDefault = new MenuDefault();
            ft.add(mMenuDefault, "m1");
        }
        mMenuDownload = fm.findFragmentByTag("m2");
        if (mMenuDownload == null) {
            mMenuDownload = new MenuDownload();
            ft.add(mMenuDownload, "m2");
        }
        mMenuDownloadNow = fm.findFragmentByTag("m3");
        if (mMenuDownloadNow == null) {
            mMenuDownloadNow = new MenuDownloadNow();
            ft.add(mMenuDownloadNow, "m3");
        }
        mMenuInstall = fm.findFragmentByTag("m4");
        if (mMenuInstall == null) {
            mMenuInstall = new MenuDefault();
            ft.add(mMenuInstall, "m4");
        }
        ft.commit();
        updateMenu(mMenuDefault);
        //making sure the download directory exists
        checkDirectory("/newrom");
        
        //call for versioninfo
        getVersionInfo();

        if (isOnline() == true) {
            new checkVersion().execute("");
        } else {
        	btnCheckUpdate.setText(getResources().getString(R.string.network_error));
        	showDialog();
        }
        

        //layout for textview
        TextView textVersion = (TextView)findViewById(R.id.text_about_cur);
        
        String versionString = String.format("Current ROM version: %s", versionCurrent);
        
        textVersion.setText(versionString);  
        
        btnCheckUpdate.setEnabled(false);
        
        btnCheckUpdate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (btnCheckUpdate.getText().equals(getResources().getString(R.string.install_now))) {
            		// copy over rebooter binary
            		copyAssets();
            		if (ShellInterface.isSuAvailable()) { 
            			ShellInterface.runCommand("/data/data/net.classic.update/rebooter recovery"); 
            		}
            		
            	} else if (btnCheckUpdate.getText().equals(getResources().getString(R.string.update_available))) {
                    if (isOnline() == true) {
                    	new UpdateCCPAsync().execute(dlURL + versionNew + ".zip");
                    } else {
                    	btnCheckUpdate.setText(getResources().getString(R.string.network_error));
                    	showDialog();
                    }
            	}
            }
        });
    }

    private class checkVersion extends AsyncTask<String, Void, Boolean> {        
		protected void onPreExecute() { 
			updateMenu(null);
			setProgress(Window.PROGRESS_END);
            setProgressBarIndeterminateVisibility(true);      
		}       
		// automatically done on worker thread (separate from UI thread)        
		protected Boolean doInBackground(final String... args) {
			try {
                //connecting to url            	
                URL u = new URL(updateURL);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.connect();
               
                //this is where the file will be seen after the download
                FileOutputStream f = new FileOutputStream(new File(sdRoot + "/newrom/" , update));
                //file input is from the url
                InputStream in = conn.getInputStream();

                //here's the download code
                byte[] buffer = new byte[1024];
                int len1 = 0;
                
                while ((len1 = in.read(buffer)) > 0) {
                    f.write(buffer, 0, len1);
                }
                f.close();
                
                URL u1 = new URL(changeURL);
                HttpURLConnection conn1 = (HttpURLConnection) u1.openConnection();
                conn1.setRequestMethod("GET");
                conn1.setDoOutput(true);
                conn1.connect();
                
                //this is where the file will be seen after the download
                FileOutputStream f1 = new FileOutputStream(new File(sdRoot + "/newrom/", changelog));
                //file input is from the url
                InputStream in1 = conn1.getInputStream();

                //here's the download code
                byte[] buffer1 = new byte[1024];
                int len = 0;
                
                while ((len = in1.read(buffer1)) > 0) {
                    f1.write(buffer1, 0, len);
                }
                f1.close();

               
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
			
			return true;
		}        
		// can use UI thread here        
		protected void onPostExecute(final Boolean success) {           
			setProgressBarIndeterminateVisibility(false);   
			if (success) { 				
				File updateTxt = new File(sdRoot + "/newrom/", update);
				versionNew = "";
				if (updateTxt.exists()) {
		            try {  
		                BufferedReader reader = new BufferedReader(new FileReader(updateTxt));  
		                String lineIn;  
		                lineIn = reader.readLine();
		                while (lineIn != null) {  
		                	versionNew += lineIn;
		                    lineIn = reader.readLine(); 
		                }  
		                reader.close();
		            }
		            catch (IOException e) {
		            	Log.e(TAG, e.getMessage());
		            }
				}
				  
				checkedVersion.setText("Latest ROM version: " + versionNew);
				
				StringBuilder read = new StringBuilder();
				File updateTxt1 = new File(sdRoot + "/newrom/", changelog);
				if (updateTxt1.exists()) {
		            try {  
		                BufferedReader reader = new BufferedReader(new FileReader(updateTxt1));  
		                String lineIn;  
		                while ((lineIn = reader.readLine()) != null) 
		                	read.append(lineIn).append('\n');
		                reader.close();
		            }
		            catch (IOException e) {
		            	Log.e(TAG, e.getMessage());
		            }
				}
				// remove extra line at end
				int size = read.length();
				read.setLength(size - 1);
				
				changeLog.setVisibility(View.VISIBLE);
				changeLog.setText(read);
				  
				if (!versionCurrent.equals(versionNew)) {
					// Newer version available
					updateMenu(mMenuDownloadNow);
					btnCheckUpdate.setEnabled(true);
					btnCheckUpdate.setText(getResources().getString(R.string.update_available));
					Toast.makeText(Update.this, "Version: " + versionNew + " available!", 
						  Toast.LENGTH_SHORT).show();
				} else {
					// Up to date
					updateMenu(mMenuDefault);
					btnCheckUpdate.setEnabled(false);
					btnCheckUpdate.setText(getResources().getString(R.string.update_unavailable));
					Toast.makeText(Update.this, "No update available", 
						  Toast.LENGTH_SHORT).show();
				} 
			} else {    
				Toast.makeText(Update.this, "Failed", Toast.LENGTH_SHORT).show();
			}
		}
	}
    
    class UpdateCCPAsync extends AsyncTask<String, String, String> {
    	
    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
            updateMenu(mMenuDownload);
            setProgressBarVisibility(true);
            setProgressBarIndeterminateVisibility(false);
            
        }
       
        @Override
        protected String doInBackground(String... aurl) {

            try {
                //connecting to url            	
                URL u = new URL(dlURL + versionNew + ".zip");
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.connect();
               
                //lenghtOfFile is used for calculating download progress
                int lenghtOfFile = conn.getContentLength();
               
                //this is where the file will be seen after the download
                FileOutputStream f = new FileOutputStream(new File(sdRoot + "/newrom/", romName + versionNew + ".zip"));
                
                //file input is from the url
                InputStream in = conn.getInputStream();

                //here's the download code
                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
               
                while ((len1 = in.read(buffer)) > 0) {
                    total += len1; //total = total + len1
                    publishProgress("" + (int)((total*100)/lenghtOfFile));
                    f.write(buffer, 0, len1);
                }
                f.close();
                
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
           
            return null;
        }
        
        protected void onProgressUpdate(String... progress) {
            Log.d(TAG,progress[0]);
            setProgress(100 * Integer.parseInt(progress[0]));
            btnCheckUpdate.setText("Downloaded " + progress[0] + "/100");
       }

        @Override
        protected void onPostExecute(String unused) {
            //dismiss the dialog after the file was downloaded
        	setProgressBarVisibility(false);
        	updateMenu(mMenuInstall);
            btnCheckUpdate.setEnabled(true);
			btnCheckUpdate.setText(getResources().getString(R.string.install_now));
			Toast.makeText(Update.this, "Download complete!", 
					  Toast.LENGTH_SHORT).show();
        }
    }
    
    public static class MenuDefault extends Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        	menu.add(0, R.id.overflow_check_again, 0, "Check again")
    		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
    }
    
    public static class MenuDownload extends Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        	menu.add("Downloading...")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
    }
    
    public static class MenuDownloadNow extends Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        	menu.add(0, R.id.overflow_download, 0, "Download now!")
    		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
    }
    
    public static class MenuInstall extends Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        	menu.add(0, R.id.overflow_install, 0, "Install")
    		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
    }
    
    void updateMenu(Fragment Menu) {
    	getActionBar().removeAllTabs();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mMenuDefault);
        ft.hide(mMenuDownload);
        ft.hide(mMenuDownloadNow);
        ft.hide(mMenuInstall);
        if (Menu != null) ft.show(Menu);
        ft.commit();
    }
    
    void showDialog() {
        MyAlertDialog newFragment = MyAlertDialog.newInstance(
                R.string.network_error, R.string.connect_data);
        newFragment.show(getFragmentManager(), "dialog");
    }
    
    //function to verify if directory exists
    public void checkDirectory(String dirName){
        File new_dir = new File( sdRoot + dirName );
        if( !new_dir.exists() ){
            new_dir.mkdirs();
        }
    }
    
    public boolean isOnline() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
        	Log.d(TAG, "Network available");
            return true;
        }
        Log.d(TAG, "Network unavailable");
        return false;
    }
	
    private void getVersionInfo () {
    	Process p = null;
    	
    	try {
			p = Runtime.getRuntime().exec("getprop net.rom.version");
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			versionCurrent = bis.readLine();
		} catch (java.io.IOException e) {
			//
		}
    }
    
    private void copyAssets() {
        AssetManager assetManager = getAssets();
            InputStream in = null;
            OutputStream out = null;
            try {
              in = assetManager.open("rebooter");   
              out = new FileOutputStream("/data/data/net.classic.update/rebooter");
              copyFile(in, out);
              Runtime.getRuntime().exec("chmod 755 /data/data/net.classic.update/" + "rebooter");
              in.close();
              in = null;
              out.flush();
              out.close();
              out = null;
            } catch(Exception e) {
                Log.e("tag", e.getMessage());
            }       
    }
    
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
          out.write(buffer, 0, read);
        }
    }
}