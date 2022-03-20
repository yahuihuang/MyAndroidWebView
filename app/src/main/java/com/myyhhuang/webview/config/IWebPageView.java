package com.myyhhuang.webview.config;

import android.content.Intent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;

public interface IWebPageView {

    /**
     * 顯示webview
     */
    void showWebView();

    /**
     * 隱藏webview
     */
    void hindWebView();

    /**
     * 進度條變化時調用
     *
     * @param newProgress 進度0-100
     */
    void startProgress(int newProgress);

    /**
     * 添加視頻全屏view
     */
    void fullViewAddView(View view);

    /**
     * 顯示全屏view
     */
    void showVideoFullView();

    /**
     * 隱藏全屏view
     */
    void hindVideoFullView();

    /**
     * 設置橫豎屏
     */
    void setRequestedOrientation(int screenOrientationPortrait);

    /**
     * 得到全屏view
     */
    FrameLayout getVideoFullView();

    /**
     * 加載視頻進度條
     */
    View getVideoLoadingProgressView();

    /**
     * 返回標題處理
     */
    void onReceivedTitle(WebView view, String title);

    /**
     * 上傳圖片打開文件夾
     */
    void startFileChooserForResult(Intent intent, int requestCode);

    /**
     * 頁面加載結束，添加js監聽等
     */
    void onPageFinished(WebView view, String url);

    /**
     * 是否處理打開三方app
     * @param url
     */
    boolean isOpenThirdApp(String url);
}