package com.myyhhuang.webview;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;
import androidx.multidex.MultiDex;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import java.util.HashMap;

public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        initX5();
    }

    public static App getInstance() {
        return app;
    }

    private void initX5() {
        // 非wifi條件下允許下載X5內核
        QbSdk.setDownloadWithoutWifi(true);
        //蒐集本地tbs內核信息並上報服務器，服務器返回結果決定使用哪個內核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回調，為true表示x5內核加載成功，否則表示x5內核加載失敗，會自動切換到系統內核。
                if (!arg0) {
                    Log.e("ByWebView", "x5內核加載失敗，自動切換到系統內核");
                }
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5內核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);

        // 在調用TBS初始化、創建WebView之前進行如下配置
        HashMap map = new HashMap();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
    }

    /**
     * 方法數超64k 解決 https://developer.android.com/studio/build/multidex?hl=zh-cn
     * 繼承 MultiDexApplication 或 實現此方法。
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        initWebView();
        MultiDex.install(this);
    }

    /**
     * Android P針對 WebView在不同進程下無法訪問非自己進程中的webview目錄
     * fix Using WebView from more than one process at once with the same data directory is not supported
     */
    private void initWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName();
            String packageName = this.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }
}