package com.apress.android.glrectangleelements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

public class GLES20Renderer implements Renderer {
	private int _rectangleProgram;
	private int _rectangleAPositionLocation;
	private FloatBuffer _rectangleVFB;
	private ShortBuffer _rectangleISB;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		initShapes();
		int _rectangleVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _rectangleVertexShaderCode);
		int _rectangleFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _rectangleFragmentShaderCode);
		_rectangleProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_rectangleProgram, _rectangleVertexShader);
		GLES20.glAttachShader(_rectangleProgram, _rectangleFragmentShader);
		GLES20.glLinkProgram(_rectangleProgram);
		_rectangleAPositionLocation = GLES20.glGetAttribLocation(_rectangleProgram, "aPosition");
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
	}

	public void onDrawFrame(GL10 gl) {
		gl.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glUseProgram(_rectangleProgram);
		GLES20.glVertexAttribPointer(_rectangleAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, _rectangleVFB);
	    GLES20.glEnableVertexAttribArray(_rectangleAPositionLocation);
	    GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, _rectangleISB);
	}

	private void initShapes()  {
		float rectangleVFA[] = {
					0.2f,0.2f,0.0f, -0.2f,0.2f,0.0f, -0.2f,-0.2f,0.0f, 0.2f,-0.2f,0.0f
				};
		ByteBuffer rectangleVBB = ByteBuffer.allocateDirect(rectangleVFA.length * 4);
		rectangleVBB.order(ByteOrder.nativeOrder());
		_rectangleVFB = rectangleVBB.asFloatBuffer();
		_rectangleVFB.put(rectangleVFA);
		_rectangleVFB.position(0);

		short rectangleISA[] = {
				0,1,2,
				2,3,0
			};
		ByteBuffer rectangleIBB = ByteBuffer.allocateDirect(rectangleISA.length * 2);
		rectangleIBB.order(ByteOrder.nativeOrder());
		_rectangleISB = rectangleIBB.asShortBuffer();
		_rectangleISB.put(rectangleISA);
		_rectangleISB.position(0);
	}

	private final String _rectangleVertexShaderCode = 
			"attribute vec4 aPosition;				\n"
		+	"void main() {							\n"
		+	" gl_Position = aPosition;				\n"
		+	"}										\n";

	private final String _rectangleFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH		\n"
		+	"precision highp float;					\n"
		+	"#else									\n"
		+	"precision mediump float;				\n"
		+	"#endif									\n"
		+	"void main() {							\n"
		+	" gl_FragColor = vec4(0,0,1,1);			\n"
		+	"}										\n";

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
	    return shader;
	}

}