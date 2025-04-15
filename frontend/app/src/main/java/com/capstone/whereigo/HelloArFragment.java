package com.capstone.whereigo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.capstone.whereigo.ui.DirectionCompassView;
import java.util.Locale;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.media.MediaPlayer;
import android.net.Uri;
import android.content.res.AssetFileDescriptor;
import java.io.IOException;
import android.content.Context;
import java.util.Queue;
import java.util.LinkedList;
import android.content.res.AssetManager;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;


public class HelloArFragment extends Fragment implements GLSurfaceView.Renderer, DisplayManager.DisplayListener {
  private static final String TAG = "HelloArFragment";
  private static final int SNACKBAR_UPDATE_INTERVAL_MILLIS = 1000;

  private GLSurfaceView surfaceView;
  private boolean viewportChanged = false;
  private int viewportWidth;
  private int viewportHeight;

  private Handler planeStatusCheckingHandler;
  private Runnable planeStatusCheckingRunnable;
  private View surfaceStatus;
  private TextView surfaceStatusText;
  private DirectionCompassView compassView;

  private static TextView cameraPoseTextView;
  private static TextView pathStatusTextView;

  private final DepthSettings depthSettings = new DepthSettings();
  private final boolean[] depthSettingsMenuDialogCheckboxes = new boolean[2];

  private final InstantPlacementSettings instantPlacementSettings = new InstantPlacementSettings();
  private final boolean[] instantPlacementSettingsMenuDialogCheckboxes = new boolean[1];

  private long nativeApplication;
  private GestureDetector gestureDetector;

  private Activity activity;

  private static HelloArFragment instance;
  private static Queue<String> audioQueue = new LinkedList<>();
  private static boolean isPlaying = false;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_hello_ar, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    activity = requireActivity();
    surfaceView = view.findViewById(R.id.surfaceview);


    surfaceStatus = view.findViewById(R.id.surface_status_container);
    surfaceStatusText = view.findViewById(R.id.surface_status_text);


    gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
      @Override
      public boolean onSingleTapUp(final MotionEvent e) {
        activity.runOnUiThread(() -> showOcclusionDialogIfNeeded());
        surfaceView.queueEvent(() -> JniInterface.onTouched(nativeApplication, e.getX(), e.getY()));
        return true;
      }

      @Override
      public boolean onDown(MotionEvent e) {
        return true;
      }
    });

    surfaceView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);

    cameraPoseTextView = new TextView(activity);
    cameraPoseTextView.setTextColor(Color.WHITE);
    cameraPoseTextView.setTextSize(14f);
    cameraPoseTextView.setPadding(20, 20, 20, 20);

    activity.addContentView(cameraPoseTextView, new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT));

    pathStatusTextView = view.findViewById(R.id.pathStatusTextView);

    JniInterface.assetManager = activity.getAssets();
    nativeApplication = JniInterface.createNativeApplication(activity.getAssets());

    planeStatusCheckingHandler = new Handler();

    depthSettings.onCreate(activity);
    instantPlacementSettings.onCreate(activity);

    compassView = view.findViewById(R.id.compassView);

    //ImageButton settingsButton = view.findViewById(R.id.settings_button);
    //settingsButton.setOnClickListener(v -> {
    //  PopupMenu popup = new PopupMenu(activity, v);
    //  popup.setOnMenuItemClickListener(this::settingsMenuClick);
    //  popup.inflate(R.menu.settings_menu);
    //  popup.show();
    //});
  }

  private boolean settingsMenuClick(MenuItem item) {
    if (item.getItemId() == R.id.depth_settings) {
      launchDepthSettingsMenuDialog();
      return true;
    } else if (item.getItemId() == R.id.instant_placement_settings) {
      launchInstantPlacementSettingsMenuDialog();
      return true;
    }
    return false;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!CameraPermissionHelper.hasCameraPermission(activity)) {
      CameraPermissionHelper.requestCameraPermission(activity);
      return;
    }

    try {
      JniInterface.onSettingsChange(
              nativeApplication, instantPlacementSettings.isInstantPlacementEnabled());
      JniInterface.onResume(nativeApplication, activity.getApplicationContext(), activity);
      surfaceView.onResume();
    } catch (Exception e) {
      Log.e(TAG, "Exception creating session", e);
      surfaceStatus.setVisibility(View.VISIBLE);
      surfaceStatusText.setText("AR 세션 오류: " + e.getMessage());
      return;
    }

    surfaceStatus.setVisibility(View.GONE);
//    surfaceStatusText.setText("Searching for surfaces...");

    pathStatusTextView.setVisibility(View.VISIBLE);

