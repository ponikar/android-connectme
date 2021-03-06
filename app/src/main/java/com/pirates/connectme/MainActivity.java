package com.pirates.connectme;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {


    // views

    private Switch toggleButton;
    private ListView devicesView;

    //bluetooth adapter
    private BluetoothAdapter adapter;

    //Bluetooth device list
    List<BluetoothDevice> deviceList;
    List<String> devicesName;

    ProgressDialog dailog;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dailog = getProgressDailog("Scanning...","Scanning");
        //initilize empty list
        deviceList = new ArrayList<>();
        devicesName = new ArrayList<>();

        askRequiedPermissionss(); //ask for  Permission


        //initilize views
        toggleButton = (Switch) findViewById(R.id.toggle_button);
        adapter = BluetoothAdapter.getDefaultAdapter();
        devicesView = (ListView) findViewById(R.id.list_devices);
        MaterialButton startScanBtn = findViewById(R.id.scan_btn);
        MaterialButton pairDevicesButton = findViewById(R.id.paired_btn);



        //set toogle button
        toggleButton.setChecked(adapter.isEnabled());

        //toogle button listner
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                enableBluetooth(); // enble blutooth on toogle btn clicked
            } else {
                adapter.disable();
            }
        });

        startScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter.isEnabled()) {

                    dailog.show();
                    startScanning();

                    Log.d("DISCOVERY", "" + adapter.startDiscovery());
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        devicesView.setOnItemClickListener((adapterView, view, i, l) -> {
            Toast.makeText(getApplicationContext(), "WORKING!", Toast.LENGTH_SHORT).show();
            ParcelUuid[] uuid = deviceList.get(i).getUuids();
            System.out.println(deviceList.get(i).fetchUuidsWithSdp());

            createBond(deviceList.get(i));
//                BluetoothSocket socket = deviceList.get(i).createRfcommSocketToServiceRecord(uuid[0].getUuid());

        });


        pairDevicesButton.setOnClickListener(view -> {
            if (adapter.isEnabled()) {
                showPairedDevices();
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean createBond(BluetoothDevice device) {
        Class class1 = null;
        boolean returnVal = false;
        try {
            ProgressDialog bar = getProgressDailog("Connecting", device.getName());
            bar.show();
            class1 = Class.forName("android.bluetooth.BluetoothDevice");
            Method createBondMethod = class1.getMethod("createBond");
            returnVal = (Boolean) createBondMethod.invoke(device);
            bar.dismiss();

            String message = returnVal ? "Device Connected" : "Something went wrong!";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return returnVal;


    }

    private void askRequiedPermissionss() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "PERMISSON GRANTED", Toast.LENGTH_SHORT);
        } else {
            askRequiedPermissionss();
        }
    }

    private void enableBluetooth() {

        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }

    }

    public void showPairedDevices() {
        clearDevices();
        Toast.makeText(getApplicationContext(), "SHOWING PAIRED DEVICES", Toast.LENGTH_LONG);
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        Toast.makeText(getApplicationContext(), "DEVICES" + devices.size(), Toast.LENGTH_LONG).show();
        for (BluetoothDevice device : devices) {
            Toast.makeText(getApplicationContext(), "FOUND SOMETHING", Toast.LENGTH_SHORT).show();
            deviceList.add(device);
            devicesName.add(device.getName());
        }
        updateList();
    }

    public void clearDevices() {
        devicesName.clear();
        deviceList.clear();
    }

    public void startScanning() {
        clearDevices();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getApplicationContext().registerReceiver(receiver, filter);

        Log.d("SCANNING", "Scanninng Started");
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("RECEIVER", "OnReceive");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                Log.d("RECEIVER", "ACTION_FOUND");
                Toast.makeText(getApplicationContext(), " Device Found", Toast.LENGTH_SHORT).show();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                devicesName.add(device.getName());
                deviceList.add(device);
                updateList();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                deviceList.clear();
                Log.d("RECEIVER", "ACTION_DISCOVERY_STARTED");
                Toast.makeText(getApplicationContext(), "Started Scanning", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                dailog.dismiss();
                Log.d("RECEIVER", "ACTION_DISCOVERY_FINISHED");
                Toast.makeText(getApplicationContext(), "Scanning Finished", Toast.LENGTH_SHORT).show();
            }
        }
    };


    public void updateList() {
        devicesView.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, devicesName));
    }

    public ProgressDialog getProgressDailog(String message, String title) {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle(title);
        dialog.setMessage(message);

        dialog.create();
        return dialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }


}