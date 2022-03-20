package com.myyhhuang.webview.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myyhhuang.webview.MainActivity;
import com.myyhhuang.webview.R;
import com.myyhhuang.webview.config.FullscreenHolder;
import com.myyhhuang.webview.config.IWebPageView;
import com.myyhhuang.webview.config.MyJavascriptInterface;
import com.myyhhuang.webview.config.MyWebChromeClient;
import com.myyhhuang.webview.config.MyWebViewClient;
import com.myyhhuang.webview.config.WebProgress;
import com.myyhhuang.webview.utils.CheckNetwork;
import com.myyhhuang.webview.utils.StatusBarUtil;
import com.myyhhuang.webview.utils.WebTools;

/**
 * 網頁可以處理:
 * 點擊相應控件：
 * - 撥打電話、發送短信、發送郵件
 * - 上傳圖片(版本兼容)
 * - 全屏播放網絡視頻
 * - 進度條顯示
 * - 返回網頁上一層、顯示網頁標題
 * JS交互部分：
 * - 前端代碼嵌入js(缺乏靈活性)
 * - 網頁自帶js跳轉
 * 被作為第三方瀏覽器打開
 */
public class WebViewActivity extends AppCompatActivity implements IWebPageView {

    // 進度條
    private WebProgress mProgressBar;
    // 全屏時視頻加載view
    private FrameLayout videoFullView;
    // 加載視頻相關
    private MyWebChromeClient mWebChromeClient;
    // 網頁鏈接
    private String mUrl;
    // 可滾動的title 使用簡單 沒有漸變效果，文字兩旁有陰影
    private Toolbar mTitleToolBar;
    private WebView webView;
    private TextView tvGunTitle;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        getIntentData();
        initTitle();
        initWebView();
        handleLoadUrl();
        getDataFromBrowser(getIntent());
    }

    private void handleLoadUrl() {
        if (!TextUtils.isEmpty(mUrl) && mUrl.endsWith("mp4") && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            webView.loadData(WebTools.getVideoHtmlBody(mTitle, mUrl), "text/html", "UTF-8");
        } else {
            webView.loadUrl(mUrl);
        }
    }

    private void getIntentData() {
        mUrl = getIntent().getStringExtra("mUrl");
        mTitle = getIntent().getStringExtra("mTitle");
    }


    private void initTitle() {
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.colorPrimary), 0);
        RelativeLayout rl_web_container = findViewById(R.id.rl_web_container);
        webView = new WebView(this);
        mProgressBar = new WebProgress(this);
        mProgressBar.setVisibility(View.GONE);
        rl_web_container.addView(webView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        rl_web_container.addView(mProgressBar);
        mProgressBar.setColor(ContextCompat.getColor(this, R.color.coloRed));
        mProgressBar.show();
        initToolBar();
    }

    private void initToolBar() {
        mTitleToolBar = findViewById(R.id.title_tool_bar);
        tvGunTitle = findViewById(R.id.tv_gun_title);
        setSupportActionBar(mTitleToolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTitleToolBar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.actionbar_more));
        tvGunTitle.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvGunTitle.setSelected(true);
            }
        }, 1900);
        setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 返回鍵
                handleFinish();
                break;
            case R.id.actionbar_share:// 分享到
                String shareText = webView.getTitle() + webView.getUrl();
                WebTools.share(WebViewActivity.this, shareText);
                break;
            case R.id.actionbar_cope:// 複製鏈接
                WebTools.copy(webView.getUrl());
                Toast.makeText(this, "複製成功", Toast.LENGTH_LONG).show();
                break;
            case R.id.actionbar_open:// 打開鏈接
                WebTools.openLink(WebViewActivity.this, webView.getUrl());
                break;
            case R.id.actionbar_webview_refresh:// 刷新頁面
                webView.reload();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView() {
        WebSettings ws = webView.getSettings();
        // 保存表單數據
        ws.setSaveFormData(true);
        // 是否應該支持使用其屏幕縮放控件和手勢縮放
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        // 啟動應用緩存
        ws.setAppCacheEnabled(true);
        // 設置緩存模式
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        // setDefaultZoom  api19被棄用
        // 網頁內容的寬度自適應屏幕
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        // 網頁縮放至100，一般的網頁達到屏幕寬度效果，個別除外
//        webView.setInitialScale(100);
        // 關掉下滑弧形陰影
//        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        // 告訴WebView啟用JavaScript執行。默認的是false。
        ws.setJavaScriptEnabled(true);
        //  頁面加載好以後，再放開圖片
        ws.setBlockNetworkImage(false);
        // 使用localStorage則必須打開
        ws.setDomStorageEnabled(true);
        // 排版適應屏幕
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        } else {
            ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        }
        // WebView是否新窗口打開(加了後可能打不開網頁)
