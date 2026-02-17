// https://github.com/google/oboe/blob/main/docs/FullGuide.md
// TODO - read audio non-blocking to have continuous readings
// TODO - which layering of audio processing is sensible?
// TODO - what comfort do you get from oboe?
// TODO - translate aubio to C++

#include <oboe/Oboe.h>
#include <android/log.h>
#include "nativeaudioservice.h"

using namespace oboe;

static const char *TAG = "NativeAudioService";

oboe::Result NativeAudioService::open() {
    mDataCallback = std::make_shared<MyDataCallback>();
    mErrorCallback = std::make_shared<MyErrorCallback>(this);

    AudioStreamBuilder builder;
    oboe::Result result = builder.setSharingMode(oboe::SharingMode::Shared)
            ->setDirection(oboe::Direction::Input)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setChannelCount(kChannelCount)
            //builder.setChannelCount(oboe::ChannelCount::Mono);
            ->setDataCallback(mDataCallback)
            ->setErrorCallback(mErrorCallback)
                    // Open using a shared_ptr.
            ->openStream(mStream);
    return result;
}

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
