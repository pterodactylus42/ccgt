package de.fff.ccgt;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static java.lang.Float.NaN;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

/**
 * Instrumented test for the ml framework
 */
@RunWith(AndroidJUnit4.class)
public class TensorFlowInstrumentedTest {

    private static final String CREPE_MODEL = "CrepeModel.tflite";
    private static final String WAVEFILE = "whistle.wav";

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("de.fff.ccgt", appContext.getPackageName());
    }

    private MappedByteBuffer loadCrepeModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().openFd(CREPE_MODEL);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private ByteBuffer loadWaveBytes() throws IOException {
        AssetFileDescriptor fileDescriptor = InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().openFd(WAVEFILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        // TODO: 28.12.22 create constants for 4*float_size and the like
        MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 45L, 4096L);
        return byteBuffer.asReadOnlyBuffer();
    }

    @Test
    public void loadCrepeModelFileTest() throws IOException {
        MappedByteBuffer mappedByteBuffer = loadCrepeModelFile();
        assertNotNull(mappedByteBuffer);
    }

    @Test
    public void analyzeLoadedCrepeModel(){
        Interpreter interpreter;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);
        try {
            interpreter = new Interpreter(loadCrepeModelFile(),interpreterOptions);
            assertNotNull(interpreter);
            System.out.println("Model has " + interpreter.getInputTensorCount() + " input tensors,");
            System.out.println("DataType: " + interpreter.getInputTensor(0).dataType());
            System.out.println("Dimensions: " + interpreter.getInputTensor(0).numDimensions());
            System.out.println("NumElements: " + interpreter.getInputTensor(0).numElements());
            System.out.println("QuantizationParams ZeroPoint: " + interpreter.getInputTensor(0).quantizationParams().getZeroPoint());
            System.out.println("QuantizationParams Scale: " + interpreter.getInputTensor(0).quantizationParams().getScale());
            System.out.println("- - - - - - - -");
            System.out.println("Model has " + interpreter.getOutputTensorCount() + " output tensors,");
            System.out.println("DataType: " + interpreter.getOutputTensor(0).dataType());
            System.out.println("Dimensions: " + interpreter.getOutputTensor(0).numDimensions());
            System.out.println("NumElements: " + interpreter.getOutputTensor(0).numElements());
            System.out.println("QuantizationParams ZeroPoint: " + interpreter.getOutputTensor(0).quantizationParams().getZeroPoint());
            System.out.println("QuantizationParams Scale: " + interpreter.getOutputTensor(0).quantizationParams().getScale());
            System.out.println("- - - - - - - -");
            int inputTensorIndex = 0;
            System.out.println("Input Tensor DataType: " + interpreter.getInputTensor(inputTensorIndex).dataType());
            System.out.print("Input Tensor Shape: ");
            int[] inputTensorShape = interpreter.getInputTensor(inputTensorIndex).shape();
            for (int j : inputTensorShape) {
                System.out.print("[" + j + "]");
            }
            System.out.println("");
            System.out.println("- - - - - - - -");
            int probabilityTensorIndex = 0;
            System.out.println("Output Tensor DataType: " + interpreter.getOutputTensor(probabilityTensorIndex).dataType());
            System.out.print("Output Tensor Shape: ");
            int[] outputTensorShape = interpreter.getOutputTensor(probabilityTensorIndex).shape();
            for (int j : outputTensorShape) {
                System.out.print("[" + j + "]");
            }
            System.out.println("");
            System.out.println("- - - - - - - -");

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void loadWaveBytesTest() throws IOException {
        ByteBuffer bytes = loadWaveBytes();
        assertNotNull(bytes);
        while(bytes.hasRemaining()) {
            System.out.println("WaveBytes : " + bytes.getFloat());
        }
    }

    @Test
    public void loadFloatArrayFromWavefileTest() throws IOException {
        float[][] floatArray = loadFloatArrayForModel();
        assertNotNull(floatArray);
        for(int i = 0; i < floatArray[0].length; i++) {
            assertTrue("NaN values are not permitted", !Float.isNaN(floatArray[0][i]));
            System.out.println("Float from wavefile at index " + i + " " + floatArray[0][i]);
        }
    }

    private float[][] loadFloatArrayForModel() {
        float[][] floatArray = new float[1][1024];
        ByteBuffer b = null;
        try {
            b = loadWaveBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        float testValue = NaN;
        int positionForNextTry = 0;
        while(Float.isNaN(testValue) && positionForNextTry < 1024) {
            testValue = b.getFloat();
            if(testValue == NaN) {
                b.rewind();
                positionForNextTry++;
                b.position(positionForNextTry);
                System.out.println("Wavefile has no sane value at index " + positionForNextTry + " retrying from next element.");
            } else {
                b.position(positionForNextTry);
                System.out.println("Entering Wavefile at index " + positionForNextTry);
            }
        }
        float maximum = 0.0f;
        for(int i = 0; i < floatArray[0].length; i++) {
            floatArray[0][i] = b.getFloat();
            if(Float.isNaN(floatArray[0][i]) ) {
                System.out.println("Wavefile corrupted at index " + i + " value " + floatArray[0][i] + " setting a guessed new value");
                if(i > 0 && i < floatArray[0].length) {
                    floatArray[0][i] = (floatArray[0][i-1] + floatArray[0][i+1]) / 2;
                } else {
                    System.out.println("Warning: could not repair file at index " + i + " ...  setting to zero");
                    floatArray[0][i] = 0.0f;
                }
            }
            if(floatArray[0][i] > maximum) maximum = floatArray[0][i];
        }
        for(int i = 0; i < floatArray[0].length; i++) {
            floatArray[0][i] = (floatArray[0][i] / maximum);
        }
        return floatArray;
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

    @Test
    public void loadedModelIsCrepeModel() {
        Interpreter interpreter;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);
        try {
            interpreter = new Interpreter(loadCrepeModelFile(),interpreterOptions);
            assertNotNull(interpreter);
            assert isCrepeModelLoaded(interpreter);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void runInterpreterWithCrepeModelFile() {
        Interpreter interpreter;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);
        try {
            interpreter = new Interpreter(loadCrepeModelFile(),interpreterOptions);

            int inputTensorIndex = 0;
            DataType inputDataType = interpreter.getInputTensor(inputTensorIndex).dataType();

            HashMap<Integer, Object> outputProbabilityBuffers = new HashMap<>();
            ByteBuffer x;
            for (int i = 0; i < interpreter.getOutputTensorCount(); i++) {
                DataType OutputDataType = interpreter.getOutputTensor(i).dataType();
                int[] OutputShape = interpreter.getOutputTensor(0).shape();
                x = TensorBuffer.createFixedSize(OutputShape, OutputDataType).getBuffer();
                outputProbabilityBuffers.put(i, x);
                System.out.println("Created output buffer of " + x.limit() + " bytes for tensor at index " + i +".");
            }

            System.out.println("Created a tflite output of " + outputProbabilityBuffers.size() + " output tensors.");

            Object[][] inputs = { loadFloatArrayForModel() };
            interpreter.runForMultipleInputsOutputs(inputs,outputProbabilityBuffers);
            ByteBuffer p = (ByteBuffer) outputProbabilityBuffers.get(0);
            p.rewind();
            System.out.println("pitches...");
            float[] rawActivation = new float[360];
            int i = 0;
            while (p.remaining() > 0 && i < rawActivation.length) {
                float currentFloat = p.getFloat();
                System.out.println("buffer position: " + p.position()/4 + " value : " + currentFloat + " i " + i);
                rawActivation[i] = currentFloat;
                i++;
            }
            float detectedPitch = crepePitchBin2Hz(rawActivation);
            System.out.println("Detected pitch " + detectedPitch);

        } catch (Exception ex){
            ex.printStackTrace();
        }
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
        assertNotNull(array);
        float result = 0.0f;
        for(int i = 0; i < array.length; i++) {
            result += array[i];
        }

        return result;
    }

    private float[] arrayProduct(final float[] arrayLeft, final float[] arrayRight) {
        assertNotNull(arrayLeft);
        assertNotNull(arrayRight);
        assertEquals("Array length must match for arrayProduct",arrayLeft.length, arrayRight.length);
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

}