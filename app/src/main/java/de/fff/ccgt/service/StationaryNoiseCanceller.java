package de.fff.ccgt.service;

import android.media.AudioFormat;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
//import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.filters.*;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.util.fft.FFT;
//import be.tarsos.dsp.io.jvm.WaveformWriter;

//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.util.Arrays;

/**
 * State-of-the-art stationary noise cancellation using TarsosDSP
 * Implements spectral subtraction with multi-stage filtering
 */
public class StationaryNoiseCanceller implements AudioProcessor {

    private final float sampleRate;
    private final int bufferSize;
    private final int overlap;

    // Noise profile
    private float[] noiseProfile;
    private int noiseProfileFrames = 0;
    private final int noiseEstimationFrames = 20; // ~0.5 seconds at typical settings
    private boolean noiseProfileComplete = false;

    // FFT processing
    private FFT fft;
    private float[] fftBuffer;
    private float[] window;

    // Spectral subtraction parameters
    private final float overSubtractionFactor = 2.0f;
    private final float spectralFloor = 0.002f; // -54dB floor to prevent musical noise
    private final float smoothingFactor = 0.8f; // Temporal smoothing
    private float[] previousMagnitude;

    // Pre/post filters
    private HighPass highPassFilter;
    private LowPassFS lowPassFilter;
    private BandPass bandPassFilter;

    public StationaryNoiseCanceller(float sampleRate, int bufferSize, int overlap) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.overlap = overlap;

        // Initialize FFT
        this.fft = new FFT(bufferSize);
        this.fftBuffer = new float[bufferSize * 2];
        this.noiseProfile = new float[bufferSize];
        this.previousMagnitude = new float[bufferSize];

        // Create Hann window for FFT
        this.window = createHannWindow(bufferSize);

        // Initialize filters
        // Remove DC offset and low rumble below 80Hz
        this.highPassFilter = new HighPass(80, sampleRate);

        // Remove high frequency noise above 8kHz
        this.lowPassFilter = new LowPassFS(8000, sampleRate);

