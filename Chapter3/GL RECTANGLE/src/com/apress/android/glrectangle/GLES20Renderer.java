package com.apress.android.glrectangle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class GLES20Renderer implements Renderer {
	private int _rectangleProgram;
	private int aPosHandle;
	private int aColorHandle;

	FloatBuffer vertexColorData;

	public static final String TAG = "GLES20Renderer";

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		initShapes();
		int _rectangleVertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
				_rectangleVertexShaderCode);
		int _rectangleFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
				_rectangleFragmentShaderCode);
		_rectangleProgram = GLES20.glCreateProgram();
		if (_rectangleProgram == 0) {
			Log.e(TAG, "glCreateProgram ERROR ");
		}
		GLES20.glAttachShader(_rectangleProgram, _rectangleVertexShader);
		GLES20.glAttachShader(_rectangleProgram, _rectangleFragmentShader);
		GLES20.glLinkProgram(_rectangleProgram);

		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(_rectangleProgram, GLES20.GL_LINK_STATUS,
				linkStatus, 0);
		if (linkStatus[0] == 0) {
			GLES20.glDeleteProgram(_rectangleProgram);
			Log.e(TAG, "linking of program failed");
		}

		aPosHandle = GLES20.glGetAttribLocation(_rectangleProgram, "a_Position");
		//if( aPosHandle == 0 ){
		//	Log.e(TAG , "aPosition error ");
		//}
		aColorHandle = GLES20.glGetAttribLocation(_rectangleProgram, "a_Color");
		//if( aColorHandle == 0 ){
		//	Log.e(TAG , "a_Color error ");
		//}
		Log.d(TAG, "aPosHandle = " + aPosHandle + " aColorHandle " + aColorHandle);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
	}

	public static boolean validateProgram(int programObjectId) {
		GLES20.glValidateProgram(programObjectId);
		final int[] validateStatus = new int[1];
		GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS,
				validateStatus, 0);
		Log.v(TAG, GLES20.glGetProgramInfoLog(programObjectId));
		return validateStatus[0] != 0;
	}

	public void onDrawFrame(GL10 gl) {
		gl.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		boolean validate = validateProgram(_rectangleProgram);
		Log.d(TAG, "validate = " + validate);
		GLES20.glUseProgram(_rectangleProgram);

		vertexColorData.position(0);
		//Log.d(TAG , " 1 " + vertexColorData.get());
		vertexColorData.position(0);
		GLES20.glVertexAttribPointer(aPosHandle, COLOR_COMPONENT_COUNT, GLES20.GL_FLOAT,
				false, STRIDE, vertexColorData);
		GLES20.glEnableVertexAttribArray(aPosHandle);

		vertexColorData.position(POSITION_COMPONENT_COUNT); // 跳过两个位置的 
		//Log.d(TAG , " 2 " + vertexColorData.get() + " STRIDE = " + STRIDE);
		vertexColorData.position(POSITION_COMPONENT_COUNT);
		GLES20.glVertexAttribPointer(aColorHandle, COLOR_COMPONENT_COUNT,
				GLES20.GL_FLOAT, false, STRIDE, vertexColorData); // 每两个颜色属性值 相差STRIDE个字节 
		GLES20.glEnableVertexAttribArray(aColorHandle);
	
		
		// GLES20.GL_TRIANGLE_FAN
		// GLES20.GL_TRIANGLE_FAN
		// GLES20.GL_TRIANGLE_FAN
		
		// GLES20.glDrawArrays mode first count
		//  GLES20.GL_TRIANGLES
		// GLES20.GL_TRIANGLE_FAN
		//  GLES20.GL_TRIANGLE_STRIP
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES , 0, 6);
		GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2); //
		// 顶点坐标COMPONENT(每个坐标有两个分量) 偏移 6个
		GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);
		GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);

	}

	final private int BYTES_PER_FLOAT = 4;
	final private int POSITION_COMPONENT_COUNT = 2; // 没有z坐标 w坐标为1
	final private int COLOR_COMPONENT_COUNT = 3;
	final private int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

	private void initShapes() {

		float[] tableVerticesWithTriangles = {
			// 两个三角形顶点坐标和三角形的颜色分量
			0f, 0f,    1f, 1f, 1f, 
			-0.5f, -0.5f,  0.7f, 0.7f, 0.7f, 
			0.5f,-0.5f,   0.7f, 0.7f, 0.7f, 
			0.5f, 0.5f,   0.7f, 0.7f, 0.7f, 
			-0.5f,0.5f,  0.7f, 0.7f, 0.7f, 
			-0.5f, -0.5f,   0.7f, 0.7f, 0.7f,

			// 两条直线坐标和直线的颜色分量
			-0.5f, 0f,   1f, 0f, 0f,
			0.5f, 0f,  0f, 0f, 1f, // 平滑着色

			// 两个顶点坐标和顶点的颜色分量
			0f, -0.25f,   0f, 0f, 1f, 
			0f, 0.25f,   1f, 0f, 0f 
		};
		vertexColorData = ByteBuffer
				.allocateDirect(
						tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		vertexColorData.put(tableVerticesWithTriangles);
	
	}

	private final String _rectangleVertexShaderCode = 
			"attribute vec4 a_Position;\n"
			+ "attribute vec4 a_Color;\n"
			+ "varying vec4 v_Color;\n"
			+ "void main() {\n"
			+ "		gl_Position = a_Position;\n"
			+ "		gl_PointSize = 20.0; \n"
			+ "		v_Color = a_Color;\n" // 平滑着色
			+ "}\n";

	private final String _rectangleFragmentShaderCode = 
			"precision mediump float;\n"
			+ "varying vec4 v_Color;\n"
			+ "void main() {\n"
			+ " gl_FragColor = v_Color;\n" 
			+ "}\n";

	private int loadShader(int type, String source) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		
		final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		if (compileStatus[0] == 0) { // 返回0 获取获取结果是0 都意味错误
            GLES20.glDeleteShader(shader);
            Log.w(TAG, "Compilation of shader failed");
		}
            
		return shader;
	}

}