package com.example.giua_ki.paid;

import android.os.AsyncTask;
import android.util.Log;

import com.example.giua_ki.listener.OnTaskCompleted;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleSheetsTask extends AsyncTask<Void, Void, String> {
    private final OnTaskCompleted listener;

    public GoogleSheetsTask(OnTaskCompleted listener) {
        this.listener = listener;
    }
    @Override
    protected String doInBackground(Void... voids) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://script.google.com/macros/s/AKfycbw45jCYcr8I2w8EcmLbPDdWYUHmX4_0xuZVjg7NRpkuhJWUGL6JnMLEHBs2887Thzp75Q/exec");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                StringBuilder inline = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    inline.append(line);
                }

                reader.close();

                return inline.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            try {
                JSONObject dataObject = new JSONObject(result);
                JSONArray dataArray = dataObject.getJSONArray("data");
                JSONObject lastPaid = dataArray.getJSONObject(dataArray.length() - 1);
                String price = lastPaid.getString("Giá trị");
                String describe = lastPaid.getString("Mô tả");
                if (listener != null) {
                    listener.onTaskCompleted(price,describe);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("GGS", "Error fetching data");
        }
    }
}



