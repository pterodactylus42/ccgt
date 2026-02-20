
#ifndef CCGT_MYDATACALLBACK_H
#define CCGT_MYDATACALLBACK_H

#include <oboe/Oboe.h>
#include "Yin.h"

class MyDataCallback : public oboe::AudioStreamDataCallback {

public:

    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream,
            void *audioData,
            int32_t numFrames)     {

        if (audioStream != mStream) {
            mStream = audioStream;
            mYin = std::make_shared<Yin>();
            mYin->initialize((float) mStream->getSampleRate(),mStream->getBufferSizeInFrames() * mStream->getChannelCount());
        }

//        oboe::ResultWithValue<int32_t> result = mStream->read(inputBuffer.get(), numFrames, 0);
//        mYin->getPitch(inputBuffer.get());
//        int32_t framesRead = result.value();
//        if (!result) {
//            inRef.requestStop();
//            return oboe::DataCallbackResult::Stop;
//        }
        return oboe::DataCallbackResult::Continue;
    }

private:
    oboe::AudioStream *mStream = nullptr;
    std::shared_ptr<Yin> mYin;

};

#endif //CCGT_MYDATACALLBACK_H
