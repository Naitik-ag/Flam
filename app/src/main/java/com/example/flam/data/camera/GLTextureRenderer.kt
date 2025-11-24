package com.example.flam.data.camera

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLTextureRenderer(
    private val glFrame: AtomicReference<ByteBuffer?>,
    private val frameWidthProvider: () -> Int,
    private val frameHeightProvider: () -> Int
) : GLSurfaceView.Renderer {

    private var program = 0
    private var texId = 0
    private var vertexBuffer: FloatBuffer
    private var uvBuffer: FloatBuffer
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0

    // track allocated texture size
    private var allocatedW = 0
    private var allocatedH = 0

    // fullscreen quad (x,y)
    private val VERTICES = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f,  1f,
        1f,  1f
    )

    private val UVS = floatArrayOf(
        1f, 1f,
        1f, 0f,
        0f, 1f,
        0f, 0f
    )

    init {
        vertexBuffer = ByteBuffer.allocateDirect(VERTICES.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(VERTICES); position(0)
            }
        uvBuffer = ByteBuffer.allocateDirect(UVS.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(UVS); position(0)
            }
    }

    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")
        textureHandle = GLES20.glGetUniformLocation(program, "u_Texture")

        // create texture
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        texId = tex[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(glUnused: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val frameBuf = glFrame.get() ?: return

        val w = frameWidthProvider()
        val h = frameHeightProvider()
        if (w <= 0 || h <= 0) return

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1)

        // allocate or reallocate texture storage only when size changes
        if (w != allocatedW || h != allocatedH) {
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                w,
                h,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
            )
            allocatedW = w
            allocatedH = h
        }

        // upload pixel data
        frameBuf.position(0)
        GLES20.glTexSubImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            0,
            0,
            w,
            h,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            frameBuf
        )

        // draw textured quad
        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, uvBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    private fun createShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e("GLTextureRenderer", "Could not compile shader: ${GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Shader compile failed")
        }
        return shader
    }

    private fun createProgram(vs: String, fs: String): Int {
        val v = createShader(GLES20.GL_VERTEX_SHADER, vs)
        val f = createShader(GLES20.GL_FRAGMENT_SHADER, fs)
        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p, v)
        GLES20.glAttachShader(p, f)
        GLES20.glLinkProgram(p)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("GLTextureRenderer", "Could not link program: ${GLES20.glGetProgramInfoLog(p)}")
            GLES20.glDeleteProgram(p)
            throw RuntimeException("Program link failed")
        }
        return p
    }

    companion object {
        private const val VERTEX_SHADER = """
        attribute vec4 a_Position;
        attribute vec2 a_TexCoord;
        varying vec2 v_TexCoord;
        void main() {
            gl_Position = a_Position;
            v_TexCoord = a_TexCoord;
        }
        """

        private const val FRAGMENT_SHADER = """
        precision mediump float;
        varying vec2 v_TexCoord;
        uniform sampler2D u_Texture;
        void main() {
            vec4 c = texture2D(u_Texture, v_TexCoord);
            // If colors look swapped (red/blue), use: gl_FragColor = vec4(c.b, c.g, c.r, c.a);
            gl_FragColor = c;
        }
        """
    }
}
