package edu.fsu.cs.mobile.onDestroy.Ringer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
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
    LocationManager mLocationManager;   //new, for use when checking on button

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
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
                startActivity(myIntent);

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
            Intent intent = new Intent();
            // Intent intent = new Intent(this, MainActivity.class);
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
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    //handles what happens when add,edit,or delete buttons clicked
    public void onClick(View v)
    {
        //handles when the add button is clicked from activity_main.xml
        if (v == mAddButton)
        {
            //launches the AddActivity
            Intent myIntent = new Intent(MainActivity.this, AddActivity.class);
            startActivity(myIntent);
        }

        //handles when the delete button is clicked from activity_main.xml
        if (v == mDeleteButton)
        {
            Intent myIntent = new Intent(MainActivity.this, DeleteActivity.class);
            startActivity(myIntent);
        }

        //handles when the toggle button is clicked (eventually would enable or disable
        //Ring Assist features (need to revamp this so that when go back from delete, add, edit
        //it stays as it was (i.e. either on or off depending on last action)
        if (v == mOnOff)
        {
            //if after clicking the toggle is on...
            if (mOnOff.isChecked() == true)
            {
                //check whether gps on or off on phone hardware (all new in this block)
                mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (statusOfGPS)
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
                else
                {
                    mOnOff.setChecked(false);
                    Toast.makeText(getApplicationContext(),
                            "Oops GPS Is Disabled!",
                            Toast.LENGTH_SHORT).show();
                }
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
