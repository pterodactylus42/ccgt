package de.fff.ccgt.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.preference.PreferenceManager;
import be.tarsos.dsp.pitch.PitchProcessor;

public class PreferencesService {

    private final static String TAG = PreferencesService.class.getSimpleName();

    private Context context;
    private SharedPreferences sharedPreferences;

    private AudioService audioService;

    public PreferencesService(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        audioService = new AudioService();
    }

    public PitchProcessor.PitchEstimationAlgorithm getAlgorithm() {
        String algorithm = sharedPreferences.getString("algorithm", "");
        if(!algorithm.isEmpty()) {
            Log.d(TAG,"Set algorithm to " + algorithm);
            if(algorithm.equals("yin")) {
                return PitchProcessor.PitchEstimationAlgorithm.YIN;
            }
            if(algorithm.equals("yinfft")) {
                return PitchProcessor.PitchEstimationAlgorithm.FFT_YIN;
            }
            if(algorithm.equals("mpm")) {
                return PitchProcessor.PitchEstimationAlgorithm.MPM;
            }
        }
        return PitchProcessor.PitchEstimationAlgorithm.YIN;
    }

    public double getCalibrationFreq() {
        String calibrationFreq = sharedPreferences.getString("calibration", "");
        if(!calibrationFreq.isEmpty()) {
            Log.d(TAG,"Setting calibrationFreq to " + calibrationFreq);
            return Double.valueOf(calibrationFreq);
        }
        return 440.0;
    }

    public int getSampleRate() {
        String samplerate = sharedPreferences.getString("samplerate", "");
        if(!samplerate.isEmpty()) {
            audioService.getValidSampleRates();
            Log.d(TAG,"Setting samplerate to " + samplerate);
            return Integer.valueOf(samplerate);
        }
        return 8000;
    }

    public int getBufferSize() {
        String buffersize = sharedPreferences.getString("buffersize", "");
        if(!buffersize.isEmpty()) {
            Log.d(TAG,"Setting samplerate to " + buffersize);
            return Integer.valueOf(buffersize);
        }
        return 2048;
    }

    public boolean isDisplaySlow() {
        boolean slow = sharedPreferences.getBoolean("slow", false);
        Log.d(TAG,"Setting slow speed to " + slow);
        return slow;
    }

    public boolean isShowSplash() {
        boolean splash = sharedPreferences.getBoolean("splash", false);
        Log.d(TAG,"Setting show splash to " + splash);
        return splash;
    }

    public boolean isSpectrogramLogarithmic() {
        boolean logspectrogram = sharedPreferences.getBoolean("logspectrogram", false);
        Log.d(TAG,"Setting logarithmic spectrogram to " + logspectrogram);
        return logspectrogram;
    }

    public boolean isShowOctave() {
        boolean showoctave = sharedPreferences.getBoolean("showoctave", false);
        Log.d(TAG,"Setting show octave to " + showoctave);
        return showoctave;
    }

}
