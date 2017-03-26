package com.apress.android.glcube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

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

	private final String TAG ="GLES20Renderer";
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
		
		
		// Matrix.invertM 求逆矩阵Inverse Matric 如果矩阵不可逆 返回false  
		float[] input = {
			1,1,1,1,
			0,0,0,0,
			0,0,0,0,
			0,0,0,0
		};
		float[] output = new float[16];
		Log.d(TAG,"invertM " + Matrix.invertM(input, 0, output, 0) );
		Log.d(TAG,"input = " + Arrays.toString(input));
		Log.d(TAG,"output = " + Arrays.toString(output));
		
		// transposeM
		Matrix.transposeM(output, 0, input, 0);
		Log.d(TAG,"input = " + Arrays.toString(input));
		Log.d(TAG,"output = " + Arrays.toString(output));
	
		
		// 
		float[] source = {
				1,1,1,1,
				0,0,0,0,
				0,0,0,0,
				0,0,0,0
		};
		float[] roateM = new float[16]; // 旋转矩阵
		Matrix.setIdentityM(roateM, 0);
		Matrix.rotateM(roateM,0,90,1,0,0); // 沿轴(1,0,0)旋转90度的旋转矩阵
		Log.d(TAG,"roateM = " + Arrays.toString(roateM));
		
		float[] resultRotateM = new float[16]; 
		Matrix.rotateM(resultRotateM,0, source, 0, 90,1,0,0); // 把矩阵  沿轴(1,0,0)旋转90度 得到结果矩阵
		Log.d(TAG,"source = " + Arrays.toString(source));
		Log.d(TAG,"resultRotateM = " + Arrays.toString(resultRotateM));
		
		float[] resultRotateM2 = new float[16]; 
		Matrix.multiplyMM(resultRotateM2, 0, roateM, 0, source, 0);
		Log.d(TAG,"source = " + Arrays.toString(source));
		Log.d(TAG,"resultRotateM2 = " + Arrays.toString(resultRotateM2));
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
		//Matrix.setIdentityM(_ViewMatrix, 0);
		//Matrix.frustumM(_ProjectionMatrix, 0, -size , size , -size * ratio , size* ratio , zNear, zFar);
		
		// 下面两个是一样的
		Matrix.frustumM(_ProjectionMatrix, 0, -size * ratio, size * ratio, -size , size , zNear, zFar);
		//Matrix.perspectiveM(_ProjectionMatrix, 0, (float)(fov*180/Math.PI) , ratio, zNear, zFar);
		
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
				0,2,-2,
				0,2,-4,
				2,2,-4, // top half
				2,2,-4,
				2,2,-2,
				0,2,-2, // top half
				0,0,-2,
				0,2,-2,
				2,2,-2, // front half
				2,2,-2,
				2,0,-2,
				0,0,-2, // front half
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
