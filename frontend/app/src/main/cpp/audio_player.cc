#include "audio_player.h"
#include <jni.h>
#include <android/log.h>

#define LOG_TAG "AudioPlayer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

namespace audio {

    void PlayAudioFromAssets(JNIEnv* env, const std::string& filename) {
        jclass clazz = env->FindClass("com/capstone/whereigo/HelloArFragment");
        if (clazz == nullptr) {
            LOGD("❌ HelloArFragment 클래스 못 찾음");
            return;
        }

        jmethodID playMethod = env->GetStaticMethodID(clazz, "playLocalAudio", "(Ljava/lang/String;)V");
        if (playMethod == nullptr) {
            LOGD("❌ playLocalAudio 메서드 못 찾음");
            return;
        }

        jstring jFilename = env->NewStringUTF(filename.c_str());
        env->CallStaticVoidMethod(clazz, playMethod, jFilename);
        env->DeleteLocalRef(jFilename);
    }

}