/****************************************************************************
Copyright (c) 2010-2012 cocos2d-x.org
Copyright (c) 2013-2016 Chukong Technologies Inc.
Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ****************************************************************************/
package org.cocos2dx.lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.enhance.gameservice.IGameTuningService;
import com.loopj.android.http.HttpGet;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class Cocos2dxHelper {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final String PREFS_NAME = "Cocos2dxPrefsFile";
    private static final int RUNNABLES_PER_FRAME = 5;
    private static final String TAG = Cocos2dxHelper.class.getSimpleName();

    // ===========================================================
    // Fields
    // ===========================================================

    private static Cocos2dxMusic sCocos2dMusic;
    private static Cocos2dxSound sCocos2dSound = null;
    private static AssetManager sAssetManager;
    private static Cocos2dxAccelerometer sCocos2dxAccelerometer = null;
    private static boolean sAccelerometerEnabled;
    private static boolean sCompassEnabled;
    private static boolean sActivityVisible;
    private static String sPackageName;
    private static Activity sActivity = null;
    private static Cocos2dxHelperListener sCocos2dxHelperListener;
    private static Set<OnActivityResultListener> onActivityResultListeners = new LinkedHashSet<OnActivityResultListener>();
    private static Vibrator sVibrateService = null;
    //Enhance API modification begin
    private static IGameTuningService mGameServiceBinder = null;
    private static final int BOOST_TIME = 7;
    //Enhance API modification end

    // The absolute path to the OBB if it exists, else the absolute path to the APK.
    private static String sAssetsPath = "";

    // The OBB file
    private static ZipResourceFile sOBBFile = null;

    // ===========================================================
    // Constructors
    // ===========================================================

    public static void runOnGLThread(final Runnable r) {
        ((Cocos2dxActivity)sActivity).runOnGLThread(r);
    }

    private static boolean sInited = false;
    public static void init(final Activity activity) {
        sActivity = activity;
        Cocos2dxHelper.sCocos2dxHelperListener = (Cocos2dxHelperListener)activity;
        if (!sInited) {

            PackageManager pm = activity.getPackageManager();
            boolean isSupportLowLatency = pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);

            Log.d(TAG, "isSupportLowLatency:" + isSupportLowLatency);

            int sampleRate = 44100;
            int bufferSizeInFrames = 192;

            if (Build.VERSION.SDK_INT >= 17) {
                AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                // use reflection to remove dependence of API 17 when compiling

                // AudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
                final Class audioManagerClass = AudioManager.class;
                Object[] parameters = new Object[]{Cocos2dxReflectionHelper.<String>getConstantValue(audioManagerClass, "PROPERTY_OUTPUT_SAMPLE_RATE")};
                final String strSampleRate = Cocos2dxReflectionHelper.<String>invokeInstanceMethod(am, "getProperty", new Class[]{String.class}, parameters);

                // AudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
                parameters = new Object[]{Cocos2dxReflectionHelper.<String>getConstantValue(audioManagerClass, "PROPERTY_OUTPUT_FRAMES_PER_BUFFER")};
                final String strBufferSizeInFrames = Cocos2dxReflectionHelper.<String>invokeInstanceMethod(am, "getProperty", new Class[]{String.class}, parameters);

                try {
                    sampleRate = Integer.parseInt(strSampleRate);
                    bufferSizeInFrames = Integer.parseInt(strBufferSizeInFrames);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "parseInt failed", e);
                }
                Log.d(TAG, "sampleRate: " + sampleRate + ", framesPerBuffer: " + bufferSizeInFrames);
            } else {
                Log.d(TAG, "android version is lower than 17");
            }

            nativeSetAudioDeviceInfo(isSupportLowLatency, sampleRate, bufferSizeInFrames);

            final ApplicationInfo applicationInfo = activity.getApplicationInfo();

            Cocos2dxHelper.sPackageName = applicationInfo.packageName;

            Cocos2dxHelper.sCocos2dMusic = new Cocos2dxMusic(activity);
            Cocos2dxHelper.sAssetManager = activity.getAssets();
            Cocos2dxHelper.nativeSetContext((Context)activity, Cocos2dxHelper.sAssetManager);

            Cocos2dxBitmap.setContext(activity);

            Cocos2dxHelper.sVibrateService = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);

            sInited = true;

            //Enhance API modification begin
            Intent serviceIntent = new Intent(IGameTuningService.class.getName());
            serviceIntent.setPackage("com.enhance.gameservice");
            boolean suc = activity.getApplicationContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
            //Enhance API modification end
        }
    }

    // This function returns the absolute path to the OBB if it exists,
    // else it returns the absolute path to the APK.
    public static String getAssetsPath()
    {
        if (Cocos2dxHelper.sAssetsPath.equals("")) {

            String pathToOBB = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/obb/" + Cocos2dxHelper.sPackageName;

	    	// Listing all files inside the folder (pathToOBB) where OBB files are expected to be found.
            String[] fileNames = new File(pathToOBB).list(new FilenameFilter() { // Using filter to pick up only main OBB file name.
                public boolean accept(File dir, String name) {
                    return name.startsWith("main.") && name.endsWith(".obb");  // It's possible to filter only by extension here to get path to patch OBB file also.
                }
            });

            String fullPathToOBB = "";
            if (fileNames != null && fileNames.length > 0)  // If there is at least 1 element inside the array with OBB file names, then we may think fileNames[0] will have desired main OBB file name.
                fullPathToOBB = pathToOBB + "/" + fileNames[0];  // Composing full file name for main OBB file.

            File obbFile = new File(fullPathToOBB);
            if (obbFile.exists())
                Cocos2dxHelper.sAssetsPath = fullPathToOBB;
            else
                Cocos2dxHelper.sAssetsPath = Cocos2dxHelper.sActivity.getApplicationInfo().sourceDir;
        }

        return Cocos2dxHelper.sAssetsPath;
    }

    public static ZipResourceFile getObbFile() {
        if (null == sOBBFile) {
            int versionCode = 1;
            try {
                versionCode = Cocos2dxActivity.getContext().getPackageManager().getPackageInfo(Cocos2dxHelper.getCocos2dxPackageName(), 0).versionCode;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

            try {
                sOBBFile = APKExpansionSupport.getAPKExpansionZipFile(Cocos2dxActivity.getContext(), versionCode, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sOBBFile;
    }

    //Enhance API modification begin
    private static ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGameServiceBinder = IGameTuningService.Stub.asInterface(service);
            fastLoading(BOOST_TIME);
        }

        public void onServiceDisconnected(ComponentName name) {
            sActivity.getApplicationContext().unbindService(connection);
        }
    };
    //Enhance API modification end

    public static Activity getActivity() {
        return sActivity;
    }

    public static void addOnActivityResultListener(OnActivityResultListener listener) {
        onActivityResultListeners.add(listener);
    }

    public static Set<OnActivityResultListener> getOnActivityResultListeners() {
        return onActivityResultListeners;
    }

    public static boolean isActivityVisible(){
        return sActivityVisible;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    private static native void nativeSetEditTextDialogResult(final byte[] pBytes);

    private static native void nativeSetContext(final Context pContext, final AssetManager pAssetManager);

    private static native void nativeSetAudioDeviceInfo(boolean isSupportLowLatency, int deviceSampleRate, int audioBufferSizeInFames);

    private static final int kDialog_Confirm = 0;
    private static final int kDialog_Negative = 1;

    private static native void nativeShowOptionDialogResult(final long listener, final int result);

    public static String getCocos2dxPackageName() {
        return Cocos2dxHelper.sPackageName;
    }
    public static String getCocos2dxWritablePath() {
        return sActivity.getFilesDir().getAbsolutePath();
    }

    public static String getCurrentLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getDeviceModel(){
        return Build.MODEL;
    }

    public static AssetManager getAssetManager() {
        return Cocos2dxHelper.sAssetManager;
    }

    public static void enableAccelerometer() {
        Cocos2dxHelper.sAccelerometerEnabled = true;
        Cocos2dxHelper.getAccelerometer().enableAccel();
    }

    public static void enableCompass() {
        Cocos2dxHelper.sCompassEnabled = true;
        Cocos2dxHelper.getAccelerometer().enableCompass();
    }

    public static void setAccelerometerInterval(float interval) {
        Cocos2dxHelper.getAccelerometer().setInterval(interval);
    }

    public static void disableAccelerometer() {
        Cocos2dxHelper.sAccelerometerEnabled = false;
        Cocos2dxHelper.getAccelerometer().disable();
    }

    public static void setKeepScreenOn(boolean value) {
        ((Cocos2dxActivity)sActivity).setKeepScreenOn(value);
    }

    public static void vibrate(float duration) {
        sVibrateService.vibrate((long)(duration * 1000));
    }

    public static String getVersion() {
        try {
            String version = Cocos2dxActivity.getContext().getPackageManager().getPackageInfo(Cocos2dxActivity.getContext().getPackageName(), 0).versionName;
            return version;
        } catch(Exception e) {
            return "";
        }
    }

    public static boolean openURL(String url) {
        boolean ret = false;
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            sActivity.startActivity(i);
            ret = true;
        } catch (Exception e) {
        }
        return ret;
    }

    public static long[] getObbAssetFileDescriptor(final String path) {
        long[] array = new long[3];
        if (Cocos2dxHelper.getObbFile() != null) {
            AssetFileDescriptor descriptor = Cocos2dxHelper.getObbFile().getAssetFileDescriptor(path);
            if (descriptor != null) {
                try {
                    ParcelFileDescriptor parcel = descriptor.getParcelFileDescriptor();
                    Method method = parcel.getClass().getMethod("getFd", new Class[] {});
                    array[0] = (Integer)method.invoke(parcel);
                    array[1] = descriptor.getStartOffset();
                    array[2] = descriptor.getLength();
                } catch (NoSuchMethodException e) {
                    Log.e(Cocos2dxHelper.TAG, "Accessing file descriptor directly from the OBB is only supported from Android 3.1 (API level 12) and above.");
                } catch (IllegalAccessException e) {
                    Log.e(Cocos2dxHelper.TAG, e.toString());
                } catch (InvocationTargetException e) {
                    Log.e(Cocos2dxHelper.TAG, e.toString());
                }
            }
        }
        return array;
    }

    public static void preloadBackgroundMusic(final String pPath) {
        Cocos2dxHelper.sCocos2dMusic.preloadBackgroundMusic(pPath);
    }

    public static void playBackgroundMusic(final String pPath, final boolean isLoop) {
        Cocos2dxHelper.sCocos2dMusic.playBackgroundMusic(pPath, isLoop);
    }

    public static void resumeBackgroundMusic() {
        Cocos2dxHelper.sCocos2dMusic.resumeBackgroundMusic();
    }

    public static void pauseBackgroundMusic() {
        Cocos2dxHelper.sCocos2dMusic.pauseBackgroundMusic();
    }

    public static void stopBackgroundMusic() {
        Cocos2dxHelper.sCocos2dMusic.stopBackgroundMusic();
    }

    public static void rewindBackgroundMusic() {
        Cocos2dxHelper.sCocos2dMusic.rewindBackgroundMusic();
    }

    public static boolean willPlayBackgroundMusic() {
        return Cocos2dxHelper.sCocos2dMusic.willPlayBackgroundMusic();
    }

    public static boolean isBackgroundMusicPlaying() {
        return Cocos2dxHelper.sCocos2dMusic.isBackgroundMusicPlaying();
    }

    public static float getBackgroundMusicVolume() {
        return Cocos2dxHelper.sCocos2dMusic.getBackgroundVolume();
    }

    public static void setBackgroundMusicVolume(final float volume) {
        Cocos2dxHelper.sCocos2dMusic.setBackgroundVolume(volume);
    }

    public static void preloadEffect(final String path) {
        Cocos2dxHelper.getSound().preloadEffect(path);
    }

    public static int playEffect(final String path, final boolean isLoop, final float pitch, final float pan, final float gain) {
        return Cocos2dxHelper.getSound().playEffect(path, isLoop, pitch, pan, gain);
    }

    public static void resumeEffect(final int soundId) {
        Cocos2dxHelper.getSound().resumeEffect(soundId);
    }

    public static void pauseEffect(final int soundId) {
        Cocos2dxHelper.getSound().pauseEffect(soundId);
    }

    public static void stopEffect(final int soundId) {
        Cocos2dxHelper.getSound().stopEffect(soundId);
    }

    public static float getEffectsVolume() {
        return Cocos2dxHelper.getSound().getEffectsVolume();
    }

    public static void setEffectsVolume(final float volume) {
        Cocos2dxHelper.getSound().setEffectsVolume(volume);
    }

    public static void unloadEffect(final String path) {
        Cocos2dxHelper.getSound().unloadEffect(path);
    }

    public static void pauseAllEffects() {
        Cocos2dxHelper.getSound().pauseAllEffects();
    }

    public static void resumeAllEffects() {
        Cocos2dxHelper.getSound().resumeAllEffects();
    }

    public static void stopAllEffects() {
        Cocos2dxHelper.getSound().stopAllEffects();
    }

    static void setAudioFocus(boolean isAudioFocus) {
        sCocos2dMusic.setAudioFocus(isAudioFocus);
        getSound().setAudioFocus(isAudioFocus);
    }

    public static void end() {
        Cocos2dxHelper.sCocos2dMusic.end();
        Cocos2dxHelper.getSound().end();
    }

    public static void onResume() {
        sActivityVisible = true;
        if (Cocos2dxHelper.sAccelerometerEnabled) {
            Cocos2dxHelper.getAccelerometer().enableAccel();
        }
        if (Cocos2dxHelper.sCompassEnabled) {
            Cocos2dxHelper.getAccelerometer().enableCompass();
        }

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        sActivity.registerReceiver(mIntentReceiver, mIntentFilter);
    }

    public static void onPause() {
        sActivityVisible = false;
        if (Cocos2dxHelper.sAccelerometerEnabled) {
            Cocos2dxHelper.getAccelerometer().disable();
        }

        try {
            sActivity.unregisterReceiver(mIntentReceiver);
        } catch (Exception e) {

        }
    }

    public static void onEnterBackground() {
        getSound().onEnterBackground();
        sCocos2dMusic.onEnterBackground();
    }

    public static void onEnterForeground() {
        getSound().onEnterForeground();
        sCocos2dMusic.onEnterForeground();
    }

    public static void terminateProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private static void showDialog(final String pTitle, final String pMessage) {
        Cocos2dxHelper.sCocos2dxHelperListener.showDialog(pTitle, pMessage);
    }


    public static void setEditTextDialogResult(final String pResult) {
        try {
            final byte[] bytesUTF8 = pResult.getBytes("UTF8");

            Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    Cocos2dxHelper.nativeSetEditTextDialogResult(bytesUTF8);
                }
            });
        } catch (UnsupportedEncodingException pUnsupportedEncodingException) {
            /* Nothing. */
        }
    }

    public static int getDPI()
    {
        if (sActivity != null)
        {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = sActivity.getWindowManager();
            if (wm != null)
            {
                Display d = wm.getDefaultDisplay();
                if (d != null)
                {
                    d.getMetrics(metrics);
                    return (int)(metrics.density*160.0f);
                }
            }
        }
        return -1;
    }

    // ===========================================================
    // Functions for CCUserDefault
    // ===========================================================

    public static boolean getBoolForKey(String key, boolean defaultValue) {
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        try {
            return settings.getBoolean(key, defaultValue);
        }
        catch (Exception ex) {
            ex.printStackTrace();

            Map allValues = settings.getAll();
            Object value = allValues.get(key);
            if ( value instanceof String)
            {
                return  Boolean.parseBoolean(value.toString());
            }
            else if (value instanceof Integer)
            {
                int intValue = ((Integer) value).intValue();
                return (intValue !=  0) ;
            }
            else if (value instanceof Float)
            {
                float floatValue = ((Float) value).floatValue();
                return (floatValue != 0.0f);
            }
        }

        return defaultValue;
    }

    public static int getIntegerForKey(String key, int defaultValue) {
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        try {
            return settings.getInt(key, defaultValue);
        }
        catch (Exception ex) {
            ex.printStackTrace();

            Map allValues = settings.getAll();
            Object value = allValues.get(key);
            if ( value instanceof String) {
                return  Integer.parseInt(value.toString());
            }
            else if (value instanceof Float)
            {
                return ((Float) value).intValue();
            }
            else if (value instanceof Boolean)
            {
                boolean booleanValue = ((Boolean) value).booleanValue();
                if (booleanValue)
                    return 1;
            }
        }

        return defaultValue;
    }

    public static float getFloatForKey(String key, float defaultValue) {
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        try {
            return settings.getFloat(key, defaultValue);
        }
        catch (Exception ex) {
            ex.printStackTrace();

            Map allValues = settings.getAll();
            Object value = allValues.get(key);
            if ( value instanceof String) {
                return  Float.parseFloat(value.toString());
            }
            else if (value instanceof Integer)
            {
                return ((Integer) value).floatValue();
            }
            else if (value instanceof Boolean)
            {
                boolean booleanValue = ((Boolean) value).booleanValue();
                if (booleanValue)
                    return 1.0f;
            }
        }

        return defaultValue;
    }

    public static double getDoubleForKey(String key, double defaultValue) {
        // SharedPreferences doesn't support saving double value
        return getFloatForKey(key, (float) defaultValue);
    }

    public static String getStringForKey(String key, String defaultValue) {
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        try {
            return settings.getString(key, defaultValue);
        }
        catch (Exception ex) {
            ex.printStackTrace();

            return settings.getAll().get(key).toString();
        }
    }

    public static void setBoolForKey(String key, boolean value) {
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void setIntegerForKey(String key, int value) {
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void setFloatForKey(String key, float value) {
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static void setDoubleForKey(String key, double value) {
        // SharedPreferences doesn't support recording double value
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, (float)value);
        editor.apply();
    }

    public static void setStringForKey(String key, String value) {
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void deleteValueForKey(String key) {
        SharedPreferences settings = sActivity.getSharedPreferences(Cocos2dxHelper.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
    }

    public static byte[] conversionEncoding(byte[] text, String fromCharset,String newCharset)
    {
        try {
            String str = new String(text,fromCharset);
            return str.getBytes(newCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /* CronlyGames Inc. All Right Reserved here */

    public static String saveDeviceId = null;
    public static String getDeviceId() {
        String deviceId = saveDeviceId;
        if (deviceId != null) {
            return deviceId;
        }

        if (deviceId == null) {
            try {
                String sdkVersion = Build.VERSION.SDK;
                if (Integer.valueOf(sdkVersion) >= 9 ) {
                    Field f = Build.class.getField("SERIAL");
                    deviceId = (String)f.get(Build.class);
                }
            } catch (Exception e) {
                deviceId = null;
            }
        }
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(sActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        if ("9774d56d682e549c".equals(deviceId) || deviceId == null) {
            deviceId = ((TelephonyManager) sActivity.getSystemService(Context.TELEPHONY_SERVICE )).getDeviceId();
        }
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
        }

        saveDeviceId = deviceId;

        return deviceId;
    }

    public static void openUrl(String str) {
        try {
            Uri uri = Uri.parse(str);
            Intent it = new Intent(Intent.ACTION_VIEW,uri);
            sActivity.startActivity(it);
        } catch (Exception e) {

        }
    }

    public static void openApp(String packageId) {
        try {
            PackageManager packageManager = sActivity.getPackageManager();
            Intent it = packageManager.getLaunchIntentForPackage(packageId);
            sActivity.startActivity(it);
        } catch (Exception e) {

        }
    }

    public static boolean isAppInstalled(String packageName) {
        PackageInfo packinfo = null;
        try {
            packinfo = sActivity.getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            packinfo = null;
        }

        return packinfo != null;
    }

    public static void showWiFiDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.NetworkNotAvailable);
        builder.setMessage(R.string.DoYouWantToSetupNetwork);

        builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;

                try {
                    String sdkVersion = android.os.Build.VERSION.SDK;
                    if(Integer.valueOf(sdkVersion) > 10) {
                        intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                    }else {
                        intent = new Intent("android.settings.WIFI_SETTINGS");
                    }
                    sActivity.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

    public static boolean isNetworkOpen(boolean bShowDialog) {
        boolean bNetworkOpen = false;
        ConnectivityManager connManager = (ConnectivityManager)sActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager.getActiveNetworkInfo() != null) {
            bNetworkOpen = connManager.getActiveNetworkInfo().isAvailable();
        }

        if (!bShowDialog) {
            return bNetworkOpen;
        } else if (!bNetworkOpen) {
            // show open wifi dialog
            Cocos2dxActivity app =  (Cocos2dxActivity)sActivity;
            app.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Cocos2dxHelper.showWiFiDialog();
                }
            });
        }

        return bNetworkOpen;
    }

    public static void doShowOptionDiloag(long callback, String title, String message, String PosAns, String NegAns) {
        AlertDialog.Builder builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(title);
        builder.setMessage(message);

        if (PosAns.length() <= 1) {
            PosAns = sActivity.getString(R.string.Yes);
        }

        if (NegAns.length() <= 1) {
            NegAns = sActivity.getString(R.string.No);
        }

        final long cb = callback;
        builder.setPositiveButton(PosAns, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cocos2dxHelper.nativeShowOptionDialogResult(cb, kDialog_Confirm);
                dialog.dismiss();
            }
        }).setNegativeButton(NegAns, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cocos2dxHelper.nativeShowOptionDialogResult(cb, kDialog_Negative);

                dialog.cancel();
            }
        }).show();
    }

    public static void showOptionDialog(long callback, String title, String message, String PosAns, String NegAns) {
        final String tStr = title;
        final String mStr = message;
        final String pStr = PosAns;
        final String nStr = NegAns;

        final long cb = callback;

        Cocos2dxActivity app =  (Cocos2dxActivity)sActivity;
        app.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Cocos2dxHelper.doShowOptionDiloag(cb, tStr, mStr, pStr, nStr);
            }
        });
    }

    @SuppressWarnings("deprecation")
    public static String getWiFiIPAddress() {
        String ret = null;
        try {
            WifiManager wifiMgr = (WifiManager)sActivity.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            ret = Formatter.formatIpAddress(ip);
        } catch (Exception e) {
            ret = "127.0.0.1";
        }

        return ret;
    }

    @SuppressLint("SdCardPath")
    public static void copyFileToSDCard(String filename) {
        String newPath = "/mnt/sdcard/" + filename;
        String oldPath = sActivity.getFilesDir().toString() + "/" + filename;

        try {
            InputStream is = new FileInputStream(oldPath);
            FileOutputStream os = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
            is.close();
            os.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void shareFile(String content) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            JSONObject res = new JSONObject(content);

            int mediaType = res.getInt("mediaType");
            if (mediaType == 0 || mediaType == 2) {
                intent.setType("text/plain");

                String text = "";
                if (res.has("title")) {
                    String str = res.getString("title");
                    text = text + str + "\n";
                }

                if (res.has("text")) {
                    String str = res.getString("text");
                    text = text + str + "\n";
                }

                if (res.has("url")) {
                    String str = res.getString("url");
                    text = text + str;
                }
                intent.putExtra(Intent.EXTRA_TEXT, text);
            } else {
                intent.setType("image/*");

                String imagePath = res.getString("imagePath");
                if (imagePath == null || imagePath.isEmpty()) {
                    final String desc = "分享信息里没有设置 imagePath";
                    OpenSDK.invokeCallback(OpenSDK.TYPE_LISTENER_SHARE, 1, desc);
                    return;
                }

                if (res.has("title")) {
                    String title = res.getString("title");
                    if (title != null) {
                        intent.putExtra(Intent.EXTRA_SUBJECT, title);
                    }
                }

                if (res.has("text")) {
                    String text = res.getString("text");
                    if (text != null) {
                        intent.putExtra(Intent.EXTRA_TEXT, text);
                    }
                }

                File file = new File(imagePath);
                Uri uri = Uri.fromFile(file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }

            String s = sActivity.getString(R.string.share_title);
            intent.putExtra(Intent.EXTRA_TITLE, s);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            s = sActivity.getString(R.string.share_please_choose);
            sActivity.startActivity(Intent.createChooser(intent, s));
        } catch (Exception e) {
            final String desc = e.toString();
            Log.e(TAG, desc);
            OpenSDK.invokeCallback(OpenSDK.TYPE_LISTENER_SHARE, 1, desc);
        }
    }


    private static double loc_longitude = 0;
    private static double loc_latitude 	= 0;
    private static double loc_altitude 	= 0;
    private static String loc_address   = "";

    public static void startLocation () {
        Activity act = sActivity;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                do_startLocation();
            }
        });
    }

    private static void do_startLocation () {
        Activity act = sActivity;

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }

        LocationManager lm = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                    lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))) {
            queryLocation();
        } else {
            Toast.makeText(act, "请开启GPS！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            act.startActivityForResult(intent, 0);
        }
    }

    private static LocationListener llistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            getAddress(loc);
        }

        @Override
        public void onProviderDisabled(String arg0) {
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    };

    private static void queryLocation() {
        new Thread(){
            public void run () {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                doQueryLocation();
            }
        }.start();
    }

    private static void doQueryLocation() {
        Map<String, Location> map = getLocationObject(llistener);

        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Location loc = map.get(key);

            getAddress(loc);
        }
    }

    private static Map<String, Location> getLocationObject(
            LocationListener locationListener) {
        Map<String, Location> lMap = new HashMap<>();

        Activity act = sActivity;
        LocationManager locationManager = (LocationManager) act
            .getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        // 获得最好的定位效果
        try {
            criteria.setAccuracy(Criteria.ACCURACY_HIGH);
        } catch (Exception e) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        }
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        // 使用省电模式
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        final long minTime = 60;
        final long minDis = 100;

        for (final String provider : locationManager.getProviders(true)) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (null != l) {
                lMap.put(provider, l);
            }
            locationManager.requestLocationUpdates(provider, minTime, minDis,
                    locationListener);
        }
        return lMap;
    }

    private static String getAddress(Location loc) {
        String address = null;

        Activity act = sActivity;
        if (loc == null) {
            return address;
        }

        loc_longitude = loc.getLongitude();
        loc_latitude = loc.getLatitude();
        loc_altitude = loc.getAltitude();

        Geocoder mGeocoder = new Geocoder(act);

        // 得到逆理编码，参数分别为：纬度，经度，最大结果集
        // 高德根据政府规定，在由GPS获取经纬度显示时，使用getFromRawGpsLocation()方法;
        List<Address> listAddress = null;
        try {
            listAddress = mGeocoder.getFromLocation(loc_latitude,
                    loc_longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (listAddress != null && listAddress.size() != 0) {
            Address a = listAddress.get(0);

            StringBuilder sb = new StringBuilder();
            if (a.getCountryName() != null) {
                sb.append(a.getCountryName());
            }

            String one = a.getLocality();
            if (one == null) {
                one = a.getAdminArea();
            }
            if (one != null) {
                sb.append(one);
            }

            one = a.getSubLocality();
            if (one == null) {
                one = a.getSubAdminArea();
            }
            if (one != null) {
                sb.append(one);
            }

            one = a.getSubThoroughfare();
            if (one == null) {
                one = a.getThoroughfare();
            } else {
                if (a.getThoroughfare() != null) {
                    one = a.getThoroughfare() + one;
                }
            }
            if (one == null) {
                one = a.getFeatureName();
            }
            if (one != null) {
                sb.append(one);
            }

            address = sb.toString();
        }

        if (address != null) {
            loc_address = address;
        }

        return address;
    }

    public static void stopLocation() {
        Activity act = sActivity;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                do_stopLocation();
            }
        });
    }

    private static void do_stopLocation() {
        Activity act = sActivity;

        LocationManager lm = (LocationManager) act
            .getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(llistener);
    }

    public static String getGPSLocation() {
        return loc_address;
    }

    public static double[] getGPSParams() {
        double params[] = { loc_longitude, loc_latitude, loc_altitude };
        return params;
    }

    public static String getNetworkType() {
        Activity act = sActivity;
        ConnectivityManager connMgr = (ConnectivityManager) act
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return "";
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            return "WIFI";
        }

        return "4G";
    }

    private static BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                // 获取当前电量
                int level = intent.getIntExtra("level", 0);
                // 电量的总刻度
                int scale = intent.getIntExtra("scale", 100);

                mBatteryLevel = level * 1.0f / scale;
            }
        }
    };

    private static float mBatteryLevel = 1.00f;

    public static float getBatteryLevel() {
        return mBatteryLevel;
    }

    private static MediaRecorder recorder = null;
    public static void startRecord(String filePath) {
        if (recorder != null) {
            recorder.reset();
        } else {
            recorder = new MediaRecorder();
        }

        recorder.setOnErrorListener(null);
        recorder.setOnInfoListener(null);
        recorder.setPreviewDisplay(null);

        // 从麦克风中录音
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置编码格式为AMR
        recorder.setAudioSamplingRate(8000);
        recorder.setAudioChannels(1);
        recorder.setMaxDuration(30 * 1000); // ms
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(filePath);
        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 结束录音
    public static void stopRecord() {
        if (recorder != null) {
            try {
                recorder.setOnErrorListener(null);
                recorder.setOnInfoListener(null);
                recorder.setPreviewDisplay(null);
                recorder.stop();//
            } catch (IllegalStateException e) {
                Log.i("Exception", Log.getStackTraceString(e));
            } catch (RuntimeException e) {
                Log.i("Exception", Log.getStackTraceString(e));
            } catch (Exception e) {
                Log.i("Exception", Log.getStackTraceString(e));
            }
            recorder.reset();
        }
    }

    private static MediaPlayer mMediaPlayer = null;
    public static void playVoice(String filePath) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        } else {
            mMediaPlayer = new MediaPlayer();
            // 设置一个error监听器
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        }

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.setVolume(1.0f, 1.0f);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isVoicePlaying() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    private static void showUpdateDialog(final String title, final String text, final String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(title);
        builder.setMessage(text);

        builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openUrl(url);
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

    private static void backHandleUpdate (String content) {
        try {
            JSONObject res = new JSONObject(content);
            String remoteVersionCode = res.getString("versionCode");
            int remoteCode = Integer.parseInt(remoteVersionCode);
            int localCode = Cocos2dxHelper.sActivity.getPackageManager().getPackageInfo(Cocos2dxHelper.sPackageName, 0).versionCode;

            if (localCode < remoteCode) {
                final String title = res.getString("versionTitle");
                final String text = res.getString("versionLog");
                JSONObject urls = res.getJSONObject("urls");
                final String url = urls.getString("android");

                Log.e("backHandleUpdate", title + "," + text + "," + url);

                Cocos2dxActivity app =  (Cocos2dxActivity)sActivity;
                app.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showUpdateDialog(title, text, url);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkUpdate (String packageName) {
        if (packageName.equals("")) {
            return;
        }

        final String url = "http://www.cronlygames.com/autoupdate/" + packageName;

        new Thread() {
            public void run () {
                HttpGet httpGet = new HttpGet(url);
                HttpClient httpClient = new DefaultHttpClient();

                // 发送请求
                try {
                    HttpResponse response = httpClient.execute(httpGet);
                    HttpEntity e = response.getEntity();
                    InputStream is = e.getContent();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len = 0;
                    byte buffer[] = new byte[1024];
                    while ((len = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    is.close();
                    baos.close();

                    String result = new String(baos.toByteArray());
                    backHandleUpdate(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void registerNotifications (String msg, String act) {
        NotificationManager nMgr = (NotificationManager)sActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nMgr == null) {
            return;
        }

        long kOneDay = 60 * 60 * 24;
        long n = (long)Math.floor(3.0 * Math.random());
        long kRandomInterval = kOneDay * (1 + n) * 1000;

        Intent intent = new Intent(sActivity.getIntent());
        PendingIntent contentIntent = PendingIntent.getActivity(sActivity,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification  = new Notification.Builder(Cocos2dxActivity.getContext())
                .setContentTitle("New notification")
                .setContentText(msg)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(contentIntent)
                .setTicker(act)
                .setWhen(System.currentTimeMillis() + kRandomInterval)
                .setAutoCancel(true).getNotification();
        nMgr.notify(0, notification);
    }

    public  static void checkNotifications () {
        NotificationManager nMgr = (NotificationManager)sActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nMgr != null) {
            nMgr.cancelAll();
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public static interface Cocos2dxHelperListener {
        public void showDialog(final String pTitle, final String pMessage);

        public void runOnGLThread(final Runnable pRunnable);
    }

    //Enhance API modification begin
    public static int setResolutionPercent(int per) {
        try {
            if (mGameServiceBinder != null) {
                return mGameServiceBinder.setPreferredResolution(per);
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int setFPS(int fps) {
        try {
            if (mGameServiceBinder != null) {
                return mGameServiceBinder.setFramePerSecond(fps);
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int fastLoading(int sec) {
        try {
            if (mGameServiceBinder != null) {
                return mGameServiceBinder.boostUp(sec);
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getTemperature() {
        try {
            if (mGameServiceBinder != null) {
                return mGameServiceBinder.getAbstractTemperature();
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int setLowPowerMode(boolean enable) {
        try {
            if (mGameServiceBinder != null) {
                return mGameServiceBinder.setGamePowerSaving(enable);
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Returns whether the screen has a round shape. Apps may choose to change styling based
     * on this property, such as the alignment or layout of text or informational icons.
     *
     * @return true if the screen is rounded, false otherwise
     */
    public static boolean isScreenRound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (sActivity.getResources().getConfiguration().isScreenRound()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Queries about whether any physical keys exist on the
     * any keyboard attached to the device and returns <code>true</code>
     * if the device does not have physical keys
     *
     * @return Returns <code>true</code> if the device have no physical keys,
     * otherwise <code>false</code> will returned.
     */
    public static boolean hasSoftKeys() {
        boolean hasSoftwareKeys = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = sActivity.getWindowManager().getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            display.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            hasSoftwareKeys = (realWidth - displayWidth) > 0 ||
                    (realHeight - displayHeight) > 0;
        } else {
            boolean hasMenuKey = ViewConfiguration.get(sActivity).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
        }
        return hasSoftwareKeys;
    }

    //Enhance API modification end
    public static float[] getAccelValue() {
        return Cocos2dxHelper.getAccelerometer().accelerometerValues;
    }

    public static float[] getCompassValue() {
        return Cocos2dxHelper.getAccelerometer().compassFieldValues;
    }

    public static int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    private static Cocos2dxAccelerometer getAccelerometer() {
        if (null == sCocos2dxAccelerometer)
            Cocos2dxHelper.sCocos2dxAccelerometer = new Cocos2dxAccelerometer(sActivity);

        return sCocos2dxAccelerometer;
    }

    private static Cocos2dxSound getSound() {
        if (null == sCocos2dSound)
            sCocos2dSound = new Cocos2dxSound(sActivity);

        return sCocos2dSound;
    }
}
