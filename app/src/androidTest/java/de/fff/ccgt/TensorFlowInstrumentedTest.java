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

    private static final String SPICE_MODEL = "SpiceModel.tflite";
    private static final String CREPE_MODEL = "CrepeModel.tflite";

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("de.fff.ccgt", appContext.getPackageName());
    }

    private MappedByteBuffer loadSpiceModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().openFd(SPICE_MODEL);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private MappedByteBuffer loadCrepeModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().openFd(CREPE_MODEL);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Test
    public void loadSpiceModelFileTest() throws IOException {
        MappedByteBuffer mappedByteBuffer = loadSpiceModelFile();
        assertNotNull(mappedByteBuffer);
    }

    @Test
    public void loadCrepeModelFileTest() throws IOException {
        MappedByteBuffer mappedByteBuffer = loadCrepeModelFile();
        assertNotNull(mappedByteBuffer);
    }

    @Test
    public void analyzeLoadedSpiceModel(){
        Interpreter interpreter;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);
        try {
            interpreter = new Interpreter(loadSpiceModelFile(),interpreterOptions);
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
    public void runInterpreterWithSpiceModelFile() {
        Interpreter interpreter;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);

        float[] sinewaveArray = new float[1024];
        for(int i = 0; i < sinewaveArray.length; i++) {
            sinewaveArray[i] = (float) Math.sin(i);
        }

        try {
            interpreter = new Interpreter(loadSpiceModelFile(),interpreterOptions);

            int inputTensorIndex = 0;
            DataType inputDataType = interpreter.getInputTensor(inputTensorIndex).dataType();

            TensorBuffer inputBuffer = TensorBuffer.createDynamic(inputDataType);
            inputBuffer.loadArray(sinewaveArray, new int[] {sinewaveArray.length});

            // 1024 samples == 64 ms audio == three pitch estimations by spice
            // as it chunks every 32 ms (0 32 64)
            int[] OutputShape = {3};
            DataType OutputDataType;
            HashMap<Integer, Object> outputProbabilityBuffers = new HashMap<>();
            ByteBuffer x;
            for (int i = 0; i < interpreter.getOutputTensorCount(); i++) {
                OutputDataType = interpreter.getOutputTensor(i).dataType();
                x = TensorBuffer.createFixedSize(OutputShape, OutputDataType).getBuffer();
                outputProbabilityBuffers.put(i, x);
                System.out.println("Created a buffer of " + x.limit() + " bytes for tensor " + i +".");
            }

            System.out.println("Created a tflite output of " + outputProbabilityBuffers.size() + " output tensors.");

            Object[] inputs = { sinewaveArray };
            interpreter.runForMultipleInputsOutputs(inputs,outputProbabilityBuffers);

            ByteBuffer p = (ByteBuffer) outputProbabilityBuffers.get(0);
            p.rewind();
            System.out.println("pitches...");
            while (p.remaining() > 0) {
                float currentFloat = p.getFloat();
                System.out.println("buffer position: " + p.position() + " pitch : " + currentFloat + " " + spicePitch2Hz(currentFloat) + " Hz");
            }
            ByteBuffer u = (ByteBuffer) outputProbabilityBuffers.get(1);
            u.rewind();
            System.out.println("uncertainties...");
            while (u.remaining() > 0) {
                float currentUncertainty = u.getFloat();
                System.out.println("buffer position: " + u.position() + " uncertainty : " + currentUncertainty + " confidence : " + (1 - currentUncertainty));
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private float spicePitch2Hz(final float spicePitch) {
        float PT_OFFSET = 25.58f;
        float PT_SLOPE = 63.07f;
        float FMIN = 10.0f;
        float BINS_PER_OCTAVE = 12.0f;
        float cqt_bin = spicePitch * PT_SLOPE + PT_OFFSET;
        return (float) (FMIN * Math.pow(2.0, 1.0 * cqt_bin / BINS_PER_OCTAVE));
    }
}