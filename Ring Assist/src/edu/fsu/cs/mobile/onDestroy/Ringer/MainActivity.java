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

    //here trying to do the map view stuff (for premium)
    /*MapView map;
    MapController mc;
    LocationManager lm;
    GeoPoint geoPoint;
    Drawable marker;*/

    /*class MyOverlay extends ItemizedOverlay<OverlayItem>
    {
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

        public MyOverlay(Drawable drawable)
        {
            super(drawable);

            boundCenterBottom(drawable);

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
                        else if ("4".equals(modePreference))
                            modePreference = "Loudest";

                        //listItems.add(mCursor.getString(1));    //right now only outputs the name

                        items.add(new OverlayItem(new GeoPoint((int) (mCursor.getInt(3) * 1E6),
                                (int) (mCursor.getInt(2) * 1E6)),
                                mCursor.getString(1), modePreference));
                    }
                }

                //items.add(new OverlayItem(geoPoint, "Hello", "Welcome to the Mobile Lab!"));
                //items.add(new OverlayItem(new GeoPoint((int) (30.446172 * 1E6),
                        //(int) (-84.299466 * 1E6)),
                        //"You're late", "You're late, Class starts at 2PM!"));

                populate();
            }
        }

        @Override
        protected OverlayItem createItem(int index)
        {
            return items.get(index);
        }

        @Override
        protected boolean onTap(int i)
        {
            Toast.makeText(MainActivity.this,
                    items.get(i).getSnippet(),
                    Toast.LENGTH_SHORT).show();
            return(true);
        }

        @Override
        public int size()
        {
            return items.size();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //map = (MapView) findViewById(R.id.mapView);
        //mc = map.getController();



        map.setBuiltInZoomControls(true);

        geoPoint = new GeoPoint((int) (30.446142 * 1E6), (int) (-84.299673 * 1E6));
        mc.setCenter(geoPoint);

        marker = getResources().getDrawable(R.drawable.ic_launcher);
        marker.setBounds(105, 105, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());

        mc.setZoom(17);

        map.getOverlays().add(new MyOverlay(marker));
    }

    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
    }*/

    public void onBackPressed()
    {
		if(System.currentTimeMillis()-time<=4000)
		{
			moveTaskToBack(true);
			
			/*Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); */
            

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
        //force actionBar Menu overflow

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
        mAddButton = (ImageButton)findViewById(R.id.addButton);
        mEditButton = (ImageButton)findViewById(R.id.editButton);
        mDeleteButton = (ImageButton)findViewById(R.id.deleteButton);
        mMainView = (ListView)findViewById(R.id.mainView);
        mOnOff = (ToggleButton)findViewById(R.id.onOff);

        //sets the buttons used with activity_main.xml as clickable
        mAddButton.setOnClickListener(this);
        mEditButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
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
               

              /*  Toast.makeText(getApplicationContext(),     //simply for error checking
                        ((TextView) view).getText(),
                        Toast.LENGTH_SHORT).show(); */
            }
        });

        settings = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        toggleSetting = settings.getBoolean("toggleValue", false);
        Log.i("toggleSetting is ","" +toggleSetting);

        if (toggleSetting == true)
        {
            mOnOff.setChecked(true);
            //send an appropriate notification
            notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            notification = new Notification(R.drawable.ic_launcher,
                    "Ring Assist", System.currentTimeMillis());
            notification.flags |= Notification.FLAG_NO_CLEAR;
           //Intent intent = new Intent();
            //go to app when notification is touched
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0); 
            notification.setLatestEventInfo(this, "Ring Assist",
                    "Adjusting Ringer For You", activity);
            notification.number += 1;
            notificationManager.notify(0, notification);
        
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
                    else if ("4".equals(modePreference))
                        modePreference = "Loudest";

                    listItems.add(mCursor.getString(1));    //right now only outputs the name
                }
            }

            //finalizes the listView additions
            adapter.notifyDataSetChanged();
        }
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
        //handles when the add button is clicked from activity_main.xml
        if (v == mAddButton)
        {
            //launches the AddActivity
            Intent myIntent = new Intent(MainActivity.this, AddActivity.class);
            startActivityForResult(myIntent, 500);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

           // startActivity(myIntent);
           
        }

        //handles when the delete button is clicked from activity_main.xml
        if (v == mDeleteButton)
        {
            Intent myIntent = new Intent(MainActivity.this, DeleteActivity.class);
            
            startActivityForResult(myIntent, 500);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            //startActivity(myIntent);
           
        }

        //handles when the toggle button is clicked (eventually would enable or disable
        //Ring Assist features (need to revamp this so that when go back from delete, add, edit
        //it stays as it was (i.e. either on or off depending on last action)
        if (v == mOnOff)
        {
            //if after clicking the toggle is on...
            if (mOnOff.isChecked() == true)
            {
                //send an appropriate notification
                notificationManager = (NotificationManager)
                        getSystemService(NOTIFICATION_SERVICE);
                notification = new Notification(R.drawable.ic_launcher,
                        "Ring Assist", System.currentTimeMillis());
                notification.flags |= Notification.FLAG_NO_CLEAR;
                Intent intent = new Intent();
                PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
                notification.setLatestEventInfo(this, "Ring Assist",
                        "Adjusting Ringer For You", activity);
                notification.number += 1;
                notificationManager.notify(0, notification);
            }
            //otherwise if after click it is off...
            else
            {
                notificationManager.cancel(0);
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
