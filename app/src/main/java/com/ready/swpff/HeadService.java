package com.ready.swpff;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import static io.ready.tools.IdHelper.getId;

public class HeadService extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View mFloatingWidgetView, collapsedView, expandedView;
    private ImageView remove_image_view;
    private Point szWindow = new Point();
    private View removeFloatingWidgetView;
    private FileWatcher fileOb, buildWatcher;

    private TextView project_title, project_path, project_api;
    private ImageView project_image;

    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;

    private boolean isLeft = true;

    private int id = 001;
    private int build_id = 001;

    private CountDownTimer timer;
    private ObjectAnimator objectAnimator;


    public HeadService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        //init WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        getWindowManagerDefaultDisplay();

        //Init LayoutInflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        addRemoveView(inflater);
        addFloatingWidgetView(inflater);
        implementClickListeners();
        implementTouchListenerToFloatingWidgetView();

        register_receiver();
    }

    private View addRemoveView(LayoutInflater inflater) {
        //Inflate the removing view layout we created
        removeFloatingWidgetView = inflater.inflate(R.layout.remove_floating_widget_layout, null);

        //Add the view to the window.
        final WindowManager.LayoutParams paramRemove;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            paramRemove = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            paramRemove = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify the view position
        paramRemove.gravity = Gravity.TOP | Gravity.LEFT;

        //Initially the Removing widget view is not visible, so set visibility to GONE
        removeFloatingWidgetView.setVisibility(View.GONE);
        remove_image_view = removeFloatingWidgetView.findViewById(R.id.remove_img);

        //Add the view to the window
        mWindowManager.addView(removeFloatingWidgetView, paramRemove);
        return remove_image_view;
    }

    /*  Add Floating Widget View to Window Manager  */
    private void addFloatingWidgetView(LayoutInflater inflater) {
        //Inflate the floating view layout we created
        mFloatingWidgetView = inflater.inflate(R.layout.head, null);

        //Add the view to the window.
        final WindowManager.LayoutParams params;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;

        //Initially view will be added to top-left corner, you change x-y coordinates according to your need
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager.addView(mFloatingWidgetView, params);

        //find id of collapsed view layout
        collapsedView = mFloatingWidgetView.findViewById(R.id.collapse_view);

        //find id of the expanded view layout
        expandedView = mFloatingWidgetView.findViewById(R.id.expanded_container);

        project_title = mFloatingWidgetView.findViewById(R.id.floating_widget_title_label);
        project_path = mFloatingWidgetView.findViewById(R.id.floating_widget_detail_label);
        project_image = mFloatingWidgetView.findViewById(R.id.floating_widget_image_view);
        project_api = mFloatingWidgetView.findViewById(R.id.floating_widget_api_info_label);

        collapsedView.setVisibility(View.VISIBLE);
        expandedView.setVisibility(View.GONE);
    }

    private void getWindowManagerDefaultDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
            mWindowManager.getDefaultDisplay().getSize(szWindow);
        else {
            int w = mWindowManager.getDefaultDisplay().getWidth();
            int h = mWindowManager.getDefaultDisplay().getHeight();
            szWindow.set(w, h);
        }
    }

    /*  Implement Touch Listener to Floating Widget Root View  */
    private void implementTouchListenerToFloatingWidgetView() {
        //Drag and move floating view using user's touch action.
        mFloatingWidgetView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {

            long time_start = 0, time_end = 0;

            boolean isLongClick = false;//variable to judge if user click long press
            boolean inBounded = false;//variable to judge if floating view is bounded to remove view
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();
            Runnable runnable_longClick = new Runnable() {
                @Override
                public void run() {
                    //On Floating Widget Long Click

                    //Set isLongClick as true
                    isLongClick = true;

                    //Set remove widget view visibility to VISIBLE
                    removeFloatingWidgetView.setVisibility(View.VISIBLE);

                    onFloatingWidgetLongClick();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Get Floating widget view params
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

                //get the touch location coordinates
                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();

                int x_cord_Destination, y_cord_Destination;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();

                        handler_longClick.postDelayed(runnable_longClick, 600);

                        remove_img_width = remove_image_view.getLayoutParams().width;
                        remove_img_height = remove_image_view.getLayoutParams().height;

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        //remember the initial position.
                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;

                        mFloatingWidgetView.setTranslationX(0);

                        return true;
                    case MotionEvent.ACTION_UP:
                        isLongClick = false;
                        removeFloatingWidgetView.setVisibility(View.GONE);
                        remove_image_view.getLayoutParams().height = remove_img_height;
                        remove_image_view.getLayoutParams().width = remove_img_width;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        //If user drag and drop the floating widget view into remove view then stop the service
                        if (inBounded) {
                            stopSelf();
                            inBounded = false;
                            break;
                        }


                        //Get the difference between initial coordinate and current coordinate
                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        //The check for x_diff <5 && y_diff< 5 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            time_end = System.currentTimeMillis();

                            //Also check the difference between start time and end time should be less than 300ms
                            if ((time_end - time_start) < 300)
                                onFloatingWidgetClick();

                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int barHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (mFloatingWidgetView.getHeight() + barHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (mFloatingWidgetView.getHeight() + barHeight);
                        }

                        layoutParams.y = y_cord_Destination;

                        inBounded = false;

                        //reset position if user drags the floating view
                        resetPosition(x_cord);

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        //If user long click the floating view, update remove view
                        if (isLongClick) {
                            int x_bound_left = szWindow.x / 2 - (int) (remove_img_width * 1.5);
                            int x_bound_right = szWindow.x / 2 + (int) (remove_img_width * 1.5);
                            int y_bound_top = szWindow.y - (int) (remove_img_height * 1.5);

                            //If Floating view comes under Remove View update Window Manager
                            if ((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top) {
                                inBounded = true;

                                int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                                int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight()));

                                if (remove_image_view.getLayoutParams().height == remove_img_height) {
                                    remove_image_view.getLayoutParams().height = (int) (remove_img_height * 1.5);
                                    remove_image_view.getLayoutParams().width = (int) (remove_img_width * 1.5);

                                    WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();
                                    param_remove.x = x_cord_remove;
                                    param_remove.y = y_cord_remove;

                                    mWindowManager.updateViewLayout(removeFloatingWidgetView, param_remove);
                                }

                                layoutParams.x = x_cord_remove + (Math.abs(removeFloatingWidgetView.getWidth() - mFloatingWidgetView.getWidth())) / 2;
                                layoutParams.y = y_cord_remove + (Math.abs(removeFloatingWidgetView.getHeight() - mFloatingWidgetView.getHeight())) / 2;

                                //Update the layout with new X & Y coordinate
                                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                                break;
                            } else {
                                //If Floating window gets out of the Remove view update Remove view again
                                inBounded = false;
                                remove_image_view.getLayoutParams().height = remove_img_height;
                                remove_image_view.getLayoutParams().width = remove_img_width;
                                onFloatingWidgetClick();
                            }

                        }


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                        return true;
                }
                return false;
            }
        });
    }

    private void implementClickListeners() {
        mFloatingWidgetView.findViewById(R.id.close_floating_view).setOnClickListener(this);
        mFloatingWidgetView.findViewById(R.id.close_expanded_view).setOnClickListener(this);
        mFloatingWidgetView.findViewById(R.id.open_activity_button).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_floating_view:
                //close the service and remove the from from the window
                stopSelf();
                break;
            case R.id.close_expanded_view:
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                break;
            case R.id.open_activity_button:
                //open the activity and stop service
                Intent intent = new Intent(HeadService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                //close the service and remove view from the view hierarchy
                stopSelf();
                break;
        }
    }

    /*  on Floating Widget Long Click, increase the size of remove view as it look like taking focus */
    private void onFloatingWidgetLongClick() {
        //Get remove Floating view params
        WindowManager.LayoutParams removeParams = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();

        //get x and y coordinates of remove view
        int x_cord = (szWindow.x - removeFloatingWidgetView.getWidth()) / 2;
        int y_cord = szWindow.y - (removeFloatingWidgetView.getHeight() + getStatusBarHeight());


        removeParams.x = x_cord;
        removeParams.y = y_cord;

        //Update Remove view params
        mWindowManager.updateViewLayout(removeFloatingWidgetView, removeParams);
    }

    /*  Reset position of Floating Widget view on dragging  */
    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);
        } else {
            isLeft = false;
            moveToRight(x_cord_now);
        }

    }


    /*  Method to move the Floating widget view to Left  */
    private void moveToLeft(final int current_x_cord) {
        final int x = szWindow.x - current_x_cord;

        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;

                mParams.x = 0 - (int) (current_x_cord * current_x_cord * step);

                //If you want bounce effect uncomment below line and comment above line
                // mParams.x = 0 - (int) (double) bounceValue(step, x);


                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }

            public void onFinish() {
                mParams.x = 0;

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }.start();

        if (timer != null) {
            timer.cancel();
        }
        if (expandedView.getVisibility() == View.GONE) {
            timer = new CountDownTimer(700, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //
                }

                @Override
                public void onFinish() {
                    objectAnimator = new ObjectAnimator();
                    objectAnimator.setTarget(mFloatingWidgetView);
                    objectAnimator.setPropertyName("translationX");
                    objectAnimator.setFloatValues(0f, -50f);
                    objectAnimator.setDuration(200);
                    objectAnimator.start();
                }
            };
            timer.start();
        }
    }

    /*  Method to move the Floating widget view to Right  */
    private void moveToRight(final int current_x_cord) {

        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;

                mParams.x = (int) (szWindow.x + (current_x_cord * current_x_cord * step) - mFloatingWidgetView.getWidth());

                //If you want bounce effect uncomment below line and comment above line
                //  mParams.x = szWindow.x + (int) (double) bounceValue(step, x_cord_now) - mFloatingWidgetView.getWidth();

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }

            public void onFinish() {
                mParams.x = szWindow.x - mFloatingWidgetView.getWidth();

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }.start();

        if (timer != null) {
            timer.cancel();
        }
        if (expandedView.getVisibility() == View.GONE) {
            timer = new CountDownTimer(700, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //
                }

                @Override
                public void onFinish() {
                    objectAnimator = new ObjectAnimator();
                    objectAnimator.setTarget(mFloatingWidgetView);
                    objectAnimator.setPropertyName("translationX");
                    objectAnimator.setFloatValues(0f, 50f);
                    objectAnimator.setDuration(200);
                    objectAnimator.start();
                }
            };
            timer.start();
        }
    }

    /*  Get Bounce value if you want to make bounce effect to your Floating Widget */
    private double bounceValue(long step, long scale) {
        double value = scale * java.lang.Math.exp(-0.055 * step) * java.lang.Math.cos(0.08 * step);
        return value;
    }


    /*  Detect if the floating view is collapsed or expanded */
    private boolean isViewCollapsed() {
        return mFloatingWidgetView == null || mFloatingWidgetView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }


    /*  return status bar height on basis of device display metrics  */
    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
    }


    /*  Update Floating Widget view coordinates on Configuration change  */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getWindowManagerDefaultDisplay();

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


            if (layoutParams.y + (mFloatingWidgetView.getHeight() + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (mFloatingWidgetView.getHeight() + getStatusBarHeight());
                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x);
            }

        }

    }

    /*  on Floating widget click show expanded view  */
    private void onFloatingWidgetClick() {
        if (isViewCollapsed()) {
            //When user clicks on the image view of the collapsed layout,
            //visibility of the collapsed layout will be changed to "View.GONE"
            //and expanded view will become visible.
            collapsedView.setVisibility(View.GONE);
            expandedView.setVisibility(View.VISIBLE);
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
            mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
        }
    }

    private void register_receiver() {
        fileOb = new FileWatcher(Environment.getExternalStorageDirectory() + "/.sketchware/data/");
        fileOb.setEventListener(new FileWatcher.EventListener() {
            @Override
            public void onCreateEvent(String path) {
                //checkPath(path);
            }

            @Override
            public void onModifyEvent(String path) {
                //checkPath(path);
            }

            @Override
            public void onDeleteEvent(String path) {
                //checkPath(path);
            }

            @Override
            public void onUpdateEvent(String path) {
                //checkPath(path);
            }

            @Override
            public void onAccessEvent(String path) {
                int di = getId(path);
                if (id != di) {
                    id = di;
                    showInfo(di);
                }
            }
        });
        fileOb.startWatching();

        register_build_receiver();
    }

    private void register_build_receiver() {
        buildWatcher = new FileWatcher(Environment.getExternalStorageDirectory() + "/.sketchware/mysc/");
        buildWatcher.setEventListener(new FileWatcher.EventListener() {
            @Override
            public void onCreateEvent(String path) {
                int di = getId(path);
                build_id = di;
                showDetailInfo(di, path);
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

    public void showInfo(final int id) {
        //Toast.makeText(this, "Project changes detected", Toast.LENGTH_SHORT).show();

        /*Handler refresh1 = new Handler(Looper.getMainLooper());
        refresh1.post(new Runnable() {
            public void run()
            {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });*/

        Handler refresh3 = new Handler(Looper.getMainLooper());
        refresh3.post(new Runnable() {
            public void run() {
                if (isViewCollapsed()) {
                    //When user clicks on the image view of the collapsed layout,
                    //visibility of the collapsed layout will be changed to "View.GONE"
                    //and expanded view will become visible.
                    collapsedView.setVisibility(View.GONE);
                    expandedView.setVisibility(View.VISIBLE);
                }

                project_title.setText(getPackage(Environment.getExternalStorageDirectory() + "/.sketchware/mysc/" + id + "/gen"));
                project_path.setText("/.sketchware/data/" + id + "/");
                project_api.setText("Press \"RUN\" to get more info");
                getImageFromUri(new File(Environment.getExternalStorageDirectory() + "/.sketchware/resources/icons/" + id + "/icon.png"), project_image);

                WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        });
    }

    public void showDetailInfo(final int id, final String path) {
        //Toast.makeText(this, "Project changes detected", Toast.LENGTH_SHORT).show();

        /*Handler refresh1 = new Handler(Looper.getMainLooper());
        refresh1.post(new Runnable() {
            public void run()
            {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });*/

        Handler refresh3 = new Handler(Looper.getMainLooper());
        refresh3.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                if (isViewCollapsed()) {
                    //When user clicks on the image view of the collapsed layout,
                    //visibility of the collapsed layout will be changed to "View.GONE"
                    //and expanded view will become visible.
                    collapsedView.setVisibility(View.GONE);
                    expandedView.setVisibility(View.VISIBLE);
                }

                String apk_path = findApk(Environment.getExternalStorageDirectory() + "/.sketchware/mysc/" + id + "/bin/");

                project_title.setText(getAppTitle(Environment.getExternalStorageDirectory() + "/.sketchware/mysc/" + id + "/app/src/main/AndroidManifest.xml"));
                project_path.setText("/.sketchware/data/" + id + "/");
                project_api.setText(
                        "Min. SDK: " + getMinSdk(Environment.getExternalStorageDirectory() + "/.sketchware/mysc/" + id + "/app/build.gradle") + "\n" +
                                "Target SDK: " + getTargetSdk(Environment.getExternalStorageDirectory() + "/.sketchware/mysc/" + id + "/app/build.gradle") + "\n" +
                                "Version code: " + getVersionCode(apk_path) + "\n" +
                                "Version name: " + getVersionName(apk_path)
                );
                getImageFromUri(new File(Environment.getExternalStorageDirectory() + "/.sketchware/resources/icons/" + id + "/icon.png"), project_image);

                //copyContentToClipboard(Environment.getExternalStorageDirectory() + "/.sketchware/data/" + id + "/logic");

                WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*  on destroy remove both view from window manager */

        try {
            if (fileOb != null)
                fileOb.stopWatching();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mFloatingWidgetView != null)
            mWindowManager.removeView(mFloatingWidgetView);

        if (removeFloatingWidgetView != null)
            mWindowManager.removeView(removeFloatingWidgetView);
    }

    public boolean isNumeric(CharSequence u) {
        String s = u.toString();
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    public boolean isNumeric(char u) {
        String s = String.valueOf(u);
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    public void getImageFromUri(File path, final ImageView im) {
        File imgFile = path;

        if (imgFile.exists()) {

            final Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            if (myBitmap != null) {
                im.setImageBitmap(myBitmap);
                Palette.from(myBitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@NonNull Palette palette) {
                        /*android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
                        gd.setColor(palette.getDominantColor(Color.parseColor("#00000000"))); *//* color *//*
                        gd.setCornerRadius(dpToPx(4)); *//* radius *//*
                        gd.setStroke(0, Color.WHITE); *//* stroke heigth and color *//*
                        im.setBackground(gd);

                        if (isColorDark(palette.getDominantColor(Color.parseColor("#00000000")))) {
                            ((ImageView) mFloatingWidgetView.findViewById(R.id.close_expanded_view)).getDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                        } else {
                            ((ImageView) mFloatingWidgetView.findViewById(R.id.close_expanded_view)).getDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
                        }*/
                        im.getBackground().setColorFilter(palette.getDominantColor(Color.parseColor("#00000000")), PorterDuff.Mode.SRC_IN);
                    }
                });
            } else {
                Bitmap empty = BitmapFactory.decodeResource(getResources(), R.drawable.ic_android_black_24dp);
                im.setImageBitmap(empty);
                im.getBackground().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                /*GradientDrawable gd = new GradientDrawable();
                gd.setColor(Color.parseColor("#ffffff")); *//* color *//*
                gd.setCornerRadius(dpToPx(4)); *//* radius *//*
                gd.setStroke(0, Color.WHITE); *//* stroke heigth and color *//*
                im.setBackground(gd);
                ((ImageView) mFloatingWidgetView.findViewById(R.id.close_expanded_view)).getDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);*/
            }
        }
    }

    public String getPackage(String path) throws NullPointerException {
        File file = new File(path);
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() && !f.getName().contains("android") && !f.getName().contains("google");
            }
        });

        StringBuilder builder = new StringBuilder();

        if (files != null) {
            if (files.length == 1) {

                builder.append(files[0]);
                File file2 = new File(files[0].getAbsolutePath());
                File[] files2 = file2.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }
                });

                if (files2 != null) {
                    for (int i = 0; i < files2.length; i++) {
                        if (!files2[i].getName().equals("android") && !files2[i].getName().equals("support") && !files2[i].getName().equals("google") && !files2[i].getName().equals("gms") && !files2[i].getName().equals("firebase")) {
                            builder.append(".");
                            builder.append(files2[i].getName());

                            File file3 = new File(files2[i].getAbsolutePath());
                            File[] files3 = file3.listFiles(new FileFilter() {
                                @Override
                                public boolean accept(File f) {
                                    return f.isDirectory();
                                }
                            });

                            if (files3 != null) {
                                for (int p = 0; p < files3.length; p++) {
                                    builder.append(".");
                                    builder.append(files3[p].getName());
                                }
                            }
                        }
                    }
                }

            } else if (files.length == 2) {

                if (!files[0].getName().equals("com")) {
                    builder.append(files[0]);

                    File file2 = new File(files[0].getAbsolutePath());
                    File[] files2 = file2.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.isDirectory();
                        }
                    });

                    if (files2 != null) {
                        for (int i = 0; i < files2.length; i++) {
                            builder.append(".");
                            builder.append(files2[i].getName());
                        }
                    }
                } else {
                    builder.append(files[1]);

                    File file2 = new File(files[1].getAbsolutePath());
                    File[] files2 = file2.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.isDirectory();
                        }
                    });

                    if (files2 != null) {
                        for (int i = 0; i < files2.length; i++) {
                            builder.append(".");
                            builder.append(files2[i].getName());
                        }
                    }
                }
            }
        }

        if (builder.toString().length() != 0) {
            return builder.toString().substring(builder.toString().indexOf("gen/") + 4, builder.toString().length());
        } else {
            return "Unable to fetch package";
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

    public String getAppTitle(String manifest) {
        File path = new File(manifest);
        String manifest_content;
        if (path.exists()) {
            manifest_content = readFile(manifest);
            if (manifest_content.length() != 0) {
                try {
                    return manifest_content.substring(manifest_content.indexOf("\n\t\tandroid:label=\"") + 18, manifest_content.indexOf("\"\n\t\tandroid:icon="));
                } catch (Exception e) {
                    return "*error*";
                }
            } else {
                return "Waiting for manifest";
            }
        } else {
            return "Waiting for manifest";
        }
    }

    public Integer getMinSdk(String gradle) {
        File path = new File(gradle);
        String gradle_content;
        if (path.exists()) {
            gradle_content = readFile(gradle);
            if (gradle_content.length() != 0) {
                try {
                    return Integer.valueOf(gradle_content.substring(gradle_content.indexOf("minSdkVersion ") + 14, gradle_content.indexOf("\n" +
                            "        targetSdkVersion")));
                } catch (Exception e) {
                    return -1;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public Integer getTargetSdk(String gradle) {
        File path = new File(gradle);
        String gradle_content;
        if (path.exists()) {
            gradle_content = readFile(gradle);
            if (gradle_content.length() != 0) {
                try {
                    return Integer.valueOf(gradle_content.substring(gradle_content.indexOf("targetSdkVersion ") + 17, gradle_content.indexOf("\n" +
                            "        versionCode")));
                } catch (Exception e) {
                    return -1;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public Integer getVersionCode(String path) {
        if(path != null) {
            final PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(path, 0);

            if (info != null) {
                try {
                    return info.versionCode;
                } catch (Exception e) {
                    return -1;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public String getVersionName(String path) {
        if(path != null) {
            final PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(path, 0);

            if (info != null) {
                try {
                    return info.versionName;
                } catch (Exception e) {
                    return "null";
                }
            } else {
                return "null";
            }
        } else {
            return "null";
        }
    }

    public void copyContentToClipboard(String file) {
        File path = new File(file);
        String gradle_content;
        if (path.exists()) {
            gradle_content = readFile(file);
            if (gradle_content.length() != 0) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", gradle_content);
                clipboard.setPrimaryClip(clip);
            } else {
                //
            }
        } else {
            //
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return !(darkness < 0.5);
    }

    public String findApk(String path) {
        File file = new File(path);
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".apk");
            }
        });

        if(files.length != 0) {
            return files[0].getAbsolutePath();
        } else {
            return null;
        }
    }
}