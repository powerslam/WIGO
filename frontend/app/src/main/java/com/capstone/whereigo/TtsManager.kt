package com.capstone.whereigo

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

object TtsManager {
    private var tts: TextToSpeech? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
                initialized = true
            }
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun shutdown() {
        tts?.shutdown()
        initialized = false
    }
}
