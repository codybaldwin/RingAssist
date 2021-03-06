package edu.fsu.cs.mobile.onDestroy.Ringer;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

//reads in the user information and stores into database, at end calls mainActivity again
public class AddActivity extends Activity implements OnClickListener /*, LocationListener */
{
    //variables used from add.xml
    EditText mNameET;
    Spinner mModeSpinner;
    ImageButton mAddToProvider;
    ImageButton mGetCurrentButton;
    CheckBox mSendText;
    EditText mMessageET;
    static double providerLatitude;
    static double providerLongitude;
    LocationManager mLocationManager;
    public boolean hasLocation = false;
  
    //animates the activity changes when back button is pressed OR when the "up" button is pressed
    @Override
    public void onBackPressed()
    {
    	Intent myIntent = new Intent(AddActivity.this, MainActivity.class);
    	startActivityForResult(myIntent, 500);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	Intent myIntent = new Intent(AddActivity.this, MainActivity.class);
            	startActivityForResult(myIntent, 500);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return(true);
        }

        return(super.onOptionsItemSelected(item));
    }
    
    //
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	ActionBar a = getActionBar();
    	a.setTitle("Add Location");
    	a.setDisplayHomeAsUpEnabled(true);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.add);
       

        //get id's of add.xml
        mNameET = (EditText)findViewById(R.id.name);
        mModeSpinner = (Spinner)findViewById(R.id.mode);
        mAddToProvider = (ImageButton)findViewById(R.id.addToProvider);
        mGetCurrentButton = (ImageButton)findViewById(R.id.getCurrent_button);
        mSendText = (CheckBox)findViewById(R.id.sendText);
        mMessageET = (EditText)findViewById(R.id.message);

        //set listener for buttons in add.xml
        mAddToProvider.setOnClickListener(this);
        mGetCurrentButton.setOnClickListener(this);

      /*  //here initially finds the location of the user but does not actually set any variables
        //used in the content provider until button is clicked
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        
      //  mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(criteria, true), 0, 0, this);
        Location location;
      
        	location = mLocationManager.getLastKnownLocation(mLocationManager.NETWORK_PROVIDER);
        Log.i("Accuracy is", location.getAccuracy()+"");
        if(location != null && location.getAccuracy()!= 0 && location.getAccuracy()<200) //&& location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Log.i("longitude is "," " + longitude);
            Log.i("latitude is ", " " + latitude);
        }
        else
        {
        	
            //calls function below if last known location is null
        	 mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        	 if(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)==null)
        	 {
        		 Toast.makeText(getApplicationContext(),"Location Unavailable!", Toast.LENGTH_LONG).show(); 
        		 Intent myIntent = new Intent(AddActivity.this, MainActivity.class);
                 startActivityForResult(myIntent, 500);
                 overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                 finish();
        	 }
        }

        //sets the longitude and latitude to whatever is most current
         */
        mGetCurrentButton.setOnClickListener(new View.OnClickListener()
        {
            //@Override
            public void onClick(View v)
            {
                providerLatitude = Globals.latitude;
                providerLongitude = Globals.longitude;
                hasLocation = true;
                Log.i("in click latitude ","" + providerLatitude);
                Log.i("in click longitude ", "" + providerLongitude);

                Toast.makeText(getApplicationContext(),"lat is " + providerLatitude +
                        " long is " + providerLongitude + " with accuracy "+Globals.accuracy, Toast.LENGTH_SHORT).show();
            }
        }); 
    }

    public void onClick(View v)
    {
        //handles when addToProvider Button clicked
        if (v == mAddToProvider)
        {
            if(!hasLocation)
            {
            	Toast.makeText(getApplicationContext(),"You must Get Location before adding!", Toast.LENGTH_SHORT).show();
            	return;
            }
        	//try to get current location in order to get longitude and latitude
            //should work fine once have button (can't do in onCreate())

            //gets the selected value for the spinner
            String spinnerSelection = mModeSpinner.getSelectedItem().toString();
            int spinnerSelectionInt = 2;

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

            Log.i("spinner number is ", " "+spinnerSelectionInt);       //error checking

            int sendTextNumber = 0;
            if (mSendText.isChecked() == true)
                sendTextNumber = 1;

            Uri mNewUri;
            ContentValues mNewValues = new ContentValues();

            //actually gets the values needed to insert (will have more values eventually)
            mNewValues.put(RingAssistProvider.COLUMN_NAME, mNameET.getText().toString().trim());
            mNewValues.put(RingAssistProvider.COLUMN_LONGITUDE, providerLongitude);
            mNewValues.put(RingAssistProvider.COLUMN_LATITUDE, providerLatitude);
            mNewValues.put(RingAssistProvider.COLUMN_PREFERENCE, spinnerSelectionInt);
            mNewValues.put(RingAssistProvider.COLUMN_SENDTEXT, sendTextNumber);
            mNewValues.put(RingAssistProvider.COLUMN_MESSAGE, mMessageET.getText().toString().trim());

            //sends the values to the content provider for insert
            mNewUri = getContentResolver().insert(RingAssistProvider.CONTENT_URI, mNewValues);

            //launches the MainActivity when insert is complete
            Intent myIntent = new Intent(AddActivity.this, MainActivity.class);
            startActivityForResult(myIntent, 500);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        }
    }

    //finds the user's location if the last known location is null (uses gps, so maybe only works outdoors)
    public void onLocationChanged(Location location)
    {
      /*  if (location != null && location.getAccuracy()<200)
        {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            Log.v("Location Changed", latitude + " and " + longitude);

          //  mLocationManager.removeUpdates(this);
        } */
    }

    //required functions, don't need definitions for our application Required functions
    public void onProviderDisabled(String arg0)
    { }
    public void onProviderEnabled(String arg0)
    { }
    public void onStatusChanged(String arg0, int arg1, Bundle arg2)
    { }
}
