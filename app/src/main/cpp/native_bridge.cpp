#include <jni.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "NDK", __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_flam_data_nat_NativeBridge_test(JNIEnv* env, jobject thiz) {
    LOGI("JNI is working");
    return env->NewStringUTF("Hello from C++");
}