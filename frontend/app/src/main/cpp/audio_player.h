#ifndef AUDIO_PLAYER_H_
#define AUDIO_PLAYER_H_

#include <jni.h>
#include <string>

namespace audio {

    void PlayAudioFromAssets(JNIEnv* env, const std::string& filename);

}  // namespace audio

#endif  // AUDIO_PLAYER_H_
