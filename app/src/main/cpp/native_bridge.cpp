#include <jni.h>
#include <android/log.h>
#include <vector>
#include <mutex>
#include <opencv2/opencv.hpp>

using namespace std;
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "NDK", __VA_ARGS__)

static std::mutex g_prev_mutex;
static cv::Mat g_prevGray; // persistent previous gray frame (protected by mutex)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_flam_data_nat_NativeBridge_test(JNIEnv* env, jobject thiz) {
    LOGI("JNI is working");
    return env->NewStringUTF("Hello from C++");
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_flam_data_nat_NativeBridge_processFrameNV21(
        JNIEnv *env,
        jobject thiz,
        jbyteArray nv21Data,
        jint width,
        jint height,
        jint mode,
        jint t1,
        jint t2
) {
    // Validate input
    if (nv21Data == nullptr || width <= 0 || height <= 0) return nullptr;

    jbyte* nv21Ptr = env->GetByteArrayElements(nv21Data, nullptr);
    if (!nv21Ptr) return nullptr;

    // Build YUV Mat from NV21 pointer (no copy)
    cv::Mat yuv(height + height / 2, width, CV_8UC1, reinterpret_cast<unsigned char*>(nv21Ptr));
    if (yuv.empty()) {
        env->ReleaseByteArrayElements(nv21Data, nv21Ptr, JNI_ABORT);
        return nullptr;
    }

    // Convert to BGR
    cv::Mat bgr;
    cv::cvtColor(yuv, bgr, cv::COLOR_YUV2BGR_NV21);

    // Pre-alloc mats
    cv::Mat gray, edges, rgba;
    bool motionDetected = false;
    const int motionThresh = 37; // fallback defaults

    switch (mode) {
        case 0: // RAW RGBA
            cv::cvtColor(bgr, rgba, cv::COLOR_BGR2RGBA);
            break;

        case 1: // GRAY
            cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);
            cv::cvtColor(gray, rgba, cv::COLOR_GRAY2RGBA);
            break;

        case 2: // CANNY EDGES
            cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);
            cv::Canny(gray, edges, t1, t2);
            cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA);
            break;

        case 3: { // THRESHOLD MODE
            cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);

            cv::Mat thresh;
            cv::threshold(gray, thresh, t1, 255, cv::THRESH_BINARY);

            cv::cvtColor(thresh, rgba, cv::COLOR_GRAY2RGBA);
            break;
        }
        default:
            cv::cvtColor(bgr, rgba, cv::COLOR_BGR2RGBA);
    }

    // MOTION DETECTION: compute only when we have a gray image available
    // Use gray if not already computed
    if (gray.empty()) {
        cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);
    }

    {
        // lock access to previous frame
        std::lock_guard<std::mutex> lock(g_prev_mutex);

        if (!g_prevGray.empty() && g_prevGray.size() == gray.size()) {
            // Compute absolute difference
            cv::Mat diff;
            cv::absdiff(gray, g_prevGray, diff);

            // Threshold the diff to get motion mask
            cv::Mat motionMask;
            cv::threshold(diff, motionMask, motionThresh, 255, cv::THRESH_BINARY);

            // Optional: morphological open to reduce noise
            cv::Mat kernel = getStructuringElement(cv::MORPH_RECT, cv::Size(3,3));
            morphologyEx(motionMask, motionMask, cv::MORPH_OPEN, kernel);
            morphologyEx(motionMask, motionMask, cv::MORPH_CLOSE, kernel);

            std::vector<std::vector<cv::Point>> contours;
            cv::findContours(motionMask, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);

            for (const auto &cnt : contours) {
                const double area = cv::contourArea(cnt);
                if (area < 800.0) continue;

                cv::Rect box = cv::boundingRect(cnt);
                cv::rectangle(rgba, box, cv::Scalar(255, 0, 0, 255), 2); // RGBA: R=255, G=0, B=0, A=255
                motionDetected = true;
            }

            if (motionDetected && rgba.rows > 4 && rgba.cols > 4) {
                for (int y = 1; y <= 3; ++y) {
                    for (int x = 1; x <= 3; ++x) {
                        cv::Vec4b &px = rgba.at<cv::Vec4b>(y, x);
                        px[0] = 0;   // Blue
                        px[1] = 0;   // Green
                        px[2] = 255; // Red
                        px[3] = 255; // Alpha
                    }
                }
            }
        }

        g_prevGray = gray.clone();
    }


    // Convert RGBA mat data to a jbyteArray
    int outSize = static_cast<int>(rgba.total() * rgba.elemSize()); // width * height * 4
    jbyteArray out = env->NewByteArray(outSize);
    if (out != nullptr) {
        env->SetByteArrayRegion(out, 0, outSize, reinterpret_cast<jbyte*>(rgba.data));
    }

    // Release NV21 buffer
    env->ReleaseByteArrayElements(nv21Data, nv21Ptr, JNI_ABORT);

    return out;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_flam_data_nat_NativeBridge_testOpenCV(
        JNIEnv* env,
        jobject /* this */) {

    cv::Mat test(100, 100, CV_8UC1, cv::Scalar(255));

    if (test.empty()) {
        return env->NewStringUTF("OpenCV FAILED");
    } else {
        return env->NewStringUTF("OpenCV WORKS!");
    }
}
