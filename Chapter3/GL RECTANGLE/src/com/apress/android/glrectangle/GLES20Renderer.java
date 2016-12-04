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
	private int uNormal ;
 
	FloatBuffer vertexColorData;
	
	private int mHeight ;
	private int mWidth ; 

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
		
		uMatrixLocation=GLES20.glGetUniformLocation(_rectangleProgram,"u_Matrix");
		checkGlError("glGetUniformLocation u_Matrix");
	    if (uMatrixLocation == -1) {
            throw new RuntimeException("Could not get uniform uMatrixLocation  ");
        }
		uNormal = GLES20.glGetUniformLocation(_rectangleProgram, "u_normal");
		checkGlError("glGetUniformLocation a_normal");
        if (uNormal == -1) {
            throw new RuntimeException("Could not get uniform u_normal  ");
        }
		Log.d(TAG, "aPosHandle = " + aPosHandle + " aColorHandle " + aColorHandle + " uNormal " + uNormal);
	}

	/*
	 *	
	 *	平移矩阵
	 *	1			X_move
	 *		1		Y_move
	 *			1	Z_move
	 *				1
	 *	
	 *	单位矩阵
	 *	1
	 *		1
	 *			1
	 *				1
	 *	
	 *	正交投影矩阵
	 *	
	 *	android.opengl.Matrix.orthoM(
	 		float[] m,int mOffset,float left,float rigth,float bottom,float top,float near,float far)

			float[] m：目标数组，这个数组长度至少有16个元素，这样它才能存储正交投影矩阵
			int mOffset：结果矩阵起始的偏移值
			float left：X轴的最小范围 (范围都是归一化的!!)
			float right：X轴的最大范围
			float bottom：Y轴的最小范围
			float top：Y轴的最大范围
			float near：Z轴的最小范围
			float far：Z轴的最大范围
 			2/(left-right)									(left+right)/(left-right)
 							2/(top-bottom)					(top+bottom)/(top-bottom)
 											-2/(far-near)	(far+near)/(far-near)	
 																1
	 *
	 *	
	 */
	private final float[] projectionMatrix=new float[16];
	private int uMatrixLocation; // 正交投影
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		mHeight = height  ;
		mWidth =  width;
		Log.d(TAG , "surface change " + "h " +  mHeight + "w " + mWidth );

		
//		if(width>height){
//			float aspectRatio = (float)width / (float)height ;
//			android.opengl.Matrix.orthoM(projectionMatrix,0,
//					-aspectRatio, aspectRatio, 
//					-1f,1f,  		
//					-1f,1f);
//		}else{
//			float aspectRatio = (float)height / (float)width ;
//			android.opengl.Matrix.orthoM(projectionMatrix,0,
//					-1f,1f,			
//					-aspectRatio,aspectRatio,	
//					-1f,1f);
//		}
		float[] temp=new float[16];
		float[] modelMatrix=new float[16];
		android.opengl.Matrix.setIdentityM(modelMatrix, 0); // 模型矩阵
		android.opengl.Matrix.translateM(modelMatrix,0,0f,0.0f,-3f); // Z方向 移远-2.5
		android.opengl.Matrix.rotateM(modelMatrix,0,-45f,1f,0f,0f);	 // 沿X轴 旋转-60 (右手坐标规则 x轴正方向指向眼睛 逆时针为负度数)
		
		perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);// 视椎体从Z值为-1的位置开始，在Z值为-10的位置结束。
		android.opengl.Matrix.multiplyMM(temp,0,projectionMatrix,0,modelMatrix,0);
		System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
		//System.arraycopy(modelMatrix, 0, projectionMatrix, 0, modelMatrix.length);

	}
	
	public static void perspectiveM(float[] m,float yFovInDegress,float aspect,float n,float f){
		
		final float angleInRadians=(float)(yFovInDegress*Math.PI/180.0);
		final float a=(float)(1.0/Math.tan(angleInRadians/2.0)); 
		
		m[0]=a/aspect;
		m[1]=0f;
		m[2]=0f;
		m[3]=0f;


		m[4]=0f;
		m[5]=a;
		m[6]=0f;
		m[7]=0f;
		
		m[8]=0f;
		m[9]=0f;
		m[10]=-((f+n)/(f-n));
		m[11]=-1f;
		
		m[12]=0f;
		m[13]=0f;
		m[14]=-((2f*f*n)/(f-n));
		m[15]=0f;
	}

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(op + ": glError " + error);
        }
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
	
		// 只有attr需要enable  uniform不需要 !
		GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
		
		float normal = (mWidth * 1.0f) / (mHeight*1.0f) ;
		Log.d(TAG , "h " + mHeight + " w " + mWidth + " n " + normal );
		GLES20.glUniform1f(uNormal , normal );
		
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
			+ "uniform mat4 u_Matrix;\n"
			+ "uniform float u_normal;\n"
			+ "attribute vec4 a_Color;\n"
			+ "varying vec4 v_Color;\n"
			+ "void main() {\n"
			// 如果 u_normal 在程序中没有使用到 GPU编译后会移除改变量 
			// GLES20.glGetUniformLocation(_rectangleProgram, "u_normal"); 返回 -1 
			//+ "		gl_Position = a_Position * vec4(1,u_normal,1, 1);\n"
			+ "		vec4 temp = vec4(1,1,1,1) * vec4(1,u_normal,1, 1) ;\n"
			+ "		mat4 temp1 = u_Matrix ;\n"
			+ "		gl_Position = u_Matrix*a_Position ;\n" // 矩阵在前 向量在后
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