//        ws.setSupportMultipleWindows(true);

        // webview從5.0開始默認不允許混合模式,https中不能加載http資源,需要設置開啟。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        /** 設置字體默認縮放大小(改變網頁字體大小,setTextSize  api14被棄用)*/
        ws.setTextZoom(100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.setScrollBarSize(WebTools.dp2px(this, 3));
        }

        mWebChromeClient = new MyWebChromeClient(this);
        webView.setWebChromeClient(mWebChromeClient);
        // 與js交互
        webView.addJavascriptInterface(new MyJavascriptInterface(this), "injectedObject");
        webView.setWebViewClient(new MyWebViewClient(this));
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return handleLongImage();
            }
        });

    }

    @Override
    public void showWebView() {
        webView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hindWebView() {
        webView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void fullViewAddView(View view) {
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        videoFullView = new FullscreenHolder(WebViewActivity.this);
        videoFullView.addView(view);
        decor.addView(videoFullView);
    }

    @Override
    public void showVideoFullView() {
        videoFullView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hindVideoFullView() {
        videoFullView.setVisibility(View.GONE);
    }

    @Override
    public void startProgress(int newProgress) {
        mProgressBar.setWebProgress(newProgress);
    }

    public void setTitle(String mTitle) {
        tvGunTitle.setText(mTitle);
    }

    /**
     * android與js交互：
     * 前端注入js代碼：不能加重複的節點，不然會覆蓋
     * 前端調用js代碼
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        if (!CheckNetwork.isNetworkConnected(this)) {
            mProgressBar.hide();
        }
        loadImageClickJS();
        loadTextClickJS();
        loadCallJS();
        loadWebsiteSourceCodeJS();
    }

    /**
     * 處理是否喚起三方app
     */
    @Override
    public boolean isOpenThirdApp(String url) {
        return WebTools.handleThirdApp(this, url);
    }

    /**
     * 前端注入JS：
     * 這段js函數的功能就是，遍歷所有的img節點，並添加onclick函數，函數的功能是在圖片點擊的時候調用本地java接口並傳遞url過去
     */
    private void loadImageClickJS() {
        loadJs("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\");" +
                "for(var i=0;i<objs.length;i++)" +
                "{" +
                "objs[i].onclick=function(){window.injectedObject.imageClick(this.getAttribute(\"src\"));}" +
                "}" +
                "})()");
    }

    /**
     * 前端注入JS：
     * 遍歷所有的<li>節點,將節點裡的屬性傳遞過去(屬性自定義,用於頁面跳轉)
     */
    private void loadTextClickJS() {
        loadJs("javascript:(function(){" +
                "var objs =document.getElementsByTagName(\"li\");" +
                "for(var i=0;i<objs.length;i++)" +
                "{" +
                "objs[i].onclick=function(){" +
                "window.injectedObject.textClick(this.getAttribute(\"type\"),this.getAttribute(\"item_pk\"));}" +
                "}" +
                "})()");
    }

    /**
     * 傳應用內的數據給html，方便html處理
     */
    private void loadCallJS() {
        // 无参数调用
        loadJs("javascript:javacalljs()");
        // 传递参数调用
        loadJs("javascript:javacalljswithargs('" + "android傳入到網頁裡的數據，有參數" + "')");
    }

    /**
     * get website source code
     * 獲取網頁源碼
     */
    private void loadWebsiteSourceCodeJS() {
        loadJs("javascript:window.injectedObject.showSource(document.getElementsByTagName('html')[0].innerHTML);");
    }

    /**
     * 全屏時按返加鍵執行退出全屏方法
     */
    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public FrameLayout getVideoFullView() {
        return videoFullView;
    }

    @Override
    public View getVideoLoadingProgressView() {
        return LayoutInflater.from(this).inflate(R.layout.video_loading_progress, null);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        setTitle(title);
    }

    @Override
    public void startFileChooserForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    /**
     * 上傳圖片之後的回調
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == MyWebChromeClient.FILECHOOSER_RESULTCODE) {
            mWebChromeClient.mUploadMessage(intent, resultCode);
        } else if (requestCode == MyWebChromeClient.FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            mWebChromeClient.mUploadMessageForAndroid5(intent, resultCode);
        }
    }


    /**
     * 使用singleTask啟動模式的Activity在系統中只會存在一個實例。
     * 如果這個實例已經存在，intent就會通過onNewIntent傳遞到這個Activity。
     * 否則新的Activity實例被創建。
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getDataFromBrowser(intent);
    }

    /**
     * 作為三方瀏覽器打開傳過來的值
     * Scheme: https
     * host: www.jianshu.com
     * path: /p/1cbaf784c29c
     * url = scheme + "://" + host + path;
     */
    private void getDataFromBrowser(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            try {
                String scheme = data.getScheme();
                String host = data.getHost();
                String path = data.getPath();
                String text = "Scheme: " + scheme + "\n" + "host: " + host + "\n" + "path: " + path;
                Log.e("data", text);
                String url = scheme + "://" + host + path;
                webView.loadUrl(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 直接通過三方瀏覽器打開時，回退到首頁
     */
    public void handleFinish() {
        supportFinishAfterTransition();
        if (!MainActivity.isLaunch) {
            MainActivity.start(this);
        }
    }

    /**
     * 4.4以上可用 evaluateJavascript 效率高
     */
    private void loadJs(String jsString) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(jsString, null);
        } else {
            webView.loadUrl(jsString);
        }
    }

    /**
     * 長按圖片事件處理
     */
    private boolean handleLongImage() {
        final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        // 如果是圖片類型或者是帶有圖片鏈接的類型
        if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            // 彈出保存圖片的對話框
            new AlertDialog.Builder(WebViewActivity.this)
                    .setItems(new String[]{"查看大圖", "保存圖片到相冊"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String picUrl = hitTestResult.getExtra();
                            //获取图片
                            Log.e("picUrl", picUrl);
                            switch (which) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                default:
                                    break;
                            }
                        }
                    })
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //全屏播放退出全屏
            if (mWebChromeClient.inCustomView()) {
                hideCustomView();
                return true;

                //返回網頁上一頁
            } else if (webView.canGoBack()) {
                webView.goBack();
                return true;

                //退出網頁
            } else {
                handleFinish();
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        // 支付寶網頁版在打開文章詳情之後,無法點擊按鈕下一步
        webView.resumeTimers();
        // 設置為橫屏
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onDestroy() {
        if (videoFullView != null) {
            videoFullView.removeAllViews();
            videoFullView = null;
        }
        if (webView != null) {
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);
            }
            webView.removeAllViews();
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    /**
     * 打開網頁:
     *
     * @param mContext 上下文
     * @param mUrl     要加載的網頁url
     * @param mTitle   標題
     */
    public static void loadUrl(Context mContext, String mUrl, String mTitle) {
        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra("mUrl", mUrl);
        intent.putExtra("mTitle", mTitle == null ? "加載中..." : mTitle);
        mContext.startActivity(intent);
    }
}
