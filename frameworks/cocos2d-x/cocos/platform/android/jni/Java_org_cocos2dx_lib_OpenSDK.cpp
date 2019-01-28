#include <string>

#include <jni.h>
#include <android/log.h>
#include "platform/android/jni/JniHelper.h"
#include "platform/android/jni/Java_org_cocos2dx_lib_OpenSDK.h"

#include "OpenSDK.h"

using namespace cocos2d;
using namespace std;

#define  LOG_TAG    "Java_org_cocos2dx_lib_OpenSDK.cpp"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

static const std::string className = "org/cocos2dx/lib/OpenSDK";

extern "C" {
    JNIEXPORT void JNICALL Java_org_cocos2dx_lib_OpenSDK_nativeInvokeCallback
        (JNIEnv*  env, jobject thiz, jint type, jint code, jstring desc)
        {
            std::string msg = JniHelper::jstring2string(desc);
            if (type == TYPE_LISTENER_LOGIN) {
                OpenSDK::notifyLoginEvent(code==0);
            } else if (type == TYPE_LISTENER_SHARE) {
                OpenSDK::notifyShareEvent(code, msg.c_str());
            } else {
                OpenSDK::notifyPayEvent(code, msg.c_str());
            }
        }
}


