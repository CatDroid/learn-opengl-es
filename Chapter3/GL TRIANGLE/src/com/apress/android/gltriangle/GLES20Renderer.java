package com.apress.android.gltriangle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

public class GLES20Renderer implements Renderer {
	private int _triangleProgram;
	private int _triangleAPositionLocation;
	private FloatBuffer _triangleVFB;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		initShapes();
		int _triangleVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _triangleVertexShaderCode);
		int _triangleFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _triangleFragmentShaderCode);
		_triangleProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_triangleProgram, _triangleVertexShader);
		GLES20.glAttachShader(_triangleProgram, _triangleFragmentShader);
		GLES20.glLinkProgram(_triangleProgram);
		_triangleAPositionLocation = GLES20.glGetAttribLocation(_triangleProgram, "aPosition");
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
	}

	public void onDrawFrame(GL10 gl) {
		gl.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glUseProgram(_triangleProgram);
		GLES20.glVertexAttribPointer(_triangleAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, _triangleVFB);
	    GLES20.glEnableVertexAttribArray(_triangleAPositionLocation);
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
	}

	private void initShapes()  {
		float triangleVFA[] = {
					-0.5f, 0.0f, 0.0f,
					0.5f, 0.0f, 0.0f,
					0.0f, 0.5f, 0.0f
				};
		ByteBuffer triangleVBB = ByteBuffer.allocateDirect(triangleVFA.length * 4);
		triangleVBB.order(ByteOrder.nativeOrder());
		_triangleVFB = triangleVBB.asFloatBuffer();
		_triangleVFB.put(triangleVFA);
		_triangleVFB.position(0);
	}

	private final String _triangleVertexShaderCode = 
			"attribute vec4 aPosition;				\n"
		+	"void main() {							\n"
		+	" gl_Position = aPosition;				\n"
		+	"}										\n";

	private final String _triangleFragmentShaderCode = 
			"void main() {							\n"
		+	" gl_FragColor = vec4(0,0,1,1);			\n"
		+	"}										\n";

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
	    return shader;
	}

}