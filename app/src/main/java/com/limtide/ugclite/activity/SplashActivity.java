package com.limtide.ugclite.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.limtide.ugclite.MainActivity;
import com.limtide.ugclite.R;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    protected boolean is_offline_mode = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity is being created.");
        setContentView(R.layout.activity_splash);

        new AlertDialog.Builder(this).setTitle("断网连接？").setMessage("是否使用断网(SQLite)模式？").setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        is_offline_mode =true;
                        Toast.makeText(SplashActivity.this, "已切换为断网模式", Toast.LENGTH_SHORT).show();
                        enterMainActiviy();
                    }
                })
                .setNegativeButton("取消",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(SplashActivity.this, "已切换为联网模式", Toast.LENGTH_SHORT).show();
                        enterMainActiviy();
                    }
                }).show();


    }

    private  void enterMainActiviy(){
        if (is_offline_mode){
            Log.d(TAG, "Entering Offline Mode");
        }else {
            Log.d(TAG, "Entering Online Mode");
        }
        //显式Intent跳转
        Intent intent = new Intent(SplashActivity.this,MainActivity.class);
        intent.putExtra("is_offline_mode", is_offline_mode);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity is being destroy.");
    }
}
