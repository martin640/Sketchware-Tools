package io.ready.tools;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ready.swpff.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Updater {

    private int version;
    private Activity context;

    public Updater(Activity context, int version_code) {
        this.version = version_code;
        this.context = context;
    }

    public Updater check() {

        if(version != context.getSharedPreferences(getClass().getName(), Context.MODE_PRIVATE).getInt("saved_instance", -1)){
            showReleaseNotes();
            context.getSharedPreferences(getClass().getName(), Context.MODE_PRIVATE).edit().putInt("saved_instance", version).apply();
        }

        return this;
    }

    public void showReleaseNotes() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.release_notes, null);
        dialogBuilder.setView(dialogView);

        TextView release_notes = dialogView.findViewById(R.id.textView6);
        Button gotit = dialogView.findViewById(R.id.button6);
        final AlertDialog alertDialog = dialogBuilder.create();

        release_notes.setText(Html.fromHtml(readFromfile("update.html", context.getApplicationContext())));
        gotit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public String readFromfile(String fileName, Context context) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets()
                    .open(fileName, Context.MODE_WORLD_READABLE);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }
}
