package be.tarsos.dsp.pitch;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.HashMap;

public class Spice implements PitchDetector {
    /**
     * The default size of the audio buffer (in samples), which is mandatory in this PitchDetector.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * The default samplerate which is mandatory in this PitchDetector due to the used model.
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

    public Spice(final float samplerate, final int buffersize) {
        assert ((int) samplerate) == DEFAULT_SAMPLERATE;
        assert buffersize == DEFAULT_BUFFER_SIZE;
        result = new PitchDetectionResult();
    }

    public void setSpiceModel(final MappedByteBuffer spiceModel) {
        assert spiceModel != null;
        this.tensorFlowModel = spiceModel;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);
        // todo add gpu support
        interpreter = new Interpreter(spiceModel, interpreterOptions);
        assert isSpiceModelLoaded(interpreter);
    }

    @Override
    public PitchDetectionResult getPitch(float[] audioBuffer) {
        assert audioBuffer.length  == DEFAULT_BUFFER_SIZE;
        assert tensorFlowModel != null;

        int inputTensorIndex = 0;
        DataType inputDataType = interpreter.getInputTensor(inputTensorIndex).dataType();

        // 1024 samples == 64 ms audio == three pitch estimations by spice
        // as it chunks every 32 ms (0 32 64)
        int[] OutputShape = {3};
        // todo maybe parametrize from buffersize
        DataType OutputDataType;
        HashMap<Integer, Object> outputProbabilityBuffers = new HashMap<>();
        ByteBuffer x;
        for (int i = 0; i < interpreter.getOutputTensorCount(); i++) {
            OutputDataType = interpreter.getOutputTensor(i).dataType();
            x = TensorBuffer.createFixedSize(OutputShape, OutputDataType).getBuffer();
            outputProbabilityBuffers.put(i, x);
        }

        Object[] inputs = { audioBuffer };
        interpreter.runForMultipleInputsOutputs(inputs,outputProbabilityBuffers);

        ByteBuffer p = (ByteBuffer) outputProbabilityBuffers.get(0);
        ByteBuffer u = (ByteBuffer) outputProbabilityBuffers.get(1);

        result.setProbability((float) (1 - u.getFloat(4)));
        result.setPitch(spicePitch2Hz(p.getFloat(4)));
        // TODO: 09.10.22 how to accomplish isPitched?
        if(result.getProbability() > 0.5f) result.setPitched(true);
        return result;
    }

    private float spicePitch2Hz(final float spicePitch) {
        float PT_OFFSET = 25.58f;
        float PT_SLOPE = 63.07f;
        float FMIN = 10.0f;
        float BINS_PER_OCTAVE = 12.0f;
        float cqt_bin = spicePitch * PT_SLOPE + PT_OFFSET;
        return (float) (FMIN * Math.pow(2.0, 1.0 * cqt_bin / BINS_PER_OCTAVE));
    }

    private boolean isSpiceModelLoaded(final Interpreter interpreter) {
        assert interpreter != null;
        boolean result = true;
        if(interpreter.getInputTensorCount() != 1) result = false;
        if(interpreter.getInputTensor(0).dataType() != DataType.FLOAT32) result = false;
        if(interpreter.getInputTensor(0).numDimensions() != 1) result = false;
        if(interpreter.getInputTensor(0).numElements() != 1) result = false;
        if(interpreter.getInputTensor(0).quantizationParams().getZeroPoint() != 0) result = false;
        if(interpreter.getInputTensor(0).quantizationParams().getScale() != 0.0f) result = false;

        if(interpreter.getOutputTensorCount() != 2) result = false;
        if(interpreter.getOutputTensor(0).dataType() != DataType.FLOAT32) result = false;
        if(interpreter.getOutputTensor(0).numDimensions() != 1) result = false;
        if(interpreter.getOutputTensor(0).numElements() != 1) result = false;
        if(interpreter.getOutputTensor(0).quantizationParams().getZeroPoint() != 0) result = false;
        if(interpreter.getOutputTensor(0).quantizationParams().getScale() != 0.0f) result = false;

        return result;
    }

}
