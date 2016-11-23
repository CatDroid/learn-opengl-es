package com.apress.android.tankfence3;

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
	private int _planeProgram;
	private int _enemyProgram;

	private int _tankAPositionLocation;
	private int _planeAPositionLocation;
	private int _enemyAPositionLocation;
	private int _tankAColorLocation;
	private int _planeAColorLocation;
	private int _enemyAColorLocation;
	private int _tankUMVPLocation;
	private int _planeUMVPLocation;
	private int _enemyUMVPLocation;

	private FloatBuffer _tankVFB;
	private FloatBuffer _tankCFB;
	private ShortBuffer _tankISB;
	private FloatBuffer _planeVFB;
	private FloatBuffer _planeCFB;
	private ShortBuffer _planeISB;
	private FloatBuffer _enemyVFB;
	private FloatBuffer _enemyCFB;
	private ShortBuffer _enemyISB;

	private int[] _tankBuffers	= new int[3];
	private int[] _planeBuffers	= new int[3];
	private int[] _enemyBuffers	= new int[3];

	private float[] _ViewMatrix			= new float[16];
	private float[] _ProjectionMatrix	= new float[16];
	private float[] _MVPMatrix			= new float[16];
	private float[] _tankRMatrix		= new float[16];
	private float[] _tankMVPMatrix		= new float[16];
	private static volatile float _zAngle;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		inittank();
		initplane();
		initenemy();

		float ratio	= (float) width / height;
		float zNear = 0.1f;
		float zFar = 1000;
		float fov = 0.95f; // 0.2 to 1.0
		float size = (float) (zNear * Math.tan(fov / 2));
		Matrix.setLookAtM(_ViewMatrix, 0, 0, 0, 75, 0, 0, 0, 0, 1, 0);
		// Matrix.setLookAtM(_ViewMatrix, 0, 0, -20, 50, 0, 0, 0, 0, 1, 0);
		Matrix.frustumM(_ProjectionMatrix, 0, -size, size, -size / ratio, size / ratio, zNear, zFar);
		Matrix.multiplyMM(_MVPMatrix, 0, _ProjectionMatrix, 0, _ViewMatrix, 0);
		Matrix.setIdentityM(_tankRMatrix, 0);

		_tankProgram = loadProgram(_tankVertexShaderCode, _tankFragmentShaderCode);
		_planeProgram = loadProgram(_planeVertexShaderCode, _planeFragmentShaderCode);
		_enemyProgram = loadProgram(_enemyVertexShaderCode, _enemyFragmentShaderCode);

		_tankAPositionLocation = GLES20.glGetAttribLocation(_tankProgram, "aPosition");
		_tankAColorLocation = GLES20.glGetAttribLocation(_tankProgram, "aColor");
		_tankUMVPLocation = GLES20.glGetUniformLocation(_tankProgram, "uMVP");
		_planeAPositionLocation = GLES20.glGetAttribLocation(_planeProgram, "aPosition");
		_planeAColorLocation = GLES20.glGetAttribLocation(_planeProgram, "aColor");
		_planeUMVPLocation = GLES20.glGetUniformLocation(_planeProgram, "uMVP");
		_enemyAPositionLocation = GLES20.glGetAttribLocation(_enemyProgram, "aPosition");
		_enemyAColorLocation = GLES20.glGetAttribLocation(_enemyProgram, "aColor");
		_enemyUMVPLocation = GLES20.glGetUniformLocation(_enemyProgram, "uMVP");
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		Matrix.setIdentityM(_tankRMatrix, 0);
		Matrix.rotateM(_tankRMatrix, 0, _zAngle, 0, 0, 1);
		Matrix.multiplyMM(_tankMVPMatrix, 0, _ViewMatrix, 0, _tankRMatrix, 0);
		Matrix.multiplyMM(_tankMVPMatrix, 0, _ProjectionMatrix, 0, _tankMVPMatrix, 0);

		GLES20.glUseProgram(_tankProgram);
		GLES20.glUniformMatrix4fv(_tankUMVPLocation, 1, false, _tankMVPMatrix, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _tankBuffers[0]);
		GLES20.glVertexAttribPointer(_tankAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, 0);
		GLES20.glEnableVertexAttribArray(_tankAPositionLocation);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _tankBuffers[1]);
		GLES20.glVertexAttribPointer(_tankAColorLocation, 4, GLES20.GL_FLOAT, false, 16, 0);
		GLES20.glEnableVertexAttribArray(_tankAColorLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _tankBuffers[2]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 84, GLES20.GL_UNSIGNED_SHORT, 0);

		GLES20.glUseProgram(_planeProgram);
		GLES20.glUniformMatrix4fv(_planeUMVPLocation, 1, false, _MVPMatrix, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _planeBuffers[0]);
		GLES20.glVertexAttribPointer(_planeAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, 0);
		GLES20.glEnableVertexAttribArray(_planeAPositionLocation);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _planeBuffers[1]);
		GLES20.glVertexAttribPointer(_planeAColorLocation, 4, GLES20.GL_FLOAT, false, 16, 0);
		GLES20.glEnableVertexAttribArray(_planeAColorLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _planeBuffers[2]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0);

		GLES20.glUseProgram(_enemyProgram);
		GLES20.glUniformMatrix4fv(_enemyUMVPLocation, 1, false, _MVPMatrix, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _enemyBuffers[0]);
		GLES20.glVertexAttribPointer(_enemyAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, 0);
		GLES20.glEnableVertexAttribArray(_enemyAPositionLocation);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _enemyBuffers[1]);
		GLES20.glVertexAttribPointer(_enemyAColorLocation, 4, GLES20.GL_FLOAT, false, 16, 0);
		GLES20.glEnableVertexAttribArray(_enemyAColorLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _enemyBuffers[2]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 24, GLES20.GL_UNSIGNED_SHORT, 0);
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

		float[] tankCFA = {
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
				0.000000f,0.000000f,1.000000f,1,
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

		ByteBuffer tankCBB = ByteBuffer.allocateDirect(tankCFA.length * 4);
		tankCBB.order(ByteOrder.nativeOrder());
		_tankCFB = tankCBB.asFloatBuffer();
		_tankCFB.put(tankCFA);
		_tankCFB.position(0);

		GLES20.glGenBuffers(3, _tankBuffers, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _tankBuffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, tankVFA.length * 4, _tankVFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _tankBuffers[1]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, tankCFA.length * 4, _tankCFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _tankBuffers[2]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, tankISA.length * 2, _tankISB, GLES20.GL_STATIC_DRAW);
	}

	private void initplane() {
		float[] planeVFA = {
				10.000000f,-10.000000f,0.000000f,
				-10.000000f,-10.000000f,0.000000f,
				10.000000f,10.000000f,0.000000f,
				-10.000000f,10.000000f,0.000000f,
		};

		short[] planeISA = {
				2,3,1,
				0,2,1,
		};

		float[] planeCFA = {
				1.000000f,1.000000f,1.000000f,1,
				1.000000f,1.000000f,1.000000f,1,
				1.000000f,1.000000f,1.000000f,1,
				1.000000f,1.000000f,1.000000f,1,
		};

		ByteBuffer planeVBB = ByteBuffer.allocateDirect(planeVFA.length * 4);
		planeVBB.order(ByteOrder.nativeOrder());
		_planeVFB = planeVBB.asFloatBuffer();
		_planeVFB.put(planeVFA);
		_planeVFB.position(0);

		ByteBuffer planeIBB = ByteBuffer.allocateDirect(planeISA.length * 2);
		planeIBB.order(ByteOrder.nativeOrder());
		_planeISB = planeIBB.asShortBuffer();
		_planeISB.put(planeISA);
		_planeISB.position(0);

		ByteBuffer planeCBB = ByteBuffer.allocateDirect(planeCFA.length * 4);
		planeCBB.order(ByteOrder.nativeOrder());
		_planeCFB = planeCBB.asFloatBuffer();
		_planeCFB.put(planeCFA);
		_planeCFB.position(0);

		GLES20.glGenBuffers(3, _planeBuffers, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _planeBuffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, planeVFA.length * 4, _planeVFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _planeBuffers[1]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, planeCFA.length * 4, _planeCFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _planeBuffers[2]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, planeISA.length * 2, _planeISB, GLES20.GL_STATIC_DRAW);
	}

	private void initenemy() {
		float[] enemyVFA = {
				10.816487f,8.585787f,-0.003767f,
				10.816488f,11.414213f,-0.003767f,
				8.367024f,10.000001f,0.007534f,
				10.817415f,8.585787f,0.197270f,
				10.817416f,11.414213f,0.197270f,
				8.367951f,10.000001f,0.208572f,
		};

		short[] enemyISA = {
				1,0,2,
				5,3,4,
				0,1,4,
				1,2,5,
				2,0,3,
				3,0,4,
				4,1,5,
				5,2,3,
		};

		float[] enemyCFA = {
				1.000000f,0.000000f,0.000000f,1,
				1.000000f,0.000000f,0.000000f,1,
				1.000000f,0.000000f,0.000000f,1,
				1.000000f,0.000000f,0.000000f,1,
				1.000000f,0.000000f,0.000000f,1,
				1.000000f,0.000000f,0.000000f,1,
		};

		ByteBuffer enemyVBB = ByteBuffer.allocateDirect(enemyVFA.length * 4);
		enemyVBB.order(ByteOrder.nativeOrder());
		_enemyVFB = enemyVBB.asFloatBuffer();
		_enemyVFB.put(enemyVFA);
		_enemyVFB.position(0);

		ByteBuffer enemyIBB = ByteBuffer.allocateDirect(enemyISA.length * 2);
		enemyIBB.order(ByteOrder.nativeOrder());
		_enemyISB = enemyIBB.asShortBuffer();
		_enemyISB.put(enemyISA);
		_enemyISB.position(0);

		ByteBuffer enemyCBB = ByteBuffer.allocateDirect(enemyCFA.length * 4);
		enemyCBB.order(ByteOrder.nativeOrder());
		_enemyCFB = enemyCBB.asFloatBuffer();
		_enemyCFB.put(enemyCFA);
		_enemyCFB.position(0);

		GLES20.glGenBuffers(3, _enemyBuffers, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _enemyBuffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, enemyVFA.length * 4, _enemyVFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _enemyBuffers[1]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, enemyCFA.length * 4, _enemyCFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _enemyBuffers[2]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, enemyISA.length * 2, _enemyISB, GLES20.GL_STATIC_DRAW);
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
		+	"attribute vec4 aColor;												\n"
		+	"varying vec4 vColor;												\n"
		+	"uniform mat4 uMVP;													\n"
		+	"void main() {														\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" vColor = aColor;													\n"
		+	" gl_Position = uMVP * vertex;										\n"
		+	"}																	\n";

	private final String _tankFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH		\n"
		+	"precision highp float;					\n"
		+	"#else									\n"
		+	"precision mediump float;				\n"
		+	"#endif									\n"
		+	"varying vec4 vColor;					\n"
		+	"void main() {							\n"
		+	" gl_FragColor = vColor;				\n"
		+	"}										\n";

	private final String _planeVertexShaderCode = 
			"attribute vec3 aPosition;											\n"
		+	"attribute vec4 aColor;												\n"
		+	"varying vec4 vColor;												\n"
		+	"uniform mat4 uMVP;													\n"
		+	"void main() {														\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" vColor = aColor;													\n"
		+	" gl_Position = uMVP * vertex;										\n"
		+	"}																	\n";

	private final String _planeFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH		\n"
		+	"precision highp float;					\n"
		+	"#else									\n"
		+	"precision mediump float;				\n"
		+	"#endif									\n"
		+	"varying vec4 vColor;					\n"
		+	"void main() {							\n"
		+	" gl_FragColor = vColor;				\n"
		+	"}										\n";

	private final String _enemyVertexShaderCode = 
			"attribute vec3 aPosition;											\n"
		+	"attribute vec4 aColor;												\n"
		+	"varying vec4 vColor;												\n"
		+	"uniform mat4 uMVP;													\n"
		+	"void main() {														\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" vColor = aColor;													\n"
		+	" gl_Position = uMVP * vertex;										\n"
		+	"}																	\n";

	private final String _enemyFragmentShaderCode = 
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
