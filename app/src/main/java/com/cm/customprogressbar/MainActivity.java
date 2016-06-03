package com.cm.customprogressbar;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CustomProgressBar mProgressBar;
    private CustomProgressBar mProgressBar2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (CustomProgressBar) findViewById(R.id.progressBar);
        mProgressBar2 = (CustomProgressBar) findViewById(R.id.progressBar2);
        mProgressBar.setOnClickListener(this);
        mProgressBar2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ObjectAnimator.ofFloat(mProgressBar,"progress",0f,1.0f).setDuration(5000).start();
        ObjectAnimator.ofFloat(mProgressBar2,"progress",0f,1.0f).setDuration(5000).start();
    }
}
