package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by TroysMacBook on 8/7/16.
 */
public class getStockHistoryTask extends AsyncTask<String,Void, String[][]> {
    public String[][] allData;
    Context mContext;

    public getStockHistoryTask(Context c){
        mContext=c;
    }

    @Override
    protected String[][] doInBackground(String... params) {

        //get current date
        Date currentDate = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(currentDate);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH)+1;
        int currentYear = calendar.get(Calendar.YEAR);

        //what timeline chart option is selected
            int YearsAgo = calendar.get(Calendar.YEAR)-1;



        String stringDay=null;
        String stringMonth = null;

        //make sure day and month are two digits
        if(day<10)
            stringDay = String.format("%02d",day);
        else
            stringDay = Integer.toString(day);

        if(month<10)
            stringMonth = String.format("%02d",month);
        else
            stringMonth = Integer.toString(month);

        StringBuilder urlStringBuilder = new StringBuilder();
        HttpURLConnection urlConnection = null;
        String priceString=null;
        String ticker = params[0];

        String newURL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%0A%20%20%20%20%20%20%20%20%20where%20%20symbol%20%20%20%20%3D%20%22"+ticker+"%22%0A%20%20%20%20%20%20%20%20%20and%20%20%20%20startDate%20%3D%20%22"+YearsAgo+"-"+stringMonth+"-"+stringDay+"%22%0A%20%20%20%20%20%20%20%20%20and%20%20%20%20endDate%20%20%20%3D%20%22"+currentYear+"-"+stringMonth+"-"+stringDay+"%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

        BufferedReader reader = null;

        try {

            URL url = new URL(newURL);

            // Create the request to MovieDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String

            InputStream inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream is empty.
            }
            priceString = buffer.toString();
        }catch (
                IOException e

                )
        {
            Log.e("loadMovieInfo", "Error ", e);
        } finally

        {
            if (urlConnection != null) {
                urlConnection.disconnect();
                System.out.println("url successful!");
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("loadMovieInfo", "Error closing stream", e);

                }
            }
        }
        final String getResults = "query";
        String getDate = "Date";
        String getClose = "Close";

        try{
            JSONObject readerObject = new JSONObject(priceString);
            JSONObject queryObject = readerObject.getJSONObject(getResults);
            JSONObject priceObject = queryObject.getJSONObject("results");

            JSONArray priceArray = priceObject.getJSONArray("quote");
            JSONObject priceData;
            allData = new String[priceArray.length()][2];

            String date;
            String close;

            for(int i=0; priceArray.length()>i;i++) {

                priceData = priceArray.getJSONObject(i);
                date = priceData.getString(getDate);
                close = priceData.getString(getClose);

                allData[i][0] = date;
                allData[i][1] = close;
            }
            return allData;

        }  catch (JSONException e) {
            e.printStackTrace();
            return null;
       }
    }
}
