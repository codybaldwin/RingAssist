package edu.fsu.cs.mobile.onDestroy.Ringer;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

//class that handles when a list item is clicked in the main Activity (edit mode)
public class EditActivity extends Activity implements OnClickListener, LocationListener
{
    //variables used below
    EditText mNameET;
    Spinner mEditModeSpinner;
    ImageButton mEditProvider;
    ImageButton mEditGetCurrentButton;
    CheckBox mEditSendText;
    EditText mEditMessageET;
    Cursor mCursor;
    static double longitude;
    static double latitude;
    static double providerLatitude;
    static double providerLongitude;
    LocationManager mLocationManager;
    boolean sendTextBool = false;
    int sendTextNumber = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);          //loads the edit.xml

        //get the id's from edit.xml
        mNameET = (EditText)findViewById(R.id.editName);
        mEditModeSpinner = (Spinner)findViewById(R.id.editMode);
        mEditProvider = (ImageButton)findViewById(R.id.editProvider);
        mEditGetCurrentButton = (ImageButton)findViewById(R.id.editGetCurrent_button);
        mEditSendText = (CheckBox)findViewById(R.id.editSendText);
        mEditMessageET = (EditText)findViewById(R.id.editMessage);

        //set listener for buttons in add.xml
        mEditProvider.setOnClickListener(this);
        mEditGetCurrentButton.setOnClickListener(this);

        //here initially finds the location of the user but does not actually set any variables
        //used in the content provider until button is clicked
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Log.i("longitude is "," " + longitude);     //testing purposes
            Log.i("latitude is ", " " + latitude);      //testing purposes
        }
        else
        {
            //calls function below if last known location is null
        	 mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        	 if(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)==null)
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        //sets the longitude and latitude to whatever is most current
        mEditGetCurrentButton.setOnClickListener(new View.OnClickListener()
        {
            //@Override
            public void onClick(View v)
            {
                providerLatitude = latitude;
                providerLongitude = longitude;

                Log.i("in click latitude ","" + providerLatitude);
                Log.i("in click longitude ", "" + providerLongitude);

                Toast.makeText(getApplicationContext(),"lat is " + providerLatitude +
                        " long is " + providerLongitude, Toast.LENGTH_SHORT).show();
            }
        });

        //populate edit.xml with appropriate data..............
        //gets the integer _ID sent from MainActivity when list item clicked for edit
        Bundle extras = getIntent().getExtras();

        if(extras != null)
        {
            String tupleName = extras.getString("tupleName");

            //selects only the tuple that matches the COLUMN_NAME
            //could make it more specific if had mode but need list view with text views inside
            String mSelectionClause = RingAssistProvider.COLUMN_NAME +  " = ? ";
            String[] mSelectionArgs = {tupleName};

            //make the query that will be used for the update
            mCursor = getContentResolver().query(RingAssistProvider.CONTENT_URI,
                    null, mSelectionClause, mSelectionArgs, null);
        }

        //if the cursor isn't empty, then populate edit.xml
        if(mCursor != null)
        {
            if(mCursor.getCount() > 0)
            {
                mCursor.moveToNext();

                String modePreference = mCursor.getString(4).trim();
                int preferenceNumber = Integer.valueOf(modePreference);

                String sendTextString = mCursor.getString(5).trim();
                if ("1".equals(sendTextString))
                {
                    sendTextNumber = 1;
                    sendTextBool = true;
                }

                Log.i("sendTextBool in edit" ," " + sendTextBool);
                Log.i("sendTextNumber in edit", " " + sendTextNumber);

                Log.i("modePreference is ", modePreference);
                Log.i("preferenceNumber", " " + preferenceNumber);

                mNameET.setText(mCursor.getString(1));
                mEditModeSpinner.setSelection(preferenceNumber + 1);    //is plus one b/c not zero indexed?
                mEditSendText.setChecked(sendTextBool);
                mEditMessageET.setText(mCursor.getString(6));
            }
        }
        else
        {
            Log.i("the cursor must be empty", "");
        }
    }

    public void onClick(View v)
    {
        //handles when addToProvider Button clicked
        if (v == mEditProvider)
        {
            Bundle extras = getIntent().getExtras();

            if(extras != null)
            {
                String tupleName = extras.getString("tupleName");

                //gets the selected value for the spinner
                String spinnerSelection = mEditModeSpinner.getSelectedItem().toString();
                int spinnerSelectionInt = 4;

                Log.i("spinner is ", spinnerSelection);     //error checking

                //gets the according integer based on the mode selected
                if ("Silent".equals(spinnerSelection))
                    spinnerSelectionInt = 0;
                else if ("Vibrate".equals(spinnerSelection))
                    spinnerSelectionInt = 1;
                else if ("Normal".equals(spinnerSelection))
                    spinnerSelectionInt = 2;
                else if ("Loud".equals(spinnerSelection))
                    spinnerSelectionInt = 3;
                else if ("Loudest".equals(spinnerSelection))
                    spinnerSelectionInt = 4;
                sendTextNumber=0;
                if (mEditSendText.isChecked() == true)
                    sendTextNumber = 1;

                //here will update provider and send back to the main activity
                ContentValues mUpdateValues = new ContentValues();

                //choose which columns to update and with what information
                mUpdateValues.put(RingAssistProvider.COLUMN_NAME, mNameET.getText().toString().trim());
                mUpdateValues.put(RingAssistProvider.COLUMN_LONGITUDE, providerLongitude);
                mUpdateValues.put(RingAssistProvider.COLUMN_LATITUDE, providerLatitude);
                mUpdateValues.put(RingAssistProvider.COLUMN_PREFERENCE, spinnerSelectionInt);
                mUpdateValues.put(RingAssistProvider.COLUMN_SENDTEXT, sendTextNumber);
                mUpdateValues.put(RingAssistProvider.COLUMN_MESSAGE, mEditMessageET.getText().toString().trim());

                //selects only the tuple that matches the COLUMN_NAME (if need to could just identify
                //with super key as name and preference or something similar) instead of _ID
                String mSelectionClause = RingAssistProvider.COLUMN_NAME +  " = ? ";
                String[] mSelectionArgs = {tupleName};

                int mRowsUpdated = 0;

                mRowsUpdated = getContentResolver().update(
                        RingAssistProvider.CONTENT_URI,
                        mUpdateValues,
                        mSelectionClause,
                        mSelectionArgs
                        );
            }
            else
            {
                Log.i("editCursor ", "must be empty");
            }

            //only here at top for testing, will eventually be at bottom
            Intent myIntent = new Intent(EditActivity.this, MainActivity.class);
            startActivity(myIntent);
        }
    }

    //finds the user's location if the last known location is null (uses gps, so maybe only works outdoors)
    public void onLocationChanged(Location location)
    {
        if (location != null)
        {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            Log.v("Location Changed", latitude + " and " + longitude);
            mLocationManager.removeUpdates(this);
        }
    }

    //Required functions, don't need definitions for our application
    public void onProviderDisabled(String arg0)
    { }

    public void onProviderEnabled(String arg0)
    { }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2)
    { }
}
