package com.example.maapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class splash_screen extends AppCompatActivity {
    ImageView imageViewSplashScreen;
    ConstraintLayout constraintLayoutSplashScreen;
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Log.d("splashScreen", "started");

        imageViewSplashScreen = findViewById(R.id.imageViewSplashScreen);
        constraintLayoutSplashScreen = findViewById(R.id.layoutSplashScreen);

        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        Animation zoom = AnimationUtils.loadAnimation(this, R.anim.zoomout);
        constraintLayoutSplashScreen.startAnimation(bounce);
        imageViewSplashScreen.startAnimation(zoom);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent i = new Intent(splash_screen.this, MainActivity.class);
                i.putExtra("splashScreen", "finished");
                startActivity(i);

                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}