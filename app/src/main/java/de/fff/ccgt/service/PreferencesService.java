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

    public int getCalibrationFreq() {
        String calibrationFreq = sharedPreferences.getString("calibration", "");
        if(!calibrationFreq.isEmpty()) {
            return Integer.parseInt(calibrationFreq);
        }
        return 440;
    }

    public void setCalibrationFreq(int freq) {
        if(! (getCalibrationFreq() == freq) ) {
            Log.d(TAG,"Setting calibrationFreq to " + freq);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("calibration", String.valueOf(freq));
            editor.apply();
        }
    }

    public int getSampleRate() {
        String samplerate = sharedPreferences.getString("samplerate", "");
        if(!samplerate.isEmpty()) {
            Log.d(TAG,"samplerate " + samplerate);
            return Integer.parseInt(samplerate);
        }
        return 8000;
    }

    public int getBufferSize() {
        String buffersize = sharedPreferences.getString("buffersize", "");
        if(!buffersize.isEmpty()) {
            Log.d(TAG,"buffersize " + buffersize);
            return Integer.parseInt(buffersize);
        }
        return 2048;
    }

    public boolean isDisplaySlow() {
        boolean slow = sharedPreferences.getBoolean("slow", false);
        return slow;
    }

    public int getDisplayWaitTime() {
        if(isDisplaySlow()) {
            return SLOW_DISPLAY;
        }
        return FAST_DISPLAY;
    }

    public boolean isShowSplash() {
        boolean splash = sharedPreferences.getBoolean("splash", false);
        Log.d(TAG,"isShowSplash " + splash);
        return splash;
    }

    public boolean isSpectrogramLogarithmic() {
        boolean logspectrogram = sharedPreferences.getBoolean("logspectrogram", false);
        Log.d(TAG,"isSpectrogramLogarithmic " + logspectrogram);
        return logspectrogram;
    }

    public boolean isShowOctave() {
        boolean showoctave = sharedPreferences.getBoolean("showoctave", false);
        Log.d(TAG,"isShowOctave " + showoctave);
        return showoctave;
    }

    public boolean isKeepScreenOn() {
        boolean keepscreenon = sharedPreferences.getBoolean("keepscreenon", false);
        Log.d(TAG,"isKeepScreenOn " + keepscreenon);
        return keepscreenon;
    }

}
