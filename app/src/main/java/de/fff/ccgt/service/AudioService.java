package de.fff.ccgt.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;

public class AudioService {

    private final static String TAG = AudioService.class.getSimpleName();
    private final Context context;

    private AudioDispatcher audioDispatcher;

    public AudioService(Context context) {
        this.context = context;
    }

    public void startAudio(final int samplerate, final int buffersize, final PitchProcessor.PitchEstimationAlgorithm pitchAlgorithm, final PitchDetectionHandler pitchDetectionHandler, final AudioProcessor fftProcessor) {
        // TODO: 07.02.26 add audio engine profiling for MPM usage
        if(PitchProcessor.PitchEstimationAlgorithm.MPM.equals(pitchAlgorithm)) {
            Toast.makeText(context, "Warning: MPM does not work with higher samplerates.", Toast.LENGTH_LONG).show();
        }
        if(audioDispatcher == null) {
            try {
                Log.d(TAG,"startAudio: trying with samplerate " + samplerate + " buffersize " + buffersize);
                // TODO: 02.02.26 add method for hardcoded overlap
                audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(samplerate, buffersize, buffersize / 4 * 3);
                synchronized (this) {
                    // TODO: 02.02.26 do filtering in own component
//                    audioDispatcher.addAudioProcessor(new LowPassFS(preferencesService.getLowpassFreq(), samplerate));
//                    audioDispatcher.addAudioProcessor(new HighPass(preferencesService.getHighpassFreq(), samplerate));
                    AudioProcessor pitchProcessor = new PitchProcessor(pitchAlgorithm, samplerate, buffersize, pitchDetectionHandler);
                    audioDispatcher.addAudioProcessor(pitchProcessor);
                    audioDispatcher.addAudioProcessor(fftProcessor);
                    new Thread(audioDispatcher, "audioDispatcher adding new processors").start();
                }
                Log.d(TAG, "startAudio: algorithm " + pitchAlgorithm + " samplerate " + samplerate + " buffersize " + buffersize );
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, "Failed to start Audio: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
            Log.d(TAG, "stopAudio: audioDispatcher stopped");
        }
    }

}
