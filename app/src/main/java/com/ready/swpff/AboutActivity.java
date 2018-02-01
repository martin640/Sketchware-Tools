package com.ready.swpff;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ready.tools.AppManagerTools;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.button5)
    Button github;
    @BindView(R.id.imageview1)
    ImageView imageView;

    String git_url = "https://github.com/gocrafterlp/Sketchware-Tools";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        try {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e) {
            e.printStackTrace();
        }

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(git_url));
                startActivity(i);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppManagerTools.isAppInstalled("com.besome.sketch", AboutActivity.this)) {
                    final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Sketchware is installed", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Open", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.besome.sketch");
                            if (launchIntent != null) {
                                startActivity(launchIntent);//null pointer check in case package name was not found
                            }
                        }
                    });
                    snackbar.show();
                } else {
                    final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Sketchware missing.", Snackbar.LENGTH_LONG);
                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
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
}