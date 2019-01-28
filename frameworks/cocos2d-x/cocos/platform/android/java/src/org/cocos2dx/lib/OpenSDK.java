package org.cocos2dx.lib;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.view.KeyEvent;

public class OpenSDK {
    private static final String TAG = "OpenSDK";

    //region ads
    public static final class AdsPos {
        public static final int	kPosCenter         = 0; 	//   --enum the toolbar is at center.
        public static final int kPosTop            = 1; 	//   --enum the toolbar is at top.
        public static final int kPosTopLeft        = 2; 	//	 --enum the toolbar is at topleft.
        public static final int kPosTopRight       = 3; 	//	 --enum the toolbar is at topright.
        public static final int kPosBottom         = 4; 	//   --enum the toolbar is at bottom.
        public static final int kPosBottomLeft     = 5; 	//	 --enum the toolbar is at bottomleft.
        public static final int kPosBottomRight    = 6; 	//   --enum the toolbar is at bottomright.
    }

    public static final class AdsType {
        public static final int	AD_TYPE_SPLASH        = -1;		//   --enum value is splash ads.
        public static final int	AD_TYPE_BANNER        = 0;		//   --enum value is banner ads.
        public static final int	AD_TYPE_FULLSCREEN    = 1;		//   --enum value is fullscreen ads.
    }
    //endregion

    //region activity callback
    public interface iActivityCallback {
        public void onNewIntent(Intent intent);
        public void onActivityResult(int requestCode, int resultCode, Intent data);

        public void onPause();
        public void onResume();

        public void onStop();
        public void onRestart();

        public void onDestroy();

        public boolean onKeyDown(int keyCode, KeyEvent event);
    }

    public static Cocos2dxActivity s_activity = null;	
    public static void init(Cocos2dxActivity app) {
        s_activity = app;
    }

    private static ArrayList<iActivityCallback> s_callbacks = new ArrayList<iActivityCallback>();
    public static void addActivityCallback(iActivityCallback one) {
        s_callbacks.add(one);
    }

    public static void onNewIntent(Intent intent) {
        for(iActivityCallback one:s_callbacks){
            one.onNewIntent(intent);
        }
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        for(iActivityCallback one:s_callbacks){
            one.onActivityResult(requestCode, resultCode, data);;
        }
    }

    public static void onPause() {
        for(iActivityCallback one:s_callbacks){
            one.onPause();
        }
    }

    public static void onResume() {
        for(iActivityCallback one:s_callbacks){
            one.onResume();
        }
    }

    public static void onStop() {
        for(iActivityCallback one:s_callbacks){
            one.onStop();
        }
    }

    public static void onRestart() {
        for(iActivityCallback one:s_callbacks){
            one.onRestart();
        }
    }

    public static void onDestroy() {
        for(iActivityCallback one:s_callbacks){
            one.onDestroy();
        }
    }

    public static boolean onKeyDown(int keyCode, KeyEvent event) {
        for(iActivityCallback one:s_callbacks){
            if(one.onKeyDown(keyCode, event)){
                return true;
            }
        }
        return false;
    }

    //endregion

    //region native callback
    public static final int TYPE_LISTENER_LOGIN 		= 0;
    public static final int TYPE_LISTENER_SHARE 		= 1;
    public static final int TYPE_LISTENER_PAYMENT 		= 2;
    private static native void nativeInvokeCallback(final int type, final int code, final String desc);

    public static void invokeCallback (final int type, final int code, final String desc) {
        s_activity.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeInvokeCallback(type, code, desc);
            }
        });
    }
    //endregion

    //region http handle
    public interface iHttpResultCallback {
        public void procResult(String result);
    }

    private static void do_http_send(String strUrl, String strData,
                                     iHttpResultCallback callback) throws Exception
    {
        URL url = new URL(strUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setReadTimeout(20000);
        urlConnection.setConnectTimeout(20000);
        urlConnection.setRequestProperty("Connection","keep-alive");
        urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length",String.valueOf(strData.getBytes().length));
        urlConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
        urlConnection.setDoOutput(true); // 发送POST请求必须设置允许输出
        urlConnection.setDoInput(true); // 发送POST请求必须设置允许输入

        OutputStream os = urlConnection.getOutputStream();
        os.write(strData.getBytes());
        os.flush();

        if (urlConnection.getResponseCode() == 200) {
            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            byte buffer[] = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer,0, len);
            }
            is.close();
            baos.close();

            final String result = new String(baos.toByteArray());
            callback.procResult(result);
        } else {
            Log.e(TAG,"链接失败......... " + strUrl);
        }
    }

    public static void httpSend(final String strUrl, final String strData, final iHttpResultCallback callback) {
        new Thread() {
            public void run() {
                try {
                    do_http_send(strUrl, strData, callback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    //endregion
}
