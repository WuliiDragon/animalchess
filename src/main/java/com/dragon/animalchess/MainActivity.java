package com.dragon.animalchess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.waps.AppConnect;


public class MainActivity extends Activity {
    private Button mStartGame;
    private Button mAboutUs;
    private Button mHowToPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        final String rule = "共有红蓝双方，各有八个棋子: 象>狮>虎>豹>狗>狼>猫>鼠 依次可以吃掉小于他的任何牌（鼠可以吃象，但象不可以吃鼠，相同的牌可以消掉）翻拍或吃棋后交换行棋权";
        mAboutUs = (Button) findViewById(R.id.about_us);
        mStartGame = (Button) findViewById(R.id.start_game);
        mHowToPlay = (Button) findViewById(R.id.how_play);
        mAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppConnect.getInstance(MainActivity.this).showOffers(MainActivity.this);
            }
        });
        mStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TurnCardModelActivity.class);
                startActivity(intent);
            }
        });
        mHowToPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("游戏规则")
                        .setMessage(rule).show();
            }
        });
    }
}
