package com.sezielioter.locator.Activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sezielioter.locator.R;
import com.sezielioter.locator.Utilities.TagData;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class UpdateTagActivity extends AppCompatActivity {

    private double latitude, longitiude;
    private String currentLoc, destLoc;
    private Button writeButton;
    private boolean written;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilter;
    private String[][] list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_tag);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addCategory("*/*");
        intentFilter = new IntentFilter[] {ndef};
        list = new String[][] {new String[] {NfcA.class.getName()}, new String[] {NfcF.class.getName()}, new String[] {NfcB.class.getName()}, new String[] {NfcV.class.getName()}};
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        written = false;
        writeButton = (Button) findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                written = true;
//                displayControl(true);
            }
        });

        Bundle bundle = getIntent().getExtras();
//        boolean tagDataPresent = bundle.getBoolean("tagdataPresent");
//        if(tagDataPresent) {
//            latitude = bundle.getDouble("latitude");
//            longitiude = bundle.getDouble("longitude");
//            destLoc = bundle.getString("destination");
//            currentLoc = bundle.getString("Current Location");
//        } else {
//            latitude = bundle.getDouble("latitude");
//            longitiude = bundle.getDouble("longitude");
//            destLoc = bundle.getString("destination");
//            currentLoc = bundle.getString("Current Location");
//        }
        latitude = bundle.getDouble("latitude");
        longitiude = bundle.getDouble("longitude");
        destLoc = bundle.getString("destination");
        currentLoc = bundle.getString("Current Location");

    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, list);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        if (text == null) {
//            Toast.makeText(getApplicationContext(), "???!", Toast.LENGTH_SHORT).show();
//
//            return;
//        }
        if (written == true) {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                Ndef ndef = Ndef.get(tag);
                try {
                    ndef.connect();
                    String geoUri = "geo:" + latitude + "," + longitiude;

                    // Creating NDEF Records for the 4 records stored on the NFC tag
                    NdefRecord geoRecord = NdefRecord.createUri(geoUri);
                    NdefRecord record1 = createPlanTextNdefRecord(currentLoc);
                    NdefRecord record2 = createPlanTextNdefRecord(destLoc);
                    String packageName = "com.sezielioter.locator";
                    NdefRecord appRecord = NdefRecord.createApplicationRecord(packageName);

                    //Store NdefRecords inside array to put inside NFC Tag
                    NdefRecord[] records = {geoRecord, record1, record2, appRecord};
                    NdefMessage ndefMessage = new NdefMessage(records);
                    ndef.writeNdefMessage(ndefMessage);
                    Toast.makeText(getApplicationContext(), "Successful Write", Toast.LENGTH_SHORT).show();
                    written = false;
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error onNewIntent", Toast.LENGTH_SHORT).show();
                } catch (FormatException e) {

                }
            }
        }
    }

    private NdefRecord createPlanTextNdefRecord(String text) throws UnsupportedEncodingException {
        if(text == null) {
            text = "ErrorNull";
            Toast.makeText(this, "cPTNdefRecERROR", Toast.LENGTH_SHORT);
        }
        if(text == "") {
            text = "ErrorEmpty";
            Toast.makeText(this, "cPTNdefRecERROR", Toast.LENGTH_SHORT);
        }
        String language = "en";
        byte[] textBytes = text.getBytes();
        byte[] languageBytes = language.getBytes("US-ASCII");
        int languageLength = languageBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[(1 + languageLength + textLength)];

        payload[0] = (byte) languageLength;

        System.arraycopy(languageBytes, 0, payload, 1, languageLength);
        System.arraycopy(textBytes, 0, payload, (1 + languageLength), textLength);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }
}
