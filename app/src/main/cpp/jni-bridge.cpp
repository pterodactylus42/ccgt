#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

static const char *TAG = "ccgtJNI";

#include <android/log.h>
#include "ccgt.h"

#ifdef __cplusplus
extern "C" {
#endif

using namespace oboe;

static ccgt audio;

JNIEXPORT jint JNICALL Java_de_fff_ccgt_service_NativeAudioService_startAudioStreamNative(
        JNIEnv * /* env */, jclass /* clazz */) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "%s", __func__);
    Result result = audio.open();
    if (result == Result::OK) {
        result = audio.start();
    }
    return (jint) result;
}

JNIEXPORT jint JNICALL Java_de_fff_ccgt_service_NativeAudioService_stopAudioStreamNative(
        JNIEnv * /* env */, jclass /* clazz */) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "%s", __func__);
    Result result1 = audio.stop();
    Result result2 = audio.close();
    return (jint) ((result1 != Result::OK) ? result1 : result2);
}
#ifdef __cplusplus
}
#endif
