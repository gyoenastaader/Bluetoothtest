package com.kmhord.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import com.androidplot.xy.XYPlot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity
{
    TextView myLabel;
    TextView ExpTime;
    TextView UpTime;

    EditText myTextbox;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    OutputStream mmOutputStream;
    InputStream mmInputStream;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    long startime = 0;
    long elapsetime = 0;

    Number UVI;

    private XYPlot mySimpleXYPlot;
    Number[] storeddata= new Number[] {0,1,2,3,4,5,4,3,2,0};

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        final CalculateUVI cUVI = new CalculateUVI();
        final GraphData graphdata = new GraphData();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openButton = (Button)findViewById(R.id.open);
        Button closeButton = (Button)findViewById(R.id.close);
        Button addButton = (Button)findViewById(R.id.add);

        myLabel = (TextView)findViewById(R.id.label);
        ExpTime = (TextView)findViewById(R.id.ExpTime);
        UpTime = (TextView)findViewById(R.id.Update);

        myTextbox = (EditText)findViewById(R.id.entry);

        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);

        graphdata.plotxygraph(storeddata, mySimpleXYPlot); // Starts graph

        //Add Button
        addButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if(myTextbox.getText().toString().equals("")) {
                    storeddata=CalculateUVI.updatehistory(cUVI.UVindex("320"), storeddata);

                }
                else {
                    storeddata=CalculateUVI.updatehistory(cUVI.UVindex(myTextbox.getText().toString()), storeddata);
                }
                graphdata.plotxygraph(storeddata, mySimpleXYPlot);
            }
        });

        //Open Button
        openButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    findBT();
                    openBT();
                }
                catch (IOException ex) { }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });
    }

    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("NANNERS"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        myLabel.setText("Bluetooth Opened");
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final CalculateUVI cUVI = new CalculateUVI();
        final GraphData graphdata = new GraphData();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                startime= CalculateUVI.starttime();

                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    //start update timer
                    elapsetime=startime - CalculateUVI.starttime();
                    UpTime.setText("Last Update: " + elapsetime + " seconds ago");

                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String sdata = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            //Calculate the UV index, and immediate pass it to store
                                            UVI=cUVI.UVindex(sdata);
                                            storeddata=CalculateUVI.updatehistory(UVI, storeddata);
                                            graphdata.plotxygraph(storeddata, mySimpleXYPlot);

                                            //End update timer
                                            startime= CalculateUVI.starttime();

                                            myLabel.setText(sdata);
                                            ExpTime.setText("Max Exposure Time: " + CalculateUVI.Burntime(UVI) + " min");

                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }

}