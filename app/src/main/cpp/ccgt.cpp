// TODO - open audio stream to record things
// TODO - read audio non-blocking to have continuous readings
// TODO - create audio input stream
// https://github.com/google/oboe/blob/main/docs/FullGuide.md
// TODO - which layering of audio processing is sensible?
// TODO - what comfort do you get from oboe?

#include <oboe/Oboe.h>
//#include <stdlib.h>
#include <android/log.h>
#include "ccgt.h"

using namespace oboe;

static const char *TAG = "ccgt";

oboe::Result ccgt::open() {
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

oboe::Result ccgt::start() {
    return mStream->requestStart();
}

oboe::Result ccgt::stop() {
    return mStream->requestStop();
}

oboe::Result ccgt::close() {
    return mStream->close();
}

DataCallbackResult ccgt::MyDataCallback::onAudioReady(
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

//class MyCallback : public oboe::AudioStreamDataCallback {
//public:
//    oboe::DataCallbackResult
//    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
//
//        // We requested AudioFormat::Float. So if the stream opens
//        // we know we got the Float format.
//        // If you do not specify a format then you should check what format
//        // the stream has and cast to the appropriate type.
//        auto *outputData = static_cast<float *>(audioData);
//
//        // Generate random numbers (white noise) centered around zero.
//        const float amplitude = 0.2f;
//        for (int i = 0; i < numFrames; ++i){
//            outputData[i] = ((float)drand48() - 0.5f) * 2 * amplitude;
//        }
//
//        return oboe::DataCallbackResult::Continue;
//    }
//};

void ccgt::MyErrorCallback::onErrorAfterClose(oboe::AudioStream *oboeStream,
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




