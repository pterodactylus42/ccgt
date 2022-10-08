package be.tarsos.dsp.pitch;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.HashMap;

public class Spice implements PitchDetector {

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

    public Spice(final MappedByteBuffer modelFile) {
        this.tensorFlowModel = modelFile;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);
        interpreter = new Interpreter(modelFile, interpreterOptions);
        result = new PitchDetectionResult();
    }

    @Override
    public PitchDetectionResult getPitch(float[] audioBuffer) {
        assert audioBuffer.length % 1024 == 0;

        int inputTensorIndex = 0;
        DataType inputDataType = interpreter.getInputTensor(inputTensorIndex).dataType();

//        TensorBuffer inputBuffer = TensorBuffer.createDynamic(inputDataType);
//        inputBuffer.loadArray(audioBuffer, new int[] {audioBuffer.length});

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

//        outputProbabilityBuffers.forEach( (zahl,bytes) -> {
//            System.out.println("Arrayindex: " + zahl);
//            System.out.println(((ByteBuffer)bytes).get(0));
//            System.out.println(((ByteBuffer)bytes).get(1));
//            System.out.println(((ByteBuffer)bytes).get(2));
//        });


        return null;
    }
}