        // Optional: focus on speech range (300-3400Hz) - comment out for full spectrum
        // this.bandPassFilter = new BandPass(300, 3400, sampleRate);
    }

    private float[] createHannWindow(int size) {
        float[] w = new float[size];
        for (int i = 0; i < size; i++) {
            w[i] = (float) (0.5 * (1 - Math.cos(2 * Math.PI * i / (size - 1))));
        }
        return w;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] audioBuffer = audioEvent.getFloatBuffer().clone();

        // Stage 1: Pre-filtering
        audioBuffer = applyPreFilters(audioBuffer);

        // Stage 2: Build noise profile (first N frames)
        if (!noiseProfileComplete) {
            updateNoiseProfile(audioBuffer);
            return true; // Pass through during noise learning
        }

        // Stage 3: Spectral subtraction
        audioBuffer = applySpectralSubtraction(audioBuffer);

        // Stage 4: Post-filtering
        audioBuffer = applyPostFilters(audioBuffer);

        // Copy processed audio back
        System.arraycopy(audioBuffer, 0, audioEvent.getFloatBuffer(), 0, audioBuffer.length);

        return true;
    }

    private float[] applyPreFilters(float[] buffer) {
        // High-pass filter to remove rumble
//        for (int i = 0; i < buffer.length; i++) {
//            buffer[i] = highPassFilter.processSingleSampleComponent(buffer[i], 0);
//        }

        // Low-pass filter to remove high-frequency noise
//        for (int i = 0; i < buffer.length; i++) {
//            buffer[i] = (float) lowPassFilter.processSingleSampleComponent(buffer[i], 0);
//        }

        // Optional bandpass for speech
//        if (bandPassFilter != null) {
//            for (int i = 0; i < buffer.length; i++) {
//                buffer[i] = (float) bandPassFilter.processSingleSampleComponent(buffer[i], 0);
//            }
//        }

        return buffer;
    }

    private void updateNoiseProfile(float[] buffer) {
        // Apply window
        for (int i = 0; i < bufferSize; i++) {
            fftBuffer[i] = buffer[i] * window[i];
        }

        // Forward FFT
        fft.forwardTransform(fftBuffer);

        // Accumulate magnitude spectrum
        for (int i = 0; i < bufferSize; i++) {
            float real = fftBuffer[2 * i];
            float imag = fftBuffer[2 * i + 1];
            float magnitude = (float) Math.sqrt(real * real + imag * imag);
            noiseProfile[i] += magnitude;
        }

        noiseProfileFrames++;

        if (noiseProfileFrames >= noiseEstimationFrames) {
            // Average the noise profile
            for (int i = 0; i < bufferSize; i++) {
                noiseProfile[i] /= noiseEstimationFrames;
            }
            noiseProfileComplete = true;
            System.out.println("Noise profile established. Processing audio...");
        }
    }

    private float[] applySpectralSubtraction(float[] buffer) {
        // Apply window
        for (int i = 0; i < bufferSize; i++) {
            fftBuffer[i] = buffer[i] * window[i];
        }

        // Forward FFT
        fft.forwardTransform(fftBuffer);

        // Process each frequency bin
        for (int i = 0; i < bufferSize; i++) {
            float real = fftBuffer[2 * i];
            float imag = fftBuffer[2 * i + 1];

            // Calculate magnitude and phase
            float magnitude = (float) Math.sqrt(real * real + imag * imag);
            float phase = (float) Math.atan2(imag, real);

            // Spectral subtraction with over-subtraction
            float noiseMag = noiseProfile[i] * overSubtractionFactor;
            float cleanMag = Math.max(magnitude - noiseMag, spectralFloor * magnitude);

            // Temporal smoothing to reduce musical noise
            cleanMag = smoothingFactor * previousMagnitude[i] + (1 - smoothingFactor) * cleanMag;
            previousMagnitude[i] = cleanMag;

            // Reconstruct complex number
            fftBuffer[2 * i] = cleanMag * (float) Math.cos(phase);
            fftBuffer[2 * i + 1] = cleanMag * (float) Math.sin(phase);
        }

        // Inverse FFT
        fft.backwardsTransform(fftBuffer);

        // Apply window again and normalize
        float[] output = new float[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            output[i] = fftBuffer[i] * window[i] / bufferSize;
        }

        return output;
    }

    private float[] applyPostFilters(float[] buffer) {
        // Light smoothing to reduce artifacts
        // Simple moving average
        float[] smoothed = new float[buffer.length];
        int windowSize = 3;

        for (int i = 0; i < buffer.length; i++) {
            float sum = 0;
            int count = 0;
            for (int j = -windowSize; j <= windowSize; j++) {
                int idx = i + j;
                if (idx >= 0 && idx < buffer.length) {
                    sum += buffer[idx];
                    count++;
                }
            }
            smoothed[i] = sum / count;
        }

        return smoothed;
    }

    @Override
    public void processingFinished() {
        System.out.println("Processing finished.");
    }

    // Main method for demonstration
//    public static void main(String[] args) throws LineUnavailableException {
//        // Configuration
//        float sampleRate = 44100;
//        int bufferSize = 1024;
//        int overlap = 512; // 50% overlap
//
//        // Input audio file
//        String inputFile = "noisy_audio.wav";
//        String outputFile = "clean_audio.wav";
//
//        // Create audio dispatcher
//        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(
//                new File(inputFile),
//                bufferSize,
//                overlap
//        );
//
//        // Add noise canceller
//        StationaryNoiseCanceller noiseCanceller =
//                new StationaryNoiseCanceller(sampleRate, bufferSize, overlap);
//        dispatcher.addAudioProcessor(noiseCanceller);
//
//        // Add writer to save output
//        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
//        dispatcher.addAudioProcessor(new WaveformWriter(format, outputFile));
//
//        System.out.println("Starting noise cancellation...");
//        System.out.println("Learning noise profile from first 0.5 seconds...");
//
//        // Run processing
//        dispatcher.run();
//
//        System.out.println("Output saved to: " + outputFile);
//    }

}
