package de.fff.ccgt.service;

public class NativeAudioService {
    static {
        System.loadLibrary("ccgt");
    }
    public static native int startAudioStreamNative();
    public static native int stopAudioStreamNative();
}
