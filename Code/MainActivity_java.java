package com.demo.smartparkng;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private ListStatusTask listStatusTask;
    Button button1;
    Button button2;
    Button button3;
    Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listStatusTask = new ListStatusTask();
        listStatusTask.execute();

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        View.OnClickListener cl = new View.OnClickListener() {
            public void onClick(View v) {
                Button cur = (Button) v;
                Intent intent = new Intent(getApplicationContext()
                        , Unlock.class)
                        .putExtra(Intent.EXTRA_TEXT, cur.getText());
                startActivity(intent);
            }
        };

        button1.setOnClickListener(cl);
        button2.setOnClickListener(cl);
        button3.setOnClickListener(cl);
        button4.setOnClickListener(cl);
    }
    class ListStatusTask extends AsyncTask<Void, Void, boolean[]> {
        private final String LOG_TAG = getClass().getSimpleName();


        @Override
        protected boolean[] doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            try {

                String baseUrl = "http://10.24.47.29:3000/list";
                URL url = new URL(baseUrl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
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
                        forecastJsonStr = buffer.toString();
                        Log.i(LOG_TAG, forecastJsonStr);
                    }
                    JSONArray forecastJson = new JSONArray(forecastJsonStr);
                    boolean[] b = new boolean[4];
                    for(int i = 0; i < 4; i++)
                        b[i] = forecastJson.getBoolean(i);
                    return b;
                }

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
            } catch (JSONException e) {
                Log.e("JSON parser", "Error ", e);
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
        protected void onPostExecute(boolean[] b) {
            if(b != null) {
                int cr = getColor(R.color.red);
                int cg = getColor(R.color.green);
                if (b[0]) button1.setTextColor(cg);
                else button1.setTextColor(cr);
                if (b[1]) button2.setTextColor(cg);
                else button2.setTextColor(cr);
                if (b[2]) button3.setTextColor(cg);
                else button3.setTextColor(cr);
                if (b[3]) button4.setTextColor(cg);
                else button4.setTextColor(cr);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listStatusTask = new ListStatusTask();
                    listStatusTask.execute();
                }
            }, 1000);
        }
    }
}
