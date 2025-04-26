#pragma once

#include <jni.h>

class JavaBridge {
public:
    static void SetJavaVM(JavaVM* vm);
    static void SetClassLoader(jobject class_loader);
    static JNIEnv* GetEnv();

    static void UpdateYaw(float cameraYaw, float pathYaw);
    static void UpdatePathStatus(const char* status);
    static void VibrateOnce();

private:
    static JavaVM* java_vm_;
    static jobject class_loader_;
    static jmethodID find_class_method_;

    static jclass FindClass(const char* class_name);
};
