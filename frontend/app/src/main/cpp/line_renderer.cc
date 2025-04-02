#include "line_renderer.h"
#include "util.h"  // shader utils

void LineRenderer::InitializeGlContent(AAssetManager* asset_manager) {
    const char* vertex_shader_path = "shaders/line.vert";
    const char* fragment_shader_path = "shaders/line.frag";

    program_ = hello_ar::util::CreateProgram(vertex_shader_path, fragment_shader_path, asset_manager);
    position_attrib_ = glGetAttribLocation(program_, "a_Position");
    mvp_uniform_ = glGetUniformLocation(program_, "u_MVP");

    glGenBuffers(1, &vbo_);  // VBO 생성
}

void LineRenderer::Draw(const std::vector<glm::vec3>& points,
                        const glm::mat4& projection_mat,
                        const glm::mat4& view_mat) {
    if (points.empty()) return;

    // 🔍 디버깅 로그 추가
    for (size_t i = 0; i < points.size(); ++i) {
        LOGI("🔸 Line Point %zu: (x=%.2f, y=%.2f, z=%.2f)", i, points[i].x, points[i].y, points[i].z);
    }

    glm::mat4 mvp = projection_mat * view_mat;

    glUseProgram(program_);
    // 🔥 블렌딩 활성화
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glUniformMatrix4fv(mvp_uniform_, 1, GL_FALSE, &mvp[0][0]);

    glBindBuffer(GL_ARRAY_BUFFER, vbo_);
    glBufferData(GL_ARRAY_BUFFER, points.size() * sizeof(glm::vec3),
                 points.data(), GL_DYNAMIC_DRAW);

    glEnableVertexAttribArray(position_attrib_);
    glVertexAttribPointer(position_attrib_, 3, GL_FLOAT, GL_FALSE, 0, 0);

    glLineWidth(10.0f);
    glDrawArrays(GL_LINE_STRIP, 0, static_cast<GLsizei>(points.size()));

    glDisableVertexAttribArray(position_attrib_);
    glBindBuffer(GL_ARRAY_BUFFER, 0);

    // 🔍 에러 체크 추가 (여기!)
    GLenum err = glGetError();
    if (err != GL_NO_ERROR) {
        LOGE("⚠️ OpenGL Error in LineRenderer::Draw: 0x%x", err);
    }
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    LOGI("🔹 그릴 라인 포인트 수: %zu", points.size());
}

