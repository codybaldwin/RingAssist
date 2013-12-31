package edu.fsu.cs.mobile.onDestroy.Ringer;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

//will change this from ListActivity to instead use a list view from delete.xml
public class DeleteActivity extends ListActivity
{
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;
    Cursor mCursor;

    
    //animates the activity changes when back button is pressed OR when the "up" button is pressed
    @Override
    public void onBackPressed()
    {
    	Intent myIntent = new Intent(DeleteActivity.this, MainActivity.class);
    	startActivityForResult(myIntent, 500);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	Intent myIntent = new Intent(DeleteActivity.this, MainActivity.class);
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
    	
        super.onCreate(savedInstanceState);
        
        ActionBar a = getActionBar();
    	a.setTitle("Remove Location");
    	a.setDisplayHomeAsUpEnabled(true); 
        //populates the listView with items to be deleted
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);

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
                    listItems.add(mCursor.getString(1)); //+ "\t\t)" + mCursor.getString(0));        //was one!!!!
                }
            }

            //finalizes the listView additions
            adapter.notifyDataSetChanged();
        }
    }

    //handles deletions when an item from the list is selected
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        //gets the item name that has been selected
        String item = (String) getListAdapter().getItem(position);
        item.trim();

        //should decide what column and which tuple to delete
        String mSelectionClause = RingAssistProvider.COLUMN_NAME +  " = ? ";
        String[] mSelectionArgs = {item};

        int mRowsDeleted = 0;

        //does the actual deletion of the tuple
        mRowsDeleted = getContentResolver().delete(
                RingAssistProvider.CONTENT_URI,
                mSelectionClause,
                mSelectionArgs
                );

        //toast just saying which item was deleted
        Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();

        //launches the main activity again
        Intent myIntent = new Intent(DeleteActivity.this, MainActivity.class);
        startActivityForResult(myIntent, 500);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}
