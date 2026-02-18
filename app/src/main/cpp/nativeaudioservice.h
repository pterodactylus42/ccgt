//
// Created on 16.02.26.
//

#ifndef CCGT_NATIVEAUDIOSERVICE_H
#define CCGT_NATIVEAUDIOSERVICE_H

#include <oboe/Oboe.h>

class NativeAudioService {
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
        MyErrorCallback(NativeAudioService *parent) : mParent(parent) {}

        virtual ~MyErrorCallback() {
        }

        void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) override;

    private:
        NativeAudioService *mParent;
    };

    std::shared_ptr<oboe::AudioStream> mStream;
    std::shared_ptr<MyDataCallback> mDataCallback;
    std::shared_ptr<MyErrorCallback> mErrorCallback;

};


#endif //CCGT_NATIVEAUDIOSERVICE_H
