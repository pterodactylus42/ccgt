package de.fff.ccgt.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import be.tarsos.dsp.util.PitchConverter;
import de.fff.ccgt.service.PreferencesService;


public class SpectrogramView extends View {

    private static final String TAG = SpectrogramView.class.getSimpleName();

    private boolean isSpectrogramLogarithmic;
    private int samplerate;
    private double pitch;
    private float[] inBuffer;
    private Paint pixelPaint;
    private double maxFrequency;
    private double minFrequency;
    private float[] amplitudesOnXAxis;

    public SpectrogramView(Context context) {
        super(context);
        init(new PreferencesService(getContext().getApplicationContext()));
    }

    public SpectrogramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(new PreferencesService(getContext().getApplicationContext()));
    }

    public SpectrogramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(new PreferencesService(getContext().getApplicationContext()));
    }

    private void init(final PreferencesService preferencesService) {
        isSpectrogramLogarithmic = preferencesService.isSpectrogramLogarithmic();
        samplerate = preferencesService.getSampleRate();
        pixelPaint = new Paint();
        pixelPaint.setColor(Color.GRAY);
        pixelPaint.setAntiAlias(true);
        pixelPaint.setStyle(Paint.Style.STROKE);
        minFrequency = 20;
        maxFrequency = samplerate / 2;
    }

    public void feedSpectrogramView(double actualPitch, float[] buffer) {
        pitch = actualPitch;
        inBuffer = buffer;
        invalidate();
    }

    /*
     * scale frequency of bin i to a x pixel value
     */
    private int frequencyToBin(final double frequency, int canvasWidth) {
        int bin = 0;
        if(frequency != 0 && frequency > minFrequency && frequency < maxFrequency) {
            double binEstimate;
            if(isSpectrogramLogarithmic) {
                final double minCent = PitchConverter.hertzToAbsoluteCent(minFrequency);
                final double maxCent = PitchConverter.hertzToAbsoluteCent(maxFrequency);
                final double absCent = PitchConverter.hertzToAbsoluteCent(frequency);
                binEstimate = (absCent - minCent) / maxCent * canvasWidth;
            } else {
                binEstimate  = ( (frequency - minFrequency) / (maxFrequency - minFrequency) ) * canvasWidth;
            }
            if(binEstimate > canvasWidth) {
                Log.d(TAG, "frequencyToBin: binEstimate exceeds view width: " + binEstimate);
            }
            bin = canvasWidth - 1 - (int) binEstimate;
            // if you prefer the other direction:
            // bin = (int) binEstimate - 1;
        }
        return bin;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if(inBuffer != null) {

            double maxAmplitude = 0;
            if(amplitudesOnXAxis == null) {
                amplitudesOnXAxis = new float[getWidth()];
            }

            for(int i = 0; i < inBuffer.length; i++) {
                // center frequency of bin i is i * samplerate / buffersize
                int pixelX = frequencyToBin( (i * (samplerate / inBuffer.length)), canvas.getWidth());
                amplitudesOnXAxis[pixelX] += inBuffer[i];
                maxAmplitude = Math.max(amplitudesOnXAxis[pixelX], maxAmplitude);
            }

            // Log.d(TAG,"onDraw: getWidth() " + getWidth() + " getHeight() " + getHeight() + " maxAmplitude " + maxAmplitude + " position " + position);

            for(int i = 0; i < amplitudesOnXAxis.length; i++) {
                //create a shade of grey depending on the given amplitude
                if(maxAmplitude != 0) {
                    final int greyValue = (int) (Math.log1p(amplitudesOnXAxis[i] / maxAmplitude) / Math.log1p(1.0000001) * 255);
                    pixelPaint.setColor(Color.rgb(greyValue,greyValue,greyValue));
                }
                canvas.drawLine(i, 0, i, getHeight(), pixelPaint);
                amplitudesOnXAxis[i] = 0.0f;
            }

            // draw the pitch slightly above the other pixels
            if(pitch != -1) {
                int pitchIndex = frequencyToBin(pitch, canvas.getWidth());
                pixelPaint.setColor(Color.RED);
                canvas.drawLine(pitchIndex, getHeight() / 2, pitchIndex, getHeight(), pixelPaint);
            }

            pixelPaint.setColor(Color.BLACK);

        }
    }

    public void setSpectrogramLogarithmic(boolean spectrogramLogarithmic) {
        isSpectrogramLogarithmic = spectrogramLogarithmic;
    }

    public void setSamplerate(int samplerate) {
        this.samplerate = samplerate;
    }

}
