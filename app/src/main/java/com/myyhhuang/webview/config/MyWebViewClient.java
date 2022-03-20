package com.myyhhuang.webview.config;

import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;

/**
 * 監聽網頁鏈接:
 * - 根據標識:打電話、發短信、發郵件
 * - 進度條的顯示
 * - 添加javascript監聽
 * - 喚起京東，支付寶，微信原生App
 */
public class MyWebViewClient extends WebViewClient {

    private IWebPageView mIWebPageView;

    public MyWebViewClient(IWebPageView mIWebPageView) {
        this.mIWebPageView = mIWebPageView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.e("Grace", "----url:" + url);
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return mIWebPageView.isOpenThirdApp(url);
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        // html加載完成之後，添加監聽圖片的點擊js函數
        mIWebPageView.onPageFinished(view, url);
        super.onPageFinished(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        //6.0以下執行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }
        String mErrorUrl = "file:///android_asset/404_error.html";
        view.loadUrl(mErrorUrl);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
//        WebTools.handleReceivedHttpError(view, errorResponse);
        // 這個方法在 android 6.0才出現
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int statusCode = errorResponse.getStatusCode();
            if (404 == statusCode || 500 == statusCode) {
                String mErrorUrl = "file:///android_asset/404_error.html";
                view.loadUrl(mErrorUrl);
            }
        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (request.isForMainFrame()) {//是否是為 main frame創建
                String mErrorUrl = "file:///android_asset/404_error.html";
                view.loadUrl(mErrorUrl);
            }
        }
    }

    /**
     * 解决google play上线 WebViewClient.onReceivedSslError问题
     */
    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("SSL認證失敗，是否繼續訪問？");
        builder.setPositiveButton("繼續", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 视频全屏播放按返回页面被放大的问题
    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        if (newScale - oldScale > 7) {
            view.setInitialScale((int) (oldScale / newScale * 100)); //異常放大，縮回去。
        }
    }

}
