package io.ready.updatelog;

public class LogObject {

    private String data;

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
