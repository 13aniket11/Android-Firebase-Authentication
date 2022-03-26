package com.example.androidauth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.androidauth.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button btnLogOut;
    FirebaseAuth mAuth;
    private TextView status;
    private Button button, button2;
    private LocationManager locationManager;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    public int flg = 0;
    private SensorEventListener gyroscopeEventListener;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference root = db.getReference().child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogOut = findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        btnLogOut.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
        });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if (gyroscopeSensor == null) {
            Toast.makeText(this, "device has no gyroscope", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
//                textViewLatitude.setText(String.valueOf(location.getLatitude()));
//                textViewLongitude.setText(String.valueOf(location.getLongitude()));


            }
        });

        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.values[0] > 5f || sensorEvent.values[0] < -5f || sensorEvent.values[1] > 5f || sensorEvent.values[1] < -5f || sensorEvent.values[2] > 5f || sensorEvent.values[2] < -5f) {
                    HashMap<String, String> userMap = new HashMap<>();

                    userMap.put("0", "" + sensorEvent.values[0]);
                    userMap.put("1", "" + sensorEvent.values[1]);
                    userMap.put("2", "" + sensorEvent.values[2]);


                    if(flg==1)
                    {
                        root.push().setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainActivity.this, "Data Saved", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        status = findViewById(R.id.text);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flg=1;
                status.setText("Connected");
                status.setTextColor(Color.parseColor("#008037"));
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flg=0;
                status.setText("Disconnected");
                status.setTextColor(Color.parseColor("#FF1616"));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainActivity.this, Login.class));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, sensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(gyroscopeEventListener);
        flg=0;
    }
}