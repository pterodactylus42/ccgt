package be.tarsos.dsp.pitch;

import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.HashMap;

public class Crepe implements PitchDetector {
    /**
     * The default size of the audio buffer (in samples).
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * The default samplerate.
     */
    public static final int DEFAULT_SAMPLERATE = 16000;

    /**
     * The TensorFlow model handed over as a MappedByteBuffer.
     */
    private MappedByteBuffer tensorFlowModel;

    /**
     * The Interpreter to run inference on.
     */
    private Interpreter interpreter;

    /**
     * The result of the pitch detection iteration.
     */
    private final PitchDetectionResult result;

    /**
     * The result of the pitch detection iteration.
     */
    private final float[][] crepeBuffer;

    public Crepe(final float samplerate, final int buffersize) {
        assert ((int) samplerate) == DEFAULT_SAMPLERATE;
        assert buffersize == DEFAULT_BUFFER_SIZE;
        this.crepeBuffer = new float[1][DEFAULT_BUFFER_SIZE];
        this.result = new PitchDetectionResult();
    }

    @Override
    public void setModel(final MappedByteBuffer model) {
        assert model != null;
        this.tensorFlowModel = model;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);
        // todo add gpu support
        interpreter = new Interpreter(model, interpreterOptions);
        assert isCrepeModelLoaded(interpreter);
    }

    @Override
    public PitchDetectionResult getPitch(float[] audioBuffer) {
        assert audioBuffer != null;

        int inputTensorIndex = 0;
        DataType inputDataType = interpreter.getInputTensor(inputTensorIndex).dataType();

        HashMap<Integer, Object> outputProbabilityBuffers = new HashMap<>();
        ByteBuffer x;
        for (int i = 0; i < interpreter.getOutputTensorCount(); i++) {
            DataType OutputDataType = interpreter.getOutputTensor(i).dataType();
            int[] OutputShape = interpreter.getOutputTensor(0).shape();
            x = TensorBuffer.createFixedSize(OutputShape, OutputDataType).getBuffer();
            outputProbabilityBuffers.put(i, x);
        }

        for(int i = 0; i < audioBuffer.length; i++) {
            crepeBuffer[0][i] = audioBuffer[i] * (Float.MAX_VALUE - 1) ;
        }

        Object[][] inputs = { crepeBuffer };
        interpreter.runForMultipleInputsOutputs(inputs,outputProbabilityBuffers);
        ByteBuffer p = (ByteBuffer) outputProbabilityBuffers.get(0);
        p.rewind();
        float[] rawActivation = new float[360];
        int i = 0;
        while (p.remaining() > 0 && i < rawActivation.length) {
            float currentFloat = p.getFloat();
            rawActivation[i] = currentFloat;
            i++;
        }

        float detectedPitch = crepePitchBin2Hz(rawActivation);

        if(Float.isNaN(detectedPitch)) {
            result.setPitched(false);
            result.setPitch(-1.0f);
        } else {
            result.setPitched(true);
            result.setPitch(detectedPitch);
        }

        return result;
    }

    private float crepePitchBin2Hz(final float[] rawActivation ) {
        float cents = toLocalAverageCents(rawActivation);
        return (float) (10 * Math.pow(2,(cents / 1200)));
    }

    private float[] createCentsMapping() {
        float[] result = new float[360];

        for(int i =  0; i < result.length; i++) {
            result[i] = (i * (7180.0f / 360.0f)) + 1997.3794084376191f;
        }

        return result;
    }

    private float arraySum(final float[] array) {
        assert array != null;
        float result = 0.0f;
        for(int i = 0; i < array.length; i++) {
            result += array[i];
        }

        return result;
    }

    private float[] arrayProduct(final float[] arrayLeft, final float[] arrayRight) {
        assert arrayLeft != null;
        assert arrayRight != null;
        assert arrayLeft.length == arrayRight.length;

        float[] result = new float[arrayLeft.length];
        for(int i = 0; i < arrayLeft.length; i++) {
            result[i] = arrayLeft[i] * arrayRight[i];
        }

        return result;
    }

    private float toLocalAverageCents(final float[] rawActivation) {
        float productSum = arraySum(arrayProduct(rawActivation,createCentsMapping()));
        float weightSum = arraySum(rawActivation);
        return productSum / weightSum;
    }

    private boolean isCrepeModelLoaded(final Interpreter interpreter) {
        assert interpreter != null;
        boolean result = true;

        if(interpreter.getInputTensorCount() != 1) result = false;
        if(interpreter.getInputTensor(0).dataType() != DataType.FLOAT32) result = false;
        if(interpreter.getInputTensor(0).numDimensions() != 2) result = false;
        if(interpreter.getInputTensor(0).numElements() != 1024) result = false;
        if(interpreter.getInputTensor(0).quantizationParams().getZeroPoint() != 0) result = false;
        if(interpreter.getInputTensor(0).quantizationParams().getScale() != 0.0f) result = false;

        if(interpreter.getOutputTensorCount() != 1) result = false;
        if(interpreter.getOutputTensor(0).dataType() != DataType.FLOAT32) result = false;
        if(interpreter.getOutputTensor(0).numDimensions() != 2) result = false;
        if(interpreter.getOutputTensor(0).numElements() != 360) result = false;
        if(interpreter.getOutputTensor(0).quantizationParams().getZeroPoint() != 0) result = false;
        if(interpreter.getOutputTensor(0).quantizationParams().getScale() != 0.0f) result = false;

        return result;
    }

}