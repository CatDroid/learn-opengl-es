package com.apress.android.gldepthtest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;

public class GLES20Renderer implements Renderer {
	private int _lineAboveProgram, _lineBelowProgram;
	private int _lineAboveAVertexLocation, _lineBelowAVertexLocation;
	private FloatBuffer _lineAboveVFB, _lineBelowVFB;
	private int _lineAboveUMVPLocation, _lineBelowUMVPLocation;
	private float[] _ViewMatrix			= new float[16];
	private float[] _ProjectionMatrix	= new float[16];
	private float[] _MVPMatrix			= new float[16];

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		//GLES20.glDepthRangef(1,0);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		initShapes();

		int lineAboveVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _lineAboveVertexShaderCode);
		int lineAboveFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _lineAboveFragmentShaderCode);
		_lineAboveProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_lineAboveProgram, lineAboveVertexShader);
		GLES20.glAttachShader(_lineAboveProgram, lineAboveFragmentShader);
		GLES20.glLinkProgram(_lineAboveProgram);

		int lineBelowVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _lineBelowVertexShaderCode);
		int lineBelowFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _lineBelowFragmentShaderCode);
		_lineBelowProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_lineBelowProgram, lineBelowVertexShader);
		GLES20.glAttachShader(_lineBelowProgram, lineBelowFragmentShader);
		GLES20.glLinkProgram(_lineBelowProgram);

		_lineAboveAVertexLocation = GLES20.glGetAttribLocation(_lineAboveProgram, "aPosition");
		_lineAboveUMVPLocation = GLES20.glGetUniformLocation(_lineAboveProgram, "uMVP");
		_lineBelowAVertexLocation = GLES20.glGetAttribLocation(_lineBelowProgram, "aPosition");
		_lineBelowUMVPLocation = GLES20.glGetUniformLocation(_lineBelowProgram, "uMVP");

		float ratio	= (float) width / height;
		float zNear = 0.01f;
		float zFar = 1000;
		float fov = 40.0f / 60.0f;
		float size = (float) (zNear * Math.tan(fov / 2));
		Matrix.setLookAtM(_ViewMatrix, 0, 0, 0, 3, 0, 0, 0, 0, 1, 0);
		Matrix.frustumM(_ProjectionMatrix, 0, -size, size, -size / ratio, size / ratio, zNear, zFar);
		Matrix.multiplyMM(_MVPMatrix, 0, _ProjectionMatrix, 0, _ViewMatrix, 0);
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glUseProgram(_lineAboveProgram);
		GLES20.glUniformMatrix4fv(_lineAboveUMVPLocation, 1, false, _MVPMatrix, 0);
	    GLES20.glVertexAttribPointer(_lineAboveAVertexLocation, 3, GLES20.GL_FLOAT, false, 0, _lineAboveVFB);
	    GLES20.glEnableVertexAttribArray(_lineAboveAVertexLocation);
	    GLES20.glLineWidth(10);
	    GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

		GLES20.glUseProgram(_lineBelowProgram);
		GLES20.glUniformMatrix4fv(_lineBelowUMVPLocation, 1, false, _MVPMatrix, 0);
	    GLES20.glVertexAttribPointer(_lineBelowAVertexLocation, 3, GLES20.GL_FLOAT, false, 0, _lineBelowVFB);
	    GLES20.glEnableVertexAttribArray(_lineBelowAVertexLocation);
	    GLES20.glLineWidth(10);
	    GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
	}

	private void initShapes()  {
		float lineAboveVFA[] = {0.0f,0.0f,-0.25f, -0.5f,0.5f,-0.25f,};
		ByteBuffer lineAboveVBB = ByteBuffer.allocateDirect(lineAboveVFA.length * 4);
		lineAboveVBB.order(ByteOrder.nativeOrder());
		_lineAboveVFB = lineAboveVBB.asFloatBuffer();
		_lineAboveVFB.put(lineAboveVFA);
		_lineAboveVFB.position(0);

		float lineBelowVFA[] = {-0.5f,0.0f,-0.5f, 0.0f,0.5f,-0.5f,};
		ByteBuffer lineBelowVBB = ByteBuffer.allocateDirect(lineBelowVFA.length * 4);
		lineBelowVBB.order(ByteOrder.nativeOrder());
		_lineBelowVFB = lineBelowVBB.asFloatBuffer();
		_lineBelowVFB.put(lineBelowVFA);
		_lineBelowVFB.position(0);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private final String _lineAboveVertexShaderCode = 
			"attribute vec4 aPosition;				\n"
		+	"uniform mat4 uMVP;						\n"
		+	"void main() {							\n"
		+	" gl_Position = uMVP * aPosition;		\n"
		+	"}										\n";

	private final String _lineAboveFragmentShaderCode = 
			"void main() {							\n"
		+	" gl_FragColor = vec4(1.0,1.0,1.0,1);	\n"
		+	"}										\n";

	private final String _lineBelowVertexShaderCode = 
			"attribute vec4 aPosition;				\n"
		+	"uniform mat4 uMVP;						\n"
		+	"void main() {							\n"
		+	" gl_Position = uMVP * aPosition;		\n"
		+	"}										\n";

	private final String _lineBelowFragmentShaderCode = 
			"void main() {							\n"
		+	" gl_FragColor = vec4(0.0,0.0,1.0,1);	\n"
		+	"}										\n";

}
