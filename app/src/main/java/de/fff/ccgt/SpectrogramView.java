package de.fff.ccgt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import be.tarsos.dsp.util.PitchConverter;


public class SpectrogramView extends View {

    private static final String TAG = SpectrogramView.class.getSimpleName();

    private int position;
    private double pitch;
    private float[] amplitudes;
    private Paint pixelPaint;


    public SpectrogramView(Context context) {
        super(context);
        init();
    }

    public SpectrogramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpectrogramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SpectrogramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        pixelPaint = new Paint();
        pixelPaint.setColor(Color.GRAY);
        pixelPaint.setAntiAlias(true);
        pixelPaint.setStyle(Paint.Style.STROKE);

        position = 0;
    }

    public void feedSpectrogramView(double actualPitch, float[] inBuffer) {
        pitch = actualPitch;
        amplitudes = inBuffer;
    }

    private int frequencyToBin(final double frequency, Canvas canvas) {
        final double minFrequency = 50; // Hz
        final double maxFrequency = 4000;
        int bin = 0;
        final boolean logarithmic = true;
        if(frequency != 0 && frequency > minFrequency && frequency < maxFrequency) {
            double binEstimate = 0;
            if(logarithmic) {
                final double minCent = PitchConverter.hertzToAbsoluteCent(minFrequency);
                final double maxCent = PitchConverter.hertzToAbsoluteCent(maxFrequency);
                final double absCent = PitchConverter.hertzToAbsoluteCent(frequency);
                binEstimate = (absCent - minCent) / maxCent * canvas.getWidth();
            } else {
                binEstimate  = ( (frequency - minFrequency) / (maxFrequency - minFrequency) ) * canvas.getWidth();
            }
            if(binEstimate > canvas.getWidth()) {
                Log.d(TAG, "frequencyToBin: binEstimate exceeds view width: " + binEstimate);
            }
            bin = canvas.getWidth() - 1 - (int) binEstimate;
            //bin = (int) binEstimate - 1;
            // if you prefer the other direction ....
        }
        return bin;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(amplitudes != null) {
            double maxAmplitude = 0;
            //for every pixel - in width of view - calculate an amplitude
            float[] pixeledAmplitudes = new float[canvas.getWidth()];

            //iterate the frequency magnitudes array and map to pixels
            for(int i = 0; i < amplitudes.length/2; i++) {
                // get the pixel for frequency of bin i
                int pixelX = frequencyToBin(i, canvas);
                pixeledAmplitudes[pixelX] += amplitudes[i];
                maxAmplitude = Math.max(pixeledAmplitudes[pixelX], maxAmplitude);
            }

            //draw the pixels
            for(int i = 0; i < pixeledAmplitudes.length; i++) {
                //create a shade of grey depending on the given amplitude
                if(maxAmplitude != 0) {
                    final int greyValue = (int) (Math.log1p(pixeledAmplitudes[i] / maxAmplitude) / Math.log1p(1.0000001) * 255);
                    pixelPaint.setColor(Color.rgb(greyValue,greyValue,greyValue));
                }
                canvas.drawLine(i, position, i, position + 15, pixelPaint);
            }

/*            //scale drawing
            pixelPaint.setColor(Color.LTGRAY);
            for(int i = 500; i < 8000; i += 500) {
                int bin = frequencyToBin(i, canvas);
                //bufferedGraphics.drawLine(0, bin, 5, bin);
                canvas.drawLine(bin, 0, bin, 1, pixelPaint);
            }*/

            // draw the pitch slightly above the other pixels
            if(pitch != -1) {
                int pitchIndex = frequencyToBin(pitch, canvas);
                pixelPaint.setColor(Color.RED);
                canvas.drawLine(pitchIndex, position - 15, pitchIndex, position, pixelPaint);
            }

            pixelPaint.setColor(Color.BLACK);

            invalidate();

            //to make the spectrogram flow for each new invalidate() :-)
            //position ++;
            //position = position % getHeight();

            // fixed position
            position = getHeight() / 2;

        }

    }

}
