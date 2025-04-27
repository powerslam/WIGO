/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef C_ARCORE_HELLOE_AR_HELLO_AR_APPLICATION_H_
#define C_ARCORE_HELLOE_AR_HELLO_AR_APPLICATION_H_

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/asset_manager.h>
#include <jni.h>

#include <memory>
#include <set>
#include <string>
#include <unordered_map>

#include "include/arcore/arcore_c_api.h"
#include "background_renderer.h"
#include "glm.h"
#include "obj_renderer.h"
#include "plane_renderer.h"
#include "point_cloud_renderer.h"
#include "texture.h"
#include "util.h"
#include "line_renderer.h"
#include "astar_pathfinding.h"
#include "audio_player.h"
#include "direction_utils.h"
#include "pose_helper.h"

#include "path_navigator.h"
#include "direction_helper.h"

namespace hello_ar {

// HelloArApplication handles all application logics.
class HelloArApplication {
 public:
  // Constructor and deconstructor.
  explicit HelloArApplication(AAssetManager* asset_manager);
  ~HelloArApplication();  // OnPause is called on the UI thread from the Activity's onPause method.
  
  void OnPause();

  // OnResume is called on the UI thread from the Activity's onResume method.
  void OnResume(JNIEnv* env, void* context, void* activity);

  // OnSurfaceCreated is called on the OpenGL thread when GLSurfaceView
  // is created.
  void OnSurfaceCreated();

  // OnDisplayGeometryChanged is called on the OpenGL thread when the
  // render surface size or display rotation changes.
  //
  // @param display_rotation: current display rotation.
  // @param width: width of the changed surface view.
  // @param height: height of the changed surface view.
  void OnDisplayGeometryChanged(int display_rotation, int width, int height);

  // OnDrawFrame is called on the OpenGL thread to render the next frame.
  void OnDrawFrame(bool depthColorVisualizationEnabled,
                   bool useDepthForOcclusion);

  // OnTouched is called on the OpenGL thread after the user touches the screen.
  // @param x: x position on the screen (pixels).
  // @param y: y position on the screen (pixels).
  void OnTouched(float x, float y);

  // Returns true if any planes have been detected.  Used for hiding the
  // "searching for planes" snackbar.
  bool HasDetectedPlanes() const { return plane_count_ > 0; }

  // Returns true if depth is supported.
  bool IsDepthSupported();

  void OnSettingsChange(bool is_instant_placement_enabled);

 private:
  glm::mat3 GetTextureTransformMatrix(const ArSession* session,
                                      const ArFrame* frame);
  ArSession* ar_session_ = nullptr;
  ArFrame* ar_frame_ = nullptr;

  JavaVM* java_vm_ = nullptr;

  bool install_requested_ = false;
  bool calculate_uv_transform_ = false;
  int width_ = 1;
  int height_ = 1;
  int display_rotation_ = 0;
  int current_path_index = 0;
  bool is_instant_placement_enabled_ = true;

  bool pitch_warning_issued_ = false;

  bool path_generated_ = false;
  bool path_ready_to_render_ = false;

  float plane_y_ = -1.6f;

  bool tts_direction_played_ = false;

  bool arrival_audio_played_ = false;

  LineRenderer line_renderer_;

  AAssetManager* const asset_manager_;
  int direction_match_count_;
  bool direction_check_enabled_;

  std::vector<Point> path;
  JNIEnv* GetJniEnv();

  PathNavigator path_navigator_;
  DirectionHelper direction_helper_;

  // The anchors at which we are drawing android models using given colors.
  struct ColoredAnchor {
    ArAnchor* anchor;
    ArTrackable* trackable;
    float color[4];
  };

  std::vector<ColoredAnchor> anchors_;
  std::vector<ColoredAnchor> carArrow_anchors_;
  ColoredAnchor location_pin_anchor_;


  PointCloudRenderer point_cloud_renderer_;
  BackgroundRenderer background_renderer_;
  PlaneRenderer plane_renderer_;
  ObjRenderer andy_renderer_;
  ObjRenderer location_pin_renderer_;
  ObjRenderer arrow_renderer_;
  ObjRenderer car_arrow_renderer_;
  Texture depth_texture_;

  int32_t plane_count_ = 0;

  bool tts_arrival_played_ = false;

  void ConfigureSession();

  void UpdateAnchorColor(ColoredAnchor* colored_anchor);
};
}  // namespace hello_ar

#endif  // C_ARCORE_HELLOE_AR_HELLO_AR_APPLICATION_H_
