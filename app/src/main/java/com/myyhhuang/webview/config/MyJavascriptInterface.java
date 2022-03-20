package com.myyhhuang.webview.config;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.myyhhuang.webview.utils.WebTools;

public class MyJavascriptInterface {

    private Context context;

    public MyJavascriptInterface(Context context) {
        this.context = context;
    }

    /**
     * 前端代碼嵌入js：
     * imageClick 名應和js函數方法名一致
     *
     * @param src 圖片的鏈接
     */
    @JavascriptInterface
    public void imageClick(String src) {
        Log.e("imageClick", "----點擊了圖片");
        Log.e("---src", src);
        WebTools.showToast(src);
    }

    /**
     * 前端代碼嵌入js
     * 遍歷<li>節點
     *
     * @param type    <li>節點下type屬性的值
     * @param item_pk item_pk屬性的值
     */
    @JavascriptInterface
    public void textClick(String type, String item_pk) {
        if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(item_pk)) {
            Log.e("textClick", "----點擊了文字");
            Log.e("type", type);
            Log.e("item_pk", item_pk);
            WebTools.showToast("type: " + type + ", item_pk:" + item_pk);
        }
    }

    /**
     * 網頁使用的js，方法無參數
     */
    @JavascriptInterface
    public void startFunction() {
        Log.e("startFunction", "----無參");
        WebTools.showToast("無參方法");
    }

    /**
     * 網頁使用的js，方法有參數，且參數名為data
     *
     * @param data 網頁js裡的參數名
     */
    @JavascriptInterface
    public void startFunction(String data) {
        Log.e("startFunction", "----有參方法: " + data);
        WebTools.showToast("----有參方法: " + data);
    }

    /**
     * 獲取網頁源代碼
     */
    @JavascriptInterface
    public void showSource(String html) {
        Log.e("showSourceCode", html);
    }
}