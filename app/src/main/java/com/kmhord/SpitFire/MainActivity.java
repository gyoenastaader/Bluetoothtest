package com.kmhord.SpitFire;

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

import com.androidplot.pie.PieChart;
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

    long start_time = 0;
    long elapse_time = 0;

    double rem_resist =0;
    double rem_time =0;
    double init_resist =0;

    double UVI;

    private XYPlot mySimpleXYPlot;
    private PieChart pie;

    Number[] storeddata= new Number[] {0,1,2,3,4,5,4,3,2,0};

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        final CalculateUVI calUVI = new CalculateUVI();
        final GraphData graphdata = new GraphData();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Grab UI ids
        Button openButton = (Button)findViewById(R.id.open);
        Button closeButton = (Button)findViewById(R.id.close);
        Button addButton = (Button)findViewById(R.id.add);

        myLabel = (TextView)findViewById(R.id.label);
        ExpTime = (TextView)findViewById(R.id.ExpTime);
        UpTime = (TextView)findViewById(R.id.Update);

        myTextbox = (EditText)findViewById(R.id.entry);

        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        pie = (PieChart) findViewById(R.id.mySimplePieChart);

        //Initiallize resistance variables
        rem_resist =calUVI.burnresistance();
        init_resist =rem_resist;
        start_time = CalculateUVI.starttime();

        // Starts graph
        graphdata.plotxygraph(storeddata, mySimpleXYPlot);
        graphdata.plotpiegraph(pie, init_resist,rem_resist);

        //Add Button
        addButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

                if(myTextbox.getText().toString().equals("")) {
                    UVI=calUVI.UVindex("480");
                    storeddata=CalculateUVI.updatehistory(UVI, storeddata);

                }
                else {
                    UVI=calUVI.UVindex(myTextbox.getText().toString());
                    storeddata=CalculateUVI.updatehistory(UVI, storeddata);
                }


                graphdata.plotxygraph(storeddata, mySimpleXYPlot);
                elapse_time =CalculateUVI.starttime()- start_time;
                rem_resist =CalculateUVI.remresist(UVI, rem_resist, elapse_time);
                rem_time = rem_resist / UVI;
                graphdata.plotpiegraph(pie, init_resist,rem_resist);
                start_time = CalculateUVI.starttime();

                //End update timer


                //Update labels
                ExpTime.setText("Max Exposure Time: " +  (int) rem_time + " min");
                UpTime.setText("Last Update: " + (int) elapse_time + " seconds ago");
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
                start_time = CalculateUVI.starttime();

                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    //start update timer
                    elapse_time =CalculateUVI.starttime()- start_time;
                    UpTime.setText("Last Update: " + elapse_time + " seconds ago");

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
                                            //Calculate the UV index, and pass it to store
                                            UVI=cUVI.UVindex(sdata);
                                            storeddata=CalculateUVI.updatehistory(UVI, storeddata);
                                            graphdata.plotxygraph(storeddata, mySimpleXYPlot);

                                            //Calculate remaining sunburn resistance
                                            rem_resist =CalculateUVI.remresist(UVI, rem_resist, elapse_time);
                                            rem_time = rem_resist /UVI;
                                            graphdata.plotpiegraph(pie, init_resist,rem_resist);

                                            //End update timer
                                            start_time = CalculateUVI.starttime();

                                            //Update labels
                                            myLabel.setText(sdata);
                                            ExpTime.setText("Max Exposure Time: " + rem_time + " min");

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