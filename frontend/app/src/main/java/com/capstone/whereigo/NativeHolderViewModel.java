package com.capstone.whereigo;

import androidx.lifecycle.ViewModel;
public class NativeHolderViewModel extends ViewModel {
    private long nativePtr = 0;
    private boolean isInitialized = false;

    public void initNativePtr(long ptr) {
        if (!isInitialized) {
            this.nativePtr = ptr;
            isInitialized = true;
        } else {
            throw new IllegalStateException("nativePtr is already initialized");
        }
    }

    public long getNativePtr() {
        if (!isInitialized) {
            throw new IllegalStateException("nativePtr is not initialized yet");
        }
        return nativePtr;
    }
}
