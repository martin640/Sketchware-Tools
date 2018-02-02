package io.ready.updatelog;

import android.app.Activity;

public class UpdateLog {

    private Activity activity;
    private LogObject object;

    public UpdateLog(Activity activity) {
        this.activity = activity;
    }

    public UpdateLog() {
        throw new UnsupportedOperationException("Missing activity data");
    }

    public static UpdateLog create(Activity activity) {
        return new UpdateLog(activity);
    }

    public void fetchLatestData() {
        this.object = new LogObject("TEST");
    }

    public String getLog() {
        if(object != null) {
            return object.getData();
        } else {
            return new LogObject().getData();
        }
    }
}
