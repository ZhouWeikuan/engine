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
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include <string>
#include "platform/android/jni/JniHelper.h"
#include "platform/android/CCFileUtils-android.h"
#include "android/asset_manager_jni.h"
#include "platform/android/jni/Java_org_cocos2dx_lib_Cocos2dxHelper.h"

#include "base/ccUTF8.h"

#define  LOG_TAG    "Java_org_cocos2dx_lib_Cocos2dxHelper.cpp"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

static const std::string className = "org.cocos2dx.lib.Cocos2dxHelper";

static EditTextCallback s_editTextCallback = nullptr;
static void* s_ctx = nullptr;

static int __deviceSampleRate = 44100;
static int __deviceAudioBufferSizeInFrames = 192;

static std::string g_apkPath;

using namespace cocos2d;
using namespace std;

extern "C" {

    JNIEXPORT void JNICALL Java_org_cocos2dx_lib_Cocos2dxHelper_nativeSetContext(JNIEnv*  env, jobject thiz, jobject context, jobject assetManager) {
        JniHelper::setClassLoaderFrom(context);
        FileUtilsAndroid::setassetmanager(AAssetManager_fromJava(env, assetManager));
    }

    JNIEXPORT void JNICALL Java_org_cocos2dx_lib_Cocos2dxHelper_nativeSetAudioDeviceInfo(JNIEnv*  env, jobject thiz, jboolean isSupportLowLatency, jint deviceSampleRate, jint deviceAudioBufferSizeInFrames) {
        __deviceSampleRate = deviceSampleRate;
        __deviceAudioBufferSizeInFrames = deviceAudioBufferSizeInFrames;
        LOGD("nativeSetAudioDeviceInfo: sampleRate: %d, bufferSizeInFrames: %d", __deviceSampleRate, __deviceAudioBufferSizeInFrames);
    }

    JNIEXPORT void JNICALL Java_org_cocos2dx_lib_Cocos2dxHelper_nativeSetEditTextDialogResult(JNIEnv * env, jobject obj, jbyteArray text) {
        jsize  size = env->GetArrayLength(text);

        if (size > 0) {
            jbyte * data = (jbyte*)env->GetByteArrayElements(text, 0);
            char* buffer = (char*)malloc(size+1);
            if (buffer != nullptr) {
                memcpy(buffer, data, size);
                buffer[size] = '\0';
                // pass data to edittext's delegate
                if (s_editTextCallback) s_editTextCallback(buffer, s_ctx);
                free(buffer);
            }
            env->ReleaseByteArrayElements(text, data, 0);
        } else {
            if (s_editTextCallback) s_editTextCallback("", s_ctx);
        }
    }

    JNIEXPORT void JNICALL Java_org_cocos2dx_lib_Cocos2dxHelper_nativeShowOptionDialogResult(JNIEnv * env, jobject obj, jlong callback, jint result) {

        CCDialogCallBack * cb = (CCDialogCallBack *)callback;
        if (cb != NULL) {
            Ref * obj = cb->m_refObj;
            // LOGD("ccobject getReferenceCount is %d in callback %p", obj->getReferenceCount(), cb);
            if (obj->getReferenceCount() > cb->m_oldCount) {
                cb->dialogCallBackWithReturnValue(result);
            }
            obj->autorelease();
        }
    }
}

const char * getApkPath() {
    if (g_apkPath.empty())
    {
        g_apkPath = JniHelper::callStaticStringMethod(className, "getAssetsPath");
    }

    return g_apkPath.c_str();
}

std::string getPackageNameJNI() {
    return JniHelper::callStaticStringMethod(className, "getCocos2dxPackageName");
}

int getObbAssetFileDescriptorJNI(const char* path, long* startOffset, long* size) {
    JniMethodInfo methodInfo;
    int fd = 0;

    if (JniHelper::getStaticMethodInfo(methodInfo, className.c_str(), "getObbAssetFileDescriptor", "(Ljava/lang/String;)[J")) {
        jstring stringArg = methodInfo.env->NewStringUTF(path);
        jlongArray newArray = (jlongArray)methodInfo.env->CallStaticObjectMethod(methodInfo.classID, methodInfo.methodID, stringArg);
        jsize theArrayLen = methodInfo.env->GetArrayLength(newArray);

        if (theArrayLen == 3) {
            jboolean copy = JNI_FALSE;
            jlong *array = methodInfo.env->GetLongArrayElements(newArray, &copy);
            fd = static_cast<int>(array[0]);
            *startOffset = array[1];
            *size = array[2];
            methodInfo.env->ReleaseLongArrayElements(newArray, array, 0);
        }

        methodInfo.env->DeleteLocalRef(methodInfo.classID);
        methodInfo.env->DeleteLocalRef(stringArg);
    }

    return fd;
}

