package io.ready.updatelog;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class UpdateLog {

    private Activity activity;
    private LogObject object;
    private String path;

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

    public void fetchLatestData() {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, path, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    object.setData(response.getJSONObject("update_log").getString("latest"));
                } catch (Exception e) {
                    e.printStackTrace();
                    object.setData("Error while downloading update log: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                object.setData("Error while downloading update log: " + error.getMessage());
            }
        });
    }

    public String getLog() {
        if (object != null) {
            return object.getData();
        } else {
            return "Unable to find log instance (developer error)!";
        }
    }
}
