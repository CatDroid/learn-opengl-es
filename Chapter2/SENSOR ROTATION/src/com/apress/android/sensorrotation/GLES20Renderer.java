package com.apress.android.sensorrotation;

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
	private int _arrowProgram;
	private int _arrowAPositionLocation;
	private int _arrowUMVPLocation;
	private FloatBuffer _VFBarrow;
	private ShortBuffer _ISBarrow;
	private int[] _arrowBuffers = new int[2];
	private float[] _RMatrix = new float[16];
	private static volatile float _zAngle;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		initArrow();

		int arrowVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _arrowVertexShaderCode);
		int arrowFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _arrowFragmentShaderCode);
		_arrowProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_arrowProgram, arrowVertexShader);
		GLES20.glAttachShader(_arrowProgram, arrowFragmentShader);
		GLES20.glLinkProgram(_arrowProgram);

		_arrowAPositionLocation = GLES20.glGetAttribLocation(_arrowProgram, "aPosition");
		_arrowUMVPLocation = GLES20.glGetUniformLocation(_arrowProgram, "uMVP");
		Matrix.setIdentityM(_RMatrix, 0);
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		Matrix.setIdentityM(_RMatrix, 0);
		Matrix.rotateM(_RMatrix, 0, _zAngle, 0, 0, 1);

		GLES20.glUseProgram(_arrowProgram);
		GLES20.glUniformMatrix4fv(_arrowUMVPLocation, 1, false, _RMatrix, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _arrowBuffers[0]);
		GLES20.glVertexAttribPointer(_arrowAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, 0);
		GLES20.glEnableVertexAttribArray(_arrowAPositionLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _arrowBuffers[1]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_SHORT, 0);
		System.gc();
	}

	public static void setZAngle(float angle) {
		GLES20Renderer._zAngle = angle;
	}

	public static float getZAngle() {
		return GLES20Renderer._zAngle;
	}

	private void initArrow() {
		float[] arrowVFA = {
				0.000467f,0.205818f,-0.000364f,
				0.026045f,-0.113806f,-0.020301f,
				-0.026045f,-0.113806f,-0.020301f,
				-0.000467f,0.205818f,-0.000364f,
				0.000467f,0.205818f,0.000364f,
				0.026045f,-0.113806f,0.020301f,
				-0.026045f,-0.113806f,0.020301f,
				-0.000467f,0.205818f,0.000364f,
		};

		short[] arrowISA = {
				0,1,3,
				4,7,6,
				0,4,1,
				1,5,2,
				2,6,3,
				4,0,7,
				1,2,3,
				5,4,6,
				4,5,1,
				5,6,2,
				6,7,3,
				0,3,7,
		};

		ByteBuffer arrowVBB = ByteBuffer.allocateDirect(arrowVFA.length * 4);
		arrowVBB.order(ByteOrder.nativeOrder());
		_VFBarrow = arrowVBB.asFloatBuffer();
		_VFBarrow.put(arrowVFA);
		_VFBarrow.position(0);

		ByteBuffer arrowIBB = ByteBuffer.allocateDirect(arrowISA.length * 2);
		arrowIBB.order(ByteOrder.nativeOrder());
		_ISBarrow = arrowIBB.asShortBuffer();
		_ISBarrow.put(arrowISA);
		_ISBarrow.position(0);

		GLES20.glGenBuffers(2, _arrowBuffers, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _arrowBuffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, arrowVFA.length * 4, _VFBarrow, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _arrowBuffers[1]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, arrowISA.length * 2, _ISBarrow, GLES20.GL_STATIC_DRAW);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private final String _arrowVertexShaderCode = 
			"attribute vec3 aPosition;											\n"
		+	"uniform mat4 uMVP;													\n"
		+	"void main() {														\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" gl_Position = uMVP * vertex;										\n"
		+	"}																	\n";

	private final String _arrowFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH		\n"
		+	"precision highp float;					\n"
		+	"#else									\n"
		+	"precision mediump float;				\n"
		+	"#endif									\n"
		+	"void main() {							\n"
		+	" gl_FragColor = vec4(1.0,1.0,0.0,1);	\n"
		+	"}										\n";

}
