package de.fff.ccgt.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.preference.PreferenceManager;
import be.tarsos.dsp.pitch.PitchProcessor;

public class PreferencesService {

    private final static String TAG = PreferencesService.class.getSimpleName();
    private final static int SLOW_DISPLAY = 255;
    private final static int FAST_DISPLAY = 127;

    private final SharedPreferences sharedPreferences;

    public PreferencesService(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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
            return Double.parseDouble(calibrationFreq);
        }
        return 440.0;
    }

    public int getSampleRate() {
        String samplerate = sharedPreferences.getString("samplerate", "");
        if(!samplerate.isEmpty()) {
            Log.d(TAG,"Setting samplerate to " + samplerate);
            return Integer.parseInt(samplerate);
        }
        return 8000;
    }

    public int getBufferSize() {
        String buffersize = sharedPreferences.getString("buffersize", "");
        if(!buffersize.isEmpty()) {
            Log.d(TAG,"Setting buffersize to " + buffersize);
            return Integer.parseInt(buffersize);
        }
        return 2048;
    }

    public boolean isDisplaySlow() {
        boolean slow = sharedPreferences.getBoolean("slow", false);
//        Log.d(TAG,"isDisplaySlow " + slow);
        return slow;
    }

    public int getDisplayWaitTime() {
        if(isDisplaySlow()) {
//            Log.d(TAG,"Setting display wait time to " + SLOW_DISPLAY);
            return SLOW_DISPLAY;
        }
//        Log.d(TAG,"Setting display wait time to " + FAST_DISPLAY);
        return FAST_DISPLAY;
    }

    public boolean isShowSplash() {
        boolean splash = sharedPreferences.getBoolean("splash", false);
        Log.d(TAG,"isShowSplash " + splash + " sharedPreferences " + sharedPreferences.toString());
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
