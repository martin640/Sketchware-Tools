package com.ready.swpff;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ready.tools.DestinyTools;
import io.ready.tools.ServiceTools;
import io.ready.tools.Updater;
import io.ready.tools.ViewHelper;

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

    @BindView(R.id.container_project_info)
    FrameLayout project_info;
    @BindView(R.id.container_pixel_helper)
    FrameLayout pixel_helper;

    ProgressDialog progressDialog;

    Updater updater;
    DisplayMetrics displayMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

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

        updater = new Updater(this, BuildConfig.VERSION_CODE);
        updater.check();

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

        if (!DestinyTools.isWideScreen(displayMetrics.heightPixels + DestinyTools.getSystemBarsHeight(MainActivity.this), displayMetrics.widthPixels)) {
            try {
                TextView d = findViewById(R.id.textView4);
                d.setText("We detected that your aspect ration is normal, so you will not need this tool.");
                ViewHelper.enableView(pixel_helper, false);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ViewHelper.enableView(pixel_helper, true);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        CountDownTimer countDownTimer = new CountDownTimer(Integer.MAX_VALUE, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (ServiceTools.serviceRunning(MainActivity.this, HeadService.class)) {
                    start_service1.setEnabled(false);
                    stop_service1.setEnabled(true);
                    indicator1.getDrawable().setColorFilter(/*GREEN*/Color.parseColor("#66BB6A"), PorterDuff.Mode.SRC_IN);
                } else {
                    start_service1.setEnabled(true);
                    stop_service1.setEnabled(false);
                    indicator1.getDrawable().setColorFilter(/*RED*/Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
                }

                if (DestinyTools.isWideScreen(displayMetrics.heightPixels + DestinyTools.getSystemBarsHeight(MainActivity.this), displayMetrics.widthPixels)) {
                    if (ServiceTools.serviceRunning(MainActivity.this, OreoService.class)) {
                        start_service2.setEnabled(false);
                        stop_service2.setEnabled(true);
                        indicator2.getDrawable().setColorFilter(/*GREEN*/Color.parseColor("#66BB6A"), PorterDuff.Mode.SRC_IN);
                    } else {
                        start_service2.setEnabled(true);
                        stop_service2.setEnabled(false);
                        indicator2.getDrawable().setColorFilter(/*RED*/Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
                    }
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
                new ProjectInfoStarter().execute("");
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
                new ProjectInfoStarter().execute("");
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
                new PixelHelperStarter().execute("");
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
                new PixelHelperStarter().execute("");
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.item_stop_all:
                stopService(new Intent(MainActivity.this, HeadService.class));
                stopService(new Intent(MainActivity.this, OreoService.class));
                Toast.makeText(this, "Services are killed.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.item_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onReadyCreated() {
        Log.d(getActivityTag(), getVersionName());
        super.onReadyCreated();
    }*/

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private class ProjectInfoStarter extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Starting service...", Toast.LENGTH_SHORT).show();
                    startService(new Intent(MainActivity.this, HeadService.class));
                }
            });
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            });
        }

        @Override
        protected void onPreExecute() {
            /*Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Starting service...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });*/
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Starting service...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });
        }
    }

    private class PixelHelperStarter extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Starting service...", Toast.LENGTH_SHORT).show();
                    startService(new Intent(MainActivity.this, OreoService.class));
                }
            });
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            });
        }

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Starting service...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });
        }
    }
}