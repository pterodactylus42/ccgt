package de.fff.ccgt.service;

import android.content.Context;
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

public class AudioService {

    private final PreferencesService preferencesService;

    private final static String TAG = AudioService.class.getSimpleName();

    private AudioDispatcher audioDispatcher;

    public AudioService(Context context) {
        preferencesService = new PreferencesService(context);
    }

    public void startAudio(PitchDetectionHandler pitchDetectionHandler, AudioProcessor fftProcessor) {
        startAudio(preferencesService.getAlgorithm(), pitchDetectionHandler, fftProcessor);
    }

        // TODO: 02.07.24 add enum with human readable names for Algorithms
    public void startAudio(PitchProcessor.PitchEstimationAlgorithm pitchAlgorithm, PitchDetectionHandler pitchDetectionHandler, AudioProcessor fftProcessor) {
        if(audioDispatcher == null) {
            int samplerate = preferencesService.getSampleRate();
            int buffersize = preferencesService.getBufferSize();
            audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(samplerate, buffersize, getOverlap());
            synchronized (this) { // TODO: 05.10.24 make highpass and lowpass freq configurable
                audioDispatcher.addAudioProcessor(new LowPassFS(3000, samplerate));
                audioDispatcher.addAudioProcessor(new HighPass(70, samplerate));
                AudioProcessor pitchProcessor = new PitchProcessor((PitchProcessor.PitchEstimationAlgorithm) pitchAlgorithm, samplerate, buffersize, pitchDetectionHandler);
                audioDispatcher.addAudioProcessor(pitchProcessor);
                audioDispatcher.addAudioProcessor(fftProcessor);
                new Thread(audioDispatcher, "audioDispatcher adding new processors").start();
            }
            Log.d(TAG, "startAudio: algorithm " + pitchAlgorithm + " samplerate " + samplerate + " buffersize " + buffersize + " overlap " + getOverlap());
        }
    }

    private int getOverlap() {
        return (preferencesService.getBufferSize()/4)*3;
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
            Log.d(TAG, "stopAudio: audioDispatcher stopped");
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
