package com.demo.smartparkng;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Unlock extends AppCompatActivity {

    Button button;
    char id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        button = ((Button) findViewById(R.id.button));
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String spot = intent.getStringExtra(Intent.EXTRA_TEXT);
            button.setText("Reserving " + spot);
            id = spot.charAt(2);
            button.setEnabled(false);
            ReserveTask reserveTask = new ReserveTask();
            reserveTask.execute();
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnlockTask unlockTask = new UnlockTask();
                button.setEnabled(false);
                unlockTask.execute();
            }
        });
    }

    class ReserveTask extends AsyncTask<Void, Void, Boolean> {
        private final String LOG_TAG = getClass().getSimpleName();


        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String respose = null;

            try {

                String baseUrl = "http://10.24.47.29:3000/reserve";
                URL url = new URL(baseUrl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty( "Content-Type", "application/json");
                urlConnection.setUseCaches( false );
                try( DataOutputStream wr = new DataOutputStream( urlConnection.getOutputStream())) {
                    String s = "{\"id\":"+ id + "}";
                    wr.write(s.getBytes());
                }
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream != null) {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() != 0) {
                        respose = buffer.toString();
                        Log.i(LOG_TAG, respose);
                    }
                    return Boolean.parseBoolean(respose.trim());
                }

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();

                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if(b){
                button.setEnabled(true);
                button.setText("Unlock PS"+id);
            }
            else{
                finish();
            }
        }
    }

    class UnlockTask extends AsyncTask<Void, Void, Boolean>{
        private final String LOG_TAG = getClass().getSimpleName();


        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String respose = null;

            try {

                String baseUrl = "http://10.24.47.29:3000/unlock";
                URL url = new URL(baseUrl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty( "Content-Type", "application/json");
                urlConnection.setUseCaches( false );
                try( DataOutputStream wr = new DataOutputStream( urlConnection.getOutputStream())) {
                    String s = "{\"id\":"+ id + "}";
                    wr.write(s.getBytes());
                }
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream != null) {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() != 0) {
                        respose = buffer.toString();
                        Log.i(LOG_TAG, respose);
                    }
                    return Boolean.parseBoolean(respose.trim());
                }

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();

                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if(b){
                button.setEnabled(false);
                button.setText("Enjoy your parking");
            }
            else{
                finish();
            }
        }
    }
}
