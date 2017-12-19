package com.pk.csi;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.data.CartesianShort;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Bmi160Gyro;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ServiceConnection{

    private Socket socket = null;
    private String ip = null;

    private Handler handler;
    private SoundMeter mSensor;
    Runnable r;

    private MetaWearBoard mwBoard;

    Bmi160Gyro gyroModule;
    Accelerometer accelModule;

    LinearLayout levelLayout;

    List<TimeStampedCartesianShort> preAccelData;
    List<TimeStampedCartesianShort> postAccelData;

    List<TimeStampedCartesianFloat> preGyroData;
    List<TimeStampedCartesianFloat> postGyroData;

    private boolean listenSound = false;

    int accPostCount = 0;
    int gyroPostCount = 0;

    private boolean accDataReady = false;
    private boolean gyroDataReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ip = getIntent().getStringExtra("IP");

        levelLayout = (LinearLayout) findViewById(R.id.levelLayout);

        preAccelData = new ArrayList<>();
        postAccelData = new ArrayList<>();

        preGyroData = new ArrayList<>();
        postGyroData = new ArrayList<>();

        connectToServer();
        initRecording();
        startRecording();

        initSensor();
    }

    private void connectToServer() {
        Thread t = new Thread() {
            public void run() {
                InetAddress serverAddr = null;
                try {
                    serverAddr = InetAddress.getByName(ip);
                    socket = new Socket(serverAddr, Constants.PORT_NUMBER);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Sorry could not connect to the server", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Sorry could not connect to the server", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                }
            }
        };
        t.start();
    }

    private void initRecording() {
        handler = new Handler();

        r = new Runnable() {

            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double volumeLevel = mSensor.getTheAmplitude();
                        double weightLevel = volumeLevel;
                        if (volumeLevel > Constants.SOUND_LEVEL_THRESHOLD) {
                            weightLevel =  Constants.SOUND_LEVEL_THRESHOLD;
                        }

                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) levelLayout.getLayoutParams();
                        lp.weight = (float) weightLevel;

                        levelLayout.setLayoutParams(lp);

                        if (volumeLevel > Constants.SOUND_LEVEL_THRESHOLD) {
                            listenSound = true;
                            Log.d("Log", volumeLevel + "");
//                            DataOutputStream dout = null;
//                            try {
//                                dout = new DataOutputStream(socket.getOutputStream());
//                                dout.writeDouble(volumeLevel);
//                            }catch (Exception e) {
//
//                            }
                        }
                        handler.postDelayed(this, 100);
                    }
                });
            }
        };
    }

    private void startRecording() {
        if (mSensor == null) {
            mSensor = new SoundMeter();
        }
        mSensor.start();
        handler.postDelayed(r, 100);
    }

    private void initSensor() {
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);
    }

    private void startGyro() {
        try {
            gyroModule = mwBoard.getModule(Bmi160Gyro.class);
                gyroModule.routeData().fromAxes().stream("gyroAxisSub")
                        .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe("gyroAxisSub", new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message msg) {
                                final CartesianFloat spinData = msg.getData(CartesianFloat.class);
                                insertGyro(spinData);
                                Log.d(Constants.TAG + "gyro : ", spinData.toString());
                            }
                        });
                    }
                });
            gyroModule.start();

        }
         catch (UnsupportedModuleException e) {
            Log.d(Constants.TAG, e.getLocalizedMessage());
        }
    }

    private void startAcceleroMeter() {
        try {
            accelModule = mwBoard.getModule(Accelerometer.class);
                accelModule.routeData().fromAxes().stream("accelSub").commit()
                        .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                            @Override
                            public void success(RouteManager result) {
                                result.subscribe("accelSub", new RouteManager.MessageHandler() {
                                    @Override
                                    public void process(Message msg) {
                                        final CartesianShort axisData = msg.getData(CartesianShort.class);
                                        insertAcc(axisData);
                                        Log.d(Constants.TAG + "Acc : ", axisData.toString());

                                    }
                                });
                                accelModule.setOutputDataRate(50.f);
                                accelModule.enableAxisSampling();
                                accelModule.start();
                            }

                            @Override
                            public void failure(Throwable error) {
                                Log.d(Constants.TAG, "Error committing route", error);
                            }
                        });
                accelModule.start();
        } catch (UnsupportedModuleException e) {
            Log.d(Constants.TAG, e.getLocalizedMessage());
        }
    }

    private void insertAcc(CartesianShort record) {
        if (!listenSound) {
            // Insert in to Pre
            preAccelData.add(new TimeStampedCartesianShort(record));

            if (preAccelData.size() > Constants.RECORD_COUNT_THRESHOLD) {
                preAccelData.remove(0);
            }
        } else {
            // Insert into post
            accPostCount++;
            postAccelData.add(new TimeStampedCartesianShort(record));
            if (accPostCount == Constants.RECORD_COUNT_THRESHOLD) {
                accDataReady = true;
                if (gyroDataReady) {
                    sendData();
                }
            }
        }
    }

    private void insertGyro(CartesianFloat record) {
        if (!listenSound) {
            // Insert in to Pre

            preGyroData.add(new TimeStampedCartesianFloat(record));
            if (preGyroData.size() > Constants.RECORD_COUNT_THRESHOLD) {
              preGyroData.remove(0);
            }
        } else {
            // Insert into post
            gyroPostCount++;
            postGyroData.add(new TimeStampedCartesianFloat(record));
            if (gyroPostCount == Constants.RECORD_COUNT_THRESHOLD) {
                gyroDataReady = true;
                if (accDataReady) {
                    sendData();
                }
            }
        }
    }

    synchronized private void sendData() {
        Collections.sort(preAccelData);
        Collections.sort(postAccelData);
        Collections.sort(preGyroData);
        Collections.sort(postGyroData);

        // So we have all Lists here
        DataOutputStream dout = null;
        try {
            dout = new DataOutputStream(socket.getOutputStream());
            dout.writeInt(preAccelData.size());

            for (TimeStampedCartesianShort cs : preAccelData) {
                dout.writeFloat(cs.get_cartesianShort().x());
                dout.writeFloat(cs.get_cartesianShort().y());
                dout.writeFloat(cs.get_cartesianShort().z());
                dout.writeLong(cs.get_timeStamp());
            }

            dout.writeInt(postAccelData.size());

            for (TimeStampedCartesianShort cs : postAccelData) {
                dout.writeFloat(cs.get_cartesianShort().x());
                dout.writeFloat(cs.get_cartesianShort().y());
                dout.writeFloat(cs.get_cartesianShort().z());
                dout.writeLong(cs.get_timeStamp());
            }

            dout.writeInt(preGyroData.size());

            for (TimeStampedCartesianFloat cf : preGyroData) {
                dout.writeFloat(cf.get_cartesianFloat().x());
                dout.writeFloat(cf.get_cartesianFloat().y());
                dout.writeFloat(cf.get_cartesianFloat().z());
                dout.writeLong(cf.get_timeStamp());
            }

            dout.writeInt(postGyroData.size());

            for (TimeStampedCartesianFloat cf : postGyroData) {
                dout.writeFloat(cf.get_cartesianFloat().x());
                dout.writeFloat(cf.get_cartesianFloat().y());
                dout.writeFloat(cf.get_cartesianFloat().z());
                dout.writeLong(cf.get_timeStamp());
            }

            accDataReady = false;
            gyroDataReady = false;

            preAccelData.clear();
            postAccelData.clear();

            preGyroData.clear();
            postGyroData.clear();

            listenSound = false;
            accPostCount = 0;
            gyroPostCount = 0;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MetaWearBleService.LocalBinder binder = (MetaWearBleService.LocalBinder) service;

        final BluetoothManager btManager= (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice= btManager.getAdapter().getRemoteDevice(Constants.DEVICE_MAC_ADDR);

        binder.executeOnUiThread();

        mwBoard= binder.getMetaWearBoard(remoteDevice);
        mwBoard.connect();
        mwBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
            @Override
            public void connected() {
                Log.d(Constants.TAG, "Connected");

                mwBoard.readDeviceInformation().onComplete(new AsyncOperation.CompletionHandler<MetaWearBoard.DeviceInformation>() {
                    @Override
                    public void success(MetaWearBoard.DeviceInformation result) {
                        Log.d(Constants.TAG, "Device Information: " + result.toString());
                    }

                    @Override
                    public void failure(Throwable error) {
                        Log.d(Constants.TAG, "Error reading device information", error);
                    }
                });
                mwBoard.readBatteryLevel().onComplete(new AsyncOperation.CompletionHandler<Byte>() {
                    @Override
                    public void success(final Byte result) {
                        Log.d(Constants.TAG, "Battery Level : " + String.format(Locale.US, "%d", result));
                    }

                    @Override
                    public void failure(Throwable error) {
                        Log.e(Constants.TAG, "Error reading battery level", error);
                    }
                });

                startAcceleroMeter();
                startGyro();
            }

            @Override
            public void disconnected() {
                Log.d(Constants.TAG, "Disconnected");
            }

            @Override
            public void failure(int status, final Throwable error) {
                Log.d(Constants.TAG, "Error connecting", error);
                Toast.makeText(MainActivity.this, "Error connect", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mwBoard.disconnect();
        getApplicationContext().unbindService(this);
        if (gyroModule != null) {
            gyroModule.stop();
        }

        if (accelModule != null) {
            accelModule.stop();
        }

        mSensor.stop();

        DataOutputStream dout = null;
        try {
            dout = new DataOutputStream(socket.getOutputStream());
            dout.writeInt(-1);
        }catch (IOException e) {

        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
    }
}
