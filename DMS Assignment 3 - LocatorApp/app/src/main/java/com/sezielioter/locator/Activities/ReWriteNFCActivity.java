package com.sezielioter.locator.Activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;

import com.sezielioter.locator.R;

import java.io.UnsupportedEncodingException;

public class ReWriteNFCActivity extends AppCompatActivity {
    private NfcAdapter myAdapter;
    private Button overwriteB;
    private PendingIntent pendingIntent;
    private IntentFilter intentFilter;
    private TextView latView, longView, destView, currentLocView;
    private IntentFilter[] intentFiltersArray;
    private String[][] lists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_write_nfc);

        latView = (TextView) findViewById(R.id.latView);
        longView = (TextView) findViewById(R.id.longView);
        destView = (TextView) findViewById(R.id.destView);
        currentLocView = (TextView) findViewById(R.id.currentLocView);


        overwriteB = (Button) findViewById(R.id.overwriteButton);
        overwriteB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ReWriteNFCActivity.this, UpdateTagActivity.class);
                double lat = Double.parseDouble(latView.getText().toString());
                double longV = Double.parseDouble(longView.getText().toString());
                String destination = destView.getText().toString();
                String currentLoc = currentLocView.getText().toString();
                Bundle bundle = new Bundle();

                bundle.putBoolean("tagdataPresent", false);
                bundle.putDouble("latitude", lat);
                bundle.putDouble("longitude", longV);
                bundle.putString("destination", destination);
                bundle.putString("Current Location", currentLoc);

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });



        myAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        intentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        intentFilter.addCategory("*/*");

        intentFiltersArray = new IntentFilter[] {intentFilter};
        lists = new String[][] {new String[] {NfcA.class.getName()}, new String[] {NfcF.class.getName()}, new String[] {NfcB.class.getName()}, new String[] {NfcV.class.getName()}};
    }


    @Override
    protected void onPause() {
        super.onPause();
        myAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, lists);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println(intent.getAction());
    }


    private boolean readNFCTag(Intent i) {
        Parcelable[] parceArray = i.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (parceArray != null) {
            NdefMessage ndefMsg = (NdefMessage) parceArray[0];
            NdefRecord ndefRecord = ndefMsg.getRecords()[0];
            if (ndefRecord != null) {
                return true;
            }
            return false;
        }
        return false;
    }
}
