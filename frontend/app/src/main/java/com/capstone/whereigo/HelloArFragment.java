package com.capstone.whereigo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.whereigo.databinding.FragmentHelloArBinding;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;
import com.capstone.whereigo.ui.DirectionCompassView;

import java.util.ArrayList;
import java.util.List;
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

import android.widget.Button;


public class HelloArFragment extends Fragment implements GLSurfaceView.Renderer, DisplayManager.DisplayListener {
  private static final String TAG = "HelloArFragment";
  private static final int SNACKBAR_UPDATE_INTERVAL_MILLIS = 1000;

  private static final int RECORD_AUDIO_REQUEST_CODE = 100;

  private GLSurfaceView surfaceView;
  private boolean viewportChanged = false;
  private int viewportWidth;
  private int viewportHeight;

  private Handler planeStatusCheckingHandler;
  private Runnable planeStatusCheckingRunnable;
  private View surfaceStatus;
  private TextView surfaceStatusText;
  private DirectionCompassView compassView;

  private GestureDetector gestureDetector;

  private long nativeApplication;
  private FragmentActivity activity;

  private static HelloArFragment instance;

  private static MediaPlayer player = null;

  private static Queue<String> audioQueue = new LinkedList<>();
  private static boolean isPlaying = false;
  private static Button elevatorButton;

  private FragmentHelloArBinding binding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    activity = requireActivity();

    binding = FragmentHelloArBinding.bind(
            inflater.inflate(R.layout.fragment_hello_ar, container, false)
    );

    SearchBar searchBar = binding.searchBar;
    searchBar.inflateMenu(R.menu.search_menu);
    searchBar.getMenu().findItem(R.id.action_voice_search).setOnMenuItemClickListener(item -> {
      if (checkAudioPermission()) {
        if (SpeechRecognizer.isRecognitionAvailable(activity)) {
          VoiceRecordDialog dialog = new VoiceRecordDialog();
          dialog.show(activity.getSupportFragmentManager(), "VoiceRecordDialog");
        } else {
          Toast.makeText(activity, "음성 인식을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
      } else {
        requestAudioPermission();
      }
      return true;
    });

    SearchView searchView = binding.searchView;
    searchView.setupWithSearchBar(searchBar);

    RecyclerView recyclerView = searchView.findViewById(R.id.search_result);

    List<String> allResults = new ArrayList<>();
    allResults.add("미래관 445호");
    allResults.add("미래관 447호");
    allResults.add("미래관 449호");
    allResults.add("미래관 444호");
    allResults.add("미래관 425호");
    allResults.add("미래관 415호");
    allResults.add("미래관 405호");

    searchView.getEditText().addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // 필요 없다면 비워둠
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String query = s.toString().trim();
        List<String> filtered = new ArrayList<>();
        for (String item : allResults) {
          if (item.toLowerCase().contains(query.toLowerCase())) {
            filtered.add(item);
          }
        }

        if (!query.isEmpty()) {
          recyclerView.setVisibility(View.VISIBLE);
          recyclerView.setLayoutManager(new LinearLayoutManager(activity));
          recyclerView.setAdapter(new SearchResultAdapter(filtered, selected -> {
          }));
        }

        else {
          recyclerView.setVisibility(View.GONE);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
        // 필요 없다면 비워둠
      }
    });

