package com.huangzhiwei.turntable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private LuckyPan mLuckyyPan;

    private ImageView mStartBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLuckyyPan = (LuckyPan) findViewById(R.id.id_luckypan);
        mStartBtn = (ImageView) findViewById(R.id.startBtn);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mLuckyyPan.isStart())
                {
                    mLuckyyPan.luckyStart(0);
                    mStartBtn.setImageResource(R.drawable.stop);
                }
                else
                {
                    if(!mLuckyyPan.isShouldEnd())
                    {
                        mLuckyyPan.luckyEnd();
                        mStartBtn.setImageResource(R.drawable.start);
                    }
                }

            }
        });
    }


}
