package io.ready.updatelog;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.ready.swpff.BuildConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.ready.tools.PostProcessor;
import io.ready.tools.Updater;

public class UpdateLog {

    private Activity activity;
    private LogObject object;
    private String path;
    private OnDataFetched listener;
    private Updater updater;

    public UpdateLog(Activity activity, String path) {
        this.activity = activity;
        this.object = new LogObject();
        this.path = path;
    }

    public UpdateLog() {
        throw new UnsupportedOperationException("Missing activity data and server path");
    }

    public static UpdateLog create(Activity activity, String path) {
        return new UpdateLog(activity, path);
    }

    public void fetchLatestData(OnDataFetched listener) {
        updater = new Updater(activity, BuildConfig.VERSION_CODE);
        if (!updater.isLatest()) {
            this.listener = listener;
            new JsonTask().execute(path);
        }
    }

    public String getLog() {
        if (object != null) {
            return object.getData();
        } else {
            return "Unable to find log instance (developer error)!";
        }
    }

    public void showLog() {
        updater.showReleaseNotes(getLog());
    }

    public interface OnDataFetched {
        void onSuccess(UpdateLog object, String logData);

        void onError(Throwable error);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            PostProcessor.processJson(result, new PostProcessor.PostResult() {
                @Override
                public void onResult(Object result) throws Exception {
                    JSONObject obj = (JSONObject) result;
                    object.setData(obj.getJSONObject("update_log").getString("latest"));
                    listener.onSuccess(UpdateLog.this, object.getData());
                }

                @Override
                public void onError(Throwable t) {
                    object.setData("Could not parse malformed JSON: \"" + result + "\"");
                    listener.onError(t);
                }
            });
        }
    }
}
