package fr.lina3d.glowquest;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    static final String URL = "https://nolangoncalves44-spec.github.io/Glow-Quest/?app=1";
    static final String SITE = "https://nolangoncalves44-spec.github.io/Glow-Quest/";
    WebView web;
    ValueCallback<Uri[]> fileCb;
    String pending;
    boolean loaded;

    void handleIntent(Intent i) {
        if (i == null) return;
        String d = i.getStringExtra("data");
        if (d == null && i.getData() != null && "glowquest".equals(i.getData().getScheme())) d = i.getData().getQueryParameter("d");
        if (d != null) { pending = d; inject(); }
    }

    void inject() {
        if (pending == null || !loaded || web == null) return;
        String js = "window.gqImport&&gqImport(" + JSONObject.quote(pending) + ")";
        pending = null;
        web.evaluateJavascript(js, null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) return;
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm == null || !cm.hasPrimaryClip()) return;
            ClipData clip = cm.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) return;
            CharSequence t = clip.getItemAt(0).getText();
            if (t == null) return;
            String s = t.toString().trim();
            if (!s.startsWith("GLOWQUEST:")) return;
            pending = s.substring(10);
            if (Build.VERSION.SDK_INT >= 28) cm.clearPrimaryClip(); else cm.setPrimaryClip(ClipData.newPlainText("", ""));
            inject();
        } catch (Exception e) { }
    }

    @Override
    protected void onNewIntent(Intent i) { super.onNewIntent(i); setIntent(i); handleIntent(i); }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        int bg = Color.parseColor("#140d2b");
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        web = new WebView(this);
        web.setBackgroundColor(bg);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        web.addJavascriptInterface(new Bridge(), "Android");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                String h = u.getHost();
                if (h != null && h.endsWith("github.io")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { }
                return true;
            }

            @Override
            public void onPageFinished(WebView v, String url) { loaded = true; inject(); }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCb != null) fileCb.onReceiveValue(null);
                fileCb = cb;
                try { startActivityForResult(p.createIntent(), 1); } catch (Exception e) { fileCb = null; return false; }
                return true;
            }
        });
        if (state != null) web.restoreState(state); else web.loadUrl(URL);
        handleIntent(getIntent());
        Reminders.reschedule(this);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == 1 && fileCb != null) {
            fileCb.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            fileCb = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); web.saveState(out); }

    @Override
    public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }

    class Bridge {
        @JavascriptInterface
        public void setReminders(boolean on, String times) {
            Reminders.save(MainActivity.this, on, times);
            if (on && Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(() -> requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 2));
            }
            Reminders.reschedule(MainActivity.this);
        }

        @JavascriptInterface
        public void setDone(String day) { Reminders.setDone(MainActivity.this, day); }

        @JavascriptInterface
        public boolean saveFile(String name, String content) {
            try {
                byte[] data = content.getBytes("UTF-8");
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues cv = new ContentValues();
                    cv.put(MediaStore.Downloads.DISPLAY_NAME, name);
                    cv.put(MediaStore.Downloads.MIME_TYPE, "application/json");
                    Uri u = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                    OutputStream os = getContentResolver().openOutputStream(u);
                    os.write(data);
                    os.close();
                } else {
                    File f = new File(getExternalFilesDir(null), name);
                    FileOutputStream os = new FileOutputStream(f);
                    os.write(data);
                    os.close();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public String platform() { return "android"; }

        @JavascriptInterface
        public void openTransfer() {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SITE + "?transfer=1"))); } catch (Exception e) { }
        }
    }
}
