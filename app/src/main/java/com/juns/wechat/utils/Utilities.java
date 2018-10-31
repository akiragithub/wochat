package com.juns.wechat.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class Utilities {

    private final static String TAG = "Utilities";

    @Nullable
    public static String akiraConnection(String stringUrl, String dataToSend) {
        Log.d(TAG, "akiraConnectionStarted");
        try {
            String responseString = "";
            URL url = new URL(stringUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setDoOutput(dataToSend!=null);
            urlConnection.connect();
            if (dataToSend!=null)
            {
                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(dataToSend.getBytes());
                Log.d(TAG,"dataToSend is not null");
            }
            if (dataToSend==null/*||urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK*/) {
                //Log.d("ConnexionresponsecodeOK", String.valueOf(urlConnection.getResponseCode()));
                Log.d(TAG,"we are here");
                InputStream inputStream = urlConnection.getInputStream();
                Log.d(TAG,"before inputString");
                responseString = streamToString(inputStream);
                Log.d("inputstreamstring", responseString);
                inputStream.close();
            } else {
                Log.d("TentativeDeConnection", urlConnection.getResponseMessage());
                return null;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(TAG, "UnknownHostError");
        } catch (MalformedURLException e) {
            Log.d(TAG, "MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "IOException");
            e.printStackTrace();
        }
        // Whenever something went wrong;
        Log.d(TAG, "something went wrong");
        return null;
    }


    public static String streamToString(InputStream inputStream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("streamToString_finished","return "+builder.toString());
        return builder.toString();
    }
    public static boolean stringToFile(String s, File file, Context c)
    {
        try{
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(c.openFileOutput(file.getName(),Context.MODE_PRIVATE));
        outputStreamWriter.write(s);
        outputStreamWriter.close();
        return true;
        }catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

    }

public static org.json.JSONObject stringToJSon(String key, String value)
{
    org.json.JSONObject object = new org.json.JSONObject();
    try
    {
        object = new org.json.JSONObject();
        object.put(key,value);
    }catch (JSONException e)
    {
        e.printStackTrace();
    }
    return object;
}
    public static org.json.JSONObject stringToJSon(String key1, String value1,String key2, String value2)
    {
        org.json.JSONObject object = new org.json.JSONObject();
        try
        {
            object = stringToJSon(key1,value1);
            object.put(key2,value2);
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return object;
    }


}