int getDeviceSampleRate()
{
    return __deviceSampleRate;
}

int getDeviceAudioBufferSizeInFrames()
{
    return __deviceAudioBufferSizeInFrames;
}

void conversionEncodingJNI(const char* src, int byteSize, const char* fromCharset, char* dst, const char* newCharset)
{
    JniMethodInfo methodInfo;

    if (JniHelper::getStaticMethodInfo(methodInfo, className.c_str(), "conversionEncoding", "([BLjava/lang/String;Ljava/lang/String;)[B")) {
        jbyteArray strArray = methodInfo.env->NewByteArray(byteSize);
        methodInfo.env->SetByteArrayRegion(strArray, 0, byteSize, reinterpret_cast<const jbyte*>(src));

        jstring stringArg1 = methodInfo.env->NewStringUTF(fromCharset);
        jstring stringArg2 = methodInfo.env->NewStringUTF(newCharset);

        jbyteArray newArray = (jbyteArray)methodInfo.env->CallStaticObjectMethod(methodInfo.classID, methodInfo.methodID, strArray, stringArg1, stringArg2);
        jsize theArrayLen = methodInfo.env->GetArrayLength(newArray);
        methodInfo.env->GetByteArrayRegion(newArray, 0, theArrayLen, (jbyte*)dst);

        methodInfo.env->DeleteLocalRef(strArray);
        methodInfo.env->DeleteLocalRef(stringArg1);
        methodInfo.env->DeleteLocalRef(stringArg2);
        methodInfo.env->DeleteLocalRef(newArray);
        methodInfo.env->DeleteLocalRef(methodInfo.classID);
    }
}

#pragma mark - cronlygames
/* CronlyGames Inc. All Right Reserved here */
std::string getDeviceIdJNI() {
    JniMethodInfo t;
    std::string ret("");

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "getDeviceId", "()Ljava/lang/String;")) {
        jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID);

        ret = JniHelper::jstring2string(str);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(str);
    }

    return ret;
}

std::string getDeviceModelJNI() {
    JniMethodInfo t;
    std::string ret("");

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "getDeviceModel", "()Ljava/lang/String;")) {
        jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID);

        ret = JniHelper::jstring2string(str);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(str);
    }

    return ret;
}

bool isNetworkOpenJNI(bool showOpenWiFiDlg) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "isNetworkOpen", "(Z)Z")) {
        jboolean ret = t.env->CallStaticBooleanMethod(t.classID, t.methodID, showOpenWiFiDlg);

        t.env->DeleteLocalRef(t.classID);

        return ret;
    }

    return false;
}

std::string getNetworkTypeJNI() {
    return JniHelper::callStaticStringMethod(className, "getNetworkType");
}

float getBatteryLevelJNI() {
    return JniHelper::callStaticFloatMethod(className, "getBatteryLevel");
}

bool isAppInstalledJNI(const char * packageName) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "isAppInstalled", "(Ljava/lang/String;)Z")) {
        jstring arg = t.env->NewStringUTF(packageName);

        jboolean ret = t.env->CallStaticBooleanMethod(t.classID, t.methodID, arg);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(arg);

        return ret;
    }
    return true;
}

void openUrlJNI(const char * pUrlStr) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "openUrl", "(Ljava/lang/String;)V")) {
        jstring arg = t.env->NewStringUTF(pUrlStr);

        t.env->CallStaticVoidMethod(t.classID, t.methodID, arg);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(arg);
    }
}

void openAppJNI(const char * pStr) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "openApp", "(Ljava/lang/String;)V")) {
        jstring arg = t.env->NewStringUTF(pStr);

        t.env->CallStaticVoidMethod(t.classID, t.methodID, arg);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(arg);
    }
}

