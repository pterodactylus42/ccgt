// https://github.com/google/oboe/blob/main/docs/FullGuide.md
// TODO - translate aubio to C++
// TODO - move data out of callback with atomic fifo

#include <oboe/Oboe.h>
#include <android/log.h>
#include "nativeaudioservice.h"

using namespace oboe;

static const char *TAG = "NativeAudioService";

oboe::Result NativeAudioService::open() {
    mDataCallback = std::make_shared<MyDataCallback>();
    mErrorCallback = std::make_shared<MyErrorCallback>(this);

    // @see be.tarsos.dsp.io.android.AudioDispatcherFactory.fromDefaultMicrophone for stream settings
    AudioStreamBuilder builder;
    oboe::Result result = builder.setSharingMode(oboe::SharingMode::Shared)
            ->setDirection(oboe::Direction::Input)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setChannelCount(kChannelCount)
            ->setDataCallback(mDataCallback)
            ->setErrorCallback(mErrorCallback)
            ->openStream(mStream);
    return result;
}

//AudioStream::requestStart runs asynchronous
oboe::Result NativeAudioService::start() {
    return mStream->requestStart();
}

oboe::Result NativeAudioService::stop() {
    return mStream->requestStop();
}

oboe::Result NativeAudioService::close() {
    return mStream->close();
}

DataCallbackResult NativeAudioService::MyDataCallback::onAudioReady(
        AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) {

    float *output = (float *) audioData;

    int numSamples = numFrames * kChannelCount;
    for (int i = 0; i < numSamples; i++) {
        *output++ = (float) ((drand48() - 0.5) * 0.6);
    }
    return oboe::DataCallbackResult::Continue;
}

void NativeAudioService::MyErrorCallback::onErrorAfterClose(oboe::AudioStream *oboeStream,
                                                            oboe::Result error) {
    __android_log_print(ANDROID_LOG_INFO, TAG,
                        "%s() - error = %s",
                        __func__,
                        oboe::convertToText(error)
    );

    if (mParent->open() == Result::OK) {
        mParent->start();
    }
}
