package de.fff.ccgt.service;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NativeAudioServiceTest {
    @Test
    public void shouldRunNativeAudioService() {
        NativeAudioService.startAudioStreamNative();
        NativeAudioService.stopAudioStreamNative();
    }
}