package de.fff.ccgt.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.tarsos.dsp.pitch.PitchProcessor;
import de.fff.ccgt.R;

public class PreferencesService {

    private final static String TAG = PreferencesService.class.getSimpleName();
    private final static int SLOW_DISPLAY = 255;
    private final static int FAST_DISPLAY = 127;
    private final static int LOWPASS_FREQ = 3000;
    private final static int HIGHPASS_FREQ = 70;

    private List<Integer> referenceFrequencies;
    private final SharedPreferences sharedPreferences;

    public PreferencesService(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        initReferenceFrequencies(context);
    }

    private void initReferenceFrequencies(Context context) {
        String[] stringArray = context.getResources().getStringArray(R.array.calibration_values);
        final int len = stringArray.length;
        referenceFrequencies = new ArrayList<>(len);
        for (int j = 0; j < len; j++) {
            Integer i = Integer.parseInt(stringArray[j]);
            referenceFrequencies.add(i);
        }
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

    public int getCalibrationFreqIndex() {
        return referenceFrequencies.indexOf(getCalibrationFreq());
    }

    public int getCalibrationFreqFor(int index) {
        return referenceFrequencies.get(index);
    }

    public void setCalibrationFreq(int freq) {
        if(! (getCalibrationFreq() == freq) ) {
            Log.d(TAG,"Setting calibrationFreq to " + freq);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("calibration", String.valueOf(freq));
            editor.apply();
        }
    }

    public void setCalibrationFreqByIndex(int index) {
        setCalibrationFreq(getCalibrationFreqFor(index));
    }

    public int getSampleRate() {
        String samplerate = sharedPreferences.getString("samplerate", "");
        if(!samplerate.isEmpty()) {
            logValidSampleRates();
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

    public int getLowpassFreq() {
        return LOWPASS_FREQ;
    }

    public int getHighpassFreq() {
        return HIGHPASS_FREQ;
    }

    private void logValidSampleRates() {
        for(int rate : new int[] {8000, 11025, 16000, 22050, 44100, 48000, 96000}) {
            //Returns: ERROR_BAD_VALUE if the recording parameters are not supported by the hardware, [...]
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if(bufferSize > 0) {
                Log.d(TAG, "getValidSampleRates: rate " + rate + " supported");
            }
        }
    }

}
