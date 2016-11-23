package com.apress.android.glpointbasic;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

public class GLES20Renderer implements Renderer {
	private int _pointProgram;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		int pointVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _pointVertexShaderCode);
		int pointFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _pointFragmentShaderCode);
		_pointProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_pointProgram, pointVertexShader);
		GLES20.glAttachShader(_pointProgram, pointFragmentShader);
		GLES20.glLinkProgram(_pointProgram);
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glUseProgram(_pointProgram);
	    GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private final String _pointVertexShaderCode = 
			"void main() {							\n"
		+	" gl_PointSize = 15.0;					\n"
		+	" gl_Position = vec4(0.0,0.0,0.0,1);	\n"
		+	"}										\n";

	private final String _pointFragmentShaderCode = 
			"void main() {							\n"
		+	" gl_FragColor = vec4(1.0,1.0,1.0,1);	\n"
		+	"}										\n";

}
