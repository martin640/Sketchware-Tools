package io.ready.updatelog;

import android.text.Spanned;

public class LogObject {

    private String data;
    private Spanned spannable;

    public LogObject() {
        this.data = "";
    }

    public LogObject(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
