package com.apress.android.glpointadvanced;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

public class GLES20Renderer implements Renderer {
	private int _pointProgram;
	private int _pointAVertexLocation;
	private FloatBuffer _pointVFB;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		initShapes();

		int pointVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _pointVertexShaderCode);
		int pointFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _pointFragmentShaderCode);
		_pointProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_pointProgram, pointVertexShader);
		GLES20.glAttachShader(_pointProgram, pointFragmentShader);
		GLES20.glLinkProgram(_pointProgram);

		_pointAVertexLocation = GLES20.glGetAttribLocation(_pointProgram, "aPosition");
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glUseProgram(_pointProgram);
		GLES20.glVertexAttribPointer(_pointAVertexLocation, 3, GLES20.GL_FLOAT, false, 12, _pointVFB);
	    GLES20.glEnableVertexAttribArray(_pointAVertexLocation);
	    GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 4);
	}

	private void initShapes()  {
		float[] pointVFA = {
				0.1f,0.1f,0.0f,
				-0.1f,0.1f,0.0f,
				-0.1f,-0.1f,0.0f,
				0.1f,-0.1f,0.0f
		};
		ByteBuffer pointVBB = ByteBuffer.allocateDirect(pointVFA.length * 4);
		pointVBB.order(ByteOrder.nativeOrder());
		_pointVFB = pointVBB.asFloatBuffer();
		_pointVFB.put(pointVFA);
		_pointVFB.position(0);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private final String _pointVertexShaderCode = 
			"attribute vec4 aPosition;				\n"
		+	"void main() {							\n"
		+	" gl_PointSize = 15.0;					\n"
		+	" gl_Position = aPosition;				\n"
		+	"}										\n";

	private final String _pointFragmentShaderCode = 
			"void main() {							\n"
		+	" gl_FragColor = vec4(1.0,1.0,1.0,1);	\n"
		+	"}										\n";

}
