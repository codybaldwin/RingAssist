package edu.fsu.cs.mobile.onDestroy.Ringer;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//main activity that deals with interactions on activity_main.xml
public class MainActivity extends Activity implements OnClickListener   //did extend Activity
{
    
	//variables used with activity_main.xml
	public GPSTracker gps;
	long time=0;
    ImageButton mAddButton;
    ImageButton mEditButton;
    ImageButton mDeleteButton;
    Cursor mCursor;
    ListView mMainView;
    ToggleButton mOnOff;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    NotificationManager notificationManager;
    Notification notification;
    public static final String PREFS_NAME = "TogglePrefFile";      //used for toggle button shared preferences
    SharedPreferences settings;
    boolean toggleSetting;

    public void onBackPressed()
    {
		if(System.currentTimeMillis()-time<=4000)
		{
			moveTaskToBack(true);
        }
        else
        {
        	time=System.currentTimeMillis();
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
        }
        
    } 
    
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	 gps = GPSTracker.Instance(getApplicationContext());
        //force actionBar Menu overflow
    	//startService(new Intent(this, GPSTracker.class));
    	//Intent startIntent = new Intent(getApplicationContext(),GPSTracker.class);
    //	startService(startIntent);
    	
    	     try {
    	        ViewConfiguration config = ViewConfiguration.get(this);
    	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
    	        if(menuKeyField != null) {
    	            menuKeyField.setAccessible(true);
    	            menuKeyField.setBoolean(config, false);
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    	
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);         //inflate the main UI (activity_main.xml)

        //get id's used with activity_main.xml
      
        mEditButton = (ImageButton)findViewById(R.id.editButton);
      
        mMainView = (ListView)findViewById(R.id.mainView);
        mOnOff = (ToggleButton)findViewById(R.id.onOff);

        //sets the buttons used with activity_main.xml as clickable
      
        mEditButton.setOnClickListener(this);
        mOnOff.setOnClickListener(this);
        

        //will fire off edit activity when a list item is clicked (anonymous listener)
        mMainView.setOnItemClickListener(new OnItemClickListener()
        {
            //launches EditActivity with one extra that is the name
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //launches EditActivity and ads tuple position as an extra (for provider to use)
                Intent myIntent = new Intent(MainActivity.this, EditActivity.class);
                myIntent.putExtra("tupleName", (((TextView) view).getText()));     //plus two b/c unreachable tuples at 0 and 1 indexes
                
                startActivityForResult(myIntent, 500);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                //startActivity(myIntent);
               
            }
        });
        LocationManager lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        settings = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        toggleSetting = settings.getBoolean("toggleValue", false);
        //if location services are not enabled, don't allow the toggle button to be true!
        if(toggleSetting && lManager.getLastKnownLocation(lManager.NETWORK_PROVIDER)==null)
        	{
        	toggleSetting=false;
        	Toast.makeText(this, "Turn on Location Services", Toast.LENGTH_LONG).show();
        	}
        
        Log.i("toggleSetting is ","" +toggleSetting);

    	notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if (toggleSetting == true)
        {
            
        	mOnOff.setChecked(true);
            notification = new Notification(R.drawable.ic_launcher,
                    "Ring Assist", System.currentTimeMillis());
            notification.flags |= Notification.FLAG_NO_CLEAR;
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
            notification.setLatestEventInfo(this, "Ring Assist",
                    "Adjusting Ringer For You", activity);
            notification.number = 1;
            notificationManager.notify(0, notification);
           
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            //  update location based on the Network Provider every 5 mins
        //    lManager.requestLocationUpdates(lManager.NETWORK_PROVIDER, 2*60*1000, 20, CallAndSmsReceiver.lListener);
        }
        else
            mOnOff.setChecked(false);

        //gets the listView ready for entries
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        mMainView.setAdapter(adapter);

        //get's the entire UserInformation table into the cursor
        mCursor = getContentResolver().query(RingAssistProvider.CONTENT_URI,
                null, null, null, null);

