package io.ready.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadyTools {

    String[] urls;

    public static ReadyTools applyPlugin(String... urls) {
        return new ReadyTools(urls);
    }

    public ReadyTools() {
        throw new UnsupportedOperationException("You need to apply any plugin first.");
    }

    public ReadyTools(String... urls) {
        if(urls.length == 0) {
            throw new UnsupportedOperationException("You need to apply any plugin first.");
        }

        this.urls = urls;
    }

    public List<String> getPluginNames() {
        return new ArrayList<>(Arrays.asList(urls));
    }
}
