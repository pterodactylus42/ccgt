package de.fff.ccgt.service;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NativeAudioServiceTest {

    @Rule
    public final GrantPermissionRule grantPermissionRecordRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Test
    public void shouldRunNativeAudioService() {
        NativeAudioService.startAudioStreamNative();
        NativeAudioService.stopAudioStreamNative();
    }
}