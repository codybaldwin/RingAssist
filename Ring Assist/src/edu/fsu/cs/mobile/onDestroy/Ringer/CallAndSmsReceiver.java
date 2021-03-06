package edu.fsu.cs.mobile.onDestroy.Ringer;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


//Note...commented out the @Overrides to compile

public class CallAndSmsReceiver extends BroadcastReceiver
{
    
	// A request to connect to Location Services
//  arbitrarily setting radius until able to get it from activity or decided upon by group
    final static double RADIUSINFEET = 300;
    final static double RADIUS = RADIUSINFEET / (60 * 5280 * 1.15); //  in degrees latitude/longitude
    final static int RING_CHANGED_NOTIFICATION_ID = 10;
    public final static String PREFS_NAME = "TogglePrefFile";
    public boolean sent = false;
    //constructor requires provider, but it will be replaced by locationManager
    public static Location userLocation= new Location("dummyProvider");
    
  /* public static LocationListener lListener = new LocationListener()
    {
        public void onLocationChanged(Location location) 
        {
            //only replace the location if the accuracy is within 200ft
        	if (location.getAccuracy()< 200 && location.getAccuracy()!=0)
        		{
        			userLocation=location;
        			//Toast.makeText(null, "Location Updated!", Toast.LENGTH_LONG).show();
        			
                }
        	
        
        }
        	

        public void onProviderDisabled(String provider) {}

        public void onProviderEnabled(String provider) {}

        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }; 
    */
    Cursor cursor;
    
    
    
