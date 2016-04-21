package com.chris.mobileguard.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chris.mobileguard.R;
import com.chris.mobileguard.domain.UrlBean;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class SplashActivity extends AppCompatActivity {

    private static final int LOADMAIN = 1;
    private static final int SHOWUPDATEDIALOG = 2;
    private static final int ERROR = 3;
    private RelativeLayout mRl_root;
    private TextView mTv_versionName;
    private ProgressBar mPb_download;
    private int mVersionCode;
    private String mVersionName;
    private UrlBean mParseJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        System.out.println("app begin");
        //使用动画的时候要用到splash的layout的ID
        mRl_root = (RelativeLayout) findViewById(R.id.rl_splash_root);
        mTv_versionName = (TextView) findViewById(R.id.tv_splash_version_name);
        mPb_download = (ProgressBar) findViewById(R.id.pb_splash_download_progress);

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
//        animationSet.addAnimation(rotateAnimation);
//        animationSet.addAnimation(scaleAnimation);
        mRl_root.startAnimation(animationSet);
        new Thread(new Runnable() {

            private BufferedReader mBufferedReader;

            //注意此处用ctrl+alt+v会自动生成URLConnection，需要改成HttpURLConnection,否则setRequestMethod等方法不能用
            private HttpURLConnection mUrlConnection;

            @Override
            public void run() {
                int errorCode = -1;
                long startTimeMillis =  System.currentTimeMillis();
                try {
                    URL url = new URL("http://192.168.3.3:8080/guardversion.json");
                    mUrlConnection = (HttpURLConnection) url.openConnection();
                    mUrlConnection.setReadTimeout(5000);
                    mUrlConnection.setConnectTimeout(5000);
                    mUrlConnection.setRequestMethod("GET");
                    int responseCode = mUrlConnection.getResponseCode();
                    System.out.println("responseCode:"+responseCode);
                    if(responseCode==200){
                        InputStream inputStream = mUrlConnection.getInputStream();
                        mBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String line = mBufferedReader.readLine();
                        //StringBuilder：线程非安全的,StringBuffer：线程安全的
                        StringBuilder json = new StringBuilder();
                        while(line!=null){
                            json.append(line);
                            line = mBufferedReader.readLine();
                        }
                        mParseJson = parseJson(json);
                        System.out.println("parseJson:"+mParseJson);

                    }else{
                        errorCode = 404;//资源找不到
                    }

                } catch (MalformedURLException e) {//无法建立连接
                    errorCode = 4002;
                    System.out.println("no permission");
                    e.printStackTrace();
                } catch (IOException e) {//无法获取流
                    errorCode = 4001;
                    e.printStackTrace();
                } catch (JSONException e) {//json格式错误
                    errorCode = 4003;
                    e.printStackTrace();
                }finally {
                    Message message = Message.obtain();
                    if(errorCode == -1){    //成功获取服务器版本更新信息
                        message.what = isNewVersion(mParseJson);
                    }else {
                        message.what = ERROR;//错误统一代号
                        message.arg1 = errorCode;//具体错误代号
                    }
                    long endTimeMillis = System.currentTimeMillis();
                    if(endTimeMillis - startTimeMillis < 3000){
                        //调用Thread.sleep时可能会报InterruptedException异常，而SystemClock.sleep方法则不会
                        SystemClock.sleep(3000 - (endTimeMillis - startTimeMillis));//保证休眠时间够3秒
                    }
                    try {
                        handler.sendMessage(message);
                        if(mBufferedReader == null || mUrlConnection == null){
                            return;
                        }
                        mBufferedReader.close();
                        mUrlConnection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            
        }).start();
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LOADMAIN:          //版本为最新，直接跳转到主界面
                    loadMain();
                    break;
                case ERROR:
                    System.out.println("获取服务器版本更新信息失败");
                    loadMain();
                    break;
                case SHOWUPDATEDIALOG:
                    showUpdateDialog();
                default:
                    break;
            }
        }
    };

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                loadMain();
            }
        }).setTitle("版本更新提醒")
                .setMessage("是否更新至最新版本？新版本新增以下特性："+mParseJson.getDesc())
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("更新apk");
                        downLoadNewApk();
                    }

                    
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loadMain();
                    }
                }).show();
    }

    private void downLoadNewApk() {
        //这里用了Xutils工具包来实现下载
        HttpUtils httpUtils = new HttpUtils();
        System.out.println("新版本路径："+mParseJson.getUrl());
        File file = new File("/mnt/sdcard/xx.apk");
        file.delete();
        httpUtils.download(mParseJson.getUrl(), "/mnt/sdcard/xx.apk", new RequestCallBack<File>() {
            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                mPb_download.setVisibility(View.VISIBLE);
                mPb_download.setMax((int) total);
                mPb_download.setProgress((int) current);
                super.onLoading(total, current, isUploading);
            }

            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                Toast.makeText(getApplicationContext(),"新版本下载完成",Toast.LENGTH_SHORT).show();
                installApk();
                mPb_download.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(getApplicationContext(),"下载新版本失败",Toast.LENGTH_SHORT).show();
                loadMain();
                mPb_download.setVisibility(View.GONE);
            }
        });
    }

    private void installApk() {
        /*从官方上层packages/apps/PackageInstaller的配置文件中可知安装apk的
        <activity android:name=".PackageInstallerActivity"
                android:configChanges="orientation|keyboardHidden"
                android:theme="@style/TallTitleBarTheme">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:scheme="content" />
                <data android:scheme="file" />
                <data android:mimeType="application/vnd.android.package-archive" />
            </intent-filter>
        </activity>*/
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        Uri data = Uri.fromFile(new File("/mnt/sdcard/xx.apk"));
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(data,type);
        //开启安装应用的系统activity。当用户点击取消，取消安装时会调用onActivityResult()
        //requestCode: If >= 0, this code will be returned in onActivityResult() when the activity exits.
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //如果用户取消更新apk，那么直接进入主界面
        loadMain();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadMain() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private int isNewVersion(UrlBean parseJson) {
        int serverCode = parseJson.getVersionCode();
        if(serverCode == mVersionCode){
            return LOADMAIN;
        }else{
            return SHOWUPDATEDIALOG;
        }
    }

    private UrlBean parseJson(StringBuilder jsonString) throws JSONException {
        UrlBean urlBean = new UrlBean();
        JSONObject jsonObject;
        // {"version":"2","url":"http://10.0.2.2:8080/xxx.apk","desc":"增加了防病毒功能"}
        jsonObject = new JSONObject(jsonString+"");
        int versionCode = jsonObject.getInt("version");
        String url = jsonObject.getString("url");
        String desc = jsonObject.getString("desc");
        urlBean.setDesc(desc);
        urlBean.setUrl(url);
        urlBean.setVersionCode(versionCode);
        return urlBean;
    }
}
