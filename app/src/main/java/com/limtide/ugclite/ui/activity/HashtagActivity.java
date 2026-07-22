package com.limtide.ugclite.ui.activity;

import android.os.Bundle;

import com.limtide.ugclite.databinding.ActivityHashtagBinding;
import com.limtide.ugclite.utils.AuthenticationGate;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 话题词页面 - 简洁设计
 */
public class HashtagActivity extends AppCompatActivity {

    // ViewBinding
    private ActivityHashtagBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthenticationGate.requireAuthenticated(this)) {
            return;
        }
        binding = ActivityHashtagBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
        handleIntent();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthenticationGate.requireAuthenticated(this)) {
            return;
        }
    }
    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void handleIntent() {
        String hashtag = getIntent().getStringExtra("hashtag");
        if (hashtag != null) {
            // 去掉#号，如果有重复的话
            String cleanHashtag = hashtag.replace("#", "").trim();
            binding.titleText.setText("#" + cleanHashtag);
            binding.hashtagText.setText("这是话题 " + cleanHashtag + " 的内容页面\n\n这里可以展示与该话题相关的所有作品和讨论。");
        }
    }
}