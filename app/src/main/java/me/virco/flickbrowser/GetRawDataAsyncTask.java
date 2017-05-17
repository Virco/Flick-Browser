package me.virco.flickbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by marco on 5/16/2017.
 */

enum DownloadStatus {
    IDLE,
    PROCESSING,
    NOT_INITIALISED,
    FAILED_OR_EMPTY,
    OK
}

public class GetRawDataAsyncTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "GetRawDataAsyncTask";

    private DownloadStatus mDownloadStatus;
    private final OnDownloadComplete mCallback;

    interface OnDownloadComplete {
        void onDownloadComplete(String data, DownloadStatus status);
    }

    public GetRawDataAsyncTask(OnDownloadComplete callback) {
        this.mDownloadStatus = DownloadStatus.IDLE;
        this.mCallback = callback;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: parameter = " + s);
        if (mCallback != null) {
            mCallback.onDownloadComplete(s, mDownloadStatus);
        }
        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if (params == null) {
            mDownloadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        try {
            mDownloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(params[0]);

            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: The response was " + response);

            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            //String line;
            //while (null != (line = reader.readLine())) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line).append("\n");
            }

            mDownloadStatus = DownloadStatus.OK;
            return result.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL", e);
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO Exception reading data:", e);
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Security Exception. Needs permission?", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing stream", e);
                }
            }
        }

        mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }
}
