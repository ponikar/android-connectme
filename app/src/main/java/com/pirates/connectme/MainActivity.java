package com.pirates.connectme;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Switch toggleButton;
    private BluetoothAdapter adapter;
    private ListView devicesView;
    List<String> deviceList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d("GPS", isGpsEnabled + "");
        if (!isGpsEnabled) {
           // startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), R.layout.activity_main);
        }
        deviceList = new ArrayList<String>();
        toggleButton = (Switch) findViewById(R.id.toggle_button);
        adapter = BluetoothAdapter.getDefaultAdapter();
        devicesView = (ListView) findViewById(R.id.list_devices);
        // initially setting button



         toggleButton.setChecked(adapter.isEnabled());
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    adapter.enable();
                    showPairedDevices();
                    startScanning();
                    Log.d("DISCOVERY", "" + adapter.startDiscovery());

                } else {
                    adapter.disable();
                }
            }
        });

    }

    public void showPairedDevices() {
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        Toast.makeText(getApplicationContext(),  "DEVICES" + devices.size(), Toast.LENGTH_LONG).show();
        for(BluetoothDevice device: devices) {
            Toast.makeText(getApplicationContext(), "FOUND SOMETHING", Toast.LENGTH_SHORT).show();
            deviceList.add(device.getName());
        }
        updateList();
    }

    public void startScanning() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        Toast.makeText(getApplicationContext(), "SCANNING", Toast.LENGTH_SHORT).show();
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "RECIVER ADDED", Toast.LENGTH_SHORT).show();
            Log.d("RECEIVER", "WORKING");
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                deviceList.add(device.getName());
                deviceList.add("WORKING");
                updateList();
            }
        }
    };

    public void updateList() {
        devicesView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item , deviceList));
    }


}