package com.ready.swpff;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static io.ready.tools.IdHelper.getId;

public class OreoService extends Service {
    private FileWatcher buildWatcher;
    private int NOTIF_ID = 45646;
    private NotificationCompat.Builder mBuilder;


    public OreoService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();

        mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setContentTitle("Sketchware Tools")
                        .setContentText("Pixel Screen Helper working")
                        .setOngoing(true);

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(NOTIF_ID, mBuilder.build());

        register_build_receiver();
    }

    private void register_build_receiver() {
        buildWatcher = new FileWatcher(Environment.getExternalStorageDirectory() + "/.sketchware/mysc/");
        buildWatcher.setEventListener(new FileWatcher.EventListener() {
            @Override
            public void onCreateEvent(String path) {
                int di = getId(path);
                updateManifest(di, path);
            }

            @Override
            public void onModifyEvent(String path) {

            }

            @Override
            public void onDeleteEvent(String path) {

            }

            @Override
            public void onUpdateEvent(String path) {

            }

            @Override
            public void onAccessEvent(String path) {

            }
        });
        buildWatcher.startWatching();
    }

    public void updateManifest(final int id, final String path) {
        Handler refresh3 = new Handler(Looper.getMainLooper());
        refresh3.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                writeManifest(Environment.getExternalStorageDirectory() + "/.sketchware/mysc/" + id + "/app/src/main/AndroidManifest.xml");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIF_ID);
    }

    public void writeManifest(String manifest) {
        File file = new File(manifest);
        String manifest_content;
        if (file.exists()) {
            manifest_content = readFile(manifest);

            String part1 = manifest_content.substring(0, manifest_content.indexOf("<activity") - 1);
            String part2 = manifest_content.substring(manifest_content.indexOf("<activity"), manifest_content.length());

            String manifest_new = part1 + "\n" +
                    "<meta-data android:name=\"android.max_aspect\" android:value=\"2.1\" />" + "\n" +
                    part2;

            try {
                FileOutputStream stream = new FileOutputStream(file);
                try {
                    stream.write(manifest_new.getBytes());
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    public String readFile(String path) {
        File file = new File(path);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return text.toString();
    }
}