    @Override
    public void onReceive(final Context context, Intent intent)
    {
        MainActivity.gps = GPSTracker.Instance(context);
    	Log.i("just got inside ", " onReceive()");

       // Toast.makeText(context, "entered on receive", Toast.LENGTH_LONG).show();

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        boolean toggleSetting = settings.getBoolean("toggleValue", false);
        Log.i("toggleSetting is ", "" + toggleSetting);
        
        if (toggleSetting)
        {
        	/*//set really high
        	userLocation.setAccuracy(999999);
            LocationManager lManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Looper looper = Looper.getMainLooper();
            Criteria criteria = new Criteria();
            
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            
            
            lManager.requestLocationUpdates(0, 0, criteria, lListener, looper);
			//update location until semi-accurate
            userLocation=lManager.getLastKnownLocation(lManager.NETWORK_PROVIDER);
           
            //test, may take too long
      //       	if(userLocation==null || userLocation.getAccuracy()>200)
        //    		userLocation=lManager.getLastKnownLocation(lManager.GPS_PROVIDER);
            
           
            Log.i("Accuracy being used:", ""+userLocation.getAccuracy());
            lManager.removeUpdates(lListener);
            	
            //  checking the database for a location that matches the user's location

            Log.i("longitude is ", " " +userLocation.getLongitude());
            Log.i("latitude is ", " " +userLocation.getLatitude());

            double longitude = userLocation.getLongitude();
            double latitude = userLocation.getLatitude();

            /*String[] projection = new String[]
                    {RingAssistProvider.COLUMN_PREFERENCE,
                    RingAssistProvider.COLUMN_SENDTEXT,
                    RingAssistProvider.COLUMN_MESSAGE};*/
            String selectionClause = RingAssistProvider.COLUMN_LONGITUDE + " > ? AND " +
                    RingAssistProvider.COLUMN_LONGITUDE + " < ? AND " +
                    RingAssistProvider.COLUMN_LATITUDE + " > ? AND " +
                    RingAssistProvider.COLUMN_LATITUDE + " < ? ";
            String[] selectionArgs = new String[]
                    {
                    (Double.toString(Globals.longitude - RADIUS)),
                    (Double.toString(Globals.longitude + RADIUS)),
                    (Double.toString(Globals.latitude - RADIUS)),
                    (Double.toString(Globals.latitude + RADIUS))
                    };

            /*String[] selectionArgs = new String[]
                    {
                    ((Double)(userLocation.getLongitude() - RADIUS)).toString(),
                    ((Double)(userLocation.getLongitude() + RADIUS)).toString(),
                    ((Double)(userLocation.getLatitude() - RADIUS)).toString(),
                    ((Double)(userLocation.getLatitude() + RADIUS)).toString()
                    };*/

            //projection instead of null before at some point
            cursor = context.getContentResolver().query(RingAssistProvider.CONTENT_URI, null,
                    selectionClause, selectionArgs, null);

            cursor.moveToNext();

            //Log.i("index zero is ", " " + cursor.getInt(0));
            //Log.i("index one is ", " " + cursor.getInt(1));
            //Log.i("index two is ", cursor.getString(2));

            Log.i("got past ", " first query");

            //  actually changing the ringer and sending a SMS(maybe)
            if (cursor != null && cursor.getCount() > 0)
            {
                Log.i("cursor is not", "NULL");

                Toast.makeText(context, "Within radius of "+cursor.getString(1), Toast.LENGTH_LONG).show();

                AudioManager aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                Log.i("got past audioManager", " ");
                int currentRingerMode = aManager.getRingerMode();   //gets the current ringer settings for later
                Log.i("got past currentRingerMode", " ");
                int userPreference = cursor.getInt(4);  //was 0
                Log.i("got past userPreference", " ");
                int sendingSMS = cursor.getInt(5);      //was 1
                Log.i("got past sendingSMS", " ");
                final String smsMessage = cursor.getString(6);  //was 2
                Log.i("got past smsMessage", " ");

                Log.i("got past bad indexes", " ");

                switch (userPreference)
                {
                    case (0):   //  silent
                        //  changes ringer volume to silent and not vibrat
                        aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    break;
                    case (1):   //  vibrate
                        //  changes ringer volume to silent and sets vibrate setting on
                        aManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    break;
                    case (2):   //  normal/default
                        //  does not change ringer volume or vibrate setting
                    //	aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                  //  for (int i = 1; i <=4; i++)
                    	aManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    
                        break;
                    case (3):   //  louder
                       
                    	aManager.setStreamVolume(2, aManager.getStreamMaxVolume(2), 0);
                    break;
                    case (4):   
                    	//DO NOTHING
                    	
                    	//  louder than 3 + vibrate
                        //  sets ringer volume louder twice and sets vibrate setting on
                    
                   // aManager.setRingerMode(AudioManager.VIBRATE_SETTING_ON);    //  depreciated
                    	
                    	//for (inter i = 1; i <=8; i++)
                    	//aManager.setStreamVolume(2, aManager.getStreamMaxVolume(2), 0);
                    	
                    break;
                }

                Log.i("got past switch", " ");

                if (sendingSMS == 1)
                {
                    Log.i("inside ", "sendingSMS");
                    if (intent.getAction() == "android.provider.Telephony.SMS_RECEIVED")
                    {
                    	Bundle smsBundle = intent.getExtras();
                    	if (smsBundle != null)
                    	{
                    		Object[] pdus = (Object[]) smsBundle.get("pdus");
                    		final SmsMessage[] messages = new SmsMessage[pdus.length];
                    		for (int i = 0; i < pdus.length; i++)
                    			messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    		String smsSenderNumber = messages[0].getOriginatingAddress();
                    		SmsManager sManager = SmsManager.getDefault();
                    		sManager.sendTextMessage(smsSenderNumber, null, smsMessage, null, null);
                    		sent=true;
                    		//test
                    		   NotificationManager nManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                               Notification ringerChangedNotification = new Notification(R.drawable.ic_launcher,
                                       "Ring Assist",
                                       System.currentTimeMillis());
                               Intent notificationIntent = new Intent(context, MainActivity.class);
                               PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                          
                            //****************************************//
                            //       GET CONTACT NAME BETA            //
                            //****************************************//
                               
                               ContentResolver cr = context.getContentResolver();
                               Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(smsSenderNumber));
                               Cursor cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
                               String contactName = null;
                               if (cursor != null) {
                            
                                   if(cursor.moveToFirst()) {
                                       contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
                                   }

                                   if(cursor != null && !cursor.isClosed()) {
                                       cursor.close();
                                   }
                               }
                               
                               if(contactName!=null)
                               {
                            	   ringerChangedNotification.setLatestEventInfo(context, "Ring Assist",
                                           "Sent "+contactName+" an automated message!", contentIntent);
                               }
                               else
                               {
                            	   ringerChangedNotification.setLatestEventInfo(context, "Ring Assist",
                                           "Sent "+smsSenderNumber+" an automated message!", contentIntent);
                               }
                               
                            //****************************************//
                            //            END GET CONTACT NAME        //
                            //****************************************//
                          
                            //   	ringerChangedNotification.setLatestEventInfo(context, "Ring Assist",
                            //           "Automated message sent!", contentIntent);
                               ringerChangedNotification.flags = Notification.FLAG_AUTO_CANCEL;
                               nManager.notify(RING_CHANGED_NOTIFICATION_ID, ringerChangedNotification);
                               //end-test
                               	
                    }

                    }
                    TelephonyManager tManager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
                    if (tManager.getCallState() == TelephonyManager.CALL_STATE_RINGING)
                    {
                    	//Log.i("stopped ringing", "call state is idle");
                 
                    //  getting phone number from incoming call
                        //  needs a delay because call log is not updated immediately when call is received
                        //  so we delay half a second (aka 500 milliseconds)
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable()
                        {
                            //@Override
                            public void run()
                            {
                                Log.i("inside", "run");

                                String[] pNumbProj = new String[] {CallLog.Calls.NUMBER};
                                Cursor pNumbCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                                        pNumbProj, null, null, CallLog.Calls.DATE + " desc");
                                //pNumbCursor.moveToFirst();
                                if (pNumbCursor != null)
                                {
                                    if (pNumbCursor.getCount() > 0)
                                    {
                                        pNumbCursor.moveToFirst();  //or moveToNext()

                                        String pNumber = pNumbCursor.getString(0);
                                        SmsManager sManager = SmsManager.getDefault();
                                        sManager.sendTextMessage(pNumber, null, smsMessage, null, null);
                                        sent=true;
                                    }
                                }

                            }
                        }, 500);
                        Toast.makeText(context, "Sent automated message.\n" + smsMessage,
                                Toast.LENGTH_LONG).show();
                      //test
             		   NotificationManager nManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                        Notification ringerChangedNotification = new Notification(R.drawable.ic_launcher,
                                "Ring Assist",
                                System.currentTimeMillis());
                        Intent notificationIntent = new Intent(context, MainActivity.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                     
                        	ringerChangedNotification.setLatestEventInfo(context, "Ring Assist",
                                "Automated message sent!", contentIntent);
                        ringerChangedNotification.flags = Notification.FLAG_AUTO_CANCEL;
                        nManager.notify(RING_CHANGED_NOTIFICATION_ID, ringerChangedNotification);
                        //end-test
                        
                    }
                    
                    /*a
                    
                    */
                }

                Log.i("got into ", "notifications");

                //  pushing a notification if the ringer/vibrate settings are changed
             /*   NotificationManager nManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification ringerChangedNotification = new Notification(R.drawable.ic_launcher,
                        "Ring Assist",
                        System.currentTimeMillis());
                Intent notificationIntent = new Intent(context, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                if(sent)
                	{
                	ringerChangedNotification.setLatestEventInfo(context, "Ring Assist",
                        "Automated message sent!", contentIntent);
                ringerChangedNotification.flags = Notification.FLAG_AUTO_CANCEL;
                nManager.notify(RING_CHANGED_NOTIFICATION_ID, ringerChangedNotification);
                	}*/
               /* else 
                {
                	ringerChangedNotification.setLatestEventInfo(context, "Ring Assist",
                            "The ringer settings have been altered.", contentIntent);
                    ringerChangedNotification.flags = Notification.FLAG_AUTO_CANCEL;
                }*/
                
            }
            else
            {
            	Toast.makeText(context, "Not within radius Lat:"+Globals.latitude+" Lon:"+Globals.longitude + " with Acc:"+Globals.accuracy, Toast.LENGTH_LONG).show();
            	
            }

            Log.i("got past", "everything");

            //  stop getting location updates so app doesn't eat the battery
            
        }
    }


}

