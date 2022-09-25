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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

import de.fff.ccgt.classify.SpiceTFLiteModel;

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
        AssetFileDescriptor fileDescriptor = InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().openFd("tflite_model_tiny.tflite");
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
    public void loadModelObjectTest() {
        try
        {
            SpiceTFLiteModel model = new SpiceTFLiteModel(InstrumentationRegistry.getInstrumentation().getTargetContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 1024}, DataType.FLOAT32);
            //inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            // SpiceTFLiteModel.Outputs outputs = model.process(inputFeature0);
            // TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
        }
        catch (IOException e)
        {
            // TODO Handle the exception
        }
    }

    @Test
    public void runInferenceOnInterpreterWithModelFile(){
        Interpreter tflite = null;
        try {
            tflite = new Interpreter(loadModelFile());
            float[] inputVal=new float[1024];
            for(int i = 0; i < 1024; i++) {
                inputVal[i]= (float) Math.sin(i);
            }
            float[] output=new float[360];
            tflite.run(inputVal,output);
//            return output[];
            System.out.println("Max value in output: " + output.length);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

//    private float doInference(String inputString) {
//        float[] inputVal=new float[1];
//        inputVal[0]=Float.parseFloat(inputString);
//        float[][] output=new float[1][1];
//        tflite.run(inputVal,output);
//        return output[0][0];
//    }

}