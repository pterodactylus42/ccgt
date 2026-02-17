package de.fff.ccgt.service;

public class NativeAudioService {
    static {
        System.loadLibrary("ccgt-native-audio");
    }
    public static native int startAudioStreamNative();
    public static native int stopAudioStreamNative();
}
