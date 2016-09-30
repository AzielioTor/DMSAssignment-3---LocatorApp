package com.sezielioter.locator.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.sezielioter.locator.R;
import com.sezielioter.locator.Utilities.DBHelper;
import com.sezielioter.locator.Utilities.TagData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class InitialOptionsActivity extends AppCompatActivity {

    private static final String TAG = "InitOptionsActivity";//LogCat message TAG
    private static final String URL_CONTEXT_ROOT = "http://uni-sezmeralda.pagekite.me/Server-war/TagDataServlet";
    private static final String URL_GET_PREFIX = "?tagID=";
    protected static final String TAG_LOCATION="tag_location", DESTINATION="destination";
    private static final int PROG_MAX=3;
    private GoogleApiClient client;
    private TextView text;
    private Button saveButton;
    private Button mapsButton;
    private Button nfcWriteButton;
    private Button updateButton;
    private ProgressBar progressBar;
    private double currentLat, currentLong, destLat, destLong;
    private LatLng tagLocation, destLocation; // tagLocation saved in NFC tag and destination is accessed from database
    private String sourceText, destText;
    private TagData tagData;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_options);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setMax(PROG_MAX);
        text = (TextView)findViewById(R.id.locations_text);

/****************************************************************************
 *          SAVE TO FAVOURITES
 *****************************************************************************/
        saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setEnabled(false);

        //anonymous onClickListener for saving destination to list of favourite locations and seeing the list
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(InitialOptionsActivity.this, FavouriteLocationsActivity.class);

            dbHelper = new DBHelper(InitialOptionsActivity.this);
            dbHelper.insertFavourite(destText, destLat, destLong);
            startActivity(intent);
            }
        });


