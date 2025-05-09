package com.capstone.whereigo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class AudioManager {
    private static AudioManager instance;

    private Context context;
    private MediaPlayer player;
    private final Queue<String> audioQueue = new LinkedList<>();
    private boolean isPlaying = false;

    private AudioManager() {
        // private constructor to enforce singleton
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void init(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    public void playTTS(String text) {
        Log.d("AudioManager", "🔊 TTS 재생 요청: " + text);
        String encoded = Uri.encode(text);
        String url = "http://54.70.209.130:8888/tts?text=" + encoded;

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("AudioManager", "TTS 재생 실패", e);
        }
    }

    public void enqueueAudio(String filename) {
        audioQueue.offer(filename);
        if (!isPlaying) {
            playNextAudio();
        }
    }

    private void playNextAudio() {
        String next = audioQueue.poll();
        if (next == null) {
            isPlaying = false;
            return;
        }

        if (context == null) {
            return;
        }

        try {
            AssetFileDescriptor afd = context.getAssets().openFd("audio/" + next);
            player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.setOnCompletionListener(mp -> {
                mp.release();
                playNextAudio();
            });
            player.start();
            isPlaying = true;
        } catch (IOException e) {
            Log.e("AudioManager", "로컬 오디오 재생 실패", e);
            isPlaying = false;
        }
    }
}
