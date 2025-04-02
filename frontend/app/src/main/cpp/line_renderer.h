#ifndef LINE_RENDERER_H_
#define LINE_RENDERER_H_

#include <GLES2/gl2.h>
#include <android/asset_manager.h>
#include <glm/glm.hpp>
#include <vector>

class LineRenderer {
public:
    void InitializeGlContent(AAssetManager* asset_manager);
    void Draw(const std::vector<glm::vec3>& points,
              const glm::mat4& projection_mat,
              const glm::mat4& view_mat);

private:
    GLuint program_ = 0;
    GLint position_attrib_ = -1;
    GLint mvp_uniform_ = -1;
    GLuint vbo_ = 0;

};

#endif  // LINE_RENDERER_H_
