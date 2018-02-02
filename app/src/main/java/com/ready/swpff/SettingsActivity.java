package com.ready.swpff;

import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;

import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ready.tools.ThemeTools;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.setting1_container)
    FrameLayout container1;
    @BindView(R.id.setting1_value)
    Switch value1;
    @BindView(R.id.setting2_container)
    TabLayout container2;

    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeTools.loadTheme(getSharedPreferences("settings", MODE_PRIVATE).getInt("2", 0), this);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        ThemeTools.finishTheme(getSharedPreferences("settings", MODE_PRIVATE).getInt("2", 0), findViewById(android.R.id.content));

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        load();

        container1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                value1.toggle();
            }
        });
        value1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("1", isChecked).apply();
            }
        });
        container2.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                getSharedPreferences("settings", MODE_PRIVATE).edit().putInt("2", tab.getPosition()).apply();

                if(timer != null) {
                    timer.cancel();
                }

                timer = new CountDownTimer(500, 500) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //
                    }

                    @Override
                    public void onFinish() {
                        recreate();
                    }
                }.start();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //
            }
        });
    }

    public void load() {
        value1.setChecked(getSharedPreferences("settings", MODE_PRIVATE).getBoolean("1", false));
        container2.getTabAt(getSharedPreferences("settings", MODE_PRIVATE).getInt("2", 0)).select();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_OK, getIntent());
        super.onDestroy();
    }
}
