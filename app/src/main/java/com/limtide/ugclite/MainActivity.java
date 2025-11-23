package com.limtide.ugclite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.limtide.ugclite.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
//    private static final String KEY_COUNTER = "counter_value";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        boolean isOffline = intent.getBooleanExtra("is_offline_mode", false);
        if (isOffline) {
            // 断网模式逻辑：比如初始化本地数据库
            Toast.makeText(this, "当前是：断网模式", Toast.LENGTH_SHORT).show();
        } else {
            // 联网模式逻辑：比如初始化网络请求库
            Toast.makeText(this, "当前是：联网模式", Toast.LENGTH_SHORT).show();
        }
        // 使用ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 设置点击事件监听器
        setupClickListeners();
    }

    private void setupClickListeners() {
        // 菜单按钮点击事件
        binding.menuButton.setOnClickListener(v -> {
            Log.d(TAG, "菜单按钮被点击");
        });

        // 搜索按钮点击事件
        binding.searchButton.setOnClickListener(v -> {
            Log.d(TAG, "搜索按钮被点击");
        });

        // 拍摄按钮点击事件
        binding.captureButton.setOnClickListener(v -> {
            Log.d(TAG, "拍摄按钮被点击");
        });

        // 底部导航栏点击事件
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // 首页标签点击
        binding.homeTab.setOnClickListener(v -> {
            Log.d(TAG, "首页标签被点击");
        });

        // 朋友标签点击
        binding.friendsTab.setOnClickListener(v -> {
            Log.d(TAG, "朋友标签被点击");
        });

        // 消息标签点击
        binding.messageTab.setOnClickListener(v -> {
            Log.d(TAG, "消息标签被点击");
        });

        // 我标签点击
        binding.profileTab.setOnClickListener(v -> {
            Log.d(TAG, "我标签被点击");
        });
    }
}