extern void showOptionDialog(Ref * obj, CCDialogCallBack * callback, const std::string &title, const std::string &message,
        const std::string& posAns, const std::string &negAns) {
    callback->m_refObj = obj;
    callback->m_oldCount = obj->getReferenceCount();
    obj->retain();

    // LOGD("ccobject is %p, count=%d, callback is %p", obj, obj->getReferenceCount(), callback);

    showOptionDialogJNI(callback, title.c_str(), message.c_str(), posAns.c_str(), negAns.c_str());
}

extern void showOptionDialogJNI(void * callback, const char * pTitle, const char * pMessage, const char * posAns, const char * negAns) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "showOptionDialog", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")) {
        jlong arg1 = (long)callback;
        jstring arg2 = t.env->NewStringUTF(pTitle);
        jstring arg3 = t.env->NewStringUTF(pMessage);
        jstring arg4 = t.env->NewStringUTF(posAns);
        jstring arg5 = t.env->NewStringUTF(negAns);

        t.env->CallStaticVoidMethod(t.classID, t.methodID, arg1, arg2, arg3, arg4, arg5);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(arg2);
        t.env->DeleteLocalRef(arg3);
        t.env->DeleteLocalRef(arg4);
        t.env->DeleteLocalRef(arg5);
    }
}

std::string getIpAddressJNI() {
    JniMethodInfo t;
    std::string ret("");

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "getWiFiIPAddress", "()Ljava/lang/String;")) {
        jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID);

        ret = JniHelper::jstring2string(str);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(str);
    }

    return ret;
}

void copyFileToSDCardJNI(const std::string &filename) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "copyFileToSDCard", "(Ljava/lang/String;)V")) {
        jstring arg = t.env->NewStringUTF(filename.c_str());

        t.env->CallStaticVoidMethod(t.classID, t.methodID, arg);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(arg);
    }

}

void shareFileJNI(const std::string &filename) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "shareFile", "(Ljava/lang/String;)V")) {
        jstring arg = t.env->NewStringUTF(filename.c_str());

        t.env->CallStaticVoidMethod(t.classID, t.methodID, arg);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(arg);
    }
}

void startLocationJNI() {
    JniHelper::callStaticVoidMethod(className, "startLocation");
}

void stopLocationJNI() {
    JniHelper::callStaticVoidMethod(className, "stopLocation");
}

std::string getLocationJNI(double & longitude, double &latitude, double & altitude) {
    JniMethodInfo t;
    std::string ret("");

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "getGPSLocation", "()Ljava/lang/String;")) {
        jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID);

        ret = JniHelper::jstring2string(str);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(str);
    }

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "getGPSParams", "()[D")) {
        jdoubleArray array = (jdoubleArray)t.env->CallStaticObjectMethod(t.classID, t.methodID);

        jsize len = t.env->GetArrayLength(array);
        if (len == 3) {
            double * p  = t.env->GetDoubleArrayElements(array, NULL);

            longitude = p[0];
            latitude  = p[1];
            altitude  = p[2];

            t.env->ReleaseDoubleArrayElements(array, p, 0);
        }

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(array);
    }

    return ret;
}

void startRecordJNI(const std::string & file) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "startRecord", "(Ljava/lang/String;)V")) {
        jstring arg = t.env->NewStringUTF(file.c_str());

        t.env->CallStaticVoidMethod(t.classID, t.methodID, arg);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(arg);
    }
}

void stopRecordJNI() {
    JniHelper::callStaticVoidMethod(className, "stopRecord");
}

void playVoiceJNI(const std::string & file) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, className.c_str(), "playVoice", "(Ljava/lang/String;)V")) {
        jstring arg = t.env->NewStringUTF(file.c_str());

        t.env->CallStaticVoidMethod(t.classID, t.methodID, arg);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(arg);
    }
}

bool isVoicePlayingJNI() {
    return JniHelper::callStaticBooleanMethod(className, "isVoicePlaying");
}

void checkUpdateJNI(const char * packageName) {
    JniHelper::callStaticVoidMethod(className, "checkUpdate", packageName);
}

void registerNotificationsJNI(const std::string & msg, const std::string & act) {
    JniHelper::callStaticVoidMethod(className, "registerNotifications", msg.c_str(), act.c_str());
}

void checkNotificationsJNI() {
    JniHelper::callStaticVoidMethod(className, "checkNotifications");
}


/* CronlyGames Inc. All Right Reserved here */