/****************************************************************************
 *          OPEN DESTINATION IN MAPS
 *****************************************************************************/
        mapsButton = (Button)findViewById(R.id.maps_button);
        mapsButton.setEnabled(false);
                    //anonymous onClickListener for seeing locations on map
        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialOptionsActivity.this, MapsActivity.class);
                Bundle bundle = new Bundle();
                    bundle.putParcelable(TAG_LOCATION, tagLocation);
                    bundle.putParcelable(DESTINATION, destLocation);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        updateButton = (Button) findViewById(R.id.updateNFCButton);
        updateButton.setVisibility(View.VISIBLE);
        updateButton.setEnabled(false);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialOptionsActivity.this, UpdateTagActivity.class);
                Bundle bundle = new Bundle();

                bundle.putBoolean("tagdataPresent", true);
                //bundle.putSerializable("tagData", tagData);
                if(tagData == null) {
                    Toast.makeText(getApplicationContext(), "TagDataNull", Toast.LENGTH_LONG).show();
                }

                bundle.putDouble("latitude", destLat);
                bundle.putDouble("longitude", destLong);
                bundle.putString("destination", destText);
                bundle.putString("Current Location", "NFC Tag");

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        nfcWriteButton = (Button) findViewById(R.id.writeToNfcButton);
        nfcWriteButton.setEnabled(false);
        nfcWriteButton.setVisibility(View.VISIBLE);
        nfcWriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialOptionsActivity.this, ReWriteNFCActivity.class);
                startActivity(intent);
            }
        });

        //Prep Server access
        tagData = null;
        CookieManager cookieMonster = new CookieManager();
        CookieHandler.setDefault(cookieMonster);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    /** gets the intent from the NFC tag **/
    public void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    /** parses the NDEF Message records from the intent and prints Strings to the TextView **/
    void processIntent(Intent intent) {
        NdefMessage msg1 = null;
        NdefRecord geoLatLng;
        NdefRecord sourceLoc;
        NdefRecord destLoc;

        //get tag id
        Tag rawTagID = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String tagID = bin2hex(rawTagID.getId());
        Log.d(TAG, "Tag ID = " + tagID);

        //get Ndef message from intent
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) msg1 = (NdefMessage)rawMsgs[0];

        geoLatLng = msg1.getRecords()[0];
        sourceLoc = msg1.getRecords()[1];

        //create LatLng from Ndef geo record
        String geo = new String(geoLatLng.getPayload());
        currentLat = Double.parseDouble(geo.substring(geo.indexOf(':') + 1, geo.indexOf(',')));
        currentLong = Double.parseDouble(geo.substring(geo.indexOf(',') + 1));
        tagLocation = new LatLng(currentLat, currentLong);

        //format text from source and destination location text records
        byte[] payload1 = sourceLoc.getPayload();
        int languageCodeLength = payload1[0] & 0077;
        sourceText = new String(payload1, languageCodeLength + 1, payload1.length - languageCodeLength - 1);

        /** gather destination data from server **/
        getDataFromServer(tagID);

    }

    /** converts the binary byte data to hex String **/
    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    /**
     * Prepares a network connection for an HTTP connection. This method is used to set-up for
     * accessing the server database
     * @param tagID The unique identifier of the NFC tag which was scanned. This value is used by the
     *              database to ascertain the destination details.
     */
    private void getDataFromServer(String tagID){
        Toast toast;
        // check connection status
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // if connected, start asyncTask to get the tag data from the DB
        // else open user wifi settings activity
        if(networkInfo != null && networkInfo.isConnected()){
            URL url = null;
            try {
                if (tagID != null && tagID.length() > 0)
                    url = new URL(URL_CONTEXT_ROOT + URL_GET_PREFIX + tagID);
                else {throw new Exception("tagID was null or empty");}

                // run DB access in another thread
                AsyncTask<URL, Integer, TagData> backgroundTask = new HttpCommunicator();
                backgroundTask.execute(url);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
                toast = Toast.makeText(this, "We're Sorry! \nThere was as unexpected error", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else{
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }


    /**
     * ASYNC TASK - Makes http connection with server for the purpose of accessing destination
     * data from the database. Results are processed by onPostExecute to save data to a TagData object
     * and pass to the Maps or FavouriteLocations Activities.
     */
    private class HttpCommunicator extends AsyncTask<URL, Integer, TagData> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(1);
        }

        @Override
        protected TagData doInBackground(URL... urls) {

            if (urls == null || urls.length == 0) {
                return null;
            }
            Integer progress = 1;
            URL url = urls[0];
            HttpURLConnection connection = null;
            BufferedReader bufReader = null;
            StringBuilder stringBuilder = new StringBuilder();
            String xmlData = "";

            /**************************************************************************
                      EXTRACT STRING FROM RESPONSE
            **************************************************************************/
            try {
                connection = (HttpURLConnection)url.openConnection();
                publishProgress(++progress); //2

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                publishProgress(++progress); // 3

                bufReader = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));

                try {
                    String line = bufReader.readLine();

                    while (line != null) {
                        stringBuilder.append(line);
                        line = bufReader.readLine();
                    }
                }
                finally {
                    bufReader.close();
                }

            }
            catch (IOException e) {
                System.out.println("IOException: " + e.getLocalizedMessage());
            }
            finally{
                if (connection != null)
                    connection.disconnect();
            }

            /**************************************************************************
                      PARSE XML INTO TAGDATA
            **************************************************************************/
            xmlData =  stringBuilder.toString();
            XmlPullParserFactory ppFactory;
            TagData results = new TagData();

            try{
                ppFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = ppFactory.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(xmlData));

                int eventType = parser.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT){
                    String name = null;

                    // save each tag type to equivalent in tagData
                    if(eventType == XmlPullParser.START_TAG){
                        name = parser.getName();

                        if(name.equalsIgnoreCase("tagID")){
                            results.setTagID(parser.nextText());
                        }
                        if(name.equalsIgnoreCase("latitude")){
                            results.setDestinationLatitude(Double.parseDouble(parser.nextText()));
                        }
                        if(name.equalsIgnoreCase("longitude")){
                            results.setDestinationLongitude(Double.parseDouble(parser.nextText()));
                        }
                        if(name.equalsIgnoreCase("count")){
                            results.setCount(Integer.parseInt(parser.nextText()));
                        }
                        if(name.equalsIgnoreCase("destination")){
                            results.setDestinationName(parser.nextText());
                        }
                        if(name.equalsIgnoreCase("location")){
                            results.setTagLocation(parser.nextText());
                        }
                    }
                    eventType = parser.next();
                }
            }catch(XmlPullParserException e){
                System.out.println("PullParser exception: "+ e.getMessage());

            }catch(IOException e){
                System.out.println("IO Exception:" + e.getMessage());
            }
            return results;
        }

        @Override
        protected void onProgressUpdate(Integer... progress){
            text.setText("Retrieving Destination");
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(TagData results) {
            tagData = results;
            destLat = tagData.getDestinationLatitude();
            destLong = tagData.getDestinationLongitude();
            destLocation = new LatLng(destLat, destLong);
            destText = tagData.getDestinationName();
            Log.d(TAG, "destination name = " + destText);
            text.setText("You are at: " + sourceText + "\n and would like to go to:\n" + destText);
            mapsButton.setEnabled(true);
            saveButton.setEnabled(true);
            updateButton.setEnabled(true);
            nfcWriteButton.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        // If the user is currently looking at the first step, allow the system to handle the
        // Back button. This calls finish() on this activity and pops the back stack.
        super.onBackPressed();
        this.finishAffinity();
    }
}