        //if the cursor isn't empty, then populate listView from database
        if(mCursor != null)
        {
            if(mCursor.getCount() > 0)
            {
                //mCursor.moveToNext();

                while (mCursor.moveToNext())
                {
                    String modePreference = mCursor.getString(5).trim();

                    if ("0".equals(modePreference))
                        modePreference = "Silent";
                    else if ("1".equals(modePreference))
                        modePreference = "Vibrate";
                    else if ("2".equals(modePreference))
                        modePreference = "Normal";
                    else if ("3".equals(modePreference))
                        modePreference = "Loud";

                    listItems.add(mCursor.getString(1));    //right now only outputs the name
                }
            }

            //finalizes the listView additions
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy()
    {
    	//stopService(new Intent(this, GPSTracker.class));

    	//Intent stopIntent = new Intent(getApplicationContext(),GPSTracker.class);
    	//stopService(stopIntent);
    }
    
    //overriding simply for the shared preferences to be able to store just in case (not sure if
    //need this since also save the shared preferences every time they change it)
    
    @Override
    protected void onStop()
    {
        super.onStop();

        //save the toggle value to shared preferences (right way might be in saved bookmark)
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("toggleValue", mOnOff.isChecked());
        Log.i("onStop() toggle is ", "" + mOnOff.isChecked());
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	  MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.actionbar_menu, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add:
            	Intent myIntent = new Intent(MainActivity.this, AddActivity.class);
                startActivityForResult(myIntent, 500);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            case R.id.action_discard:
            	Intent myIntent2 = new Intent(MainActivity.this, DeleteActivity.class);
                startActivityForResult(myIntent2, 500);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //handles what happens when add,edit,or delete buttons clicked
    public void onClick(View v)
    {


        //handles when the toggle button is clicked (eventually would enable or disable
        //Ring Assist features (need to revamp this so that when go back from delete, add, edit
        //it stays as it was (i.e. either on or off depending on last action)
        if (v == mOnOff)
        {
        	LocationManager lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            /*LocationListener lListener = new LocationListener()
            {
                //@Override
                //@Override
                public void onLocationChanged(Location location) {Log.i("Location Update", "success");}
                //@Override
                //@Override
                public void onProviderDisabled(String provider) {}
                //@Override
                //@Override
                public void onProviderEnabled(String provider) {}
                //@Override
                //@Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
            }; */
        	notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            //if after clicking the toggle is on...
            if (mOnOff.isChecked() == true)
            {
            	//if location services are off, don't allow toggle button to be on!
               if(!gps.canGetLocation())
               {
            	   mOnOff.setChecked(false);
            	   gps.showSettingsAlert();
            	   return;
               }
               
            	//send an appropriate notification
                
                notification = new Notification(R.drawable.ic_launcher,
                        "Ring Assist", System.currentTimeMillis());
                notification.flags |= Notification.FLAG_NO_CLEAR;
                Intent intent = new Intent(this, MainActivity.class);
                PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
                notification.setLatestEventInfo(this, "Ring Assist",
                        "Adjusting Ringer For You", activity);
                notification.number = 1;
                notificationManager.notify(0, notification);
               
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                //  update location based on the best provider
           //     lManager.requestLocationUpdates(lManager.NETWORK_PROVIDER, 5*60*1000, 20, CallAndSmsReceiver.lListener); //update location every 5 mins in bg
               
              
             
                
            }
            //otherwise if after click it is off...
            else
            {
                notificationManager.cancel(0);
              //  lManager.removeUpdates(CallAndSmsReceiver.lListener);
            }

            //save the toggle value to shared preferences (right way might be in saved bookmark)
            //not sure if needed/will work here but probably should have here also
            settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("toggleValue", mOnOff.isChecked());
            Log.i("button press toggle is ", "" + mOnOff.isChecked());
            editor.commit();
        }
    }
}
