package de.fff.ccgt;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//import de.fff.ccgt.classify.SpiceTFLiteModel;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TensorFlowInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("de.fff.ccgt", appContext.getPackageName());
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().openFd("SpiceModel.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Test
    public void loadModelFileTest() throws IOException {
        MappedByteBuffer mappedByteBuffer = loadModelFile();
        assertNotNull(mappedByteBuffer);
    }

    @Test
    public void analyzeLoadedTFLiteModel(){
        Interpreter interpreter = null;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);
        try {
            interpreter = new Interpreter(loadModelFile(),interpreterOptions);
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
            for(int i = 0; i < inputTensorShape.length; i++) {
                System.out.print("["+inputTensorShape[i]+"]");
            }
            System.out.println("");
            System.out.println("- - - - - - - -");
            int probabilityTensorIndex = 0;
            System.out.println("Output Tensor DataType: " + interpreter.getOutputTensor(probabilityTensorIndex).dataType());
            System.out.print("Output Tensor Shape: ");
            int[] outputTensorShape = interpreter.getOutputTensor(probabilityTensorIndex).shape();
            for(int i = 0; i < outputTensorShape.length; i++) {
                System.out.print("["+outputTensorShape[i]+"]");
            }
            System.out.println("");
            System.out.println("- - - - - - - -");

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void runInterpreterWithModelFile(){
        Interpreter interpreter = null;
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(1);

        float[] sinewaveArray = new float[1024];
        for(int i = 0; i < sinewaveArray.length; i++) {
            sinewaveArray[i] = (float) Math.sin(i);
        }

        try {
            interpreter = new Interpreter(loadModelFile(),interpreterOptions);

            int inputTensorIndex = 0;
            DataType inputDataType = interpreter.getInputTensor(inputTensorIndex).dataType();

            TensorBuffer inputBuffer = TensorBuffer.createDynamic(inputDataType);
            inputBuffer.loadArray(sinewaveArray, new int[] {sinewaveArray.length});

            // The shape of *1* output's tensor
            int[] OutputShape = {3};
            // The type of the *1* output's tensor
            DataType OutputDataType;
            // The multi-tensor ready storage
            HashMap<Integer, Object> outputProbabilityBuffers = new HashMap<>();
            ByteBuffer x;
            // For each model's tensors (there are getOutputTensorCount() of them for this tflite model)
            for (int i = 0; i < interpreter.getOutputTensorCount(); i++) {
                OutputDataType = interpreter.getOutputTensor(i).dataType();
                x = TensorBuffer.createFixedSize(OutputShape, OutputDataType).getBuffer();
                outputProbabilityBuffers.put(i, x);
                System.out.println("Created a buffer of " + x.limit() + " bytes for tensor " + i +".");
            }

            System.out.println("Created a tflite output of " + outputProbabilityBuffers.size() + " output tensors.");

            Object[] inputs = { sinewaveArray };
            interpreter.runForMultipleInputsOutputs(inputs,outputProbabilityBuffers);

            outputProbabilityBuffers.forEach( (zahl,bytes) -> {
                System.out.println("Arrayindex: " + zahl);
                System.out.println(((ByteBuffer)bytes).get(0));
                System.out.println(((ByteBuffer)bytes).get(1));
                System.out.println(((ByteBuffer)bytes).get(2));
            });
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}