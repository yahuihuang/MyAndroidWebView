package com.myyhhuang.webview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.myyhhuang.webview.ui.WebViewActivity;
import com.myyhhuang.webview.utils.StatusBarUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 是否開啟了主頁，沒有開啟則會返回主頁
    public static boolean isLaunch = false;
    private AutoCompleteTextView etSearch;
    private RadioButton rbSystem;
    private int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.colorPrimary), 0);
        initView();
        isLaunch = true;
    }

    private void initView() {
        findViewById(R.id.bt_deeplink).setOnClickListener(this);
        findViewById(R.id.bt_openUrl).setOnClickListener(this);
        findViewById(R.id.bt_youtube).setOnClickListener(this);
        findViewById(R.id.bt_movie).setOnClickListener(this);
        findViewById(R.id.bt_upload_photo).setOnClickListener(this);
        findViewById(R.id.bt_call).setOnClickListener(this);
        findViewById(R.id.bt_java_js).setOnClickListener(this);
        findViewById(R.id.bt_toolbar).setOnClickListener(this);

        etSearch = findViewById(R.id.et_search);
        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(String.format("❤版本：v%s", BuildConfig.VERSION_NAME));
        tvVersion.setOnClickListener(this);
        /** 處理鍵盤搜索鍵 */
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    openUrl();
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_openUrl:
                openUrl();
                break;
            case R.id.bt_youtube:// 百度一下
                state = 0;
                String youtubeUrl = "https://www.youtube.com/";
                loadUrl(youtubeUrl, getString(R.string.text_youtube));
                break;
            case R.id.bt_movie:// 網絡視頻
                state = 0;
                String movieUrl = "https://sv.baidu.com/videoui/page/videoland?context=%7B%22nid%22%3A%22sv_5861863042579737844%22%7D&pd=feedtab_h5";
                loadUrl(movieUrl, getString(R.string.text_movie));
                break;
            case R.id.bt_upload_photo:// 上傳圖片
                state = 0;
                String uploadUrl = "file:///android_asset/upload_photo.html";
                loadUrl(uploadUrl, getString(R.string.text_upload_photo));
                break;
            case R.id.bt_call:// 打電話、發短信、發郵件、JS
                state = 1;
                String callUrl = "file:///android_asset/callsms.html";
                loadUrl(callUrl, getString(R.string.text_js));
                break;
            case R.id.bt_java_js://  js與android原生代碼互調
                state = 2;
                String javaJs = "file:///android_asset/java_js.html";
                loadUrl(javaJs, getString(R.string.js_android));
                break;
            case R.id.bt_deeplink:// DeepLink通過網頁跳入App
                state = 0;
                String deepLinkUrl = "file:///android_asset/deeplink.html";
                loadUrl(deepLinkUrl, getString(R.string.deeplink));
                break;
            case R.id.tv_version:
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("感謝");
                builder.setMessage("給我一個星");
                builder.setNegativeButton("已給", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "感謝!", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setPositiveButton("給Star", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        state = 0;
                        loadUrl("https://github.com/yahuihuang/MyAndroidWebView", "WebView");
                    }
                });
                builder.show();
                break;
            default:
                break;
        }
    }

    /**
     * 打開網頁
     */
    private void openUrl() {
        state = 0;
//        String url = ByWebTools.getUrl(etSearch.getText().toString().trim());
        String url = etSearch.getText().toString().trim();
        loadUrl(!TextUtils.isEmpty(url) ? url : "https://github.com/yahuihuang/MyAndroidWebView", "WebView");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionbar_update:
                state = 0;
                loadUrl("https://github.com/yahuihuang/MyAndroidWebView/blob/master/download/MyAndroidWebView.apk", "MyAndroidWebView.apk");
                break;
            case R.id.actionbar_about:
                state = 0;
                loadUrl("https://github.com/yahuihuang/MyAndroidWebView", "WebView");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUrl(String mUrl, String mTitle) {
        WebViewActivity.loadUrl(this, mUrl, mTitle);
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLaunch = false;
    }
}
