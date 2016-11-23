package com.apress.android.glcullface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

public class GLES20Renderer implements Renderer {
	private int _cubeProgram;
	private int _cubeAPositionLocation;
	private int _cubeAColorLocation;
	private int _cubeUMVPLocation;
	private FloatBuffer _VFBcube;
	private FloatBuffer _CFBcube;

	private float[] _ViewMatrix					= new float[16];
	private float[] _ProjectionMatrix			= new float[16];
	private float[] _MVPMatrix					= new float[16];

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glFrontFace(GLES20.GL_CCW);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		initCube();

		float ratio	= (float) width / height;
		float zNear = 0.1f;
		float zFar = 1000;
		float fov = 0.75f; // 0.2 to 1.0
		float size = (float) (zNear * Math.tan(fov / 2));
		Matrix.setLookAtM(_ViewMatrix, 0, -13, 5, 10, 0, 0, 0, 0, 1, 0);
		Matrix.frustumM(_ProjectionMatrix, 0, -size, size, -size / ratio, size / ratio, zNear, zFar);
		Matrix.multiplyMM(_MVPMatrix, 0, _ProjectionMatrix, 0, _ViewMatrix, 0);

		int cubeVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _cubeVertexShaderCode);
		int cubeFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _cubeFragmentShaderCode);
		_cubeProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_cubeProgram, cubeVertexShader);
		GLES20.glAttachShader(_cubeProgram, cubeFragmentShader);
		GLES20.glLinkProgram(_cubeProgram);

		_cubeAPositionLocation = GLES20.glGetAttribLocation(_cubeProgram, "aPosition");
		_cubeAColorLocation = GLES20.glGetAttribLocation(_cubeProgram, "aColor");
		_cubeUMVPLocation = GLES20.glGetUniformLocation(_cubeProgram, "uMVP");
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glUseProgram(_cubeProgram);
		GLES20.glUniformMatrix4fv(_cubeUMVPLocation, 1, false, _MVPMatrix, 0);
	    GLES20.glVertexAttribPointer(_cubeAPositionLocation, 3, GLES20.GL_FLOAT, false, 0, _VFBcube);
	    GLES20.glEnableVertexAttribArray(_cubeAPositionLocation);
	    GLES20.glVertexAttribPointer(_cubeAColorLocation, 4, GLES20.GL_FLOAT, false, 0, _CFBcube);
	    GLES20.glEnableVertexAttribArray(_cubeAColorLocation);
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 18);
	}

	private void initCube() {
		float[] cubeVFA = {
				0,0,-4,
				0,2,-4,
				2,2,-4, // back half
				2,2,-4,
				2,0,-4,
				0,0,-4, // back half
				2,2,-4,
				0,2,-4,
				0,2,-2, // top half
				0,2,-2,
				2,2,-2,
				2,2,-4, // top half
				2,2,-2,
				0,2,-2,
				0,0,-2, // front half
				0,0,-2,
				2,0,-2,
				2,2,-2, // front half
		};

		float[] cubeCFA = {
				0.3f,0.3f,0.3f,1,
				0.3f,0.3f,0.3f,1,
				0.3f,0.3f,0.3f,1,
				0.3f,0.3f,0.3f,1,
				0.3f,0.3f,0.3f,1,
				0.3f,0.3f,0.3f,1, // back half
				0.5f,0.5f,0.5f,1,
				0.5f,0.5f,0.5f,1,
				0.5f,0.5f,0.5f,1,
				0.5f,0.5f,0.5f,1,
				0.5f,0.5f,0.5f,1,
				0.5f,0.5f,0.5f,1, // top half
				0.6f,0.6f,0.6f,1,
				0.6f,0.6f,0.6f,1,
				0.6f,0.6f,0.6f,1,
				0.6f,0.6f,0.6f,1,
				0.6f,0.6f,0.6f,1,
				0.6f,0.6f,0.6f,1, // front half
		};

		ByteBuffer cubeVBB = ByteBuffer.allocateDirect(cubeVFA.length * 4);
		cubeVBB.order(ByteOrder.nativeOrder());
		_VFBcube = cubeVBB.asFloatBuffer();
		_VFBcube.put(cubeVFA);
		_VFBcube.position(0);

		ByteBuffer cubeCBB = ByteBuffer.allocateDirect(cubeCFA.length * 4);
		cubeCBB.order(ByteOrder.nativeOrder());
		_CFBcube = cubeCBB.asFloatBuffer();
		_CFBcube.put(cubeCFA);
		_CFBcube.position(0);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private final String _cubeVertexShaderCode = 
			"attribute vec3 aPosition;											\n"
		+	"attribute vec4 aColor;												\n"
		+	"varying vec4 vColor;												\n"
		+	"uniform mat4 uMVP;													\n"
		+	"void main() {														\n"
		+	" vColor = aColor;													\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" gl_Position = uMVP * vertex;										\n"
		+	"}																	\n";

	private final String _cubeFragmentShaderCode = 
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
