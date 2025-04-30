package com.capstone.whereigo

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.io.IOException
import java.util.LinkedList
import java.util.Queue

object AudioManager {
    private var context: Context? = null
    private var player: MediaPlayer? = null
    private val audioQueue: Queue<String> = LinkedList()
    private var isPlaying = false

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun playTTS(text: String) {
        Log.d("AudioManager", "🔊 TTS 재생 요청: $text")
        val encoded = Uri.encode(text)
        val url = "http://54.70.209.130:8888/tts?text=$encoded"

        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.setOnPreparedListener { it.start() }
            mediaPlayer.prepareAsync()
        } catch (e: IOException) {
            Log.e("AudioManager", "TTS 재생 실패", e)
        }
    }

    fun enqueueAudio(filename: String) {
        audioQueue.offer(filename)
        if (!isPlaying) {
            playNextAudio()
        }
    }

    private fun playNextAudio() {
        val next = audioQueue.poll()
        if (next == null) {
            isPlaying = false
            return
        }

        val ctx = context ?: return
        try {
            val afd: AssetFileDescriptor = ctx.assets.openFd("audio/$next")
            player = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                setOnCompletionListener {
                    it.release()
                    playNextAudio()
                }
                start()
            }
            isPlaying = true
        } catch (e: IOException) {
            Log.e("AudioManager", "로컬 오디오 재생 실패", e)
            isPlaying = false
        }
    }
}
