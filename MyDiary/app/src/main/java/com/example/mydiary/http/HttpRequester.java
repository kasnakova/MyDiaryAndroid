package com.example.mydiary.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

import com.example.mydiary.fragments.CalendarFragment;
import com.example.mydiary.interfaces.IAsyncResponse;
import com.example.mydiary.utilities.Logger;

public class HttpRequester extends AsyncTask<String, Void, String> {
    private final String USER_AGENT = "Mozilla/5.0";
    private final String TAG = "HttpRequester";
    private final int CONNECTION_TIMEOUT = 10000;
    private IAsyncResponse delegate;
    private HttpURLConnection connection;

    public HttpRequester(IAsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        try {
            String requestMethod = params[1];
            URL objUrl = new URL(url);
            connection = (HttpURLConnection) objUrl.openConnection();

            connection.setRequestMethod(requestMethod);
            //connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            if(params.length > 3){
                connection.setRequestProperty("Authorization", "Bearer " + params[3]);
            }

            if (requestMethod == "POST") {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setUseCaches (true);
                connection.setDoOutput(true);
                connection.setDoInput(true);

                String urlParameters = params[2];
                if(!(urlParameters.equals(""))) {
                    DataOutputStream wr = new DataOutputStream(
                            connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();
                }
            }

            Logger.getInstance().logMessage(TAG, "Sending http request");
            int responseCode = connection.getResponseCode();
            Logger.getInstance().logMessage(TAG, "Response code " + responseCode);

            StringBuffer response = new StringBuffer();
            response.append("{'success':");
            BufferedReader in = null;
            if(responseCode == 200) {
                in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                response.append("'true'");
            } else {
                in = new BufferedReader(new InputStreamReader(
                        connection.getErrorStream()));
                response.append("'false'");
            }
            response.append(",'url':'" + url + "'");
            response.append(",'data':'");
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            response.append("'}");
            return response.toString();
        } catch (Exception e) {
            try {
                int responseCode = connection.getResponseCode();
                Logger.getInstance().logMessage(TAG, "Response code from try block " + responseCode);
                if(responseCode == 401){
                    return "{'success':'false','url':'" + url + "','data':'" + e.getMessage() + "}";
                }
            }catch (Exception ex){
                Logger.getInstance().logError(TAG, ex);
            }

            Logger.getInstance().logError(TAG, e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String data) {
        delegate.processFinish(data);
    }
}
