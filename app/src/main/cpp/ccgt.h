//
// Created on 16.02.26.
//

#ifndef CCGT_CCGT_H
#define CCGT_CCGT_H

#include <oboe/Oboe.h>

class ccgt {
public:

    oboe::Result open();

    oboe::Result start();

    oboe::Result stop();

    oboe::Result close();

private:

    class MyDataCallback : public oboe::AudioStreamDataCallback {
    public:
        oboe::DataCallbackResult onAudioReady(
                oboe::AudioStream *audioStream,
                void *audioData,
                int32_t numFrames) override;

    };

    class MyErrorCallback : public oboe::AudioStreamErrorCallback {
    public:
        MyErrorCallback(ccgt *parent) : mParent(parent) {}

        virtual ~MyErrorCallback() {
        }

        void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) override;

    private:
        ccgt *mParent;
    };

    std::shared_ptr<oboe::AudioStream> mStream;
    std::shared_ptr<MyDataCallback> mDataCallback;
    std::shared_ptr<MyErrorCallback> mErrorCallback;

    static constexpr int kChannelCount = 2;
};


#endif //CCGT_CCGT_H