//    planeStatusCheckingHandler = new Handler();
//
//    planeStatusCheckingRunnable = () -> {
//      try {
//        if (JniInterface.hasDetectedPlanes(nativeApplication)) {
//          surfaceStatus.setVisibility(View.GONE);
//          pathStatusTextView.setVisibility(View.VISIBLE);
//        } else {
//          planeStatusCheckingHandler.postDelayed(planeStatusCheckingRunnable, SNACKBAR_UPDATE_INTERVAL_MILLIS);
//        }
//      } catch (Exception e) {
//        Log.e(TAG, e.getMessage());
//      }
//    };
//    planeStatusCheckingHandler.post(planeStatusCheckingRunnable);

    activity.getSystemService(DisplayManager.class).registerDisplayListener(this, null);
  }

  @Override
  public void onPause() {
    super.onPause();
    surfaceView.onPause();
    JniInterface.onPause(nativeApplication);
    if (planeStatusCheckingHandler != null && planeStatusCheckingRunnable != null) {
      planeStatusCheckingHandler.removeCallbacks(planeStatusCheckingRunnable);
    }
    activity.getSystemService(DisplayManager.class).unregisterDisplayListener(this);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    synchronized (this) {
      JniInterface.destroyNativeApplication(nativeApplication);
      nativeApplication = 0;
    }
  }


  private void showOcclusionDialogIfNeeded() {
    boolean isDepthSupported = JniInterface.isDepthSupported(nativeApplication);
    if (!depthSettings.shouldShowDepthEnableDialog() || !isDepthSupported) return;

    new AlertDialog.Builder(activity)
            .setTitle(R.string.options_title_with_depth)
            .setMessage(R.string.depth_use_explanation)
            .setPositiveButton(R.string.button_text_enable_depth,
                    (dialog, which) -> depthSettings.setUseDepthForOcclusion(true))
            .setNegativeButton(R.string.button_text_disable_depth,
                    (dialog, which) -> depthSettings.setUseDepthForOcclusion(false))
            .show();
  }

  private void launchDepthSettingsMenuDialog() {
    resetSettingsMenuDialogCheckboxes();
    Resources res = getResources();
    if (JniInterface.isDepthSupported(nativeApplication)) {
      new AlertDialog.Builder(activity)
              .setTitle(R.string.options_title_with_depth)
              .setMultiChoiceItems(res.getStringArray(R.array.depth_options_array),
                      depthSettingsMenuDialogCheckboxes,
                      (dialog, which, isChecked) -> depthSettingsMenuDialogCheckboxes[which] = isChecked)
              .setPositiveButton(R.string.done,
                      (dialog, which) -> applySettingsMenuDialogCheckboxes())
              .setNegativeButton(android.R.string.cancel,
                      (dialog, which) -> resetSettingsMenuDialogCheckboxes())
              .show();
    } else {
      new AlertDialog.Builder(activity)
              .setTitle(R.string.options_title_without_depth)
              .setPositiveButton(R.string.done,
                      (dialog, which) -> applySettingsMenuDialogCheckboxes())
              .show();
    }
  }

  private void launchInstantPlacementSettingsMenuDialog() {
    resetSettingsMenuDialogCheckboxes();
    Resources res = getResources();
    new AlertDialog.Builder(activity)
            .setTitle(R.string.options_title_instant_placement)
            .setMultiChoiceItems(res.getStringArray(R.array.instant_placement_options_array),
                    instantPlacementSettingsMenuDialogCheckboxes,
                    (dialog, which, isChecked) -> instantPlacementSettingsMenuDialogCheckboxes[which] = isChecked)
            .setPositiveButton(R.string.done,
                    (dialog, which) -> applySettingsMenuDialogCheckboxes())
            .setNegativeButton(android.R.string.cancel,
                    (dialog, which) -> resetSettingsMenuDialogCheckboxes())
            .show();
  }

  private void applySettingsMenuDialogCheckboxes() {
    depthSettings.setUseDepthForOcclusion(depthSettingsMenuDialogCheckboxes[0]);
    depthSettings.setDepthColorVisualizationEnabled(depthSettingsMenuDialogCheckboxes[1]);
    instantPlacementSettings.setInstantPlacementEnabled(instantPlacementSettingsMenuDialogCheckboxes[0]);
    JniInterface.onSettingsChange(nativeApplication, instantPlacementSettings.isInstantPlacementEnabled());
  }

  private void resetSettingsMenuDialogCheckboxes() {
    depthSettingsMenuDialogCheckboxes[0] = depthSettings.useDepthForOcclusion();
    depthSettingsMenuDialogCheckboxes[1] = depthSettings.depthColorVisualizationEnabled();
    instantPlacementSettingsMenuDialogCheckboxes[0] = instantPlacementSettings.isInstantPlacementEnabled();
  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
    JniInterface.onGlSurfaceCreated(nativeApplication);
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    viewportWidth = width;
    viewportHeight = height;
    viewportChanged = true;
  }

  @Override public void onDrawFrame(GL10 gl) {
    synchronized (this) {
      if (nativeApplication == 0) return;
      if (viewportChanged) {
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        JniInterface.onDisplayGeometryChanged(nativeApplication, displayRotation, viewportWidth, viewportHeight);
        viewportChanged = false;
      }
      JniInterface.onGlSurfaceDrawFrame(nativeApplication,
              depthSettings.depthColorVisualizationEnabled(),
              depthSettings.useDepthForOcclusion());
    }
  }

  @Override public void onDisplayAdded(int displayId) {}
  @Override public void onDisplayRemoved(int displayId) {}
  @Override public void onDisplayChanged(int displayId) {
    viewportChanged = true;
  }

  public static void updateYawFromNative(float cameraYaw, float pathYaw) {
    if (instance != null && instance.compassView != null) {
      instance.compassView.post(() -> instance.compassView.setYawValues(cameraYaw, pathYaw));
    }
  }

  public static void updatePoseFromNative(float[] pose) {
    String poseText = String.format(Locale.US,
            "Camera Pos: x=%.2f, y=%.2f, z=%.2f\nRot: x=%.2f, y=%.2f, z=%.2f, w=%.2f",
            pose[4], pose[5], pose[6], pose[0], pose[1], pose[2], pose[3]);
    if (cameraPoseTextView != null) {
      cameraPoseTextView.post(() -> cameraPoseTextView.setText(poseText));
    }



    float qx = pose[0];
    float qy = pose[1];
    float qz = pose[2];
    float qw = pose[3];

    float siny_cosp = 2 * (qw * qy + qx * qz);
    float cosy_cosp = 1 - 2 * (qy * qy + qz * qz);
    float yaw = (float) Math.toDegrees(Math.atan2(siny_cosp, cosy_cosp));
    if (yaw < 0) yaw += 360;

    if (instance != null && instance.compassView != null) {
        updateYawFromNative(yaw, /*pathYaw은 C++에서 updateYawFromNative로 따로 전달됨*/ 0f);
    }
  }

  public static void updatePathStatusFromNative(String status) {
    if (pathStatusTextView != null) {
      pathStatusTextView.post(() -> pathStatusTextView.setText(status));
    }
  }

  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    instance = this;
  }

  public static void playTTS(String text) {
    Log.d("TTS", "✅ playTTS 호출됨, text = " + text);
    String encoded = Uri.encode(text);
    String url = "http://54.70.209.130:8888/tts?text=" + encoded;

    MediaPlayer mediaPlayer = new MediaPlayer();
    try {
      mediaPlayer.setDataSource(url);
      mediaPlayer.setOnPreparedListener(MediaPlayer::start);
      mediaPlayer.prepareAsync();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public static void playLocalAudio(String filename) {
    try {
      AssetFileDescriptor afd = instance.requireActivity().getAssets().openFd("audio/" + filename);

      MediaPlayer mediaPlayer = new MediaPlayer();
      mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
      mediaPlayer.prepare();
      mediaPlayer.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void enqueueAudio(String filename) {
    audioQueue.offer(filename);
    if (!isPlaying) {
      playNextAudio();
    }
  }

  private static void playNextAudio() {
    String next = audioQueue.poll();
    if (next == null) {
      isPlaying = false;
      return;
    }

    isPlaying = true;
    MediaPlayer player = new MediaPlayer();
    try {
      AssetManager assetManager = instance.getContext().getAssets();
      AssetFileDescriptor afd = assetManager.openFd("audio/" + next);
      player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
      player.prepare();
      player.setOnCompletionListener(mp -> {
        mp.release();
        playNextAudio();  // 다음 오디오 재생
      });
      player.start();
    } catch (IOException e) {
      e.printStackTrace();
      isPlaying = false;
    }
  }

  public static void setCameraPoseVisibility(boolean visible) {
    if (cameraPoseTextView != null) {
      cameraPoseTextView.post(() -> cameraPoseTextView.setVisibility(visible ? View.VISIBLE : View.GONE));
    }
  }
  public static void vibrateOnce() {
    Vibrator vibrator = (Vibrator) instance.requireContext().getSystemService(Context.VIBRATOR_SERVICE);
    if (vibrator != null && vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(300); // deprecated but for older versions
        }
    }
}
}
