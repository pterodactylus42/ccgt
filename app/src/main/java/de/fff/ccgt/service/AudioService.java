package de.fff.ccgt.service;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.filters.HighPass;
import be.tarsos.dsp.filters.LowPassFS;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

public class AudioService {

    private final static String TAG = AudioService.class.getSimpleName();

    // todo: use values from preferences
    public final static int SAMPLERATE = 8000;
    public final static int BUFFERSIZE = 2048;
    public final static int OVERLAP = (BUFFERSIZE/4)*3;
    public final static PitchEstimationAlgorithm DEFAULT_ALGORITHM = PitchProcessor.PitchEstimationAlgorithm.YIN;

    private AudioDispatcher audioDispatcher;

    public void startAudio(PitchDetectionHandler pitchDetectionHandler, AudioProcessor fftProcessor) {
        startAudio(DEFAULT_ALGORITHM, pitchDetectionHandler, fftProcessor);
    }

        // TODO: 02.07.24 add enum with human readable names for Algorithms
    public void startAudio(PitchProcessor.PitchEstimationAlgorithm pitchAlgorithm, PitchDetectionHandler pitchDetectionHandler, AudioProcessor fftProcessor) {
        if(audioDispatcher == null) {
            audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLERATE, BUFFERSIZE, OVERLAP);


            synchronized (this) {
                audioDispatcher.addAudioProcessor(new LowPassFS(3000, SAMPLERATE));
                audioDispatcher.addAudioProcessor(new HighPass(70, SAMPLERATE));
                AudioProcessor pitchProcessor = new PitchProcessor((PitchProcessor.PitchEstimationAlgorithm) pitchAlgorithm, SAMPLERATE, BUFFERSIZE, pitchDetectionHandler);
                audioDispatcher.addAudioProcessor(pitchProcessor);
                audioDispatcher.addAudioProcessor(fftProcessor);
                new Thread(audioDispatcher, "audioDispatcher adding new processors").start();
            }
        }
    }

    public void stopAudio() {
        if(audioDispatcher != null) {
            audioDispatcher.stop();
            audioDispatcher = null;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: 04.10.24 use as precondition for prefs selection
    public void getValidSampleRates() {
        for(int rate : new int[] {8000, 11025, 16000, 22050, 44100, 48000, 96000}) {
            //Returns: ERROR_BAD_VALUE if the recording parameters are not supported by the hardware, [...]
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if(bufferSize > 0) {
                Log.d(TAG, "getValidSampleRates: rate " + rate + " supported");
            }
        }
    }

}
