package com.tarandeepsingh.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {
    LinearLayout dev_container;
    Geocoder geocoder;
    String API_KEY = "e6a99aef963a3e835da961d3cf9aeeb6";
    FusedLocationProviderClient mfusedproviderclient;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    TextView tv_sunrise,tv_sunset,tv_wind,tv_pressure,tv_humid,tv_dev,tv_address;
    ProgressBar progressBar;
    TextView errorText;
    RelativeLayout relativeLayout;
    TextView tv_more_Details;
    String mlat,mlon;
    private static DecimalFormat df2;
    TextView updatedtv,temperature,temp_min,temp_max,current_status;

    // function to check if network connection is available

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_more_Details = findViewById(R.id.tv_more_details);
        df2 = new DecimalFormat("#.##");
        tv_address = findViewById(R.id.tv_address);
        temp_max= findViewById(R.id.temp_max);
        temp_min = findViewById(R.id.temp_min);
        current_status = findViewById(R.id.current_status);
        updatedtv = findViewById(R.id.updated_at);
        temperature = findViewById(R.id.temperature);
        relativeLayout = findViewById(R.id.main_container);
        tv_sunset = findViewById(R.id.tv_sunset);
        tv_sunrise = findViewById(R.id.tv_sunrise);
        tv_dev = findViewById(R.id.tv_contact);
        tv_humid = findViewById(R.id.tv_Humidity);
        tv_pressure = findViewById(R.id.tv_pressure);
        tv_wind = findViewById(R.id.tv_wind);
        progressBar = findViewById(R.id.loader);
        errorText = findViewById(R.id.errorText);
        dev_container = findViewById(R.id.dev_container);
        dev_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,dev_info.class));
            }
        });
        tv_more_Details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://openweathermap.org/city"));
                startActivity(i);
            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},5);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},5);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.INTERNET},5);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_NETWORK_STATE},5);
        }

//        if(!haveNetworkConnection()){
//            Toast.makeText(this, "No internet Available", Toast.LENGTH_SHORT).show();
//            errorText.setVisibility(View.VISIBLE);
//        }
        // work to get lat and lng
        geocoder = new Geocoder(this, Locale.getDefault());
        mfusedproviderclient = LocationServices.getFusedLocationProviderClient(this);
        mfusedproviderclient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    mlat = Double.toString(location.getLatitude());
                    mlon = Double.toString(location.getLongitude());
                    if(haveNetworkConnection()){
                        setWeather();
                    }
                    else{
                        errorText.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                    }

                    Log.d("last known mainlat/long",mlat+","+mlon);
                    try {
                        List<Address> list = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        String address = list.get(0).getAddressLine(0);
                        tv_address.setText(list.get(0).getSubLocality()+","+list.get(0).getLocality());

                        Log.d("adress",address);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setSmallestDisplacement(250); //  read displacement 250m
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult==null){
                    return;
                }
                Location location = locationResult.getLocations().get(0);
                if(location!=null){
                    mlat = Double.toString(location.getLatitude());
                    mlon = Double.toString(location.getLongitude());
                    if(haveNetworkConnection()){
                        setWeather();
                    }
                    Log.d("callback lat/long",mlat+","+mlon);
                    try {
                        List<Address> list = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        String address = list.get(0).getAddressLine(0);
                        Log.d("adress",address);
                        tv_address.setText(list.get(0).getSubLocality()+","+list.get(0).getLocality());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        mfusedproviderclient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.getMainLooper());

        /****location work done****/



    }

    private void setWeather() {

        Log.d("setweather lat/long",mlat+","+mlon);
        // pressure and wind speed using open weather
        (new weatherTask()).execute("http://api.openweathermap.org/data/2.5/weather?lat="+mlat+"&lon="+mlon+"&appid="+API_KEY);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==5 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && haveNetworkConnection()){
            mfusedproviderclient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null){
                        mlat = Double.toString(location.getLatitude());
                        mlon = Double.toString(location.getLongitude());
                        setWeather();
                        Log.d("last known mainlat/long",mlat+","+mlon);
                        try {
                            List<Address> list = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                            String address = list.get(0).getAddressLine(0);

                            tv_address.setText(list.get(0).getSubLocality()+","+list.get(0).getLocality());

                            Log.d("on request permsion",address);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        }
        else{
            Log.d("on req permsion","toast show");
            errorText.setVisibility(View.VISIBLE);
        }
    }

    private class weatherTask extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
            errorText.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {

                JSONObject jsonObject = new JSONObject(s);
                JSONObject weather =jsonObject.getJSONArray("weather").getJSONObject(0);
                JSONObject main = jsonObject.getJSONObject("main");
                JSONObject wind = jsonObject.getJSONObject("wind");
                JSONObject sys = jsonObject.getJSONObject("sys");

                Long dt = jsonObject.getLong("dt");
                Log.d("time stamp",Long.toString(dt));
                Date updatedf = new java.util.Date(dt*1000);
                String updatedTime = new SimpleDateFormat("MM dd, yyyy hh:mma").format(updatedf);
                updatedtv.setText(updatedTime);


                String desc = weather.getString("description");
                current_status.setText(desc);


                temperature.setText(df2.format(main.getInt("temp")-273.15) +"°C");
                temp_min.setText("Min temp:"+df2.format(main.getInt("temp_min")-273.15)+"°C");
                temp_max.setText("Max temp:"+df2.format(main.getInt("temp_max")-273.15)+"°C");

                // its need to be in milisecond
                long sunriseTimestamp= sys.getLong("sunrise")*1000;
                long sunsetTimestamp= sys.getLong("sunset")*1000;

                Date sunsetdf = new java.util.Date(sunsetTimestamp);
                Date sunrisedf = new java.util.Date(sunriseTimestamp);
                String sunriseString = new SimpleDateFormat("hh:mma").format(sunrisedf);
                String sunsetString = new SimpleDateFormat("hh:mma").format(sunsetdf);
                tv_sunrise.setText(sunriseString);
                tv_sunset.setText(sunsetString);
                tv_wind.setText(wind.getString("speed")+" m/s");
                tv_pressure.setText(main.getString("pressure")+" Pa");
                tv_humid.setText(main.getString("humidity"));
                relativeLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

            } catch (Exception e) {
                relativeLayout.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                errorText.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }



                // StandardCharsets.UTF_8.name() > JDK 7
                Log.d("result",result.toString("UTF-8"));
                return result.toString("UTF-8");
            }
            catch (Exception e){
//                errorText.setVisibility(View.VISIBLE);
                Log.i("inside","do in back");
//                relativeLayout.setVisibility( View.GONE);
                e.printStackTrace();
            }
            return null;
        }
    }
}
