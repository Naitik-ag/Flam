#include <jni.h>
#include <android/log.h>
#include <vector>
using namespace std;
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "NDK", __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_flam_data_nat_NativeBridge_test(JNIEnv* env, jobject thiz) {
    LOGI("JNI is working");
    return env->NewStringUTF("Hello from C++");
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_flam_data_nat_NativeBridge_processFrame(
        JNIEnv* env,
        jobject thiz,
        jbyteArray frameData,
        jint width,
        jint height
) {
    // Convert incoming frame to C++ buffer
    jsize len = env->GetArrayLength(frameData);
    vector<uint8_t> input(len);
    env->GetByteArrayRegion(frameData, 0, len, reinterpret_cast<jbyte*>(input.data()));

    // Create dummy RGBA buffer (all white pixels)
    int pixelCount = width * height;
    std::vector<uint8_t> rgba(pixelCount * 4);

    for (int i = 0; i < pixelCount; i++) {
        rgba[i * 4 + 0] = 255; // R
        rgba[i * 4 + 1] = 255; // G
        rgba[i * 4 + 2] = 255; // B
        rgba[i * 4 + 3] = 255; // A
    }

    // Convert to jbyteArray
    jbyteArray output = env->NewByteArray(rgba.size());
    env->SetByteArrayRegion(output, 0, rgba.size(), reinterpret_cast<const jbyte*>(rgba.data()));
    return output;
}