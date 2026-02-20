//
// Created on 16.02.26.
//

#ifndef CCGT_NATIVEAUDIOSERVICE_H
#define CCGT_NATIVEAUDIOSERVICE_H

#include <oboe/Oboe.h>
#include "MyDataCallback.h"

class NativeAudioService {
public:

    oboe::Result open();

    oboe::Result start();

    oboe::Result stop();

    oboe::Result close();

private:

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

    static constexpr int kChannelCount = oboe::ChannelCount::Mono;
};


#endif //CCGT_NATIVEAUDIOSERVICE_H
