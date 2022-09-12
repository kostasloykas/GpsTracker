package com.loukas.gpstracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;

import java.security.Permissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;
import java.util.function.Consumer;

public class ProcessActivity extends AppCompatActivity implements LocationListener {

    private Button start_but;
    private EditText message_meter;
    private Process process = new Process();
    private LocationManager locationManager = null;
    private String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.SEND_SMS};
    private Handler mhandler = new Handler();
    private Runnable location_request;
    private long time = 0;
    private Runnable calculate_distance = new Runnable() {
        private int i=0;
        @Override
        public void run() {
            try{
                assert GetValueOfKey("phone") != null;
                double meter = Double.parseDouble(message_meter.getText().toString());
                if (ActivityCompat.checkSelfPermission(ProcessActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ProcessActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ProcessActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

                    ((EditText) findViewById(R.id.out)).append("ERROR PERMISSION\n");
                    return;
                }
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                process.setX(location.getLatitude());
                process.setY(location.getLongitude());
                process.CalculateDistance();
                ((EditText) findViewById(R.id.out)).append(i+" Distance="+process.getDistance()+"\n");
                i++;
                if(process.getDistance() <= meter ){
                    ((EditText) findViewById(R.id.out)).append("Το λαστιχο εφτασε στο καρουλι\n");
                    mhandler.removeCallbacks(location_request);
                    SendMessage("ΤΟ ΛΑΣΤΙΧΟ ΕΦΤΑΣΕ ΣΤΑ ΜΕΤΡΑ ΠΟΥ ΤΟ ΟΡΙΣΑΤΕ \n" + Calendar.getInstance().getTime());
                    return;
                }
                mhandler.postDelayed(this ,  time+2000);
            }catch (NullPointerException e){
                ((EditText) findViewById(R.id.out)).append("NullPointerException\n");
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ClearTitle();
        setContentView(R.layout.activity_process);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);
        }

        requestPermissions(PERMISSIONS, 1);
        ((EditText) findViewById(R.id.out)).append("Request Permissions\n");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mhandler.removeCallbacks(location_request);
        mhandler.removeCallbacks(calculate_distance);
    }


    public void ClearTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
    }

    public void StartButtonClicked(View view) {
        start_but = (Button) findViewById(R.id.start_button);
        message_meter = (EditText) findViewById(R.id.meter_input);

        if ("start".equalsIgnoreCase(start_but.getText().toString())) {
            ((EditText) findViewById(R.id.out)).setText("");
            TextView kar_x = (TextView) findViewById(R.id.kar_x);
            TextView kar_y = (TextView) findViewById(R.id.kar_y);
            TextView x = (TextView) findViewById(R.id.x);
            TextView y = (TextView) findViewById(R.id.y);
            if(kar_x.getText().toString().equals("0") || kar_y.getText().toString().equals("0")
            || y.getText().toString().equals("0") || x.getText().toString().equals("0")){
                Toast.makeText(getApplicationContext(), "Πρεπει να μπουν οι συντεταγμενες στο καρουλι και στο λαστιχο", Toast.LENGTH_SHORT).show();
                return;
            }
            if (message_meter.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Πρεπει να συμπληρωσετε τα μετρα", Toast.LENGTH_SHORT).show();
                return;
            }

            if (GetValueOfKey("phone") == null) {
                Toast.makeText(getApplicationContext(), "Πρεπει να συμπληρωσετε τα στοιχεια χρηστη", Toast.LENGTH_SHORT).show();
                return;
            }

            process.CalculateDistance();
            if(process.getDistance() < Double.parseDouble(message_meter.getText().toString())){
                Toast.makeText(getApplicationContext(),"Τα μετρα ειναι μεγαλυτερα απο την αποσταση", Toast.LENGTH_SHORT).show();
                return;
            }
            time=20*1000;
            ((EditText) findViewById(R.id.out)).append("Distance="+process.getDistance()+"\n");
            calculate_distance.run();



            start_but.getBackground().setTint(Color.RED);
            start_but.setText("STOP");

        } else if ("stop".equalsIgnoreCase(start_but.getText().toString())) {
            mhandler.removeCallbacks(calculate_distance);
            time=0;
            location_request.run();
            start_but.getBackground().setTint(Color.GREEN);
            start_but.setText("START");
        } else
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
    }


    public void KarouliButtonClicked(View view) {
        TextView kar_x = (TextView) findViewById(R.id.kar_x);
        TextView kar_y = (TextView) findViewById(R.id.kar_y);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location tmp=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(tmp!=null) {
            float lat = (float) tmp.getLatitude();
            float lon = (float) tmp.getLongitude();
            process.setKar_x(tmp.getLatitude());
            process.setKar_y(tmp.getLongitude());
            kar_x.setText(String.valueOf(lat));
            kar_y.setText(String.valueOf(lon));
        }

        return;
    }

    public void LastixoButtonClicked(View view) {
        TextView x = (TextView) findViewById(R.id.x);
        TextView y = (TextView) findViewById(R.id.y);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location tmp=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(tmp!=null) {
            float lat = (float) tmp.getLatitude();
            float lon = (float) tmp.getLongitude();
            process.setX(tmp.getLatitude());
            process.setY(tmp.getLongitude());
            x.setText(String.valueOf(lat));
            y.setText(String.valueOf(lon));
        }
        return;
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        mhandler.post(new Runnable() {
            @Override
            public void run() {
                ((EditText) findViewById(R.id.out)).append(location.getLatitude()+"--"+location.getLongitude()+"\n");
                locationManager.removeUpdates(ProcessActivity.this);
                return;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ((EditText) findViewById(R.id.out)).append("Permission Granted\n");
            time=0;
            location_request = new Runnable() {
                @Override
                public void run() {
                    RequestLocation();
                    mhandler.postDelayed(this,time);
                }
            };
            mhandler.postDelayed(location_request, 1000);
        }

    }

    public void RequestLocation() {

        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ((EditText) findViewById(R.id.out)).append("Permission Not Granted\n");
                return;
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            }
        }else{
            ((EditText) findViewById(R.id.out)).append("Gps is not enable\n");
        }

    }

    public String GetValueOfKey(String key){
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        Map<String,?> entries = sharedPreferences.getAll();
        for (Map.Entry<String,?> entry:entries.entrySet()){
            if(entry.getKey().equals(key)){
                return (String) entry.getValue();
            }
        }
        return null;
    }


    public void SendMessage(String str){
        assert GetValueOfKey("phone")!=null;
        String phone=GetValueOfKey("phone").trim();
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone,null,str,null,null);
        Toast.makeText(getApplicationContext(),"Το μηνυμα σταλθηκε" , Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}