package com.macduo.android

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class DuoRenderer(private val bitmap: Bitmap) : GLSurfaceView.Renderer {

    private val vertices = floatArrayOf(
        // x, y, z, u, v
        -1f, -1f, 0f, 0f, 0f,
        1f, -1f, 0f, 1f, 0f,
        -1f, 1f, 0f, 0f, 1f,
        1f, 1f, 0f, 1f, 1f,
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(vertices)
        .apply { position(0) }

    private var program = 0
    private var textureId = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var mvpHandle = 0
    private var textureHandle = 0
    private var foldHandle = 0
    private var blurHandle = 0
    private var dimHandle = 0
    private var texSizeHandle = 0

    private val mvp = FloatArray(16)
    private val model = FloatArray(16)
    private val view = FloatArray(16)
    private val projection = FloatArray(16)
    private val tmp = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        foldHandle = GLES20.glGetUniformLocation(program, "uFold")
        blurHandle = GLES20.glGetUniformLocation(program, "uBlurRadius")
        dimHandle = GLES20.glGetUniformLocation(program, "uDim")
        texSizeHandle = GLES20.glGetUniformLocation(program, "uTexSize")

        textureId = uploadTexture(flipVertically(bitmap))
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projection, 0, 45f, aspect, 0.5f, 20f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val fold = FoldState.foldProgress
        val foldDegrees = fold * FoldState.MAX_FOLD_DEGREES

        Matrix.setIdentityM(model, 0)
        Matrix.translateM(model, 0, 0f, 1f, 0f)
        Matrix.rotateM(model, 0, foldDegrees, 1f, 0f, 0f)
        Matrix.translateM(model, 0, 0f, -1f, 0f)

        Matrix.setLookAtM(view, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(tmp, 0, view, 0, model, 0)
        Matrix.multiplyMM(mvp, 0, projection, 0, tmp, 0)

        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glUniform1f(foldHandle, fold)
        GLES20.glUniform1f(blurHandle, 24f)
        GLES20.glUniform1f(dimHandle, 0.9f)
        GLES20.glUniform2f(texSizeHandle, bitmap.width.toFloat(), bitmap.height.toFloat())

        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 20, vertexBuffer)

        vertexBuffer.position(3)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 20, vertexBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun uploadTexture(bitmap: Bitmap): Int {
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0])
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        return ids[0]
    }

    private fun flipVertically(source: Bitmap): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postScale(1f, -1f)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun buildProgram(vertexSource: String, fragmentSource: String): Int {
        val vertex = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertex)
        GLES20.glAttachShader(program, fragment)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        return shader
    }

    companion object {
        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            uniform mat4 uMVPMatrix;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vTexCoord = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            uniform float uFold;
            uniform float uBlurRadius;
            uniform float uDim;
            uniform vec2 uTexSize;
            void main() {
                float h = clamp(vTexCoord.y, 0.0, 1.0);
                float strength = uFold * (0.25 + 0.75 * h);
                vec2 stepUV = vec2(uBlurRadius * strength) / uTexSize;
                vec3 color = vec3(0.0);
                float total = 0.0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        vec2 uv = clamp(vTexCoord + vec2(float(i), float(j)) * stepUV, 0.0, 1.0);
                        color += texture2D(uTexture, uv).rgb;
                        total += 1.0;
                    }
                }
                color /= total;
                color *= (1.0 - uDim * strength);
                gl_FragColor = vec4(color, 1.0);
            }
        """
    }
}
