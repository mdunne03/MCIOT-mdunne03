package com.mdunne03.scanwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WifiScannerActivity extends AppCompatActivity implements Serializable
{
    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private Button buttonGPS;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private static final String TAG ="";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonScan = (Button) findViewById(R.id.scanBtn);
        buttonScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                scanWifi();
            }
        });
/*
        // Read from the database...we do not use this here but will be useful later.
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value Is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }

        });*/

        listView = (ListView) findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled())
        {
            Toast.makeText(this, "WiFi is disabled ... Please enable", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        scanWifi();

        buttonGPS = (Button) findViewById(R.id.locationBtn);
        buttonGPS.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(WifiScannerActivity.this, LocationActivity.class);
                Bundle bundle = new Bundle();
                //putSerializable() necessary to pass arraylist as an intent tp LocationActivity.java
                bundle.putSerializable("arraylist", arrayList);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void scanWifi()
    {
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }
    //Broadcast receiver class scans area for available wi-fi networks.
    BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            results = wifiManager.getScanResults();
            // after scan is complete, unregister to save resources.
            unregisterReceiver(this);

            //iterate through list of results and call values we are interested in
            for (ScanResult scanResult : results)
            {
                arrayList.add("\n" + "Network: " + scanResult.SSID + "\n" + "\n" + "MAC address: "
                        + scanResult.BSSID + "\n" + "Signal level: " + scanResult.level + " dBm" + "\n"
                        + "Frequency: " + scanResult.frequency + " MHz" + "\n");
                adapter.notifyDataSetChanged();
            }
        };
    };
}
