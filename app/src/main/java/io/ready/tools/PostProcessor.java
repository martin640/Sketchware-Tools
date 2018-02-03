package io.ready.tools;

import org.json.JSONException;
import org.json.JSONObject;

public class PostProcessor {

    public static void processJson(String src, PostResult postResult) {
        try {
            JSONObject object = new JSONObject(src);
            postResult.onResult(object);
        } catch (Exception e) {
            postResult.onError(new JSONException("Error when parsing json \"" + src + "\""));
        }
    }

    public static void processPost(Long id, PostResult postResult) {
        postResult.onError(new UnsupportedOperationException("Coming soon... (requested post " + id + ")"));
    }

    /**
     * PostResult interface is used to handle void result. You have to cast 'result' to requested type
     * for example {@code JSONObject target = (JSONObject) result;}
     */
    public interface PostResult {
        void onResult(Object result) throws Exception;

        void onError(Throwable t);
    }
}
