package com.capstone.whereigo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.whereigo.databinding.ActivityMainBinding;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer, DisplayManager.DisplayListener {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private GLSurfaceView surfaceView;

    private NativeHolderViewModel viewModel;

    private int viewportWidth;
    private int viewportHeight;
    private boolean viewportChanged = false;

    private Handler planeStatusCheckingHandler;
    private Runnable planeStatusCheckingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        surfaceView = binding.surfaceview;
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);

        JniInterface.setClassLoader(this.getClass().getClassLoader());
        JniInterface.assetManager = getAssets();

        viewModel = new ViewModelProvider(this).get(NativeHolderViewModel.class);
        viewModel.initNativePtr(
                JniInterface.createNativeApplication(
                        getAssets(),
                        getExternalFilesDir("pose_graph").getAbsolutePath()
                )
        );

//        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                if (searchView.isShowing()) {
//                    searchView.hide();
//                } else {
//                    long currentTime = System.currentTimeMillis();
//                    if (currentTime - backPressedTime < backPressInterval) {
//                        setEnabled(false);
//                        getOnBackPressedDispatcher().onBackPressed();
//                    } else {
//                        backPressedTime = currentTime;
//                    }
//                }
//            }
//        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HelloArFragment())
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
        }

        try {
            JniInterface.onResume(viewModel.getNativePtr(), getApplicationContext(), this);
            surfaceView.onResume();
        } catch (Exception e) {
            Log.e(TAG, "Exception creating session", e);
            return;
        }

        getSystemService(DisplayManager.class).registerDisplayListener(this, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        surfaceView.onPause();
        JniInterface.onPause(viewModel.getNativePtr());
        getSystemService(DisplayManager.class).unregisterDisplayListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            JniInterface.destroyNativeApplication(viewModel.getNativePtr());
            viewModel = new ViewModelProvider(this).get(NativeHolderViewModel.class);
            viewModel.initNativePtr(0);
        }
    }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        JniInterface.onGlSurfaceCreated(viewModel.getNativePtr());
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
        viewportWidth = width;
        viewportHeight = height;
        viewportChanged = true;
    }

    @Override public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            if (viewModel.getNativePtr() == 0) return;
            if (viewportChanged) {
                int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
                JniInterface.onDisplayGeometryChanged(viewModel.getNativePtr(), displayRotation, viewportWidth, viewportHeight);
                viewportChanged = false;
            }

            JniInterface.onGlSurfaceDrawFrame(viewModel.getNativePtr(), false, false);
        }
    }

    @Override public void onDisplayAdded(int displayId) {}
    @Override public void onDisplayRemoved(int displayId) {}
    @Override public void onDisplayChanged(int displayId) {
        viewportChanged = true;
    }

}
