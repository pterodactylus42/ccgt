package de.fff.ccgt;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import java.io.File;
import java.net.URISyntaxException;

public class TensorFlowUnitTest {
    @Test
    public void testLoadPackageLocalFile() {
        File file = new File("howtoPublish-fDroid");
        assertNotNull(file);
        System.out.println(file.getAbsolutePath());
    }

    @Test
    public void testLoadModel() throws URISyntaxException {
        File tfLiteModel = new File("/Users/carstenneubauer/StudioProjects/ccgt/app/src/main/ml/tflite_model_tiny.tflite");
        assertNotNull(tfLiteModel);
        System.out.println(tfLiteModel.getAbsolutePath());
    }

}
