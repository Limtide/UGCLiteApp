package com.limtide.ugclite.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.limtide.ugclite.R;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ImageView backArrow;
    private TextView profileTitle;
    private TextView profileSubtitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        backArrow = findViewById(R.id.back_arrow);
        profileTitle = findViewById(R.id.profile_title);
        profileSubtitle = findViewById(R.id.profile_subtitle);
    }

    private void setupClickListeners() {
        // 返回按钮点击事件
        backArrow.setOnClickListener(v -> {
            Log.d(TAG, "返回按钮被点击");
            finish(); // 关闭当前Activity，返回上一个页面
        });

        // 这里可以添加其他点击事件
        setupProfileOptionClickListeners();
    }

    private void setupProfileOptionClickListeners() {
        // 设置项点击事件
        int[] optionIds = {
            R.id.option_settings,
            R.id.option_privacy,
            R.id.option_help,
            R.id.option_about,
            R.id.option_logout
        };

        String[] optionNames = {
            "设置", "隐私政策", "帮助中心", "关于我们", "退出登录"
        };

        for (int i = 0; i < optionIds.length; i++) {
            final int index = i;
            findViewById(optionIds[i]).setOnClickListener(v -> {
                Log.d(TAG, optionNames[index] + "被点击");
                // 这里可以添加具体的跳转逻辑
            });
        }
    }
}