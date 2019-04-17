package com.ardovic.carmovementsimulation.engine

import android.graphics.*
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_COLOR_BUFFER_BIT
import javax.microedition.khronos.opengles.GL10.GL_DEPTH_BUFFER_BIT

class Renderer(private val drawer: Drawer) : GLSurfaceView.Renderer {

    private var texture = Texture()
    private var lastT: Long = 0

    override fun onDrawFrame(gl: GL10) {
        val elapsedT = System.nanoTime() - lastT
        val minElapsedT = 16_666
        if (elapsedT < minElapsedT)
            try {
                Thread.sleep(minElapsedT - elapsedT)
            } catch (e: InterruptedException) {
                Log.d(TAG, e.message)
            }
        lastT = System.nanoTime()

        gl.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        gl.glLoadIdentity()
        gl.glRotatef(-180F, 1F, 0F, 0F)

        drawer.onDrawFrame(gl, this)

        val currentLayerData = texture.layerData
        val vertices = currentLayerData.getVertices()
        val indices = currentLayerData.getIndices()
        val textureCoordinates = currentLayerData.getTextureCoordinates()
        if (vertices != null && indices != null && textureCoordinates != null) {
            val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
            vbb.order(ByteOrder.nativeOrder())
            val vertexBuffer = vbb.asFloatBuffer()
            vertexBuffer.put(vertices)
            vertexBuffer.position(0)
            val ibb = ByteBuffer.allocateDirect(indices.size * 2)
            ibb.order(ByteOrder.nativeOrder())
            val indexBuffer = ibb.asShortBuffer()
            indexBuffer.put(indices)
            indexBuffer.position(0)
            val tbb = ByteBuffer.allocateDirect(textureCoordinates.size * 4)
            tbb.order(ByteOrder.nativeOrder())
            val textureBuffer = tbb.asFloatBuffer()
            textureBuffer.put(textureCoordinates)
            textureBuffer.position(0)
            val color = currentLayerData.argb
            val r = Color.red(color).toFloat() / 255
            val g = Color.green(color).toFloat() / 255
            val b = Color.blue(color).toFloat() / 255
            val a = Color.alpha(color).toFloat() / 255
            gl.glColor4f(r, g, b, a)
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.textureId)
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer)
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer)
            gl.glDrawElements(GL10.GL_TRIANGLES, indices.size, GL10.GL_UNSIGNED_SHORT, indexBuffer)
            currentLayerData.clear()
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        gl.glViewport(0, 0, width, height)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glOrthof(0f, width.toFloat(), (-height).toFloat(), 0F, -1F, 8F)
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        gl.glClearColor(0F, 0F, 0F, 0F)
        gl.glEnable(GL10.GL_CULL_FACE)
        gl.glCullFace(GL10.GL_BACK)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
        val textureIds = IntArray(1)
        gl.glGenTextures(0, IntArray(texture.textureId), 0)
        texture.textureId = textureIds[0]
        addTexture(gl, texture, textureIds[0])
    }

    private fun addTexture(gl: GL10, texture: Texture, textureId: Int) {
        val bitmap = generateSourceBitmap()
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT.toFloat())
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        texture.setDimensions(bitmap.width, bitmap.height)
        bitmap.recycle()
    }

    private fun generateSourceBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.rgb(255, 165, 0)
        canvas.drawRect(1F, 1F, 9F, 9F, paint)
        paint.color = Color.GREEN
        canvas.drawCircle(15F, 5F, 4F, paint)
        paint.color = Color.RED
        canvas.drawCircle(25F, 5F, 4F, paint)
        return bitmap
    }

    fun draw(src: Rect, dst: Rect, angle: Int) {
        texture.addSprite(src, dst, angle)
    }

    private companion object {
        private val TAG = Renderer::class.java.simpleName
    }

    interface Drawer {
        fun onDrawFrame(gl: GL10, renderer: Renderer)
    }
}