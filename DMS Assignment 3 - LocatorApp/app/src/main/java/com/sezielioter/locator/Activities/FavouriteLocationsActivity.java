package com.sezielioter.locator.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.sezielioter.locator.R;
import com.sezielioter.locator.Utilities.DBHelper;

import java.util.ArrayList;

public class FavouriteLocationsActivity extends AppCompatActivity {

    private TextView locationText;
    private ListView locationList;
    private SQLiteDatabase faveLocs;
    private LatLng destLocation;
    private DBHelper dbHelper;
    private ArrayList favesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_locations);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set title text for view
        locationText = (TextView)findViewById(R.id.favourites_text);
        locationText.setText(R.string.click_list_item);

/**  Get currently saved items from SQLite db    **/
        faveLocs = this.openOrCreateDatabase(DBHelper.DATABASE_NAME, MODE_PRIVATE, null);

        try {
            dbHelper = new DBHelper(this);
            favesList = dbHelper.getDestinationNames();

        }finally {
            dbHelper.close();
        }

/** Show currently saved destinations as a list   **/
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, favesList);
        adapter.setNotifyOnChange(true);
        locationList = (ListView)findViewById(R.id.location_list);
        locationList.setAdapter(adapter);

/************************************************************
         * SHOW SELECTED FAVOURITE IN MAP
**************************************************************/
            //anonymous onItemClickListener to select destination to show in maps
        locationList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get destination location
            try {
                dbHelper = new DBHelper(FavouriteLocationsActivity.this);
                String destName = (String)parent.getItemAtPosition(position);
                destLocation = dbHelper.getDestinationGeoRef(destName);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbHelper.close();
            }
            Intent intent = new Intent(FavouriteLocationsActivity.this, MapsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(InitialOptionsActivity.DESTINATION, destLocation);
            intent.putExtras(bundle);//if only one entry in bundle change to putExtra
            startActivity(intent);
            }
        });


/************************************************************
 * DELETE SELECTED FAVOURITE
 **************************************************************/
        locationList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    dbHelper = new DBHelper(FavouriteLocationsActivity.this);
                    String destName = (String)parent.getItemAtPosition(position);
                    dbHelper.deleteFavourite(destName);
                    adapter.remove(destName);
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    dbHelper.close();
                }
                return true;
            }
        });
    }

}
