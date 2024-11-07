package com.example.lookforsun;

import static android.graphics.BlendMode.COLOR;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.http.Headers;


public class MainActivity extends AppCompatActivity implements LocationListener {

    TextView locationString;
    TextView sunlevel;
    TextView protection;
    Button buttonUV;
    LocationManager locationManager;
    Location deviceLocation;
    String uvlevel;

    ConstraintLayout main;
    String apiKey = "openuv-33p4rm0mieojt-io";  // Replace with your actual OpenUV API key


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        locationString = findViewById(R.id.locationText);
        buttonUV = findViewById(R.id.buttonUV);
        main = findViewById(R.id.main);
        sunlevel= findViewById(R.id.sunlevel);
        protection= findViewById(R.id.protection);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }
        getLocation();
        buttonUV.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(deviceLocation!=null) GetJsonRepsponse();
            }
        });

    }

    public void GetJsonRepsponse(){
        String url = "https://api.openuv.io/api/v1/uv?lat="+deviceLocation.getLatitude()+"&lng="+deviceLocation.getLongitude()+"&alt=100&dt=";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    uvlevel = response.getJSONObject("result").getString("uv");
                    locationString.setText(uvlevel);
                    ChangeBackground(Float.parseFloat(uvlevel));
                } catch (Exception ex) {

                }
            }}, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse (VolleyError error){
            }

        }){
            // Override the getHeaders() method to add the API key to the headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-access-token", apiKey);  // Add your OpenUV API key here
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
    @SuppressLint("MissingPermission")
    public void getLocation(){
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5,MainActivity.this);
            deviceLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        }catch (Exception e){

        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
            deviceLocation = location;
        }
        public void ChangeBackground(float v){
            if(v>=0&&v<=2){ main.setBackgroundColor(Color.GREEN);
            buttonUV.setBackgroundColor(Color.rgb(27,100,46));
            sunlevel.setText("low");
            protection.setText("No protection \nneeded");
            }
            else if(v>2&&v<=5){main.setBackgroundColor(Color.YELLOW);
                buttonUV.setBackgroundColor(Color.rgb(250,200,0));
                sunlevel.setText("medium");
                protection.setText("Some protection \n is required");
            }
            else if(v>5&&v<=7){main.setBackgroundColor(Color.rgb(255,  165,  0));
                buttonUV.setBackgroundColor(Color.rgb(250,125,0));
                sunlevel.setText("high");
                protection.setText("Protection \n  essential");
            }
            else if(v>7&&v<=10){main.setBackgroundColor(Color.RED);
                buttonUV.setBackgroundColor(Color.rgb(171,19,19));
                sunlevel.setText("very high");
                protection.setText("Extra protection \n is needed");}
            else {main.setBackgroundColor(Color.MAGENTA);
                buttonUV.setBackgroundColor(Color.rgb(171,19,161));
                sunlevel.setText("extreme");
                protection.setText("It is safer \n to stay inside");
            }
        }
}