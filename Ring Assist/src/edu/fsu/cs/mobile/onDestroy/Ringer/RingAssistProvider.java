package edu.fsu.cs.mobile.onDestroy.Ringer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

//class used to store the user desired locations and other relevant data
public class RingAssistProvider extends ContentProvider
{

    MainDatabaseHelper mOpenHelper;     //used to manipulate SQLite database

    public final static String DBNAME = "RingAssistDatabase";       //name of the database

    //the primary key ID identifier for a tuple
    public final static String COLUMN_ID = "_ID";
    //the nickname the user gives for a desired location
    public final static String COLUMN_NAME = "name";
    //longitude of a user desired location
    public final static String COLUMN_LONGITUDE = "longitude";
    //latitude of a user desired location
    public final static String COLUMN_LATITUDE = "latitude";
    //ring type: i.e. 0=silent, 1=normal, 2=loud, 3=loudest, 4=default
    public final static String COLUMN_PREFERENCE = "preference";
    //integer representing whether user desires text notifications (for premium), 0 = no, 1 = yes
    public final static String COLUMN_SENDTEXT = "sendtext";
    //text representing the user's desired message (for premium)
    public final static String COLUMN_MESSAGE = "message";

    //following statement creates the UserInformation database relation table
    private static final String SQL_CREATE_MAIN =
            "CREATE TABLE UserInformation ( " +
                    " COLUMN_ID INTEGER PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_LONGITUDE + " REAL DEFAULT \'4.2\', " +
                    COLUMN_LATITUDE + " REAL DEFAULT \'3.6\', " +
                    COLUMN_PREFERENCE + " INTEGER DEFAULT \'4\', " +
                    COLUMN_SENDTEXT + " INTEGER DEFAULT \'0\', " +
                    COLUMN_MESSAGE + " TEXT DEFAULT \'Not Applicable\') ";

    //should take care of most of the URI details for us
    public static final Uri CONTENT_URI =
            Uri.parse("content://edu.fsu.cs.mobile.onDestroy.Ringer.provider");

    //deletes a tuple from the UserInformation database
    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs)
    {
        return mOpenHelper.getWritableDatabase().
                delete("UserInformation", whereClause, whereArgs);
    }

    //auto generated stub
    @Override
    public String getType(Uri uri)
    {
        // TODO Auto-generated method stub
        return null;
    }

    //inserts user information into UserInformation database
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        //will un-comment later when adding all values, right now using defaults
        values.getAsString(COLUMN_NAME).trim();
        values.getAsFloat(COLUMN_LONGITUDE);
        values.getAsFloat(COLUMN_LATITUDE);
        values.getAsInteger(COLUMN_PREFERENCE);
        values.getAsInteger(COLUMN_SENDTEXT);
        values.getAsString(COLUMN_MESSAGE).trim();

        long id = mOpenHelper.getWritableDatabase()
                .insert("UserInformation", null, values);

        String dog = values.toString();     //used for error checking only
        Log.i("insert is",dog);             //allows a visual of what has been inserted

        return Uri.withAppendedPath(CONTENT_URI, "" + id);
    }

    //creates the UserInformation table
    @Override
    public boolean onCreate()
    {
        mOpenHelper = new MainDatabaseHelper(getContext());

        return true;
    }

    //allows for queries within the UserInformation database
    @Override
    public Cursor query(Uri table, String[] columns, String selection, String[] args,
            String orderBy)
    {
        return mOpenHelper.getReadableDatabase()
                .query("UserInformation", columns, selection, args, null, null, orderBy);
    }

    //allows the update of items in the database
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        String cat = values.toString();     //used for error checking only
        Log.i("update is",cat);             //allows a visual of what has been inserted

        return mOpenHelper.getWritableDatabase().
                update("UserInformation", values, selection, selectionArgs);
    }

    //allows us to utilize SQLite features
    protected static final class MainDatabaseHelper extends SQLiteOpenHelper
    {
        MainDatabaseHelper(Context context)
        {
            super(context, "DBNAME", null, 1);      //not sure if need quotes here on DBNAME
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(SQL_CREATE_MAIN);
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2)
        {

        }
    }

}
