#ifndef AUDIO_PLAYER_H_
#define AUDIO_PLAYER_H_

#include <jni.h>
#include <string>
#include "java_bridge.h"

namespace audio {

    void PlayAudioFromAssets(const std::string& filename);

}  // namespace audio

#endif  // AUDIO_PLAYER_H_
