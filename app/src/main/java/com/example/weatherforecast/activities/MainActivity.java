package com.example.weatherforecast.activities;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.textclassifier.TextLinks;
import android.widget.Toast;

import com.example.weatherforecast.R;
import com.example.weatherforecast.databinding.ActivityMainBinding;
import com.example.weatherforecast.network.Network;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import kotlin.Suppress;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    String currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        currentLocation = intent.getStringExtra("LATITUDE") + ","
                + intent.getStringExtra("LONGITUDE");

        //Log.v("Hi", currentLocation);

        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(Network.openWeatherAPI + "current.json?key=" + Network.openWeatherAPIKey
                        + "&aqi=no&q=" + currentLocation)
                .build();

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        return null;
                    }
                    return response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null) {
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(s);
                        JSONObject locationObject = jsonResponse.getJSONObject("location");
                        binding.locationText.setText(locationObject.getString("name"));

                        JSONObject currentObject = jsonResponse.getJSONObject("current");
                        String currentT = String.valueOf(Math.round(Double.parseDouble(currentObject.getString("temp_c"))));
                        binding.currentText.setText(currentT + "C");
                        JSONObject forecast = jsonResponse.getJSONObject("forecast");
                        JSONArray forecastDay = forecast.getJSONArray("forecastday");
                        JSONObject day = forecastDay.getJSONObject(0).getJSONObject("day");
                        String minT = String.valueOf(Math.round(Double.parseDouble(day.getString("mintemp_c"))));
                        String maxT = String.valueOf(Math.round(Double.parseDouble(day.getString("maxtemp_c"))));
                        binding.minimumText.setText("min: " + minT + "C");
                        binding.maximumText.setText("max: " + maxT + "C");
                        JSONObject condition = currentObject.getJSONObject("condition");
                        String image_url =  "https:" + condition.getString("icon");

                        Picasso.get().load(image_url).resize(100, 100).into(binding.weatherImage);//libreria pa la imagen

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        asyncTask.execute();
    }
}