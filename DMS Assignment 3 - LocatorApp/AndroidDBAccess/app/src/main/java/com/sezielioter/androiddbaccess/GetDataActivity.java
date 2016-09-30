package com.sezielioter.androiddbaccess;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetDataActivity extends ListActivity implements View.OnClickListener {

    private Button initButton;
    private static final String URL_MAIN = "http://uni-sezmeralda.pagekite.me/Server-war";
    private TagData tagData;

    /**
     * TERRY the following three lines can be deleted
     *      they are for dev/testing
     */
    private List<String> serverResponseList;
    private ArrayAdapter<String> listAdapter;
    private static final String SERIAL = "04C7C3E2833480";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tagData = null;
        setContentView(R.layout.activity_get_data);

        initButton = (Button) findViewById(R.id.initButton);
        initButton.setOnClickListener(this);

        /**
         * TERRY the following three lines can be deleted
         *      they are for dev/testing
         *
         */
        serverResponseList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, serverResponseList);
        setListAdapter(listAdapter);

        CookieManager cookieMonster = new CookieManager();
        CookieHandler.setDefault(cookieMonster);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onClick(View view) {
        Toast toast;

        if (view == initButton) {
            // check connection status
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            // if connected, start asyncTask to get the tag data from the DB
                // else open user wifi settings activity
            if (networkInfo != null && networkInfo.isConnected()) {

                URL url = null;

                // appends the query to the http URL
                /**
                 *
                 * TERRY - change SERIAL to the tag id as
                 *              gathered by your NFC activity
                 *
                 *
                 */
                try {
                    if (SERIAL != null && SERIAL.length() > 0)
                        url = new URL(URL_MAIN + "/TagDataServlet?tagID=" + SERIAL);
                    else url = new URL(URL_MAIN);

                    // run DB access in another thread
                    AsyncTask<URL, Void, TagData> backgroundTask = new HttpCommunicator();
                    backgroundTask.execute(url);

                    /**
                     * TERRY - this can be deleted,
                     *          it's for dev/testing
                     *
                     */
                    if(tagData != null){
                        serverResponseList.add(tagData.toString());
                        listAdapter.notifyDataSetChanged();
                    }


                } catch (MalformedURLException e) {
                    toast = Toast.makeText(getApplicationContext(), "Shit! " + e.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                }
            } else {
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GetData Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.sezielioter.androiddbaccess/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GetData Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.sezielioter.androiddbaccess/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class HttpCommunicator extends AsyncTask<URL, Void, TagData> {

        @Override
        protected TagData doInBackground(URL... urls) {
            HttpURLConnection connection = null;
            if (urls == null || urls.length == 0) return null;
            URL url = urls[0];
            StringBuilder stringBuilder = new StringBuilder();
            String xmlData = "";

    // **************************************************************************
    //          EXTRACT STRING FROM RESPONSE
    // **************************************************************************
            try {
                connection = (HttpURLConnection)url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();

                BufferedReader bufReader = new BufferedReader(
                                            new InputStreamReader(
                                              connection.getInputStream()));

                String line = bufReader.readLine();
                while(line != null){
                    stringBuilder.append(line);
                    line = bufReader.readLine();
                }
                bufReader.close();

            }
            catch (MalformedURLException e) {
                System.out.println("malformedURLException: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IOException: " + e.getLocalizedMessage());
            }finally
            {
                if (connection != null)
                    connection.disconnect();
            }


    // **************************************************************************
    //          PARSE XML INTO TAGDATA
    // **************************************************************************
            xmlData =  stringBuilder.toString();
            XmlPullParserFactory ppFactory;
            TagData results = new TagData();

            try {
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

        protected void onPostExecute(TagData results) {
            tagData = results;
        }
    }
}
