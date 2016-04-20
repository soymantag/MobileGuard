package com.chris.mobileguard.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chris.mobileguard.R;

public class SplashActivity extends AppCompatActivity {

    private RelativeLayout mRl_root;
    private TextView mTv_versionName;
    private ProgressBar mPb_download;
    private int mVersionCode;
    private String mVersionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //使用动画的时候要用到splash的layout的ID
        mRl_root = (RelativeLayout) findViewById(R.id.rl_splash_root);
        mTv_versionName = (TextView) findViewById(R.id.tv_splash_version_name);
        mPb_download = (ProgressBar) findViewById(R.id.pb_splashActivity);

        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            mVersionCode = packageInfo.versionCode;//版本号：默认1   见build.gradle(Module:app)
            mVersionName = packageInfo.versionName;//版本名字：默认1.0  见build.gradle(Module:app)
            mTv_versionName.setText(mVersionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //透明度变化的动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(3000);
        alphaAnimation.setFillAfter(true);
        //旋转变化的动画
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, AlphaAnimation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(3000);
        rotateAnimation.setFillAfter(true);
        //比例动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(3000);
        scaleAnimation.setFillAfter(true);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
//            animationSet.addAnimation(rotateAnimation);
//            animationSet.addAnimation(scaleAnimation);
//        mRl_root.startAnimation(animationSet);

    }
}
