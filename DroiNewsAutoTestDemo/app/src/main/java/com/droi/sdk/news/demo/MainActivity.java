package com.droi.sdk.news.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.droi.sdk.DroiError;
import com.droi.sdk.news.DroiNews;
import com.droi.sdk.news.NewsObject;
import com.droi.sdk.news.OnGetCategroryListern;
import com.droi.sdk.news.OnLoadNewsListListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener,OnTestStateChangeListen {

    public static final String TAG = "DROI_NEWS_DEMO";
    private boolean isDebugMode = false;

    public static final String MEDIA_ID = "a9044b490";//媒体ID
    public static final String SLOT_ID_SMALL_IMAGE = "s3921473a";//广告位ID
    public static final String SLOT_ID_BIG_IMAGE = "s9664cfde";//广告位ID
    public static final String SLOT_ID_TRI_IMAGE = "sa31be3bc";//广告位ID
    public static final String JS_SLOT_ID = "sa64629ab";//广告位ID

    private Button mAutoTest;

    private TextView mResultBoard;

    private String mUrl;

    private Boolean mTopNewsIsOk = false;
    private Boolean mInitIsOk = false;
    private Boolean mPullUpIsOk = false;
    private Boolean mPullDownIsOk = false;
    private Boolean mGetCategroryIsOk = false;

    private int mTopNews = 0;
    private int mInitData = 1;
    private int mPullUp = 2;
    private int mPullDown = 3;
    private int mGetCategrory = 4;

    private OnTestStateChangeListen listen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listen = this;

        mResultBoard = (TextView) findViewById(R.id.result);
        mAutoTest = (Button) findViewById(R.id.one_key_auto_test);
        mAutoTest.setOnClickListener(this);

        HashMap<Integer,String> ids = new HashMap<>();
        ids.put(0,"sd69b9896");
        ids.put(1,"s04e3d9fc");
        ids.put(2,"s04e3d9fc");
        DroiNews.initialize(getApplicationContext(), MEDIA_ID, isDebugMode,ids,"-adroi");
        DroiNews.registerAdSlot(DroiNews.AD_STYLE_SMALL_IMAGE, SLOT_ID_SMALL_IMAGE);
        DroiNews.registerAdSlot(DroiNews.AD_STYLE_BIG_IMAGE, SLOT_ID_BIG_IMAGE);
        DroiNews.registerAdSlot(DroiNews.AD_STYLE_TRI_IMAGE, SLOT_ID_TRI_IMAGE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.one_key_auto_test:{
                toAutoTest();
            }
            break;
        }
    }

    private void toAutoTest() {
        mResultBoard.setText("");
        requestTopNews();
        initData();
        pullUp();
        pullDown();
        getCategrory();
    }

    private void initData(){
        JSONObject data = new JSONObject();
        try {
            data.put("act", 1);
            data.put("size", 7);
            requestNewsList(data, 1);
        } catch (JSONException e) {
        }
    }

    private void pullUp(){
        JSONObject data = new JSONObject();
        try {
            data.put("act", 2);
            data.put("size", 7);
            requestNewsList(data, 2);
        } catch (JSONException e) {
        }
    }

    private void pullDown(){
        JSONObject data = new JSONObject();
        try {
            data.put("act", 3);
            data.put("size", 7);
            requestNewsList(data, 3);
        } catch (JSONException e) {
        }
    }

    private void getCategrory(){
        DroiNews.requestCategrory(new OnGetCategroryListern() {
            @Override
            public void onGetCategroryListern(JSONObject jsonObject) {
                if (jsonObject != null){
                    mGetCategroryIsOk = true;
                    listen.onStateChange(mGetCategrory,mGetCategroryIsOk);
                    Log.e("分类","====>"+jsonObject.toString());
                }
            }
        });
    }

    private void requestTopNews() {
        DroiNews.requestTopNews(new JSONObject(), new OnLoadNewsListListener() {
            @Override
            public void onLoadNewsListResult(final DroiError droiError, List<NewsObject> list) {
                if (droiError.isOk()) {
                    if (list != null) {
                        mTopNewsIsOk = true;
                        listen.onStateChange(mTopNews,mTopNewsIsOk);
                        final StringBuilder builder = new StringBuilder();
                        for (NewsObject obj : list) {
                            builder.append(obj.getContent().toString());
                            Log.e(TAG, "item: " + obj.getContent().toString());
                            builder.append("\n\n");
                        }
                    }
                } else {
                    Log.e(TAG, "request news object failed...");
                }
            }
        });
    }

    private void requestNewsList(JSONObject requestObj, final int action_type) {
        DroiNews.requestNewsList(requestObj, new OnLoadNewsListListener() {
            @Override
            public void onLoadNewsListResult(final DroiError droiError, List<NewsObject> list) {
                if (droiError.isOk()) {
                    if (list != null) {
                        if (action_type == 2){
                            mPullUpIsOk = true;
                            listen.onStateChange(mPullUp,mPullUpIsOk);
                        }
                        if (action_type == 1){
                            mInitIsOk = true;
                            listen.onStateChange(mInitData,mInitIsOk);
                        }
                        final StringBuilder builder = new StringBuilder();
                        for (NewsObject obj : list) {
                            builder.append(obj.getContent().toString());
                            Log.e(TAG, "item: " + obj.getContent().toString());
                            builder.append("\n\n");
                            try {
                                mUrl = obj.getContent().getString("url");
                                Log.e("MainActivity","===========>"+mUrl);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                } else {
                    Log.e(TAG, "request news object failed...");
                }
                mPullDownIsOk = true;
                listen.onStateChange(mPullDown,mPullDownIsOk);
            }
        });
    }

    @Override
    public void onStateChange(int type, Boolean state) {
        final StringBuilder builder = new StringBuilder();

        if (mTopNewsIsOk){
            builder.append("获取置顶新闻接口成功!!!");
            builder.append("\n\n");
        }else {
            builder.append("获取置顶新闻接口失败!!!");
            builder.append("\n\n");
        }
        if (mInitIsOk){
            builder.append("获取初始化新闻接口成功!!!");
            builder.append("\n\n");
        }else {
            builder.append("获取初始化新闻接口失败!!!");
            builder.append("\n\n");
        }
        if (mPullUpIsOk){
            builder.append("获取上拉加载新闻接口成功!!!");
            builder.append("\n\n");
        }else {
            builder.append("获取上拉加载新闻接口失败!!!");
            builder.append("\n\n");
        }
        if (mPullDownIsOk){
            builder.append("获取下拉刷新新闻接口成功!!!");
            builder.append("\n\n");
        }else {
            builder.append("获取下拉刷新新闻接口失败!!!");
            builder.append("\n\n");
        }
        if (mGetCategroryIsOk){
            builder.append("获取新闻分类接口成功!!!");
            builder.append("\n\n");
        }else {
            builder.append("获取新闻分类接口失败!!!");
            builder.append("\n\n");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mResultBoard.setText(builder.toString());
            }
        });
    }
}

