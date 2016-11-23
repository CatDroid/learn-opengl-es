package com.apress.android.tankfence1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

public class GLES20Renderer implements Renderer {
	private int _tankProgram;
	private int _tankAPositionLocation;
	private int _tankUMVPLocation;
	private FloatBuffer _tankVFB;
	private ShortBuffer _tankISB;
	private int[] _tankBuffers = new int[2];

	private float[] _RMatrix			= new float[16];
	private float[] _ViewMatrix			= new float[16];
	private float[] _ProjectionMatrix	= new float[16];
	private float[] _MVPMatrix			= new float[16];
	private static volatile float _zAngle;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		inittank();

		float ratio	= (float) width / height;
		float zNear = 0.1f;
		float zFar = 1000;
		float fov = 0.95f; // 0.2 to 1.0
		float size = (float) (zNear * Math.tan(fov / 2));
		Matrix.setLookAtM(_ViewMatrix, 0, 0, 0, 50, 0, 0, 0, 0, 1, 0);
		Matrix.frustumM(_ProjectionMatrix, 0, -size, size, -size / ratio, size / ratio, zNear, zFar);
		Matrix.setIdentityM(_RMatrix, 0);

		_tankProgram = loadProgram(_tankVertexShaderCode, _tankFragmentShaderCode);

		_tankAPositionLocation = GLES20.glGetAttribLocation(_tankProgram, "aPosition");
		_tankUMVPLocation = GLES20.glGetUniformLocation(_tankProgram, "uMVP");
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		Matrix.setIdentityM(_RMatrix, 0);
		Matrix.rotateM(_RMatrix, 0, _zAngle, 0, 0, 1);
		Matrix.multiplyMM(_MVPMatrix, 0, _ViewMatrix, 0, _RMatrix, 0);
		Matrix.multiplyMM(_MVPMatrix, 0, _ProjectionMatrix, 0, _MVPMatrix, 0);

		GLES20.glUseProgram(_tankProgram);
		GLES20.glUniformMatrix4fv(_tankUMVPLocation, 1, false, _MVPMatrix, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _tankBuffers[0]);
		GLES20.glVertexAttribPointer(_tankAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, 0);
		GLES20.glEnableVertexAttribArray(_tankAPositionLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _tankBuffers[1]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 84, GLES20.GL_UNSIGNED_SHORT, 0);
		System.gc();
	}

	public static void setZAngle(float angle) {
		GLES20Renderer._zAngle = angle;
	}

	public static float getZAngle() {
		return GLES20Renderer._zAngle;
	}

	private void inittank() {
		float[] tankVFA = {
				-1.562685f,-2.427994f,0.000000f,
				-1.562685f,1.139500f,0.000000f,
				1.562685f,1.139500f,0.000000f,
				1.562685f,-2.427994f,0.000000f,
				-1.562685f,-2.427994f,2.000000f,
				-1.562685f,1.139500f,2.000000f,
				1.562685f,1.139500f,2.000000f,
				1.562685f,-2.427994f,2.000000f,
				-0.781342f,1.139500f,0.500000f,
				0.781342f,1.139500f,0.500000f,
				-0.781342f,1.139500f,1.500000f,
				0.781342f,1.139500f,1.500000f,
				-0.781342f,3.437026f,0.500000f,
				0.781342f,3.437026f,0.500000f,
				-0.781342f,3.437026f,1.500000f,
				0.781342f,3.437026f,1.500000f,
		};

		short[] tankISA = {
				4,5,1,
				2,1,8,
				6,7,3,
				4,0,7,
				0,1,2,
				7,6,5,
				9,8,12,
				5,6,11,
				6,2,9,
				10,8,1,
				14,15,13,
				11,9,13,
				14,12,8,
				10,11,15,
				0,4,1,
				9,2,8,
				2,6,3,
				7,0,3,
				3,0,2,
				4,7,5,
				13,9,12,
				10,5,11,
				11,6,9,
				5,10,1,
				12,14,13,
				15,11,13,
				10,14,8,
				14,10,15,
		};

		ByteBuffer tankVBB = ByteBuffer.allocateDirect(tankVFA.length * 4);
		tankVBB.order(ByteOrder.nativeOrder());
		_tankVFB = tankVBB.asFloatBuffer();
		_tankVFB.put(tankVFA);
		_tankVFB.position(0);

		ByteBuffer tankIBB = ByteBuffer.allocateDirect(tankISA.length * 2);
		tankIBB.order(ByteOrder.nativeOrder());
		_tankISB = tankIBB.asShortBuffer();
		_tankISB.put(tankISA);
		_tankISB.position(0);

		GLES20.glGenBuffers(2, _tankBuffers, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _tankBuffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, tankVFA.length * 4, _tankVFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _tankBuffers[1]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, tankISA.length * 2, _tankISB, GLES20.GL_STATIC_DRAW);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private int loadProgram(String vertexShaderCode, String fragmentShaderCode) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
        return program;
    }

	private final String _tankVertexShaderCode = 
			"attribute vec3 aPosition;											\n"
		+	"uniform mat4 uMVP;													\n"
		+	"void main() {														\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" gl_Position = uMVP * vertex;										\n"
		+	"}																	\n";

	private final String _tankFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH		\n"
		+	"precision highp float;					\n"
		+	"#else									\n"
		+	"precision mediump float;				\n"
		+	"#endif									\n"
		+	"void main() {							\n"
		+	" gl_FragColor = vec4(1.0,1.0,0.0,1);	\n"
		+	"}										\n";

}
