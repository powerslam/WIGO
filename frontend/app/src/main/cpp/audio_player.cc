#include "audio_player.h"
#include "java_bridge.h"
#include <jni.h>
#include <android/log.h>

#define LOG_TAG "AudioPlayer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

namespace audio {

    void PlayAudioFromAssets(const std::string& filename) {
        JNIEnv* env = JavaBridge::GetEnv();
        if (!env) return;

        // java에 선언된 enqueue method를 사용하기 위해 FindClass 사용
        jclass clazz = JavaBridge::FindClass("com/capstone/whereigo/HelloArFragment");
        if (clazz == nullptr) {
            LOGD("❌ HelloArFragment 클래스 못 찾음 (JavaBridge)");
            return;
        }

        jmethodID queueMethod = env->GetStaticMethodID(clazz, "enqueueAudio", "(Ljava/lang/String;)V");
        if (queueMethod == nullptr) {
            LOGD("❌ enqueueAudio 메서드 못 찾음");
            return;
        }

        jstring jFilename = env->NewStringUTF(filename.c_str());
        env->CallStaticVoidMethod(clazz, queueMethod, jFilename);
        env->DeleteLocalRef(jFilename);
    }

}