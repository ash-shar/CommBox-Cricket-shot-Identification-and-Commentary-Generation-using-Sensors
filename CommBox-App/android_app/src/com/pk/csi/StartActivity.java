package com.pk.csi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    Button startBtn;
    EditText ipEdit;

    private boolean permissionToRecordAccepted = false;
    private boolean permissionToWriteAccepted = false;
    private String [] appPermissions = {"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private final int REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strart);
        startBtn = (Button) findViewById(R.id.startBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRecord(appPermissions);
            }
        });

        ipEdit = (EditText) findViewById(R.id.ipEdit);
    }

    public void startSampling() {
        String ip = ipEdit.getText().toString();

        if (ip.length() < 7) {
            Toast.makeText(this, "Please enter proper IP", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("IP", ipEdit.getText().toString());
        startActivity(i);
    }

    private void initRecord(String[] permissions) {
        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        } else {
            startSampling();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                permissionToWriteAccepted  = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (permissionToRecordAccepted  && permissionToWriteAccepted) {
            startSampling();
        } else {
            Toast.makeText(this, "We need the permissions to continue", Toast.LENGTH_LONG).show();
        }
    }

}
