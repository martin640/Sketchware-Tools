package com.ready.swpff;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ready.tools.ServiceTools;

public class MainActivity extends AppCompatActivity {

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    @BindView(R.id.button)
    Button start_service1;
    @BindView(R.id.button2)
    Button stop_service1;
    @BindView(R.id.button3)
    Button start_service2;
    @BindView(R.id.button4)
    Button stop_service2;
    @BindView(R.id.indicator1)
    ImageView indicator1;
    @BindView(R.id.indicator2)
    ImageView indicator2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }
    }

    private void initializeView() {
        start_service1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callService1();
            }
        });

        stop_service1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, HeadService.class));
                Toast.makeText(MainActivity.this, "Service has been killed", Toast.LENGTH_SHORT).show();
            }
        });

        start_service2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callService2();
            }
        });

        stop_service2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, OreoService.class));
                Toast.makeText(MainActivity.this, "Service has been killed", Toast.LENGTH_SHORT).show();
            }
        });

        CountDownTimer countDownTimer = new CountDownTimer(Integer.MAX_VALUE, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (ServiceTools.serviceRunning(MainActivity.this, HeadService.class)) {
                    start_service1.setEnabled(false);
                    stop_service1.setEnabled(true);
                    indicator1.getDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                } else {
                    start_service1.setEnabled(true);
                    stop_service1.setEnabled(false);
                    indicator1.getDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                }

                if (ServiceTools.serviceRunning(MainActivity.this, OreoService.class)) {
                    start_service2.setEnabled(false);
                    stop_service2.setEnabled(true);
                    indicator2.getDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                } else {
                    start_service2.setEnabled(true);
                    stop_service2.setEnabled(false);
                    indicator2.getDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onFinish() {
                return;
            }
        }.start();
    }

    public void callService1() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "Starting service...", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, HeadService.class));
                //finish();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            TedPermission.with(this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Starting service...", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, HeadService.class));
                //finish();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    public void callService2() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "Starting service...", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, OreoService.class));
                //finish();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            TedPermission.with(this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Starting service...", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, OreoService.class));
                //finish();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Toast.makeText(MainActivity.this, "Permissions \"" + Arrays.toString(permissions) + "\" denied.", Toast.LENGTH_SHORT).show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}