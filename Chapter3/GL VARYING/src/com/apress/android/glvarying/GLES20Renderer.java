package com.apress.android.glvarying;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

public class GLES20Renderer implements Renderer {
	private int _lineProgram;
	private int _lineAVertexLocation;
	private int _lineAColorLocation;
	private FloatBuffer _lineVFB;
	private FloatBuffer _lineCFB;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		initShapes();

		int lineVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _lineVertexShaderCode);
		int lineFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _lineFragmentShaderCode);
		_lineProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_lineProgram, lineVertexShader);
		GLES20.glAttachShader(_lineProgram, lineFragmentShader);
		GLES20.glLinkProgram(_lineProgram);

		_lineAVertexLocation = GLES20.glGetAttribLocation(_lineProgram, "aPosition");
		_lineAColorLocation = GLES20.glGetAttribLocation(_lineProgram, "aColor");
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glUseProgram(_lineProgram);
	    GLES20.glVertexAttribPointer(_lineAVertexLocation, 3, GLES20.GL_FLOAT, false, 0, _lineVFB);
	    GLES20.glEnableVertexAttribArray(_lineAVertexLocation);
	    GLES20.glVertexAttribPointer(_lineAColorLocation, 4, GLES20.GL_FLOAT, false, 0, _lineCFB);
	    GLES20.glEnableVertexAttribArray(_lineAColorLocation);
	    GLES20.glLineWidth(3);
	    GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
	}

	private void initShapes()  {
		float lineVFA[] = {0.0f,0.0f,0.0f, 0.5f,0.5f,0.0f};
		ByteBuffer lineVBB = ByteBuffer.allocateDirect(lineVFA.length * 4);
		lineVBB.order(ByteOrder.nativeOrder());
		_lineVFB = lineVBB.asFloatBuffer();
		_lineVFB.put(lineVFA);
		_lineVFB.position(0);

		float lineCFA[] = {0,0,1,1, 1,1,0,1};
		ByteBuffer lineCBB = ByteBuffer.allocateDirect(lineCFA.length * 4);
		lineCBB.order(ByteOrder.nativeOrder());
		_lineCFB = lineCBB.asFloatBuffer();
		_lineCFB.put(lineCFA);
		_lineCFB.position(0);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private final String _lineVertexShaderCode = 
			"attribute vec4 aPosition;				\n"
		+	"attribute vec4 aColor;					\n"
		+	"varying vec4 vColor;					\n"
		+	"void main() {							\n"
		+	" vColor = aColor;						\n"
		+	" gl_Position = aPosition;				\n"
		+	"}										\n";

	private final String _lineFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH		\n"
		+	"precision highp float;					\n"
		+	"#else									\n"
		+	"precision mediump float;				\n"
		+	"#endif									\n"
		+	"varying vec4 vColor;					\n"
		+	"void main() {							\n"
		+	" gl_FragColor = vColor;				\n"
		+	"}										\n";

}
