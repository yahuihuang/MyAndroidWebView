package com.myyhhuang.webview.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.myyhhuang.webview.R;

import java.util.List;

/**
 * 測試DeepLink打開頁面
 */
public class DeepLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link);
        TextView textView = (TextView) findViewById(R.id.tv_deeplink);
        getDataFromBrowser(textView);
    }

    /**
     * 從deep link中獲取數據
     * 'scheme://host/path?傳過來的數據' 示例：will://link/testId?type=1&id=345
     */
    private void getDataFromBrowser(TextView textView) {
        Uri data = getIntent().getData();
        try {
            String scheme = data.getScheme();
            String host = data.getHost();
            String path = data.getPath();
            // 從網頁傳過來的數據
            String query = data.getQuery();
            String text = "scheme: " + scheme + "\n" + "host: " + host + "\n" + "path: " + path + "\n" + "query: " + query;
            Log.e("ScrollingActivity", text);
            textView.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}