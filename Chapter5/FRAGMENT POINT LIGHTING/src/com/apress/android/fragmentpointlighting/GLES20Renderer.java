package com.apress.android.fragmentpointlighting;

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
	private int _pointProgram;

	private int _tankAPositionLocation;
	private int _tankANormalLocation;
	private int _pointAPositionLocation;
	private int _tankUNormalLocation;
	private int _tankUMVLocation;
	private int _tankUMVLightLocation;
	private int _tankUMVPLocation;
	private int _pointUMVPLocation;

	private FloatBuffer _tankVFB;
	private FloatBuffer _tankVNFB;
	private ShortBuffer _tankISB;
	private FloatBuffer _pointVFB;
	private ShortBuffer _pointISB;

	private float[] _ViewMatrix			= new float[16];
	private float[] _ProjectionMatrix	= new float[16];
	private float[] _tankRMatrix		= new float[16];
	private float[] _tankNormalMatrix	= new float[9];
	private float[] _tankMVMatrix		= new float[16];
	private float[] _tankMVPMatrix		= new float[16];
	private float[] _pointRMatrix		= new float[16];
	private float[] _pointMVMatrix		= new float[16];
	private float[] _pointMVPMatrix		= new float[16];
	private boolean _rotatePointOnly	= true;
	private static volatile float _zAngle;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		inittank();
		initpoint();

		float ratio	= (float) width / height;
		float zNear = 0.1f;
		float zFar = 1000;
		float fov = 0.95f; // 0.2 to 1.0
		float size = (float) (zNear * Math.tan(fov / 2));
		Matrix.setLookAtM(_ViewMatrix, 0, 0, 0, 45, 0, 0, 0, 0, 1, 0);
		if(!_rotatePointOnly) {
			System.arraycopy(_ViewMatrix, 0, _pointMVMatrix, 0, 16);
		}
		Matrix.frustumM(_ProjectionMatrix, 0, -size, size, -size / ratio, size / ratio, zNear, zFar);
		if(!_rotatePointOnly) {
			Matrix.multiplyMM(_pointMVPMatrix, 0, _ProjectionMatrix, 0, _ViewMatrix, 0);
		}
		Matrix.setIdentityM(_tankRMatrix, 0);
		Matrix.setIdentityM(_pointRMatrix, 0);

		_tankProgram = loadProgram(_tankVertexShaderCode, _tankFragmentShaderCode);
		_pointProgram = loadProgram(_pointVertexShaderCode, _pointFragmentShaderCode);

		_tankAPositionLocation = GLES20.glGetAttribLocation(_tankProgram, "aPosition");
		_tankANormalLocation = GLES20.glGetAttribLocation(_tankProgram, "aNormal");
		_tankUNormalLocation = GLES20.glGetUniformLocation(_tankProgram, "uNormal");
		_tankUMVLocation = GLES20.glGetUniformLocation(_tankProgram, "uMV");
		_tankUMVPLocation = GLES20.glGetUniformLocation(_tankProgram, "uMVP");
		_tankUMVLightLocation = GLES20.glGetUniformLocation(_tankProgram, "uMVLight");
		_pointAPositionLocation = GLES20.glGetAttribLocation(_pointProgram, "aPosition");
		_pointUMVPLocation = GLES20.glGetUniformLocation(_pointProgram, "uMVP");
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		if(!_rotatePointOnly) {
			Matrix.setIdentityM(_tankRMatrix, 0);
			Matrix.rotateM(_tankRMatrix, 0, _zAngle, 0, 0, 1);
		}
		Matrix.multiplyMM(_tankMVPMatrix, 0, _ViewMatrix, 0, _tankRMatrix, 0);
		if(_rotatePointOnly) {
			Matrix.rotateM(_pointRMatrix, 0, _zAngle * 0.5f, 0, 0, 1);
			Matrix.multiplyMM(_pointMVMatrix, 0, _ViewMatrix, 0, _pointRMatrix, 0);
			Matrix.multiplyMM(_pointMVPMatrix, 0, _ProjectionMatrix, 0, _pointMVMatrix, 0);
		}

		_tankNormalMatrix[0] = _tankMVPMatrix[0];
		_tankNormalMatrix[1] = _tankMVPMatrix[1];
		_tankNormalMatrix[2] = _tankMVPMatrix[2]; // from 1st column, ending at [3]

		_tankNormalMatrix[3] = _tankMVPMatrix[4];
		_tankNormalMatrix[4] = _tankMVPMatrix[5];
		_tankNormalMatrix[5] = _tankMVPMatrix[6]; // from 2nd column, ending at [7]

		_tankNormalMatrix[6] = _tankMVPMatrix[8];
		_tankNormalMatrix[7] = _tankMVPMatrix[9];
		_tankNormalMatrix[8] = _tankMVPMatrix[10]; // from 3rd column, ending at [11]

		System.arraycopy(_tankMVPMatrix, 0, _tankMVMatrix, 0, 16);
		Matrix.multiplyMM(_tankMVPMatrix, 0, _ProjectionMatrix, 0, _tankMVPMatrix, 0);

		GLES20.glUseProgram(_tankProgram);
		GLES20.glUniformMatrix4fv(_tankUMVLocation, 1, false, _tankMVMatrix, 0);
		GLES20.glUniformMatrix4fv(_tankUMVPLocation, 1, false, _tankMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(_tankUMVLightLocation, 1, false, _pointMVMatrix, 0);
		GLES20.glUniformMatrix3fv(_tankUNormalLocation, 1, false, _tankNormalMatrix, 0);
		GLES20.glVertexAttribPointer(_tankAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, _tankVFB);
		GLES20.glEnableVertexAttribArray(_tankAPositionLocation);
		GLES20.glVertexAttribPointer(_tankANormalLocation, 3, GLES20.GL_FLOAT, false, 12, _tankVNFB);
		GLES20.glEnableVertexAttribArray(_tankANormalLocation);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 84, GLES20.GL_UNSIGNED_SHORT, _tankISB);

		GLES20.glUseProgram(_pointProgram);
		GLES20.glUniformMatrix4fv(_pointUMVPLocation, 1, false, _pointMVPMatrix, 0);
		GLES20.glVertexAttribPointer(_pointAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, _pointVFB);
		GLES20.glEnableVertexAttribArray(_pointAPositionLocation);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 60, GLES20.GL_UNSIGNED_SHORT, _pointISB);
		System.gc();
	}

	public static void setZAngle(float angle) {
		GLES20Renderer._zAngle = angle;
	}

	public static float getZAngle() {
		return GLES20Renderer._zAngle;
	}

	private void inittank() {
		float[] tankVNFA = {
				0.000000f,-1.000000f,0.000000f,
				-1.000000f,0.000000f,0.000000f,
				0.000000f,1.000000f,0.000000f,
				1.000000f,0.000000f,0.000000f,
				-1.000000f,0.000000f,0.000000f,
				-0.707106f,0.707106f,0.000000f,
				0.707106f,0.707106f,0.000000f,
				1.000000f,0.000000f,0.000000f,
				0.000000f,1.000000f,0.000000f,
				0.000000f,0.000000f,-1.000000f,
				0.000000f,1.000000f,0.000000f,
				0.000000f,1.000000f,0.000000f,
				0.000000f,0.000000f,-1.000000f,
				0.000000f,1.000000f,0.000000f,
				0.000000f,1.000000f,0.000000f,
				0.000000f,1.000000f,0.000000f,
		};

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

		ByteBuffer tankVNBB = ByteBuffer.allocateDirect(tankVNFA.length * 4);
		tankVNBB.order(ByteOrder.nativeOrder());
		_tankVNFB = tankVNBB.asFloatBuffer();
		_tankVNFB.put(tankVNFA);
		_tankVNFB.position(0);
	}

	private void initpoint() {
		float[] pointVFA = {
				10.000000f,10.000000f,-1.000000f,
				10.723600f,9.474280f,-0.447215f,
				9.723615f,9.149360f,-0.447215f,
				9.105575f,10.000000f,-0.447215f,
				9.723615f,10.850640f,-0.447215f,
				10.723600f,10.525720f,-0.447215f,
				10.276385f,9.149360f,0.447215f,
				9.276400f,9.474280f,0.447215f,
				9.276400f,10.525720f,0.447215f,
				10.276385f,10.850640f,0.447215f,
				10.894425f,10.000000f,0.447215f,
				10.000000f,10.000000f,1.000000f,
		};

		short[] pointISA = {
				0,1,2,
				1,0,5,
				0,2,3,
				0,3,4,
				0,4,5,
				1,5,10,
				2,1,6,
				3,2,7,
				4,3,8,
				5,4,9,
				1,10,6,
				2,6,7,
				3,7,8,
				4,8,9,
				5,9,10,
				6,10,11,
				7,6,11,
				8,7,11,
				9,8,11,
				10,9,11,
		};

		ByteBuffer pointVBB = ByteBuffer.allocateDirect(pointVFA.length * 4);
		pointVBB.order(ByteOrder.nativeOrder());
		_pointVFB = pointVBB.asFloatBuffer();
		_pointVFB.put(pointVFA);
		_pointVFB.position(0);

		ByteBuffer pointIBB = ByteBuffer.allocateDirect(pointISA.length * 2);
		pointIBB.order(ByteOrder.nativeOrder());
		_pointISB = pointIBB.asShortBuffer();
		_pointISB.put(pointISA);
		_pointISB.position(0);
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
			"attribute vec3 aPosition;													\n"
		+	"attribute vec3 aNormal;													\n"
		+	"varying vec4 vertex;														\n"
		+	"varying vec3 normal;														\n"
		+	"uniform mat3 uNormal;														\n"
		+	"uniform mat4 uMV;															\n"
		+	"uniform mat4 uMVP;															\n"
		+	"uniform mat4 uMVLight;														\n"
		+	"void main() {																\n"
		+	" vertex = vec4(aPosition[0], aPosition[1], aPosition[2], 1.0);				\n"
		+	" normal = normalize(vec3(uNormal * aNormal));								\n"
		+	"																			\n"
		+	" gl_Position = vec4(uMVP * vertex); // ensures that we provide a vec4		\n"
		+	"}																			\n";

	private final String _tankFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH											\n"
		+	"precision highp float;														\n"
		+	"#else																		\n"
		+	"precision mediump float;													\n"
		+	"#endif																		\n"
		+	"varying float diffuseIntensity;											\n"
		+	"varying vec4 vertex;														\n"
		+	"varying vec3 normal;														\n"
		+	"uniform mat3 uNormal;														\n"
		+	"uniform mat4 uMV;															\n"
		+	"uniform mat4 uMVP;															\n"
		+	"uniform mat4 uMVLight;														\n"
		+	"const vec4 lightPositionWorld = vec4(10.0, 10.0, 0.0, 1.0);				\n"
		+	"void main() {																\n"
		+	" float diffuseIntensity;													\n"
		+	" vec4 vertexEye = vec4(uMV * vertex);										\n"
		+	" vec4 lightPositionEye = vec4(uMVLight * lightPositionWorld);				\n"
		+	" vec3 ds = normalize(vec3(lightPositionEye - vertexEye));					\n"
		+	"																			\n"
		+	" diffuseIntensity = max(dot(ds, normal), 0.210);							\n"
		+	" diffuseIntensity = 0.570 * 0.210 * diffuseIntensity;						\n"
		+	" vec3 diffuse = vec3(diffuseIntensity);									\n"
		+	" // gl_FragColor = vec4(0.1, 0.1, 0.25, 1.0) + vec4(diffuse, 1.0);			\n"
		+	" gl_FragColor = vec4(diffuse, 1.0);										\n"
		+	"}																			\n";

	private final String _pointVertexShaderCode = 
			"attribute vec3 aPosition;												\n"
		+	"uniform mat4 uMVP;														\n"
		+	"void main() {															\n"
		+	" vec4 vertex = vec4(aPosition[0], aPosition[1], aPosition[2], 1.0);	\n"
		+	" gl_Position = vec4(uMVP * vertex);									\n"
		+	"}																		\n";

	private final String _pointFragmentShaderCode = 
			"precision lowp float;			\n"
		+	"void main() {					\n"
		+	" gl_FragColor = vec4(1.0);		\n"
		+	"}								\n";

}
