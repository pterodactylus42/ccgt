package de.fff.ccgt;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import java.io.File;
import java.net.URISyntaxException;

public class TensorFlowUnitTest {
    @Test
    public void testLoadPackageLocalFile() {
        File file = new File("packagelocalfile");
        assertNotNull(file);
        System.out.println(file.getAbsolutePath());
    }

    @Test
    public void testLoadModel() throws URISyntaxException {
        File tfLiteModel = new File("app/src/main/assets/SpiceModel.tflite");
        assertNotNull(tfLiteModel);
        System.out.println(tfLiteModel.getAbsolutePath());
    }

}