    surfaceView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);

    JniInterface.assetManager = activity.getAssets();
    nativeApplication = JniInterface.createNativeApplication(activity.getAssets(), this.getContext().getExternalFilesDir("pose_graph").getAbsolutePath(), true);

    planeStatusCheckingHandler = new Handler();

    return binding.getRoot();
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

  @Override
  public void onResume() {
    super.onResume();
    if (!CameraPermissionHelper.hasCameraPermission(activity)) {
      CameraPermissionHelper.requestCameraPermission(activity);
      return;
    }

    try {
      JniInterface.onResume(nativeApplication, activity.getApplicationContext(), activity);
      surfaceView.onResume();
    } catch (Exception e) {
      Log.e(TAG, "Exception creating session", e);
      return;
    }

    activity.getSystemService(DisplayManager.class).registerDisplayListener(this, null);
  }

  private boolean checkAudioPermission() {
    return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED;
  }

  private void requestAudioPermission() {
    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(requireContext(), "음성 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(requireContext(), "음성 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
      }
    }
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

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    TtsManager.INSTANCE.init(requireContext());
    AudioManager.getInstance().init(requireContext());

    elevatorButton = view.findViewById(R.id.btn_elevator);
    compassView = view.findViewById(R.id.compassView);
  }

  public void sendMultiGoalsToNative(float[] coords) {
    if (nativeApplication != 0 && coords != null && coords.length % 2 == 0) {
      Log.i("HelloArFragment", "📤 sendMultiGoalsToNative: 총 " + (coords.length / 2) + "개 좌표 전송");

      JniInterface.sendMultiGoalsToNative(nativeApplication, coords);
    } else {
      Log.e("HelloArFragment", "❌ nativeApplication 또는 coords 오류");
    }
  }

  public void loadPoseGraphFromFile(String filePath, int floor) {
    if (nativeApplication != 0) {
      JniInterface.loadPoseGraphFromFile(nativeApplication, filePath, floor);
    }
  }

  public void setCurrentFloor(int floor) {
    JniInterface.setCurrentFloor(nativeApplication, floor);
  }
  public static void updateYawFromNative(float cameraYaw, float pathYaw) {
    if (instance != null && instance.compassView != null) {
      // Log.d("HelloArFragment", "updateYawFromNative called: cameraYaw=" + cameraYaw + ", pathYaw=" + pathYaw);
//      Log.d("HelloArFragment", "updateYawFromNative called: cameraYaw=" + cameraYaw + ", pathYaw=" + pathYaw);
      instance.compassView.post(() -> instance.compassView.setYawValues(cameraYaw, pathYaw));
    }
  }

  @Override public void onDrawFrame(GL10 gl) {
    synchronized (this) {
      if (nativeApplication == 0) return;
      if (viewportChanged) {
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        JniInterface.onDisplayGeometryChanged(nativeApplication, displayRotation, viewportWidth, viewportHeight);
        viewportChanged = false;
      }
      JniInterface.onGlSurfaceDrawFrame(nativeApplication, false, false);
    }
  }

  @Override public void onDisplayAdded(int displayId) {}
  @Override public void onDisplayRemoved(int displayId) {}
  @Override public void onDisplayChanged(int displayId) {
    viewportChanged = true;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    synchronized (this) {
      JniInterface.destroyNativeApplication(nativeApplication);
      nativeApplication = 0;
    }
    binding = null;
  }

  public static void onGoalStatusChanged(int status) {
    if (instance == null) return;

    String message = (status == 0)
            ? "다음 목표로 이동합니다"
            : "목적지에 도착했습니다";

    instance.requireActivity().runOnUiThread(() -> {
      Toast.makeText(instance.requireContext(), message, Toast.LENGTH_SHORT).show();

      if (elevatorButton != null) {
        if (status == 0) {
          elevatorButton.setVisibility(View.VISIBLE);

          elevatorButton.setOnClickListener(v -> {
            JniInterface.restartSession(instance.nativeApplication, instance.activity.getApplicationContext(), instance.activity);
            JniInterface.changeStatus(instance.nativeApplication);
            JniInterface.setCurrentFloor(instance.nativeApplication, SearchResultHandler.goal_floor);
            elevatorButton.setVisibility(View.GONE); // 버튼 숨김
          });

        } else {
          elevatorButton.setVisibility(View.GONE); // 최종 도착 시 숨김
        }
      }
    });

    Log.d("NativeCallback", "🎯 onGoalStatusChanged: " + message);
  }

  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    instance = this;
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
