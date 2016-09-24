package com.dragon.animalchess;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dragon.animalchess.view.MyView;

import cn.waps.AppConnect;

public class TurnCardModelActivity extends Activity {
    private MyView myView;
    private TextView textView;
    private ImageView mBack;
    private ImageView mPauseSound;
    private ImageView mResumeSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.turn_card);
        AppConnect.getInstance(this);
        LinearLayout adlayout = new LinearLayout(this);
        adlayout.setGravity(Gravity.CENTER_HORIZONTAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AppConnect.getInstance(this).showBannerAd(this, adlayout);
        layoutParams.height=20;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);//设置顶端或低端
        this.addContentView(adlayout, layoutParams);
        myView = (MyView) findViewById(R.id.gameview);
        textView = (TextView) findViewById(R.id.isrun);
        mBack = (ImageView) findViewById(R.id.button_back);
        mPauseSound = (ImageView) findViewById(R.id.pause_sound);
        mResumeSound = (ImageView) findViewById(R.id.resume_sound);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPauseSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myView.pauseSound();
                mPauseSound.setVisibility(View.GONE);
                mResumeSound.setVisibility(View.VISIBLE);
            }
        });
        mResumeSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myView.resumeSound();
                mResumeSound.setVisibility(View.GONE);
                mPauseSound.setVisibility(View.VISIBLE);
            }
        });
        myView.setmListener(new MyView.changeListener() {
            @Override
            public void playerChange(boolean isblue) {
                if (isblue) {
                    textView.setBackgroundResource(R.drawable.blue);
                } else {
                    textView.setBackgroundResource(R.drawable.red);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        myView.pauseSound();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myView.resumeSound();
    }
}
